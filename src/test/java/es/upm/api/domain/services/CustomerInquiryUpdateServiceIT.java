package es.upm.api.domain.services;

import es.upm.api.domain.model.CustomerInquiry;
import es.upm.api.domain.model.InquiryCategory;
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

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class CustomerInquiryUpdateServiceIT {

    @Autowired
    private CustomerInquiryService customerInquiryService;

    @MockitoBean
    private CustomerInquiryPersistence customerInquiryPersistence;

    @Test
    @WithMockUser(username = "customer1", roles = "customer")
    void shouldUpdateOwnOpenInquiry() {
        UUID id = UUID.randomUUID();
        CustomerInquiry existing = CustomerInquiry.builder()
                .id(id).customerMobile("customer1").state(InquiryState.OPEN)
                .subject("Old subject").description("Old desc").category(InquiryCategory.BILLING).build();
        CustomerInquiry patch = CustomerInquiry.builder()
                .subject("New subject").description("New desc").category(InquiryCategory.BILLING).build();
        when(this.customerInquiryPersistence.readById(id)).thenReturn(existing);
        when(this.customerInquiryPersistence.update(any())).thenAnswer(i -> i.getArgument(0));

        CustomerInquiry result = this.customerInquiryService.update(id, patch);

        assertEquals("New subject", result.getSubject());
        assertEquals("New desc", result.getDescription());
        verify(this.customerInquiryPersistence).update(any());
    }

    @Test
    @WithMockUser(username = "customer1", roles = "customer")
    void shouldThrowInvalidTransitionWhenInquiryNotOpen() {
        UUID id = UUID.randomUUID();
        CustomerInquiry existing = CustomerInquiry.builder()
                .id(id).customerMobile("customer1").state(InquiryState.ANSWERED).build();
        when(this.customerInquiryPersistence.readById(id)).thenReturn(existing);

        assertThrows(InvalidTransitionException.class,
                () -> this.customerInquiryService.update(id, new CustomerInquiry()));
        verify(this.customerInquiryPersistence, never()).update(any());
    }

    @Test
    @WithMockUser(username = "customer2", roles = "customer")
    void shouldThrowForbiddenWhenNotOwner() {
        UUID id = UUID.randomUUID();
        CustomerInquiry existing = CustomerInquiry.builder()
                .id(id).customerMobile("customer1").state(InquiryState.OPEN).build();
        when(this.customerInquiryPersistence.readById(id)).thenReturn(existing);

        assertThrows(ForbiddenException.class,
                () -> this.customerInquiryService.update(id, new CustomerInquiry()));
        verify(this.customerInquiryPersistence, never()).update(any());
    }
}