const form = document.querySelector("#resetPasswordForm");
const emailInput = document.querySelector("#email");
const codeInput = document.querySelector("#code");
const newPasswordInput = document.querySelector("#newPassword");
const newPasswordConfirmInput = document.querySelector("#newPasswordConfirm");
const emailError = document.querySelector("#emailError");
const codeError = document.querySelector("#codeError");
const newPasswordError = document.querySelector("#newPasswordError");
const newPasswordConfirmError = document.querySelector("#newPasswordConfirmError");
const messageBox = document.querySelector("#resetPasswordMessage");
const requestCodeButton = document.querySelector("#requestCodeButton");
const verifyCodeButton = document.querySelector("#verifyCodeButton");
const resetPasswordButton = document.querySelector("#resetPasswordButton");
const toggleNewPassword = document.querySelector("#toggleNewPassword");

let verifiedEmail = "";
let verifiedCode = "";

function setMessage(message, type = "error") {
	messageBox.textContent = message;
	messageBox.dataset.type = type;
	messageBox.hidden = !message;
}

function setLoading(button, loading, text) {
	button.disabled = loading;
	if (text) {
		button.textContent = text;
	}
}

function clearErrors() {
	emailError.textContent = "";
	codeError.textContent = "";
	newPasswordError.textContent = "";
	newPasswordConfirmError.textContent = "";
	setMessage("");
}

function validateEmail() {
	const email = emailInput.value.trim();
	if (!email) {
		emailError.textContent = "이메일을 입력해주세요.";
		return false;
	}
	if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
		emailError.textContent = "올바른 이메일 형식으로 입력해주세요.";
		return false;
	}
	return true;
}

function validateCode() {
	if (!/^\d{6}$/.test(codeInput.value.trim())) {
		codeError.textContent = "6자리 인증번호를 입력해주세요.";
		return false;
	}
	return true;
}

function validatePassword() {
	const password = newPasswordInput.value;
	const passwordConfirm = newPasswordConfirmInput.value;
	let valid = true;

	if (!password) {
		newPasswordError.textContent = "새 비밀번호를 입력해주세요.";
		valid = false;
	} else if (password.length < 8 || password.length > 64) {
		newPasswordError.textContent = "비밀번호는 8자 이상 64자 이하로 입력해주세요.";
		valid = false;
	} else if (!/[A-Za-z]/.test(password) || !/\d/.test(password)) {
		newPasswordError.textContent = "비밀번호에는 영문과 숫자가 각각 1개 이상 필요합니다.";
		valid = false;
	}

	if (!passwordConfirm) {
		newPasswordConfirmError.textContent = "새 비밀번호 확인을 입력해주세요.";
		valid = false;
	} else if (password !== passwordConfirm) {
		newPasswordConfirmError.textContent = "비밀번호가 서로 일치하지 않습니다.";
		valid = false;
	}

	return valid;
}

async function parseError(response) {
	try {
		const payload = await response.json();
		return payload.detail || "요청을 처리하지 못했습니다.";
	} catch {
		return "요청을 처리하지 못했습니다.";
	}
}

async function requestCode() {
	clearErrors();
	if (!validateEmail()) {
		return;
	}

	setLoading(requestCodeButton, true, "발송 중");
	try {
		const response = await fetch("/api/members/password-reset-verifications", {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify({ email: emailInput.value.trim() })
		});

		if (!response.ok) {
			throw new Error(await parseError(response));
		}

		verifiedEmail = "";
		verifiedCode = "";
		setMessage("가입 이메일이라면 인증번호가 발송됩니다.", "success");
	} catch (error) {
		setMessage(translateServerMessage(error.message));
	} finally {
		setLoading(requestCodeButton, false, "인증번호");
	}
}

async function verifyCode() {
	clearErrors();
	if (!validateEmail() || !validateCode()) {
		return;
	}

	setLoading(verifyCodeButton, true, "확인 중");
	try {
		const email = emailInput.value.trim();
		const code = codeInput.value.trim();
		const response = await fetch("/api/members/password-reset-verifications/verify", {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify({ email, code })
		});

		if (!response.ok) {
			throw new Error(await parseError(response));
		}

		verifiedEmail = email;
		verifiedCode = code;
		setMessage("인증번호가 확인되었습니다.", "success");
		newPasswordInput.focus();
	} catch (error) {
		verifiedEmail = "";
		verifiedCode = "";
		setMessage(translateServerMessage(error.message));
	} finally {
		setLoading(verifyCodeButton, false, "확인");
	}
}

async function resetPassword(event) {
	event.preventDefault();
	clearErrors();

	const email = emailInput.value.trim();
	const code = codeInput.value.trim();
	if (!validateEmail() || !validateCode() || !validatePassword()) {
		return;
	}
	if (verifiedEmail !== email || verifiedCode !== code) {
		codeError.textContent = "인증번호 확인을 먼저 완료해주세요.";
		return;
	}

	setLoading(resetPasswordButton, true, "변경 중");
	try {
		const response = await fetch("/api/members/reset-password", {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify({
				email,
				code,
				newPassword: newPasswordInput.value
			})
		});

		if (!response.ok) {
			throw new Error(await parseError(response));
		}

		setMessage("비밀번호가 변경되었습니다. 새 비밀번호로 로그인해주세요.", "success");
		window.setTimeout(() => {
			window.location.href = "/login";
		}, 1200);
	} catch (error) {
		setMessage(translateServerMessage(error.message));
	} finally {
		setLoading(resetPasswordButton, false, "비밀번호 변경");
	}
}

function translateServerMessage(message) {
	const messages = {
		"verification code can be requested once per minute": "인증번호는 1분에 한 번만 요청할 수 있습니다.",
		"verification code is invalid or expired": "인증번호가 올바르지 않거나 만료되었습니다.",
		"new password must be different from current password": "기존 비밀번호와 다른 비밀번호를 입력해주세요."
	};
	return messages[message] || message || "요청을 처리하지 못했습니다.";
}

toggleNewPassword.addEventListener("click", () => {
	const isPassword = newPasswordInput.type === "password";
	const hiddenIcon = toggleNewPassword.querySelector('[data-visible="false"]');
	const visibleIcon = toggleNewPassword.querySelector('[data-visible="true"]');

	newPasswordInput.type = isPassword ? "text" : "password";
	toggleNewPassword.setAttribute("aria-label", isPassword ? "비밀번호 숨기기" : "비밀번호 표시");
	toggleNewPassword.setAttribute("aria-pressed", String(isPassword));
	hiddenIcon.hidden = isPassword;
	visibleIcon.hidden = !isPassword;
	newPasswordInput.focus();
});

emailInput.addEventListener("input", () => {
	verifiedEmail = "";
	verifiedCode = "";
});
codeInput.addEventListener("input", () => {
	codeInput.value = codeInput.value.replace(/\D/g, "").slice(0, 6);
	verifiedEmail = "";
	verifiedCode = "";
});
requestCodeButton.addEventListener("click", requestCode);
verifyCodeButton.addEventListener("click", verifyCode);
form.addEventListener("submit", resetPassword);
