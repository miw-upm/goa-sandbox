package es.upm.api.domain.services;

import es.upm.api.domain.model.CustomerInquiry;
import es.upm.api.domain.model.InquiryState;
import es.upm.api.domain.persistence.CustomerInquiryPersistence;
import es.upm.miw.exception.ForbiddenException;
import es.upm.miw.exception.InvalidTransitionException;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;

import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class CustomerInquiryDeleteServiceIT {

    @Autowired
    private CustomerInquiryService customerInquiryService;

    @MockitoBean
    private CustomerInquiryPersistence customerInquiryPersistence;

    @Test
    @WithMockUser(username = "customer1", roles = "customer")
    void shouldDeleteOwnOpenInquiry() {
        UUID id = UUID.randomUUID();
        CustomerInquiry inquiry = CustomerInquiry.builder()
                .id(id).customerMobile("customer1").state(InquiryState.OPEN).build();
        when(this.customerInquiryPersistence.readById(id)).thenReturn(inquiry);

        this.customerInquiryService.delete(id);

        verify(this.customerInquiryPersistence).deleteById(id);
    }

    @Test
    @WithMockUser(username = "customer1", roles = "customer")
    void shouldThrowInvalidTransitionWhenNotOpen() {
        UUID id = UUID.randomUUID();
        CustomerInquiry inquiry = CustomerInquiry.builder()
                .id(id).customerMobile("customer1").state(InquiryState.ANSWERED).build();
        when(this.customerInquiryPersistence.readById(id)).thenReturn(inquiry);

        assertThrows(InvalidTransitionException.class, () -> this.customerInquiryService.delete(id));
        verify(this.customerInquiryPersistence, never()).deleteById(any());
    }

    @Test
    @WithMockUser(username = "customer2", roles = "customer")
    void shouldThrowForbiddenWhenNotOwner() {
        UUID id = UUID.randomUUID();
        CustomerInquiry inquiry = CustomerInquiry.builder()
                .id(id).customerMobile("customer1").state(InquiryState.OPEN).build();
        when(this.customerInquiryPersistence.readById(id)).thenReturn(inquiry);

        assertThrows(ForbiddenException.class, () -> this.customerInquiryService.delete(id));
        verify(this.customerInquiryPersistence, never()).deleteById(any());
    }
}