package es.upm.api.infrastructure.resources;

import es.upm.api.domain.model.Complaint;
import es.upm.api.domain.services.ComplaintService;
import es.upm.api.infrastructure.resources.dtos.ComplaintUpdateRequest;
import es.upm.miw.security.Security;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.security.SecurityRequirement;
import jakarta.validation.Valid;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
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

    @GetMapping("/{id}")
    @Operation(summary = "Read complaint by id")
    public Complaint readById(@PathVariable UUID id) {
        return this.complaintService.readById(id);
    }

    @GetMapping
    public Stream<Complaint> findAll() {
        return this.complaintService.findAll();
    }

    @PutMapping("/{id}")
    @Operation(summary = "Update complaint")
    @SecurityRequirement(name = "bearerAuth")
    @PreAuthorize(Security.ADMIN_MANAGER_OPERATOR)
    public Complaint update(@PathVariable UUID id, @Valid @RequestBody ComplaintUpdateRequest request) {
        return this.complaintService.update(
                id,
                Complaint.builder()
                        .engagementId(request.getEngagementId())
                        .mobile(request.getMobile())
                        .description(request.getDescription())
                        .status(request.getStatus())
                        .createdAt(request.getCreatedAt())
                        .build()
        );
    }
}