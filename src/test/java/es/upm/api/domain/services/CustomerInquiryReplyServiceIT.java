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
class CustomerInquiryReplyServiceIT {

    @Autowired
    private CustomerInquiryService customerInquiryService;

    @MockitoBean
    private CustomerInquiryPersistence customerInquiryPersistence;

    @Test
    @WithMockUser(username = "manager1", roles = "manager")
    void shouldReplyToOpenInquiry() {
        UUID id = UUID.randomUUID();
        CustomerInquiry inquiry = CustomerInquiry.builder()
                .id(id).customerMobile("customer1").state(InquiryState.OPEN).build();
        when(this.customerInquiryPersistence.readById(id)).thenReturn(inquiry);
        when(this.customerInquiryPersistence.update(any())).thenAnswer(i -> i.getArgument(0));

        CustomerInquiry result = this.customerInquiryService.reply(id, "Your issue is resolved");

        assertEquals(InquiryState.ANSWERED, result.getState());
        assertEquals("Your issue is resolved", result.getReply());
        assertEquals("manager1", result.getRepliedByMobile());
        assertNotNull(result.getReplyDate());
        verify(this.customerInquiryPersistence).update(any());
    }

    @Test
    @WithMockUser(username = "manager1", roles = "manager")
    void shouldThrowInvalidTransitionWhenNotOpen() {
        UUID id = UUID.randomUUID();
        CustomerInquiry inquiry = CustomerInquiry.builder()
                .id(id).customerMobile("customer1").state(InquiryState.ANSWERED).build();
        when(this.customerInquiryPersistence.readById(id)).thenReturn(inquiry);

        assertThrows(InvalidTransitionException.class,
                () -> this.customerInquiryService.reply(id, "response"));
        verify(this.customerInquiryPersistence, never()).update(any());
    }
}