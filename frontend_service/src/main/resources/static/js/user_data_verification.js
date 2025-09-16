
    let myTimer;

    document.getElementById("profile-form").addEventListener("submit", function (e) {
        e.preventDefault();
        const form = e.target;
        const formData = new FormData(form);
    document.getElementById("common-block").style.display = "";
    const submitBTN = document.getElementById("form-submit-btn");
    const loadingIndicator = document.querySelector('.loader');
    submitBTN.style.display = "none";
    loadingIndicator.style.display="";

    const password = document.getElementById("password").value.trim();
    const repeatedPassword = document.getElementById("repeatedPassword").value.trim();
    const msg = document.getElementById("message-block");
        msg.innerHTML = ""; //очистить все ошибки

    const isReg = form.action.includes("users/registration")

    if(isReg){
        if(!password||!repeatedPassword){
            msg.innerHTML += `<div class="alert alert-danger">Пожалуйте, заполните все строки ввода пароля.</div>`;
            submitBTN.style.display = "";
            loadingIndicator.style.display="none";
            return;
        }
        if(!(password===repeatedPassword)){
            msg.innerHTML += `<div class="alert alert-danger">Пароли не совпадют!</div>`;
            submitBTN.style.display = "";
            loadingIndicator.style.display="none";
            return;
        }
    }
    else{
        if((!password&&repeatedPassword)||(password&&!repeatedPassword)){
            msg.innerHTML += `<div class="alert alert-danger">Похоже, что вы забыли заполнить строку ввода пароля.</div>`;
            submitBTN.style.display = "";
            loadingIndicator.style.display="none";
            return;
        }
        if((password&&repeatedPassword)&&(!(password===repeatedPassword))){
            msg.innerHTML += `<div class="alert alert-danger">Пароли не совпадют!</div>`;
            submitBTN.style.display = "";
            loadingIndicator.style.display="none";
            return;
        }
    }




    fetch("/api/users/"+(isReg?"create":"update"), {
    method: "POST",
    headers: {
        'Content-type':'application/json'
    },
        body: JSON.stringify(Object.fromEntries(formData))

})
    .then(res =>{
        if(!res.ok){
            submitBTN.style.display = "";
            loadingIndicator.style.display="none";
            msg.innerHTML += `<div class="alert alert-danger">Произошла ошибка. Попробуйте снова чуть позже.</div>`;
        } return res.json()})
    .then(data => {
    msg.innerHTML = "";

    if (data.ERRORS) {
    data.ERRORS.forEach(err => {
    msg.innerHTML += `<div class="alert alert-danger">${err}</div>`;
});
        submitBTN.style.display = "";
        loadingIndicator.style.display="none";
    return;
}

    if (data.EMAIL_SENT) {
        loadingIndicator.style.display="none";
    msg.innerHTML = `<div class="alert alert-info">На адрес <b>${formData.get("email")}</b> был отправлен код. Введите его в течение 1 часа.</div>`;
    document.getElementById("email").readOnly = true;
    document.getElementById("code-block").style.display = "block";


    startTimer(60 * 60);
}

    if (data.SUCCESS) {
    if(isReg){
        msg.innerHTML = `<div class="alert alert-success">Вы успешно зарегистрировались! Теперь можете <a href="/users/welcome" >войти в свой аккаунт</a>.</div>`;
    }
    else {
        msg.innerHTML = `<div class="alert alert-success">Данные профиля были успешно изменены.</div>`;
    }
    document.getElementById("code-block").style.display = "none";
    loadingIndicator.style.display="none";
    submitBTN.style.display = "none";
}
});
    });
    function verifyCode(isReg) {

    const email = document.getElementById("email").value;
    const code = document.getElementById("code").value;

    fetch("/api/users/verify_email", {
        method: "POST",
        headers: {
            'Content-type':'application/json'
        },
        body: JSON.stringify({email:email,userCode:code})

    })
    .then(res => {
    if (!res.ok) throw new Error("Ошибка ответа от сервера");
    return res.json();
})
    .then(data => {
    console.log("Ответ сервера:", data);
    const msg = document.getElementById("message-block");
    msg.innerHTML = "";

    switch (data){

        case "SUCCESS":
            if(isReg){
                msg.innerHTML = `<div class="alert alert-success">Email подтверждён! Можете продолжить регистрацию!</div>`;
            }
            else{
                msg.innerHTML = `<div class="alert alert-success">Email подтверждён! Теперь можете сохранить свои новые данные!.</div>`;

            }
            document.getElementById("code-block").style.display = "none";
            document.getElementById("form-submit-btn").style.display = "";
            break;

        case "EXPIRED":
            msg.innerHTML = `<div class="alert alert-warning">Срок действия кода истёк. Пожалуйста, запросите новый.</div>`;
            document.getElementById("code-block").style.display = "none";
            document.getElementById("email").readOnly = false;
            document.getElementById("form-submit-btn").style.display = "";
            break;

        case "NOT_MATCH":
            msg.innerHTML = `<div class="alert alert-danger">Введённый код неверен. Попробуйте ещё раз.</div>`;
            break;

        case "TOO_MANY_ATTEMPTS":
            msg.innerHTML = `<div class="alert alert-danger">Слишком много попыток верификации данного email адреса. Попробуйте позже.</div>`;
            document.getElementById("code-block").style.display = "none";
            break;

        default:
            msg.innerHTML = `<div class="alert alert-danger">Неизвестная ошибка. Попробуйте позже или перезагрузите страницу.</div>`;
            document.getElementById("code-block").style.display = "none";
            document.getElementById("email").readOnly = false;
            document.getElementById("form-submit-btn").style.display = "";
            break;

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
    let display = document.getElementById("code-timer-text");
    if(myTimer){
        clearInterval(myTimer);
    }

    myTimer = setInterval(() => {
    let minutes = String(Math.floor(timer / 60)).padStart(2, '0');
    let seconds = String(timer % 60).padStart(2, '0');
    display.textContent = `Осталось времени: ${minutes}:${seconds}`;

    if (--timer < 0) {
    clearInterval(myTimer);
    document.getElementById("common-block").style.display = "none";
    document.getElementById("email").readOnly = false;
    document.getElementById("form-submit-btn").style.display = "";
}
}, 1000);
}