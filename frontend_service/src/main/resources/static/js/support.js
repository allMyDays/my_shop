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


let currentSupportChatId = null;
let currentSupportChatCreatorId = null;
let supportStompClient = null;
const subscriptionMap = new Map();
let pendingDeleteChatId = null;
const supportMessageSound = document.getElementById("support-message-sound");

document.addEventListener("DOMContentLoaded", async function () {


        fetch(userIsStaff()===false?`/api/support/chat/count_unread`:`/api/support/admin/count_active_chats`, {
            method: "GET"
        }).then(res => {
            if (!res.ok) {
                console.log("Не удалось получить количество непрочитанных чатов.");
            }
            return res.json();
        })
            .then(size => {
                updateSupportBadgeQuantity(false, size);
            });

        await connectToSupportWebSocket();

        if(userIsStaff()===false){
            fetch(`/api/support/chat/get_unanswered`, {
                method: "GET"
            }).then(res => {
                if (!res.ok) {
                    console.log("Не удалось получить id чатов, нуждающихся в ответе.");
                }
                return res.json();
            }).then(ids => {
                ids.forEach(id=>{
                    subscribeToChatChannel(id);
                });
            });
        }

});


function playSupportMessageSound(){
    supportMessageSound.currentTime=0;

    supportMessageSound.play().catch(e => console.log("Автовоспроизведение звука не удалось: ", e));


}

//  блок статуса "печатает..."

let typingTimeout = null;
let typingAnimInterval = null;
let typingDots = 0;
let typingSentRecently = false;

const messageInput = document.getElementById("support-chat-input");
const typingIndicator = document.getElementById("typing-indicator");

// 👂 Слушаем ввод
messageInput.addEventListener("input", () => {
    if (!supportStompClient || !supportStompClient.connected) return;

    if (typingSentRecently === false) {
        sendTypingStatus(true);
        typingSentRecently = true;

        setTimeout(() => typingSentRecently = false, 300);
    }

    // Обновляем таймаут очистки индикатора
    if (typingTimeout) clearTimeout(typingTimeout);
    typingTimeout = setTimeout(() => {
        sendTypingStatus(false); // остановка печати
    }, 500); // если пользователь перестал печатать
});

// 📤 Отправка статуса через WebSocket
function sendTypingStatus(isTyping) {
    supportStompClient.send("/support-chat-input-controller/typing_status", {}, JSON.stringify({
        chatId: currentSupportChatId,
        typing: isTyping,
        agent: userIsStaff()
    }));
}

// 👁️ Показ и анимация
function showTypingIndicator(isTyping, agent) {

    if ((userIsStaff() && agent) || (!userIsStaff() && !agent)) return;


    if (isTyping) {
        typingIndicator.style.display = "block";

        if (typingAnimInterval) clearInterval(typingAnimInterval);
        typingDots = 0;

        typingAnimInterval = setInterval(() => {
            typingDots = (typingDots + 1) % 4;
            typingIndicator.textContent = (agent ? "Агент поддержки" : "Пользователь") + " печатает" + ".".repeat(typingDots);
        }, 400);
    } else {
        clearInterval(typingAnimInterval);
        typingIndicator.style.display = "none";
    }
}


async function connectToSupportWebSocket() {
    return new Promise((resolve, reject) => {
        if (!supportStompClient) {
            supportStompClient = Stomp.over(new SockJS('/ws-support'));
        }
        if (supportStompClient.connected) {
            resolve();
            return;
        }

        supportStompClient.connect({},
            () => {
                console.log('Connected to support WebSocket');
                resolve();
            },
            (error) => {
                console.error('Connection error:', error);
                reject(error);
            }
        );
    });
}

async function subscribeToChatChannel(chatId) {

    if(subscriptionMap.has(chatId)) return;

    let supportChatStompSubscription = supportStompClient.subscribe(`/support-chat-output-topic/${chatId}`, async function (response) {
        const data = JSON.parse(response.body);
        if(!currentSupportChatId||currentSupportChatId!==data.chatId){
            if(userIsStaff()===true&&currentSupportChatId){
                updateSupportBadgeQuantity(false);
            } else{
                if(currentSupportChatId) closeSupportChat(); // очистить визуально прошлый чат

                const response = await fetch(`/api/support/chat/get?chatId=${data.chatId}`, {
                    method: "GET",
                    credentials: "include"
                });
                if (!response.ok) {
                    console.log("Произошла ошибка при попытке получить данные чата: "+response.body);
                    return;
                }
                const chat = await response.json();

                await collectExistingChat(data.chatId, chat.topic, chat.closed, chat.needsAnswer, chat.userId, chat.containsMessages, chat.read);
                playSupportMessageSound();
            }
        } else{
            appendMessage(data.message, data.userMessage, data.dateOfCreation, true);
            markSupportChatAsRead(data.chatId);



        }

    });

    let supportTypingStompSubscription = supportStompClient.subscribe(`/support-chat-output-topic/${chatId}/typing`, function (response) {
        const data = JSON.parse(response.body);
        if(currentSupportChatId&&currentSupportChatId===data.chatId){
            showTypingIndicator(data.typing, data.agent);
        }
    });

    subscriptionMap.set(chatId, [supportChatStompSubscription, supportTypingStompSubscription])

}

