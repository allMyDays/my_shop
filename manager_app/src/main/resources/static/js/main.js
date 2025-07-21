
let userRoles=null;

document.addEventListener("DOMContentLoaded", async function () {

    loadCartSize();
    loadWishListSize();


    const urlParams = new URLSearchParams(window.location.search);  // загрузить категории на фронт
    const selectedCategoryId = urlParams.get('categoryId');

    fetch('/api/categories', {
        method: "GET"
    })
        .then(response => response.json())
        .then(categories => {
            const select = document.getElementById('categorySelect');
            categories.forEach(cat => {
                const option = document.createElement('option');
                option.value = cat.id;
                option.textContent = cat.name;
                select.appendChild(option);
            });
            if (selectedCategoryId) {
                select.value = selectedCategoryId;
            }
        });

    const userSupportButtons = document.getElementById("support-user-buttons");
    const agentSupportButtons = document.getElementById("support-agent-buttons");

    fetch("/get_user_roles")
        .then(res => res.json())
        .then(roles => {
            userRoles=roles;
            if (isStaff()) {
                agentSupportButtons.style.display = "block";
                userSupportButtons.style.display = "none";
            } else {
                agentSupportButtons.style.display = "none";
                userSupportButtons.style.display = "block";
            }
        })
        .catch(err => {
            console.error("Ошибка при получении ролей:", err);
        });

});

function isStaff(){
    if(!userRoles) return false;
    return (userRoles.includes("ROLE_AGENT") || userRoles.includes("ROLE_ADMIN"));
}


function loadCartSize() {
    fetch(`/api/cart/size`, {
        method: "GET"
    }).then(res => res.json())
        .then(data => {
            const badge = document.getElementById("cart-count-badge");
            badge.textContent = data.count;

        });
}

function loadWishListSize() {
    fetch(`/api/wish-list/size`, {
        method: "GET"
    }).then(res => res.json())
        .then(data => {
            const badge = document.getElementById("wish-count-badge");
            badge.textContent = data.count;

        });
}

function formatDateTime(timestampStr) {
    if (!timestampStr) return "";

    const date = new Date(timestampStr);
    const day = String(date.getDate()).padStart(2, '0');
    const month = String(date.getMonth() + 1).padStart(2, '0');
    const year = String(date.getFullYear()).slice(-2); // последние две цифры года
    const hours = String(date.getHours()).padStart(2, '0');
    const minutes = String(date.getMinutes()).padStart(2, '0');

    return `${day}.${month}.${year}, ${hours}:${minutes}`;
}


<!-- блок чата поддержки -->


document.addEventListener("DOMContentLoaded", function () {     // отправка сообщений по Enter
    const input = document.getElementById("support-chat-input");

    input.addEventListener("keydown", function (event) {
        if (event.key === "Enter" && !event.shiftKey) {
            event.preventDefault();  // чтобы не добавлялся перенос строки
            sendSupportMessage();
        }
    });
});


document.addEventListener("input", function (event) {      // авторост поля ввода
    const textarea = event.target;
    if (textarea.classList.contains("auto-resize")) {
        textarea.style.height = "auto"; // сброс
        const maxHeight = 120; // максимум в px (примерно 5-6 строк)
        if (textarea.scrollHeight > maxHeight) {
            textarea.style.overflowY = "scroll";
            textarea.style.height = maxHeight + "px";
        } else {
            textarea.style.overflowY = "hidden";
            textarea.style.height = textarea.scrollHeight + "px";
        }
    }
});




let supportChatId = null;
let supportStompClient = null;
let supportStompSubscription = null;
let pendingDeleteChatId = null;

function connectToSupportWebSocket() {
    return new Promise((resolve, reject) => {

    if(!supportChatId){
        return reject("ChatId is null");
    }

    if(!supportStompClient){
        supportStompClient = Stomp.over(new SockJS('/ws'));
    }

    if(supportStompClient.connected){
        // уже подключен - просто переключаем подписку
        if(supportStompSubscription){
            supportStompSubscription.unsubscribe();
        }
        supportStompSubscription = supportStompClient.subscribe(`/topic/support_chat/${supportChatId}`, function (response) {
            const data = JSON.parse(response.body);
            appendMessage(data.message, data.userMessage, data.dateOfCreation); // сообщение от юзера/поддержки
        });
        return;
    }


    supportStompClient.connect({}, function () {
        supportStompSubscription = supportStompClient.subscribe(`/topic/support_chat/${supportChatId}`, function (response) {
            const data = JSON.parse(response.body);
            appendMessage(data.message, data.userMessage, data.dateOfCreation); // сообщение от юзера/поддержки
        });
        resolve(); // соединение установлено!
    }, reject); // если ошибка соединения

    });
}





