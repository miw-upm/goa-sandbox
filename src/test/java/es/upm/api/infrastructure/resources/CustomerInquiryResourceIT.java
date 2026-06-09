package es.upm.api.infrastructure.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.upm.api.domain.model.CustomerInquiry;
import es.upm.api.domain.model.InquiryCategory;
import es.upm.api.domain.model.InquiryState;
import es.upm.api.domain.services.CustomerInquiryService;
import es.upm.miw.exception.ConflictException;
import es.upm.miw.exception.ForbiddenException;
import es.upm.miw.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Stream;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class CustomerInquiryResourceIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private CustomerInquiryService customerInquiryService;

    private CustomerInquiry inquiry;
    private UUID inquiryId;

    @BeforeEach
    void setUp() {
        this.inquiryId = UUID.randomUUID();
        this.inquiry = CustomerInquiry.builder()
                .id(this.inquiryId)
                .registrationDate(LocalDateTime.of(2026, 6, 1, 10, 0))
                .customer("customer1")
                .subject("Billing issue")
                .description("Charged twice")
                .category(InquiryCategory.BILLING)
                .state(InquiryState.OPEN)
                .build();
    }

    @Test
    @WithMockUser(username = "customer1", roles = "customer")
    void shouldCreateInquiry() throws Exception {
        when(this.customerInquiryService.create(any())).thenReturn(this.inquiry);

        this.mockMvc.perform(post(CustomerInquiryResource.INQUIRIES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(this.inquiry)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").value(this.inquiryId.toString()))
                .andExpect(jsonPath("$.state").value("OPEN"))
                .andExpect(jsonPath("$.customer").value("customer1"));

        verify(this.customerInquiryService).create(any());
    }

    @Test
    @WithMockUser(username = "customer1", roles = "customer")
    void shouldReturnConflictWhenAlreadyHasOpenInquiry() throws Exception {
        when(this.customerInquiryService.create(any())).thenThrow(new ConflictException("already open"));

        this.mockMvc.perform(post(CustomerInquiryResource.INQUIRIES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(this.inquiry)))
                .andExpect(status().isConflict());
    }

    @Test
    @WithMockUser(username = "customer1", roles = "customer")
    void shouldReturnBadRequestWhenSubjectIsBlank() throws Exception {
        CustomerInquiry bad = CustomerInquiry.builder()
                .subject("").description("desc").category(InquiryCategory.BILLING).build();

        this.mockMvc.perform(post(CustomerInquiryResource.INQUIRIES)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(bad)))
                .andExpect(status().isBadRequest());

        verify(this.customerInquiryService, never()).create(any());
    }

    @Test
    @WithMockUser(username = "customer1", roles = "customer")
    void shouldFindAllForCustomer() throws Exception {
        when(this.customerInquiryService.findAll()).thenReturn(Stream.of(this.inquiry));

        this.mockMvc.perform(get(CustomerInquiryResource.INQUIRIES))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].id").value(this.inquiryId.toString()))
                .andExpect(jsonPath("$[0].subject").value("Billing issue"));

        verify(this.customerInquiryService).findAll();
    }

    @Test
    @WithMockUser(username = "customer1", roles = "customer")
    void shouldReadById() throws Exception {
        when(this.customerInquiryService.readById(this.inquiryId)).thenReturn(this.inquiry);

        this.mockMvc.perform(get(CustomerInquiryResource.INQUIRIES + "/" + this.inquiryId))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(this.inquiryId.toString()));

        verify(this.customerInquiryService).readById(this.inquiryId);
    }

    @Test
    @WithMockUser(username = "customer1", roles = "customer")
    void shouldReturnNotFoundWhenInquiryDoesNotExist() throws Exception {
        when(this.customerInquiryService.readById(this.inquiryId))
                .thenThrow(new NotFoundException("CustomerInquiry id: " + this.inquiryId));

        this.mockMvc.perform(get(CustomerInquiryResource.INQUIRIES + "/" + this.inquiryId))
                .andExpect(status().isNotFound());
    }

    @Test
    @WithMockUser(username = "customer2", roles = "customer")
    void shouldReturnForbiddenWhenCustomerAccessesOtherInquiry() throws Exception {
        when(this.customerInquiryService.readById(this.inquiryId))
                .thenThrow(new ForbiddenException("Access denied"));

        this.mockMvc.perform(get(CustomerInquiryResource.INQUIRIES + "/" + this.inquiryId))
                .andExpect(status().isForbidden());
    }

    @Test
    @WithMockUser(username = "customer1", roles = "customer")
    void shouldUpdateInquiry() throws Exception {
        CustomerInquiry updated = CustomerInquiry.builder()
                .id(this.inquiryId).subject("New subject").description("New desc")
                .category(InquiryCategory.BILLING).build();
        CustomerInquiry response = CustomerInquiry.builder()
                .id(this.inquiryId).customer("customer1").subject("New subject")
                .description("New desc").category(InquiryCategory.BILLING).state(InquiryState.OPEN).build();

        when(this.customerInquiryService.update(eq(this.inquiryId), any())).thenReturn(response);

        this.mockMvc.perform(put(CustomerInquiryResource.INQUIRIES + "/" + this.inquiryId)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(updated)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.subject").value("New subject"));
    }

    @Test
    @WithMockUser(username = "customer1", roles = "customer")
    void shouldDeleteInquiry() throws Exception {
        doNothing().when(this.customerInquiryService).delete(this.inquiryId);

        this.mockMvc.perform(delete(CustomerInquiryResource.INQUIRIES + "/" + this.inquiryId))
                .andExpect(status().isNoContent());

        verify(this.customerInquiryService).delete(this.inquiryId);
    }

    @Test
    @WithMockUser(username = "manager1", roles = "manager")
    void shouldReplyToInquiry() throws Exception {
        CustomerInquiry answered = CustomerInquiry.builder()
                .id(this.inquiryId).state(InquiryState.ANSWERED).reply("We are looking into it").build();
        when(this.customerInquiryService.reply(eq(this.inquiryId), any())).thenReturn(answered);

        this.mockMvc.perform(patch(CustomerInquiryResource.INQUIRIES + "/" + this.inquiryId + "/reply")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("\"We are looking into it\""))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("ANSWERED"));
    }

    @Test
    @WithMockUser(username = "manager1", roles = "manager")
    void shouldCloseInquiry() throws Exception {
        CustomerInquiry closed = CustomerInquiry.builder()
                .id(this.inquiryId).state(InquiryState.CLOSED).build();
        when(this.customerInquiryService.close(this.inquiryId)).thenReturn(closed);

        this.mockMvc.perform(patch(CustomerInquiryResource.INQUIRIES + "/" + this.inquiryId + "/close"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.state").value("CLOSED"));
    }
}