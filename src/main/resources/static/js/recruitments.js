const form = document.querySelector("#searchForm");
const searchKeyword = document.querySelector("#searchKeyword");
const keywordSearchButton = document.querySelector("#keywordSearchButton");
const clearKeywordButton = document.querySelector("#clearKeywordButton");
const keywordSearchControl = document.querySelector(".list-filter-search-control");
const loadingState = document.querySelector("#loadingState");
const emptyState = document.querySelector("#emptyState");
const resultCountSummary = document.querySelector("#resultCountSummary");
const dataRefreshText = document.querySelector("#dataRefreshText");
const dataRefreshButton = document.querySelector("#dataRefreshButton");
const syncStatusBadge = document.querySelector("#syncStatusBadge");
const syncFailureDetailButton = document.querySelector("#syncFailureDetailButton");
const syncFailurePanel = document.querySelector("#syncFailurePanel");
const syncFailureContent = document.querySelector("#syncFailureContent");
const listFilterRow = document.querySelector("#listFilterRow");
const listStatusFilter = document.querySelector("#listStatusFilter");
const listCompanyFilter = document.querySelector("#listCompanyFilter");
const listPeriodSortFilter = document.querySelector("#listPeriodSortFilter");
const listCategoryFilter = document.querySelector("#listCategoryFilter");
const listHireTypeFilter = document.querySelector("#listHireTypeFilter");
const listNcsFilter = document.querySelector("#listNcsFilter");
const listRegionFilter = document.querySelector("#listRegionFilter");
const resultList = document.querySelector("#resultList");
const pagination = document.querySelector("#pagination");
const resultStage = document.querySelector("#resultStage");
const statusBanner = document.querySelector("#statusBanner");
const debugPanel = document.querySelector("#debugPanel");
const debugContent = document.querySelector("#debugContent");
const jobPreferenceToggle = document.querySelector("#jobPreferenceToggle");
const favoriteToggle = document.querySelector("#favoriteToggle");
const FAVORITE_API_PATH = "/api/members/me/favorite-recruitments";
const PAGE_SIZE = 10;

let currentPage = 1;
let currentItems = [];
let currentSummaryContext = { keyword: "", totalCount: 0 };
let latestOverallTotalCount = 0;
let isClearingKeyword = false;
let syncEventSource = null;
let lastSyncStatus = "IDLE";
let syncCompletionHideTimer = null;
let syncStatusPollTimer = null;
let shouldShowSyncCompletion = false;
let refreshButtonStateTimer = null;
let refreshButtonContentTimer = null;
let jobPreferenceCache = null;
let jobPreferenceCompanyOptionsCache = null;
const favoriteRecruitmentIds = new Set();
const MIN_LOADING_MS = 350;
const refreshButtonIcon = `
	<svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
		<path d="M21 12a9 9 0 0 1-15.5 6.2"></path>
		<path d="M3 12a9 9 0 0 1 15.5-6.2"></path>
		<path d="M18 3v4h-4"></path>
		<path d="M6 21v-4h4"></path>
	</svg>
`;
const completedButtonContent = `
	<svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
		<path d="M20 6 9 17l-5-5"></path>
	</svg>
	<span class="data-refresh-label">갱신 완료</span>
`;

function buildRefreshButtonContent(icon, label, detail = "") {
	return `
		<span class="data-refresh-icon">${icon || ""}</span>
		<span class="data-refresh-label">${label}</span>
		<span class="data-refresh-detail">${detail}</span>
	`;
}

function buildProgressDetail(percentage, currentPage, totalPages) {
	return `
		<span class="data-refresh-dynamic">${percentage}%</span>
		<span> (</span><span class="data-refresh-dynamic">${currentPage}</span><span>/${totalPages})</span>
	`;
}

function setRefreshButtonContent(content, viewKey) {
	if (!dataRefreshButton) {
		return;
	}

	const previousViewKey = dataRefreshButton.dataset.viewKey || "";
	const isContentChanged = previousViewKey === viewKey;
	dataRefreshButton.dataset.viewKey = viewKey;
	dataRefreshButton.innerHTML = content;

	if (isContentChanged) {
		window.clearTimeout(refreshButtonContentTimer);
		dataRefreshButton.classList.remove("is-content-changing");
		void dataRefreshButton.offsetWidth;
		dataRefreshButton.classList.add("is-content-changing");
		refreshButtonContentTimer = window.setTimeout(() => {
			dataRefreshButton.classList.remove("is-content-changing");
		}, 220);
		return;
	}

	window.clearTimeout(refreshButtonStateTimer);
	window.clearTimeout(refreshButtonContentTimer);
	dataRefreshButton.classList.remove("is-content-changing", "is-state-changing");
	void dataRefreshButton.offsetWidth;
	dataRefreshButton.classList.add("is-state-changing");
	refreshButtonStateTimer = window.setTimeout(() => {
		dataRefreshButton.classList.remove("is-state-changing");
	}, 240);
}

const selectOptions = {
	hireTypeLst: [
		{ value: "R1010", label: "정규직" },
		{ value: "R1020", label: "계약직" },
		{ value: "R1030", label: "무기계약직" },
		{ value: "R1040", label: "비정규직" },
		{ value: "R1050", label: "청년인턴" },
		{ value: "R1060", label: "청년인턴(체험형)" },
		{ value: "R1070", label: "청년인턴(채용형)" }
	],
	workRgnLst: [
		{ value: "R3010", label: "서울" },
		{ value: "R3011", label: "인천" },
		{ value: "R3012", label: "대전" },
		{ value: "R3013", label: "대구" },
		{ value: "R3014", label: "부산" },
		{ value: "R3015", label: "광주" },
		{ value: "R3016", label: "울산" },
		{ value: "R3017", label: "경기" },
		{ value: "R3018", label: "강원" },
		{ value: "R3019", label: "충청남도" },
		{ value: "R3020", label: "충청북도" },
		{ value: "R3021", label: "경상북도" },
		{ value: "R3022", label: "경상남도" },
		{ value: "R3023", label: "전라남도" },
		{ value: "R3024", label: "전라북도" },
		{ value: "R3025", label: "제주" },
		{ value: "R3026", label: "세종" },
		{ value: "R3030", label: "해외" }
	],
	ncsCdLst: [
		{ value: "R600001", label: "사업관리" },
		{ value: "R600002", label: "경영·회계·사무" },
		{ value: "R600003", label: "금융·보험" },
		{ value: "R600004", label: "교육·자연·사회과학" },
		{ value: "R600005", label: "법률·경찰·소방·교도·국방" },
		{ value: "R600006", label: "보건·의료" },
		{ value: "R600007", label: "사회복지·종교" },
		{ value: "R600008", label: "문화·예술·디자인·방송" },
		{ value: "R600009", label: "운전·운송" },
		{ value: "R600010", label: "영업·판매" },
		{ value: "R600011", label: "경비·청소" },
		{ value: "R600012", label: "이용·숙박·여행·오락·스포츠" },
		{ value: "R600013", label: "음식서비스" },
		{ value: "R600014", label: "건설" },
		{ value: "R600015", label: "기계" },
		{ value: "R600016", label: "재료" },
		{ value: "R600017", label: "화학" },
		{ value: "R600018", label: "섬유·의복" },
		{ value: "R600019", label: "전기·전자" },
		{ value: "R600020", label: "정보통신" },
		{ value: "R600021", label: "식품가공" },
		{ value: "R600022", label: "인쇄·목재·가구·공예" },
		{ value: "R600023", label: "환경·에너지·안전" },
		{ value: "R600024", label: "농림어업" },
		{ value: "R600025", label: "연구" }
	]
};