function disconnectWebSocket() {
    if (supportStompClient !== null) {
        supportStompClient.disconnect(() => {
            console.log("WebSocket отключён");
        });
        supportStompClient = null;
    }
    unsubscribeChannel();
}
function unsubscribeChannel() {
    if(supportStompSubscription){
        supportStompSubscription.unsubscribe();
    }
    supportStompSubscription=null;
}


function openSupportChat(topic) {

    document.getElementById("support-chat-topic-display").textContent = topic;
    document.getElementById("support-chat").style.display = "flex";
}

function closeSupportChat(disconnect) {
    document.getElementById("support-chat").style.display = "none";
    document.getElementById("support-chat-body").innerHTML = "";
    document.getElementById("support-chat-topic-display").textContent = "";
    document.getElementById("support-chat-input").value = "";
    document.getElementById("support-chat-footer").style.display = "flex";
    document.getElementById("support-chat-closed-msg").style.display = "none";

    supportChatId = null;
    if(disconnect){
        disconnectWebSocket();
    }
}


async function tryCreateNewSupportChat() {
    const response = await fetch(`/api/support/create_chat_check_limit`);
    if (response.status === 429) {
        alert("К сожалению, чаты нельзя создавать так часто. Пожалуйста, попробуйте позже.");
        return;
    }

    new bootstrap.Modal(document.getElementById('topicModal')).show();
}

async function createNewSupportChat() {

    const topicInput = document.getElementById("support-topic-input");
    const errorDiv = document.getElementById("topic-error");

    const topic = topicInput.value.trim();

    if (!topic) {
        errorDiv.style.display = "block";
        return;
    }

    errorDiv.style.display = "none";

    // Закрыть модальное окно
    bootstrap.Modal.getInstance(document.getElementById("topicModal")).hide();

    // очистить визуально прошлый чат если он есть
    closeSupportChat(false);
    unsubscribeChannel();


    try {
        const response = await fetch(`/api/support/create_chat?topic=${encodeURIComponent(topic)}`);
        const data = await response.json();
        supportChatId = data.chatId;

        openSupportChat(topic);
        appendMessage("Напишите здесь ваш вопрос и мы рассмотрим его в ближайшее время.", false);

    } catch (e) {
        console.error("Ошибка при создании чата:", e);
    }
}

function deleteSupportChat() {
    if (!pendingDeleteChatId) return;

    // Закрыть модальное окно
    bootstrap.Modal.getInstance(document.getElementById("deleteSupportChatModal")).hide();

    fetch(`/api/support/delete_chat?chatId=${pendingDeleteChatId}`, {
        method: "DELETE"
    })
        .then(res => {
            if (res.ok) {
                fetch("/api/support/get_user_chats")
                    .then(res => res.json())
                    .then(data => {
                        renderSupportChatList(data,false);
                    });

            } else {
                alert("Ошибка при удалении чата");
            }
        })
        .catch(err => {
            console.error("Ошибка при удалении чата", err);
        });
    if(supportChatId&&supportChatId===pendingDeleteChatId){
        closeSupportChat(true);
    }

    pendingDeleteChatId = null;
}





function appendMessage(message, isUser, timestampStr) {
    const msgContainer = document.getElementById("support-chat-body");

    const div = document.createElement("div");
    if(isStaff()===true) isUser=!isUser;
    div.className = (isUser ? "chat-message user" : "chat-message agent");

    const msgText = document.createElement("div");
    msgText.textContent = message;

    const timeSpan = document.createElement("span");
    timeSpan.textContent = formatDateTime(timestampStr);
    timeSpan.classList.add("chat-timestamp");

    div.appendChild(msgText);
    div.appendChild(timeSpan);

    msgContainer.appendChild(div);
    msgContainer.scrollTop = msgContainer.scrollHeight;
}

