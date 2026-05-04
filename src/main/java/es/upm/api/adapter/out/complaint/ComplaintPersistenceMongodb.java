package es.upm.api.adapter.out.complaint;

import es.upm.api.domain.model.Complaint;
import es.upm.api.domain.ports.out.ComplaintRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ComplaintPersistenceMongodb implements ComplaintRepository {

    private final ComplaintMongoRepository complaintMongoRepository;

    @Autowired
    public ComplaintPersistenceMongodb(ComplaintMongoRepository complaintMongoRepository) {
        this.complaintMongoRepository = complaintMongoRepository;
    }

    @Override
    public Complaint create(Complaint complaint) {
        // Convertimos el objeto de Dominio a Entidad de MongoDB
        ComplaintEntity entity = new ComplaintEntity(complaint);
        // Guardamos y volvemos a convertir a Dominio para devolverlo
        return this.complaintMongoRepository.save(entity).toComplaint();
    }

    @Override
    public Optional<Complaint> read(String id) {
        return this.complaintMongoRepository.findById(id)
                .map(ComplaintEntity::toComplaint);
    }
}