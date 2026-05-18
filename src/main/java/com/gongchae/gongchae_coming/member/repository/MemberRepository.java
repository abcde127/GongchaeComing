package com.gongchae.gongchae_coming.member.repository;

import com.gongchae.gongchae_coming.member.domain.Member;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

	boolean existsByEmail(String email);

	boolean existsByNickname(String nickname);

	boolean existsByNicknameAndIdNot(String nickname, Long id);

	Optional<Member> findByEmail(String email);

	Optional<Member> findByEmailAndNickname(String email, String nickname);
}
