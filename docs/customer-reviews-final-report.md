# Customer Reviews / Valoraciones de clientes — Final Report

> UPM MIW BETCA / GOA
> Entrega: Customer Reviews
> Rama principal de trabajo: `feature/reviews` en los tres repositorios

---

## 1. Resumen

Esta práctica implementa la funcionalidad **Customer Reviews / Valoraciones de clientes** conforme a los requisitos del enunciado.

El usuario con rol **CUSTOMER** puede, desde la interfaz web de goa-front, buscar una Hoja de Encargo por su identificador y asignarle una valoración de **1 a 5 estrellas** junto con un **comentario de texto libre**. El mismo endpoint sirve tanto para crear una valoración nueva como para actualizar la existente (upsert semántico). El acceso está protegido mediante **OAuth2/OIDC con JWT**; el `userId` se extrae automáticamente del claim `sub` del token, por lo que no es necesario enviarlo en el cuerpo de la petición. La unicidad de una valoración por usuario y Hoja de Encargo se garantiza a nivel de base de datos mediante un **índice compuesto único** `(userId, letterId)` en MongoDB.

---

## 2. Requisitos del enunciado

| Requisito | Estado | Evidencia |
|---|---|---|
| stars con valores 1..5 | ✅ Completado | `ReviewService.validateStars()`, DTOs `@Min(1)` / `@Max(5)` |
| elemento visual estrellas | ✅ Completado | `reviews.component.html` usa `<mat-icon>star` / `star_border` |
| opinion (comentario de texto) | ✅ Completado | `ReviewUpdateDto.opinion` / `ReviewCreateDto.opinion` con `@NotBlank` |
| CUSTOMER puede crear valoración | ✅ Completado | `@PreAuthorize("hasRole('CUSTOMER')")` en todos los endpoints |
| CUSTOMER puede modificar su valoración | ✅ Completado | `PUT /api/reviews/{letterId}` y `POST /api/reviews` con upsert |
| Trabajar en goa-sandbox | ✅ Completado | `ReviewResource`, `ReviewService`, `ReviewEntity`, tests |
| Trabajar en goa-front con feature/reviews | ✅ Completado | Rama `feature/reviews`, todos los merges a dicha rama |
| goa-front debe usar workflow feature/reviews + issues | ✅ Completado | 5 issue branches mergeadas a `feature/reviews` |
| El usuario solo valora sus propias Hojas de Encargo | ⚠️ Parcialmente | Aislamiento por `userId` + índice único compuesto; **sin validación de ownership contra goa-engagement** — ver §10 |
| goa-gateway route para goa-sandbox | ✅ Completado (local) | `lb://goa-sandbox` + `RewritePath`; ver §6 |

---

## 3. Repositorios y ramas

| Repositorio | Rama | Último commit | push a origin | Validación |
|---|---|---|---|---|
| `goa-sandbox` | `feature/reviews` | `ddb0516` — Merge issue/reviews-unique-index | ✅ Push OK | `mvn test` — 32 tests, BUILD SUCCESS |
| `goa-front` | `feature/reviews` | `31bdddc` — Merge issue/reviews-ui-modernization | ✅ Push OK | `npm run build-prod` — BUILD SUCCESS, warnings preexistentes |
| `goa-gateway` | `feature/reviews` | `aeb60a7` — Merge issue/reviews-gateway-final-route | ❌ READ permission en el repo — no se puede hacer push | `mvn test` — 1 test, BUILD SUCCESS |

**Nota sobre goa-gateway:** la implementación local está completa y validada, pero no se ha podido hacer push a `origin/feature/reviews` porque el usuario actual tiene permiso de solo lectura (`viewerPermission: READ`) en el repositorio. Se proporcionan como替代交付物:
- `goa-gateway-feature-reviews.patch`
- `goa-gateway-feature-reviews.diff`

---

## 4. Implementación backend en goa-sandbox

### 4.1 API REST

| Método | Path | Rol | Descripción |
|---|---|---|---|
| `POST` | `/api/reviews` | CUSTOMER | Crea o actualiza (upsert) la valoración del usuario para la Hoja indicada |
| `PUT` | `/api/reviews/{letterId}` | CUSTOMER | Actualiza la valoración existente del usuario para la Hoja |
| `GET` | `/api/reviews/{letterId}` | CUSTOMER | Devuelve la valoración del usuario para la Hoja, o 404 si no existe |

### 4.2 Componentes principales

