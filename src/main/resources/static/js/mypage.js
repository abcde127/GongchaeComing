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
const sectionButtons = document.querySelectorAll("[data-section-target]");
const sections = document.querySelectorAll("[data-section]");
const preferenceInputClearButtons = document.querySelectorAll("[data-preference-clear]");

let currentMember = null;

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
	});
});

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
