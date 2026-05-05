# Customer Reviews

## Source of truth

- Direct assignment source: teacher email.
- Highest project standard: GOA repository README and repository requirements.
- Secondary guidance: BETCA slides and course architecture rules.
- Implementation source of truth: current goa-sandbox and goa-front code.

## Local repositories

- Backend repository: goa-sandbox
- Frontend repository: goa-front
- The project must be developed from the real teacher repositories, not from a newly generated empty project.

## Repository setup facts from README

- goa-sandbox technology stack: Java, Maven, Spring Boot, Docker, OpenAPI, GitHub Actions, GitHub Packages.
- goa-sandbox local Docker execution: `docker network create goa` (once), then `docker compose up --build -d`.
- goa-sandbox Swagger/OpenAPI URL: `http://localhost:8088/swagger-ui.html`.
- goa-front technology stack: Angular (CLI version 19.1.6), Node-based build tooling.
- goa-front local development command: `ng serve`.
- goa-front local application URL: `http://localhost:4200/`.
- goa-front build command: `ng build`.

## Assignment scope

- Enunciado: Customer Reviews.
- Customers can manage votes with comments.
- Stars must be in range 1..5.
- The frontend must use a visual element for stars, for example star rating.
- Initial interface from teacher:
  Review { user: User; letter: EngagementLetter; stars: number; opinion: string }
- The customer can only see their own Hojas de encargo / Engagement Letters.
- The customer can create one review per own Engagement Letter.
- The customer can update their existing review.
- The backend microservice is goa-sandbox.
- The frontend is goa-front.
- In goa-front, do not work directly on develop.
- In goa-front, create feature/reviews and treat it as the development branch.
- Issue branches must be created from feature/reviews and merged back into feature/reviews.

## Architecture constraints

- Do not implement this as a public generic CRUD.
- Do not add admin review management unless explicitly required later.
- Do not expose all users' reviews to customers.
- Do not allow a customer to review another customer's Engagement Letter.
- Do not access another microservice database directly.
- Persist cross-microservice references by ID unless the current project architecture proves another pattern.
- API responses should use DTOs, not entities or primitive/native returns.
- Business logic must stay in service/domain layer, not in repository/persistence.
- Follow existing goa-sandbox and goa-front code style after audit.

## Explicitly out of scope unless the teacher later requests it

- Admin moderation of reviews.
- Anonymous reviews.
- Public review listing.
- Average rating dashboards.
- Deleting reviews.
- Ranking or statistics.
- Email/SMS notifications.
- AI review analysis.

## Sensitive credentials rule

- GitHub tokens, Maven settings passwords, API keys and secrets must stay local.
- They must not be copied into documentation.
- They must not be committed.
- If a token has been exposed in a screenshot or chat, it should be regenerated.

## Acceptance criteria for M0.1

- The real repositories goa-sandbox and goa-front exist locally under the same parent workspace.
- goa-sandbox/docs/customer-reviews-scope.md exists.
- The scope document mentions goa-sandbox, goa-front and feature/reviews.
- The scope document states stars 1..5.
- The scope document states one review per customer per Engagement Letter.
- The scope document states customers can update their review.
- The scope document states customers can only see their own Hojas de encargo.
- The scope document clearly lists out-of-scope features.
- No business code is changed.
- No credentials are copied into any file.