| Clase | Responsabilidad |
|---|---|
| `ReviewResource` | Controlador REST; extrae `userId` del claim `sub` del JWT (`@AuthenticationPrincipal Jwt`); delega en `ReviewService` |
| `ReviewService` | Lógica de negocio: upsert, lectura, validaciones (stars, opinion, letterId, userId) |
| `Review` | Modelo de dominio con campos: `id`, `userId`, `letterId`, `stars` (1..5), `opinion`, `createdAt`, `updatedAt` |
| `ReviewCreateDto` / `ReviewUpdateDto` | DTOs de entrada con validación Bean Validation (`@NotBlank`, `@Min`, `@Max`) |
| `ReviewEntity` | Entidad MongoDB con `@Document(collection = "reviews")` y `@CompoundIndex(name = "userId_letterId_unique", def = "{'userId': 1, 'letterId': 1}", unique = true)` |
| `ReviewMongoRepository` | Repositorio Spring Data MongoDB |
| `ReviewPersistenceMongodb` | Adapter Ports-out que implementa `ReviewRepository` |
| `ReviewRepository` | Puerto de salida del dominio |

### 4.3 Seguridad

- **Resource Server OAuth2**: `spring-boot-starter-oauth2-resource-server`valida el JWT contra el issuer configurado.
- **Roles**: el claim `roles` del JWT se mapea a autoridades Spring Security con prefijo `ROLE_`.
- **AUTHORITY**: `ResourceServerConfig.jwtAuthenticationConverter()`convierte `roles: ["CUSTOMER"]` → `ROLE_CUSTOMER`.
- **userId**: se obtiene exclusivamente de `jwt.getClaimAsString("sub")`; nunca del cuerpo de la petición.
- **CUSTOMER**: todos los endpoints REST están protegidos con `@PreAuthorize("hasRole('CUSTOMER')")`.

---

## 5. Implementación frontend en goa-front

### 5.1 Ruta y guard

- **`/home/reviews`** — registrada en `home.routes.ts` con `data: { roles: [Role.CUSTOMER] }`.
- `RoleGuard` comprueba que el usuario tiene rol `CUSTOMER` antes de permitir el acceso.

### 5.2 Componentes

| Componente / Servicio | Función |
|---|---|
| `ReviewsComponent` | Componente principal de la página `/home/reviews` |
| `ReviewsService` | Llamadas HTTP a `GET/PUT/POST /reviews/{letterId}` vía `ENDPOINTS.reviews` |
| `ENDPOINTS.reviews` | Apunta a `${REST_SANDBOX}/reviews` → `http://localhost:8080/api/goa-sandbox/reviews` |
| `environment.REST_SANDBOX` | `http://localhost:8080/api/goa-sandbox` (a través de goa-gateway) |

### 5.3 Interfaz de usuario

La página `/home/reviews` proporciona:

1. **Campo de búsqueda**: input para introducir el identificador de Hoja de Encargo (`letterId`).
2. **Tu valoración**: información de la valoración actual del usuario para esa Hoja (stars + opinion), o mensaje de ausencia.
3. **Selector visual de estrellas** (`mat-icon`):radio group accessible con 5 opciones ★, representando valores 1..5; la estrella actual se muestra en color ámbar, las no seleccionadas en gris.
4. **Badge dinámico**: muestra "Creando" o "Editando" según el estado actual.
5. **Textarea opinion**: campo de comentario de texto libre con validación de no vacío.
6. **Botón de envío**: llama a la API según el modo crear/editar.
7. **MatSnackBar feedback**: notificación de éxito o error tras la llamada HTTP.
8. **Responsive y accessible**: usa radiogroup/radio para las estrellas y muestra texto alternativo.

---

## 6. Implementación en goa-gateway

### 6.1 Route final en `application.yml`

```yaml
- id: goa-sandbox
  uri: lb://goa-sandbox
  predicates:
    - Path=/api/goa-sandbox/**
  filters:
    - RewritePath=/api/goa-sandbox/(?<segment>.*), /api/${segment}
```

**Decisión de diseño:** se utiliza `lb://goa-sandbox` (service discovery via Eureka) en lugar de una URL fija `http://localhost:8088`, y `RewritePath` en lugar de `StripPrefix=2`. La razón es que `goa-sandbox` tiene configurado `server.servlet.context-path=/api`; por tanto, las peticiones que llegan a `/api/goa-sandbox/reviews/...` deben reescribirse a `/api/reviews/...` al ser entregadas al servicio downstream. `StripPrefix=2` eliminaría los dos primeros segmentos del path (`/api` y `goa-sandbox`), pero el servicio esperaría recibir `/reviews/...` sin el prefijo `/api`, lo cual provocaría un **404 en el endpoint interno**. `RewritePath` realiza la reescritura correcta.

### 6.2 Estado de entrega

| Aspecto | Estado |
|---|---|
| Implementación local | ✅ Completa |
| `mvn test` | ✅ BUILD SUCCESS, 1 test |
| Patch disponible | ✅ `goa-gateway-feature-reviews.patch` (2 888 bytes) |
| Diff disponible | ✅ `goa-gateway-feature-reviews.diff` (618 bytes) |
| push a origin | ❌ Bloqueado por `viewerPermission: READ` en el repositorio |

