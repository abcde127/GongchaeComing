const totalRecruitmentCount = document.querySelector("#totalRecruitmentCount");
const scheduledRecruitmentCount = document.querySelector("#scheduledRecruitmentCount");
const activeRecruitmentCount = document.querySelector("#activeRecruitmentCount");
const statisticsReferenceAt = document.querySelector("#statisticsReferenceAt");
const statisticsStatus = document.querySelector("#statisticsStatus");
const statisticsSection = document.querySelector("#statistics");
const regionStats = document.querySelector("#regionStats");
const regionDetailStats = document.querySelector("#regionDetailStats");
const statTabs = document.querySelectorAll("[data-stat-tab]");
const revealElements = document.querySelectorAll(".reveal-on-scroll");
const SUMMARY_ANIMATION_DURATION_MS = 900;
const RECRUITMENT_CATEGORY_DISPLAY_OPTIONS = [
	{ code: "R2010", label: "신입", sourceCodes: ["R2010", "R2030"] },
	{ code: "R2020", label: "경력", sourceCodes: ["R2020", "R2030"] },
	{ code: "R2040", label: "외국인", sourceCodes: ["R2040"] }
];
const HIRE_TYPE_DISPLAY_OPTIONS = [
	{ code: "R1010", label: "정규직" },
	{ code: "R1020", label: "계약직" },
	{ code: "R1030", label: "무기계약직" },
	{ code: "R1040", label: "비정규직" },
	{ code: "R1050", label: "청년인턴" },
	{ code: "R1060", label: "청년인턴(체험형)" },
	{ code: "R1070", label: "청년인턴(채용형)" }
];
const WORK_REGION_OPTIONS = [
	{ code: "ALL", label: "전국" },
	{ code: "R3010", label: "서울" },
	{ code: "R3011", label: "인천" },
	{ code: "R3012", label: "대전" },
	{ code: "R3013", label: "대구" },
	{ code: "R3014", label: "부산" },
	{ code: "R3015", label: "광주" },
	{ code: "R3016", label: "울산" },
	{ code: "R3017", label: "경기" },
	{ code: "R3018", label: "강원" },
	{ code: "R3019", label: "충청남도" },
	{ code: "R3020", label: "충청북도" },
	{ code: "R3021", label: "경상북도" },
	{ code: "R3022", label: "경상남도" },
	{ code: "R3023", label: "전라남도" },
	{ code: "R3024", label: "전라북도" },
	{ code: "R3025", label: "제주" },
	{ code: "R3026", label: "세종" },
	{ code: "R3030", label: "해외" }
];
let detailStatisticsLoaded = false;
let selectedRegion = null;
let activeDetailTab = "period";
let selectedPeriodYear = String(new Date().getFullYear());
let summaryTotalCount = null;
const regionDetailCache = new Map();

loadStatistics();

async function loadStatistics() {
	prepareScrollReveal();
	await loadSummaryStatistics();
	prepareDetailStatisticsLoading();
}

async function loadSummaryStatistics() {
	try {
		const summary = await fetchJson("/api/recruitments/alio/statistics/summary");
		summaryTotalCount = summary.totalCount || 0;
		animateNumber(totalRecruitmentCount, summary.totalCount || 0);
		animateNumber(scheduledRecruitmentCount, summary.scheduledCount || 0);
		animateNumber(activeRecruitmentCount, summary.activeCount || 0);
		animateRegionCount("ALL", summaryTotalCount);
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
		showStatus(error.message || "요약 통계를 불러오지 못했습니다.", true);
	}
}

function animateNumber(element, targetValue) {
	if (!element) {
		return;
	}
	element.classList.remove("is-loading", "is-error", "region-count-skeleton", "skeleton-surface");
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
	renderStatisticLoadingState(regionDetailStats, "period");
	try {
		const monthlyCounts = await fetchRegionStatistic("period");
		renderBarList(regionDetailStats, latestItems(monthlyCounts || [], 12), "yearMonth");
	} catch (error) {
		renderPanelError(regionDetailStats, error.message || "월별 통계를 불러오지 못했습니다.");
	}
}

async function loadRegionStatistics() {
	renderRegionLoadingState(regionStats);
	try {
		const regionCounts = await fetchJson("/api/recruitments/alio/statistics/region-counts");
		renderRegionButtons(mergeRegionCounts(regionCounts || []), true);
	} catch (error) {
		renderPanelError(regionStats, error.message || "지역별 통계를 불러오지 못했습니다.");
	}
}

