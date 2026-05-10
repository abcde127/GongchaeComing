const form = document.querySelector("#searchForm");
const searchKeyword = document.querySelector("#searchKeyword");
const keywordSearchButton = document.querySelector("#keywordSearchButton");
const clearKeywordButton = document.querySelector("#clearKeywordButton");
const keywordSearchControl = document.querySelector(".list-filter-search-control");
const loadingState = document.querySelector("#loadingState");
const emptyState = document.querySelector("#emptyState");
const listFilterRow = document.querySelector("#listFilterRow");
const listStatusFilter = document.querySelector("#listStatusFilter");
const listCompanyFilter = document.querySelector("#listCompanyFilter");
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
const PAGE_SIZE = 10;

let currentPage = 1;
let currentItems = [];
let currentSummaryContext = { keyword: "", totalCount: 0 };
let isClearingKeyword = false;
const MIN_LOADING_MS = 350;

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

function syncCompanyFilterOptions(items) {
	const selectedValues = new Set(getCheckedFilterValues(listCompanyFilter));
	const companyNames = Array.from(new Set(
		items
			.map((item) => getValue(item, "pblntInstNm", "instNm"))
			.filter(Boolean)
	)).sort((first, second) => first.localeCompare(second, "ko"));

	listCompanyFilter.innerHTML = "";
	companyNames.forEach((companyName) => {
		const option = createListFilterCheckbox(companyName, companyName);
		const checkbox = option.querySelector("input");
		checkbox.checked = selectedValues.has(companyName);
		listCompanyFilter.appendChild(option);
	});
	updateListFilterIndicators();
}

function setLoading(isLoading) {
	loadingState.hidden = !isLoading;
	resultStage.classList.toggle("is-loading", isLoading);
	keywordSearchButton.disabled = isLoading;
	clearKeywordButton.disabled = isLoading;
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
	return `<span class="status-badge status-badge-${tone}">${label}</span>`;
}

function createRecruitmentCard(item) {
	const title = getValue(item, "recrutPbancTtl");
	const institution = getValue(item, "pblntInstNm", "instNm");
	const companyDivision = getValue(item, "instClsfNm", "instClsf", "instSeNm", "instSe", "orgSeNm", "orgSe");
	const companyType = getValue(item, "instTypeNm", "instType", "instKndNm", "instKnd", "pblntInstTypeNm", "pblntInstType");
	const hireType = getHireTypeLabel(item);
	const recruitmentCategory = getRecruitmentCategoryLabel(item);
	const region = summarizeListValue(getValue(item, "workRgnNmLst", "workRgnNm", "workRegionNm"));
	const ncs = summarizeListValue(getValue(item, "ncsCdNmLst", "ncsNmLst", "ncsNm", "ncsName"));
	const rawStartDate = getValue(item, "pbancBgngYmd", "pbancRgtrYmd");
	const rawEndDate = getValue(item, "pbancEndYmd", "aplyEndYmd", "endDate");
	const startDate = formatDate(rawStartDate);
	const endDate = formatDate(rawEndDate);
	const period = `${startDate} ~ ${endDate}`;
	const status = getRecruitmentStatus(rawStartDate, rawEndDate);
	const detailUrl = getValue(item, "recrutPbancUrl", "srcUrl", "url");

	return `
		<article class="recruitment-card">
			<div class="card-top">
				${createStatusBadge(status)}
				<div class="card-main">
					<h3 class="card-title">${title || "제목 정보 없음"}</h3>
				</div>
			</div>

			<div class="meta-list">
				${createMetaRow(period, "meta-row-period")}
				${createCompanyMeta(institution, companyDivision, companyType)}
				${createMetaRow(region)}
				${createMetaRow(recruitmentCategory)}
				${createMetaRow(hireType)}
				${createMetaRow(ncs)}
			</div>

			<div class="card-actions">
				${detailUrl ? `<a class="card-link card-link-icon" href="${detailUrl}" target="_blank" rel="noopener noreferrer" aria-label="원문보기" title="원문보기">↗</a>` : `<span class="card-footnote">상세 링크 정보 없음</span>`}
			</div>
		</article>
	`;
}

