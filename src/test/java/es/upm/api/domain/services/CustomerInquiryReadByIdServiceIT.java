package es.upm.api.domain.services;

import es.upm.api.domain.model.CustomerInquiry;
import es.upm.api.domain.model.InquiryCategory;
import es.upm.api.domain.model.InquiryState;
import es.upm.api.domain.persistence.CustomerInquiryPersistence;
import es.upm.miw.exception.ForbiddenException;
import es.upm.miw.exception.NotFoundException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class CustomerInquiryReadByIdServiceIT {

    @Autowired
    private CustomerInquiryService customerInquiryService;

    @MockitoBean
    private CustomerInquiryPersistence customerInquiryPersistence;

    @Test
    @WithMockUser(username = "customer1", roles = "customer")
    void shouldReadOwnInquiry() {
        UUID id = UUID.randomUUID();
        CustomerInquiry inquiry = CustomerInquiry.builder()
                .id(id).customerMobile("customer1").state(InquiryState.OPEN)
                .subject("s").description("d").category(InquiryCategory.BILLING).build();
        when(this.customerInquiryPersistence.readById(id)).thenReturn(inquiry);

        CustomerInquiry result = this.customerInquiryService.readById(id);

        assertEquals(inquiry, result);
        verify(this.customerInquiryPersistence).readById(id);
    }

    @Test
    @WithMockUser(username = "customer2", roles = "customer")
    void shouldThrowForbiddenWhenCustomerAccessesOtherInquiry() {
        UUID id = UUID.randomUUID();
        CustomerInquiry inquiry = CustomerInquiry.builder()
                .id(id).customerMobile("customer1").state(InquiryState.OPEN).build();
        when(this.customerInquiryPersistence.readById(id)).thenReturn(inquiry);

        assertThrows(ForbiddenException.class, () -> this.customerInquiryService.readById(id));
    }

    @Test
    @WithMockUser(username = "manager1", roles = "manager")
    void shouldReadAnyInquiryAsManager() {
        UUID id = UUID.randomUUID();
        CustomerInquiry inquiry = CustomerInquiry.builder()
                .id(id).customerMobile("customer1").state(InquiryState.OPEN).build();
        when(this.customerInquiryPersistence.readById(id)).thenReturn(inquiry);

        CustomerInquiry result = this.customerInquiryService.readById(id);

        assertEquals(inquiry, result);
    }

    @Test
    @WithMockUser(username = "customer1", roles = "customer")
    void shouldThrowNotFoundWhenInquiryDoesNotExist() {
        UUID id = UUID.randomUUID();
        when(this.customerInquiryPersistence.readById(id)).thenThrow(new NotFoundException("CustomerInquiry id: " + id));

        assertThrows(NotFoundException.class, () -> this.customerInquiryService.readById(id));
    }
}