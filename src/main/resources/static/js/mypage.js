const nicknameDisplay = document.querySelector("#nicknameDisplay");
const nicknameEditForm = document.querySelector("#nicknameEditForm");
const nicknameInput = document.querySelector("#nicknameInput");
const editNicknameButton = document.querySelector("#editNicknameButton");
const saveNicknameButton = document.querySelector("#saveNicknameButton");
const cancelNicknameButton = document.querySelector("#cancelNicknameButton");
const profileInitial = document.querySelector("#profileInitial");
const profileEmailText = document.querySelector("#profileEmailText");
const profileNicknameText = document.querySelector("#profileNicknameText");
const profileCreatedAtText = document.querySelector("#profileCreatedAtText");
const kakaoLinkedBanner = document.querySelector("#kakaoLinkedBanner");
const kakaoLinkedAtText = document.querySelector("#kakaoLinkedAtText");
const notificationHeadingActions = document.querySelector("#notificationHeadingActions");
const message = document.querySelector("#mypageMessage");
const passwordChangeForm = document.querySelector("#passwordChangeForm");
const currentPasswordInput = document.querySelector("#currentPassword");
const newPasswordInput = document.querySelector("#newPassword");
const newPasswordConfirmInput = document.querySelector("#newPasswordConfirm");
const currentPasswordError = document.querySelector("#currentPasswordError");
const newPasswordError = document.querySelector("#newPasswordError");
const newPasswordConfirmError = document.querySelector("#newPasswordConfirmError");
const passwordSaveButton = document.querySelector("#passwordSaveButton");
const passwordMessage = document.querySelector("#passwordMessage");
const jobPreferenceReadView = document.querySelector("#jobPreferenceReadView");
const jobPreferenceForm = document.querySelector("#jobPreferenceForm");
const preferenceSearchKeyword = document.querySelector("#preferenceSearchKeyword");
const preferenceCompanySearch = document.querySelector("#preferenceCompanySearch");
const selectedCompaniesSummary = document.querySelector("#selectedCompaniesSummary");
const jobPreferenceMessage = document.querySelector("#jobPreferenceMessage");
const jobPreferenceHeadingActions = document.querySelector("#jobPreferenceHeadingActions");
const jobPreferenceEditButton = document.querySelector("#jobPreferenceEditButton");
const jobPreferenceEditActions = document.querySelector("#jobPreferenceEditActions");
const jobPreferenceSaveButton = document.querySelector("#jobPreferenceSaveButton");
const jobPreferenceResetButton = document.querySelector("#jobPreferenceResetButton");
const favoritesToolbar = document.querySelector("#favoritesToolbar");
const favoriteSelectAll = document.querySelector("#favoriteSelectAll");
const favoriteDeleteSelectedButton = document.querySelector("#favoriteDeleteSelectedButton");
const favoritesList = document.querySelector("#favoritesList");
const notificationSettingsShell = document.querySelector(".notification-settings-shell");
const kakaoConnectButton = document.querySelector("#kakaoConnectButton");
const favoriteReminderToggle = document.querySelector("#favoriteReminderToggle");
const favoriteReminderTimeSetting = document.querySelector("#favoriteReminderTimeSetting");
const favoriteReminderTime = document.querySelector("#favoriteReminderTime");
const favoriteReminderTimeDial = document.querySelector("#favoriteReminderTimeDial");
const favoriteReminderTimePanel = document.querySelector("#favoriteReminderTimePanel");
const favoriteReminderTimeText = document.querySelector("#favoriteReminderTimeText");
const favoriteReminderTimeHourText = document.querySelector("[data-time-display='hour']");
const favoriteReminderTimeMinuteText = document.querySelector("[data-time-display='minute']");
const notificationHistoryButton = document.querySelector("#notificationHistoryButton");
const notificationHistoryPanel = document.querySelector("#notificationHistoryPanel");
const notificationHistoryRefreshButton = document.querySelector("#notificationHistoryRefreshButton");
const notificationHistoryList = document.querySelector("#notificationHistoryList");
const notificationHistoryCloseButtons = document.querySelectorAll("[data-notification-history-close]");
const sectionButtons = document.querySelectorAll("[data-section-target]");
const sections = document.querySelectorAll("[data-section]");
const preferenceInputClearButtons = document.querySelectorAll("[data-preference-clear]");

let currentMember = null;
let currentFavorites = [];
let favoriteReminderHour = "09";
let favoriteReminderMinute = "00";
let favoriteReminderLastSavedTime = null;
let favoriteReminderCloseTimer = null;
let notificationHistoriesLoaded = false;
const favoriteReminderScrollTimers = {};

