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
const sectionButtons = document.querySelectorAll("[data-section-target]");
const sections = document.querySelectorAll("[data-section]");

let currentMember = null;

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

sectionButtons.forEach((button) => {
	button.addEventListener("click", () => {
		activateSection(button.dataset.sectionTarget);
	});
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

loadProfile().catch((error) => {
	showMessage(error.message);
});
