package es.upm.api.domain.model;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.AllArgsConstructor;
import lombok.Builder;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class Complaint {
    @Id
    private String id;
    private LocalDateTime registrationDate;
    private String mobile;
    private String barcode;
    private String description;
    private ComplaintState state;
    private String reply;
    private String userId;
}