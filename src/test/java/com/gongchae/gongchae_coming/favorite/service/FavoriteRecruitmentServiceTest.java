package com.gongchae.gongchae_coming.favorite.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.gongchae.gongchae_coming.favorite.dto.FavoriteRecruitmentCreateRequest;
import com.gongchae.gongchae_coming.favorite.dto.FavoriteRecruitmentResponse;
import com.gongchae.gongchae_coming.favorite.repository.FavoriteRecruitmentRepository;
import com.gongchae.gongchae_coming.member.dto.MemberSignupRequest;
import com.gongchae.gongchae_coming.member.dto.MemberSignupResponse;
import com.gongchae.gongchae_coming.member.exception.MemberNotFoundException;
import com.gongchae.gongchae_coming.member.service.MemberService;
import java.util.List;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@Transactional
class FavoriteRecruitmentServiceTest {

	@Autowired
	private FavoriteRecruitmentService favoriteRecruitmentService;

	@Autowired
	private FavoriteRecruitmentRepository favoriteRecruitmentRepository;

	@Autowired
	private MemberService memberService;

	@Test
	void createSavesFavoriteRecruitment() {
		MemberSignupResponse member = createMember();

		FavoriteRecruitmentResponse response = favoriteRecruitmentService.create(
			member.id(),
			new FavoriteRecruitmentCreateRequest(
				null,
				"PBANC-001",
				"2026년 상반기 일반직 채용",
				"한국공사",
				"정규직",
				"서울",
				"2026-04-01",
				"2026-04-30",
				"https://example.com/recruitments/1"
			)
		);

		assertThat(response.created()).isTrue();
		assertThat(response.memberId()).isEqualTo(member.id());
		assertThat(response.source()).isEqualTo("ALIO");
		assertThat(response.sourceRecruitmentId()).isEqualTo("PBANC-001");
		assertThat(favoriteRecruitmentRepository.findByMemberIdOrderByCreatedAtDescIdDesc(member.id())).hasSize(1);
	}

	@Test
	void createReturnsExistingFavoriteWhenRecruitmentAlreadySaved() {
		MemberSignupResponse member = createMember();
		FavoriteRecruitmentCreateRequest request = new FavoriteRecruitmentCreateRequest(
			"ALIO",
			"PBANC-001",
			"2026년 상반기 일반직 채용",
			"한국공사",
			"정규직",
			"서울",
			"2026-04-01",
			"2026-04-30",
			"https://example.com/recruitments/1"
		);

		FavoriteRecruitmentResponse firstResponse = favoriteRecruitmentService.create(member.id(), request);
		FavoriteRecruitmentResponse secondResponse = favoriteRecruitmentService.create(member.id(), request);

		assertThat(firstResponse.id()).isEqualTo(secondResponse.id());
		assertThat(secondResponse.created()).isFalse();
		assertThat(favoriteRecruitmentRepository.findByMemberIdOrderByCreatedAtDescIdDesc(member.id())).hasSize(1);
	}

	@Test
	void getFavoritesReturnsSavedFavoritesInLatestFirstOrder() {
		MemberSignupResponse member = createMember();

		favoriteRecruitmentService.create(member.id(), new FavoriteRecruitmentCreateRequest(
			"ALIO",
			"PBANC-001",
			"첫 번째 공고",
			"기관A",
			"정규직",
			"서울",
			"2026-04-01",
			"2026-04-30",
			null
		));
		favoriteRecruitmentService.create(member.id(), new FavoriteRecruitmentCreateRequest(
			"ALIO",
			"PBANC-002",
			"두 번째 공고",
			"기관B",
			"계약직",
			"부산",
			"2026-04-02",
			"2026-05-01",
			null
		));

		List<FavoriteRecruitmentResponse> favorites = favoriteRecruitmentService.getFavorites(member.id());

		assertThat(favorites).hasSize(2);
		assertThat(favorites.get(0).sourceRecruitmentId()).isEqualTo("PBANC-002");
		assertThat(favorites.get(1).sourceRecruitmentId()).isEqualTo("PBANC-001");
	}

	@Test
	void deleteRemovesFavoriteRecruitment() {
		MemberSignupResponse member = createMember();

		favoriteRecruitmentService.create(member.id(), new FavoriteRecruitmentCreateRequest(
			"ALIO",
			"PBANC-001",
			"삭제될 공고",
			"기관A",
			"정규직",
			"서울",
			null,
			null,
			null
		));

		favoriteRecruitmentService.delete(member.id(), "ALIO", "PBANC-001");

		assertThat(favoriteRecruitmentService.getFavorites(member.id())).isEmpty();
	}

	@Test
	void createRejectsUnknownMember() {
		assertThatThrownBy(() -> favoriteRecruitmentService.create(
			999L,
			new FavoriteRecruitmentCreateRequest(
				"ALIO",
				"PBANC-001",
				"공고",
				"기관A",
				null,
				null,
				null,
				null,
				null
			)
		)).isInstanceOf(MemberNotFoundException.class)
			.hasMessage("member not found");
	}

	private MemberSignupResponse createMember() {
		return memberService.signup(new MemberSignupRequest(
			"user@example.com",
			"gongchae",
			"password1"
		));
	}
}
