package es.upm.api.domain.services;

import es.upm.api.domain.model.Review;
import es.upm.api.domain.ports.out.ReviewRepository;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.http.HttpStatus;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;
import java.util.Optional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
class ReviewServiceTest {

    @Mock
    private ReviewRepository reviewRepository;

    @InjectMocks
    private ReviewService reviewService;

    // -------------------------------------------------------------------------
    // A — create new review when none exists
    // -------------------------------------------------------------------------

    @Test
    void createOrUpdate_createsNewReview_whenNoneExists() {
        when(reviewRepository.findByUserIdAndLetterId("u1", "L1"))
                .thenReturn(Optional.empty());
        when(reviewRepository.create(any(Review.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Review result = reviewService.createOrUpdate("u1", "L1", 5, "Great");

        assertEquals("u1", result.getUserId());
        assertEquals("L1", result.getLetterId());
        assertEquals(5, result.getStars());
        assertEquals("Great", result.getOpinion());
        assertNotNull(result.getId());
        assertFalse(result.getId().isBlank());
        assertNotNull(result.getCreatedAt());
        assertNotNull(result.getUpdatedAt());

        verify(reviewRepository).create(any(Review.class));
        verify(reviewRepository, never()).update(any());
    }

    @Test
    void createOrUpdate_neverCallsCreate_whenReviewAlreadyExists() {
        Review existing = new Review();
        existing.setId("existing-id");
        existing.setUserId("u1");
        existing.setLetterId("L1");
        existing.setStars(1);
        existing.setOpinion("Old");
        existing.setCreatedAt(LocalDateTime.of(2025, 1, 1, 0, 0));
        existing.setUpdatedAt(LocalDateTime.of(2025, 1, 1, 0, 0));

        when(reviewRepository.findByUserIdAndLetterId("u1", "L1"))
                .thenReturn(Optional.of(existing));
        when(reviewRepository.update(any(Review.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Review result = reviewService.createOrUpdate("u1", "L1", 3, "Updated");

        verify(reviewRepository, never()).create(any());
        verify(reviewRepository).update(any(Review.class));
    }

    // -------------------------------------------------------------------------
    // B — update existing review preserves id/userId/letterId/createdAt
    // -------------------------------------------------------------------------

    @Test
    void createOrUpdate_preservesIdAndCreatedAt_whenUpdating() {
        LocalDateTime originalCreated = LocalDateTime.of(2025, 1, 1, 0, 0);
        Review existing = new Review();
        existing.setId("preserve-id");
        existing.setUserId("u1");
        existing.setLetterId("L1");
        existing.setStars(2);
        existing.setOpinion("Old opinion");
        existing.setCreatedAt(originalCreated);
        existing.setUpdatedAt(originalCreated);

        when(reviewRepository.findByUserIdAndLetterId("u1", "L1"))
                .thenReturn(Optional.of(existing));
        when(reviewRepository.update(any(Review.class)))
                .thenAnswer(invocation -> invocation.getArgument(0));

        Review result = reviewService.createOrUpdate("u1", "L1", 4, "New opinion");

        assertEquals("preserve-id", result.getId());
        assertEquals("u1", result.getUserId());
        assertEquals("L1", result.getLetterId());
        assertEquals(originalCreated, result.getCreatedAt());
        assertEquals(4, result.getStars());
        assertEquals("New opinion", result.getOpinion());
        assertNotEquals(originalCreated, result.getUpdatedAt());
    }

    // -------------------------------------------------------------------------
    // C — reject invalid stars
    // -------------------------------------------------------------------------

    @Test
    void createOrUpdate_throwsBadRequest_whenStarsZero() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reviewService.createOrUpdate("u1", "L1", 0, "opinion"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verifyNoInteractions(reviewRepository);
    }

    @Test
    void createOrUpdate_throwsBadRequest_whenStarsSix() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reviewService.createOrUpdate("u1", "L1", 6, "opinion"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verifyNoInteractions(reviewRepository);
    }

    @Test
    void createOrUpdate_throwsBadRequest_whenStarsNegative() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reviewService.createOrUpdate("u1", "L1", -1, "opinion"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verifyNoInteractions(reviewRepository);
    }

    // -------------------------------------------------------------------------
    // D — reject blank/null fields
    // -------------------------------------------------------------------------

    @Test
    void createOrUpdate_throwsBadRequest_whenUserIdBlank() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reviewService.createOrUpdate("  ", "L1", 5, "opinion"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verifyNoInteractions(reviewRepository);
    }

    @Test
    void createOrUpdate_throwsBadRequest_whenUserIdNull() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reviewService.createOrUpdate(null, "L1", 5, "opinion"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verifyNoInteractions(reviewRepository);
    }

    @Test
    void createOrUpdate_throwsBadRequest_whenLetterIdBlank() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reviewService.createOrUpdate("u1", "", 5, "opinion"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verifyNoInteractions(reviewRepository);
    }

    @Test
    void createOrUpdate_throwsBadRequest_whenLetterIdNull() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reviewService.createOrUpdate("u1", null, 5, "opinion"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verifyNoInteractions(reviewRepository);
    }

    @Test
    void createOrUpdate_throwsBadRequest_whenOpinionBlank() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reviewService.createOrUpdate("u1", "L1", 5, "   "));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verifyNoInteractions(reviewRepository);
    }

    @Test
    void createOrUpdate_throwsBadRequest_whenOpinionNull() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reviewService.createOrUpdate("u1", "L1", 5, null));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verifyNoInteractions(reviewRepository);
    }

    // -------------------------------------------------------------------------
    // E — read existing review
    // -------------------------------------------------------------------------

    @Test
    void readByUserIdAndLetterId_returnsReview_whenExists() {
        Review review = new Review();
        review.setId("r1");
        review.setUserId("u1");
        review.setLetterId("L1");
        review.setStars(4);
        review.setOpinion("Good");

        when(reviewRepository.findByUserIdAndLetterId("u1", "L1"))
                .thenReturn(Optional.of(review));

        Review result = reviewService.readByUserIdAndLetterId("u1", "L1");

        assertEquals("r1", result.getId());
        assertEquals("u1", result.getUserId());
        assertEquals("L1", result.getLetterId());
        assertEquals(4, result.getStars());
        assertEquals("Good", result.getOpinion());
    }

    // -------------------------------------------------------------------------
    // F — read missing review throws NOT_FOUND
    // -------------------------------------------------------------------------

    @Test
    void readByUserIdAndLetterId_throwsNotFound_whenMissing() {
        when(reviewRepository.findByUserIdAndLetterId("u1", "L1"))
                .thenReturn(Optional.empty());

        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reviewService.readByUserIdAndLetterId("u1", "L1"));
        assertEquals(HttpStatus.NOT_FOUND, ex.getStatusCode());
        assertEquals("Review not found", ex.getReason());
    }

    @Test
    void readByUserIdAndLetterId_throwsBadRequest_whenUserIdBlank() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reviewService.readByUserIdAndLetterId("", "L1"));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verifyNoInteractions(reviewRepository);
    }

    @Test
    void readByUserIdAndLetterId_throwsBadRequest_whenLetterIdBlank() {
        ResponseStatusException ex = assertThrows(ResponseStatusException.class,
                () -> reviewService.readByUserIdAndLetterId("u1", ""));
        assertEquals(HttpStatus.BAD_REQUEST, ex.getStatusCode());
        verifyNoInteractions(reviewRepository);
    }
}