const preferenceOptions = {
	companies: [],
	recruitmentStatuses: [
		{ value: "scheduled", label: "예정" },
		{ value: "active", label: "진행" },
		{ value: "closed", label: "마감" }
	],
	regions: [
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
	categories: [
		{ value: "R2010", label: "신입" },
		{ value: "R2020", label: "경력" },
		{ value: "R2040", label: "외국인" }
	],
	hireTypes: [
		{ value: "R1010", label: "정규직" },
		{ value: "R1020", label: "계약직" },
		{ value: "R1030", label: "무기계약직" },
		{ value: "R1040", label: "비정규직" },
		{ value: "R1050", label: "청년인턴" },
		{ value: "R1060", label: "청년인턴(체험형)" },
		{ value: "R1070", label: "청년인턴(채용형)" }
	],
	ncsCodes: [
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

const preferenceGroupNames = ["companies", "recruitmentStatuses", "regions", "categories", "hireTypes", "ncsCodes"];
const jobPreferenceLeaveMessage = "수정사항이 저장되지 않습니다. 이동하시겠습니까?";
let currentJobPreference = null;

function showMessage(text, type = "error") {
	message.textContent = text;
	message.dataset.type = type;
	message.hidden = false;
}

function clearMessage() {
	message.textContent = "";
	message.hidden = true;
	delete message.dataset.type;
}

function showPasswordMessage(text, type = "error") {
	passwordMessage.textContent = text;
	passwordMessage.dataset.type = type;
	passwordMessage.hidden = false;
}

function clearPasswordMessage() {
	passwordMessage.textContent = "";
	passwordMessage.hidden = true;
	delete passwordMessage.dataset.type;
}

function showJobPreferenceMessage(text, type = "error") {
	jobPreferenceMessage.textContent = text;
	jobPreferenceMessage.dataset.type = type;
	jobPreferenceMessage.hidden = false;
}

function clearJobPreferenceMessage() {
	jobPreferenceMessage.textContent = "";
	jobPreferenceMessage.hidden = true;
	delete jobPreferenceMessage.dataset.type;
}

function showFieldError(errorElement, inputElement, text) {
	errorElement.textContent = text;
	errorElement.hidden = false;
	inputElement.setAttribute("aria-invalid", "true");
}

function clearFieldError(errorElement, inputElement) {
	errorElement.textContent = "";
	errorElement.hidden = true;
	inputElement.removeAttribute("aria-invalid");
}

function clearPasswordFieldErrors() {
	clearFieldError(currentPasswordError, currentPasswordInput);
	clearFieldError(newPasswordError, newPasswordInput);
	clearFieldError(newPasswordConfirmError, newPasswordConfirmInput);
}

function formatDate(value) {
	if (!value) {
		return "-";
	}

	const date = new Date(value);
	if (Number.isNaN(date.getTime())) {
		return value;
	}

	return new Intl.DateTimeFormat("ko-KR", {
		year: "numeric",
		month: "2-digit",
		day: "2-digit"
	}).format(date);
}

function formatIsoDate(value) {
	if (!value) {
		return "-";
	}

	const date = new Date(value);
	if (Number.isNaN(date.getTime())) {
		return String(value).slice(0, 10);
	}

	const year = date.getFullYear();
	const month = String(date.getMonth() + 1).padStart(2, "0");
	const day = String(date.getDate()).padStart(2, "0");
	return `${year}.${month}.${day}`;
}

function formatDateTime(value) {
	if (!value) {
		return "-";
	}

	const date = new Date(value);
	if (Number.isNaN(date.getTime())) {
		return String(value).replace("T", " ").slice(0, 16);
	}

	return new Intl.DateTimeFormat("ko-KR", {
		year: "numeric",
		month: "2-digit",
		day: "2-digit",
		hour: "2-digit",
		minute: "2-digit",
		hour12: false
	}).format(date);
}

function formatDateGroup(value) {
	if (!value) {
		return "날짜 정보 없음";
	}

	const date = new Date(value);
	if (Number.isNaN(date.getTime())) {
		return String(value).slice(0, 10).replaceAll("-", ".");
	}

	return new Intl.DateTimeFormat("ko-KR", {
		year: "numeric",
		month: "2-digit",
		day: "2-digit",
		weekday: "short"
	}).format(date);
}

function notificationHistoryTimestamp(value) {
	const timestamp = new Date(value).getTime();
	return Number.isNaN(timestamp) ? 0 : timestamp;
}

function escapeHtml(value) {
	return String(value ?? "")
		.replaceAll("&", "&amp;")
		.replaceAll("<", "&lt;")
		.replaceAll(">", "&gt;")
		.replaceAll('"', "&quot;")
		.replaceAll("'", "&#39;");
}

function parseFavoriteDate(value) {
	if (!value) {
		return null;
	}

	const normalized = String(value).replaceAll(/[^0-9]/g, "");
	if (normalized.length < 8) {
		return null;
	}

	const year = Number(normalized.slice(0, 4));
	const month = Number(normalized.slice(4, 6)) - 1;
	const day = Number(normalized.slice(6, 8));
	const date = new Date(year, month, day);
	return Number.isNaN(date.getTime()) ? null : date;
}

function getFavoriteStatus(favorite) {
	const today = new Date();
	const normalizedToday = new Date(today.getFullYear(), today.getMonth(), today.getDate());
	const startDate = parseFavoriteDate(favorite.recruitmentStartDate);
	const endDate = parseFavoriteDate(favorite.recruitmentEndDate);

	if (startDate && normalizedToday < startDate) {
		return { label: "예정", tone: "scheduled" };
	}
	if (endDate && normalizedToday <= endDate) {
		const remainingDays = Math.ceil((endDate.getTime() - normalizedToday.getTime()) / (1000 * 60 * 60 * 24));
		const dday = remainingDays === 0 ? "D-day" : `D-${remainingDays}`;
		return { label: `진행중 ${dday}`, tone: "active" };
	}
	return { label: "마감", tone: "closed" };
}

function updateFavoriteSelectionState() {
	if (!favoritesList || !favoriteSelectAll || !favoriteDeleteSelectedButton) {
		return;
	}

	const checkboxes = Array.from(favoritesList.querySelectorAll(".favorite-checkbox"));
	const checkedCount = checkboxes.filter((checkbox) => checkbox.checked).length;
	favoriteSelectAll.checked = checkboxes.length > 0 && checkedCount === checkboxes.length;
	favoriteSelectAll.indeterminate = checkedCount > 0 && checkedCount < checkboxes.length;
	favoriteDeleteSelectedButton.disabled = checkedCount === 0;
	const deleteLabel = checkedCount > 0 ? `선택 삭제 (${checkedCount})` : "선택 삭제";
	favoriteDeleteSelectedButton.setAttribute("aria-label", deleteLabel);
	favoriteDeleteSelectedButton.setAttribute("title", deleteLabel);
}

function renderFavoriteRecruitments(favorites) {
	if (!favoritesList || !favoritesToolbar) {
		return;
	}

	currentFavorites = favorites;
	if (!favorites.length) {
		favoritesToolbar.hidden = true;
		favoritesList.innerHTML = `<div class="favorites-empty-message">설정된 관심공고가 없습니다.</div>`;
		favoritesList.hidden = false;
		updateFavoriteSelectionState();
		return;
	}

	favoritesList.innerHTML = favorites.map((favorite) => {
		const title = escapeHtml(favorite.recruitmentTitle || "제목 정보 없음");
		const institution = escapeHtml(favorite.institutionName || "기관 정보 없음");
		const source = escapeHtml(favorite.source || "ALIO");
		const sourceRecruitmentId = escapeHtml(favorite.sourceRecruitmentId || "");
		const status = getFavoriteStatus(favorite);
		return `
			<article class="favorite-card" data-source="${source}" data-source-recruitment-id="${sourceRecruitmentId}">
				<label class="favorite-card-check" aria-label="관심공고 선택">
					<input type="checkbox" class="favorite-checkbox">
				</label>
				<div class="favorite-card-main">
					<h3>${title}</h3>
					<p>${institution}</p>
				</div>
				<span class="favorite-status favorite-status-${status.tone}">${escapeHtml(status.label)}</span>
			</article>
		`;
	}).join("");
	favoritesToolbar.hidden = false;
	favoritesList.hidden = false;
	updateFavoriteSelectionState();
}

async function loadFavoriteRecruitments() {
	const response = await fetch("/api/members/me/favorite-recruitments", {
		headers: {
			Accept: "application/json"
		}
	});

	if (!response.ok) {
		const problem = await response.json().catch(() => null);
		throw new Error(problem?.detail || "관심공고를 불러오지 못했습니다.");
	}

	renderFavoriteRecruitments(await response.json());
}

function renderNotificationHistories(histories) {
	if (!notificationHistoryList) {
		return;
	}

	if (!histories.length) {
		notificationHistoryList.innerHTML = `<div class="notification-history-empty">아직 발송된 알림이 없습니다.</div>`;
		return;
	}

	const sortedHistories = [...histories].sort((first, second) => {
		return notificationHistoryTimestamp(second.sentAt) - notificationHistoryTimestamp(first.sentAt);
	});
	let currentGroup = null;
	notificationHistoryList.innerHTML = sortedHistories.map((history) => {
		const dateGroup = formatDateGroup(history.sentAt);
		const status = history.status === "SUCCESS" ? "success" : "failure";
		const reason = escapeHtml(history.failureReason || "실패 원인을 확인할 수 없습니다.");
		const failureButton = status === "failure"
			? `<button type="button" class="notification-history-info" data-failure-reason="${reason}" aria-label="알림 실패 원인 보기" title="실패 원인 보기">
				<svg viewBox="0 0 24 24" aria-hidden="true">
					<circle cx="12" cy="12" r="10"></circle>
					<path d="M12 16v-4"></path>
					<path d="M12 8h.01"></path>
				</svg>
			</button>`
			: "";
		const groupDivider = currentGroup === dateGroup
			? ""
			: `<div class="notification-history-date">${escapeHtml(dateGroup)}</div>`;
		currentGroup = dateGroup;

		return `
			${groupDivider}
			<article class="notification-history-item">
				<div>
					<strong>${escapeHtml(history.typeLabel || history.type || "-")}</strong>
					<small>${escapeHtml(formatDateTime(history.sentAt))}</small>
				</div>
				<span class="notification-history-status notification-history-status-${status}">
					${escapeHtml(history.statusLabel || history.status || "-")}
				</span>
				${failureButton}
			</article>
		`;
	}).join("");
}

async function loadNotificationHistories({ force = false } = {}) {
	if (!notificationHistoryList || (!force && notificationHistoriesLoaded)) {
		return;
	}

	notificationHistoryList.innerHTML = `<div class="notification-history-empty">이력을 불러오는 중입니다.</div>`;
	const response = await fetch("/api/members/me/notifications/histories", {
		headers: {
			Accept: "application/json"
		}
	});

	if (!response.ok) {
		const problem = await response.json().catch(() => null);
		throw new Error(problem?.detail || "알림 발송이력을 불러오지 못했습니다.");
	}

	renderNotificationHistories(await response.json());
	notificationHistoriesLoaded = true;
}

function openNotificationHistoryModal() {
	if (!notificationHistoryPanel || !notificationHistoryButton) {
		return;
	}

	notificationHistoryButton.setAttribute("aria-expanded", "true");
	notificationHistoryPanel.hidden = false;
	document.body.classList.add("is-notification-history-open");
	loadNotificationHistories().catch((error) => {
		showMessage(error.message);
	});
}

function closeNotificationHistoryModal() {
	if (!notificationHistoryPanel || !notificationHistoryButton) {
		return;
	}

	notificationHistoryButton.setAttribute("aria-expanded", "false");
	notificationHistoryPanel.hidden = true;
	document.body.classList.remove("is-notification-history-open");
}

async function deleteFavoriteRecruitment(sourceRecruitmentId, source = "ALIO") {
	const response = await fetch(
		`/api/members/me/favorite-recruitments/${encodeURIComponent(sourceRecruitmentId)}?source=${encodeURIComponent(source)}`,
		{
			method: "DELETE",
			headers: {
				Accept: "application/json"
			}
		}
	);

	if (!response.ok) {
		const problem = await response.json().catch(() => null);
		throw new Error(problem?.detail || "관심공고를 삭제하지 못했습니다.");
	}
}

async function deleteSelectedFavorites() {
	if (!favoritesList) {
		return;
	}

	const selectedCards = Array.from(favoritesList.querySelectorAll(".favorite-card"))
		.filter((card) => card.querySelector(".favorite-checkbox")?.checked);
	if (!selectedCards.length) {
		return;
	}

	if (!window.confirm(`선택한 관심공고 ${selectedCards.length}개를 삭제하시겠습니까?`)) {
		return;
	}

	favoriteDeleteSelectedButton.disabled = true;

	try {
		await Promise.all(selectedCards.map((card) => deleteFavoriteRecruitment(
			card.dataset.sourceRecruitmentId,
			card.dataset.source || "ALIO"
		)));
		showMessage("선택한 관심공고가 삭제되었습니다.", "success");
		await loadFavoriteRecruitments();
	} catch (error) {
		showMessage(error.message);
		updateFavoriteSelectionState();
	}
}

function setNicknameEditMode(isEditing) {
	nicknameDisplay.classList.toggle("is-active", !isEditing);
	nicknameEditForm.classList.toggle("is-active", isEditing);

	if (isEditing) {
		clearMessage();
		nicknameInput.value = currentMember?.nickname || "";
		nicknameInput.focus();
		nicknameInput.select();
	}
}

function updateProfileView(member) {
	currentMember = member;

	const nickname = member.nickname || "-";
	const email = member.email || "-";

	profileInitial.textContent = nickname.trim().charAt(0).toUpperCase() || "G";
	profileEmailText.textContent = email;
	profileNicknameText.textContent = nickname;
	profileCreatedAtText.textContent = formatDate(member.createdAt);
	updateNotificationSettingsState(Boolean(member.kakaoLinked));
	updateFavoriteReminderView(member);
	updateKakaoLinkedBanner(member);
}

function updateNotificationSettingsState(isKakaoLinked) {
	if (!notificationSettingsShell) {
		return;
	}

	notificationSettingsShell.dataset.kakaoLinked = String(isKakaoLinked);
	if (notificationHeadingActions) {
		notificationHeadingActions.dataset.kakaoLinked = String(isKakaoLinked);
	}
	notificationSettingsShell.querySelectorAll(".notification-toggle").forEach((button) => {
		button.disabled = !isKakaoLinked;
	});
	if (notificationHistoryButton) {
		notificationHistoryButton.disabled = !isKakaoLinked;
	}
	updateFavoriteReminderTimeSetting();
}

function updateKakaoLinkedBanner(member) {
	if (!kakaoLinkedBanner || !kakaoLinkedAtText) {
		return;
	}

	kakaoLinkedBanner.hidden = !member.kakaoLinked;
	kakaoLinkedAtText.textContent = member.kakaoLinkedAt
		? formatIsoDate(member.kakaoLinkedAt)
		: "연동일 확인 불가";
}

function updateFavoriteReminderView(member) {
	if (!favoriteReminderToggle) {
		return;
	}

	const reminderTime = member.favoriteReminderTime || "09:00";
	const [hour = "09", minute = "00"] = reminderTime.split(":");
	favoriteReminderHour = hour.padStart(2, "0");
	favoriteReminderMinute = minute.padStart(2, "0");
	favoriteReminderLastSavedTime = `${favoriteReminderHour}:${favoriteReminderMinute}`;
	favoriteReminderToggle.setAttribute("aria-pressed", String(Boolean(member.favoriteReminderEnabled)));
	updateFavoriteReminderTimeDial();
	updateFavoriteReminderTimeSetting();
}

function validateNickname() {
	const nickname = nicknameInput.value.trim();

	if (nickname.length < 2 || nickname.length > 50) {
		showMessage("닉네임은 2자 이상 50자 이하로 입력해주세요.");
		return null;
	}

	return nickname;
}

function validatePasswordForm() {
	const currentPassword = currentPasswordInput.value;
	const newPassword = newPasswordInput.value;
	const newPasswordConfirm = newPasswordConfirmInput.value;
	clearPasswordFieldErrors();

	if (!currentPassword) {
		showFieldError(currentPasswordError, currentPasswordInput, "현재 비밀번호를 입력해주세요.");
		return null;
	}

	if (newPassword.length < 8 || newPassword.length > 64) {
		showFieldError(newPasswordError, newPasswordInput, "새 비밀번호는 8자 이상 64자 이하로 입력해주세요.");
		return null;
	}

	if (!/[A-Za-z]/.test(newPassword) || !/\d/.test(newPassword)) {
		showFieldError(newPasswordError, newPasswordInput, "새 비밀번호는 영문과 숫자를 포함해야 합니다.");
		return null;
	}

	if (newPassword !== newPasswordConfirm) {
		showFieldError(newPasswordConfirmError, newPasswordConfirmInput, "새 비밀번호가 일치하지 않습니다.");
		return null;
	}

	return { currentPassword, newPassword };
}

function setNicknameSaving(isSaving) {
	saveNicknameButton.disabled = isSaving;
	editNicknameButton.disabled = isSaving;
	cancelNicknameButton.disabled = isSaving;
}

function setPasswordSaving(isSaving) {
	passwordSaveButton.disabled = isSaving;
	passwordSaveButton.classList.toggle("is-loading", isSaving);
}

function setJobPreferenceSaving(isSaving) {
	jobPreferenceSaveButton.disabled = isSaving;
	jobPreferenceResetButton.disabled = isSaving;
	jobPreferenceSaveButton.classList.toggle("is-loading", isSaving);
}

function updatePreferenceInputClearButton(input) {
	const clearButton = document.querySelector(`[data-preference-clear="${input.id}"]`);
	if (clearButton) {
		clearButton.hidden = !input.value;
	}
}

function activateSection(sectionName) {
	sections.forEach((section) => {
		const isActive = section.dataset.section === sectionName;
		section.hidden = !isActive;
		section.classList.toggle("is-active", isActive);
	});

	sectionButtons.forEach((button) => {
		const isActive = button.dataset.sectionTarget === sectionName;
		button.classList.toggle("is-active", isActive);
		if (isActive) {
			button.setAttribute("aria-current", "page");
			return;
		}
		button.removeAttribute("aria-current");
	});
}

async function loadProfile() {
	const response = await fetch("/api/members/me");

	if (!response.ok) {
		throw new Error("회원 정보를 불러오지 못했습니다.");
	}

	const member = await response.json();
	updateProfileView(member);
}

function createPreferenceOption(groupName, option) {
	const label = document.createElement("label");
	label.className = "preference-option";
	label.dataset.preferenceLabel = normalizeCompanySearchText(option.label);

	const input = document.createElement("input");
	input.type = "checkbox";
	input.name = groupName;
	input.value = option.value;

	const text = document.createElement("span");
	text.textContent = option.label;

	label.append(input, text);
	return label;
}

function normalizeCompanySearchText(value) {
	return value
		.toLowerCase()
		.replace(/\s+/g, "")
		.replace(/[()（）주㈜.,·-]/g, "");
}

function updateCompanyOptionVisibility() {
	const keyword = normalizeCompanySearchText(preferenceCompanySearch.value.trim());
	const companyOptions = jobPreferenceForm.querySelector('[data-preference-options="companies"]');

	jobPreferenceForm.querySelectorAll('input[name="companies"]').forEach((input) => {
		const option = input.closest(".preference-option");
		const isVisible = !keyword || option.dataset.preferenceLabel.includes(keyword);
		option.classList.toggle("is-filtered-out", !isVisible);
	});

	if (companyOptions) {
		companyOptions.scrollTop = 0;
	}
}

function updateSelectedCompaniesSummary() {
	const selectedInputs = Array.from(jobPreferenceForm.querySelectorAll('input[name="companies"]:checked'));

	selectedCompaniesSummary.innerHTML = "";
	if (!selectedInputs.length) {
		const emptyText = document.createElement("span");
		emptyText.className = "preference-selected-empty";
		emptyText.textContent = "선택된 기업명이 없습니다.";
		selectedCompaniesSummary.appendChild(emptyText);
		return;
	}

	selectedInputs.forEach((input) => {
		const optionText = input.closest(".preference-option").querySelector("span").textContent;
		const chip = document.createElement("button");
		chip.type = "button";
		chip.className = "preference-selected-chip";
		chip.dataset.companyValue = input.value;
		chip.textContent = optionText;
		selectedCompaniesSummary.appendChild(chip);
	});
}

function updatePreferenceSelectionCounts() {
	preferenceGroupNames.forEach((groupName) => {
		const container = jobPreferenceForm.querySelector(`[data-preference-options="${groupName}"]`);
		const heading = container?.closest(".preference-card")?.querySelector(".preference-card-heading strong");
		if (!heading) {
			return;
		}

		let count = heading.querySelector(".preference-selection-count");
		if (!count) {
			count = document.createElement("span");
			count.className = "preference-selection-count";
			heading.appendChild(count);
		}
		count.textContent = `(${getCheckedValues(groupName).length})`;
	});
}

function optionLabelByValue(groupName, value) {
	return preferenceOptions[groupName]?.find((option) => option.value === value)?.label || value;
}

function renderReadOnlyValues(container, values, groupName) {
	container.innerHTML = "";
	container.closest(".preference-read-card").classList.toggle("is-empty", !values?.length);
	if (!values?.length) {
		const empty = document.createElement("span");
		empty.className = "preference-read-empty";
		empty.textContent = "설정 없음";
		container.appendChild(empty);
		return;
	}

	values.forEach((value) => {
		const chip = document.createElement("span");
		chip.className = "preference-read-chip";
		chip.textContent = optionLabelByValue(groupName, value);
		container.appendChild(chip);
	});
}

function renderJobPreferenceReadView(preference) {
	const searchKeywordContainer = jobPreferenceReadView.querySelector('[data-preference-read="searchKeyword"]');
	searchKeywordContainer.closest(".preference-read-card").classList.toggle("is-empty", !preference.searchKeyword);
	searchKeywordContainer.innerHTML = "";
	if (preference.searchKeyword) {
		searchKeywordContainer.textContent = preference.searchKeyword;
	} else {
		const empty = document.createElement("span");
		empty.className = "preference-read-empty";
		empty.textContent = "설정 없음";
		searchKeywordContainer.appendChild(empty);
	}
	preferenceGroupNames.forEach((groupName) => {
		renderReadOnlyValues(
			jobPreferenceReadView.querySelector(`[data-preference-read="${groupName}"]`),
			preference[groupName],
			groupName
		);
	});
}

function setJobPreferenceEditMode(isEditing) {
	jobPreferenceReadView.classList.toggle("is-active", !isEditing);
	jobPreferenceForm.classList.toggle("is-active", isEditing);
	jobPreferenceHeadingActions.classList.toggle("is-editing", isEditing);
	if (isEditing) {
		clearJobPreferenceMessage();
		preferenceSearchKeyword.focus();
	}
}

function isJobPreferenceEditing() {
	return jobPreferenceForm.classList.contains("is-active");
}

function discardJobPreferenceEdits() {
	if (currentJobPreference) {
		applyJobPreference(currentJobPreference);
	}
	setJobPreferenceEditMode(false);
}

function confirmDiscardJobPreferenceEdits() {
	if (!isJobPreferenceEditing()) {
		return true;
	}

	if (!window.confirm(jobPreferenceLeaveMessage)) {
		return false;
	}

	discardJobPreferenceEdits();
	return true;
}

async function loadJobPreferenceCompanyOptions() {
	const response = await fetch("/api/members/me/job-preference/companies", {
		headers: {
			Accept: "application/json"
		}
	});

	if (!response.ok) {
		throw new Error("기업 목록을 불러오지 못했습니다.");
	}

	preferenceOptions.companies = (await response.json()).map((company) => ({
		value: company.detailCode,
		label: company.detailName
	}));
}

function renderJobPreferenceOptions() {
	Object.entries(preferenceOptions).forEach(([groupName, options]) => {
		const container = document.querySelector(`[data-preference-options="${groupName}"]`);
		if (!container) {
			return;
		}

		container.innerHTML = "";
		options.forEach((option) => {
			container.appendChild(createPreferenceOption(groupName, option));
		});
	});
	updateCompanyOptionVisibility();
	updateSelectedCompaniesSummary();
	updatePreferenceSelectionCounts();
}

async function initializeJobPreferenceOptions() {
	await loadJobPreferenceCompanyOptions();
	renderJobPreferenceOptions();
}

function getCheckedValues(groupName) {
	return Array.from(
		jobPreferenceForm.querySelectorAll(`input[name="${groupName}"]:checked`),
		(input) => input.value
	);
}

function setCheckedValues(groupName, values = []) {
	const selectedValues = new Set(values);
	jobPreferenceForm.querySelectorAll(`input[name="${groupName}"]`).forEach((input) => {
		input.checked = selectedValues.has(input.value);
	});
	if (groupName === "companies") {
		updateSelectedCompaniesSummary();
	}
	updatePreferenceSelectionCounts();
}

function resetJobPreferenceForm() {
	preferenceSearchKeyword.value = "";
	preferenceCompanySearch.value = "";
	updatePreferenceInputClearButton(preferenceSearchKeyword);
	updatePreferenceInputClearButton(preferenceCompanySearch);
	["companies", "recruitmentStatuses", "regions", "categories", "hireTypes", "ncsCodes"].forEach((groupName) => {
		setCheckedValues(groupName);
	});
	updateCompanyOptionVisibility();
}

function applyJobPreference(preference) {
	currentJobPreference = preference;
	preferenceSearchKeyword.value = preference.searchKeyword || "";
	updatePreferenceInputClearButton(preferenceSearchKeyword);
	updatePreferenceInputClearButton(preferenceCompanySearch);
	setCheckedValues("companies", preference.companies);
	setCheckedValues("recruitmentStatuses", preference.recruitmentStatuses);
	setCheckedValues("regions", preference.regions);
	setCheckedValues("categories", preference.categories);
	setCheckedValues("hireTypes", preference.hireTypes);
	setCheckedValues("ncsCodes", preference.ncsCodes);
	renderJobPreferenceReadView(preference);
}

function buildJobPreferencePayload() {
	return {
		searchKeyword: preferenceSearchKeyword.value.trim(),
		companies: getCheckedValues("companies"),
		recruitmentStatuses: getCheckedValues("recruitmentStatuses"),
		regions: getCheckedValues("regions"),
		categories: getCheckedValues("categories"),
		hireTypes: getCheckedValues("hireTypes"),
		ncsCodes: getCheckedValues("ncsCodes")
	};
}

async function loadJobPreference() {
	const response = await fetch("/api/members/me/job-preference");

	if (!response.ok) {
		throw new Error("맞춤공고 설정을 불러오지 못했습니다.");
	}

	applyJobPreference(await response.json());
}

sectionButtons.forEach((button) => {
	button.addEventListener("click", () => {
		if (button.dataset.sectionTarget !== "job-preference" && !confirmDiscardJobPreferenceEdits()) {
			return;
		}
		activateSection(button.dataset.sectionTarget);
		if (button.dataset.sectionTarget === "favorites") {
			loadFavoriteRecruitments().catch((error) => {
				showMessage(error.message);
			});
		}
	});
});

function activateInitialSection() {
	const params = new URLSearchParams(window.location.search);
	const requestedSection = params.get("section");
	const sectionExists = Array.from(sections).some((section) => section.dataset.section === requestedSection);
	const initialSection = sectionExists ? requestedSection : "profile";

	activateSection(initialSection);
	if (initialSection === "favorites") {
		loadFavoriteRecruitments().catch((error) => {
			showMessage(error.message);
		});
	}
	if (params.get("kakao") === "linked") {
		showMessage("카카오 계정 연동이 완료되었습니다.", "success");
	}
}

document.addEventListener("click", (event) => {
	const link = event.target.closest("a[href]");
	if (!link || !isJobPreferenceEditing()) {
		return;
	}

	if (!window.confirm(jobPreferenceLeaveMessage)) {
		event.preventDefault();
		return;
	}

	discardJobPreferenceEdits();
});

document.addEventListener("submit", (event) => {
	if (event.target === jobPreferenceForm || !isJobPreferenceEditing()) {
		return;
	}

	if (!window.confirm(jobPreferenceLeaveMessage)) {
		event.preventDefault();
		return;
	}

	discardJobPreferenceEdits();
});

window.addEventListener("beforeunload", (event) => {
	if (!isJobPreferenceEditing()) {
		return;
	}

	event.preventDefault();
	event.returnValue = "";
});

preferenceCompanySearch.addEventListener("input", updateCompanyOptionVisibility);

preferenceSearchKeyword.addEventListener("input", () => {
	updatePreferenceInputClearButton(preferenceSearchKeyword);
});

preferenceCompanySearch.addEventListener("input", () => {
	updatePreferenceInputClearButton(preferenceCompanySearch);
});

preferenceInputClearButtons.forEach((button) => {
	button.addEventListener("click", () => {
		const input = document.querySelector(`#${button.dataset.preferenceClear}`);
		input.value = "";
		updatePreferenceInputClearButton(input);
		if (input === preferenceCompanySearch) {
			updateCompanyOptionVisibility();
		}
		input.focus();
	});
});

preferenceCompanySearch.addEventListener("keydown", (event) => {
	if (event.key === "Enter") {
		event.preventDefault();
	}
});

jobPreferenceEditButton.addEventListener("click", () => {
	if (currentJobPreference) {
		applyJobPreference(currentJobPreference);
	}
	setJobPreferenceEditMode(true);
});

jobPreferenceForm.addEventListener("change", (event) => {
	if (event.target.matches(".preference-option input")) {
		updateSelectedCompaniesSummary();
		updatePreferenceSelectionCounts();
	}
});

selectedCompaniesSummary.addEventListener("click", (event) => {
	const chip = event.target.closest("[data-company-value]");
	if (!chip) {
		return;
	}

	const input = jobPreferenceForm.querySelector(`input[name="companies"][value="${chip.dataset.companyValue}"]`);
	if (input) {
		input.checked = false;
		updateSelectedCompaniesSummary();
		updatePreferenceSelectionCounts();
	}
});

if (favoriteSelectAll) {
	favoriteSelectAll.addEventListener("change", () => {
		if (!favoritesList) {
			return;
		}

		favoritesList.querySelectorAll(".favorite-checkbox").forEach((checkbox) => {
			checkbox.checked = favoriteSelectAll.checked;
		});
		updateFavoriteSelectionState();
	});
}

if (favoritesList) {
	favoritesList.addEventListener("click", (event) => {
		if (event.target.matches("input, button, a")) {
			return;
		}

		const card = event.target.closest(".favorite-card");
		const checkbox = card?.querySelector(".favorite-checkbox");
		if (!checkbox) {
			return;
		}

		checkbox.checked = !checkbox.checked;
		updateFavoriteSelectionState();
	});

	favoritesList.addEventListener("change", (event) => {
		if (event.target.matches(".favorite-checkbox")) {
			updateFavoriteSelectionState();
		}
	});
}

if (favoriteDeleteSelectedButton) {
	favoriteDeleteSelectedButton.addEventListener("click", deleteSelectedFavorites);
}

if (kakaoConnectButton) {
	kakaoConnectButton.addEventListener("click", () => {
		window.location.href = "/api/kakao/authorize";
	});
}

if (notificationHistoryButton) {
	notificationHistoryButton.addEventListener("click", () => {
		if (notificationHistoryButton.disabled) {
			return;
		}
		openNotificationHistoryModal();
	});
}

if (notificationHistoryRefreshButton) {
	notificationHistoryRefreshButton.addEventListener("click", () => {
		loadNotificationHistories({ force: true }).catch((error) => {
			showMessage(error.message);
		});
	});
}

if (notificationHistoryList) {
	notificationHistoryList.addEventListener("click", (event) => {
		const button = event.target.closest(".notification-history-info");
		if (!button) {
			return;
		}

		window.alert(button.dataset.failureReason || "실패 원인을 확인할 수 없습니다.");
	});
}

notificationHistoryCloseButtons.forEach((button) => {
	button.addEventListener("click", closeNotificationHistoryModal);
});

document.addEventListener("keydown", (event) => {
	if (event.key === "Escape" && notificationHistoryPanel && !notificationHistoryPanel.hidden) {
		closeNotificationHistoryModal();
	}
});

document.querySelectorAll(".notification-toggle").forEach((button) => {
	button.addEventListener("click", async () => {
		const isActive = button.getAttribute("aria-pressed") === "true";
		const nextActive = !isActive;
		button.setAttribute("aria-pressed", String(nextActive));
		if (button === favoriteReminderToggle) {
			updateFavoriteReminderTimeSetting();
			try {
				await saveFavoriteReminderSetting(nextActive, { showAlert: true });
			} catch (error) {
				button.setAttribute("aria-pressed", String(isActive));
				updateFavoriteReminderTimeSetting();
				showMessage(error.message);
			}
		}
	});
});

if (favoriteReminderTime) {
	favoriteReminderTime.addEventListener("click", toggleFavoriteReminderTimeDial);
}

if (favoriteReminderTimePanel) {
	favoriteReminderTimePanel.addEventListener("click", (event) => {
		const option = event.target.closest(".time-dial-option");
		if (!option) {
			return;
		}

		if (option.dataset.timeType === "hour") {
			favoriteReminderHour = option.dataset.timeValue;
		}
		if (option.dataset.timeType === "minute") {
			favoriteReminderMinute = option.dataset.timeValue;
		}
		updateFavoriteReminderTimeDial();
		scrollTimeOptionToCenter(option, "smooth");
	});
}

document.addEventListener("click", (event) => {
	if (!favoriteReminderTimeDial || favoriteReminderTimeDial.contains(event.target)) {
		return;
	}
	closeFavoriteReminderTimeDial({ save: true });
});

function updateFavoriteReminderTimeSetting() {
	if (!favoriteReminderToggle || !favoriteReminderTimeSetting || !favoriteReminderTime) {
		return;
	}

	const isActive = favoriteReminderToggle.getAttribute("aria-pressed") === "true";
	const isDisabled = favoriteReminderToggle.disabled;
	favoriteReminderTimeSetting.hidden = !isActive;
	favoriteReminderTime.disabled = !isActive || isDisabled;
	if (!isActive || isDisabled) {
		closeFavoriteReminderTimeDial();
	}
}

function initializeFavoriteReminderTimeDial() {
	if (!favoriteReminderTimeDial || !favoriteReminderTimePanel || !favoriteReminderTimeText) {
		return;
	}

	renderTimeWheel("hour", 24);
	renderTimeWheel("minute", 60);
	favoriteReminderTimePanel.querySelectorAll(".time-dial-window").forEach((wheel) => {
		wheel.addEventListener("scroll", () => handleTimeWheelScroll(wheel));
	});
	updateFavoriteReminderTimeDial();
}

function renderTimeWheel(type, count) {
	const wheel = favoriteReminderTimePanel.querySelector(`[data-time-wheel="${type}"]`);
	if (!wheel) {
		return;
	}

	wheel.replaceChildren();
	Array.from({ length: count }, (_, index) => String(index).padStart(2, "0")).forEach((value) => {
		const option = document.createElement("button");
		option.type = "button";
		option.className = "time-dial-option";
		option.dataset.timeValue = value;
		option.dataset.timeType = type;
		option.textContent = value;
		wheel.append(option);
	});
}

function updateFavoriteReminderTimeDial() {
	if (!favoriteReminderTimePanel || !favoriteReminderTimeText) {
		return;
	}

	if (favoriteReminderTimeHourText && favoriteReminderTimeMinuteText) {
		favoriteReminderTimeHourText.textContent = favoriteReminderHour;
		favoriteReminderTimeMinuteText.textContent = favoriteReminderMinute;
	}
	favoriteReminderTimePanel.querySelectorAll(".time-dial-option").forEach((option) => {
		const isSelected = (
			(option.dataset.timeType === "hour" && option.dataset.timeValue === favoriteReminderHour)
				|| (option.dataset.timeType === "minute" && option.dataset.timeValue === favoriteReminderMinute)
		);
		option.classList.toggle("is-selected", isSelected);
		option.setAttribute("aria-pressed", String(isSelected));
	});
}

function handleTimeWheelScroll(wheel) {
	const option = findCenteredTimeOption(wheel);
	if (!option) {
		return;
	}

	if (option.dataset.timeType === "hour" && option.dataset.timeValue !== favoriteReminderHour) {
		favoriteReminderHour = option.dataset.timeValue;
		updateFavoriteReminderTimeDial();
	}
	if (option.dataset.timeType === "minute" && option.dataset.timeValue !== favoriteReminderMinute) {
		favoriteReminderMinute = option.dataset.timeValue;
		updateFavoriteReminderTimeDial();
	}

	clearTimeout(favoriteReminderScrollTimers[option.dataset.timeType]);
	favoriteReminderScrollTimers[option.dataset.timeType] = setTimeout(() => {
		scrollTimeOptionToCenter(option, "smooth");
	}, 120);
}

function findCenteredTimeOption(wheel) {
	const wheelRect = wheel.getBoundingClientRect();
	const wheelCenter = wheelRect.top + wheelRect.height / 2;
	return Array.from(wheel.querySelectorAll(".time-dial-option"))
		.reduce((closestOption, option) => {
			const optionRect = option.getBoundingClientRect();
			const distance = Math.abs(optionRect.top + optionRect.height / 2 - wheelCenter);
			if (!closestOption || distance < closestOption.distance) {
				return { option, distance };
			}
			return closestOption;
		}, null)?.option;
}

function toggleFavoriteReminderTimeDial() {
	if (!favoriteReminderTime || !favoriteReminderTimePanel || favoriteReminderTime.disabled) {
		return;
	}

	const isOpen = favoriteReminderTime.getAttribute("aria-expanded") === "true";
	if (isOpen) {
		closeFavoriteReminderTimeDial({ save: true });
		return;
	}

	clearTimeout(favoriteReminderCloseTimer);
	favoriteReminderTimePanel.classList.remove("is-closing");
	favoriteReminderTime.setAttribute("aria-expanded", String(!isOpen));
	favoriteReminderTimePanel.hidden = false;
	scrollSelectedTimeOptionsIntoView();
}

function closeFavoriteReminderTimeDial({ save = false } = {}) {
	if (!favoriteReminderTime || !favoriteReminderTimePanel) {
		return;
	}

	const wasOpen = favoriteReminderTime.getAttribute("aria-expanded") === "true";
	if (!wasOpen) {
		return;
	}

	favoriteReminderTime.setAttribute("aria-expanded", "false");
	favoriteReminderTimePanel.classList.add("is-closing");
	clearTimeout(favoriteReminderCloseTimer);
	favoriteReminderCloseTimer = setTimeout(() => {
		favoriteReminderTimePanel.hidden = true;
		favoriteReminderTimePanel.classList.remove("is-closing");
	}, 160);
	if (save && wasOpen) {
		saveFavoriteReminderTime();
	}
}

async function saveFavoriteReminderTime() {
	if (!favoriteReminderToggle || favoriteReminderToggle.getAttribute("aria-pressed") !== "true") {
		return;
	}

	await saveFavoriteReminderSetting(true, { showAlert: true });
}

async function saveFavoriteReminderSetting(enabled, { showAlert = false } = {}) {
	const reminderTime = `${favoriteReminderHour}:${favoriteReminderMinute}`;

	const response = await fetch("/api/members/me/notifications/favorite-reminder", {
		method: "PATCH",
		headers: {
			"Content-Type": "application/json"
		},
		body: JSON.stringify({
			enabled,
			reminderTime
		})
	});

	if (!response.ok) {
		const problem = await response.json().catch(() => null);
		throw new Error(problem?.detail || "리마인드 알림 설정을 저장하지 못했습니다.");
	}

	updateProfileView(await response.json());
	favoriteReminderLastSavedTime = reminderTime;
	if (showAlert) {
		window.alert(enabled
			? `매일 ${reminderTime}분 리마인드 알림이 설정되었습니다.`
			: "리마인드 알림이 해제되었습니다.");
	}
}

function scrollSelectedTimeOptionsIntoView() {
	if (!favoriteReminderTimePanel) {
		return;
	}

	favoriteReminderTimePanel.querySelectorAll(".time-dial-option.is-selected").forEach((option) => {
		scrollTimeOptionToCenter(option);
	});
}

function scrollTimeOptionToCenter(option, behavior = "auto") {
	const wheel = option.closest(".time-dial-window");
	if (!wheel) {
		return;
	}

	const targetTop = option.offsetTop - (wheel.clientHeight - option.offsetHeight) / 2;
	wheel.scrollTo({ top: targetTop, behavior });
}

editNicknameButton.addEventListener("click", () => {
	setNicknameEditMode(true);
});

cancelNicknameButton.addEventListener("click", () => {
	setNicknameEditMode(false);
});

document.querySelectorAll("[data-password-toggle]").forEach((button) => {
	button.addEventListener("click", () => {
		const input = document.querySelector(`#${button.dataset.passwordToggle}`);
		const isPassword = input.type === "password";
		input.type = isPassword ? "text" : "password";
		button.setAttribute("aria-pressed", String(isPassword));
		button.querySelector("[data-visible='false']").hidden = isPassword;
		button.querySelector("[data-visible='true']").hidden = !isPassword;
	});
});

currentPasswordInput.addEventListener("input", () => {
	clearFieldError(currentPasswordError, currentPasswordInput);
});

newPasswordInput.addEventListener("input", () => {
	clearFieldError(newPasswordError, newPasswordInput);
	clearFieldError(newPasswordConfirmError, newPasswordConfirmInput);
});

newPasswordConfirmInput.addEventListener("input", () => {
	clearFieldError(newPasswordConfirmError, newPasswordConfirmInput);
});

nicknameEditForm.addEventListener("submit", async (event) => {
	event.preventDefault();
	clearMessage();

	const nickname = validateNickname();
	if (!nickname) {
		return;
	}

	setNicknameSaving(true);

	try {
		const response = await fetch("/api/members/me/nickname", {
			method: "PATCH",
			headers: {
				"Content-Type": "application/json"
			},
			body: JSON.stringify({ nickname })
		});

		if (!response.ok) {
			const problem = await response.json().catch(() => null);
			throw new Error(problem?.detail || "닉네임을 수정하지 못했습니다.");
		}

		const member = await response.json();
		updateProfileView(member);
		setNicknameEditMode(false);
		showMessage("닉네임이 수정되었습니다.", "success");
	} catch (error) {
		showMessage(error.message);
	} finally {
		setNicknameSaving(false);
	}
});

passwordChangeForm.addEventListener("submit", async (event) => {
	event.preventDefault();
	clearPasswordMessage();
	clearPasswordFieldErrors();

	const payload = validatePasswordForm();
	if (!payload) {
		return;
	}

	setPasswordSaving(true);

	try {
		const response = await fetch("/api/members/me/password", {
			method: "PATCH",
			headers: {
				"Content-Type": "application/json"
			},
			body: JSON.stringify(payload)
		});

		if (!response.ok) {
			const problem = await response.json().catch(() => null);
			throw new Error(problem?.detail || "비밀번호를 변경하지 못했습니다.");
		}

		currentPasswordInput.value = "";
		newPasswordInput.value = "";
		newPasswordConfirmInput.value = "";
		clearPasswordFieldErrors();
		showPasswordMessage("비밀번호가 변경되었습니다.", "success");
	} catch (error) {
		showPasswordMessage(error.message);
	} finally {
		setPasswordSaving(false);
	}
});

jobPreferenceResetButton.addEventListener("click", () => {
	if (!window.confirm("맞춤공고 설정 입력값을 초기화하시겠습니까?")) {
		return;
	}

	clearJobPreferenceMessage();
	resetJobPreferenceForm();
});

jobPreferenceForm.addEventListener("submit", async (event) => {
	event.preventDefault();
	if (event.submitter !== jobPreferenceSaveButton) {
		return;
	}

	clearJobPreferenceMessage();
	setJobPreferenceSaving(true);

	try {
		const response = await fetch("/api/members/me/job-preference", {
			method: "PATCH",
			headers: {
				"Content-Type": "application/json"
			},
			body: JSON.stringify(buildJobPreferencePayload())
		});

		if (!response.ok) {
			const problem = await response.json().catch(() => null);
			throw new Error(problem?.detail || "맞춤공고 설정을 저장하지 못했습니다.");
		}

		applyJobPreference(await response.json());
		setJobPreferenceEditMode(false);
		showJobPreferenceMessage("맞춤공고 설정이 저장되었습니다.", "success");
	} catch (error) {
		showJobPreferenceMessage(error.message);
	} finally {
		setJobPreferenceSaving(false);
	}
});

initializeFavoriteReminderTimeDial();
activateInitialSection();

loadProfile().catch((error) => {
	showMessage(error.message);
});

initializeJobPreferenceOptions()
	.then(loadJobPreference)
	.then(() => {
		setJobPreferenceEditMode(false);
	})
	.catch((error) => {
	showJobPreferenceMessage(error.message);
});

loadFavoriteRecruitments().catch((error) => {
	showMessage(error.message);
});
