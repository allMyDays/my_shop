let myTimer;
function verifyEmailCode() {

    console.log('функция verifyEmailCode запущена');

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
                        msg.innerHTML = `<div class="alert alert-success">Email успешно подтверждён!</div>`;
                    document.getElementById("code-block").style.display = "none";
                    document.getElementById("form-submit-btn").click();

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