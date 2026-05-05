# Customer Reviews User and EngagementLetter Audit

> Audit date: 2026-05-05
> Auditor: M1.2 only — no business code written
> Scope: goa-sandbox + goa-front, feature/reviews

---

## Current branches and status

### goa-sandbox
- branch: `feature/reviews`
- status: `nothing to commit, working tree clean`
- latest commit: `4b6ea9d docs: audit goa-sandbox architecture for Review implementation`
- ahead/behind: **ahead of origin/feature/reviews by 1 commit** (audit doc, not pushed)

### goa-front
- branch: `feature/reviews`
- status: `nothing to commit, working tree clean`
- latest commit: `c46b3de version: 1.12.0-SNAPSHOT`
- ahead/behind: **up to date with origin/develop** (no local commits on feature/reviews)

---

## Assignment identity model

From teacher's email:

```
interface Review { user: User; letter: EngagementLetter; stars: number; opinion: string }
```

**Important clarification:** This is the *API/persistence interface as described by the teacher*, not the required internal architecture. Based on goa-sandbox existing patterns:
- `Review.user` → **should be stored as a `String userId`** (mobile string), NOT as a full User object
- `Review.letter` → **should be stored as a `String letterId`** (barcode), NOT as a full EngagementLetter object
- This matches the existing Complaint pattern where `userId` and `barcode` are stored as strings, not full objects

---

## goa-sandbox User evidence

### JWT / Authentication

| File | Evidence |
|---|---|
| `configurations/ResourceServerConfig.java` | JWT Bearer token via `spring-boot-starter-oauth2-resource-server`. Roles claim name `"roles"` with `ROLE_` prefix. |
| `adapter/in/resources/ComplaintResource.java` | `@AuthenticationPrincipal Jwt jwt` + `jwt.getClaimAsString("username")` extracts user identity |
| `configurations/FeignConfig.java` | `SecurityContextHolder.getContext().getAuthentication()` for Feign token propagation |
| `test/ComplaintResourceIT.java` | JWT mock: `jwt().jwt(builder -> builder.claim("username", "6").subject("6")).authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))` |

**Critical finding — `username` claim IS the mobile string:**
- Test JWT uses `"username": "6"` (the mobile number "6" is a simplified test value)
- `jwt.getClaimAsString("username")` returns the mobile number
- `UserSnapshot.id` is `UUID` but the JWT `username` claim is a **mobile string**
- goa-sandbox does NOT store full User objects — it uses the mobile string as the identifier

### User / Identity model

| File | Evidence |
|---|---|
| `domain/model/external/UserSnapshot.java` | `UUID id`, `String mobile`, `String firstName`, `String familyName`, `String email` — Lombok `@Data @Builder` |
| `adapter/out/user/feign/GoaUserClient.java` | Feign client: `readUserById(UUID)`, `readUserByMobile(String)`, `findUser(attribute)` |
| `domain/ports/out/user/UserFinder.java` | Port interface: `readById(UUID)`, `readByMobile(String)`, `find(String)` |

**Summary:** `userId` in goa-sandbox is the **mobile string** (e.g., `"600000000"`). Not UUID.

### Roles

| Role | Usage |
|---|---|
| `ROLE_CUSTOMER` | Used in `@PreAuthorize("hasRole('CUSTOMER') or hasRole('MANAGER') or hasRole('ADMIN')")` on ComplaintResource.create() |
| `ROLE_ADMIN`, `ROLE_MANAGER`, `ROLE_OPERATOR`, `ROLE_CUSTOMER` | Defined in goa-front `role.model.ts` |

### Current user extraction pattern

```java
@PreAuthorize("hasRole('CUSTOMER')")
public Complaint create(@Valid @RequestBody Complaint complaint, @AuthenticationPrincipal Jwt jwt) {
    String userId = jwt.getClaimAsString("username"); // ← mobile string
    complaint.setUserId(userId);
    return this.complaintService.create(complaint);
}
```

---

## goa-sandbox EngagementLetter / Hoja de encargo evidence

**Status: NOT FOUND in goa-sandbox source code.**

- No `EngagementLetter.java`, `Letter.java`, `HojaDeEncargo.java`, or similar class exists in goa-sandbox
- No `barcode` field used as an EngagementLetter identifier in goa-sandbox
- The existing `Complaint` entity has a `barcode` field described as "Hoja de Encargo" in comments, but this is the complaint's reference, not an EngagementLetter entity
- No Feign client for `goa-engagement` exists in goa-sandbox
- **goa-sandbox does NOT own EngagementLetter data and cannot directly query goa-engagement**

**Implication for Review:**
- Review's `letterId` field must be a `String` identifier only
- Whether it is called `letterId` or `barcode` needs confirmation — **not confirmed**
- The most likely identifier is the `EngagementLetter.id` (UUID string) as returned by `goa-engagement` API
- Cannot validate letter ownership from within goa-sandbox without a Feign client to goa-engagement

