package es.upm.api.adapter.in.resources;

import com.fasterxml.jackson.databind.ObjectMapper;
import es.upm.api.adapter.in.resources.dto.ReviewCreateDto;
import es.upm.api.adapter.in.resources.dto.ReviewUpdateDto;
import es.upm.api.domain.model.Review;
import es.upm.api.domain.services.ReviewService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.FilterType;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.server.ResponseStatusException;

import java.time.LocalDateTime;

import static org.mockito.ArgumentMatchers.*;
import static org.mockito.Mockito.*;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.jwt;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

/**
 * Resource-layer unit tests for ReviewResource.
 * Uses @WebMvcTest to slice only the web layer — no full Spring context, no MongoDB.
 * The two ApiExceptionHandler beans from the full app are excluded to avoid bean conflict
 * in the test slice; the one in es.upm.api.adapter.in.resources is sufficient.
 */
@WebMvcTest(
        controllers = ReviewResource.class,
        excludeFilters = @ComponentScan.Filter(
                type = FilterType.ASSIGNABLE_TYPE,
                classes = {
                        es.upm.api.adapter.in.resources.httperrors.ApiExceptionHandler.class
                }
        )
)
@Import(es.upm.api.configurations.ResourceServerConfig.class)
class ReviewResourceTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private ReviewService reviewService;

    // -------------------------------------------------------------------------
    // A — POST /reviews with CUSTOMER creates or updates current-user review
    // -------------------------------------------------------------------------

    @Test
    void postReview_withCustomer_returnsReviewAndCallsServiceWithJwtUserId() throws Exception {
        Review created = Review.builder()
                .id("review-id-1")
                .userId("600000000")
                .letterId("LTR-001")
                .stars(5)
                .opinion("Great service")
                .createdAt(LocalDateTime.of(2026, 1, 15, 10, 0))
                .updatedAt(LocalDateTime.of(2026, 1, 15, 10, 0))
                .build();

        when(reviewService.createOrUpdate(eq("600000000"), eq("LTR-001"), eq(5), eq("Great service")))
                .thenReturn(created);

        String body = """
                {"letterId": "LTR-001", "stars": 5, "opinion": "Great service"}
                """;

        mockMvc.perform(post("/reviews")
                        .with(jwt()
                                .jwt(builder -> builder
                                        .claim("username", "600000000")
                                        .subject("600000000"))
                                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("review-id-1"))
                .andExpect(jsonPath("$.userId").value("600000000"))
                .andExpect(jsonPath("$.letterId").value("LTR-001"))
                .andExpect(jsonPath("$.stars").value(5))
                .andExpect(jsonPath("$.opinion").value("Great service"))
                .andExpect(jsonPath("$.createdAt").exists())
                .andExpect(jsonPath("$.updatedAt").exists());

        verify(reviewService).createOrUpdate("600000000", "LTR-001", 5, "Great service");
    }

    // -------------------------------------------------------------------------
    // B — PUT /reviews/{letterId} with CUSTOMER updates current-user review
    // -------------------------------------------------------------------------

    @Test
    void putReview_withCustomer_callsServiceWithJwtUserIdAndPathLetterId() throws Exception {
        Review updated = Review.builder()
                .id("existing-id")
                .userId("600000000")
                .letterId("LTR-002")
                .stars(3)
                .opinion("Updated opinion")
                .createdAt(LocalDateTime.of(2026, 1, 1, 0, 0))
                .updatedAt(LocalDateTime.of(2026, 2, 1, 0, 0))
                .build();

        when(reviewService.createOrUpdate(eq("600000000"), eq("LTR-002"), eq(3), eq("Updated opinion")))
                .thenReturn(updated);

        String body = """
                {"stars": 3, "opinion": "Updated opinion"}
                """;

        mockMvc.perform(put("/reviews/LTR-002")
                        .with(jwt()
                                .jwt(builder -> builder
                                        .claim("username", "600000000")
                                        .subject("600000000"))
                                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("existing-id"))
                .andExpect(jsonPath("$.userId").value("600000000"))
                .andExpect(jsonPath("$.letterId").value("LTR-002"))
                .andExpect(jsonPath("$.stars").value(3))
                .andExpect(jsonPath("$.opinion").value("Updated opinion"));

        verify(reviewService).createOrUpdate("600000000", "LTR-002", 3, "Updated opinion");
    }

    // -------------------------------------------------------------------------
    // C — GET /reviews/{letterId} with CUSTOMER reads current-user review
    // -------------------------------------------------------------------------

    @Test
    void getReview_withCustomer_returnsReviewUsingJwtUserId() throws Exception {
        Review review = Review.builder()
                .id("r1")
                .userId("600000000")
                .letterId("LTR-003")
                .stars(4)
                .opinion("Good")
                .createdAt(LocalDateTime.of(2026, 1, 10, 0, 0))
                .updatedAt(LocalDateTime.of(2026, 1, 10, 0, 0))
                .build();

        when(reviewService.readByUserIdAndLetterId("600000000", "LTR-003"))
                .thenReturn(review);

        mockMvc.perform(get("/reviews/LTR-003")
                        .with(jwt()
                                .jwt(builder -> builder
                                        .claim("username", "600000000")
                                        .subject("600000000"))
                                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id").value("r1"))
                .andExpect(jsonPath("$.userId").value("600000000"))
                .andExpect(jsonPath("$.letterId").value("LTR-003"))
                .andExpect(jsonPath("$.stars").value(4))
                .andExpect(jsonPath("$.opinion").value("Good"));

        verify(reviewService).readByUserIdAndLetterId("600000000", "LTR-003");
    }

    // -------------------------------------------------------------------------
    // D — Unauthorized request rejected
    // -------------------------------------------------------------------------

    @Test
    void postReview_withoutJwt_returnsUnauthorized() throws Exception {
        String body = """
                {"letterId": "LTR-001", "stars": 5, "opinion": "Great service"}
                """;

        mockMvc.perform(post("/reviews")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());
    }

    @Test
    void getReview_withoutJwt_returnsUnauthorized() throws Exception {
        mockMvc.perform(get("/reviews/LTR-001"))
                .andExpect(status().isUnauthorized());
    }

    // -------------------------------------------------------------------------
    // E — Non-CUSTOMER role rejected (ADMIN and MANAGER)
    // -------------------------------------------------------------------------

    @Test
    void postReview_withAdminRole_returnsUnauthorized() throws Exception {
        String body = """
                {"letterId": "LTR-001", "stars": 5, "opinion": "Great service"}
                """;

        mockMvc.perform(post("/reviews")
                        .with(jwt()
                                .jwt(builder -> builder
                                        .claim("username", "admin1")
                                        .subject("admin1"))
                                .authorities(new SimpleGrantedAuthority("ROLE_ADMIN")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(reviewService);
    }

    @Test
    void postReview_withManagerRole_returnsUnauthorized() throws Exception {
        String body = """
                {"letterId": "LTR-001", "stars": 5, "opinion": "Great service"}
                """;

        mockMvc.perform(post("/reviews")
                        .with(jwt()
                                .jwt(builder -> builder
                                        .claim("username", "manager1")
                                        .subject("manager1"))
                                .authorities(new SimpleGrantedAuthority("ROLE_MANAGER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isUnauthorized());

        verifyNoInteractions(reviewService);
    }

    // -------------------------------------------------------------------------
    // F — POST validation rejects invalid payload
    // -------------------------------------------------------------------------

    @Test
    void postReview_withBlankLetterId_returnsBadRequest() throws Exception {
        String body = """
                {"letterId": "", "stars": 5, "opinion": "Great service"}
                """;

        mockMvc.perform(post("/reviews")
                        .with(jwt()
                                .jwt(builder -> builder
                                        .claim("username", "600000000")
                                        .subject("600000000"))
                                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reviewService);
    }

    @Test
    void postReview_withMissingLetterId_returnsBadRequest() throws Exception {
        String body = """
                {"stars": 5, "opinion": "Great service"}
                """;

        mockMvc.perform(post("/reviews")
                        .with(jwt()
                                .jwt(builder -> builder
                                        .claim("username", "600000000")
                                        .subject("600000000"))
                                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reviewService);
    }

    @Test
    void postReview_withStarsZero_returnsBadRequest() throws Exception {
        String body = """
                {"letterId": "LTR-001", "stars": 0, "opinion": "Bad request"}
                """;

        mockMvc.perform(post("/reviews")
                        .with(jwt()
                                .jwt(builder -> builder
                                        .claim("username", "600000000")
                                        .subject("600000000"))
                                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reviewService);
    }

    @Test
    void postReview_withStarsSix_returnsBadRequest() throws Exception {
        String body = """
                {"letterId": "LTR-001", "stars": 6, "opinion": "Bad request"}
                """;

        mockMvc.perform(post("/reviews")
                        .with(jwt()
                                .jwt(builder -> builder
                                        .claim("username", "600000000")
                                        .subject("600000000"))
                                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reviewService);
    }

    @Test
    void postReview_withBlankOpinion_returnsBadRequest() throws Exception {
        String body = """
                {"letterId": "LTR-001", "stars": 5, "opinion": "   "}
                """;

        mockMvc.perform(post("/reviews")
                        .with(jwt()
                                .jwt(builder -> builder
                                        .claim("username", "600000000")
                                        .subject("600000000"))
                                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reviewService);
    }

    // -------------------------------------------------------------------------
    // G — PUT validation rejects invalid payload
    // -------------------------------------------------------------------------

    @Test
    void putReview_withStarsZero_returnsBadRequest() throws Exception {
        String body = """
                {"stars": 0, "opinion": "Updated opinion"}
                """;

        mockMvc.perform(put("/reviews/LTR-001")
                        .with(jwt()
                                .jwt(builder -> builder
                                        .claim("username", "600000000")
                                        .subject("600000000"))
                                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reviewService);
    }

    @Test
    void putReview_withBlankOpinion_returnsBadRequest() throws Exception {
        String body = """
                {"stars": 5, "opinion": "   "}
                """;

        mockMvc.perform(put("/reviews/LTR-001")
                        .with(jwt()
                                .jwt(builder -> builder
                                        .claim("username", "600000000")
                                        .subject("600000000"))
                                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER")))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(body))
                .andExpect(status().isBadRequest());

        verifyNoInteractions(reviewService);
    }

    // -------------------------------------------------------------------------
    // H — GET missing review maps service NOT_FOUND to HTTP 404
    // -------------------------------------------------------------------------

    @Test
    void getReview_whenServiceThrowsNotFound_returns404() throws Exception {
        when(reviewService.readByUserIdAndLetterId("600000000", "LTR-NOTEXIST"))
                .thenThrow(new ResponseStatusException(
                        org.springframework.http.HttpStatus.NOT_FOUND, "Review not found"));

        mockMvc.perform(get("/reviews/LTR-NOTEXIST")
                        .with(jwt()
                                .jwt(builder -> builder
                                        .claim("username", "600000000")
                                        .subject("600000000"))
                                .authorities(new SimpleGrantedAuthority("ROLE_CUSTOMER"))))
                .andExpect(status().isNotFound());
    }
}
