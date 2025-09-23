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

    const isRegistrationPage = form.action.includes("users/registration")

    if(isRegistrationPage){
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




    fetch("/api/users/"+(isRegistrationPage?"create":"update"), {
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
    if(isRegistrationPage){
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
