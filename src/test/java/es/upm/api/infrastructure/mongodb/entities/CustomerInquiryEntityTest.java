package es.upm.api.infrastructure.mongodb.entities;

import es.upm.api.domain.model.CustomerInquiry;
import es.upm.api.domain.model.InquiryCategory;
import es.upm.api.domain.model.InquiryState;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDateTime;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertEquals;

class CustomerInquiryEntityTest {

    private CustomerInquiry inquiry;

    @BeforeEach
    void setUp() {
        this.inquiry = CustomerInquiry.builder()
                .id(UUID.randomUUID())
                .registrationDate(LocalDateTime.of(2026, 6, 1, 10, 0))
                .customerMobile("customer1")
                .subject("My subject")
                .description("My description")
                .category(InquiryCategory.BILLING)
                .state(InquiryState.OPEN)
                .build();
    }

    @Test
    void shouldBuildEntityFromCustomerInquiry() {
        CustomerInquiryEntity entity = new CustomerInquiryEntity(this.inquiry);

        assertEquals(this.inquiry.getId(), entity.getId());
        assertEquals(this.inquiry.getRegistrationDate(), entity.getRegistrationDate());
        assertEquals(this.inquiry.getCustomerMobile(), entity.getCustomerMobile());
        assertEquals(this.inquiry.getSubject(), entity.getSubject());
        assertEquals(this.inquiry.getDescription(), entity.getDescription());
        assertEquals(this.inquiry.getCategory(), entity.getCategory());
        assertEquals(this.inquiry.getState(), entity.getState());
    }

    @Test
    void shouldConvertEntityToCustomerInquiry() {
        CustomerInquiryEntity entity = new CustomerInquiryEntity(this.inquiry);

        CustomerInquiry mapped = entity.toCustomerInquiry();

        assertEquals(entity.getId(), mapped.getId());
        assertEquals(entity.getCustomerMobile(), mapped.getCustomerMobile());
        assertEquals(entity.getSubject(), mapped.getSubject());
        assertEquals(entity.getDescription(), mapped.getDescription());
        assertEquals(entity.getCategory(), mapped.getCategory());
        assertEquals(entity.getState(), mapped.getState());
    }
}