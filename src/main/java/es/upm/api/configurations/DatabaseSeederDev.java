package es.upm.api.configurations;

import es.upm.api.adapter.out.legal.mongo.customerfiledownload.CustomerFileDownloadEntity;
import es.upm.api.adapter.out.legal.mongo.customerfiledownload.CustomerFileDownloadRepository;
import es.upm.api.adapter.out.legal.mongo.engagementletter.*;
import es.upm.api.adapter.out.legal.mongo.legalproceduretemplate.LegalProcedureTemplateEntity;
import es.upm.api.adapter.out.legal.mongo.legalproceduretemplate.LegalProcedureTemplateRepository;
import es.upm.api.adapter.out.legal.mongo.legaltask.LegalTaskEntity;
import es.upm.api.adapter.out.legal.mongo.legaltask.LegalTaskRepository;
import es.upm.miw.device.DeviceInfo;
import es.upm.miw.uuid.UUIDBase64;
import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Log4j2
@Service
@RequiredArgsConstructor
@Profile({"dev", "test"})
public class DatabaseSeederDev {

    public static final UUID[] UUIDS = {
            UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0000"),
            UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0001"),
            UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0002"),
            UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0003"),
            UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0004"),
            UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0005"),
            UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0006"),
            UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0007"),
            UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0008"),
            UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0009"),
            UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff000a"),
            UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff000b"),
            UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff000c"),
            UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff000d"),
    };
    public static final UUID[] US = {
            UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0004"),
            UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0005"),
            UUID.fromString("aaaaaaaa-bbbb-cccc-dddd-eeeeffff0006")
    };
    private static final String LEGAL_CLAUSE = "Clausula especial legal!!!. Clausula especial legal!!!. Clausula especial legal!!!."
            + "Clausula especial legal!!!. Clausula especial legal!!!. Clausula especial legal!!!."
            + "Clausula especial legal!!!. Clausula especial legal!!!. Clausula especial legal!!!."
            + "Clausula especial legal!!!. Clausula especial legal!!!. Clausula especial legal!!!.";

    private final LegalTaskRepository legalTaskRepository;
    private final LegalProcedureTemplateRepository legalProcedureTemplateRepository;
    private final EngagementLetterRepository engagementLetterRepository;
    private final CustomerFileDownloadRepository customerFileDownloadRepository;

    @PostConstruct
    public void init() {
        this.deleteAllAndInitializeAndSeedDataBase();
    }

    public void deleteAllAndInitializeAndSeedDataBase() {
        this.deleteAllAndInitialize();
        this.seedDataBaseJava();
    }

    private void deleteAllAndInitialize() {
        this.customerFileDownloadRepository.deleteAll();
        this.engagementLetterRepository.deleteAll();
        this.legalProcedureTemplateRepository.deleteAll();
        this.legalTaskRepository.deleteAll();
        log.warn("------- Delete All -----------");
    }

