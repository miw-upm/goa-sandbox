package es.upm.api.infrastructure.mongodb.entities;

import com.fasterxml.jackson.annotation.JsonFormat;
import es.upm.api.domain.model.Complaint;
import es.upm.api.domain.model.Status;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class ComplaintEntity {
    @Id
    private UUID id;
    private UUID engagementId;
    private String mobile; // Se puede poblar desde el servicio si viene del perfil
    private String description;
    private Status status; // Enum: OPEN, IN_PROGRESS, RESOLVED, etc.
    private LocalDateTime createdAt;

    public ComplaintEntity(Complaint complaint) {
        BeanUtils.copyProperties(complaint, this);
    }

    public Complaint toComplaint() {
        Complaint complaint = new Complaint();
        BeanUtils.copyProperties(this, complaint);
        return complaint;
    }
}