function getFilteredItems(items) {
	const statusFilters = getCheckedFilterValues(listStatusFilter);
	const companyFilters = getCheckedFilterValues(listCompanyFilter);
	const categoryFilters = getCheckedFilterValues(listCategoryFilter);
	const hireTypeFilters = getCheckedFilterValues(listHireTypeFilter);
	const ncsFilters = getCheckedFilterValues(listNcsFilter);
	const regionFilters = getCheckedFilterValues(listRegionFilter);

	return items.filter((item) => {
		const rawStartDate = getValue(item, "pbancBgngYmd", "pbancRgtrYmd");
		const rawEndDate = getValue(item, "pbancEndYmd", "aplyEndYmd", "endDate");
		const status = getRecruitmentStatus(rawStartDate, rawEndDate);

		if (statusFilters.length && !statusFilters.includes(status?.tone)) {
			return false;
		}

		if (companyFilters.length && !companyFilters.includes(getValue(item, "pblntInstNm", "instNm"))) {
			return false;
		}

		if (categoryFilters.length && !categoryFilters.some((categoryFilter) => getRecruitmentCategoryCodes(item).includes(categoryFilter))) {
			return false;
		}

		if (hireTypeFilters.length && !hireTypeFilters.some((hireTypeFilter) => itemMatchesHireType(item, hireTypeFilter))) {
			return false;
		}

		if (ncsFilters.length && !ncsFilters.some((ncsFilter) => itemMatchesNcs(item, ncsFilter))) {
			return false;
		}

		return !regionFilters.length || regionFilters.some((regionFilter) => itemMatchesRegion(item, regionFilter));
	});
}

function hasListHeaderFilter() {
	return [listStatusFilter, listCompanyFilter, listCategoryFilter, listHireTypeFilter, listNcsFilter, listRegionFilter]
		.some((filter) => getCheckedFilterValues(filter).length > 0);
}

function getCheckedFilterValues(filter) {
	return Array.from(filter.querySelectorAll("input:checked"), (input) => input.value);
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

function renderItems(items, summary, options = {}) {
	const showHeader = options.showHeader ?? items.length > 0;
	listFilterRow.hidden = !showHeader;

	if (!items.length) {
		resultList.hidden = true;
		resultList.innerHTML = "";
		pagination.hidden = true;
		pagination.innerHTML = "";
		emptyState.hidden = false;

		const emptyTitle = emptyState.querySelector("h3");
		const emptyDescription = emptyState.querySelector("p");

		if (emptyTitle) {
			emptyTitle.textContent = hasListHeaderFilter()
				? "목록 필터에 맞는 채용공고가 없습니다."
				: "조건에 맞는 채용공고가 없습니다.";
		}
		if (emptyDescription) {
			emptyDescription.textContent = summary || "키워드나 제목 필터를 조정한 뒤 다시 시도해보세요.";
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

	const startPage = Math.max(1, page - 2);
	const endPage = Math.min(totalPages, startPage + 4);
	const visibleStart = Math.max(1, endPage - 4);
	const pageNumbers = [];

	for (let number = visibleStart; number <= endPage; number += 1) {
		pageNumbers.push(number);
	}

	pagination.innerHTML = `
		<button class="pagination-button" data-page="${page - 1}" ${page === 1 ? "disabled" : ""}>이전</button>
		${pageNumbers.map((number) => `
			<button class="pagination-button ${number === page ? "is-active" : ""}" data-page="${number}">
				${number}
			</button>
		`).join("")}
		<button class="pagination-button" data-page="${page + 1}" ${page === totalPages ? "disabled" : ""}>다음</button>
	`;
	pagination.hidden = false;
}

function buildQueryString(page = currentPage) {
	const formData = new FormData(form);
	const params = new URLSearchParams();

	formData.forEach((value, key) => {
		const trimmed = String(value).trim();
		if (trimmed) {
			params.append(key, trimmed);
		}
	});

	params.set("pageNo", String(page));
	params.set("numOfRows", String(PAGE_SIZE));
	return params.toString();
}

async function loadRecruitments(page = currentPage) {
	currentPage = page;
	const loadingStartedAt = Date.now();
	setLoading(true);
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
		const keyword = searchKeyword.value.trim();
		currentItems = items;
		currentSummaryContext = { keyword, totalCount };
		syncCompanyFilterOptions(items);
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
		if (elapsed < MIN_LOADING_MS) {
			await new Promise((resolve) => window.setTimeout(resolve, MIN_LOADING_MS - elapsed));
		}
		setLoading(false);
	}
}

function resetForm() {
	searchKeyword.value = "";
	keywordSearchControl.classList.remove("is-expanded");
	updateKeywordSearchState();
	currentPage = 1;
	clearListHeaderFilters();
	setStatus("");
	setDebug(null);
	loadRecruitments(1);
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
	filter.addEventListener("change", renderFilteredItems);
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
		const checkedCount = getCheckedFilterValues(menu).length;
		trigger.classList.toggle("is-filtered", checkedCount > 0);
		trigger.dataset.filterCount = checkedCount > 0 ? String(checkedCount) : "";
	});
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
	const button = event.target.closest(".pagination-button");
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
loadRecruitments();
