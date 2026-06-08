package com.ensah.nlp_annotation_platform.domain;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.Instant;
import java.util.UUID;

/**
 * Persistent refresh-token for the stateless JWT auth flow.
 *
 * <p>Flow summary:
 * <ol>
 *   <li>Login → server issues short-lived access JWT + stores one
 *       {@code RefreshToken} row.</li>
 *   <li>{@code POST /api/auth/refresh} → client sends token string → server
 *       validates {@code expiry}, issues new access JWT, rotates (or reuses)
 *       the refresh token.</li>
 *   <li>Logout → delete the row.</li>
 * </ol>
 * </p>
 *
 * <p>The token value is a random UUID, making it unguessable and safe to
 * transmit as an HTTP-only cookie or inside a JSON body.</p>
 */
@Entity
@Table(
        name = "refresh_tokens",
        indexes = {
                @Index(name = "idx_refresh_tokens_user_id", columnList = "user_id")
        }
)
@Data
@NoArgsConstructor
@AllArgsConstructor
public class RefreshToken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    /**
     * Opaque token string sent to the client.
     * Generated as a UUID by the service layer; indexed for O(1) lookup.
     */
    @Column(nullable = false, unique = true, length = 36)
    private String token = UUID.randomUUID().toString();

    @ManyToOne(fetch = FetchType.LAZY, optional = false)
    @JoinColumn(name = "user_id", nullable = false)
    private User user;

    @Column(nullable = false)
    private Instant expiry;
}