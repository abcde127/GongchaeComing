package com.gongchae.gongchae_coming.member.repository;

import com.gongchae.gongchae_coming.member.domain.PasswordResetVerification;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface PasswordResetVerificationRepository extends JpaRepository<PasswordResetVerification, Long> {

	Optional<PasswordResetVerification> findTopByEmailOrderByCreatedAtDesc(String email);

	@Query("""
		select verification from PasswordResetVerification verification
		where verification.email = :email
			and verification.usedAt is null
		order by verification.createdAt desc
	""")
	List<PasswordResetVerification> findActiveByEmail(@Param("email") String email);

	@Modifying(clearAutomatically = true, flushAutomatically = true)
	@Query("""
		delete from PasswordResetVerification verification
		where verification.expiresAt < :threshold
			or verification.usedAt < :threshold
	""")
	int deleteExpiredOrUsedBefore(@Param("threshold") LocalDateTime threshold);
}