const recruitmentCategoryLabels = {
	R2010: "신입",
	R2020: "경력",
	R2040: "외국인"
};

const recruitmentCategoryAliases = {
	R2010: ["R2010"],
	R2020: ["R2020"],
	R2030: ["R2010", "R2020"],
	R2040: ["R2040"]
};

function initializeSelectOptions() {
	Object.entries(selectOptions).forEach(([id, options]) => {
		const select = document.querySelector(`#${id}`);
		if (!select) {
			return;
		}

		options.forEach((option) => {
			const element = document.createElement("option");
			element.value = option.value;
			element.textContent = option.label;
			select.appendChild(element);
		});
	});
}

function initializeListFilterOptions() {
	Object.entries(recruitmentCategoryLabels).forEach(([value, label]) => {
		listCategoryFilter.appendChild(createListFilterCheckbox(value, label));
	});

	selectOptions.hireTypeLst.forEach((hireType) => {
		listHireTypeFilter.appendChild(createListFilterCheckbox(hireType.value, hireType.label));
	});

	selectOptions.ncsCdLst.forEach((ncs) => {
		listNcsFilter.appendChild(createListFilterCheckbox(ncs.value, ncs.label));
	});

	selectOptions.workRgnLst.forEach((region) => {
		listRegionFilter.appendChild(createListFilterCheckbox(region.value, region.label));
	});
}

function createListFilterCheckbox(value, label) {
	const wrapper = document.createElement("label");
	const checkbox = document.createElement("input");
	checkbox.type = "checkbox";
	checkbox.value = value;
	wrapper.append(checkbox, ` ${label}`);
	return wrapper;
}

function syncCompanyFilterOptions(companyOptions = []) {
	const selectedValues = new Set(getCheckedFilterValues(listCompanyFilter));
	const companyNames = Array.from(new Set(companyOptions.filter(Boolean)))
		.sort((first, second) => first.localeCompare(second, "ko"));
	const companyLabelByValue = new Map(
		(jobPreferenceCompanyOptionsCache || []).map((company) => [company.detailCode, company.detailName])
	);

	listCompanyFilter.innerHTML = "";
	selectedValues.forEach((value) => {
		if (!companyNames.includes(value)) {
			companyNames.push(value);
		}
	});

	companyNames.forEach((companyName) => {
		const option = createListFilterCheckbox(companyName, companyLabelByValue.get(companyName) || companyName);
		const checkbox = option.querySelector("input");
		checkbox.checked = selectedValues.has(companyName);
		listCompanyFilter.appendChild(option);
	});
	updateListFilterIndicators();
}

async function fetchJobPreferenceCompanyOptions() {
	if (jobPreferenceCompanyOptionsCache) {
		return jobPreferenceCompanyOptionsCache;
	}

	const response = await fetch("/api/members/me/job-preference/companies", {
		headers: {
			Accept: "application/json"
		}
	});

	if (!response.ok) {
		throw new Error("기업 목록을 불러오지 못했습니다.");
	}

	jobPreferenceCompanyOptionsCache = await response.json();
	return jobPreferenceCompanyOptionsCache;
}

async function ensureCompanyFilterOptions(values = []) {
	if (!values.length) {
		return;
	}

	const existingValues = new Set(getCheckedFilterValues(listCompanyFilter));
	listCompanyFilter.querySelectorAll("input").forEach((input) => {
		existingValues.add(input.value);
	});

	const missingValues = values.filter((value) => !existingValues.has(value));
	if (!missingValues.length) {
		return;
	}

	const companyOptions = await fetchJobPreferenceCompanyOptions();
	const labelByValue = new Map(companyOptions.map((company) => [company.detailCode, company.detailName]));

	missingValues.forEach((value) => {
		listCompanyFilter.appendChild(createListFilterCheckbox(value, labelByValue.get(value) || value));
	});
}

function setLoading(isLoading) {
	loadingState.hidden = !isLoading;
	resultStage.classList.toggle("is-loading", isLoading);
	keywordSearchButton.disabled = isLoading;
	clearKeywordButton.disabled = isLoading;
}

function updateDataRefreshText(lastFetchedAt) {
	if (!lastFetchedAt) {
		dataRefreshText.textContent = "최종 갱신: 정보 없음";
		return;
	}

	const fetchedAt = new Date(lastFetchedAt);
	if (Number.isNaN(fetchedAt.getTime())) {
		dataRefreshText.textContent = `최종 갱신: ${lastFetchedAt}`;
		return;
	}

	dataRefreshText.textContent = `최종 갱신: ${new Intl.DateTimeFormat("ko-KR", {
		year: "numeric",
		month: "2-digit",
		day: "2-digit",
		hour: "2-digit",
		minute: "2-digit"
	}).format(fetchedAt)}`;
}

function updateResultCountSummary(totalCount, matchedCount) {
	const total = Number.isFinite(totalCount) ? totalCount : 0;
	const matched = Number.isFinite(matchedCount) ? matchedCount : 0;
	resultCountSummary.innerHTML = `
		전체 <strong>${total.toLocaleString("ko-KR")}</strong>개 중
		<strong>${matched.toLocaleString("ko-KR")}</strong>개
	`;
}

function renderRefreshButtonIdle() {
	if (!dataRefreshButton) {
		return;
	}
	dataRefreshButton.hidden = false;
	dataRefreshButton.disabled = false;
	dataRefreshButton.classList.remove("is-syncing", "is-pending", "is-failed", "is-completed");
	dataRefreshButton.style.removeProperty("--sync-progress");
	setRefreshButtonContent(refreshButtonIcon, "idle");
	dataRefreshButton.setAttribute("aria-label", "공고 갱신");
	dataRefreshButton.title = "공고 갱신";
}

function renderRefreshButtonProgress(label, percentage = 0, isPending = false, detail = "") {
	if (!dataRefreshButton) {
		return;
	}
	const safePercentage = Math.max(0, Math.min(100, parseFloat(percentage) || 0));
	dataRefreshButton.hidden = false;
	dataRefreshButton.disabled = true;
	dataRefreshButton.classList.add("is-syncing");
	dataRefreshButton.classList.remove("is-failed", "is-completed");
	dataRefreshButton.classList.toggle("is-pending", isPending);
	dataRefreshButton.style.setProperty("--sync-progress", `${safePercentage}%`);
	setRefreshButtonContent(
		buildRefreshButtonContent(isPending ? "" : refreshButtonIcon, label, detail),
		isPending ? `pending:${label}` : "syncing"
	);
	dataRefreshButton.setAttribute("aria-label", label);
	dataRefreshButton.title = label;
}

function renderRefreshButtonCompleted() {
	if (!dataRefreshButton) {
		return;
	}
	dataRefreshButton.hidden = false;
	dataRefreshButton.disabled = true;
	dataRefreshButton.classList.remove("is-syncing", "is-pending", "is-failed");
	dataRefreshButton.classList.add("is-completed");
	dataRefreshButton.style.removeProperty("--sync-progress");
	setRefreshButtonContent(completedButtonContent, "completed");
	dataRefreshButton.setAttribute("aria-label", "갱신 완료");
	dataRefreshButton.title = "갱신 완료";
}

