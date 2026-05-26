const form = document.querySelector("#signupForm");
const emailInput = document.querySelector("#email");
const nicknameInput = document.querySelector("#nickname");
const passwordInput = document.querySelector("#password");
const passwordConfirmInput = document.querySelector("#passwordConfirm");
const emailError = document.querySelector("#emailError");
const nicknameError = document.querySelector("#nicknameError");
const passwordError = document.querySelector("#passwordError");
const passwordConfirmError = document.querySelector("#passwordConfirmError");
const signupMessage = document.querySelector("#signupMessage");
const signupButton = document.querySelector("#signupButton");
const checkEmailButton = document.querySelector("#checkEmailButton");
const togglePassword = document.querySelector("#togglePassword");
const togglePasswordConfirm = document.querySelector("#togglePasswordConfirm");
let checkedEmail = "";

function setMessage(message, type = "error") {
	signupMessage.textContent = "";
	signupMessage.hidden = true;
	delete signupMessage.dataset.type;
	if (message) {
		showToast(message, type);
	}
}

function showToast(message, type = "error") {
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

function setEmailFeedback(message, type = "error") {
	emailError.textContent = message;
	emailError.dataset.type = type;
}

function clearErrors() {
	setEmailFeedback("");
	nicknameError.textContent = "";
	passwordError.textContent = "";
	passwordConfirmError.textContent = "";
	setMessage("");
}

function validateEmail() {
	const email = emailInput.value.trim();
	setEmailFeedback("");
	setMessage("");

	if (!email) {
		setEmailFeedback("이메일을 입력해주세요.");
		return false;
	} else if (!emailInput.validity.valid) {
		setEmailFeedback("올바른 이메일 형식으로 입력해주세요.");
		return false;
	}

	return true;
}

function validateSignupForm() {
	let valid = true;
	const email = emailInput.value.trim();
	const nickname = nicknameInput.value.trim();
	const password = passwordInput.value;
	const passwordConfirm = passwordConfirmInput.value;

	clearErrors();

	if (!validateEmail()) {
		valid = false;
	} else if (checkedEmail !== email) {
		setEmailFeedback("이메일 중복 확인을 먼저 완료해주세요.");
		valid = false;
	}

	if (!nickname) {
		nicknameError.textContent = "닉네임을 입력해주세요.";
		valid = false;
	} else if (nickname.length < 2 || nickname.length > 50) {
		nicknameError.textContent = "닉네임은 2자 이상 50자 이하로 입력해주세요.";
		valid = false;
	}

	if (!password) {
		passwordError.textContent = "비밀번호를 입력해주세요.";
		valid = false;
	} else if (password.length < 8 || password.length > 64) {
		passwordError.textContent = "비밀번호는 8자 이상 64자 이하로 입력해주세요.";
		valid = false;
	} else if (!/[A-Za-z]/.test(password) || !/\d/.test(password)) {
		passwordError.textContent = "비밀번호에는 영문과 숫자가 각각 1개 이상 필요합니다.";
		valid = false;
	}

	if (!passwordConfirm) {
		passwordConfirmError.textContent = "비밀번호 확인을 입력해주세요.";
		valid = false;
	} else if (password !== passwordConfirm) {
		passwordConfirmError.textContent = "비밀번호가 서로 일치하지 않습니다.";
		valid = false;
	}

	return valid;
}

function translateServerMessage(detail) {
	if (detail === "email already exists") {
		return "이미 가입된 이메일입니다.";
	}

	if (detail === "nickname already exists") {
		return "이미 사용 중인 닉네임입니다.";
	}

	return "회원가입에 실패했습니다. 입력 내용을 다시 확인해주세요.";
}

async function checkEmailAvailability() {
	if (!validateEmail()) {
		return;
	}

	const email = emailInput.value.trim();
	checkEmailButton.disabled = true;
	checkEmailButton.textContent = "확인 중";

	try {
		const response = await fetch(`/api/members/email-availability?email=${encodeURIComponent(email)}`);

		if (!response.ok) {
			const error = await response.json().catch(() => ({}));
			setEmailFeedback(translateServerMessage(error.detail));
			checkedEmail = "";
			return;
		}

		const result = await response.json();

		if (result.available) {
			checkedEmail = email;
			setEmailFeedback("사용 가능한 이메일입니다.", "success");
			checkEmailButton.disabled = true;
			return;
		}

		checkedEmail = "";
		setEmailFeedback("이미 가입된 이메일입니다.");
	} catch (error) {
		checkedEmail = "";
		setMessage("서버와 연결할 수 없습니다. 잠시 후 다시 시도해주세요.");
	} finally {
		checkEmailButton.disabled = checkedEmail === email;
		checkEmailButton.textContent = "중복 확인";
	}
}

function togglePasswordVisibility(input, button, visibleLabel, hiddenLabel) {
	const isPassword = input.type === "password";
	const hiddenIcon = button.querySelector('[data-visible="false"]');
	const visibleIcon = button.querySelector('[data-visible="true"]');

	input.type = isPassword ? "text" : "password";
	button.setAttribute("aria-label", isPassword ? hiddenLabel : visibleLabel);
	button.setAttribute("aria-pressed", String(isPassword));
	hiddenIcon.hidden = isPassword;
	visibleIcon.hidden = !isPassword;
	input.focus();
}

emailInput.addEventListener("input", () => {
	checkedEmail = "";
	checkEmailButton.disabled = false;
	setEmailFeedback("");
	setMessage("");
});

checkEmailButton.addEventListener("click", checkEmailAvailability);

togglePassword.addEventListener("click", () => {
	togglePasswordVisibility(passwordInput, togglePassword, "비밀번호 표시", "비밀번호 숨기기");
});

togglePasswordConfirm.addEventListener("click", () => {
	togglePasswordVisibility(
		passwordConfirmInput,
		togglePasswordConfirm,
		"비밀번호 확인 표시",
		"비밀번호 확인 숨기기"
	);
});

form.addEventListener("submit", async (event) => {
	event.preventDefault();

	if (!validateSignupForm()) {
		return;
	}

	signupButton.disabled = true;
	signupButton.textContent = "가입 중";
	signupButton.classList.add("is-loading");
	let signupCompleted = false;

	try {
		const response = await fetch("/api/members/signup", {
			method: "POST",
			headers: {
				"Content-Type": "application/json"
			},
			body: JSON.stringify({
				email: emailInput.value.trim(),
				nickname: nicknameInput.value.trim(),
				password: passwordInput.value
			})
		});

		if (!response.ok) {
			const error = await response.json().catch(() => ({}));
			setMessage(translateServerMessage(error.detail));
			return;
		}

		const signupResult = await response.json();
		sessionStorage.setItem("signupResult", JSON.stringify(signupResult));
		signupCompleted = true;
		window.location.href = "/signup-complete";
	} catch (error) {
		setMessage("서버와 연결할 수 없습니다. 잠시 후 다시 시도해주세요.");
	} finally {
		if (!signupCompleted) {
			signupButton.disabled = false;
			signupButton.textContent = "회원가입";
			signupButton.classList.remove("is-loading");
		}
	}
});