function markSupportChatAsRead(chatId){

    if(userIsStaff()===false){

        fetch(`/api/support/chat/mark_as_read?chatId=${chatId}`, {
            method: "PUT"
        })
            .then(res => {
                if (res.ok){
                    updateSupportBadgeQuantity(true);
                }
            })
            .catch(err => {
                console.error("Ошибка при попытке отметить чат прочитанным.", err);
            });
    }
}

function updateSupportBadgeQuantity(decrease, requiredQuantity=1){
    const badge = document.getElementById("support-count-badge");
    let currentQuantity = parseInt(badge.textContent);
    let newQuantity;

    if(decrease===true&&currentQuantity-requiredQuantity<0) newQuantity = (0).toString();
    else newQuantity = (currentQuantity+(decrease===true?-requiredQuantity:requiredQuantity)).toString();

    badge.textContent=newQuantity;
    if(userIsStaff()===false) document.getElementById("support-count-badge-2").textContent=newQuantity;
    else document.getElementById("support-count-badge-3").textContent=newQuantity;


}


function unsubscribeFromChatChannel(chatId){

    if(!subscriptionMap.has(chatId)) return;

    const [sub1, sub2] = subscriptionMap.get(chatId);

    sub1.unsubscribe();
    sub2.unsubscribe();

    subscriptionMap.delete(chatId);

}


function openSupportChat(topic) {

    document.getElementById("support-chat-topic-display").textContent = topic;
    document.getElementById("support-chat").style.display = "flex";
    if (userIsStaff() === true) {
        document.getElementById("support-chat-agent-buttons").style.display = "block";

    }
    const chatBody = document.getElementById("support-chat-body");

    chatBody.scrollTop = chatBody.scrollHeight;


}

function closeSupportChat() {
    document.getElementById("support-chat").style.display = "none";
    document.getElementById("support-chat-body").innerHTML = "";
    document.getElementById("support-chat-topic-display").textContent = "";
    document.getElementById("support-chat-input").value = "";
    document.getElementById("support-chat-footer").style.display = "flex";
    document.getElementById("support-chat-closed-msg").style.display = "none";

    currentSupportChatId = null;
    currentSupportChatCreatorId = null;
}


async function tryCreateNewSupportChat(isOrderSupport=false) {
    const response = await fetch(`/api/support/chat/create-ability`);
    if (response.status === 429) {
        alert("К сожалению, чаты нельзя создавать так часто. Пожалуйста, попробуйте позже.");
        return;
    }
    if (!response.ok) {
        alert("Произошла ошибка. Пожалуйста, попробуйте позже.");
        return;
    }

    if(isOrderSupport===false){
        new bootstrap.Modal(document.getElementById('topicModal')).show();
    }
    else await createNewSupportChat(true);



}

async function createNewSupportChat(isOrderSupport=false) {

    const topicInput = document.getElementById("support-topic-input");
    const errorDiv = document.getElementById("topic-error");

    const topic = isOrderSupport===false?topicInput.value.trim()
        :("Помощь по заказу #"+document.getElementById("orderIdField").value);

    if (!topic || topic.length < 3 || topic.length > 30) {
        errorDiv.style.display = "block";
        return;
    }

    errorDiv.style.display = "none";

    // Закрыть модальное окно
    if(isOrderSupport===false) bootstrap.Modal.getInstance(document.getElementById("topicModal")).hide();

    // очистить визуально прошлый чат если он есть
    closeSupportChat();
    //unsubscribeSupportChannels();


    try {
        const response = await fetch('/api/support/chat/create', {
            method: "POST",
            headers: {
                'Content-Type': 'application/json'
            },
            body: JSON.stringify({topic: topic})
        });
        if (!response.ok) {
            alert("Произошла ошибка при создани чата. Пожалуйста, попробуйте еще раз.")
            return;
        }


        const data = await response.json();
        currentSupportChatId = data.chatId;
        currentSupportChatCreatorId = data.userId;

        openSupportChat(topic);
        appendMessage("Напишите здесь ваш вопрос и мы рассмотрим его в ближайшее время.", false);

    } catch (e) {
        console.error("Ошибка при создании чата:", e);
        alert("Произошла ошибка при создании чата. Пожалуйста, попробуйте еще раз.")
    }
}