function renderRefreshButtonFailed() {
	if (!dataRefreshButton) {
		return;
	}
	dataRefreshButton.hidden = false;
	dataRefreshButton.disabled = false;
	dataRefreshButton.classList.add("is-failed");
	dataRefreshButton.classList.remove("is-pending", "is-completed");
	dataRefreshButton.style.removeProperty("--sync-progress");
	setRefreshButtonContent(buildRefreshButtonContent(refreshButtonIcon, "갱신 실패"), "failed");
	dataRefreshButton.setAttribute("aria-label", "공고 다시 갱신");
	dataRefreshButton.title = "공고 다시 갱신";
}

function updateSyncStatus(progress) {
	const status = progress?.status || "IDLE";
	const previousStatus = lastSyncStatus;
	const percentage = Number(progress?.percentage ?? 0);
	const currentPage = Number(progress?.currentPage ?? 0);
	const totalPages = Number(progress?.totalPages ?? 0);
	lastSyncStatus = status;

	syncStatusBadge.classList.toggle("failed", status === "FAILED");
	syncStatusBadge.classList.toggle("completed", status === "COMPLETED");
	syncStatusBadge.classList.toggle("running", Boolean(progress?.inProgress));
	syncFailureDetailButton.hidden = status !== "FAILED";

	if (progress?.inProgress) {
		shouldShowSyncCompletion = true;
		window.clearTimeout(syncCompletionHideTimer);
		syncStatusBadge.hidden = true;
		dataRefreshText.hidden = true;
		renderRefreshButtonProgress(
			totalPages ? "갱신 중" : "갱신 준비 중",
			percentage,
			!totalPages,
			totalPages ? buildProgressDetail(percentage, currentPage, totalPages) : ""
		);
		syncFailurePanel.hidden = true;
		return;
	}

	stopSyncStatusPolling();

	if (status === "FAILED") {
		shouldShowSyncCompletion = false;
		window.clearTimeout(syncCompletionHideTimer);
		renderRefreshButtonFailed();
		syncStatusBadge.hidden = true;
		dataRefreshText.hidden = true;
		syncFailureContent.textContent = JSON.stringify(progress?.failureResponse || {}, null, 2);
		return;
	}

	if (status === "COMPLETED") {
		if (shouldShowSyncCompletion && previousStatus !== "COMPLETED") {
			shouldShowSyncCompletion = false;
			renderRefreshButtonCompleted();
			syncStatusBadge.hidden = true;
			dataRefreshText.hidden = true;
			loadRecruitments(currentPage, false);
			window.clearTimeout(syncCompletionHideTimer);
			syncCompletionHideTimer = window.setTimeout(() => {
				renderRefreshButtonIdle();
				dataRefreshText.hidden = false;
			}, 3000);
		}
		return;
	}

	if (status === "CANCELED") {
		shouldShowSyncCompletion = false;
		window.clearTimeout(syncCompletionHideTimer);
		renderRefreshButtonIdle();
		syncStatusBadge.hidden = true;
		dataRefreshText.hidden = false;
		syncStatusBadge.textContent = "";
		return;
	}

	window.clearTimeout(syncCompletionHideTimer);
	shouldShowSyncCompletion = false;
	renderRefreshButtonIdle();
	syncStatusBadge.hidden = true;
	dataRefreshText.hidden = false;
	syncStatusBadge.textContent = "";
	syncFailurePanel.hidden = true;
}

function stopSyncStatusPolling() {
	window.clearTimeout(syncStatusPollTimer);
	syncStatusPollTimer = null;
}

function connectSyncEvents() {
	if (!window.EventSource || syncEventSource) {
		return;
	}
	syncEventSource = new EventSource("/api/recruitments/alio/sync-events");
	syncEventSource.addEventListener("progress", (event) => {
		try {
			updateSyncStatus(JSON.parse(event.data));
		} catch (error) {
			// Ignore malformed server-sent events.
		}
	});
	syncEventSource.onerror = () => {
		syncEventSource.close();
		syncEventSource = null;
		window.setTimeout(connectSyncEvents, 3000);
	};
}

async function pollSyncStatus() {
	try {
		const response = await fetch("/api/recruitments/alio/sync-status", {
			headers: {
				Accept: "application/json"
			}
		});
		const progress = await response.json();
		if (!response.ok) {
			throw new Error(progress?.detail || "갱신 상태를 확인하지 못했습니다.");
		}
		updateSyncStatus(progress);
		if (progress?.inProgress) {
			syncStatusPollTimer = window.setTimeout(pollSyncStatus, 1500);
		}
	} catch (error) {
		syncStatusPollTimer = window.setTimeout(pollSyncStatus, 3000);
	}
}

function startSyncStatusPolling() {
	stopSyncStatusPolling();
	syncStatusPollTimer = window.setTimeout(pollSyncStatus, 1200);
}

async function startRecruitmentSynchronization() {
	if (!dataRefreshButton || dataRefreshButton.disabled || lastSyncStatus === "RUNNING") {
		return;
	}

	try {
		shouldShowSyncCompletion = true;
		dataRefreshButton.disabled = true;
		dataRefreshText.hidden = true;
		syncFailurePanel.hidden = true;
		renderRefreshButtonProgress("갱신 준비 중", 0, true);
		startSyncStatusPolling();
		const response = await fetch("/api/recruitments/alio/sync", {
			method: "POST",
			headers: {
				Accept: "application/json"
			}
		});
		const progress = await response.json().catch(() => null);

		if (!response.ok) {
			throw new Error(progress?.detail || "공고 갱신 요청 중 오류가 발생했습니다.");
		}

		updateSyncStatus(progress);
		if (progress?.inProgress) {
			startSyncStatusPolling();
		}
	} catch (error) {
		stopSyncStatusPolling();
		shouldShowSyncCompletion = false;
		setStatus(error.message || "공고 갱신 요청 중 오류가 발생했습니다.");
		renderRefreshButtonIdle();
		dataRefreshText.hidden = false;
	}
}

function setStatus(message, type = "error") {
	statusBanner.textContent = message || "";
	statusBanner.hidden = !message;
	statusBanner.classList.toggle("success", type === "success");
}

function setDebug(debugData) {
	if (!debugData) {
		debugPanel.hidden = true;
		debugContent.textContent = "";
		return;
	}

	debugPanel.hidden = false;
	debugContent.textContent = JSON.stringify(debugData, null, 2);
}

function extractItems(payload) {
	if (Array.isArray(payload?.result)) {
		return payload.result;
	}

	if (Array.isArray(payload?.response?.body?.items?.item)) {
		return payload.response.body.items.item;
	}

	if (Array.isArray(payload?.response?.body?.items)) {
		return payload.response.body.items;
	}

	if (Array.isArray(payload?.response?.body?.item)) {
		return payload.response.body.item;
	}

	return [];
}

function isSuccessfulResponse(payload) {
	const resultCode = payload?.resultCode;

	if (resultCode === undefined || resultCode === null || resultCode === "") {
		return true;
	}

	const normalized = String(resultCode).trim();
	return normalized === "00" || normalized === "200";
}

function formatDate(value) {
	if (!value) {
		return "정보 없음";
	}

	const text = String(value).trim();
	if (/^\d{8}$/.test(text)) {
		return `${text.slice(0, 4)}.${text.slice(4, 6)}.${text.slice(6, 8)}`;
	}

	const date = new Date(text);
	if (Number.isNaN(date.getTime())) {
		return text;
	}

	return new Intl.DateTimeFormat("ko-KR", {
		year: "numeric",
		month: "2-digit",
		day: "2-digit"
	}).format(date);
}

