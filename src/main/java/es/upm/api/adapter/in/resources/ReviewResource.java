package es.upm.api.adapter.in.resources;

import es.upm.api.adapter.in.resources.dto.ReviewCreateDto;
import es.upm.api.adapter.in.resources.dto.ReviewUpdateDto;
import es.upm.api.domain.model.Review;
import es.upm.api.domain.services.ReviewService;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.security.oauth2.jwt.Jwt;
import org.springframework.web.bind.annotation.*;

/**
 * ReviewResource — customer-scoped review management.
 *
 * All endpoints are scoped to the authenticated user via JWT.
 * userId is ALWAYS extracted from the JWT username claim, never from the request body.
 *
 * Ownership validation: this implementation does NOT verify that the given letterId
 * belongs to the authenticated user. goa-sandbox currently has no EngagementLetter
 * entity and no goa-engagement Feign client to perform such validation.
 * This limitation is documented and deferred to a later integration step (M7).
 */
@CrossOrigin(origins = "http://localhost:4200")
@RestController
@RequestMapping("/reviews")
public class ReviewResource {

    private final ReviewService reviewService;

    @Autowired
    public ReviewResource(ReviewService reviewService) {
        this.reviewService = reviewService;
    }

    /**
     * Creates a new review or updates the existing one for the current user + letterId.
     * Semantically equivalent to upsert.
     */
    @PostMapping
    @PreAuthorize("hasRole('CUSTOMER')")
    public Review createOrUpdate(@Valid @RequestBody ReviewCreateDto dto,
                                 @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaimAsString("username");
        return this.reviewService.createOrUpdate(userId, dto.getLetterId(),
                dto.getStars(), dto.getOpinion());
    }

    /**
     * Updates the existing review for the current user + letterId.
     * Still performs upsert if no review exists yet.
     */
    @PutMapping("/{letterId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public Review update(@PathVariable String letterId,
                         @Valid @RequestBody ReviewUpdateDto dto,
                         @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaimAsString("username");
        return this.reviewService.createOrUpdate(userId, letterId,
                dto.getStars(), dto.getOpinion());
    }

    /**
     * Reads the current user's review for the given letterId.
     * Returns 404 if no review exists for this user + letterId.
     */
    @GetMapping("/{letterId}")
    @PreAuthorize("hasRole('CUSTOMER')")
    public ResponseEntity<Review> read(@PathVariable String letterId,
                                        @AuthenticationPrincipal Jwt jwt) {
        String userId = jwt.getClaimAsString("username");
        return this.reviewService.readByUserIdAndLetterId(userId, letterId)
                .map(ResponseEntity::ok)
                .orElse(ResponseEntity.notFound().build());
    }
}
