package es.upm.api.adapter.out.review;

import es.upm.api.domain.model.Review;
import es.upm.api.domain.ports.out.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public class ReviewPersistenceMongodb implements ReviewRepository {

    private final ReviewMongoRepository reviewMongoRepository;

    @Autowired
    public ReviewPersistenceMongodb(ReviewMongoRepository reviewMongoRepository) {
        this.reviewMongoRepository = reviewMongoRepository;
    }

    @Override
    public Review create(Review review) {
        ReviewEntity entity = new ReviewEntity(review);
        return this.reviewMongoRepository.save(entity).toReview();
    }

    @Override
    public Optional<Review> read(String id) {
        return this.reviewMongoRepository.findById(id)
                .map(ReviewEntity::toReview);
    }

    @Override
    public Review update(Review review) {
        ReviewEntity entity = new ReviewEntity(review);
        return this.reviewMongoRepository.save(entity).toReview();
    }

    @Override
    public boolean existsByUserIdAndLetterId(String userId, String letterId) {
        return this.reviewMongoRepository.existsByUserIdAndLetterId(userId, letterId);
    }

    @Override
    public Optional<Review> findByUserIdAndLetterId(String userId, String letterId) {
        return this.reviewMongoRepository.findByUserIdAndLetterId(userId, letterId)
                .map(ReviewEntity::toReview);
    }
}
