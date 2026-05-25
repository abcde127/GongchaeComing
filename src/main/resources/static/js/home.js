const totalRecruitmentCount = document.querySelector("#totalRecruitmentCount");
const scheduledRecruitmentCount = document.querySelector("#scheduledRecruitmentCount");
const activeRecruitmentCount = document.querySelector("#activeRecruitmentCount");
const statisticsReferenceAt = document.querySelector("#statisticsReferenceAt");
const revealElements = document.querySelectorAll(".reveal-on-scroll");
const SUMMARY_ANIMATION_DURATION_MS = 900;

prepareScrollReveal();
loadSummaryStatistics();

async function loadSummaryStatistics() {
	if (!totalRecruitmentCount || !scheduledRecruitmentCount || !activeRecruitmentCount) {
		return;
	}
	try {
		const summary = await fetchJson("/api/recruitments/alio/statistics/summary");
		animateNumber(totalRecruitmentCount, summary.totalCount || 0);
		animateNumber(scheduledRecruitmentCount, summary.scheduledCount || 0);
		animateNumber(activeRecruitmentCount, summary.activeCount || 0);
		if (statisticsReferenceAt) {
			statisticsReferenceAt.textContent = summary.referenceAt
				? `${formatDateTime(summary.referenceAt)} 기준`
				: "기준 정보 없음";
		}
	} catch (error) {
		renderSummaryError(totalRecruitmentCount);
		renderSummaryError(scheduledRecruitmentCount);
		renderSummaryError(activeRecruitmentCount);
		if (statisticsReferenceAt) {
			statisticsReferenceAt.textContent = "기준 정보 없음";
		}
	}
}

async function fetchJson(url) {
	const response = await fetch(url, {
		headers: {
			Accept: "application/json"
		}
	});
	if (!response.ok) {
		throw new Error(`통계 조회에 실패했습니다. (${response.status})`);
	}
	return response.json();
}

function animateNumber(element, targetValue) {
	if (!element) {
		return;
	}
	element.classList.remove("is-loading", "is-error");
	const target = Number.isFinite(Number(targetValue)) ? Number(targetValue) : 0;
	const startTime = performance.now();

	function update(now) {
		const progress = Math.min((now - startTime) / SUMMARY_ANIMATION_DURATION_MS, 1);
		const easedProgress = 1 - Math.pow(1 - progress, 3);
		element.textContent = formatNumber(Math.round(target * easedProgress));
		if (progress < 1) {
			requestAnimationFrame(update);
			return;
		}
		element.textContent = formatNumber(target);
	}

	requestAnimationFrame(update);
}

function renderSummaryError(element) {
	if (!element) {
		return;
	}
	element.classList.remove("is-loading");
	element.classList.add("is-error");
	element.textContent = "-";
}

function prepareScrollReveal() {
	if (!revealElements.length) {
		return;
	}
	if (!("IntersectionObserver" in window)) {
		revealElements.forEach((element) => element.classList.add("is-visible"));
		return;
	}

	const observer = new IntersectionObserver((entries) => {
		entries.forEach((entry) => {
			if (!entry.isIntersecting) {
				return;
			}
			entry.target.classList.add("is-visible");
			observer.unobserve(entry.target);
		});
	}, {
		threshold: 0.28
	});

	revealElements.forEach((element, index) => {
		element.style.transitionDelay = `${index * 90}ms`;
		observer.observe(element);
	});
}

function formatNumber(value) {
	return new Intl.NumberFormat("ko-KR").format(value);
}

function formatDateTime(value) {
	const date = new Date(value);
	if (Number.isNaN(date.getTime())) {
		return value;
	}
	return new Intl.DateTimeFormat("ko-KR", {
		year: "numeric",
		month: "2-digit",
		day: "2-digit",
		hour: "2-digit",
		minute: "2-digit"
	}).format(date);
}
