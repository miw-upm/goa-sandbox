package es.upm.api.domain.services;

import es.upm.api.domain.model.CustomerInquiry;
import es.upm.api.domain.model.InquiryCategory;
import es.upm.api.domain.model.InquiryState;
import es.upm.api.domain.persistence.CustomerInquiryPersistence;
import es.upm.miw.exception.ConflictException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.Optional;
import java.util.UUID;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class CustomerInquiryServiceIT {

    @Autowired
    private CustomerInquiryService customerInquiryService;

    @MockitoBean
    private CustomerInquiryPersistence customerInquiryPersistence;

    private CustomerInquiry inquiry;

    @BeforeEach
    void setUp() {
        this.inquiry = CustomerInquiry.builder()
                .subject("Issue with billing")
                .description("I was charged twice")
                .category(InquiryCategory.BILLING)
                .build();
    }

    @Test
    @WithMockUser(username = "customer1", roles = "customer")
    void shouldCreateInquiry() {
        when(this.customerInquiryPersistence.findOpenByCustomer("customer1")).thenReturn(Optional.empty());
        when(this.customerInquiryPersistence.create(any())).thenAnswer(i -> i.getArgument(0));

        CustomerInquiry result = this.customerInquiryService.create(this.inquiry);

        assertNotNull(result.getId());
        assertEquals("customer1", result.getCustomer());
        assertEquals(InquiryState.OPEN, result.getState());
        assertNotNull(result.getRegistrationDate());
        verify(this.customerInquiryPersistence).create(any());
    }

    @Test
    @WithMockUser(username = "customer1", roles = "customer")
    void shouldThrowConflictWhenCustomerAlreadyHasOpenInquiry() {
        CustomerInquiry existing = CustomerInquiry.builder()
                .id(UUID.randomUUID()).customer("customer1").state(InquiryState.OPEN).build();
        when(this.customerInquiryPersistence.findOpenByCustomer("customer1")).thenReturn(Optional.of(existing));

        assertThrows(ConflictException.class, () -> this.customerInquiryService.create(this.inquiry));
        verify(this.customerInquiryPersistence, never()).create(any());
    }
}