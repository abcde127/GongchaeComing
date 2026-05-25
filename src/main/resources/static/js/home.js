const totalRecruitmentCount = document.querySelector("#totalRecruitmentCount");
const scheduledRecruitmentCount = document.querySelector("#scheduledRecruitmentCount");
const activeRecruitmentCount = document.querySelector("#activeRecruitmentCount");
const statisticsReferenceAt = document.querySelector("#statisticsReferenceAt");
const statisticsStatus = document.querySelector("#statisticsStatus");
const statisticsSection = document.querySelector("#statistics");
const regionStats = document.querySelector("#regionStats");
const regionDetailTitle = document.querySelector("#regionDetailTitle");
const selectedRegionCount = document.querySelector("#selectedRegionCount");
const regionDetailStats = document.querySelector("#regionDetailStats");
const statTabs = document.querySelectorAll("[data-stat-tab]");
const SUMMARY_ANIMATION_DURATION_MS = 900;
let detailStatisticsLoaded = false;
let selectedRegion = null;
let activeDetailTab = "period";
let summaryTotalCount = null;
const regionDetailCache = new Map();

loadStatistics();

function loadStatistics() {
	loadSummaryStatistics();
	prepareDetailStatisticsLoading();
}

async function loadSummaryStatistics() {
	try {
		const summary = await fetchJson("/api/recruitments/alio/statistics/summary");
		summaryTotalCount = summary.totalCount || 0;
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
	renderLoadingState(regionDetailStats);
	try {
		const monthlyCounts = await fetchRegionStatistic("period");
		renderBarList(regionDetailStats, latestItems(monthlyCounts || [], 12), "yearMonth");
	} catch (error) {
		renderPanelError(regionDetailStats, error.message || "월별 통계를 불러오지 못했습니다.");
	}
}

async function loadRegionStatistics() {
	renderLoadingState(regionStats);
	try {
		const regionCounts = await fetchJson("/api/recruitments/alio/statistics/region-counts");
		renderRegionButtons([
			{ code: "ALL", label: "전국", count: summaryTotalCount },
			...(regionCounts || []).slice(0, 18)
		]);
	} catch (error) {
		renderPanelError(regionStats, error.message || "지역별 통계를 불러오지 못했습니다.");
	}
}

function prepareDetailStatisticsLoading() {
	renderLoadingState(regionStats);
	renderLoadingState(regionDetailStats);
	if (!statisticsSection || !("IntersectionObserver" in window)) {
		loadDetailStatistics();
		return;
	}

	const observer = new IntersectionObserver((entries) => {
		if (!entries.some((entry) => entry.isIntersecting)) {
			return;
		}
		observer.disconnect();
		loadDetailStatistics();
	}, {
		rootMargin: "240px 0px"
	});
	observer.observe(statisticsSection);
}

async function loadDetailStatistics() {
	if (detailStatisticsLoaded) {
		return;
	}
	detailStatisticsLoaded = true;
	await loadRegionStatistics();
}

function renderRegionButtons(regions) {
	regionStats.replaceChildren();
	if (!regions.length) {
		renderPanelError(regionStats, "표시할 지역 통계가 없습니다.");
		return;
	}
	regions.forEach((region) => {
		const button = document.createElement("button");
		button.type = "button";
		button.className = "region-stat-button";
		button.dataset.regionCode = region.code;
		button.innerHTML = `
			<span>${escapeHtml(region.label || "-")}</span>
			<strong>${formatOptionalNumber(region.count)}</strong>
		`;
		button.addEventListener("click", () => selectRegion(region));
		regionStats.appendChild(button);
	});
	selectRegion(regions[0]);
}

function selectRegion(region) {
	selectedRegion = region;
	regionDetailTitle.textContent = `${region.label} 통계`;
	selectedRegionCount.textContent = `${formatOptionalNumber(region.count)}개`;
	document.querySelectorAll(".region-stat-button").forEach((button) => {
		button.classList.toggle("is-active", button.dataset.regionCode === region.code);
	});
	loadSelectedRegionStatistic();
}

async function loadSelectedRegionStatistic() {
	if (!selectedRegion) {
		return;
	}
	renderLoadingState(regionDetailStats);
	try {
		const items = await fetchRegionStatistic(activeDetailTab);
		renderRegionDetailStatistic(items || []);
	} catch (error) {
		renderPanelError(regionDetailStats, error.message || "지역 통계를 불러오지 못했습니다.");
	}
}

async function fetchRegionStatistic(tab) {
	const cacheKey = `${selectedRegion.code}:${tab}`;
	if (regionDetailCache.has(cacheKey)) {
		return regionDetailCache.get(cacheKey);
	}
	const url = new URL(statisticEndpoint(tab), window.location.origin);
	if (selectedRegion.code !== "ALL") {
		url.searchParams.set("regionCode", selectedRegion.code);
	}
	const items = await fetchJson(url.pathname + url.search);
	regionDetailCache.set(cacheKey, items);
	return items;
}

function statisticEndpoint(tab) {
	if (tab === "period") {
		return "/api/recruitments/alio/statistics/monthly-start-counts";
	}
	if (tab === "ncs") {
		return "/api/recruitments/alio/statistics/ncs-counts";
	}
	if (tab === "company") {
		return "/api/recruitments/alio/statistics/company-counts";
	}
	return "/api/recruitments/alio/statistics/monthly-start-counts";
}

function renderRegionDetailStatistic(items) {
	if (activeDetailTab === "period") {
		renderPeriodStatistic(items);
		return;
	}
	renderBarList(regionDetailStats, items.slice(0, 12), "label");
}

function renderPeriodStatistic(items) {
	regionDetailStats.replaceChildren();
	regionDetailStats.appendChild(createStatisticGroup("년도별 공고 수", yearlyCountsFromMonthly(items), "year"));
	regionDetailStats.appendChild(createStatisticGroup("최근 12개월 공고 수", latestItems(items, 12), "yearMonth"));
}

function createStatisticGroup(title, items, labelKey) {
	const group = document.createElement("section");
	group.className = "statistic-group";
	const heading = document.createElement("h4");
	heading.textContent = title;
	const list = document.createElement("div");
	list.className = "bar-list statistic-group-list";
	group.append(heading, list);
	renderBarList(list, items, labelKey);
	return group;
}

function yearlyCountsFromMonthly(items) {
	const yearlyCounts = new Map();
	items.forEach((item) => {
		const year = String(item.yearMonth || "").slice(0, 4);
		if (!year) {
			return;
		}
		yearlyCounts.set(year, (yearlyCounts.get(year) || 0) + (item.count || 0));
	});
	return [...yearlyCounts.entries()]
		.sort(([left], [right]) => left.localeCompare(right))
		.map(([year, count]) => ({ year, count }));
}

statTabs.forEach((tab) => {
	tab.addEventListener("click", () => {
		activeDetailTab = tab.dataset.statTab;
		statTabs.forEach((item) => item.classList.toggle("is-active", item === tab));
		loadSelectedRegionStatistic();
	});
});

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

function formatOptionalNumber(value) {
	return Number.isFinite(Number(value)) ? formatNumber(Number(value)) : "-";
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
