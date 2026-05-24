package com.gongchae.gongchae_coming.favorite.controller;

import com.gongchae.gongchae_coming.favorite.dto.FavoriteRecruitmentCreateRequest;
import com.gongchae.gongchae_coming.favorite.dto.FavoriteRecruitmentResponse;
import com.gongchae.gongchae_coming.favorite.service.FavoriteRecruitmentService;
import com.gongchae.gongchae_coming.member.domain.Member;
import com.gongchae.gongchae_coming.member.exception.MemberNotFoundException;
import com.gongchae.gongchae_coming.member.repository.MemberRepository;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.Authentication;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/members")
public class FavoriteRecruitmentController {

	private final FavoriteRecruitmentService favoriteRecruitmentService;
	private final MemberRepository memberRepository;

	@PostMapping("/{memberId}/favorite-recruitments")
	public ResponseEntity<FavoriteRecruitmentResponse> create(
		@PathVariable Long memberId,
		@Valid @RequestBody FavoriteRecruitmentCreateRequest request
	) {
		FavoriteRecruitmentResponse response = favoriteRecruitmentService.create(memberId, request);
		HttpStatus status = response.created() ? HttpStatus.CREATED : HttpStatus.OK;
		return ResponseEntity.status(status).body(response);
	}

	@PostMapping("/me/favorite-recruitments")
	public ResponseEntity<FavoriteRecruitmentResponse> createForMe(
		Authentication authentication,
		@Valid @RequestBody FavoriteRecruitmentCreateRequest request
	) {
		return create(resolveCurrentMemberId(authentication), request);
	}

	@GetMapping("/{memberId}/favorite-recruitments")
	public List<FavoriteRecruitmentResponse> getFavorites(@PathVariable Long memberId) {
		return favoriteRecruitmentService.getFavorites(memberId);
	}

	@GetMapping("/me/favorite-recruitments")
	public List<FavoriteRecruitmentResponse> getMyFavorites(Authentication authentication) {
		return getFavorites(resolveCurrentMemberId(authentication));
	}

	@DeleteMapping("/{memberId}/favorite-recruitments/{sourceRecruitmentId}")
	public ResponseEntity<Void> delete(
		@PathVariable Long memberId,
		@PathVariable String sourceRecruitmentId,
		@RequestParam(defaultValue = "ALIO") String source
	) {
		favoriteRecruitmentService.delete(memberId, source, sourceRecruitmentId);
		return ResponseEntity.noContent().build();
	}

	@DeleteMapping("/me/favorite-recruitments/{sourceRecruitmentId}")
	public ResponseEntity<Void> deleteForMe(
		Authentication authentication,
		@PathVariable String sourceRecruitmentId,
		@RequestParam(defaultValue = "ALIO") String source
	) {
		return delete(resolveCurrentMemberId(authentication), sourceRecruitmentId, source);
	}

	private Long resolveCurrentMemberId(Authentication authentication) {
		Member member = memberRepository.findByEmail(authentication.getName())
			.orElseThrow(() -> new MemberNotFoundException("member not found"));
		return member.getId();
	}
}