---

## goa-sandbox access-control evidence

| Pattern | Found | File |
|---|---|---|
| `@PreAuthorize("hasRole('CUSTOMER')")` | ✅ Yes | `ComplaintResource.java` line 25 |
| `@AuthenticationPrincipal Jwt jwt` | ✅ Yes | `ComplaintResource.java` line 26 |
| JWT `username` claim extraction | ✅ Yes | `jwt.getClaimAsString("username")` → mobile string |
| User can only access their own resources | ⚠️ Partial | Complaint stores `userId` in domain object, but no explicit ownership check in service |
| Feign token propagation | ✅ Yes | `FeignConfig.java` injects JWT into outbound Feign calls |
| `ROLE_CUSTOMER` role guard in frontend | ✅ Yes | `role.guard.ts` + `RoleGuard` in routes |

**Ownership check in ComplaintService:** Not explicitly present. The service sets `userId` from JWT and persists it. There is no `findByUserId` in the repository. Each complaint carries its own `userId` field.

**Safe source for current customer identity (confirmed):**
```java
@AuthenticationPrincipal Jwt jwt
String userId = jwt.getClaimAsString("username"); // mobile string
```

---

## goa-front User evidence

### Auth / OIDC

| File | Evidence |
|---|---|
| `core/auth/auth.service.ts` | Uses `OidcSecurityService` from `angular-auth-oidc-client`. Stores `name`, `mobile`, `roles`. |
| `features/auth/pages/login-callback.component.ts` | `oidcSecurityService.checkAuth()` → extracts: `data['name']`, `data['sub']` → `mobile`, `data['roles']` |
| `features/home/pages/home.component.ts` | `authService.mobile` → used to read user profile via `UserService.read(auth.mobile)` |
| `core/auth/models/role.model.ts` | Enum `Role { ADMIN, MANAGER, OPERATOR, CUSTOMER }` |
| `core/auth/role.guard.ts` | `CanActivate` guard using `auth.hasRoles(roles)` |
| `shared/models/user.model.ts` | `User` interface: `mobile: string`, `firstName`, `familyName`, `email`, `role?: Role` |

**Current user source in goa-front (confirmed):**
```typescript
// From login-callback.component.ts
this.oidcSecurityService.getPayloadFromAccessToken().subscribe(data => {
    this.authService.name = data['name'];
    this.authService.mobile = data['sub'];  // ← mobile IS 'sub' claim
    this.authService.roles = data['roles'];
});
```

**JWT claims mapping (confirmed):**
- `sub` → mobile
- `name` → full name
- `roles` → role string

### User service

| File | Evidence |
|---|---|
| `features/shared/services/shared-user.service.ts` | `read(mobile: string): Observable<User>` via `GET /users/{mobile}` |
| `features/home/users/user.service.ts` | Used by home component for profile editing |

---

## goa-front EngagementLetter / Hoja de encargo evidence

### EngagementLetter model and service (fully present)

| File | Evidence |
|---|---|
| `features/home/engagement-letter/models/engagement-letter.model.ts` | `EngagementLetter` interface with `id?: string`, `budgetOnly`, `owner?: User`, `legalProcedures`, `paymentMethods`, etc. |
| `features/home/engagement-letter/engagement-letter.service.ts` | `create()`, `read(id)`, `update(id)`, `delete(id)`, `search(criteria)`, `createAccessLink()` |
| `features/home/engagement-letter/models/engagement-letter-find-criteria.model.ts` | `opened?: boolean`, `budgetOnly?: boolean`, `client?: string`, `legalProcedureTitle?: string`, `taskTitle?: string` |
| `features/home/engagement-letter/pages/engagement-letters.component.ts` | `EngagementLettersComponent` — full CRUD list page. `deleteVisibility = auth.isAdmin()`. Only ADMIN/MANAGER/OPERATOR can access (RoleGuard). |
| `features/home/engagement-letter/pages/engagement-letter-form.component.ts` | `EngagementLetterFormComponent` for create/edit. Owner selected via `SearchByUserComponent`. |

**EngagementLetter.id is a `string` (UUID).**

### Customer-specific EngagementLetter access (CRITICAL — already exists!)

| File | Evidence |
|---|---|
| `features/customer/engagement-letter/read/read-engagement-letter.service.ts` | Customer can read their own engagement letters via `GET /engagement-letters/{scope}/{urlId}/{token}` (public access via token, no JWT required) |
| `features/customer/engagement-letter/read/pages/read-engagement-letter.component.ts` | `ReadEngagementLetterComponent` — customer-facing component |
| `features/customer/customer.routes.ts` | Route: `/customer/read-engagement-letter/:urlId/:token` (public, no auth guard) |
| `features/customer/engagement-letter/sign/` | Customer can sign engagement letters via token-based access |

