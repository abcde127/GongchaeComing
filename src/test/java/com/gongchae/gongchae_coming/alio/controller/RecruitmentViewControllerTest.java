package com.gongchae.gongchae_coming.alio.controller;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.Test;
import org.springframework.ui.ConcurrentModel;

class RecruitmentViewControllerTest {

	private final RecruitmentViewController controller = new RecruitmentViewController();

	@Test
	void homeRendersLandingTemplate() {
		ConcurrentModel model = new ConcurrentModel();

		String viewName = controller.home(null, model);

		assertThat(viewName).isEqualTo("home");
		assertThat(model.getAttribute("isLoggedIn")).isEqualTo(false);
	}

	@Test
	void statisticsRendersStatisticsTemplate() {
		ConcurrentModel model = new ConcurrentModel();

		String viewName = controller.statistics(null, model);

		assertThat(viewName).isEqualTo("statistics");
		assertThat(model.getAttribute("isLoggedIn")).isEqualTo(false);
	}

	@Test
	void recruitmentRedirectPageRendersRedirectLandingTemplate() {
		ConcurrentModel model = new ConcurrentModel();

		String viewName = controller.recruitmentRedirectPage("PBANC-001", null, model);

		assertThat(viewName).isEqualTo("recruitment-redirect");
		assertThat(model.getAttribute("recruitmentId")).isEqualTo("PBANC-001");
	}
}
