package es.upm.api.domain.services;

import es.upm.api.domain.model.CustomerInquiry;
import es.upm.api.domain.model.InquiryState;
import es.upm.api.domain.persistence.CustomerInquiryPersistence;
import es.upm.miw.exception.ConflictException;
import es.upm.miw.exception.ForbiddenException;
import es.upm.miw.exception.InvalidTransitionException;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.UUID;
import java.util.stream.Stream;

@Service
public class CustomerInquiryService {

    private final CustomerInquiryPersistence customerInquiryPersistence;

    public CustomerInquiryService(CustomerInquiryPersistence customerInquiryPersistence) {
        this.customerInquiryPersistence = customerInquiryPersistence;
    }

    public CustomerInquiry create(CustomerInquiry inquiry) {
        String currentUser = currentUsername();
        if (this.customerInquiryPersistence.findOpenByCustomerMobile(currentUser).isPresent()) {
            throw new ConflictException("Customer already has an open inquiry");
        }
        inquiry.setId(UUID.randomUUID());
        inquiry.setCustomerMobile(currentUser);
        inquiry.setRegistrationDate(LocalDateTime.now());
        inquiry.setState(InquiryState.OPEN);
        return this.customerInquiryPersistence.create(inquiry);
    }

    public Stream<CustomerInquiry> findAll() {
        if (hasRole("customer")) {
            return this.customerInquiryPersistence.findByCustomerMobile(currentUsername());
        }
        return this.customerInquiryPersistence.findAll();
    }

    public CustomerInquiry readById(UUID id) {
        CustomerInquiry inquiry = this.customerInquiryPersistence.readById(id);
        if (hasRole("customer") && !inquiry.getCustomerMobile().equals(currentUsername())) {
            throw new ForbiddenException("Access denied to inquiry: " + id);
        }
        return inquiry;
    }

    public CustomerInquiry update(UUID id, CustomerInquiry patch) {
        CustomerInquiry inquiry = this.customerInquiryPersistence.readById(id);
        assertOwner(inquiry);
        if (!InquiryState.OPEN.equals(inquiry.getState())) {
            throw new InvalidTransitionException("Inquiry must be OPEN to edit");
        }
        inquiry.setSubject(patch.getSubject());
        inquiry.setDescription(patch.getDescription());
        return this.customerInquiryPersistence.update(inquiry);
    }

    public void delete(UUID id) {
        CustomerInquiry inquiry = this.customerInquiryPersistence.readById(id);
        assertOwner(inquiry);
        if (!InquiryState.OPEN.equals(inquiry.getState())) {
            throw new InvalidTransitionException("Inquiry must be OPEN to delete");
        }
        this.customerInquiryPersistence.deleteById(id);
    }

    public CustomerInquiry reply(UUID id, String replyText) {
        CustomerInquiry inquiry = this.customerInquiryPersistence.readById(id);
        if (!InquiryState.OPEN.equals(inquiry.getState())) {
            throw new InvalidTransitionException("Inquiry must be OPEN to reply");
        }
        inquiry.setReply(replyText);
        inquiry.setRepliedByMobile(currentUsername());
        inquiry.setReplyDate(LocalDateTime.now());
        inquiry.setState(InquiryState.ANSWERED);
        return this.customerInquiryPersistence.update(inquiry);
    }

    public CustomerInquiry close(UUID id) {
        CustomerInquiry inquiry = this.customerInquiryPersistence.readById(id);
        if (!InquiryState.ANSWERED.equals(inquiry.getState())) {
            throw new InvalidTransitionException("Inquiry must be ANSWERED to close");
        }
        inquiry.setState(InquiryState.CLOSED);
        return this.customerInquiryPersistence.update(inquiry);
    }

    private String currentUsername() {
        return SecurityContextHolder.getContext().getAuthentication().getName();
    }

    private boolean hasRole(String role) {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();
        return auth.getAuthorities().stream()
                .anyMatch(a -> a.getAuthority().equalsIgnoreCase("ROLE_" + role));
    }

    private void assertOwner(CustomerInquiry inquiry) {
        if (!isAdmin() && !inquiry.getCustomerMobile().equals(currentUsername())) {
            throw new ForbiddenException("Access denied to inquiry: " + inquiry.getId());
        }
    }

    private boolean isAdmin() {
        return hasRole("admin");
    }
}