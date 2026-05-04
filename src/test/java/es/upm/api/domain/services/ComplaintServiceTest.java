package es.upm.api.domain.services;

import es.upm.api.domain.model.Complaint;
import es.upm.api.domain.ports.out.ComplaintRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.server.ResponseStatusException;

import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ComplaintServiceTest {

    @Mock
    private ComplaintRepository complaintRepository;

    @InjectMocks
    private ComplaintService complaintService;

    @Test
    void testCreateDuplicateComplaintThrowsConflict() {
        Complaint complaint = new Complaint();
        complaint.setBarcode("123");
        complaint.setUserId("user1");

        // Simulamos que la queja ya existe en la BD
        when(complaintRepository.read(anyString())).thenReturn(Optional.of(complaint));

        assertThrows(ResponseStatusException.class, () -> {
            complaintService.create(complaint);
        });
    }
}