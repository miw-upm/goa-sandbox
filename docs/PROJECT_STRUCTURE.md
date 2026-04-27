# Estructura del proyecto (excluyendo `target`)

```text
goa-engagement
├── .github
│   └── workflows
│       ├── cd-main-aws-lightsail.yml
│       ├── cd-staging-aws-lightsail.yml
│       └── ci.yml
├── .idea
│   ├── .gitignore
│   ├── compiler.xml
│   ├── encodings.xml
│   ├── jarRepositories.xml
│   ├── misc.xml
│   ├── vcs.xml
│   └── workspace.xml
├── docs
│   ├── load-template.js
│   └── templates.json
├── src
│   ├── main
│   │   ├── java
│   │   │   └── es
│   │   │       └── upm
│   │   │           └── api
│   │   │               ├── adapter
│   │   │               │   ├── in
│   │   │               │   │   └── resources
│   │   │               │   │       ├── httperrors
│   │   │               │   │       │   └── ApiExceptionHandler.java
│   │   │               │   │       ├── EngagementLetterResource.java
│   │   │               │   │       ├── LegalProcedureTemplateResource.java
│   │   │               │   │       ├── LegalTaskResource.java
│   │   │               │   │       └── SystemResource.java
│   │   │               │   └── out
│   │   │               │       ├── legal
│   │   │               │       │   └── mono
│   │   │               │       │       ├── engagementletter
│   │   │               │       │       │   ├── AcceptanceDocumentEntity.java
│   │   │               │       │       │   ├── EngagementLetterAdapter.java
│   │   │               │       │       │   ├── EngagementLetterEntity.java
│   │   │               │       │       │   ├── EngagementLetterRepository.java
│   │   │               │       │       │   ├── LegalProcedureEntity.java
│   │   │               │       │       │   └── PaymentMethodEntity.java
│   │   │               │       │       ├── legalproceduretemplate
│   │   │               │       │       │   ├── LegalProcedureTemplateAdapter.java
│   │   │               │       │       │   ├── LegalProcedureTemplateEntity.java
│   │   │               │       │       │   └── LegalProcedureTemplateRepository.java
│   │   │               │       │       ├── legaltask
│   │   │               │       │       │   ├── LegalTaskAdapter.java
│   │   │               │       │       │   ├── LegalTaskEntity.java
│   │   │               │       │       │   └── LegalTaskRepository.java
│   │   │               │       │       └── mongo
│   │   │               │       └── user
│   │   │               │           └── feign
│   │   │               │               ├── UserFinderAdapter.java
│   │   │               │               └── UserFinderClient.java
│   │   │               ├── configurations
│   │   │               │   ├── DatabaseSeederDev.java
│   │   │               │   ├── EurekaConfig.java
│   │   │               │   ├── FeignConfig.java
│   │   │               │   ├── LoggingFilter.java
│   │   │               │   ├── OpenApiConfig.java
│   │   │               │   ├── ResourceServerConfig.java
│   │   │               │   └── TokenManager.java
│   │   │               ├── domain
│   │   │               │   ├── model
│   │   │               │   │   ├── criteria
│   │   │               │   │   │   ├── EngagementLetterFindCriteria.java
│   │   │               │   │   │   └── LegalProcedureTemplateFindCriteria.java
│   │   │               │   │   ├── external
│   │   │               │   │   │   ├── AccessLinkSnapshot.java
│   │   │               │   │   │   └── UserSnapshot.java
│   │   │               │   │   ├── AcceptanceEngagement.java
│   │   │               │   │   ├── EngagementLetter.java
│   │   │               │   │   ├── LegalProcedure.java
│   │   │               │   │   ├── LegalProcedureTemplate.java
│   │   │               │   │   ├── LegalTask.java
│   │   │               │   │   └── PaymentMethod.java
│   │   │               │   ├── ports
│   │   │               │   │   └── out
│   │   │               │   │       ├── legal
│   │   │               │   │       │   ├── EngagementLetterGateway.java
│   │   │               │   │       │   ├── LegalProcedureTemplateGateway.java
│   │   │               │   │       │   └── LegalTaskGateway.java
│   │   │               │   │       └── user
│   │   │               │   │           └── UserFinder.java
│   │   │               │   └── services
│   │   │               │       ├── EngagementLetterService.java
│   │   │               │       ├── LegalProcedureTemplateService.java
│   │   │               │       └── LegalTaskService.java
│   │   │               └── Application.java
│   │   └── resources
│   │       ├── images
│   │       │   ├── oa.png
│   │       │   ├── only-sign.png
│   │       │   ├── only-stamp.png
│   │       │   └── stamp.png
│   │       ├── templates
│   │       │   └── engagement-letter-texts.txt
│   │       ├── application.yml
│   │       ├── application-dev.yml
│   │       └── application-prod.yml
│   └── test
│       ├── java
│       │   └── es
│       │       └── upm
│       │           └── api
│       │               ├── adapter
│       │               │   ├── in
│       │               │   │   └── legal
│       │               │   │       └── resources
│       │               │   │           ├── EngagementLetterResourceIT.java
│       │               │   │           └── LegalTaskResourceIT.java
│       │               │   └── out
│       │               │       └── legal
│       │               │           └── mono
│       │               │               ├── engengamentletter
│       │               │               │   └── EngagementLetterAdapterIT.java
│       │               │               ├── legalproceduretemplate
│       │               │               └── legalTask
│       │               │                   └── LegalTaskRepositoryTest.java
│       │               └── domain
│       │                   └── services
│       │                       ├── EngagementLetterPdfCheck.java
│       │                       ├── EngagementLetterServiceIT.java
│       │                       ├── LegalProcedureTemplateServiceIT.java
│       │                       └── LegalTaskServiceIT.java
│       └── resources
│           ├── application-test.yml
│           └── logback-test.xml
├── .gitignore
├── docker-compose.yml
├── Dockerfile
├── LICENSE.md
├── pom.xml
└── README.md
```

