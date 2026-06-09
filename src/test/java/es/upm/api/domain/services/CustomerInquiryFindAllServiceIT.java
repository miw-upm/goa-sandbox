package es.upm.api.domain.services;

import es.upm.api.domain.model.CustomerInquiry;
import es.upm.api.domain.model.InquiryCategory;
import es.upm.api.domain.model.InquiryState;
import es.upm.api.domain.persistence.CustomerInquiryPersistence;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class CustomerInquiryFindAllServiceIT {

    @Autowired
    private CustomerInquiryService customerInquiryService;

    @MockitoBean
    private CustomerInquiryPersistence customerInquiryPersistence;

    @Test
    @WithMockUser(username = "customer1", roles = "customer")
    void shouldReturnOnlyOwnInquiriesForCustomer() {
        CustomerInquiry own = CustomerInquiry.builder()
                .id(UUID.randomUUID()).customer("customer1").state(InquiryState.OPEN)
                .subject("Mine").description("d").category(InquiryCategory.BILLING).build();
        when(this.customerInquiryPersistence.findByCustomer("customer1")).thenReturn(Stream.of(own));

        Stream<CustomerInquiry> result = this.customerInquiryService.findAll();

        assertEquals(own, result.findFirst().orElse(null));
        verify(this.customerInquiryPersistence).findByCustomer("customer1");
        verify(this.customerInquiryPersistence, never()).findAll();
    }

    @Test
    @WithMockUser(username = "manager1", roles = "manager")
    void shouldReturnAllInquiriesForManager() {
        CustomerInquiry i1 = CustomerInquiry.builder().id(UUID.randomUUID()).customer("c1").build();
        CustomerInquiry i2 = CustomerInquiry.builder().id(UUID.randomUUID()).customer("c2").build();
        when(this.customerInquiryPersistence.findAll()).thenReturn(Stream.of(i1, i2));

        long count = this.customerInquiryService.findAll().count();

        assertEquals(2, count);
        verify(this.customerInquiryPersistence).findAll();
        verify(this.customerInquiryPersistence, never()).findByCustomer(any());
    }
}