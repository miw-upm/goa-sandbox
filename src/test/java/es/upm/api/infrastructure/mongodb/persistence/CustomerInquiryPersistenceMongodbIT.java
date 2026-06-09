package es.upm.api.infrastructure.mongodb.persistence;

import es.upm.api.domain.model.CustomerInquiry;
import es.upm.api.domain.model.InquiryCategory;
import es.upm.api.domain.model.InquiryState;
import es.upm.api.infrastructure.mongodb.entities.CustomerInquiryEntity;
import es.upm.api.infrastructure.mongodb.repositories.CustomerInquiryRepository;
import es.upm.miw.exception.NotFoundException;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.ArgumentCaptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.bean.override.mockito.MockitoBean;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@SpringBootTest
@ActiveProfiles("test")
class CustomerInquiryPersistenceMongodbIT {

    @Autowired
    private CustomerInquiryPersistenceMongodb persistence;

    @MockitoBean
    private CustomerInquiryRepository customerInquiryRepository;

    private CustomerInquiry inquiry;

    @BeforeEach
    void setUp() {
        this.inquiry = CustomerInquiry.builder()
                .id(UUID.randomUUID())
                .registrationDate(LocalDateTime.of(2026, 6, 1, 10, 0))
                .customer("customer1")
                .subject("Billing issue")
                .description("Charged twice")
                .category(InquiryCategory.BILLING)
                .state(InquiryState.OPEN)
                .build();
    }

    @Test
    void shouldCreateInquiry() {
        when(this.customerInquiryRepository.save(any())).thenAnswer(i -> i.getArgument(0));

        CustomerInquiry result = this.persistence.create(this.inquiry);

        ArgumentCaptor<CustomerInquiryEntity> captor = ArgumentCaptor.forClass(CustomerInquiryEntity.class);
        verify(this.customerInquiryRepository).save(captor.capture());
        assertEquals(this.inquiry.getId(), captor.getValue().getId());
        assertEquals(this.inquiry, result);
    }

    @Test
    void shouldReadById() {
        when(this.customerInquiryRepository.findById(this.inquiry.getId()))
                .thenReturn(Optional.of(new CustomerInquiryEntity(this.inquiry)));

        CustomerInquiry result = this.persistence.readById(this.inquiry.getId());

        assertEquals(this.inquiry, result);
    }

    @Test
    void shouldThrowNotFoundWhenReadByIdMissing() {
        when(this.customerInquiryRepository.findById(this.inquiry.getId())).thenReturn(Optional.empty());

        assertThrows(NotFoundException.class, () -> this.persistence.readById(this.inquiry.getId()));
    }

    @Test
    void shouldFindAll() {
        when(this.customerInquiryRepository.findAll(CustomerInquiryPersistenceMongodb.REGISTRATION_DATE_DESC))
                .thenReturn(List.of(new CustomerInquiryEntity(this.inquiry)));

        Stream<CustomerInquiry> result = this.persistence.findAll();

        assertEquals(this.inquiry, result.findFirst().orElse(null));
    }

    @Test
    void shouldFindByCustomer() {
        when(this.customerInquiryRepository.findByCustomer("customer1"))
                .thenReturn(List.of(new CustomerInquiryEntity(this.inquiry)));

        Stream<CustomerInquiry> result = this.persistence.findByCustomer("customer1");

        assertEquals(this.inquiry, result.findFirst().orElse(null));
    }

    @Test
    void shouldFindOpenByCustomer() {
        when(this.customerInquiryRepository.findByCustomerAndState("customer1", InquiryState.OPEN))
                .thenReturn(Optional.of(new CustomerInquiryEntity(this.inquiry)));

        Optional<CustomerInquiry> result = this.persistence.findOpenByCustomer("customer1");

        assertTrue(result.isPresent());
        assertEquals(this.inquiry, result.get());
    }

    @Test
    void shouldDeleteById() {
        when(this.customerInquiryRepository.existsById(this.inquiry.getId())).thenReturn(true);

        this.persistence.deleteById(this.inquiry.getId());

        verify(this.customerInquiryRepository).deleteById(this.inquiry.getId());
    }

    @Test
    void shouldThrowNotFoundWhenDeleteMissing() {
        when(this.customerInquiryRepository.existsById(this.inquiry.getId())).thenReturn(false);

        assertThrows(NotFoundException.class, () -> this.persistence.deleteById(this.inquiry.getId()));
        verify(this.customerInquiryRepository, never()).deleteById(any());
    }
}