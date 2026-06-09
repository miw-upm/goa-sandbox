package es.upm.api.infrastructure.mongodb.persistence;

import es.upm.api.domain.model.CustomerInquiry;
import es.upm.api.domain.model.InquiryState;
import es.upm.api.domain.persistence.CustomerInquiryPersistence;
import es.upm.api.infrastructure.mongodb.entities.CustomerInquiryEntity;
import es.upm.api.infrastructure.mongodb.repositories.CustomerInquiryRepository;
import es.upm.miw.exception.NotFoundException;
import org.springframework.beans.BeanUtils;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

@Repository
public class CustomerInquiryPersistenceMongodb implements CustomerInquiryPersistence {
    public static final Sort REGISTRATION_DATE_DESC = Sort.by(Sort.Direction.DESC, "registrationDate");

    private final CustomerInquiryRepository customerInquiryRepository;

    public CustomerInquiryPersistenceMongodb(CustomerInquiryRepository customerInquiryRepository) {
        this.customerInquiryRepository = customerInquiryRepository;
    }

    @Override
    public CustomerInquiry create(CustomerInquiry inquiry) {
        return this.customerInquiryRepository.save(new CustomerInquiryEntity(inquiry)).toCustomerInquiry();
    }

    @Override
    public CustomerInquiry readById(UUID id) {
        return this.customerInquiryRepository.findById(id)
                .map(CustomerInquiryEntity::toCustomerInquiry)
                .orElseThrow(() -> new NotFoundException("CustomerInquiry id: " + id));
    }

    @Override
    public Stream<CustomerInquiry> findAll() {
        return this.customerInquiryRepository.findAll(REGISTRATION_DATE_DESC).stream()
                .map(CustomerInquiryEntity::toCustomerInquiry);
    }

    @Override
    public Stream<CustomerInquiry> findByCustomerMobile(String customerMobile) {
        return this.customerInquiryRepository.findByCustomerMobile(customerMobile).stream()
                .map(CustomerInquiryEntity::toCustomerInquiry);
    }

    @Override
    public CustomerInquiry update(CustomerInquiry inquiry) {
        CustomerInquiryEntity entity = this.customerInquiryRepository.findById(inquiry.getId())
                .orElseThrow(() -> new NotFoundException("CustomerInquiry id: " + inquiry.getId()));
        BeanUtils.copyProperties(inquiry, entity);
        return this.customerInquiryRepository.save(entity).toCustomerInquiry();
    }

    @Override
    public void deleteById(UUID id) {
        if (!this.customerInquiryRepository.existsById(id)) {
            throw new NotFoundException("CustomerInquiry id: " + id);
        }
        this.customerInquiryRepository.deleteById(id);
    }

    @Override
    public Optional<CustomerInquiry> findOpenByCustomerMobile(String customerMobile) {
        return this.customerInquiryRepository.findByCustomerMobileAndState(customerMobile, InquiryState.OPEN)
                .map(CustomerInquiryEntity::toCustomerInquiry);
    }
}