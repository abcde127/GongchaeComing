const signupResult = sessionStorage.getItem("signupResult");

if (!signupResult) {
	window.location.replace("/signup");
} else {
	try {
		const member = JSON.parse(signupResult);
		const memberEmail = document.querySelector("#memberEmail");
		const memberNickname = document.querySelector("#memberNickname");

		memberEmail.textContent = member.email;
		memberNickname.textContent = member.nickname;
	} catch (error) {
		sessionStorage.removeItem("signupResult");
		window.location.replace("/signup");
	}
}