function prepareDetailStatisticsLoading() {
	renderRegionLoadingState(regionStats);
	renderStatisticLoadingState(regionDetailStats, activeDetailTab);
	loadDetailStatistics();
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

async function loadDetailStatistics() {
	if (detailStatisticsLoaded) {
		return;
	}
	detailStatisticsLoaded = true;
	await loadRegionStatistics();
}

function renderRegionButtons(regions, animateCounts = false) {
	regionStats.replaceChildren();
	regionStats.removeAttribute("aria-busy");
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
			<strong data-region-count="${escapeHtml(region.code)}">${formatOptionalNumber(region.count)}</strong>
		`;
		button.addEventListener("click", () => selectRegion(region));
		regionStats.appendChild(button);
		if (animateCounts) {
			animateNumber(button.querySelector("[data-region-count]"), region.count || 0);
		}
	});
	selectRegion(regions.find((region) => region.code === selectedRegion?.code) || regions[0]);
}

function selectRegion(region) {
	selectedRegion = region;
	document.querySelectorAll(".region-stat-button").forEach((button) => {
		button.classList.toggle("is-active", button.dataset.regionCode === region.code);
	});
	loadSelectedRegionStatistic();
}

async function loadSelectedRegionStatistic() {
	if (!selectedRegion) {
		return;
	}
	renderStatisticLoadingState(regionDetailStats, activeDetailTab);
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
	if (tab === "recruitmentCategory") {
		return "/api/recruitments/alio/statistics/recruitment-category-counts";
	}
	if (tab === "hireType") {
		return "/api/recruitments/alio/statistics/hire-type-counts";
	}
	return "/api/recruitments/alio/statistics/monthly-start-counts";
}

function renderRegionDetailStatistic(items) {
	regionDetailStats.removeAttribute("aria-busy");
	regionDetailStats.classList.toggle("is-compact-statistic", activeDetailTab === "recruitmentCategory");
	if (activeDetailTab === "period") {
		renderPeriodStatistic(items);
		return;
	}
	if (activeDetailTab === "recruitmentCategory") {
		renderBarList(regionDetailStats, recruitmentCategoryDisplayCounts(items), "label");
		return;
	}
	renderBarList(regionDetailStats, items, "label");
}

function recruitmentCategoryDisplayCounts(items) {
	const countsByCode = new Map(items.map((item) => [item.code, item.count || 0]));
	return RECRUITMENT_CATEGORY_DISPLAY_OPTIONS.map((option) => ({
		code: option.code,
		label: option.label,
		count: option.sourceCodes.reduce((sum, code) => sum + (countsByCode.get(code) || 0), 0)
	}));
}

function renderPeriodStatistic(items) {
	const yearlyCounts = latestNineYearCountsFromMonthly(items);
	if (!yearlyCounts.some((item) => item.year === selectedPeriodYear)) {
		selectedPeriodYear = yearlyCounts.at(-1)?.year || String(new Date().getFullYear());
	}
	regionDetailStats.replaceChildren();
	regionDetailStats.appendChild(createStatisticGroup(yearlyCounts, "year", "yearly", {
		selectedLabel: selectedPeriodYear,
		onSelect: (item) => {
			selectedPeriodYear = item.year;
			updateSelectedPeriodYear(regionDetailStats, selectedPeriodYear);
			renderSelectedYearMonthlyStatistic(regionDetailStats, items, selectedPeriodYear);
		}
	}));
	regionDetailStats.appendChild(createPeriodMonthlyGroup(items, selectedPeriodYear));
}

function updateSelectedPeriodYear(container, selectedYear) {
	container.querySelectorAll(".column-chart-yearly .column-item").forEach((column) => {
		column.classList.toggle("is-selected", column.dataset.statLabel === selectedYear);
	});
}

function renderSelectedYearMonthlyStatistic(container, items, year) {
	const monthlyGroup = container.querySelector(".statistic-group-monthly");
	if (!monthlyGroup) {
		container.appendChild(createPeriodMonthlyGroup(items, year));
		return;
	}
	monthlyGroup.replaceWith(createPeriodMonthlyGroup(items, year));
}

function createPeriodMonthlyGroup(items, year) {
	return createStatisticGroup(monthlyCountsForYear(items, year), "label", "monthly");
}

function createStatisticGroup(items, labelKey, variant, options = {}) {
	const group = document.createElement("section");
	group.className = `statistic-group statistic-group-${variant}`;
	const list = document.createElement("div");
	list.className = `column-chart statistic-group-list column-chart-${variant}`;
	group.appendChild(list);
	renderColumnChart(list, items, labelKey, options);
	return group;
}

function renderColumnChart(container, items, labelKey, options = {}) {
	container.replaceChildren();
	container.removeAttribute("aria-busy");
	if (!items.length) {
		const empty = document.createElement("p");
		empty.className = "empty-note";
		empty.textContent = "표시할 통계가 없습니다.";
		container.appendChild(empty);
		return;
	}

	const maxCount = Math.max(...items.map((item) => item.count || 0), 1);
	items.forEach((item) => {
		const value = item.count || 0;
		const label = item[labelKey] || "-";
		const percent = Math.max(6, Math.round((value / maxCount) * 100));
		const column = document.createElement(options.onSelect ? "button" : "div");
		column.className = "column-item";
		if (options.onSelect) {
			column.type = "button";
			column.classList.add("is-clickable");
			column.classList.toggle("is-selected", label === options.selectedLabel);
			column.addEventListener("click", () => options.onSelect(item));
		}
		column.dataset.statLabel = label;
		column.tabIndex = 0;
		column.setAttribute("aria-label", `${label} ${formatNumber(value)}개`);
		column.innerHTML = `
			<strong class="column-value">${formatNumber(value)}</strong>
			<div class="column-bar-wrap" aria-hidden="true">
				<span class="column-bar" style="--bar-height: ${percent}%"></span>
			</div>
			<span class="column-label">${escapeHtml(label)}</span>
		`;
		container.appendChild(column);
	});
}

function latestNineYearCountsFromMonthly(items) {
	const yearlyCounts = new Map();
	items.forEach((item) => {
		const year = String(item.yearMonth || "").slice(0, 4);
		if (!year) {
			return;
		}
		yearlyCounts.set(year, (yearlyCounts.get(year) || 0) + (item.count || 0));
	});
	const currentYear = new Date().getFullYear();
	return Array.from({ length: 9 }, (_, index) => String(currentYear - 8 + index))
		.map((year) => ({ year, count: yearlyCounts.get(year) || 0 }));
}

function monthlyCountsForYear(items, year) {
	const countsByMonth = new Map();
	items.forEach((item) => {
		const yearMonth = String(item.yearMonth || "");
		if (!yearMonth.startsWith(`${year}-`)) {
			return;
		}
		const month = Number(yearMonth.slice(5, 7));
		if (!month) {
			return;
		}
		countsByMonth.set(month, (countsByMonth.get(month) || 0) + (item.count || 0));
	});
	return Array.from({ length: 12 }, (_, index) => {
		const month = index + 1;
		return {
			label: `${month}월`,
			count: countsByMonth.get(month) || 0
		};
	});
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
	container.removeAttribute("aria-busy");
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
				<span style="--bar-width: ${percent}%"></span>
			</div>
		`;
		container.appendChild(row);
	});
}

