package es.upm.api.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
import jakarta.validation.Valid;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Complaint {
    private UUID id;

    @NotNull
    private UUID engagementId;

    private String mobile; // Se puede poblar desde el servicio si viene del perfil

    @NotBlank(message = "Description is mandatory and cannot be empty")
    private String description;

    private Status status; // Enum: OPEN, IN_PROGRESS, RESOLVED, etc.

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm:ss")
    private LocalDateTime createdAt;
}