async function collectExistingChat(chatid, topic, closed, needsAnswer, agent) {
    closeSupportChat(false); // очистить визуально прошлый чат если он есть
    unsubscribeChannel();


    try {
        const response = await fetch(`/api/support/get_chat_messages?chatId=${chatid}`, {
            method: "GET"
        });
        const messages = await response.json();

        messages.forEach(message => {

            appendMessage(message.message,message.userMessage, message.dateOfCreation);

        });

        supportChatId=chatid;

        // Закрыть модальное окно
        bootstrap.Modal.getInstance(document.getElementById("chatListModal")).hide();

        openSupportChat(topic);

        if(closed){
            document.getElementById("support-chat-footer").style.display = "none";
            document.getElementById("support-chat-closed-msg").style.display = "block";

        }
        else if(needsAnswer){

            await connectToSupportWebSocket();
        }



    } catch (error) {
        console.error("Ошибка при рендере сообщений чата", error);
    }

}

async function sendSupportMessage() {
    const messageInput = document.getElementById("support-chat-input");

    const message = messageInput.value.trim();

    if (!message || !supportChatId) return;

    if(isStaff()===false) {
        try {
            const response = await fetch(`/api/support/handle_message_check_limit?chatId=${supportChatId}`, {
                method: "GET"
            });

            if (response.status === 429) {
                alert("Вы можете отправлять сообщения в чат поддержки не чаще одного раза в 2 минуты.");
                return;
            }
        } catch (error) {
            console.error("Ошибка при проверке лимита:", error);
        }
    }

        if (!supportStompClient || !supportStompSubscription || !supportStompClient.connected) {
            await connectToSupportWebSocket();
        }



    messageInput.value = "";
    messageInput.style.height="auto";


    const payload = {
        chatId: supportChatId,
        message: message
    };

    supportStompClient.send("/app/"+(isStaff()?"handle_agent_message":"handle_user_message"), {}, JSON.stringify(payload));

}

<!-- блок списка чатов поддержки -->

function openSupportChatList(){
    // Закрыть модальное окно
    bootstrap.Modal.getInstance(document.getElementById("supportModal")).hide();


    fetch("/api/support"+(isStaff()===true?"/get_active_chats":"/get_user_chats"))
        .then(res => res.json())
        .then(data => {
            renderSupportChatList(data);
             new bootstrap.Modal(document.getElementById("chatListModal")).show();
        });

}



function renderSupportChatList(chatList) {
    const container = document.getElementById("chat-list-container");

    const listName = document.getElementById("chatListModalLabel");
    if(isStaff()===true) listName.textContent="Эти чаты ждут ваш ответ!";

    container.innerHTML = ""; // Очистить список

    chatList.forEach(chat => {
        const li = document.createElement("li");
        li.className = "list-group-item chat-item d-flex justify-content-between align-items-center";

        // Левая часть: тема и статус
        const left = document.createElement("div");
        left.className = "chat-topic";
        left.style.cursor = "pointer";
        left.onclick = () => collectExistingChat(chat.id, chat.topic, chat.closed, chat.needsAnswer);

        const topicSpan = document.createElement("span");
        topicSpan.textContent = chat.topic;
        left.appendChild(topicSpan);

        const statusSpan = document.createElement("span");
        statusSpan.className = "chat-status ms-2 " + (chat.closed
            ? "text-danger"
            : (chat.needsAnswer ? "text-warning" : "text-success"));
        statusSpan.textContent = chat.closed
            ? "Закрыт"
            : (chat.needsAnswer ? (isStaff()===true?"Ждёт ваш ответ":"На рассмотрении") : "Есть ответ");
        left.appendChild(statusSpan);

        // Правая часть: дата + крестик
        const right = document.createElement("div");
        right.className = "d-flex align-items-center";

        const dateDiv = document.createElement("div");
        dateDiv.className = "chat-date me-3";
        dateDiv.textContent = formatDateTime(chat.dateOfCreation);

        right.appendChild(dateDiv);

        if(isStaff()===false){
        const deleteBtn = document.createElement("button");
        deleteBtn.className = "btn btn-link p-0 m-0 text-dark";
        deleteBtn.style.textDecoration = "none"; // убираем подчёркивание
        deleteBtn.innerHTML = '<i class="bi bi-x fs-5"></i>';
        deleteBtn.onclick = (e) => {
            e.stopPropagation(); // чтобы не открывался чат
            pendingDeleteChatId = chat.id;
            new bootstrap.Modal(document.getElementById('deleteSupportChatModal')).show();
        };
            right.appendChild(deleteBtn);
        }

        li.appendChild(left);
        li.appendChild(right);
        container.appendChild(li);
    });
}













































































