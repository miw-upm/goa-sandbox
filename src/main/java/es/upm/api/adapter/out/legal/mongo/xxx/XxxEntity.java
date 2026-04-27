package es.upm.api.adapter.out.legal.mongo.customerfiledownload;

import es.upm.api.domain.model.CustomerFileDownload;
import es.upm.api.domain.model.external.UserSnapshot;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class CustomerFileDownloadEntity {
    @Id
    private UUID id;
    private LocalDateTime downloadedAt;
    private UUID customerId;
    private String documentType;
    private UUID documentId;
    private String downloadToken;

    public CustomerFileDownloadEntity(CustomerFileDownload customerFileDownload) {
        BeanUtils.copyProperties(customerFileDownload, this);
        Optional.ofNullable(customerFileDownload.getCustomer())
                .ifPresent(customer -> this.customerId = customer.getId());
    }

    public CustomerFileDownload toDomain() {
        CustomerFileDownload customerFileDownload = new CustomerFileDownload();
        BeanUtils.copyProperties(this, customerFileDownload);
        customerFileDownload.setCustomer(
                Optional.ofNullable(this.customerId)
                        .map(id -> UserSnapshot.builder().id(id).build())
                        .orElse(null)
        );
        return customerFileDownload;
    }
}
