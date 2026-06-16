# Debug Session: users-list-empty
- **Status**: [OPEN]
- **Issue**: La vista de usuarios no muestra resultados aunque la base de datos contiene usuarios y el frontend debería ocultar solo al usuario autenticado.
- **Debug Server**: Pending
- **Log File**: .dbg/trae-debug-log-users-list-empty.ndjson

## Reproduction Steps
1. Iniciar sesión en el frontend.
2. Abrir la ruta `/users`.
3. Observar que la lista aparece vacía (`Total de usuarios: 0`).

## Hypotheses & Verification
| ID | Hypothesis | Likelihood | Effort | Evidence |
|----|------------|------------|--------|----------|
| A | La API de `GET /api/user` está devolviendo una lista vacía o un objeto con forma distinta a la que espera el frontend. | High | Low | Pending |
| B | El frontend está filtrando mal la lista y elimina todos los usuarios al excluir al usuario autenticado. | High | Low | Pending |
| C | El frontend está leyendo un campo equivocado (`users`, `data`, `content`) y por eso interpreta la respuesta como vacía. | High | Low | Pending |
| D | El request a `/api/user` se hace con paginación o credenciales incorrectas y la API responde distinto a lo esperado. | Med | Med | Pending |
| E | Hay caché en el frontend o reutilización de estado que deja una lista vacía aunque la API sí devuelve datos. | Med | Low | Pending |

## Log Evidence
- Evidencia estática confirmada en `api/src/main/java/com/asociaciondomitila/controller/UserController.java`: `GET /api/user` devuelve `ApiResponse<PageResponse<UserDto>>`.
- Evidencia estática confirmada en `api/src/main/java/com/asociaciondomitila/dto/PageResponse.java`: la colección paginada viaja en `items`, no en `users`.
- Evidencia estática confirmada en `frontend/components/users/users.component.js`: el frontend estaba leyendo `response.users` y `response.pagination`, por lo que una respuesta válida de backend se interpretaba como vacía.
- Instrumentación añadida en `frontend/components/users/users.component.js` para reportar request, forma de respuesta y estado final del mapeo si se reproduce de nuevo en navegador.

## Verification Conclusion
- Hipótesis A: **Rejected parcial**. La API no parece tener el contrato roto; devuelve una estructura paginada consistente.
- Hipótesis B: **Confirmed parcial**. El frontend no estaba excluyendo explícitamente al usuario autenticado.
- Hipótesis C: **Confirmed**. El frontend estaba leyendo propiedades incompatibles con el contrato actual (`response.users` vs `response.data.items`).
- Hipótesis D: **Pending**. Sigue existiendo una posible inconsistencia de permisos: el backend protege `GET /api/user` solo para `ADMIN`, mientras que el frontend permite `PREMIUM` en la vista.
- Hipótesis E: **Rejected** como causa principal del bug visible.
