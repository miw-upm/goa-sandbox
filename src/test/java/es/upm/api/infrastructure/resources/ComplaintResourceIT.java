package es.upm.api.infrastructure.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.upm.api.domain.model.Complaint;
import es.upm.api.domain.model.Status;
import es.upm.api.domain.services.ComplaintService;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@ActiveProfiles("test")
class ComplaintResourceIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockitoBean
    private ComplaintService complaintService;

    @Test
    @WithMockUser(roles = "admin")
    void shouldCreateComplaint() throws Exception {
        UUID complaintId = UUID.randomUUID();
        UUID engagementId = UUID.randomUUID();

        Complaint request = Complaint.builder()
                .engagementId(engagementId)
                .description("Service not as described")
                .status(Status.OPEN)
                .createdAt(LocalDateTime.of(2026, 5, 31, 10, 0))
                .build();

        Complaint response = Complaint.builder()
                .id(complaintId)
                .engagementId(engagementId)
                .description("Service not as described")
                .status(Status.OPEN)
                .createdAt(LocalDateTime.of(2026, 5, 31, 10, 0))
                .build();

        when(this.complaintService.create(any())).thenReturn(response);

        this.mockMvc.perform(post(ComplaintResource.COMPLAINTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(complaintId.toString()))
                .andExpect(jsonPath("$.engagementId").value(engagementId.toString()))
                .andExpect(jsonPath("$.description").value("Service not as described"))
                .andExpect(jsonPath("$.status").value("OPEN"));

        verify(this.complaintService).create(any());
    }

    @Test
    @WithMockUser(roles = "admin")
    void shouldCreateComplaintWhenCreatedAtHasNoSeconds() throws Exception {
        UUID complaintId = UUID.randomUUID();
        UUID engagementId = UUID.randomUUID();

        Complaint response = Complaint.builder()
                .id(complaintId)
                .engagementId(engagementId)
                .description("Service not as described")
                .status(Status.OPEN)
                .createdAt(LocalDateTime.of(2026, 7, 6, 1, 14))
                .build();

        when(this.complaintService.create(any())).thenReturn(response);

        this.mockMvc.perform(post(ComplaintResource.COMPLAINTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content("""
                                {
                                  "engagementId": "%s",
                                  "description": "Service not as described",
                                  "status": "OPEN",
                                  "createdAt": "2026-07-06T01:14"
                                }
                                """.formatted(engagementId)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value(complaintId.toString()))
                .andExpect(jsonPath("$.engagementId").value(engagementId.toString()))
                .andExpect(jsonPath("$.description").value("Service not as described"))
                .andExpect(jsonPath("$.status").value("OPEN"));

        ArgumentCaptor<Complaint> complaintCaptor = ArgumentCaptor.forClass(Complaint.class);
        verify(this.complaintService).create(complaintCaptor.capture());
        assertEquals(LocalDateTime.of(2026, 7, 6, 1, 14), complaintCaptor.getValue().getCreatedAt());
    }

    @Test
    @WithMockUser(roles = "admin")
    void shouldReturnBadRequestWhenEngagementIdIsNull() throws Exception {
        Complaint request = Complaint.builder()
                .engagementId(null)
                .description("Valid description")
                .build();

        this.mockMvc.perform(post(ComplaintResource.COMPLAINTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(this.complaintService, never()).create(any());
    }

    @Test
    @WithMockUser(roles = "admin")
    void shouldReturnBadRequestWhenDescriptionIsBlank() throws Exception {
        Complaint request = Complaint.builder()
                .engagementId(UUID.randomUUID())
                .description("")
                .build();

        this.mockMvc.perform(post(ComplaintResource.COMPLAINTS)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(this.objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());

        verify(this.complaintService, never()).create(any());
    }
}
