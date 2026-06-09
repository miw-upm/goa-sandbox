package es.upm.api.domain.services;

import es.upm.api.domain.model.CustomerInquiry;
import es.upm.api.domain.model.InquiryState;
import es.upm.api.domain.persistence.CustomerInquiryPersistence;
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
class CustomerInquiryCloseServiceIT {

    @Autowired
    private CustomerInquiryService customerInquiryService;

    @MockitoBean
    private CustomerInquiryPersistence customerInquiryPersistence;

    @Test
    @WithMockUser(username = "manager1", roles = "manager")
    void shouldCloseAnsweredInquiry() {
        UUID id = UUID.randomUUID();
        CustomerInquiry inquiry = CustomerInquiry.builder()
                .id(id).customerMobile("customer1").state(InquiryState.ANSWERED)
                .reply("Done").repliedByMobile("manager1").build();
        when(this.customerInquiryPersistence.readById(id)).thenReturn(inquiry);
        when(this.customerInquiryPersistence.update(any())).thenAnswer(i -> i.getArgument(0));

        CustomerInquiry result = this.customerInquiryService.close(id);

        assertEquals(InquiryState.CLOSED, result.getState());
        verify(this.customerInquiryPersistence).update(any());
    }

    @Test
    @WithMockUser(username = "manager1", roles = "manager")
    void shouldThrowInvalidTransitionWhenNotAnswered() {
        UUID id = UUID.randomUUID();
        CustomerInquiry inquiry = CustomerInquiry.builder()
                .id(id).customerMobile("customer1").state(InquiryState.OPEN).build();
        when(this.customerInquiryPersistence.readById(id)).thenReturn(inquiry);

        assertThrows(InvalidTransitionException.class, () -> this.customerInquiryService.close(id));
        verify(this.customerInquiryPersistence, never()).update(any());
    }
}