**Key insight:** Customers access their own Engagement Letters via signed URL tokens (`urlId` + `token`), NOT via JWT-authenticated API calls from the home dashboard. The `/customer/` routes are unauthenticated (public) and use token-based access.

### What exists for customer to see their own letters via authenticated session?

**NOT found in goa-front.** There is no authenticated customer route (no RoleGuard with CUSTOMER) in `home.routes.ts` for EngagementLetters. The home routes for EngagementLetters require `ADMIN | MANAGER | OPERATOR` only.

**Implication for Review:** The customer sees their letters via token-based public links, NOT via an authenticated dashboard. The Review feature should likely follow the same pattern or add a CUSTOMER-authenticated route.

---

## goa-front API and endpoint style

### Base URLs

| Key | Value | Source |
|---|---|---|
| `REST_USER` | `http://localhost:8080/api/goa-user` | `environment.ts` |
| `REST_ENGAGEMENT` | `http://localhost:8080/api/goa-engagement` | `environment.ts` |
| `REST_BILLING` | `http://localhost:8080/api/goa-billing` | `environment.ts` |
| `REST_SUPPORT` | `http://localhost:8080/api/goa-support` | `environment.ts` |
| `REST_DOCUMENT` | `http://localhost:8080/api/goa-document` | `environment.ts` |
| `SECURE_ROUTES` | `[API, CHATBOT]` | `environment.ts` — API = `http://localhost:8080/api` |

**No `goa-sandbox` REST endpoint constant exists yet in goa-front.**

### ENDPOINTS pattern (endpoints.ts)

```typescript
const ENGAGEMENT_LETTER_ROOT = `${environment.REST_ENGAGEMENT}/engagement-letters`;
export const ENDPOINTS = {
    engagementLetters: {
        root: ENGAGEMENT_LETTER_ROOT,
        byId: (id: string) => `${ENGAGEMENT_LETTER_ROOT}/${enc(id)}`,
        pendingSigners: (id: string) => `${ENGAGEMENT_LETTER_ROOT}/${id}/pending-signers`,
    },
    // ...
}
```

**Convention:** URL-encode parameters, use factory functions `byId(id)`, group by entity.

### HTTP service pattern

| Layer | File | Pattern |
|---|---|---|
| Service | `shared/ui/api/http.service.ts` | `HttpService.request()` returns `HttpViewBuilder` |
| Builder | `core/http/http-request-builder.ts` | `get()`, `post()`, `put()`, `patch()`, `delete()`, `getBlob()` — all return `Observable<T>` |
| View | `shared/ui/api/http-view-builder.ts` | Wraps builder with snack notifications, error handling, dialog warnings |
| Interceptor | **NOT found** | No `HttpInterceptor` found. `angular-auth-oidc-client` handles token automatically via library configuration. |

### goa-sandbox API path in goa-front

**Status: NOT defined yet.** goa-front does not have a `REST_SANDBOX` or `goa-sandbox` entry in `environment.ts`. The `goa-sandbox` base URL is not configured in goa-front.

**Will need to add:** `REST_SANDBOX: ${API}/goa-sandbox` in environment.ts when implementing Review frontend.

### Token attachment (how does Angular attach JWT to requests?)

**Mechanism: `angular-auth-oidc-client` library.** The library automatically attaches the access token as a Bearer token to HTTP requests for `SECURE_ROUTES`. All requests to `${API}/*` (which includes `goa-sandbox` at `/api/goa-sandbox/*`) automatically receive the JWT Bearer token.

**Evidence:** `environment.ts` line 19: `SECURE_ROUTES: [API, CHATBOT]` — this is where the library is configured to attach tokens.

### Gateway path confirmation

| Microservice | Gateway path |
|---|---|
| goa-user | `http://localhost:8080/api/goa-user` |
| goa-engagement | `http://localhost:8080/api/goa-engagement` |
| goa-billing | `http://localhost:8080/api/goa-billing` |
| goa-support | `http://localhost:8080/api/goa-support` |
| goa-sandbox | **NOT confirmed** — likely `http://localhost:8080/api/goa-sandbox` |

**Likely:** goa-sandbox at `http://localhost:8080/api/goa-sandbox`. Gateway routes should be confirmed with docker-compose or teacher documentation.

---

## Cross-service boundary conclusion

### Can Review store full User object?
**No.** goa-sandbox does not have a User entity or UserRepository. Storing a full User object would require embedding or duplicating data. Based on Complaint pattern: store only `String userId` (mobile).

### Can Review store full EngagementLetter object?
**No.** goa-sandbox does not own EngagementLetter data. Cannot access goa-engagement database directly (scope rule). Cannot confirm whether a Feign client to goa-engagement exists. Storing only `String letterId` (EngagementLetter UUID) is the safest approach.

