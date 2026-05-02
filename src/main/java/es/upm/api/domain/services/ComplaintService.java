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
        // 1. Validar que los datos mínimos vienen presentes
        if (complaint.getBarcode() == null || complaint.getUserId() == null) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST, "Barcode y UserId son obligatorios");
        }

        // 2. Generar el ID único basado en el alcance: barcode + userId + state
        // Usamos OPEN porque es el estado inicial obligatorio
        String generatedId = complaint.getBarcode() + "-" + complaint.getUserId() + "-" + ComplaintState.OPEN;
        complaint.setId(generatedId);

        // 3. Garantizar que solo exista una queja OPEN (regla de negocio)
        // Si el read(id) encuentra algo, significa que ya hay una queja OPEN para este producto/usuario
        if (this.complaintRepository.read(generatedId).isPresent()) {
            throw new ResponseStatusException(HttpStatus.CONFLICT,
                    "Ya existe una queja abierta para este producto y usuario");
        }

        // 4. Configurar valores por defecto para una nueva queja
        complaint.setRegistrationDate(LocalDateTime.now());
        complaint.setState(ComplaintState.OPEN);
        complaint.setReply(null); // No hay respuesta al crearla

        // 5. Persistir
        return this.complaintRepository.create(complaint);
    }
}