package es.upm.api.domain.persistence;

import es.upm.api.domain.model.CustomerInquiry;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface CustomerInquiryPersistence {
    CustomerInquiry create(CustomerInquiry inquiry);

    CustomerInquiry readById(UUID id);

    Stream<CustomerInquiry> findAll();

    Stream<CustomerInquiry> findByCustomerMobile(String customerMobile);

    CustomerInquiry update(CustomerInquiry inquiry);

    void deleteById(UUID id);

    Optional<CustomerInquiry> findOpenByCustomerMobile(String customerMobile);
}