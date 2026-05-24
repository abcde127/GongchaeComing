package com.gongchae.gongchae_coming.member.repository;

import com.gongchae.gongchae_coming.member.domain.Member;
import java.time.LocalTime;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface MemberRepository extends JpaRepository<Member, Long> {

	boolean existsByEmail(String email);

	boolean existsByNickname(String nickname);

	boolean existsByNicknameAndIdNot(String nickname, Long id);

	Optional<Member> findByEmail(String email);

	Optional<Member> findByEmailAndNickname(String email, String nickname);

	@Query("""
		select member from Member member
		where member.favoriteReminderEnabled = true
			and member.favoriteReminderTime = :reminderTime
			and member.kakaoAccessToken is not null
			and member.kakaoRefreshToken is not null
		""")
	List<Member> findFavoriteReminderTargets(@Param("reminderTime") LocalTime reminderTime);
}
