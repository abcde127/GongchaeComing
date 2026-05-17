package com.gongchae.gongchae_coming.favorite.controller;

import com.gongchae.gongchae_coming.favorite.dto.FavoriteRecruitmentCreateRequest;
import com.gongchae.gongchae_coming.favorite.dto.FavoriteRecruitmentResponse;
import com.gongchae.gongchae_coming.favorite.service.FavoriteRecruitmentService;
import jakarta.validation.Valid;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
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
@RequestMapping("/api/members/{memberId}/favorite-recruitments")
public class FavoriteRecruitmentController {

	private final FavoriteRecruitmentService favoriteRecruitmentService;

	@PostMapping
	public ResponseEntity<FavoriteRecruitmentResponse> create(
		@PathVariable Long memberId,
		@Valid @RequestBody FavoriteRecruitmentCreateRequest request
	) {
		FavoriteRecruitmentResponse response = favoriteRecruitmentService.create(memberId, request);
		HttpStatus status = response.created() ? HttpStatus.CREATED : HttpStatus.OK;
		return ResponseEntity.status(status).body(response);
	}

	@GetMapping
	public List<FavoriteRecruitmentResponse> getFavorites(@PathVariable Long memberId) {
		return favoriteRecruitmentService.getFavorites(memberId);
	}

	@DeleteMapping("/{sourceRecruitmentId}")
	public ResponseEntity<Void> delete(
		@PathVariable Long memberId,
		@PathVariable String sourceRecruitmentId,
		@RequestParam(defaultValue = "ALIO") String source
	) {
		favoriteRecruitmentService.delete(memberId, source, sourceRecruitmentId);
		return ResponseEntity.noContent().build();
	}
}
