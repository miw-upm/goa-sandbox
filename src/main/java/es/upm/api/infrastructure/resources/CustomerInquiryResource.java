package es.upm.api.infrastructure.resources;

import es.upm.api.domain.model.CustomerInquiry;
import es.upm.api.domain.services.CustomerInquiryService;
import es.upm.miw.security.Security;
import io.swagger.v3.oas.annotations.Operation;
import jakarta.validation.Valid;
import org.springframework.http.HttpStatus;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.UUID;
import java.util.stream.Stream;

@RestController
@RequestMapping(CustomerInquiryResource.INQUIRIES)
public class CustomerInquiryResource {

    public static final String INQUIRIES = "/inquiries";
    public static final String ID = "/{id}";
    public static final String REPLY = "/{id}/reply";
    public static final String CLOSE = "/{id}/close";

    private final CustomerInquiryService customerInquiryService;

    public CustomerInquiryResource(CustomerInquiryService customerInquiryService) {
        this.customerInquiryService = customerInquiryService;
    }

    @PostMapping
    @ResponseStatus(HttpStatus.CREATED)
    @PreAuthorize("hasAnyRole('admin','customer')")
    @Operation(summary = "Create inquiry (CUSTOMER)")
    public CustomerInquiry create(@Valid @RequestBody CustomerInquiry inquiry) {
        return this.customerInquiryService.create(inquiry);
    }

    @GetMapping
    @PreAuthorize(Security.ADMIN_MANAGER_OPERATOR_CUSTOMER)
    @Operation(summary = "List inquiries")
    public Stream<CustomerInquiry> findAll() {
        return this.customerInquiryService.findAll();
    }

    @GetMapping(ID)
    @PreAuthorize(Security.ADMIN_MANAGER_OPERATOR_CUSTOMER)
    @Operation(summary = "Get inquiry by id")
    public CustomerInquiry readById(@PathVariable UUID id) {
        return this.customerInquiryService.readById(id);
    }

    @PutMapping(ID)
    @PreAuthorize("hasAnyRole('admin','customer')")
    @Operation(summary = "Edit subject and description (CUSTOMER, OPEN)")
    public CustomerInquiry update(@PathVariable UUID id, @Valid @RequestBody CustomerInquiry patch) {
        return this.customerInquiryService.update(id, patch);
    }

    @DeleteMapping(ID)
    @ResponseStatus(HttpStatus.NO_CONTENT)
    @PreAuthorize("hasAnyRole('admin','customer')")
    @Operation(summary = "Delete inquiry (CUSTOMER, OPEN)")
    public void delete(@PathVariable UUID id) {
        this.customerInquiryService.delete(id);
    }

    @PatchMapping(REPLY)
    @PreAuthorize(Security.ADMIN_MANAGER)
    @Operation(summary = "Reply to inquiry (MANAGER, OPEN→ANSWERED)")
    public CustomerInquiry reply(@PathVariable UUID id, @RequestBody String reply) {
        return this.customerInquiryService.reply(id, reply);
    }

    @PatchMapping(CLOSE)
    @PreAuthorize(Security.ADMIN_MANAGER)
    @Operation(summary = "Close inquiry (MANAGER, ANSWERED→CLOSED)")
    public CustomerInquiry close(@PathVariable UUID id) {
        return this.customerInquiryService.close(id);
    }
}