    private void seedDataBaseJava() {
        log.warn("------- Initial Load from JAVA ---------------------------------------------------------------");

        LegalTaskEntity[] tasks = {
                new LegalTaskEntity(UUIDS[0], "Estudio de antecedentes y documentación"),
                new LegalTaskEntity(UUIDS[1], "Asesoramiento jurídico"),
                new LegalTaskEntity(UUIDS[2], "Localización de personas"),
                new LegalTaskEntity(UUIDS[3], "Negociación de la aceptación o renuncia con contrario"),
                new LegalTaskEntity(UUIDS[4], "Tramitación notarial de la herencia"),
                new LegalTaskEntity(UUIDS[5], "Liquidación del Impuesto de Sucesiones y Plusvalía Mortis causa"),
                new LegalTaskEntity(UUIDS[6], "Redacción del cuaderno particional de la herencia ante el notario correspondiente"),
                new LegalTaskEntity(UUIDS[7], "Liquidación de Impuesto de Sucesiones (prescrito)"),
                new LegalTaskEntity(UUIDS[8], "Averiguación de los posibles pasivos (deuda) existente"),
                new LegalTaskEntity(UUIDS[9], "Tramitación de los seguros"),
                new LegalTaskEntity(UUIDS[9], "Redacción de la escritura de herencia y tramitación con la notaría correspondiente"),
                new LegalTaskEntity(UUIDS[9], "Asistencia letrada en la notaría"),
                new LegalTaskEntity(UUIDS[9], "Inscripción de los correspondientes bienes inmuebles en los Registros de la Propiedad"),
                new LegalTaskEntity(UUIDS[9], "Tramitación de la venta de las viviendas de la herencia con la inmobiliaria"),
        };
        this.legalTaskRepository.saveAll(List.of(tasks));
        log.warn("        ------- tareas legales --------------------------------------------------------------------");


        LegalProcedureTemplateEntity[] templates = {
                new LegalProcedureTemplateEntity(UUIDS[0], "Procedimiento de herencia", new BigDecimal("2500"),
                        List.of(tasks[0], tasks[1], tasks[2], tasks[3], tasks[4], tasks[5], tasks[6])),
                new LegalProcedureTemplateEntity(UUIDS[1], "División de Herencia", new BigDecimal("3000"),
                        List.of(tasks[0], tasks[1], tasks[7], tasks[8], tasks[9], tasks[10], tasks[11])),
                new LegalProcedureTemplateEntity(UUIDS[2], "Herencia notarial", new BigDecimal("1000"),
                        List.of(tasks[0], tasks[1], tasks[12], tasks[13])),
                new LegalProcedureTemplateEntity(UUIDS[3], "Procedimiento de ejecución hipotecaria", new BigDecimal("4000"),
                        List.of(tasks[0], tasks[1], tasks[13]))
        };
        this.legalProcedureTemplateRepository.saveAll(List.of(templates));
        log.warn("        ------- plantilla de procedimientos legales -----------------------------------------------");


        DeviceInfo device = DeviceInfo.builder().deviceType("Escritorio").ipAddress("83.52.10.24")
                .operatingSystem("Windows").browser("chrome").build();

        AcceptanceEngagementEntity[] acceptances = {
                AcceptanceEngagementEntity.builder()
                        .signatureAt(LocalDateTime.now().plusHours(1))
                        .signerId(US[0])
                        .signerFullName("c1 family-c1")
                        .signerIdentity("66666603E")
                        .mobile("666666000")
                        .signerEmail("c1@gmail.com")
                        .signatureToken(UUIDBase64.URL.encode())
                        .deviceInfo(device)
                        .documentAccepted(true)
                        .build(),
                AcceptanceEngagementEntity.builder()
                        .signatureAt(LocalDateTime.now().plusHours(1))
                        .signerId(US[1])
                        .signerFullName("c2 family-c2")
                        .signerIdentity("66666604T")
                        .mobile("666666001")
                        .signerEmail("c2@gmail.com")
                        .signatureToken(UUIDBase64.URL.encode())
                        .deviceInfo(device).build(),
        };

        LegalProcedureEntity[] procedimientos = {
                LegalProcedureEntity.builder().title(templates[0].getTitle())
                        .legalTasks(List.of(tasks[0].getTitle(), tasks[1].getTitle(), tasks[2].getTitle(),
                                tasks[3].getTitle(), tasks[4].getTitle(), tasks[5].getTitle(), tasks[6].getTitle()))
                        .budget(new BigDecimal("2500")).startDate(LocalDate.now().minusDays(2)).vatIncluded(false).build(),
                LegalProcedureEntity.builder().title(templates[1].getTitle())
                        .legalTasks(List.of(tasks[0].getTitle(), tasks[1].getTitle(), tasks[7].getTitle(),
                                tasks[8].getTitle(), tasks[9].getTitle(), tasks[10].getTitle(), tasks[11].getTitle()))
                        .budget(new BigDecimal("3000")).startDate(LocalDate.now()).vatIncluded(true).build(),
                LegalProcedureEntity.builder().title(templates[2].getTitle())
                        .legalTasks(List.of(tasks[0].getTitle(), tasks[1].getTitle(), tasks[12].getTitle(), tasks[13].getTitle()))
                        .budget(new BigDecimal("1000")).startDate(LocalDate.now()).vatIncluded(false).build(),
        };

        EngagementLetterEntity[] encargos = {
                EngagementLetterEntity.builder().id(UUIDS[0])
                        .budgetOnly(true)
                        .discount(10).lastUpdatedDate(LocalDate.now().minusDays(5))
                        .paymentMethodEntity(PaymentMethodEntity.builder().description("Provisión de fondos").percentage("40%").build())
                        .paymentMethodEntity(PaymentMethodEntity.builder().description("Finalizado el procedimiento").percentage("60%").build())
                        .ownerId(US[0])
                        .attachmentId(US[1])
                        .legalClause(LEGAL_CLAUSE)
                        .legalProcedureEntities(List.of(procedimientos[0], procedimientos[2]))
                        .acceptanceEngagementEntities(List.of(acceptances[0]))
                        .build(),
                EngagementLetterEntity.builder().id(UUIDS[1])
                        .budgetOnly(false)
                        .discount(20).lastUpdatedDate(LocalDate.now())
                        .paymentMethodEntity(PaymentMethodEntity.builder().description("Provisión de fondos").percentage("40%").build())
                        .paymentMethodEntity(PaymentMethodEntity.builder().description("Finalizado el procedimiento").percentage("60%").build())
                        .ownerId(US[0])
                        .attachmentId(US[1])
                        .legalProcedureEntities(List.of(procedimientos[1], procedimientos[2]))
                        .acceptanceEngagementEntities(List.of(acceptances[0], acceptances[1]))
                        .build(),
                EngagementLetterEntity.builder().id(UUIDS[2])
                        .budgetOnly(false)
                        .discount(15)
                        .legalClause(LEGAL_CLAUSE)
                        .lastUpdatedDate(LocalDate.now())
                        .ownerId(US[1])
                        .attachmentId(US[2])
                        .paymentMethodEntity(PaymentMethodEntity.builder()
                                .description("A la firma de la carta de encargo")
                                .percentage("50%").build())
                        .paymentMethodEntity(PaymentMethodEntity.builder()
                                .description("A la finalización del procedimiento")
                                .percentage("50%").build())
                        .legalProcedureEntities(List.of(procedimientos[0], procedimientos[1], procedimientos[2]))
                        .acceptanceEngagementEntities(List.of(acceptances[1]))
                        .build(),
                EngagementLetterEntity.builder().id(UUIDS[3])
                        .budgetOnly(false)
                        .discount(10)
                        .lastUpdatedDate(LocalDate.now().minusDays(30))
                        .closingDate(LocalDate.now().minusDays(5))  // CERRADO
                        .ownerId(US[0])
                        .paymentMethodEntity(PaymentMethodEntity.builder().description("Completo").percentage("100%").build())
                        .legalProcedureEntities(List.of(procedimientos[0]))
                        .build(),
                EngagementLetterEntity.builder().id(UUIDS[4])
                        .budgetOnly(false)
                        .discount(20).lastUpdatedDate(LocalDate.now())
                        .paymentMethodEntity(PaymentMethodEntity.builder().description("Provisión de fondos").percentage("40%").build())
                        .ownerId(US[0])
                        .legalProcedureEntities(List.of(procedimientos[1], procedimientos[2]))
                        .acceptanceEngagementEntities(List.of(acceptances[0]))
                        .build(),


        };

        this.engagementLetterRepository.saveAll(List.of(encargos));
        log.warn("        ------- Hojas de encargo ------------------------------------------------------------------");

        CustomerFileDownloadEntity[] customerFileDownloads = {
                CustomerFileDownloadEntity.builder()
                        .id(UUIDS[0])
                        .downloadedAt(LocalDateTime.now().minusHours(2))
                        .customerId(US[0])
                        .documentType("engagement-letter")
                        .documentId(UUIDS[0])
                        .downloadToken(UUIDBase64.URL.encode())
                        .build(),
                CustomerFileDownloadEntity.builder()
                        .id(UUIDS[1])
                        .downloadedAt(LocalDateTime.now().minusHours(1))
                        .customerId(US[1])
                        .documentType("engagement-letter")
                        .documentId(UUIDS[1])
                        .downloadToken(UUIDBase64.URL.encode())
                        .build(),
                CustomerFileDownloadEntity.builder()
                        .id(UUIDS[2])
                        .downloadedAt(LocalDateTime.now().minusMinutes(20))
                        .customerId(US[0])
                        .documentType("engagement-budget")
                        .documentId(UUIDS[2])
                        .downloadToken(UUIDBase64.URL.encode())
                        .build(),
        };

        this.customerFileDownloadRepository.saveAll(List.of(customerFileDownloads));
        log.warn("        ------- Descargas de documentos de clientes -----------------------------------------------");
    }

}
