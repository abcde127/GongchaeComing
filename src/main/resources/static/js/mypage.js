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

function setNicknameSaving(isSaving) {
	saveNicknameButton.disabled = isSaving;
	editNicknameButton.disabled = isSaving;
	cancelNicknameButton.disabled = isSaving;
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

loadProfile().catch((error) => {
	showMessage(error.message);
});
