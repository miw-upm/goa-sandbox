package es.upm.api.adapter.out.complaint;

import es.upm.api.domain.model.Complaint;
import es.upm.api.domain.model.ComplaintState;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Document(collection = "complaints")
public class ComplaintEntity {
    @Id
    private String id;
    private LocalDateTime registrationDate;
    private String mobile;
    private String barcode;
    private String description;
    private ComplaintState state;
    private String reply;
    private String userId;

    // constructor que convierte el objeto de Dominio a Entidad de BD
    public ComplaintEntity(Complaint complaint) {
        BeanUtils.copyProperties(complaint, this);
    }

    // método que convierte la Entidad de BD de vuelta al objeto de Dominio
    public Complaint toComplaint() {
        Complaint complaint = new Complaint();
        BeanUtils.copyProperties(this, complaint);
        return complaint;
    }
}
