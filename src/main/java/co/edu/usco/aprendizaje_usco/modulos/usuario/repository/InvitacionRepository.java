package co.edu.usco.aprendizaje_usco.modulos.usuario.repository;

import co.edu.usco.aprendizaje_usco.modulos.usuario.model.Invitacion;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface InvitacionRepository extends JpaRepository<Invitacion, Long> {

    Optional<Invitacion> findByTokenHash(String tokenHash);

    Optional<Invitacion> findByGrupoIdAndCorreo(Long grupoId, String correo);

    @Query("""
            select i from Invitacion i
            where i.grupo.id = :grupoId
            order by i.apellidos, i.nombres""")
    List<Invitacion> listarPorGrupo(@Param("grupoId") Long grupoId);
}
