package es.upm.api.adapter.out.review;

import es.upm.api.domain.model.Review;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.BeanUtils;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@Document(collection = "reviews")
public class ReviewEntity {
    @Id
    private String id;
    private String userId;
    private String letterId;
    private int stars;
    private String opinion;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public ReviewEntity(Review review) {
        BeanUtils.copyProperties(review, this);
    }

    public Review toReview() {
        Review review = new Review();
        BeanUtils.copyProperties(this, review);
        return review;
    }
}
