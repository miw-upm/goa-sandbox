package es.upm.api.infrastructure.mongodb.entities;

import es.upm.api.domain.model.CustomerInquiry;
import es.upm.api.domain.model.InquiryCategory;
import es.upm.api.domain.model.InquiryState;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;
import java.util.UUID;

@Builder
@Data
@NoArgsConstructor
@AllArgsConstructor
@Document
public class CustomerInquiryEntity {
    @Id
    private UUID id;
    private LocalDateTime registrationDate;
    private String customerMobile;
    private String subject;
    private String description;
    private InquiryCategory category;
    private InquiryState state;
    private String reply;
    private String repliedByMobile;
    private LocalDateTime replyDate;

    public CustomerInquiryEntity(CustomerInquiry inquiry) {
        BeanUtils.copyProperties(inquiry, this);
    }

    public CustomerInquiry toCustomerInquiry() {
        CustomerInquiry inquiry = new CustomerInquiry();
        BeanUtils.copyProperties(this, inquiry);
        return inquiry;
    }
}