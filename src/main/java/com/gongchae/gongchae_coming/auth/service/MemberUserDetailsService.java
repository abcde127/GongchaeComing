package com.gongchae.gongchae_coming.auth.service;

import com.gongchae.gongchae_coming.member.domain.Member;
import com.gongchae.gongchae_coming.member.repository.MemberRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

@Service
@RequiredArgsConstructor
public class MemberUserDetailsService implements UserDetailsService {

	private final MemberRepository memberRepository;

	@Override
	public UserDetails loadUserByUsername(String username) {
		Member member = memberRepository.findByEmail(username)
			.orElseThrow(() -> new UsernameNotFoundException("member not found"));

		return User.withUsername(member.getEmail())
			.password(member.getPassword())
			.roles("USER")
			.build();
	}
}
