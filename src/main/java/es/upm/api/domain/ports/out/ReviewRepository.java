package es.upm.api.domain.ports.out;

import es.upm.api.domain.model.Review;

import java.util.Optional;

public interface ReviewRepository {
    Review create(Review review);

    Optional<Review> read(String id);

    Review update(Review review);

    boolean existsByUserIdAndLetterId(String userId, String letterId);

    Optional<Review> findByUserIdAndLetterId(String userId, String letterId);
}
