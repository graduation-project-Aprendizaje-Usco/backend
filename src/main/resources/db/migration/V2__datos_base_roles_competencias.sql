-- =====================================================================
-- V2 · Datos base requeridos en TODOS los entornos
-- (desarrollo, pruebas, staging y producción).
-- Los usuarios de prueba NO van aquí: están en db/dev-data.
-- =====================================================================

INSERT INTO rol (nombre) VALUES
    ('ESTUDIANTE'),
    ('DOCENTE'),
    ('ADMIN');

-- Dimensiones de la Prueba Diagnóstica de Competencias Matemáticas (cap. 3 del informe).
INSERT INTO competencia (codigo, nombre, descripcion) VALUES
    ('ALGEBRAICA',
     'Competencia algebraica',
     'Manipulación de expresiones algebraicas: factorización, simplificación y resolución de ecuaciones e inecuaciones.'),
    ('LOGICA',
     'Razonamiento lógico',
     'Interpretación de proposiciones, condicionales y cuantificadores, y construcción de argumentos válidos.'),
    ('FUNCIONAL',
     'Comprensión funcional',
     'Interpretación de funciones y sus representaciones: dominio, rango, gráficas y relación entre parámetros y comportamiento.');
