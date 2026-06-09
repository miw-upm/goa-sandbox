package es.upm.api.infrastructure.mongodb.persistence;

import es.upm.api.domain.model.Complaint;
import es.upm.api.domain.persistence.ComplaintPersistence;
import es.upm.api.infrastructure.mongodb.entities.ComplaintEntity;
import es.upm.api.infrastructure.mongodb.repositories.ComplaintRepository;
import es.upm.miw.exception.NotFoundException;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Repository;

import java.util.UUID;
import java.util.stream.Stream;

@Repository
public class ComplaintPersistenceMongodb implements ComplaintPersistence {
    public static final Sort CREATEDAT = Sort.by(Sort.Direction.DESC, "createdAt");

    private final ComplaintRepository complaintRepository;

    public ComplaintPersistenceMongodb(ComplaintRepository complaintRepository) {
        this.complaintRepository = complaintRepository;
    }

    @Override
    public void create(Complaint complaint) {
        this.complaintRepository.save(new ComplaintEntity(complaint));
    }

    @Override
    public Complaint readById(UUID id) {
        return this.complaintRepository.findById(id)
                .map(ComplaintEntity::toComplaint)
                .orElseThrow(() -> new NotFoundException("Complaint id: " + id));
    }

    @Override
    public Complaint update(UUID id, Complaint complaint) {
        ComplaintEntity complaintEntity = this.complaintRepository.findById(id)
                .orElseThrow(() -> new NotFoundException("Complaint id: " + id));
        // Mapeo de campos de actualización
        complaintEntity.setEngagementId(complaint.getEngagementId());
        complaintEntity.setMobile(complaint.getMobile());
        complaintEntity.setDescription(complaint.getDescription());
        complaintEntity.setStatus(complaint.getStatus());
        complaintEntity.setCreatedAt(complaint.getCreatedAt());

        return this.complaintRepository.save(complaintEntity).toComplaint();
    }

    public Stream<Complaint> findAll() {
        return this.complaintRepository.findAll(CREATEDAT).stream()
                .map(ComplaintEntity::toComplaint);
    }


}