function parseRecruitmentDate(value) {
	if (!value) {
		return null;
	}

	const text = String(value).trim();
	if (!text) {
		return null;
	}

	if (/^\d{8}$/.test(text)) {
		const year = Number(text.slice(0, 4));
		const month = Number(text.slice(4, 6)) - 1;
		const day = Number(text.slice(6, 8));
		return new Date(year, month, day);
	}

	const parsed = new Date(text);
	if (Number.isNaN(parsed.getTime())) {
		return null;
	}

	return new Date(parsed.getFullYear(), parsed.getMonth(), parsed.getDate());
}

function getRecruitmentStatus(startDateValue, endDateValue) {
	const startDate = parseRecruitmentDate(startDateValue);
	const endDate = parseRecruitmentDate(endDateValue);

	if (!startDate || !endDate) {
		return null;
	}

	const today = new Date();
	const normalizedToday = new Date(today.getFullYear(), today.getMonth(), today.getDate());

	if (normalizedToday < startDate) {
		return {
			label: "예정",
			tone: "scheduled"
		};
	}

	if (normalizedToday > endDate) {
		return {
			label: "마감",
			tone: "closed"
		};
	}

	return {
		label: "진행",
		tone: "active"
	};
}

function getValue(item, ...keys) {
	for (const key of keys) {
		const value = item?.[key];
		if (value !== undefined && value !== null && String(value).trim() !== "") {
			return String(value).trim();
		}
	}

	return "";
}

function escapeAttribute(value) {
	return String(value)
		.replaceAll("&", "&amp;")
		.replaceAll("\"", "&quot;")
		.replaceAll("<", "&lt;")
		.replaceAll(">", "&gt;");
}

function createMetaCountBadge(values) {
	const hiddenValues = values.slice(1);
	const tooltip = escapeAttribute(hiddenValues.join("\n"));
	return `<span class="meta-count" data-tooltip="${tooltip}" title="${tooltip}">+${hiddenValues.length}</span>`;
}

function summarizeListValue(value) {
	if (!value) {
		return "";
	}

	const values = String(value)
		.split(/\s*(?:,|\||;)\s*/)
		.map((text) => text.trim())
		.filter(Boolean);

	if (values.length <= 1) {
		return value;
	}

	return `<span class="meta-primary">${values[0]}</span>${createMetaCountBadge(values)}`;
}

function summarizeMetaValues(values) {
	const filteredValues = values.map((value) => String(value).trim()).filter(Boolean);

	if (filteredValues.length <= 1) {
		return filteredValues[0] || "";
	}

	return `<span class="meta-primary">${filteredValues[0]}</span>${createMetaCountBadge(filteredValues)}`;
}

function getHireTypeLabel(item) {
	const hireTypeValue = getValue(item, "hireTypeNmLst", "hireTypeNm", "hireType");
	const matchedLabels = selectOptions.hireTypeLst
		.map((hireType) => hireType.label)
		.filter((label) => hireTypeValue.includes(label));

	return matchedLabels.length
		? summarizeMetaValues(matchedLabels)
		: summarizeListValue(hireTypeValue);
}

function getRecruitmentCategoryLabel(item) {
	const codes = getRecruitmentCategoryCodes(item);
	const name = getValue(item, "recrutSeNm");
	const labels = codes
		.map((code) => recruitmentCategoryLabels[code])
		.filter(Boolean);

	if (labels.length > 1) {
		return summarizeMetaValues(labels);
	}

	if (labels.length === 1) {
		return labels[0];
	}

	if (name.includes("+")) {
		return summarizeMetaValues(name.split(/\s*\+\s*/));
	}

	return name || getValue(item, "recrutSe") || "";
}

function getRecruitmentCategoryCodes(item) {
	const code = getValue(item, "recrutSe");
	const name = getValue(item, "recrutSeNm");

	if (recruitmentCategoryAliases[code]) {
		return recruitmentCategoryAliases[code];
	}

	if (name.includes("신입") && name.includes("경력")) {
		return ["R2010", "R2020"];
	}

	return code ? [code] : [];
}

function itemMatchesHireType(item, hireTypeCode) {
	const hireTypeLabel = selectOptions.hireTypeLst.find((hireType) => hireType.value === hireTypeCode)?.label || "";
	const hireTypeValues = [
		getValue(item, "hireTypeLst", "hireTypeCdLst", "hireTypeCd"),
		getValue(item, "hireTypeNmLst", "hireTypeNm", "hireType")
	].join(" ");

	return hireTypeValues.includes(hireTypeCode) || (!!hireTypeLabel && hireTypeValues.includes(hireTypeLabel));
}

function itemMatchesNcs(item, ncsCode) {
	const ncsLabel = selectOptions.ncsCdLst.find((ncs) => ncs.value === ncsCode)?.label || "";
	const ncsValues = [
		getValue(item, "ncsCdLst", "ncsCd", "ncsCode"),
		getValue(item, "ncsCdNmLst", "ncsNmLst", "ncsNm", "ncsName")
	].join(" ");

	return ncsValues.includes(ncsCode) || (!!ncsLabel && ncsValues.includes(ncsLabel));
}

function itemMatchesRegion(item, regionCode) {
	if (!regionCode) {
		return true;
	}

	const regionLabel = selectOptions.workRgnLst.find((region) => region.value === regionCode)?.label || "";
	const regionValues = [
		getValue(item, "workRgnLst", "workRgnCdLst", "workRgnCd", "workRegionCode"),
		getValue(item, "workRgnNmLst", "workRgnNm", "workRegionNm")
	].join(" ");

	return regionValues.includes(regionCode) || (!!regionLabel && regionValues.includes(regionLabel));
}

function createMetaRow(value, className = "") {
	return `
		<div class="meta-row ${className}">
			<span class="meta-value">${value || "정보 없음"}</span>
		</div>
	`;
}

function createCompanyMeta(institution, companyDivision, companyType) {
	const details = [companyDivision, companyType].filter(Boolean);

	return `
		<div class="meta-row meta-row-company">
			<span class="company-meta">
				${details.length ? `<span class="company-meta-detail">${details.join(" · ")}</span>` : ""}
				<span class="company-meta-name">${institution || "정보 없음"}</span>
			</span>
		</div>
	`;
}

function createStatusBadge(status) {
	const tone = status?.tone || "unknown";
	const label = status?.label || "정보 없음";
	return `<span class="status-badge status-badge-${tone}" aria-label="${escapeAttribute(label)}" title="${escapeAttribute(label)}"><span class="visually-hidden">${label}</span></span>`;
}

function getPeriodDdayBadge(status, startDateValue, endDateValue) {
	const today = new Date();
	const normalizedToday = new Date(today.getFullYear(), today.getMonth(), today.getDate());
	const startDate = parseRecruitmentDate(startDateValue);
	const endDate = parseRecruitmentDate(endDateValue);
	const targetDate = status?.tone === "scheduled" ? startDate : status?.tone === "active" ? endDate : null;
	if (!targetDate) {
		return "";
	}

	const remainingDays = Math.ceil((targetDate.getTime() - normalizedToday.getTime()) / (1000 * 60 * 60 * 24));
	if (remainingDays < 0) {
		return "";
	}

	const label = status.tone === "scheduled" ? "신청까지" : "마감까지";
	const dayText = remainingDays === 0 ? "D-day" : `D-${remainingDays}`;
	return `<span class="period-dday-wrap"><span class="period-dday-label">${label}</span><span class="period-dday">${dayText}</span></span>`;
}

