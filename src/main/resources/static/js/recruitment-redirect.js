const redirectShell = document.querySelector(".redirect-shell");
const redirectLoader = document.querySelector("#redirectLoader");
const redirectTitle = document.querySelector("#redirectTitle");
const redirectCopy = document.querySelector("#redirectCopy");
const retryRedirectButton = document.querySelector("#retryRedirectButton");

async function redirectToOriginalRecruitment() {
	const recruitmentId = redirectShell?.dataset.recruitmentId;
	if (!recruitmentId) {
		showRedirectFailure("공고 식별 정보를 찾지 못했습니다.");
		return;
	}

	retryRedirectButton.hidden = true;
	redirectLoader.hidden = false;
	redirectTitle.hidden = false;
	redirectCopy.textContent = "잠시만 기다려주세요. 원문 링크를 확인하고 있습니다.";

	try {
		const response = await fetch(`/api/recruitments/${encodeURIComponent(recruitmentId)}/redirect-url`, {
			method: "GET",
			credentials: "same-origin"
		});
		if (!response.ok) {
			throw new Error("원문 링크를 찾지 못했습니다.");
		}
		const payload = await response.json();
		if (!payload?.url) {
			throw new Error("원문 링크를 찾지 못했습니다.");
		}
		window.location.replace(payload.url);
	} catch (error) {
		showRedirectFailure(error.message);
	}
}

function showRedirectFailure(message) {
	redirectLoader.hidden = true;
	redirectTitle.hidden = true;
	redirectCopy.textContent = `${message} 잠시 후 다시 시도해주세요.`;
	retryRedirectButton.hidden = false;
}

retryRedirectButton.addEventListener("click", redirectToOriginalRecruitment);
redirectToOriginalRecruitment();
