package com.gongchae.gongchae_coming.member.repository;

import com.gongchae.gongchae_coming.member.domain.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MemberRepository extends JpaRepository<Member, Long> {

	boolean existsByEmail(String email);

	boolean existsByNickname(String nickname);
}