function escapeAttribute(value) {
	return String(value ?? "")
		.replaceAll("&", "&amp;")
		.replaceAll('"', "&quot;")
		.replaceAll("<", "&lt;")
		.replaceAll(">", "&gt;");
}

function getRecruitmentFavoriteId(item) {
	return String(getValue(item, "sourceRecruitmentId", "recrutPblntSn", "pbancSn") || "");
}

function limitText(value, maxLength) {
	const text = String(value || "").trim();
	return text.length > maxLength ? text.slice(0, maxLength) : text;
}

function buildFavoriteRequest(item) {
	const sourceRecruitmentId = getRecruitmentFavoriteId(item);
	return {
		source: "ALIO",
		sourceRecruitmentId,
		recruitmentTitle: limitText(getValue(item, "recrutPbancTtl") || "제목 정보 없음", 255),
		institutionName: limitText(getValue(item, "instNm") || "기관 정보 없음", 100),
		hireType: limitText(getHireTypeLabel(item), 100),
		workRegion: limitText(summarizeListValue(getValue(item, "workRgnNmLst", "workRgnNm", "workRegionNm")), 100),
		recruitmentStartDate: getValue(item, "pbancBgngYmd"),
		recruitmentEndDate: getValue(item, "pbancEndYmd"),
		recruitmentUrl: limitText(getValue(item, "srcUrl", "url"), 500)
	};
}

function normalizeFavoriteRecruitment(favorite) {
	return {
		sourceRecruitmentId: String(favorite.sourceRecruitmentId || ""),
		recrutPbancTtl: favorite.recruitmentTitle,
		instNm: favorite.institutionName,
		hireTypeNmLst: favorite.hireType,
		workRgnNmLst: favorite.workRegion,
		pbancBgngYmd: favorite.recruitmentStartDate,
		pbancEndYmd: favorite.recruitmentEndDate,
		srcUrl: favorite.recruitmentUrl
	};
}

function isFavoriteListActive() {
	return Boolean(favoriteToggle?.checked);
}

function createRecruitmentCard(item) {
	const title = getValue(item, "recrutPbancTtl");
	const institution = getValue(item, "instNm");
	const companyDivision = getValue(item, "instSeNm", "instSe", "orgSeNm", "orgSe");
	const companyType = getValue(item, "instKndNm", "instKnd", "pblntInstTypeNm", "pblntInstType");
	const hireType = getHireTypeLabel(item);
	const recruitmentCategory = getRecruitmentCategoryLabel(item);
	const region = summarizeListValue(getValue(item, "workRgnNmLst", "workRgnNm", "workRegionNm"));
	const ncs = summarizeListValue(getValue(item, "ncsCdNmLst", "ncsNmLst", "ncsNm", "ncsName"));
	const rawStartDate = getValue(item, "pbancBgngYmd");
	const rawEndDate = getValue(item, "pbancEndYmd");
	const startDate = formatDate(rawStartDate);
	const endDate = formatDate(rawEndDate);
	const period = `${startDate} ~ ${endDate}`;
	const status = getRecruitmentStatus(rawStartDate, rawEndDate);
	const periodDdayBadge = getPeriodDdayBadge(status, rawStartDate, rawEndDate);
	const detailUrl = getValue(item, "srcUrl", "url");
	const favoriteId = getRecruitmentFavoriteId(item);
	const isFavorite = favoriteId && favoriteRecruitmentIds.has(favoriteId);
	const favoriteActionLabel = isFavorite ? "관심공고 해제" : "관심공고 설정";
	const favoriteActionButton = favoriteToggle
		? `
			<button type="button" class="favorite-reveal-button" data-favorite-action aria-label="${favoriteActionLabel}" title="${favoriteActionLabel}">
				<svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
					<path d="M19 21l-7-4-7 4V5a2 2 0 0 1 2-2h10a2 2 0 0 1 2 2z"></path>
				</svg>
			</button>
		`
		: "";

	const cardContent = `
		<div class="card-top">
			${createStatusBadge(status)}
			<div class="card-main">
				<h3 class="card-title">${title || "제목 정보 없음"}</h3>
			</div>
		</div>

		<div class="meta-list">
			${createMetaRow(`${period}${periodDdayBadge}`, "meta-row-period")}
			${createCompanyMeta(institution, companyDivision, companyType)}
			${createMetaRow(region)}
			${createMetaRow(recruitmentCategory)}
			${createMetaRow(hireType)}
			${createMetaRow(ncs)}
		</div>
	`;
	const cardLabel = `${title || "채용공고"} 원문보기`;
	const cardElement = detailUrl
		? `<a class="recruitment-card" href="${escapeAttribute(detailUrl)}" target="_blank" rel="noopener noreferrer" aria-label="${escapeAttribute(cardLabel)}">${cardContent}</a>`
		: `<div class="recruitment-card recruitment-card-disabled" aria-label="상세 링크 정보 없음">${cardContent}</div>`;

	return `
		<article class="recruitment-card-shell ${isFavorite ? "is-favorite" : ""}" data-recruitment-id="${escapeAttribute(favoriteId)}">
			${favoriteActionButton}
			${cardElement}
		</article>
	`;
}

function getFilteredItems(items) {
	return items;
}

function hasListHeaderFilter() {
	return [listStatusFilter, listCompanyFilter, listCategoryFilter, listHireTypeFilter, listNcsFilter, listRegionFilter]
		.some((filter) => getCheckedFilterValues(filter).length > 0);
}

function getCheckedFilterValues(filter) {
	return Array.from(filter.querySelectorAll("input:checked"), (input) => input.value);
}

function setCheckedFilterValues(filter, values = []) {
	const selectedValues = new Set(values);
	filter.querySelectorAll("input").forEach((input) => {
		input.checked = selectedValues.has(input.value);
	});
}

function setPeriodSort(value = "recent") {
	const periodSortInput = listPeriodSortFilter.querySelector(`input[value="${value}"]`);
	if (periodSortInput) {
		periodSortInput.checked = true;
	}
}

function hasJobPreferenceValues(preference) {
	return Boolean(
		preference?.searchKeyword
			|| preference?.recruitmentStatuses?.length
			|| preference?.regions?.length
			|| preference?.categories?.length
			|| preference?.hireTypes?.length
			|| preference?.ncsCodes?.length
	);
}

function buildSummary(items) {
	const { keyword, totalCount } = currentSummaryContext;
	const filterText = hasListHeaderFilter() ? ` 목록 필터 적용 후 ${items.length}건을 표시하고 있습니다.` : "";

	return keyword
		? `"${keyword}" 검색 결과입니다. 전체 ${totalCount}건 중 현재 ${items.length}건을 표시하고 있습니다.${filterText}`
		: `필터가 적용되지 않은 기본 목록입니다. 전체 ${totalCount}건 중 현재 ${items.length}건을 표시하고 있습니다.${filterText}`;
}

function renderFilteredItems() {
	updateListFilterIndicators();
	const filteredItems = getFilteredItems(currentItems);
	renderItems(filteredItems, buildSummary(filteredItems), { showHeader: currentItems.length > 0 });
	if (filteredItems.length) {
		renderPagination(currentSummaryContext.totalCount, PAGE_SIZE, currentPage);
	}
}

