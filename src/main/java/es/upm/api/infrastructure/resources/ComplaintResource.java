package es.upm.api.infrastructure.resources;

import es.upm.api.domain.model.Complaint;
import es.upm.api.domain.services.ComplaintService;
import es.upm.miw.security.Security;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.util.stream.Stream;

@RestController
@RequestMapping(ComplaintResource.COMPLAINTS)
@PreAuthorize(Security.ADMIN_MANAGER_OPERATOR)
public class ComplaintResource {

    public static final String COMPLAINTS = "/complaints";

    private final ComplaintService complaintService;

    public ComplaintResource(ComplaintService complaintService) {
        this.complaintService = complaintService;
    }

    @PostMapping
    @Operation(summary = "Create complaint")
    public Complaint create(@Valid @RequestBody Complaint complaint) {
        return this.complaintService.create(complaint);
    }

    @GetMapping
    public Stream<Complaint> findAll() {
        return this.complaintService.findAll();
    }
}