package es.upm.api.infrastructure.mongodb.entities;

import es.upm.api.domain.model.Complaint;
import es.upm.api.domain.model.Status;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class ComplaintEntityTest {

    private Complaint complaint;

    @BeforeEach
    void setUp() {
        this.complaint = Complaint.builder()
                .id(UUID.randomUUID())
                .engagementId(UUID.randomUUID())
                .mobile("600111222")
                .description("Service not as described")
                .status(Status.OPEN)
                .createdAt(LocalDateTime.of(2026, 5, 31, 15, 0))
                .build();
    }

    @Test
    void shouldBuildComplaintEntityFromComplaint() {
        ComplaintEntity complaintEntity = new ComplaintEntity(this.complaint);

        assertEquals(this.complaint.getId(), complaintEntity.getId());
        assertEquals(this.complaint.getEngagementId(), complaintEntity.getEngagementId());
        assertEquals(this.complaint.getMobile(), complaintEntity.getMobile());
        assertEquals(this.complaint.getDescription(), complaintEntity.getDescription());
        assertEquals(this.complaint.getStatus(), complaintEntity.getStatus());
        assertEquals(this.complaint.getCreatedAt(), complaintEntity.getCreatedAt());
    }

    @Test
    void shouldConvertComplaintEntityToComplaint() {
        ComplaintEntity complaintEntity = new ComplaintEntity();
        complaintEntity.setId(this.complaint.getId());
        complaintEntity.setEngagementId(this.complaint.getEngagementId());
        complaintEntity.setMobile(this.complaint.getMobile());
        complaintEntity.setDescription(this.complaint.getDescription());
        complaintEntity.setCreatedAt(this.complaint.getCreatedAt());

        Complaint mappedComplaint = complaintEntity.toComplaint();

        assertEquals(complaintEntity.getId(), mappedComplaint.getId());
        assertEquals(complaintEntity.getEngagementId(), mappedComplaint.getEngagementId());
        assertEquals(complaintEntity.getMobile(), mappedComplaint.getMobile());
        assertEquals(complaintEntity.getDescription(), mappedComplaint.getDescription());
        assertEquals(complaintEntity.getCreatedAt(), mappedComplaint.getCreatedAt());
    }
}