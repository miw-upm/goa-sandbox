package es.upm.api.infrastructure.mongodb.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import es.upm.api.domain.model.Complaint;
import es.upm.api.domain.model.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Document
public class ComplaintEntity {
    @Id
    private UUID id;
    private UUID engagementId;
    private String mobile; // Se puede poblar desde el servicio si viene del perfil
    private String description;
    private Status status; // Enum: OPEN, IN_PROGRESS, RESOLVED, etc.
    private LocalDateTime createdAt;

    public ComplaintEntity() {
        // Empty for framework
    }

    public ComplaintEntity(Complaint complaint) {
        BeanUtils.copyProperties(complaint, this);
    }

    public Complaint toComplaint() {
        Complaint complaint = new Complaint();
        BeanUtils.copyProperties(this, complaint);
        return complaint;
    }

    public UUID getId() {
        return id;
    }

    public void setId(UUID id) {
        this.id = id;
    }

    public UUID getEngagementId() {
        return engagementId;
    }

    public void setEngagementId(UUID engagementId) {
        this.engagementId = engagementId;
    }

    public String getMobile() {
        return mobile;
    }

    public void setAmount(String mobile) {
        this.mobile = mobile;
    }

    public Status getStatus() {
        return status;
    }

    public LocalDateTime getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(LocalDateTime createdAt) {
        this.createdAt = createdAt;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}