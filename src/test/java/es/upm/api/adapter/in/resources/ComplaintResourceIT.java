package es.upm.api.adapter.in.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.upm.api.domain.model.Complaint;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

// IMPORTANT: Import this for the .with(jwt()) method
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc // Remove addFilters = false to let Security work
@ActiveProfiles("test")
class ComplaintResourceIT {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Test
    void testCreateComplaint() throws Exception {
        Complaint complaint = new Complaint();
        complaint.setBarcode("84111111111");
        complaint.setMobile("600000000");
        complaint.setDescription("Test integration");

        mockMvc.perform(post("/complaints")
                        .with(jwt()
                                .jwt(builder -> builder
                                        .claim("username", "6") // CLAVE: Coincidir con jwt.getClaimAsString("username")
                                        .subject("6"))
                                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")) // Prefijo ROLE_ indispensable
                        )
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(complaint)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").exists())
                .andExpect(jsonPath("$.barcode").value("84111111111"));
    }
}