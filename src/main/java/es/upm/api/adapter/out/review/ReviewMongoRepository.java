package es.upm.api.adapter.out.review;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface ReviewMongoRepository extends MongoRepository<ReviewEntity, String> {
    boolean existsByUserIdAndLetterId(String userId, String letterId);

    Optional<ReviewEntity> findByUserIdAndLetterId(String userId, String letterId);
}