function updateListSortTrigger() {
	document.querySelectorAll("[data-sort-trigger]").forEach((trigger) => {
		const menu = document.querySelector(`#${trigger.getAttribute("aria-controls")}`);
		const checkedInput = menu?.querySelector("input:checked");
		const badgeText = checkedInput?.value === "deadline" ? "마감" : "등록";
		trigger.classList.add("is-filtered");
		trigger.dataset.filterCount = badgeText;
	});
}

function renderItems(items, summary, options = {}) {
	listFilterRow.hidden = false;

	if (!items.length) {
		resultList.hidden = true;
		resultList.innerHTML = "";
		pagination.hidden = true;
		pagination.innerHTML = "";
		emptyState.hidden = false;

		const emptyTitle = emptyState.querySelector("h3");
		const emptyDescription = emptyState.querySelector("p");

		if (emptyTitle) {
			emptyTitle.textContent = options.emptyTitle || "해당 조건을 만족하는 공고가 없습니다.";
		}
		if (emptyDescription) {
			emptyDescription.textContent = options.emptyDescription || "";
		}
		return;
	}

	resultList.innerHTML = items.map(createRecruitmentCard).join("");
	resultList.hidden = false;
	emptyState.hidden = true;
}

function renderPagination(totalCount, pageSize, page) {
	const totalPages = Math.max(1, Math.ceil(totalCount / pageSize));

	if (totalPages <= 1) {
		pagination.hidden = true;
		pagination.innerHTML = "";
		return;
	}

	const pageGroupStart = Math.floor((page - 1) / 10) * 10 + 1;
	const pageGroupEnd = Math.min(pageGroupStart + 9, totalPages);
	const pageControls = [];
	for (let number = pageGroupStart; number <= pageGroupEnd; number += 1) {
		pageControls.push(`
			<button class="pagination-button ${number === page ? "is-active" : ""}" data-page="${number}" ${number === page ? 'aria-current="page"' : ""}>
				${number}
			</button>
		`);
	}

	pagination.innerHTML = `
		<button class="pagination-edge-button" data-page="1" ${page === 1 ? "disabled" : ""}>처음</button>
		<div class="pagination-center">
			<button class="pagination-control-button" data-page="${Math.max(1, page - 10)}" ${page <= 10 ? "disabled" : ""} aria-label="10페이지 이전" title="10페이지 이전">
				<svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
					<path d="m11 17-5-5 5-5"></path>
					<path d="m18 17-5-5 5-5"></path>
				</svg>
			</button>
			<button class="pagination-control-button" data-page="${page - 1}" ${page === 1 ? "disabled" : ""} aria-label="이전 페이지" title="이전 페이지">
				<svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
					<path d="m15 18-6-6 6-6"></path>
				</svg>
			</button>
			<div class="pagination-pages">
				${pageControls.join("")}
			</div>
			<button class="pagination-control-button" data-page="${page + 1}" ${page === totalPages ? "disabled" : ""} aria-label="다음 페이지" title="다음 페이지">
				<svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
					<path d="m9 18 6-6-6-6"></path>
				</svg>
			</button>
			<button class="pagination-control-button" data-page="${Math.min(totalPages, page + 10)}" ${pageGroupEnd === totalPages ? "disabled" : ""} aria-label="10페이지 다음" title="10페이지 다음">
				<svg viewBox="0 0 24 24" aria-hidden="true" focusable="false">
					<path d="m6 17 5-5-5-5"></path>
					<path d="m13 17 5-5-5-5"></path>
				</svg>
			</button>
		</div>
		<button class="pagination-edge-button" data-page="${totalPages}" ${page === totalPages ? "disabled" : ""}>마지막</button>
	`;
	pagination.hidden = false;
}

function buildQueryString(page = currentPage) {
	const formData = new FormData(form);
	const params = new URLSearchParams();
	const periodSort = listPeriodSortFilter.querySelector("input:checked")?.value || "recent";
	const statusFilters = getCheckedFilterValues(listStatusFilter);
	const companyFilters = getCheckedFilterValues(listCompanyFilter);
	const regionFilters = getCheckedFilterValues(listRegionFilter);
	const categoryFilters = getCheckedFilterValues(listCategoryFilter);
	const hireTypeFilters = getCheckedFilterValues(listHireTypeFilter);
	const ncsFilters = getCheckedFilterValues(listNcsFilter);

	formData.forEach((value, key) => {
		const trimmed = String(value).trim();
		if (trimmed) {
			params.append(key, trimmed);
		}
	});

	params.set("pageNo", String(page));
	params.set("numOfRows", String(PAGE_SIZE));
	params.set("sortBy", periodSort === "deadline" ? "DEADLINE_DATE" : "RECRUITMENT_SEQUENCE");
	params.set("sortDirection", periodSort === "deadline" ? "ASC" : "DESC");
	if (statusFilters.length) {
		params.set("recruitmentStatus", statusFilters.join(","));
	}
	if (companyFilters.length) {
		params.set("pblntInstCd", companyFilters.join(","));
	}
	if (regionFilters.length) {
		params.set("workRgnLst", regionFilters.join(","));
	}
	if (categoryFilters.length) {
		params.set("recrutSe", categoryFilters.join(","));
	}
	if (hireTypeFilters.length) {
		params.set("hireTypeLst", hireTypeFilters.join(","));
	}
	if (ncsFilters.length) {
		params.set("ncsCdLst", ncsFilters.join(","));
	}
	return params.toString();
}

async function loadRecruitments(page = currentPage, showLoading = true) {
	if (isFavoriteListActive()) {
		await renderFavoriteRecruitments();
		return;
	}

	currentPage = page;
	const loadingStartedAt = Date.now();
	if (showLoading) {
		setLoading(true);
	}
	setStatus("");
	setDebug(null);

	try {
		const query = buildQueryString(page);
		const response = await fetch(`/api/recruitments/alio?${query}`, {
			headers: {
				Accept: "application/json"
			}
		});

		const payload = await response.json();

		if (!response.ok) {
			setStatus(payload.detail || "목록 조회 중 오류가 발생했습니다.");
			setDebug(payload);
			renderItems([], "오류로 인해 목록을 불러오지 못했습니다.");
			return;
		}

		if (!isSuccessfulResponse(payload)) {
			setStatus(`${payload.resultMsg || "ALIO 오류"} (${payload.resultMsgEng || payload.resultCode})`);
			setDebug(payload._debug || payload);
			renderItems([], "ALIO 응답에서 오류가 반환되었습니다.");
			return;
		}

		const items = extractItems(payload);
		const totalCount = Number(payload?.totalCount ?? items.length);
		const overallTotalCount = Number(payload?.overallTotalCount ?? payload?.response?.body?.overallTotalCount ?? totalCount);
		const keyword = searchKeyword.value.trim();
		currentItems = items;
		currentSummaryContext = { keyword, totalCount };
		latestOverallTotalCount = overallTotalCount;
		updateDataRefreshText(payload?.lastFetchedAt ?? payload?.response?.body?.lastFetchedAt);
		updateResultCountSummary(overallTotalCount, totalCount);
		syncCompanyFilterOptions(payload?.filterOptions?.companies || []);
		const filteredItems = getFilteredItems(items);

		setStatus("");
		setDebug(null);
		renderItems(filteredItems, buildSummary(filteredItems), { showHeader: items.length > 0 });
		renderPagination(totalCount, PAGE_SIZE, currentPage);
	} catch (error) {
		setStatus("네트워크 오류가 발생했습니다. 서버 실행 상태를 확인해주세요.");
		setDebug({ message: error.message });
		renderItems([], "네트워크 오류로 인해 목록을 불러오지 못했습니다.");
	} finally {
		const elapsed = Date.now() - loadingStartedAt;
		if (showLoading && elapsed < MIN_LOADING_MS) {
			await new Promise((resolve) => window.setTimeout(resolve, MIN_LOADING_MS - elapsed));
		}
		if (showLoading) {
			setLoading(false);
		}
	}
}