---

## 7. Seguridad

- **OAuth2 Resource Server**: validación de firma y caducidad del JWT en cada petición.
- **JWT sin secretos en código**: el issuer URI se configura en `application.yml`mediante variable de entorno; ningún token real está commitado.
- **Rol CUSTOMER como barrera**: los endpoints de review solo aceptan tokens con `ROLE_CUSTOMER` en las autoridades del JWT.
- **`userId` del JWT sub**: no se acepta `userId` del cuerpo de la petición; se extrae siempre del token autenticado.
- **Aislamiento por `userId` + `letterId`**: el índice compuesto único garantiza que un mismo usuario no pueda tener más de una valoración para la misma Hoja.
- **Sin acceso directo entre bases de datos**: goa-sandbox no conecta a la base de datos de goa-engagement.
- **Datos sensibles no commitados**: no hay tokens, contraseñas, secretos ni Authorization headers reales en los repositorios.

---

## 8. Tests y validación

| Repositorio | Comando | Resultado | Detalle | Evidence |
|---|---|---|---|---|
| goa-sandbox | `mvn test` | ✅ BUILD SUCCESS | 32 tests: ReviewResourceTest 15, ComplaintServiceTest 1, ReviewServiceTest 16; 0 failures/errors/skipped | `evidence/goa-sandbox-mvn-test.txt` |
| goa-front | `npm run build-prod` | ✅ BUILD SUCCESS | Generación correcta; 2 warnings preexistentes (bundle budget + chatbot CSS budget, no errors) | `evidence/goa-front-build-prod.txt` |
| goa-gateway | `mvn test` | ✅ BUILD SUCCESS | 1 test; 0 failures/errors/skipped | `evidence/goa-gateway-mvn-test.txt` |

---

## 9. Validación manual en navegador

Los siguientes flujos fueron validados en entorno local (Chrome DevTools, red y consola):

1. **Login como CUSTOMER**: se autentica en goa-user y se recibe JWT con rol CUSTOMER y claim `sub`.
2. **Navegación a `/home/reviews`**: el RoleGuard permite el acceso; la página carga sin CORS ni 401.
3. **Crear valoración**: se introduce un `letterId` válido, se seleccionan 5 estrellas y un opinion, se pulsa enviar → `POST /api/reviews` → respuesta 200 con JSON del review creado.
4. **Recargar valoración existente**: se reutiliza el mismo `letterId` → `GET /api/reviews/{letterId}` → respuesta 200 con los datos guardados.
5. **Actualizar valoración**: se cambia a 3 estrellas y opinion diferente → `PUT /api/reviews/{letterId}` → respuesta 200 con datos actualizados.
6. **Tráfico de red**: todas las llamadas atraviesan `http://localhost:8080/api/goa-sandbox/reviews/...` (goa-gateway); no se observa 401, 403, 404, 500 ni 503.
7. **Errores esperados**: se verificaron respuestas 400 para stars fuera de rango, opinion vacía y `letterId`en blanco.

---

## 10. Limitaciones conocidas

### 10.1 Validación de ownership contra goa-engagement

> **Limitación conocida:**
>
> La implementación actual asocia cada valoración al usuario autenticado mediante el claim `sub` del JWT y al identificador de Hoja de Encargo introducido por el usuario. No se ha integrado una validación adicional contra `goa-engagement` para comprobar que dicho identificador pertenece efectivamente al CUSTOMER autenticado, ya que `goa-engagement` no expone una API protegida con rol `CUSTOMER` que permita listar las Hojas de Encargo del usuario autenticado ni comprobar el ownership de una Hoja concreta.
>
> El endpoint `GET /engagement-letters` de `goa-engagement` requiere roles de administración, gestión y operación (`ADMIN_MANAGER_OPERATOR`), y `EngagementLetterFindCriteria` no dispone de un filtro por `customer` o `sub`. Los endpoints públicos `/read-engagement-letter/{urlId}/{token}` y `/sign-engagement-letter/{urlId}/{token}` están diseñados para enlaces con `urlId` y `token` y no son aplicables a una validación de ownership basada en JWT. Además, los identificadores internos de `EngagementLetter` son UUID, mientras que el API de Reviews recibe `letterId` como cadena libre.
>
> Por tanto, la restricción de que el usuario solo valore sus propias Hojas de Encargo queda **parcialmente cubierta** por el aislamiento de las valoraciones por usuario y por el índice compuesto único `(userId, letterId)`, pero **no por una validación completa de ownership contra goa-engagement**. Para resolverlo sería necesario que `goa-engagement` exponga un endpoint `CUSTOMER`-scoped con autenticación JWT y filtro por `sub`, lo cual queda fuera del alcance de esta práctica.

