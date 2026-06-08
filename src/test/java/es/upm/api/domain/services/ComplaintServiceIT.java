package es.upm.api.domain.services;

import es.upm.api.domain.model.Complaint;
import es.upm.api.domain.model.Status; // Asegúrate de importar tu enum Status
import es.upm.api.domain.persistence.ComplaintPersistence;
import es.upm.api.domain.webclients.EngagementWebClient;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class ComplaintServiceIT {

    @Autowired
    private ComplaintService complaintService;

    @MockitoBean
    private ComplaintPersistence complaintPersistence;

    @MockitoBean
    private EngagementWebClient engagementWebClient;

    private Complaint complaint;

    @BeforeEach
    void setUp() {
        this.complaint = Complaint.builder()
                .engagementId(UUID.randomUUID())
                .description("Service not as described")
                .status(Status.OPEN)
                .createdAt(LocalDateTime.of(2026, 5, 31, 10, 0))
                .build();
    }

    @Test
    void shouldCreateComplaint() {
        when(this.engagementWebClient.readById(this.complaint.getEngagementId())).thenReturn(new Object());

        Complaint createdComplaint = this.complaintService.create(this.complaint);

        assertNotNull(createdComplaint);
        assertNotNull(createdComplaint.getId());
        assertEquals(this.complaint.getEngagementId(), createdComplaint.getEngagementId());
        assertEquals(this.complaint.getDescription(), createdComplaint.getDescription());
        assertEquals(this.complaint.getCreatedAt(), createdComplaint.getCreatedAt());
        assertEquals(this.complaint.getStatus(), createdComplaint.getStatus());

        ArgumentCaptor<Complaint> complaintCaptor = ArgumentCaptor.forClass(Complaint.class);
        verify(this.complaintPersistence).create(complaintCaptor.capture());
        verify(this.engagementWebClient).readById(this.complaint.getEngagementId());

        Complaint persistedComplaint = complaintCaptor.getValue();
        assertEquals(createdComplaint.getId(), persistedComplaint.getId());
    }

    @Test
    void shouldNotPersistComplaintWhenEngagementDoesNotExist() {
        RuntimeException exception = new RuntimeException("Engagement not found");
        when(this.engagementWebClient.readById(this.complaint.getEngagementId())).thenThrow(exception);

        assertThrows(RuntimeException.class, () -> this.complaintService.create(this.complaint));

        verify(this.engagementWebClient).readById(this.complaint.getEngagementId());
        verify(this.complaintPersistence, never()).create(any());
    }

    @Test
    void shouldFindAll() {
        Stream<Complaint> complaintStream = Stream.of(this.complaint);
        when(this.complaintPersistence.findAll()).thenReturn(complaintStream);

        Stream<Complaint> allComplaints = this.complaintService.findAll();

        verify(this.complaintPersistence).findAll();
        assertEquals(this.complaint, allComplaints.findFirst().orElse(null));
    }

    @Test
    void shouldReadComplaintById() {
        this.complaint.setId(UUID.randomUUID());
        when(this.complaintPersistence.readById(this.complaint.getId())).thenReturn(this.complaint);

        Complaint readComplaint = this.complaintService.readById(this.complaint.getId());

        assertEquals(this.complaint, readComplaint);
        verify(this.complaintPersistence).readById(this.complaint.getId());
        verifyNoInteractions(this.engagementWebClient);
    }
}