function resetForm() {
	searchKeyword.value = "";
	keywordSearchControl.classList.remove("is-expanded");
	if (jobPreferenceToggle) {
		jobPreferenceToggle.checked = false;
	}
	updateKeywordSearchState();
	currentPage = 1;
	clearListHeaderFilters();
	setPeriodSort("recent");
	setStatus("");
	setDebug(null);
	loadRecruitments(1);
}

async function fetchJobPreference() {
	if (jobPreferenceCache) {
		return jobPreferenceCache;
	}

	const response = await fetch("/api/members/me/job-preference", {
		headers: {
			Accept: "application/json"
		}
	});

	if (!response.ok) {
		const problem = await response.json().catch(() => null);
		throw new Error(problem?.detail || "맞춤공고 설정을 불러오지 못했습니다.");
	}

	jobPreferenceCache = await response.json();
	return jobPreferenceCache;
}

async function getResponseErrorMessage(response, fallbackMessage) {
	const problem = await response.json().catch(() => null);
	return problem?.detail || fallbackMessage;
}

async function applyJobPreference(preference) {
	searchKeyword.value = preference.searchKeyword || "";
	keywordSearchControl.classList.toggle("is-expanded", Boolean(searchKeyword.value.trim()));
	updateKeywordSearchState();
	clearListHeaderFilters();
	await ensureCompanyFilterOptions(preference.companies || []);
	setCheckedFilterValues(listCompanyFilter, preference.companies);
	setCheckedFilterValues(listStatusFilter, preference.recruitmentStatuses);
	setCheckedFilterValues(listRegionFilter, preference.regions);
	setCheckedFilterValues(listCategoryFilter, preference.categories);
	setCheckedFilterValues(listHireTypeFilter, preference.hireTypes);
	setCheckedFilterValues(listNcsFilter, preference.ncsCodes);
	updateListFilterIndicators();
}

function clearJobPreferenceApplication() {
	searchKeyword.value = "";
	keywordSearchControl.classList.remove("is-expanded");
	updateKeywordSearchState();
	clearListHeaderFilters();
	updateListFilterIndicators();
}

form.addEventListener("submit", (event) => {
	event.preventDefault();
	loadRecruitments(1);
});

function updateKeywordSearchState() {
	keywordSearchControl.classList.toggle("has-value", Boolean(searchKeyword.value.trim()));
}

searchKeyword.addEventListener("input", updateKeywordSearchState);

searchKeyword.addEventListener("keydown", (event) => {
	if (event.key !== "Enter") {
		return;
	}

	event.preventDefault();
	loadRecruitments(1);
});

keywordSearchButton.addEventListener("click", () => {
	if (!keywordSearchControl.classList.contains("is-expanded")) {
		keywordSearchControl.classList.add("is-expanded");
		searchKeyword.focus();
		return;
	}

	loadRecruitments(1);
});

syncFailureDetailButton.addEventListener("click", () => {
	syncFailurePanel.hidden = !syncFailurePanel.hidden;
});

clearKeywordButton.addEventListener("click", () => {
	isClearingKeyword = true;
	searchKeyword.value = "";
	updateKeywordSearchState();
	searchKeyword.focus();
	window.setTimeout(() => {
		isClearingKeyword = false;
	}, 0);
});

keywordSearchControl.addEventListener("focusout", () => {
	window.setTimeout(() => {
		if (isClearingKeyword || keywordSearchControl.contains(document.activeElement) || searchKeyword.value.trim()) {
			return;
		}

		keywordSearchControl.classList.remove("is-expanded");
	}, 0);
});

[listStatusFilter, listCompanyFilter, listCategoryFilter, listHireTypeFilter, listNcsFilter, listRegionFilter].forEach((filter) => {
	filter.addEventListener("change", () => {
		updateListFilterIndicators();
		loadRecruitments(1);
	});
});

listPeriodSortFilter.addEventListener("change", () => {
	updateListFilterIndicators();
	loadRecruitments(1);
});

if (jobPreferenceToggle) {
	jobPreferenceToggle.addEventListener("change", async () => {
		if (favoriteToggle?.checked) {
			favoriteToggle.checked = false;
		}
		jobPreferenceToggle.disabled = true;
		setStatus("");

		try {
			if (jobPreferenceToggle.checked) {
				const preference = await fetchJobPreference();
				await applyJobPreference(preference);
				if (!hasJobPreferenceValues(preference)) {
					setStatus("저장된 맞춤공고 조건이 없습니다. 마이페이지에서 조건을 저장해주세요.", "success");
				}
				loadRecruitments(1);
				return;
			}

			clearJobPreferenceApplication();
			loadRecruitments(1);
		} catch (error) {
			jobPreferenceToggle.checked = false;
			setStatus(error.message);
		} finally {
			jobPreferenceToggle.disabled = false;
		}
	});
}

if (favoriteToggle) {
	favoriteToggle.addEventListener("change", async () => {
		favoriteToggle.disabled = true;
		setStatus("");

		try {
			if (favoriteToggle.checked) {
				if (jobPreferenceToggle) {
					jobPreferenceToggle.checked = false;
				}
				clearJobPreferenceApplication();
				await renderFavoriteRecruitments();
				return;
			}

			currentPage = 1;
			await loadRecruitments(1);
		} catch (error) {
			favoriteToggle.checked = false;
			setStatus(error.message);
			setDebug({ message: error.message });
		} finally {
			favoriteToggle.disabled = false;
		}
	});
}

async function loadFavoriteRecruitments() {
	if (!favoriteToggle) {
		return [];
	}

	const response = await fetch(FAVORITE_API_PATH, {
		headers: {
			Accept: "application/json"
		}
	});
	if (!response.ok) {
		throw new Error(await getResponseErrorMessage(response, "관심공고 목록을 불러오지 못했습니다."));
	}

	const favorites = await response.json();
	favoriteRecruitmentIds.clear();
	favorites.forEach((favorite) => {
		if (favorite.sourceRecruitmentId) {
			favoriteRecruitmentIds.add(String(favorite.sourceRecruitmentId));
		}
	});
	resultList.querySelectorAll(".recruitment-card-shell").forEach((card) => {
		card.classList.toggle("is-favorite", favoriteRecruitmentIds.has(card.dataset.recruitmentId));
	});
	return favorites;
}

async function renderFavoriteRecruitments() {
	setLoading(true);
	setStatus("");
	setDebug(null);

	try {
		const favorites = await loadFavoriteRecruitments();
		const favoriteItems = favorites.map(normalizeFavoriteRecruitment);
		currentItems = favoriteItems;
		currentSummaryContext = { keyword: "", totalCount: favoriteItems.length };
		updateResultCountSummary(latestOverallTotalCount, favoriteItems.length);
		renderItems(favoriteItems, "관심공고 목록입니다.", {
			emptyTitle: "저장된 관심공고가 없습니다.",
			emptyDescription: "목록에서 책갈피 버튼을 눌러 관심공고를 추가할 수 있습니다."
		});
		pagination.hidden = true;
		pagination.innerHTML = "";
	} finally {
		setLoading(false);
	}
}

