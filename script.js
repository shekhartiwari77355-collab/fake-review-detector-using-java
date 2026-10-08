const button = document.getElementById("analyzeBtn");
const reviewBox = document.getElementById("review");
const result = document.getElementById("result");
const label = document.getElementById("label");
const score = document.getElementById("score");
const reason = document.getElementById("reason");
const barFill = document.getElementById("barFill");

button.addEventListener("click", async () => {
    const review = reviewBox.value.trim();

    if (review === "") {
        alert("Please enter a review first.");
        return;
    }

    button.textContent = "Analyzing...";
    button.disabled = true;

    try {
        const response = await fetch("/analyze", {
            method: "POST",
            headers: {
                "Content-Type": "application/x-www-form-urlencoded"
            },
            body: "review=" + encodeURIComponent(review)
        });

        const data = await response.json();

        if (data.error) {
            alert(data.error);
            return;
        }

        result.classList.remove("hidden");
        label.textContent = data.label;
        score.textContent = data.score + "%";
        reason.textContent = data.reason;
        barFill.style.width = data.score + "%";

        if (data.score >= 50) {
            label.style.color = "#dc2626";
            barFill.style.background = "#dc2626";
        } else if (data.score >= 25) {
            label.style.color = "#d97706";
            barFill.style.background = "#d97706";
        } else {
            label.style.color = "#16a34a";
            barFill.style.background = "#16a34a";
        }

    } catch (error) {
        alert("Could not connect to the Java server.");
    } finally {
        button.textContent = "Analyze Review";
        button.disabled = false;
    }
});