function deleteSupportChat() {
    if (!pendingDeleteChatId) return;

    // Закрыть модальное окно
    bootstrap.Modal.getInstance(document.getElementById("deleteSupportChatModal")).hide();

    fetch(`/api/support/chat/delete?chatId=${pendingDeleteChatId}`, {
        method: "DELETE"
    })
        .then(res => {
            if (res.ok) {
                fetch("/api/support/chat/get_all")
                    .then(res => res.json())
                    .then(data => {
                        renderSupportChatList(data, false);
                    });

            } else {
                alert("Ошибка при удалении чата");
            }
        })
        .catch(err => {
            console.error("Ошибка при удалении чата", err);
            alert("Произошла ошибка при удалении чата. Пожалуйста, попробуйте еще раз.")
        });
    if (currentSupportChatId && currentSupportChatId === pendingDeleteChatId) {
        closeSupportChat();
    }
    unsubscribeFromChatChannel(pendingDeleteChatId)

    pendingDeleteChatId = null;
}


function appendMessage(message, isUser, timestampStr, isRealTime=false) {
    const msgContainer = document.getElementById("support-chat-body");

    const div = document.createElement("div");
    if (userIsStaff() === true) isUser = !isUser;
    div.className = (isUser ? "chat-message user" : "chat-message agent");

    const msgText = document.createElement("div");
    msgText.textContent = message;

    const timeSpan = document.createElement("span");
    timeSpan.textContent = formatDateTime(timestampStr);
    timeSpan.classList.add("chat-timestamp");

    div.appendChild(msgText);
    div.appendChild(timeSpan);

    msgContainer.appendChild(div);

    if(isRealTime&&!isUser){
        playSupportMessageSound();
    }

    msgContainer.scrollTop = msgContainer.scrollHeight;
}

async function collectExistingChat(chatId, topic, closed, needsAnswer, userId, containsMessages, isRead) {
    closeSupportChat(); // очистить визуально прошлый чат если он есть
    // unsubscribeSupportChannels();

    let request = `/api/support/message/get_all?chatId=${chatId}`;

    if (userIsStaff() === true) {
        request = `/api/support/admin/get_chat_messages?chatId=${chatId}`;
    }

    try {
        const response = await fetch(request, {
            method: "GET"
        });

        if (!response.ok) {
            alert("Произошла ошибка при запросе списка чатов.");
            return;
        }


        const messages = await response.json();

        messages.forEach(message => {

            appendMessage(message.message, message.userMessage, message.dateOfCreation);

        });

        // Закрыть модальное окно
        const modalEl =  document.getElementById("chatListModal")
        const modalInst = bootstrap.Modal.getInstance(modalEl);

        if(modalInst&&modalInst._isShown){
            modalInst.hide();
        }


        if (closed) {
            document.getElementById("support-chat-footer").style.display = "none";
            document.getElementById("support-chat-closed-msg").style.display = "block";

        } else {
            if(needsAnswer===true) await subscribeToChatChannel(chatId);
        }
        openSupportChat(topic);
        if(containsMessages===false){
            appendMessage("Напишите здесь ваш вопрос и мы рассмотрим его в ближайшее время.", false);

        }

        currentSupportChatId = chatId;
        currentSupportChatCreatorId = userId;

        if(isRead===false) markSupportChatAsRead(chatId);


    } catch (error) {
        console.error("Ошибка при рендере сообщений чата", error);
        alert("Произошла ошибка при попытке отобразить чат поддержки. Пожалуйста, попробуйте еще раз.")
        currentSupportChatId = null;
        currentSupportChatCreatorId = null;
    }

}

function openUserPageStaffMethod() {
    if (!currentSupportChatCreatorId) {
        alert("Произошла ошибка: userId is null");
        return;
    }
    window.location.href = "/admin/profile?userId=" + currentSupportChatCreatorId;
}
async function goToSupportAgentPage() {
    try {
        const response = await fetch("/api/users/login", {
            method: "POST",
            headers: {
                "Content-Type": "application/json"
            },
            credentials: "include",
            body: JSON.stringify({
                nickName: "агент",
                password: "agent"
            })
        });
        if (response.ok) {
            window.location.href = '/';
        } else{
            alert("Произошла ошибка!!");

        }
    } catch (error) {
        console.error('Error:', error);
    }
}


