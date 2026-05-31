package es.upm.api.infrastructure.mongodb.persistence;

import es.upm.api.domain.model.Complaint;
import es.upm.api.domain.persistence.ComplaintPersistence;
import es.upm.api.infrastructure.mongodb.entities.ComplaintEntity;
import es.upm.api.infrastructure.mongodb.repositories.ComplaintRepository;
import org.springframework.stereotype.Repository;

@Repository
public class ComplaintPersistenceMongodb implements ComplaintPersistence {

    private final ComplaintRepository complaintRepository;

    public ComplaintPersistenceMongodb(ComplaintRepository complaintRepository) {
        this.complaintRepository = complaintRepository;
    }

    @Override
    public void create(Complaint complaint) {
        this.complaintRepository.save(new ComplaintEntity(complaint));
    }
}