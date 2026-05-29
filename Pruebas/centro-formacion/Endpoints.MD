- Alumnos

- GET /api/v1/alumnos → Devuelve solo alumnos ( id , nombre , email ).
- GET /api/v1/alumnos/en-cursos → Devuelve alumnos con sus cursos.
- DELETE /api/v1/alumnos/1 → Elimina el Alumno con ID 1.
Implementado en AlumnoController .


- Cursos

- GET /api/v1/cursos → Devuelve solo cursos ( id , titulo , modalidad , duracion )
- GET /api/v1/cursos/con-alumnos → Devuelve cursos con sus alumnos.
- DELETE /api/v1/cursoss/1 → Elimina el Curso con ID 1.
Implementado en CursoController .