async function sendSupportMessage() {
    const messageInput = document.getElementById("support-chat-input");

    const message = messageInput.value.trim();

    if(!currentSupportChatId) alert("Ошибка: currentSupportChatId is null");

    if (!message || !currentSupportChatId) return;

    if (userIsStaff() === false) {
        try {
            const response = await fetch(`/api/support/message/send-ability?chatId=${currentSupportChatId}`, {
                method: "GET"
            });

            if (response.status === 429) {
                alert("К сожалению, сообщения в чат нельзя отправлять так часто. Попробуйте снова чуть позже.");
                return;
            }
        } catch (error) {
            console.error("Ошибка при проверке лимита:", error);
        }
    }
    await subscribeToChatChannel(currentSupportChatId);


    messageInput.value = "";
    messageInput.style.height = "auto";


    const payload = {
        chatId: currentSupportChatId,
        message: message
    };

    try {
        supportStompClient.send("/support-chat-input-controller/" + (userIsStaff() ? "handle_agent_message" : "handle_user_message"), {}, JSON.stringify(payload));
    } catch (error) {
        alert("При отправке сообщения произошла ошибка. Пожалуйста, попробуйте еще раз.");
    }

    if (userIsStaff()===true) updateSupportBadgeQuantity(true);
    else{
        setTimeout(() => {
            new bootstrap.Modal(document.getElementById('supportAgentSuggestModal')).show();
        }, 700);

    }


}

<!-- блок списка чатов поддержки -->

function openSupportChatList(getAllActiveChats) {
    // Закрыть модальное окно
    bootstrap.Modal.getInstance(document.getElementById("supportModal")).hide();


    let request = null;

    if (userIsStaff() === true) {
        if (getAllActiveChats === true) {
            request = `/api/support/admin/get_active_chats`
        } else {
            request = `/api/support/admin/get_user_chats/${currentSupportChatCreatorId}`
        }
    } else {
        request = `/api/support/chat/get_all`
    }
    fetch(request)


        .then(res => {
            if (!res.ok) {
                alert("Произошла ошибка при запросе списка чатов.")
            }

            return res.json()

        })
        .then(data => {
            renderSupportChatList(data, getAllActiveChats);
            new bootstrap.Modal(document.getElementById("chatListModal")).show();
        });

}


function renderSupportChatList(chatList, getAllActiveChats) {
    const container = document.getElementById("chat-list-container");

    const listName = document.getElementById("chatListModalLabel");
    if (userIsStaff() === true) {
        if (getAllActiveChats === true) listName.textContent = "Эти чаты ждут ваш ответ!";
        else listName.textContent = "Чаты пользователя";
    }

    container.innerHTML = ""; // Очистить список

    if (chatList.length === 0) {
        container.innerHTML = `
                        <td colspan="5" class="text-center text-muted">Список чатов пуст</td>
                    `;
        return;
    }
    chatList.forEach(chat => {
        const li = document.createElement("li");
        li.className = "list-group-item chat-item d-flex justify-content-between align-items-center";

        // Левая часть: тема и статус
        const left = document.createElement("div");
        left.className = "chat-topic";
        left.style.cursor = "pointer";
        left.onclick = () => collectExistingChat(chat.id, chat.topic, chat.closed, chat.needsAnswer, chat.userId, chat.containsMessages, chat.read);

        const topicSpan = document.createElement("span");
        topicSpan.textContent = chat.topic;
        left.appendChild(topicSpan);

        const statusSpan = document.createElement("span");

        let statusClass;
        let statusText;
        if(chat.closed){
            statusClass = "text-danger";
            statusText = "Закрыт";

        } else if(chat.needsAnswer===true){
            statusClass = "text-warning";
            statusText = userIsStaff()?"Ждёт ваш ответ" : "На рассмотрении";
        } else if(chat.read===false){
            statusClass = "text-dark";
            statusText = "Есть новый ответ"
        } else if(chat.containsMessages===false){
            statusClass = "text-success";
            statusText = "Нет сообщений";
        }
        else{
            statusClass = "text-success";
            statusText = "Есть ответ";
        }

        statusSpan.className = "chat-status ms-2 " +statusClass;
        statusSpan.textContent = statusText;


        left.appendChild(statusSpan);

        // Правая часть: дата + крестик
        const right = document.createElement("div");
        right.className = "d-flex align-items-center";

        const dateDiv = document.createElement("div");
        dateDiv.className = "chat-date me-3";
        dateDiv.textContent = formatDateTime(chat.dateOfCreation);

        right.appendChild(dateDiv);

        if (userIsStaff() === false) {
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