const form = document.querySelector("#loginForm");
const emailInput = document.querySelector("#email");
const passwordInput = document.querySelector("#password");
const emailError = document.querySelector("#emailError");
const passwordError = document.querySelector("#passwordError");
const loginMessage = document.querySelector("#loginMessage");
const loginButton = document.querySelector("#loginButton");
const togglePassword = document.querySelector("#togglePassword");

function setMessage(message) {
	loginMessage.textContent = message;
	loginMessage.hidden = !message;
}

function validateLoginForm() {
	let valid = true;
	const email = emailInput.value.trim();
	const password = passwordInput.value;

	emailError.textContent = "";
	passwordError.textContent = "";
	setMessage("");

	if (!email) {
		emailError.textContent = "이메일을 입력해주세요.";
		valid = false;
	} else if (!emailInput.validity.valid) {
		emailError.textContent = "올바른 이메일 형식으로 입력해주세요.";
		valid = false;
	}

	if (!password) {
		passwordError.textContent = "비밀번호를 입력해주세요.";
		valid = false;
	} else if (password.length < 8) {
		passwordError.textContent = "비밀번호는 8자 이상이어야 합니다.";
		valid = false;
	}

	return valid;
}

togglePassword.addEventListener("click", () => {
	const isPassword = passwordInput.type === "password";

	passwordInput.type = isPassword ? "text" : "password";
	togglePassword.textContent = isPassword ? "숨김" : "보기";
	togglePassword.setAttribute("aria-label", isPassword ? "비밀번호 숨기기" : "비밀번호 표시");
	passwordInput.focus();
});

form.addEventListener("submit", (event) => {
	if (!validateLoginForm()) {
		event.preventDefault();
		return;
	}

	loginButton.disabled = true;
	loginButton.textContent = "로그인 중";
});
