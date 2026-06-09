package es.upm.api.infrastructure.mongodb.repositories;

import es.upm.api.domain.model.InquiryState;
import es.upm.api.infrastructure.mongodb.entities.CustomerInquiryEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

public interface CustomerInquiryRepository extends MongoRepository<CustomerInquiryEntity, UUID> {
    List<CustomerInquiryEntity> findByCustomer(String customer);

    Optional<CustomerInquiryEntity> findByCustomerAndState(String customer, InquiryState state);
}