package es.upm.api.domain.persistence;

import es.upm.api.domain.model.Complaint;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.stream.Stream;

@Repository
public interface ComplaintPersistence {
    void create(Complaint complaint);

    Complaint readById(UUID id);

    Complaint update(UUID id, Complaint complaint);

    Stream<Complaint> findAll();
}