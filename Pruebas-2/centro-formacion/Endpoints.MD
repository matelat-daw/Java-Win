## Alumnos

- **GET** `/api/v1/alumnos` → Devuelve solo alumnos (id, nombre, email).
- **GET** `/api/v1/alumnos/en-cursos` → Devuelve alumnos con la lista de sus cursos.
- **POST** `/api/v1/alumnos` → Crea un nuevo alumno.
  - Body ejemplo (JSON):
    ```json
    {
      "nombre": "Ana Pérez",
      "email": "ana.perez@example.com"
    }
    ```
  - Respuestas: `201 Created` con el `AlumnoResponse` creado; `400 Bad Request` si falta `email`; `409 Conflict` si ya existe un alumno con ese email.
- **DELETE** `/api/v1/alumnos/{alumnoId}` → Elimina el alumno indicado.
  - Respuestas: `204 No Content` si se elimina correctamente; `404 Not Found` si no existe.
  - Comportamiento: antes de borrar se desasocian las relaciones ManyToMany con cursos (no se borran los cursos).
  - Implementado en `AlumnoController`.

## Cursos

- **GET** `/api/v1/cursos` → Devuelve solo cursos (id, titulo, modalidad, duracion).
- **GET** `/api/v1/cursos/con-alumnos` → Devuelve cursos con la lista de sus alumnos.
- **POST** `/api/v1/cursos` → Crea un nuevo curso.
  - Body ejemplo (JSON):
    ```json
    {
      "titulo": "Avanzado de Java",
      "modalidad": "PRESENCIAL",
      "duracion": 45
    }
    ```
  - Respuestas: `201 Created` con el `CursoResponse` creado; `400 Bad Request` si falta `titulo`; `409 Conflict` si ya existe un curso con ese título.
- **DELETE** `/api/v1/cursos/{cursoId}` → Elimina el curso indicado.
  - Respuestas: `204 No Content` si se elimina correctamente; `404 Not Found` si no existe.
  - Comportamiento: antes de borrar se desasocian los alumnos del curso (no se borran los alumnos).
  - Implementado en `CursoController`.

## Inscripciones / Matriculaciones

- **POST** `/api/v1/alumnos/{alumnoId}/cursos/{cursoId}` → Inscribe al alumno en el curso.
  - No necesita body.
  - Respuestas: `200 OK` con el `AlumnoResponse` actualizado con sus cursos; `404 Not Found` si alumno o curso no existen; `409 Conflict` si ya estaba inscrito.
- **DELETE** `/api/v1/alumnos/{alumnoId}/cursos/{cursoId}` → Quita la inscripción del alumno en el curso.
  - Respuestas: `204 No Content` si se desasocia correctamente; `404 Not Found` si no existe la relación, el alumno o el curso.