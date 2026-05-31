package es.upm.api.infrastructure.mongodb.repositories;

import es.upm.api.infrastructure.mongodb.entities.ComplaintEntity;
import org.springframework.data.mongodb.repository.MongoRepository;

import java.util.UUID;

public interface ComplaintRepository extends MongoRepository<ComplaintEntity, UUID> {
}