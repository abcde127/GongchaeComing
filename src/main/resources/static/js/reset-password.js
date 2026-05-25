const form = document.querySelector("#resetPasswordForm");
const emailInput = document.querySelector("#email");
const codeInput = document.querySelector("#code");
const newPasswordInput = document.querySelector("#newPassword");
const newPasswordConfirmInput = document.querySelector("#newPasswordConfirm");
const emailError = document.querySelector("#emailError");
const codeError = document.querySelector("#codeError");
const newPasswordError = document.querySelector("#newPasswordError");
const newPasswordConfirmError = document.querySelector("#newPasswordConfirmError");
const emailGroup = document.querySelector("#emailGroup");
const codeGroup = document.querySelector("#codeGroup");
const codeHint = document.querySelector("#codeHint");
const newPasswordGroup = document.querySelector("#newPasswordGroup");
const newPasswordConfirmGroup = document.querySelector("#newPasswordConfirmGroup");
const messageBox = document.querySelector("#resetPasswordMessage");
const requestCodeButton = document.querySelector("#requestCodeButton");
const verifyCodeButton = document.querySelector("#verifyCodeButton");
const resetPasswordButton = document.querySelector("#resetPasswordButton");
const toggleNewPassword = document.querySelector("#toggleNewPassword");

const CODE_TTL_SECONDS = 5 * 60;
const RESEND_COOLDOWN_SECONDS = 60;

let verifiedEmail = "";
let verifiedCode = "";
let codeRequestedEmail = "";
let codeTimerId = null;
let resendTimerId = null;

function setMessage(message, type = "error") {
	messageBox.textContent = message;
	messageBox.dataset.type = type;
	messageBox.hidden = !message;
}

function showToast(message) {
	const toast = document.createElement("div");
	toast.className = "toast";
	toast.setAttribute("role", "status");
	toast.setAttribute("aria-live", "polite");
	toast.textContent = message;
	document.body.append(toast);

	window.setTimeout(() => {
		toast.classList.add("is-hiding");
	}, 2400);

	window.setTimeout(() => {
		toast.remove();
	}, 2800);
}

function setLoading(button, loading, text) {
	button.disabled = loading;
	if (text) {
		button.textContent = text;
	}
}

function formatTime(totalSeconds) {
	const minutes = String(Math.floor(totalSeconds / 60)).padStart(2, "0");
	const seconds = String(totalSeconds % 60).padStart(2, "0");
	return `${minutes}:${seconds}`;
}

function stopCodeTimer() {
	if (codeTimerId) {
		window.clearInterval(codeTimerId);
		codeTimerId = null;
	}
}

function stopResendCooldown() {
	if (resendTimerId) {
		window.clearInterval(resendTimerId);
		resendTimerId = null;
	}
}

function updateVerifyCodeButtonState() {
	verifyCodeButton.disabled = !codeRequestedEmail || codeInput.value.trim().length === 0;
}

function renderCodeTimer(remainingSeconds) {
	codeHint.hidden = false;
	codeHint.innerHTML = `인증번호 유효 시간 <strong class="verification-timer" id="codeTimer">${formatTime(remainingSeconds)}</strong>`;
}

function expireVerificationCode() {
	stopCodeTimer();
	codeRequestedEmail = "";
	verifiedEmail = "";
	verifiedCode = "";
	verifyCodeButton.disabled = true;
	newPasswordGroup.hidden = true;
	newPasswordConfirmGroup.hidden = true;
	resetPasswordButton.hidden = true;
	codeHint.hidden = false;
	codeHint.textContent = "유효 시간이 만료되었습니다. 인증 번호를 다시 요청해주세요.";
}

function startCodeTimer() {
	stopCodeTimer();
	let remainingSeconds = CODE_TTL_SECONDS;
	renderCodeTimer(remainingSeconds);

	codeTimerId = window.setInterval(() => {
		remainingSeconds -= 1;
		if (remainingSeconds <= 0) {
			expireVerificationCode();
			return;
		}
		renderCodeTimer(remainingSeconds);
	}, 1000);
}

function startResendCooldown() {
	stopResendCooldown();
	let remainingSeconds = RESEND_COOLDOWN_SECONDS;
	requestCodeButton.disabled = true;
	requestCodeButton.textContent = `재발송 ${formatTime(remainingSeconds)}`;

	resendTimerId = window.setInterval(() => {
		remainingSeconds -= 1;
		if (remainingSeconds <= 0) {
			stopResendCooldown();
			requestCodeButton.disabled = false;
			requestCodeButton.textContent = "인증번호 발송";
			return;
		}
		requestCodeButton.textContent = `재발송 ${formatTime(remainingSeconds)}`;
	}, 1000);
}

