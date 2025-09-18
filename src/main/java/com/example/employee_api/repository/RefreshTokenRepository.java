package com.example.employee_api.repository;

import com.example.employee_api.model.RefreshToken;
import com.example.employee_api.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

/**
 * Repository interface for RefreshToken entity operations
 */
@Repository
public interface RefreshTokenRepository extends JpaRepository<RefreshToken, Long> {

    /**
     * Find refresh token by token string
     */
    Optional<RefreshToken> findByToken(String token);

    /**
     * Find refresh tokens by user
     */
    List<RefreshToken> findByUser(User user);

    /**
     * Find refresh tokens by user ID
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId")
    List<RefreshToken> findByUserId(@Param("userId") Long userId);

    /**
     * Find valid (non-expired, non-revoked) refresh tokens by user
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.expiresAt > :now AND rt.revokedAt IS NULL")
    List<RefreshToken> findValidTokensByUserId(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /**
     * Find expired refresh tokens
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.expiresAt <= :now")
    List<RefreshToken> findExpiredTokens(@Param("now") LocalDateTime now);

    /**
     * Find revoked refresh tokens
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.revokedAt IS NOT NULL")
    List<RefreshToken> findRevokedTokens();

    /**
     * Check if token exists and is valid
     */
    @Query("SELECT CASE WHEN COUNT(rt) > 0 THEN true ELSE false END FROM RefreshToken rt " +
           "WHERE rt.token = :token AND rt.expiresAt > :now AND rt.revokedAt IS NULL")
    boolean existsValidToken(@Param("token") String token, @Param("now") LocalDateTime now);

    /**
     * Revoke refresh token
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revokedAt = :revokedAt WHERE rt.token = :token")
    void revokeToken(@Param("token") String token, @Param("revokedAt") LocalDateTime revokedAt);

    /**
     * Revoke all refresh tokens for a user
     */
    @Modifying
    @Query("UPDATE RefreshToken rt SET rt.revokedAt = :revokedAt WHERE rt.user.id = :userId AND rt.revokedAt IS NULL")
    void revokeAllTokensForUser(@Param("userId") Long userId, @Param("revokedAt") LocalDateTime revokedAt);

    /**
     * Delete expired refresh tokens
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.expiresAt <= :cutoffDate")
    int deleteExpiredTokens(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Delete revoked refresh tokens older than specified date
     */
    @Modifying
    @Query("DELETE FROM RefreshToken rt WHERE rt.revokedAt IS NOT NULL AND rt.revokedAt <= :cutoffDate")
    int deleteOldRevokedTokens(@Param("cutoffDate") LocalDateTime cutoffDate);

    /**
     * Count active refresh tokens for a user
     */
    @Query("SELECT COUNT(rt) FROM RefreshToken rt WHERE rt.user.id = :userId AND rt.expiresAt > :now AND rt.revokedAt IS NULL")
    Long countActiveTokensForUser(@Param("userId") Long userId, @Param("now") LocalDateTime now);

    /**
     * Find refresh tokens by IP address
     */
    List<RefreshToken> findByIpAddress(String ipAddress);

    /**
     * Find refresh tokens by device info
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.deviceInfo LIKE %:deviceInfo%")
    List<RefreshToken> findByDeviceInfo(@Param("deviceInfo") String deviceInfo);

    /**
     * Find refresh tokens created within a date range
     */
    @Query("SELECT rt FROM RefreshToken rt WHERE rt.createdAt BETWEEN :startDate AND :endDate")
    List<RefreshToken> findTokensCreatedBetween(@Param("startDate") LocalDateTime startDate, 
                                               @Param("endDate") LocalDateTime endDate);

    /**
     * Get refresh token statistics
     */
    @Query("SELECT " +
           "COUNT(rt) as totalTokens, " +
           "SUM(CASE WHEN rt.expiresAt > :now AND rt.revokedAt IS NULL THEN 1 ELSE 0 END) as activeTokens, " +
           "SUM(CASE WHEN rt.expiresAt <= :now THEN 1 ELSE 0 END) as expiredTokens, " +
           "SUM(CASE WHEN rt.revokedAt IS NOT NULL THEN 1 ELSE 0 END) as revokedTokens " +
           "FROM RefreshToken rt")
    Object[] getTokenStatistics(@Param("now") LocalDateTime now);
}