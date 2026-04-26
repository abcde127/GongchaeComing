const form = document.querySelector("#searchForm");
const searchButton = document.querySelector("#searchButton");
const resetButton = document.querySelector("#resetButton");
const loadingState = document.querySelector("#loadingState");
const emptyState = document.querySelector("#emptyState");
const resultList = document.querySelector("#resultList");
const pagination = document.querySelector("#pagination");
const resultStage = document.querySelector("#resultStage");
const statusBanner = document.querySelector("#statusBanner");
const debugPanel = document.querySelector("#debugPanel");
const debugContent = document.querySelector("#debugContent");
const listOptionSelectors = ["#ongoingYn", "#sortBy", "#sortDirection", "#numOfRows"];

let currentPage = 1;
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
	instType: [
		{ value: "A2001", label: "공기업(시장형)" },
		{ value: "A2002", label: "공기업(준시장형)" },
		{ value: "A2003", label: "준정부기관(기금관리형)" },
		{ value: "A2004", label: "준정부기관(위탁집행형)" },
		{ value: "A2005", label: "기타공공기관" }
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
	R2030: "신입+경력",
	R2040: "외국인"
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

function setLoading(isLoading) {
	loadingState.hidden = !isLoading;
	resultStage.classList.toggle("is-loading", isLoading);
	searchButton.disabled = isLoading;
	resetButton.disabled = isLoading;
	searchButton.textContent = isLoading ? "검색 중..." : "검색하기";
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

function getDateDiffInDays(targetDate, baseDate) {
	const millisecondsPerDay = 24 * 60 * 60 * 1000;
	return Math.round((targetDate.getTime() - baseDate.getTime()) / millisecondsPerDay);
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
		const daysUntilStart = getDateDiffInDays(startDate, normalizedToday);
		return {
			label: "예정",
			dDay: daysUntilStart === 0 ? "D-Day" : `D-${daysUntilStart}`,
			tone: "scheduled"
		};
	}

	if (normalizedToday > endDate) {
		return {
			label: "마감",
			dDay: "",
			tone: "closed"
		};
	}

	const daysUntilEnd = getDateDiffInDays(endDate, normalizedToday);
	return {
		label: "진행",
		dDay: daysUntilEnd === 0 ? "D-Day" : `D-${daysUntilEnd}`,
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

function getRecruitmentCategoryLabel(item) {
	const code = getValue(item, "recrutSe");
	const name = getValue(item, "recrutSeNm");

	if (recruitmentCategoryLabels[code]) {
		return recruitmentCategoryLabels[code];
	}

	return name || code || "";
}

function createMetaRow(label, value) {
	return `
		<div class="meta-row">
			<span class="meta-label">${label}</span>
			<span class="meta-value">${value || "정보 없음"}</span>
		</div>
	`;
}

function createRecruitmentCard(item) {
	const title = getValue(item, "recrutPbancTtl");
	const institution = getValue(item, "pblntInstNm", "instNm");
	const hireType = getValue(item, "hireTypeNmLst", "hireTypeNm", "hireType");
	const recruitmentCategory = getRecruitmentCategoryLabel(item);
	const region = getValue(item, "workRgnNmLst", "workRgnNm", "workRegionNm");
	const rawStartDate = getValue(item, "pbancBgngYmd", "pbancRgtrYmd");
	const rawEndDate = getValue(item, "pbancEndYmd", "aplyEndYmd", "endDate");
	const startDate = formatDate(rawStartDate);
	const endDate = formatDate(rawEndDate);
	const period = `${startDate} ~ ${endDate}`;
	const status = getRecruitmentStatus(rawStartDate, rawEndDate);
	const detailUrl = getValue(item, "recrutPbancUrl", "srcUrl", "url");
	const sourceId = getValue(item, "recrutPblntSn", "recrutPbancSn", "recrutNo", "sourceRecruitmentId");

	return `
		<article class="recruitment-card">
			<div class="card-top">
				<div>
					<span class="badge">${hireType || "채용공고"}</span>
					<h3 class="card-title">${title || "제목 정보 없음"}</h3>
					<p class="card-org">${institution || "기관 정보 없음"}</p>
				</div>
				${status ? `
					<div class="status-badge status-${status.tone}">
						<span class="status-label">${status.label}</span>
						${status.dDay ? `<span class="status-dday">${status.dDay}</span>` : ""}
					</div>
				` : ""}
			</div>

			<div class="meta-list">
				${createMetaRow("채용 구분", recruitmentCategory)}
				${createMetaRow("근무지", region)}
				${createMetaRow("공고 기간", period)}
				${createMetaRow("공고 ID", sourceId)}
			</div>

			<div class="card-actions">
				${detailUrl ? `<a class="card-link" href="${detailUrl}" target="_blank" rel="noopener noreferrer">공고 바로가기</a>` : `<span class="card-footnote">상세 링크 정보 없음</span>`}
				<span class="card-footnote">출처: ALIO</span>
			</div>
		</article>
	`;
}

function renderItems(items, summary) {
	if (!items.length) {
		resultList.hidden = true;
		resultList.innerHTML = "";
		pagination.hidden = true;
		pagination.innerHTML = "";
		emptyState.hidden = false;

		const emptyTitle = emptyState.querySelector("h3");
		const emptyDescription = emptyState.querySelector("p");

		if (emptyTitle) {
			emptyTitle.textContent = "조건에 맞는 채용공고가 없습니다.";
		}
		if (emptyDescription) {
			emptyDescription.textContent = summary || "검색 조건을 조정한 뒤 다시 시도해보세요.";
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
		const pageSize = Number(document.querySelector("#numOfRows").value || 12);
		const keyword = document.querySelector("#searchKeyword").value.trim();
		const summary = keyword
			? `"${keyword}" 검색 결과입니다. 전체 ${totalCount}건 중 현재 ${items.length}건을 표시하고 있습니다.`
			: `필터가 적용되지 않은 기본 목록입니다. 전체 ${totalCount}건 중 현재 ${items.length}건을 표시하고 있습니다.`;

		setStatus("");
		setDebug(null);
		renderItems(items, summary);
		renderPagination(totalCount, pageSize, currentPage);
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
	document.querySelector("#searchKeyword").value = "";
	document.querySelector("#hireTypeLst").value = "";
	document.querySelector("#instType").value = "";
	document.querySelector("#workRgnLst").value = "";
	document.querySelector("#ncsCdLst").value = "";
	currentPage = 1;
	setStatus("");
	setDebug(null);
	loadRecruitments(1);
}

form.addEventListener("submit", (event) => {
	event.preventDefault();
	loadRecruitments(1);
});

resetButton.addEventListener("click", resetForm);

listOptionSelectors.forEach((selector) => {
	const element = document.querySelector(selector);
	if (!element) {
		return;
	}

	element.addEventListener("change", () => {
		loadRecruitments(1);
	});
});

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
loadRecruitments();
