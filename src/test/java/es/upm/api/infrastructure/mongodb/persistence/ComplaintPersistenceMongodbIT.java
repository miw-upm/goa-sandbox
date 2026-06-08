package es.upm.api.infrastructure.mongodb.persistence;

import es.upm.api.domain.model.Complaint;
import es.upm.api.domain.model.Status;
import es.upm.api.infrastructure.mongodb.entities.ComplaintEntity;
import es.upm.api.infrastructure.mongodb.repositories.ComplaintRepository;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Stream;
import java.util.List;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
class ComplaintPersistenceMongodbIT {

    @Autowired
    private ComplaintPersistenceMongodb complaintPersistenceMongodb;

    @MockitoBean
    private ComplaintRepository complaintRepository;

    private Complaint complaint;

    @BeforeEach
    void setUp() {
        this.complaint = Complaint.builder()
                .id(UUID.randomUUID())
                .engagementId(UUID.randomUUID())
                .description("Service not as described")
                .status(Status.OPEN)
                .createdAt(LocalDateTime.of(2026, 5, 31, 15, 0))
                .build();
    }

    @Test
    void shouldCreateComplaint() {
        when(this.complaintRepository.save(any(ComplaintEntity.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        this.complaintPersistenceMongodb.create(this.complaint);

        ArgumentCaptor<ComplaintEntity> entityCaptor = ArgumentCaptor.forClass(ComplaintEntity.class);
        verify(this.complaintRepository).save(entityCaptor.capture());

        ComplaintEntity persistedEntity = entityCaptor.getValue();
        assertEquals(this.complaint.getId(), persistedEntity.getId());
        assertEquals(this.complaint.getEngagementId(), persistedEntity.getEngagementId());
        assertEquals(this.complaint.getDescription(), persistedEntity.getDescription());
        assertEquals(this.complaint.getStatus(), persistedEntity.getStatus());
        assertEquals(this.complaint.getCreatedAt(), persistedEntity.getCreatedAt());
    }

    @Test
    void shouldPropagateExceptionWhenRepositoryFails() {
        RuntimeException exception = new RuntimeException("Mongo error");
        when(this.complaintRepository.save(any(ComplaintEntity.class))).thenThrow(exception);

        assertThrows(RuntimeException.class,
                () -> this.complaintPersistenceMongodb.create(this.complaint));

        verify(this.complaintRepository).save(any(ComplaintEntity.class));
    }

    @Test
    void shouldFindAll() {
        when(this.complaintRepository.findAll(ComplaintPersistenceMongodb.CREATEDAT))
                .thenReturn(List.of(new ComplaintEntity(this.complaint)));

        Stream<Complaint> complaintStream = this.complaintPersistenceMongodb.findAll();

        verify(this.complaintRepository).findAll(ComplaintPersistenceMongodb.CREATEDAT);

        assertEquals(this.complaint, complaintStream.findFirst().orElse(null));
    }
}