---

## 11. Cómo ejecutar en local

### 11.1 Servicios necesarios (orden de arranque)

| Servicio | Puerto | Rol |
|---|---|---|
| MongoDB | 27017 | Base de datos de goa-sandbox |
| goa-eureka | 8761 | Service discovery |
| goa-user | 8080 (via gateway) / directo 8081 | Autenticación OAuth2/OIDC |
| goa-sandbox | 8088 directo / a través de gateway 8080 | Microservicio de reviews |
| goa-gateway | 8080 | API gateway |
| goa-front | 4200 | Aplicación Angular |

### 11.2 URLs relevantes

| URL | Descripción |
|---|---|
| `http://localhost:4200` | Aplicación goa-front |
| `http://localhost:4200/home/reviews` | Página de Customer Reviews |
| `http://localhost:8080/api/goa-sandbox/reviews/{letterId}` | API a través del gateway |
| `http://localhost:8088/api/reviews/{letterId}` | API goa-sandbox (acceso directo, sin gateway) |
| `http://localhost:8088/api/actuator/health` | Health check de goa-sandbox |
| `http://localhost:8761` | Eureka Dashboard |

### 11.3 usuario de prueba

Usar las credenciales de CUSTOMER configuradas en el seeder de goa-user para obtener el JWT con rol `CUSTOMER` y `sub` claim.

---

## 12. Nota sobre datos sensibles

Durante la implementación de Customer Reviews no se han añadido tokens JWT,
cabeceras Authorization, contraseñas, GitHub Personal Access Tokens ni secretos
reales en los ficheros modificados o generados para esta feature.

Los artefactos generados para la validación de esta entrega (`evidence/*.txt`),
el informe final y los ficheros `goa-gateway-feature-reviews.patch` /
`goa-gateway-feature-reviews.diff` no contienen credenciales reales.

Durante la revisión final se detectó un riesgo preexistente en el repositorio:
un fichero histórico bajo `docs/` contiene un token JWT hardcodeado utilizado
por un script de carga de plantillas. Este fichero no ha sido creado ni
modificado por la implementación de Customer Reviews y no se utiliza en la
feature desarrollada. Por seguridad, no se reproduce aquí el valor del token.
Se recomienda comunicar este hallazgo a los mantenedores del repositorio para
que evalúen la rotación de credenciales, la eliminación del token del fichero
y, si procede, la limpieza del historial de Git.

Por tanto, la entrega de Customer Reviews se considera limpia respecto a
secretos introducidos por esta feature, pero existe un riesgo sensible
preexistente en el repositorio original que debe tratarse fuera del alcance de
esta implementación.

---

## 13. Commit history en feature/reviews

### goa-sandbox — commits mergeados a `feature/reviews`

| Commit | Descripción |
|---|---|
| `ddb0516` | Merge `issue/reviews-unique-index` — índice compuesto único en `ReviewEntity` |
| `65a832a` | fix: enforce unique review per user and letter |
| `f9e5350` | Merge `issue/reviews-sandbox-role-authorities-fix` |
| `2da6c8c` | fix: complete review resource integration wiring |
| `4909ae7` | Merge `issue/reviews-sandbox-startup-fix` |
| `48a89d7` | fix: align api exception handler package path |
| `7152bc7` | Merge `issue/reviews-resource-tests` |
| `948d912` | test: add review resource tests |
| `415a9da` | Merge `issue/reviews-service-resource` |
| `91e9bce` | test: add review service tests |
| `7787f8d` | fix: return not found for missing review |

### goa-front — commits mergeados a `feature/reviews`

| Commit | Descripción |
|---|---|
| `31bdddc` | Merge `issue/reviews-ui-modernization` — UI modernizada |
| `be3aca7` | feat: polish customer reviews page |
| `512950d` | Merge `issue/reviews-front-auth-hydration` |
| `517d4ad` | fix: hydrate auth state before app startup |
| `f02a4bf` | Merge `issue/reviews-star-rating` |
| `076aca5` | feat: replace review stars select with visual star rating |
| `49bc4e3` | Merge `issue/reviews-page-route` |
| `eb7ab8b` | feat: add customer reviews page route |

### goa-gateway — commits mergeados a `feature/reviews` (local)

| Commit | Descripción |
|---|---|
| `aeb60a7` | Merge `issue/reviews-gateway-final-route` — route final con Eureka discovery |
| `4387f55` | fix: use service discovery for sandbox gateway route |
| `6c28fac` | Merge `issue/reviews-gateway-route` |
| `01e685e` | fix: route sandbox reviews through gateway |
| `7fb3695` | feat: add gateway route for goa-sandbox |
