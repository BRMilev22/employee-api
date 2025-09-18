package com.example.employee_api.service;

import com.example.employee_api.exception.TokenRefreshException;
import com.example.employee_api.model.RefreshToken;
import com.example.employee_api.repository.RefreshTokenRepository;
import com.example.employee_api.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.Optional;
import java.util.UUID;

/**
 * Service for managing refresh tokens
 */
@Service
@Transactional
public class RefreshTokenService {

    @Value("${app.jwt.refresh-expiration:604800000}") // 7 days in milliseconds
    private Long refreshTokenDurationMs;

    @Autowired
    private RefreshTokenRepository refreshTokenRepository;

    @Autowired
    private UserRepository userRepository;

    /**
     * Find refresh token by token string
     */
    public Optional<RefreshToken> findByToken(String token) {
        return refreshTokenRepository.findByToken(token);
    }

    /**
     * Create new refresh token for user
     */
    public RefreshToken createRefreshToken(Long userId) {
        RefreshToken refreshToken = new RefreshToken();

        refreshToken.setUser(userRepository.findById(userId).orElseThrow(() -> 
            new RuntimeException("User not found with id: " + userId)));
        refreshToken.setExpiresAt(LocalDateTime.now().plusSeconds(refreshTokenDurationMs / 1000));
        refreshToken.setToken(UUID.randomUUID().toString());

        refreshToken = refreshTokenRepository.save(refreshToken);
        return refreshToken;
    }

    /**
     * Verify if refresh token is expired
     */
    public RefreshToken verifyExpiration(RefreshToken token) {
        if (token.isExpired()) {
            refreshTokenRepository.delete(token);
            throw new TokenRefreshException(token.getToken(), 
                "Refresh token was expired. Please make a new signin request");
        }

        return token;
    }

    /**
     * Delete refresh token by token string
     */
    @Transactional
    public void deleteByToken(String token) {
        refreshTokenRepository.findByToken(token).ifPresent(refreshToken -> {
            refreshToken.revoke();
            refreshTokenRepository.save(refreshToken);
        });
    }

    /**
     * Delete all refresh tokens for a user
     */
    @Transactional
    public void deleteByUserId(Long userId) {
        refreshTokenRepository.revokeAllTokensForUser(userId, LocalDateTime.now());
    }

    /**
     * Clean up expired tokens
     */
    @Transactional
    public int deleteExpiredTokens() {
        return refreshTokenRepository.deleteExpiredTokens(LocalDateTime.now());
    }

    /**
     * Clean up old revoked tokens
     */
    @Transactional
    public int deleteOldRevokedTokens(int daysOld) {
        LocalDateTime cutoffDate = LocalDateTime.now().minusDays(daysOld);
        return refreshTokenRepository.deleteOldRevokedTokens(cutoffDate);
    }

    /**
     * Count active tokens for user
     */
    public Long countActiveTokensForUser(Long userId) {
        return refreshTokenRepository.countActiveTokensForUser(userId, LocalDateTime.now());
    }

    /**
     * Check if user has too many active tokens
     */
    public boolean hasExceededTokenLimit(Long userId, int maxTokens) {
        return countActiveTokensForUser(userId) >= maxTokens;
    }

    /**
     * Revoke oldest tokens if limit exceeded
     */
    @Transactional
    public void enforceTokenLimit(Long userId, int maxTokens) {
        Long activeTokenCount = countActiveTokensForUser(userId);
        if (activeTokenCount >= maxTokens) {
            // Revoke oldest tokens
            refreshTokenRepository.findValidTokensByUserId(userId, LocalDateTime.now())
                .stream()
                .sorted((t1, t2) -> t1.getCreatedAt().compareTo(t2.getCreatedAt()))
                .limit(activeTokenCount - maxTokens + 1)
                .forEach(token -> {
                    token.revoke();
                    refreshTokenRepository.save(token);
                });
        }
    }

    /**
     * Get token statistics
     */
    public Object[] getTokenStatistics() {
        return refreshTokenRepository.getTokenStatistics(LocalDateTime.now());
    }
}