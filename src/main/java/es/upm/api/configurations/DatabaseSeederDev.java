package es.upm.api.configurations;

import jakarta.annotation.PostConstruct;
import lombok.RequiredArgsConstructor;
import lombok.extern.log4j.Log4j2;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Service;

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

    @PostConstruct
    public void init() {
        this.deleteAllAndInitializeAndSeedDataBase();
    }

    public void deleteAllAndInitializeAndSeedDataBase() {
        this.deleteAllAndInitialize();
        this.seedDataBaseJava();
    }

    private void deleteAllAndInitialize() {
        log.warn("------- Delete All -----------");
    }

    private void seedDataBaseJava() {
        log.warn("------- Initial Load from JAVA ---------------------------------------------------------------");
    }

}
