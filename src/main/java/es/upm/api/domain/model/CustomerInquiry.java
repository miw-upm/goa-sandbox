package es.upm.api.domain.model;

import com.fasterxml.jackson.annotation.JsonFormat;
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
public class CustomerInquiry {
    private UUID id;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm[:ss]")
    private LocalDateTime registrationDate;

    private String customer;

    @NotBlank(message = "Subject is mandatory")
    private String subject;

    @NotBlank(message = "Description is mandatory")
    private String description;

    @NotNull(message = "Category is mandatory")
    private InquiryCategory category;

    private InquiryState state;

    private String reply;

    private String repliedBy;

    @JsonFormat(pattern = "yyyy-MM-dd'T'HH:mm[:ss]")
    private LocalDateTime replyDate;
}