function resetVerificationState() {
	stopCodeTimer();
	codeRequestedEmail = "";
	verifiedEmail = "";
	verifiedCode = "";
	codeInput.value = "";
	codeHint.hidden = true;
	verifyCodeButton.disabled = true;
	verifyCodeButton.hidden = false;
	emailGroup.hidden = false;
	codeGroup.hidden = false;
	newPasswordGroup.hidden = true;
	newPasswordConfirmGroup.hidden = true;
	resetPasswordButton.hidden = true;
	newPasswordInput.value = "";
	newPasswordConfirmInput.value = "";
}

function showPasswordFields() {
	stopCodeTimer();
	emailGroup.hidden = true;
	codeGroup.hidden = true;
	verifyCodeButton.hidden = true;
	newPasswordGroup.hidden = false;
	newPasswordConfirmGroup.hidden = false;
	resetPasswordButton.hidden = false;
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
		emailError.textContent = "아이디를 입력해주세요.";
		return false;
	}
	if (!/^[^\s@]+@[^\s@]+\.[^\s@]+$/.test(email)) {
		emailError.textContent = "올바른 이메일 형식의 아이디를 입력해주세요.";
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

	let requestSucceeded = false;
	setLoading(requestCodeButton, true, "발송 중");
	try {
		const email = emailInput.value.trim();
		const response = await fetch("/api/members/password-reset-verifications", {
			method: "POST",
			headers: { "Content-Type": "application/json" },
			body: JSON.stringify({ email })
		});

		if (!response.ok) {
			throw new Error(await parseError(response));
		}

		verifiedEmail = "";
		verifiedCode = "";
		codeInput.value = "";
		codeRequestedEmail = email;
		codeHint.hidden = false;
		updateVerifyCodeButtonState();
		newPasswordGroup.hidden = true;
		newPasswordConfirmGroup.hidden = true;
		resetPasswordButton.hidden = true;
		startCodeTimer();
		requestSucceeded = true;
		showToast("인증번호 발송 요청이 접수되었습니다. 메일함을 확인해주세요.");
	} catch (error) {
		setMessage(translateServerMessage(error.message));
	} finally {
		if (requestSucceeded) {
			startResendCooldown();
		} else {
			setLoading(requestCodeButton, false, "인증번호 발송");
		}
	}
}

async function verifyCode() {
	clearErrors();
	if (!codeRequestedEmail) {
		codeError.textContent = "인증번호 발송을 먼저 완료해주세요.";
		return;
	}
	if (!validateEmail() || !validateCode()) {
		return;
	}
	if (codeRequestedEmail !== emailInput.value.trim()) {
		codeError.textContent = "인증번호를 발송한 이메일과 일치하지 않습니다.";
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
		showPasswordFields();
		showToast("인증번호가 확인되었습니다.");
		newPasswordInput.focus();
	} catch (error) {
		verifiedEmail = "";
		verifiedCode = "";
		setMessage(translateServerMessage(error.message));
	} finally {
		setLoading(verifyCodeButton, false, "인증번호 확인");
	}
}

async function resetPassword(event) {
	event.preventDefault();
	clearErrors();

	if (resetPasswordButton.hidden) {
		await verifyCode();
		return;
	}

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
		if (error.message === "new password must be different from current password") {
			newPasswordError.textContent = "기존 비밀번호와 다른 비밀번호를 입력해주세요.";
			newPasswordInput.focus();
			return;
		}
		setMessage(translateServerMessage(error.message));
	} finally {
		setLoading(resetPasswordButton, false, "비밀번호 변경");
	}
}

function translateServerMessage(message) {
	const messages = {
		"verification code can be requested once per minute": "인증번호는 1분에 한 번만 요청할 수 있습니다.",
		"verification code is invalid or expired": "인증번호가 올바르지 않거나 만료되었습니다."
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
	resetVerificationState();
});
codeInput.addEventListener("input", () => {
	codeInput.value = codeInput.value.replace(/\D/g, "").slice(0, 6);
	verifiedEmail = "";
	verifiedCode = "";
	newPasswordGroup.hidden = true;
	newPasswordConfirmGroup.hidden = true;
	resetPasswordButton.hidden = true;
	updateVerifyCodeButtonState();
});
requestCodeButton.addEventListener("click", requestCode);
verifyCodeButton.addEventListener("click", verifyCode);
form.addEventListener("submit", resetPassword);
