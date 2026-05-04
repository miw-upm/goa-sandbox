package es.upm.api.adapter.in.resources;

import es.upm.api.domain.model.Complaint;
import es.upm.api.domain.services.ComplaintService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/complaints") // La ruta real será /api/goa-sandbox/complaints por el Gateway
public class ComplaintResource {

    private final ComplaintService complaintService;

    @Autowired
    public ComplaintResource(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER') or hasRole('MANAGER') or hasRole('ADMIN')")
    public Complaint create(@Valid @RequestBody Complaint complaint, @AuthenticationPrincipal Jwt jwt) {
        // Extraemos el userId del token JWT automáticamente por seguridad
        // El campo suele llamarse "sub" o "username" dependiendo de tu goa-user
        String userId = jwt.getClaimAsString("username");
        complaint.setUserId(userId);

        return this.complaintService.create(complaint);
    }
}