function latestItems(items, limit) {
	return [...items]
		.sort((left, right) => String(right.yearMonth || "").localeCompare(String(left.yearMonth || "")))
		.slice(0, limit)
		.reverse();
}

function renderRegionLoadingState(container) {
	container.replaceChildren();
	container.setAttribute("aria-busy", "true");
	WORK_REGION_OPTIONS.forEach((region) => {
		const button = document.createElement("button");
		button.type = "button";
		button.className = "region-stat-button is-loading";
		button.disabled = true;
		button.dataset.regionCode = region.code;
		button.innerHTML = `
			<span>${escapeHtml(region.label)}</span>
			<strong class="region-count-skeleton skeleton-surface"></strong>
		`;
		container.appendChild(button);
	});
}

function renderStatisticLoadingState(container, tab) {
	container.replaceChildren();
	container.setAttribute("aria-busy", "true");
	if (tab === "period") {
		container.appendChild(createColumnChartSkeleton(recentYearLabels(), "yearly"));
		container.appendChild(createColumnChartSkeleton(monthLabels(), "monthly"));
		return;
	}
	if (tab === "recruitmentCategory") {
		renderFixedBarLoadingState(container, RECRUITMENT_CATEGORY_DISPLAY_OPTIONS, true);
		return;
	}
	if (tab === "hireType") {
		renderFixedBarLoadingState(container, HIRE_TYPE_DISPLAY_OPTIONS, false);
		return;
	}

	const list = document.createElement("div");
	list.className = "bar-list skeleton-bar-list";
	for (let index = 0; index < 8; index += 1) {
		const row = document.createElement("div");
		row.className = "bar-row skeleton-bar-row";
		row.innerHTML = `
			<div class="bar-row-meta">
				<span class="skeleton-surface"></span>
				<strong class="skeleton-surface"></strong>
			</div>
			<div class="bar-track skeleton-surface" aria-hidden="true"></div>
		`;
		list.appendChild(row);
	}
	container.appendChild(list);
}

