package co.edu.usco.aprendizaje_usco.modulos.usuario.service;

import co.edu.usco.aprendizaje_usco.modulos.usuario.model.NombreRol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Rol;
import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Usuario;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.RolRepository;
import co.edu.usco.aprendizaje_usco.modulos.usuario.repository.UsuarioRepository;
import java.nio.charset.StandardCharsets;
import java.util.Optional;
import java.util.regex.Pattern;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.StringUtils;

@Component
public class BootstrapAdminRunner implements ApplicationRunner {

    private static final Logger LOGGER = LoggerFactory.getLogger(BootstrapAdminRunner.class);
    private static final Pattern EMAIL_PATTERN = Pattern.compile("^[^\\s@]+@[^\\s@]+\\.[^\\s@]+$");
    private static final Pattern PASSWORD_PATTERN = Pattern.compile("^(?=.*\\p{L})(?=.*\\d).+$");
    private static final int MIN_PASSWORD_LENGTH = 12;
    private static final int MAX_BCRYPT_BYTES = 72;

    private final UsuarioRepository usuarioRepository;
    private final RolRepository rolRepository;
    private final PasswordEncoder passwordEncoder;
    private final String adminEmail;
    private final String adminPassword;
    private final String adminNames;
    private final String adminSurnames;

    public BootstrapAdminRunner(
            UsuarioRepository usuarioRepository,
            RolRepository rolRepository,
            PasswordEncoder passwordEncoder,
            @Value("${app.bootstrap-admin.email:}") String adminEmail,
            @Value("${app.bootstrap-admin.password:}") String adminPassword,
            @Value("${app.bootstrap-admin.nombres:Administrador}") String adminNames,
            @Value("${app.bootstrap-admin.apellidos:Sistema}") String adminSurnames) {
        this.usuarioRepository = usuarioRepository;
        this.rolRepository = rolRepository;
        this.passwordEncoder = passwordEncoder;
        this.adminEmail = adminEmail;
        this.adminPassword = adminPassword;
        this.adminNames = adminNames;
        this.adminSurnames = adminSurnames;
    }

    @Override
    @Transactional
    public void run(ApplicationArguments args) {
        boolean hasEmail = StringUtils.hasText(adminEmail);
        boolean hasPassword = StringUtils.hasText(adminPassword);
        if (!hasEmail && !hasPassword) {
            LOGGER.info("Bootstrap de administrador omitido: configura BOOTSTRAP_ADMIN_EMAIL y "
                    + "BOOTSTRAP_ADMIN_PASSWORD para habilitarlo.");
            return;
        }
        if (!hasEmail || !hasPassword) {
            throw new IllegalStateException("Para crear el administrador inicial debes configurar tanto "
                    + "BOOTSTRAP_ADMIN_EMAIL como BOOTSTRAP_ADMIN_PASSWORD.");
        }

        String correo = Usuario.normalizarCorreo(adminEmail);
        validarConfiguracion(correo, adminPassword, adminNames, adminSurnames);

        Rol rolAdmin = rolRepository.findByNombreParaBloquear(NombreRol.ADMIN)
                .orElseThrow(() -> new IllegalStateException("Falta el rol ADMIN en la base de datos."));
        Optional<Usuario> existente = usuarioRepository.findByCorreo(correo);
        if (existente.isPresent()) {
            if (existente.get().getRol().getNombre() != NombreRol.ADMIN) {
                throw new IllegalStateException("BOOTSTRAP_ADMIN_EMAIL ya pertenece a un usuario que no es ADMIN.");
            }
            LOGGER.info("El administrador bootstrap ya existe; no se modificó la cuenta.");
            return;
        }

        Usuario admin = new Usuario(adminNames.trim(), adminSurnames.trim(), correo,
                passwordEncoder.encode(adminPassword), rolAdmin);
        usuarioRepository.saveAndFlush(admin);
        LOGGER.info("Se creó el administrador bootstrap configurado.");
    }

    private static void validarConfiguracion(String correo, String password, String nombres, String apellidos) {
        if (correo.length() > 150 || !EMAIL_PATTERN.matcher(correo).matches()) {
            throw new IllegalStateException("BOOTSTRAP_ADMIN_EMAIL debe ser un correo válido de máximo 150 caracteres.");
        }
        if (password.length() < MIN_PASSWORD_LENGTH
                || password.getBytes(StandardCharsets.UTF_8).length > MAX_BCRYPT_BYTES
                || !PASSWORD_PATTERN.matcher(password).matches()) {
            throw new IllegalStateException("BOOTSTRAP_ADMIN_PASSWORD debe tener al menos 12 caracteres, "
                    + "incluir una letra y un número, y ocupar como máximo 72 bytes UTF-8.");
        }
        validarNombre(nombres, "BOOTSTRAP_ADMIN_NAMES");
        validarNombre(apellidos, "BOOTSTRAP_ADMIN_SURNAMES");
    }

    private static void validarNombre(String valor, String propiedad) {
        if (!StringUtils.hasText(valor) || valor.trim().length() > 100) {
            throw new IllegalStateException(propiedad + " debe tener entre 1 y 100 caracteres.");
        }
    }

}