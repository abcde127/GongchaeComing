const signupResult = sessionStorage.getItem("signupResult");
const confettiLayer = document.querySelector("#confettiLayer");

function launchConfetti() {
	const colors = ["#0f766e", "#38bdf8", "#f59e0b", "#ef4444", "#8b5cf6", "#22c55e"];
	const fragment = document.createDocumentFragment();
	const viewportWidth = window.innerWidth;
	const viewportHeight = window.innerHeight;

	for (let index = 0; index < 72; index += 1) {
		const piece = document.createElement("span");
		const size = Math.floor(Math.random() * 8) + 6;
		const duration = Math.floor(Math.random() * 900) + 2800;
		const fromLeft = index % 2 === 0;
		const direction = fromLeft ? 1 : -1;
		const distanceX = (Math.random() * 0.38 + 0.18) * viewportWidth;
		const fall = (Math.random() * 0.34 + 0.82) * viewportHeight;
		const driftY = (Math.random() * 0.08 + 0.04) * viewportHeight;
		const spin = Math.floor(Math.random() * 540) + 360;
		const keyframes = [];

		piece.className = "confetti-piece";
		piece.style.left = fromLeft ? "8%" : "92%";
		piece.style.width = `${size}px`;
		piece.style.height = `${Math.max(5, size - 2)}px`;
		piece.style.backgroundColor = colors[index % colors.length];

		for (let step = 0; step <= 20; step += 1) {
			const progress = step / 20;
			const x = direction * distanceX * progress;
			const y = (fall * progress * progress) + (driftY * progress);
			const opacity = progress < 0.08 ? progress / 0.08 : Math.max(0, 1 - ((progress - 0.82) / 0.18));

			keyframes.push({
				opacity,
				transform: `translate3d(${x}px, ${y}px, 0) rotate(${spin * progress}deg)`
			});
		}

		window.setTimeout(() => {
			piece.animate(keyframes, {
				duration,
				easing: "linear",
				fill: "forwards"
			});
		}, Math.random() * 180);

		fragment.appendChild(piece);
	}

	confettiLayer.appendChild(fragment);

	window.setTimeout(() => {
		confettiLayer.replaceChildren();
	}, 4400);
}

if (!signupResult) {
	window.location.replace("/signup");
} else {
	try {
		const member = JSON.parse(signupResult);
		const memberEmail = document.querySelector("#memberEmail");
		const memberNickname = document.querySelector("#memberNickname");

		memberEmail.textContent = member.email;
		memberNickname.textContent = member.nickname;
		launchConfetti();
	} catch (error) {
		sessionStorage.removeItem("signupResult");
		window.location.replace("/signup");
	}
}
