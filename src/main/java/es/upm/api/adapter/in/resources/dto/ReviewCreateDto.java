package es.upm.api.adapter.in.resources.dto;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ReviewCreateDto {
    @NotBlank(message = "Letter ID is required")
    private String letterId;

    @Min(value = 1, message = "Stars must be between 1 and 5")
    @Max(value = 5, message = "Stars must be between 1 and 5")
    private int stars;

    @NotBlank(message = "Opinion is required")
    private String opinion;
}