### Recommended storage for Review:
- `String userId` (mobile string) — confirmed from JWT username claim
- `String letterId` (EngagementLetter UUID string) — likely, needs confirmation

### Can goa-sandbox directly query goa-engagement database?
**No evidence found.** No MongoDB connection to goa-engagement, no Feign client to goa-engagement. Scope document rule: "do not access another microservice database directly." A Feign client could be added but is not currently present.

---

## Recommended Review identifier strategy for M2/M3

### user identifier candidate
- **Candidate:** `String userId` = JWT `username` claim = mobile string
- **Status:** confirmed from ComplaintResource pattern
- **Backend usage:** `jwt.getClaimAsString("username")` in controller, stored in domain model

### letter identifier candidate
- **Candidate:** `String letterId` = EngagementLetter UUID (`EngagementLetter.id: string`)
- **Status:** likely (from goa-front `EngagementLetter.id?: string`), **not confirmed from goa-sandbox perspective**
- **Alternative candidate:** `String barcode` (if EngagementLetter uses barcode as human-readable ID)
- **Status:** not confirmed — the existing `barcode` in Complaint is a legacy field, not the EngagementLetter UUID

### Review uniqueness candidate
- **Candidate:** One review per customer per Engagement Letter
- **Implementation:** Compound unique constraint: `(userId, letterId)` must be unique
- **Backend check:** `ReviewRepository.existsByUserIdAndLetterId(userId, letterId)` before create — throw `HttpStatus.CONFLICT` if exists

### Review ownership validation candidate
- **Candidate:** Customer identity from JWT `username` claim (mobile)
- **Implementation:** `ReviewResource` extracts `userId` from JWT, not from request body
- **Scope enforcement:** Review list/update endpoints filter by `userId == jwt.userId`
- **Status:** confirmed from ComplaintResource pattern

### API request path candidate
- **Candidate:** `POST /reviews` (backend), `GET /reviews/{id}`, `PUT /reviews/{id}` (update only, no delete)
- **Gateway path:** `http://localhost:8080/api/goa-sandbox/reviews`
- **Status:** likely, **gateway route not confirmed**

---

## Open questions before implementation

| # | Question | Severity | Notes |
|---|---|---|---|
| 1 | **What is the exact EngagementLetter identifier field in goa-engagement?** | HIGH | Is it UUID `id` or human-readable `barcode`? Frontend uses `EngagementLetter.id?: string`. Backend needs the same. |
| 2 | **Does goa-sandbox need a Feign client to goa-engagement?** | HIGH | For validating that the letter belongs to the current customer before creating a review? Scope says customer can only see their own letters — but if review is token-based, no validation needed. |
| 3 | **How does the customer identify their letters in the Review UI?** | HIGH | Via token-based public links? Or does a CUSTOMER-authenticated route need to be added to home.routes.ts? Currently only ADMIN/MANAGER/OPERATOR can access EngagementLetters in home. |
| 4 | **What is the gateway route for goa-sandbox?** | MEDIUM | Likely `/api/goa-sandbox`, but not confirmed. docker-compose.yml comment says "real route will be /api/goa-sandbox/complaints". |
| 5 | **What is the EngagementLetter search API for a specific customer?** | MEDIUM | Does goa-engagement have an endpoint like `GET /engagement-letters?client={mobile}`? The find criteria has `client?: string` but endpoint is not customer-filtered by default. |
| 6 | **Should Review frontend be under `/customer/` (public token) or `/home/reviews` (JWT auth)?** | MEDIUM | goa-front already has `/customer/` routes for public token-based access. Review could follow the same pattern. |
| 7 | **Should Review be a new feature page or integrated into the existing EngagementLetter list?** | LOW | Could add a "Review" column/button to the existing EngagementLettersComponent. But CUSTOMER cannot access home/engagement-letters. |

---

## M1.2 acceptance checklist

- goa-sandbox branch checked: ✅ `feature/reviews`
- goa-front branch checked: ✅ `feature/reviews`
- no business code changed: ✅
- no src modified: ✅
- no pom.xml modified: ✅
- no package.json modified: ✅
- no docker-compose.yml modified: ✅
- no .github modified: ✅
- audit document created: ✅ `docs/customer-reviews-user-letter-audit.md`
- User evidence collected: ✅ (JWT username=mobile, UserSnapshot, Customer role, angular-auth-oidc-client)
- EngagementLetter evidence collected: ✅ (goa-front has full model/service/pages; goa-sandbox has none)
- frontend evidence collected: ✅ (EngagementLetter CRUD, customer token access, API endpoints, HttpService, auth)
- unknowns marked as 未确认: ✅ items 1–7 documented
- no push performed: ✅
