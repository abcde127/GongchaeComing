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
const togglePassword = document.querySelector("#togglePassword");
const togglePasswordConfirm = document.querySelector("#togglePasswordConfirm");

function setMessage(message, type = "error") {
	signupMessage.textContent = message;
	signupMessage.dataset.type = type;
	signupMessage.hidden = !message;
}

function clearErrors() {
	emailError.textContent = "";
	nicknameError.textContent = "";
	passwordError.textContent = "";
	passwordConfirmError.textContent = "";
	setMessage("");
}

function validateSignupForm() {
	let valid = true;
	const email = emailInput.value.trim();
	const nickname = nicknameInput.value.trim();
	const password = passwordInput.value;
	const passwordConfirm = passwordConfirmInput.value;

	clearErrors();

	if (!email) {
		emailError.textContent = "이메일을 입력해주세요.";
		valid = false;
	} else if (!emailInput.validity.valid) {
		emailError.textContent = "올바른 이메일 형식으로 입력해주세요.";
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

function togglePasswordVisibility(input, button, visibleLabel, hiddenLabel) {
	const isPassword = input.type === "password";

	input.type = isPassword ? "text" : "password";
	button.textContent = isPassword ? "숨김" : "보기";
	button.setAttribute("aria-label", isPassword ? hiddenLabel : visibleLabel);
	input.focus();
}

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

		setMessage("회원가입이 완료되었습니다. 로그인 화면으로 이동합니다.", "success");
		window.setTimeout(() => {
			window.location.href = "/login";
		}, 700);
	} catch (error) {
		setMessage("서버와 연결할 수 없습니다. 잠시 후 다시 시도해주세요.");
	} finally {
		signupButton.disabled = false;
		signupButton.textContent = "회원가입";
	}
});
