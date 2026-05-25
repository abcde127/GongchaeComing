const totalRecruitmentCount = document.querySelector("#totalRecruitmentCount");
const scheduledRecruitmentCount = document.querySelector("#scheduledRecruitmentCount");
const activeRecruitmentCount = document.querySelector("#activeRecruitmentCount");
const statisticsReferenceAt = document.querySelector("#statisticsReferenceAt");
const statisticsStatus = document.querySelector("#statisticsStatus");
const monthlyStats = document.querySelector("#monthlyStats");
const regionStats = document.querySelector("#regionStats");
const SUMMARY_ANIMATION_DURATION_MS = 900;

loadStatistics();

function loadStatistics() {
	loadSummaryStatistics();
	loadMonthlyStatistics();
	loadRegionStatistics();
}

async function loadSummaryStatistics() {
	try {
		const summary = await fetchJson("/api/recruitments/alio/statistics/summary");
		animateNumber(totalRecruitmentCount, summary.totalCount || 0);
		animateNumber(scheduledRecruitmentCount, summary.scheduledCount || 0);
		animateNumber(activeRecruitmentCount, summary.activeCount || 0);
		statisticsReferenceAt.textContent = summary.referenceAt
			? `${formatDateTime(summary.referenceAt)} 기준`
			: "기준 정보 없음";
	} catch (error) {
		renderSummaryError(totalRecruitmentCount);
		renderSummaryError(scheduledRecruitmentCount);
		renderSummaryError(activeRecruitmentCount);
		statisticsReferenceAt.textContent = "기준 정보 없음";
		showStatus(error.message || "요약 통계를 불러오지 못했습니다.", true);
	}
}

function animateNumber(element, targetValue) {
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
	element.classList.remove("is-loading");
	element.classList.add("is-error");
	element.textContent = "-";
}

async function loadMonthlyStatistics() {
	renderLoadingState(monthlyStats);
	try {
		const monthlyCounts = await fetchJson("/api/recruitments/alio/statistics/monthly-start-counts");
		renderBarList(monthlyStats, latestItems(monthlyCounts || [], 12), "yearMonth");
	} catch (error) {
		renderPanelError(monthlyStats, error.message || "월별 통계를 불러오지 못했습니다.");
	}
}

async function loadRegionStatistics() {
	renderLoadingState(regionStats);
	try {
		const regionCounts = await fetchJson("/api/recruitments/alio/statistics/region-counts");
		renderBarList(regionStats, (regionCounts || []).slice(0, 12), "label");
	} catch (error) {
		renderPanelError(regionStats, error.message || "지역별 통계를 불러오지 못했습니다.");
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

function renderBarList(container, items, labelKey) {
	container.replaceChildren();
	if (!items.length) {
		const empty = document.createElement("p");
		empty.className = "empty-note";
		empty.textContent = "표시할 통계가 없습니다.";
		container.appendChild(empty);
		return;
	}

	const maxCount = Math.max(...items.map((item) => item.count || 0), 1);
	items.forEach((item) => {
		const row = document.createElement("div");
		row.className = "bar-row";
		const percent = Math.max(4, Math.round(((item.count || 0) / maxCount) * 100));
		row.innerHTML = `
			<div class="bar-row-meta">
				<span>${escapeHtml(item[labelKey] || "-")}</span>
				<strong>${formatNumber(item.count || 0)}</strong>
			</div>
			<div class="bar-track" aria-hidden="true">
				<span style="width: ${percent}%"></span>
			</div>
		`;
		container.appendChild(row);
	});
}

function latestItems(items, limit) {
	return [...items]
		.sort((left, right) => String(right.yearMonth || "").localeCompare(String(left.yearMonth || "")))
		.slice(0, limit);
}

function renderLoadingState(container) {
	container.replaceChildren();
	const loading = document.createElement("p");
	loading.className = "empty-note";
	loading.textContent = "통계를 불러오고 있습니다.";
	container.appendChild(loading);
}

function renderPanelError(container, message) {
	container.replaceChildren();
	const error = document.createElement("p");
	error.className = "empty-note is-error";
	error.textContent = message;
	container.appendChild(error);
}

function showStatus(message, isError = false) {
	statisticsStatus.hidden = false;
	statisticsStatus.textContent = message;
	statisticsStatus.classList.toggle("is-error", isError);
}

function hideStatus() {
	statisticsStatus.hidden = true;
	statisticsStatus.textContent = "";
	statisticsStatus.classList.remove("is-error");
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

function escapeHtml(value) {
	return String(value)
		.replaceAll("&", "&amp;")
		.replaceAll("<", "&lt;")
		.replaceAll(">", "&gt;")
		.replaceAll('"', "&quot;")
		.replaceAll("'", "&#039;");
}
