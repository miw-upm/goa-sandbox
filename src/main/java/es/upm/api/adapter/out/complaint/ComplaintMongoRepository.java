package es.upm.api.adapter.out.complaint;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ComplaintMongoRepository extends MongoRepository<ComplaintEntity, String> {
    // Aquí podrías añadir métodos como:
    // List<ComplaintEntity> findByUserId(String userId);
}