package es.upm.api.domain.services;

import es.upm.api.domain.model.Review;
import es.upm.api.domain.ports.out.ReviewRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

@Service
public class ReviewService {

    private final ReviewRepository reviewRepository;

    @Autowired
    public ReviewService(ReviewRepository reviewRepository) {
        this.reviewRepository = reviewRepository;
    }

    /**
     * Upsert: creates a new review if none exists for (userId, letterId),
     * or updates the existing review otherwise.
     * Does NOT verify that letterId belongs to userId — goa-sandbox has no
     * EngagementLetter entity or goa-engagement client to validate ownership.
     */
    public Review createOrUpdate(String userId, String letterId, int stars, String opinion) {
        validateUserId(userId);
        validateLetterId(letterId);
        validateStars(stars);
        validateOpinion(opinion);

        Optional<Review> existing = this.reviewRepository.findByUserIdAndLetterId(userId, letterId);

        if (existing.isPresent()) {
            Review review = existing.get();
            review.setStars(stars);
            review.setOpinion(opinion);
            review.setUpdatedAt(LocalDateTime.now());
            return this.reviewRepository.update(review);
        } else {
            Review review = new Review();
            String rawId = userId + letterId + System.currentTimeMillis();
            review.setId(org.apache.commons.codec.digest.DigestUtils.sha256Hex(rawId));
            review.setUserId(userId);
            review.setLetterId(letterId);
            review.setStars(stars);
            review.setOpinion(opinion);
            review.setCreatedAt(LocalDateTime.now());
            review.setUpdatedAt(review.getCreatedAt());
            return this.reviewRepository.create(review);
        }
    }

    /**
     * Reads the review belonging to the given userId and letterId.
     * Throws NOT_FOUND if no such review exists.
     */
    public Optional<Review> readByUserIdAndLetterId(String userId, String letterId) {
        validateUserId(userId);
        validateLetterId(letterId);
        return this.reviewRepository.findByUserIdAndLetterId(userId, letterId);
    }

    private void validateUserId(String userId) {
        if (userId == null || userId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "User ID is required");
        }
    }

    private void validateLetterId(String letterId) {
        if (letterId == null || letterId.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Letter ID is required");
        }
    }

    private void validateStars(int stars) {
        if (stars < 1 || stars > 5) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Stars must be between 1 and 5");
        }
    }

    private void validateOpinion(String opinion) {
        if (opinion == null || opinion.isBlank()) {
            throw new ResponseStatusException(HttpStatus.BAD_REQUEST,
                    "Opinion is required");
        }
    }
}