async function createFavoriteRecruitment(item, card) {
	if (!favoriteToggle) {
		window.alert("로그인 후 관심공고를 설정할 수 있습니다.");
		return;
	}

	const request = buildFavoriteRequest(item);
	if (!request.sourceRecruitmentId) {
		window.alert("공고 식별 정보가 없어 관심공고로 설정할 수 없습니다.");
		return;
	}

	if (favoriteRecruitmentIds.has(request.sourceRecruitmentId)) {
		window.alert("이미 관심공고로 설정된 공고입니다.");
		return;
	}

	if (!window.confirm("이 공고를 관심공고로 설정할까요?")) {
		return;
	}

	try {
		const response = await fetch(FAVORITE_API_PATH, {
			method: "POST",
			headers: {
				Accept: "application/json",
				"Content-Type": "application/json"
			},
			body: JSON.stringify(request)
		});
		const payload = await response.json().catch(() => null);
		if (!response.ok) {
			throw new Error(payload?.detail || "관심공고 설정 중 오류가 발생했습니다.");
		}

		favoriteRecruitmentIds.add(request.sourceRecruitmentId);
		card.classList.add("is-favorite");
		const button = card.querySelector("[data-favorite-action]");
		if (button) {
			button.setAttribute("aria-label", "관심공고 해제");
			button.setAttribute("title", "관심공고 해제");
		}
		window.alert(payload?.created === false ? "이미 관심공고로 설정된 공고입니다." : "관심공고로 설정되었습니다.");
	} catch (error) {
		window.alert(error.message);
	}
}

async function deleteFavoriteRecruitment(item, card) {
	if (!favoriteToggle) {
		window.alert("로그인 후 관심공고를 해제할 수 있습니다.");
		return;
	}

	const sourceRecruitmentId = getRecruitmentFavoriteId(item);
	if (!sourceRecruitmentId) {
		window.alert("공고 식별 정보가 없어 관심공고를 해제할 수 없습니다.");
		return;
	}

	if (!window.confirm("이 공고를 관심공고에서 해제할까요?")) {
		return;
	}

	try {
		const response = await fetch(
			`${FAVORITE_API_PATH}/${encodeURIComponent(sourceRecruitmentId)}?source=ALIO`,
			{
				method: "DELETE",
				headers: {
					Accept: "application/json"
				}
			}
		);
		if (!response.ok) {
			const payload = await response.json().catch(() => null);
			throw new Error(payload?.detail || "관심공고 해제 중 오류가 발생했습니다.");
		}

		favoriteRecruitmentIds.delete(sourceRecruitmentId);
		card.classList.remove("is-favorite");
		const button = card.querySelector("[data-favorite-action]");
		if (button) {
			button.setAttribute("aria-label", "관심공고 설정");
			button.setAttribute("title", "관심공고 설정");
		}
		if (isFavoriteListActive()) {
			currentItems = currentItems.filter((favorite) => getRecruitmentFavoriteId(favorite) !== sourceRecruitmentId);
			renderItems(currentItems, "관심공고 목록입니다.", {
				emptyTitle: "저장된 관심공고가 없습니다.",
				emptyDescription: "목록에서 책갈피 버튼을 눌러 관심공고를 추가할 수 있습니다."
			});
			updateResultCountSummary(latestOverallTotalCount, currentItems.length);
		}
		window.alert("관심공고에서 해제되었습니다.");
	} catch (error) {
		window.alert(error.message);
	}
}

resultList.addEventListener("click", (event) => {
	const button = event.target.closest("[data-favorite-action]");
	if (!button) {
		const cardLink = event.target.closest("a.recruitment-card");
		const card = cardLink?.closest(".recruitment-card-shell");
		if (card) {
			card.classList.add("is-favorite-action-suppressed");
			cardLink.blur();
		}
		return;
	}
	button.blur();

	const card = button.closest(".recruitment-card-shell");
	const recruitmentId = card?.dataset.recruitmentId;
	const item = currentItems.find((candidate) => getRecruitmentFavoriteId(candidate) === recruitmentId);
	if (!card || !item) {
		window.alert("공고 정보를 찾을 수 없습니다.");
		return;
	}

	if (favoriteRecruitmentIds.has(recruitmentId)) {
		deleteFavoriteRecruitment(item, card);
		return;
	}

	createFavoriteRecruitment(item, card);
});

resultList.addEventListener("pointerover", (event) => {
	const card = event.target.closest(".recruitment-card-shell");
	if (card && (!event.relatedTarget || !card.contains(event.relatedTarget))) {
		card.classList.remove("is-favorite-action-suppressed");
	}
});

resultList.addEventListener("focusin", (event) => {
	event.target.closest(".recruitment-card-shell")?.classList.remove("is-favorite-action-suppressed");
});

function clearListHeaderFilters() {
	[listStatusFilter, listCompanyFilter, listCategoryFilter, listHireTypeFilter, listNcsFilter, listRegionFilter].forEach((filter) => {
		filter.querySelectorAll("input:checked").forEach((input) => {
			input.checked = false;
		});
	});
	updateListFilterIndicators();
}

function updateListFilterIndicators() {
	document.querySelectorAll("[data-filter-trigger]").forEach((trigger) => {
		const menu = document.querySelector(`#${trigger.getAttribute("aria-controls")}`);
		if (menu?.dataset.sortMenu !== undefined) {
			return;
		}
		const checkedCount = getCheckedFilterValues(menu).length;
		trigger.classList.toggle("is-filtered", checkedCount > 0);
		trigger.dataset.filterCount = checkedCount > 0 ? String(checkedCount) : "";
	});
	updateListSortTrigger();
}

document.querySelectorAll("[data-filter-trigger]").forEach((trigger) => {
	trigger.addEventListener("click", () => {
		const menu = document.querySelector(`#${trigger.getAttribute("aria-controls")}`);
		const willOpen = menu.hidden;
		closeListFilterMenus();
		menu.hidden = !willOpen;
		trigger.setAttribute("aria-expanded", String(willOpen));
	});
});

document.addEventListener("click", (event) => {
	if (!event.target.closest(".list-filter-cell, .list-filter-status")) {
		closeListFilterMenus();
	}
});

function closeListFilterMenus() {
	document.querySelectorAll("[data-filter-menu]").forEach((menu) => {
		menu.hidden = true;
	});
	document.querySelectorAll("[data-filter-trigger]").forEach((trigger) => {
		trigger.setAttribute("aria-expanded", "false");
	});
}

pagination.addEventListener("click", (event) => {
	const button = event.target.closest("button[data-page]");
	if (!button || button.disabled) {
		return;
	}

	const page = Number(button.dataset.page);
	if (!Number.isInteger(page) || page < 1 || page === currentPage) {
		return;
	}

	loadRecruitments(page);
});

initializeSelectOptions();
initializeListFilterOptions();
updateListFilterIndicators();
updateKeywordSearchState();
renderRefreshButtonIdle();
connectSyncEvents();
dataRefreshButton?.addEventListener("click", startRecruitmentSynchronization);
if (favoriteToggle) {
	loadFavoriteRecruitments().catch((error) => {
		setDebug({ message: error.message });
	});
}
loadRecruitments(1);
