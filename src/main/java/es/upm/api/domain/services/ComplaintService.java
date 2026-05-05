package es.upm.api.domain.services;

import es.upm.api.domain.model.Complaint;
import es.upm.api.domain.model.ComplaintState;
import es.upm.api.domain.ports.out.ComplaintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

@Service
public class ComplaintService {

    private final ComplaintRepository complaintRepository;

    @Autowired
    public ComplaintService(ComplaintRepository complaintRepository) {
        this.complaintRepository = complaintRepository;
    }

    public Complaint create(Complaint complaint) {
        // 1. Validación de campos obligatorios (Barcode = Hoja de Encargo, Mobile = ID Cliente)
        if (complaint.getBarcode() == null || complaint.getMobile() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "El ID de la Hoja de Encargo (barcode) y del Cliente (mobile) son obligatorios");
        }

        // 2. Generar el Hash ID único
        // Usamos DigestUtils (de org.apache.commons.codec.digest) o String.hashCode() simple
        // Aquí lo hacemos robusto:
        String rawId = complaint.getBarcode() + complaint.getMobile() + ComplaintState.OPEN;
        String generatedId = org.apache.commons.codec.digest.DigestUtils.sha256Hex(rawId);
        complaint.setId(generatedId);

        // 3. Regla de Negocio: Solo una queja OPEN por Hoja de Encargo y Cliente
        if (this.complaintRepository.read(generatedId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe una queja abierta para esta Hoja de Encargo");
        }

        // 4. Configurar valores por defecto
        complaint.setRegistrationDate(LocalDateTime.now());
        complaint.setState(ComplaintState.OPEN); // Aseguramos que se guarda como OPEN
        complaint.setReply(null);

        // 5. Persistir
        return this.complaintRepository.create(complaint);
    }
}