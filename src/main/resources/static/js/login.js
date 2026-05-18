const form = document.querySelector("#loginForm");
const emailInput = document.querySelector("#email");
const passwordInput = document.querySelector("#password");
const emailError = document.querySelector("#emailError");
const passwordError = document.querySelector("#passwordError");
const loginMessage = document.querySelector("#loginMessage");
const loginButton = document.querySelector("#loginButton");
const togglePassword = document.querySelector("#togglePassword");

const params = new URLSearchParams(window.location.search);

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
		emailError.textContent = "아이디를 입력해주세요.";
		valid = false;
	}

	if (!password) {
		passwordError.textContent = "비밀번호를 입력해주세요.";
		valid = false;
	}

	return valid;
}

togglePassword.addEventListener("click", () => {
	const isPassword = passwordInput.type === "password";
	const hiddenIcon = togglePassword.querySelector('[data-visible="false"]');
	const visibleIcon = togglePassword.querySelector('[data-visible="true"]');

	passwordInput.type = isPassword ? "text" : "password";
	togglePassword.setAttribute("aria-label", isPassword ? "비밀번호 숨기기" : "비밀번호 표시");
	togglePassword.setAttribute("aria-pressed", String(isPassword));
	hiddenIcon.hidden = isPassword;
	visibleIcon.hidden = !isPassword;
	passwordInput.focus();
});

if (params.has("error")) {
	setMessage("아이디 또는 비밀번호가 올바르지 않습니다.");
}

if (params.has("logout")) {
	setMessage("로그아웃되었습니다.");
}

form.addEventListener("submit", (event) => {
	if (!validateLoginForm()) {
		event.preventDefault();
		return;
	}

	loginButton.disabled = true;
	loginButton.textContent = "로그인 중";
});