function renderFixedBarLoadingState(container, items, compact) {
	const list = document.createElement("div");
	list.className = `bar-list skeleton-bar-list${compact ? " is-compact-statistic" : ""}`;
	items.forEach((item) => {
		const row = document.createElement("div");
		row.className = "bar-row skeleton-bar-row skeleton-fixed-label-row";
		row.innerHTML = `
			<div class="bar-row-meta">
				<span>${escapeHtml(item.label)}</span>
				<strong class="skeleton-surface"></strong>
			</div>
			<div class="bar-track skeleton-surface" aria-hidden="true"></div>
		`;
		list.appendChild(row);
	});
	container.appendChild(list);
}

function createColumnChartSkeleton(labels, variant) {
	const group = document.createElement("section");
	group.className = "statistic-group";
	const chart = document.createElement("div");
	chart.className = `column-chart statistic-group-list column-chart-${variant} column-chart-skeleton`;
	labels.forEach((label, index) => {
		const column = document.createElement("div");
		column.className = "column-item skeleton-column-item";
		const height = 28 + ((index * 19) % 58);
		column.innerHTML = `
			<strong class="column-value" aria-hidden="true"></strong>
			<div class="column-bar-wrap skeleton-bar-wrap" aria-hidden="true">
				<span class="column-bar skeleton-surface" style="--bar-height: ${height}%"></span>
			</div>
			<span class="column-label">${escapeHtml(label)}</span>
		`;
		chart.appendChild(column);
	});
	group.appendChild(chart);
	return group;
}

function recentYearLabels() {
	const currentYear = new Date().getFullYear();
	return Array.from({ length: 9 }, (_, index) => String(currentYear - 8 + index));
}

function monthLabels() {
	return Array.from({ length: 12 }, (_, index) => `${index + 1}월`);
}

function mergeRegionCounts(regionCounts) {
	const countsByCode = new Map(regionCounts.map((region) => [region.code, region.count || 0]));
	return WORK_REGION_OPTIONS.map((region) => ({
		...region,
		count: region.code === "ALL"
			? summaryTotalCount
			: countsByCode.get(region.code) || 0
	}));
}

function animateRegionCount(regionCode, count) {
	const target = regionStats?.querySelector(`[data-region-count="${regionCode}"]`);
	if (target) {
		animateNumber(target, count || 0);
	}
}

function renderPanelError(container, message) {
	container.replaceChildren();
	container.removeAttribute("aria-busy");
	const error = document.createElement("p");
	error.className = "empty-note is-error";
	error.textContent = message;
	container.appendChild(error);
}

function showStatus(message, isError = false) {
	if (!statisticsStatus) {
		return;
	}
	statisticsStatus.hidden = true;
	statisticsStatus.textContent = "";
	statisticsStatus.classList.remove("is-error");
	if (message) {
		showToast(message, isError ? "error" : "success");
	}
}

function hideStatus() {
	if (!statisticsStatus) {
		return;
	}
	statisticsStatus.hidden = true;
	statisticsStatus.textContent = "";
	statisticsStatus.classList.remove("is-error");
}

function showToast(message, type = "error") {
	if (!message) {
		return;
	}

	const toast = document.createElement("div");
	toast.className = "toast";
	toast.dataset.type = type;
	toast.setAttribute("role", type === "error" ? "alert" : "status");
	toast.setAttribute("aria-live", type === "error" ? "assertive" : "polite");
	toast.textContent = message;
	document.body.append(toast);

	window.setTimeout(() => {
		toast.classList.add("is-hiding");
	}, 2400);

	window.setTimeout(() => {
		toast.remove();
	}, 2800);
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
