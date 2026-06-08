package es.upm.api.infrastructure.resources.dtos;

import com.fasterxml.jackson.annotation.JsonFormat;
import es.upm.api.domain.model.Status;
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
public class ComplaintUpdateRequest {

    @NotNull(message = "Engagement ID is mandatory")
    private UUID engagementId;

    private String mobile;

    @NotBlank(message = "Description is mandatory")
    private String description;

    @NotNull(message = "Status is mandatory")
    private Status status;

    @NotNull(message = "Creation date is mandatory")
    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm[:ss]")
    private LocalDateTime createdAt;
}