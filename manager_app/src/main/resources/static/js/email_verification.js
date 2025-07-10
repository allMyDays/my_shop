
    let emailVerified = false;

    document.getElementById("profile-form").addEventListener("submit", function (e) {
    e.preventDefault();

    const form = e.target;
    const formData = new FormData(form);
    formData.append("isVerified", emailVerified);

    const isReg = form.action.includes("/registration")

    fetch(isReg?"/registration":"/my_profile", {
    method: "POST",
    body: new URLSearchParams(formData)
})
    .then(res => res.json())
    .then(data => {
    const msg = document.getElementById("message-block");
    msg.innerHTML = "";

    if (data.errors) {
    data.errors.forEach(err => {
    msg.innerHTML += `<div class="alert alert-danger">${err}</div>`;
});
    return;
}

    if (data.emailSent) {
    msg.innerHTML = `<div class="alert alert-info">На адрес <b>${formData.get("email")}</b> был отправлен код. Введите его в течение 15 минут.</div>`;
    document.getElementById("email").readOnly = true;
    document.getElementById("code-block").style.display = "block";
    startTimer(15 * 60);
}

    if (data.userSuccess) {
    if(isReg){
        msg.innerHTML = `<div class="alert alert-success">Вы успешно зарегистрировались! Теперь можете войти в аккаунт через кнопку «Вход».</div>`;
    }
    else {
        msg.innerHTML = `<div class="alert alert-success">Данные профиля были успешно изменены.</div>`;
    }
    emailVerified = true;
    document.getElementById("code-block").style.display = "none";
}
});
});
    function verifyCode(isReg) {

    const email = document.getElementById("email").value;
    const code = document.getElementById("code").value;

    fetch("/verify_email", {
    method: "POST",
    headers: { "Content-Type": "application/x-www-form-urlencoded" },
    body: new URLSearchParams({
    email: email,
    code: code
})
})
    .then(res => {
    if (!res.ok) throw new Error("Ошибка ответа от сервера");
    return res.json();
})
    .then(data => {
    console.log("Ответ сервера:", data); // для отладки
    const msg = document.getElementById("message-block");
    msg.innerHTML = "";

    if (data.success) {
        if(isReg){
            msg.innerHTML = `<div class="alert alert-success">Email подтверждён! Можете продолжить регистрацию!</div>`;
        }
        else{
            msg.innerHTML = `<div class="alert alert-success">Email подтверждён! Теперь можете сохранить свои новые данные!.</div>`;

        }
    emailVerified = true;
    document.getElementById("code-block").style.display = "none";
} else if (data.expired) {
    msg.innerHTML = `<div class="alert alert-warning">Срок действия кода истёк. Пожалуйста, запросите новый.</div>`;
} else if (data.notMatch) {
    msg.innerHTML = `<div class="alert alert-danger">Введённый код неверен. Попробуйте ещё раз.</div>`;
} else {
    msg.innerHTML = `<div class="alert alert-danger">Неизвестная ошибка. Попробуйте позже.</div>`;
}
})
    .catch(err => {
    console.error("Ошибка при проверке кода:", err);
    document.getElementById("message-block").innerHTML =
    `<div class="alert alert-danger">Произошла ошибка при отправке запроса.</div>`;
});
}

    function startTimer(duration) {
    let timer = duration;
    const display = document.getElementById("code-timer-text");

    const interval = setInterval(() => {
    const minutes = String(Math.floor(timer / 60)).padStart(2, '0');
    const seconds = String(timer % 60).padStart(2, '0');
    display.textContent = `Осталось времени: ${minutes}:${seconds}`;

    if (--timer < 0) {
    clearInterval(interval);
    display.textContent = "Срок действия кода истёк.";
}
}, 1000);
}