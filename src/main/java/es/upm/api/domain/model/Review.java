package es.upm.api.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Review {
    private String id;
    private String userId;
    private String letterId;
    private int stars;
    private String opinion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
