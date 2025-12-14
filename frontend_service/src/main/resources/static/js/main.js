let userRoles = null;
let authenticated = false;

document.addEventListener("DOMContentLoaded", async function () {

    const urlParams = new URLSearchParams(window.location.search);  // загрузить категории на фронт
    const selectedCategoryId = urlParams.get('categoryId');

    fetch('/api/catalogue/categories', {
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

    const response = await fetch('/api/users/get_roles');

    if (response.ok) {
        authenticated = true;
        userRoles = await response.json();

        if (userIsStaff()) {
            agentSupportButtons.style.display = "block";
            userSupportButtons.style.display = "none";
        } else {
            agentSupportButtons.style.display = "none";
            userSupportButtons.style.display = "block";
        }
    } else {
        authenticated = false;
        console.error('Ошибка при получении ролей:', response.status);
    }

    loadCartSize();
    loadWishListSize();

});


function mainSearchFind() {

    const input = document.getElementById("main-search-input");

    const categoryId = document.getElementById("categorySelect").value;

    const inputValue = input.value.trim();

    if (!inputValue || inputValue.length < 2) {
        alert("Необходимо ввести запрос в строку поиска.");
        return;
    }

    const params = new URLSearchParams();

    params.append("filter", inputValue);
    if (categoryId) params.append("categoryId", categoryId);

    window.location.href = "/products/get/all?" + params.toString();
}

async function logout() {

    try {
        const response = await fetch("/api/users/logout", {
            method: "GET",
            credentials: "include"
        });
        if (response.ok) {
            window.location.href = '/';
        }
    } catch (error) {
        console.error('Error:', error);
    }
}

function userIsStaff() {
    if (!userRoles) return false;
    return (userRoles.includes("ROLE_AGENT") || userRoles.includes("ROLE_ADMIN"));
}

function userIsAuthenticated() {
    return authenticated;
}


function loadCartSize() {
    const badge = document.getElementById("cart-count-badge");
    badge.textContent = '0';
    fetch(`/api/cart/size`, {
        method: "GET"
    }).then(res => {
        if (!res.ok) {
            return;
        }
        return res.json();
    })
        .then(size => {
            badge.textContent = size;

        });
}

function loadWishListSize() {
    const badge = document.getElementById("wish-count-badge");
    badge.textContent = '0';

    fetch(`/api/wish-list/size`, {
        method: "GET"
    }).then(res => {
        if (!res.ok) {
            return;
        }
        return res.json()
    })
        .then(size => {
            badge.textContent = size;

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
let supportChatCreatorId = null;
let supportStompClient = null;
let supportChatStompSubscription = null;
let supportTypingStompSubscription = null;
let pendingDeleteChatId = null;
const supportMessageSound = document.getElementById("support-message-sound");

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

        setTimeout(() => typingSentRecently = false, 3000);
    }

    // Обновляем таймаут очистки индикатора
    if (typingTimeout) clearTimeout(typingTimeout);
    typingTimeout = setTimeout(() => {
        sendTypingStatus(false); // остановка печати
    }, 5000); // если пользователь перестал печатать
});

// 📤 Отправка статуса через WebSocket
function sendTypingStatus(isTyping) {
    supportStompClient.send("/support-chat-input-controller/typing_status", {}, JSON.stringify({
        chatId: supportChatId,
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


function connectToSupportWebSocket() {
    return new Promise((resolve, reject) => {

        if (!supportChatId) {
            return reject("ChatId is null");
        }

        if (!supportStompClient) {
            supportStompClient = Stomp.over(new SockJS('/ws-support'));
        }

        if (supportStompClient.connected) {
            // уже подключен - просто переключаем подписку
            if (supportChatStompSubscription) supportChatStompSubscription.unsubscribe();
            if (supportTypingStompSubscription) supportTypingStompSubscription.unsubscribe();
            subscribeSupportChannels();
            return;
        }


        supportStompClient.connect({}, function () {
            subscribeSupportChannels();

            resolve(); // соединение установлено!
        }, reject); // если ошибка соединения

    });
}

function subscribeSupportChannels() {

    if (supportChatStompSubscription) supportChatStompSubscription.unsubscribe();
    if (supportTypingStompSubscription) supportTypingStompSubscription.unsubscribe();

    supportChatStompSubscription = supportStompClient.subscribe(`/support-chat-output-topic/${supportChatId}`, function (response) {
        const data = JSON.parse(response.body);
        appendMessage(data.message, data.userMessage, data.dateOfCreation, true); // сообщение от юзера/поддержки
    });

    supportTypingStompSubscription = supportStompClient.subscribe(`/support-chat-output-topic/${supportChatId}/typing`, function (response) {
        const data = JSON.parse(response.body);
        showTypingIndicator(data.typing, data.agent);
    });

}


function disconnectWebSocket() {
    if (supportStompClient !== null) {
        supportStompClient.disconnect(() => {
            console.log("WebSocket отключён");
        });
        supportStompClient = null;
    }
    unsubscribeSupportChannels();
}

function unsubscribeSupportChannels() {
    if (supportChatStompSubscription) supportChatStompSubscription.unsubscribe();
    if (supportTypingStompSubscription) supportTypingStompSubscription.unsubscribe();
    supportChatStompSubscription = null;
    supportTypingStompSubscription = null;
}


function openSupportChat(topic) {

    document.getElementById("support-chat-topic-display").textContent = topic;
    document.getElementById("support-chat").style.display = "flex";
    if (userIsStaff() === true) {
        document.getElementById("support-chat-agent-buttons").style.display = "block";

    }
}

function closeSupportChat(disconnect) {
    document.getElementById("support-chat").style.display = "none";
    document.getElementById("support-chat-body").innerHTML = "";
    document.getElementById("support-chat-topic-display").textContent = "";
    document.getElementById("support-chat-input").value = "";
    document.getElementById("support-chat-footer").style.display = "flex";
    document.getElementById("support-chat-closed-msg").style.display = "none";

    supportChatId = null;
    supportChatCreatorId = null;
    if (disconnect) {
        disconnectWebSocket();
    }
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
    closeSupportChat(false);
    unsubscribeSupportChannels();


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
        supportChatId = data.chatId;
        supportChatCreatorId = data.userId;

        await connectToSupportWebSocket();

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
    if (supportChatId && supportChatId === pendingDeleteChatId) {
        closeSupportChat(true);
    }

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

async function collectExistingChat(chatId, topic, closed, needsAnswer, userId, containsMessages) {
    closeSupportChat(false); // очистить визуально прошлый чат если он есть
    unsubscribeSupportChannels();

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

        supportChatId = chatId;
        supportChatCreatorId = userId;

        // Закрыть модальное окно
        bootstrap.Modal.getInstance(document.getElementById("chatListModal")).hide();

        if (closed) {
            document.getElementById("support-chat-footer").style.display = "none";
            document.getElementById("support-chat-closed-msg").style.display = "block";

        } else {

            await connectToSupportWebSocket();
        }
        openSupportChat(topic);
        if(containsMessages===false){
            appendMessage("Напишите здесь ваш вопрос и мы рассмотрим его в ближайшее время.", false);

        }


    } catch (error) {
        console.error("Ошибка при рендере сообщений чата", error);
        alert("Произошла ошибка. Пожалуйста, попробуйте еще раз.")
    }

}

function openUserPageStaffMethod() {
    if (!supportChatCreatorId) {
        alert("Произошла ошибка: userId is null");
        return;
    }
    window.location.href = "/admin/profile?userId=" + supportChatCreatorId;
}


async function sendSupportMessage() {
    const messageInput = document.getElementById("support-chat-input");

    const message = messageInput.value.trim();

    if(!supportChatId) alert("Ошибка: supportChatId is null");

    if (!message || !supportChatId) return;

    if (userIsStaff() === false) {
        try {
            const response = await fetch(`/api/support/message/send-ability?chatId=${supportChatId}`, {
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

    if (!supportStompClient || !supportChatStompSubscription || !supportStompClient.connected) {
        await connectToSupportWebSocket();
    }


    messageInput.value = "";
    messageInput.style.height = "auto";


    const payload = {
        chatId: supportChatId,
        message: message
    };

    try {
        supportStompClient.send("/support-chat-input-controller/" + (userIsStaff() ? "handle_agent_message" : "handle_user_message"), {}, JSON.stringify(payload));
    } catch (error) {
        alert("При отправке сообщения произошла ошибка. Пожалуйста, попробуйте еще раз.");
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
            request = `/api/support/admin/get_user_chats/${supportChatCreatorId}`
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
        left.onclick = () => collectExistingChat(chat.id, chat.topic, chat.closed, chat.needsAnswer, chat.userId, chat.containsMessages);

        const topicSpan = document.createElement("span");
        topicSpan.textContent = chat.topic;
        left.appendChild(topicSpan);

        const statusSpan = document.createElement("span");
        statusSpan.className = "chat-status ms-2 " + (chat.closed
            ? "text-danger"
            : (chat.needsAnswer ? "text-warning" : "text-success"));
        statusSpan.textContent = chat.closed
            ? "Закрыт"
            : (chat.needsAnswer ? (userIsStaff() === true ? "Ждёт ваш ответ" : "На рассмотрении") : (chat.containsMessages===true?"Есть ответ":"Нет сообщений"));
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

<!-- блок заказов -->

async function tryCreateNewOrder(orderAll, productId) {
    const response = await fetch(`/api/order/create-ability`);
    if (response.status === 409) {
        new bootstrap.Modal(document.getElementById('orderErrorModal')).show();
        return;
    }
    if (!response.ok) {
        alert("Произошла ошибка. Пожалуйста, попробуйте позже.");
        return;
    }

    if (orderAll === true) {
        new bootstrap.Modal(document.getElementById('orderConfirmModal')).show();
        return;
    } openOrderModal(false, productId);

}

let orderRequestItems;
let orderProductOffset = 0;
const OrderProductLimit = 40;
let OrderLoading = false;
let allOrderLoaded = false;

const carousel = document.getElementById("orderProductsCarousel");

carousel.addEventListener("slid.bs.carousel", () => {
    const items = document.querySelectorAll("#orderProductsContainer .carousel-item");
    const activeIndex = Array.from(items).findIndex(item => item.classList.contains("active"));

    // если активный элемент — последний
    if (activeIndex === items.length - 1) {
        // подгружаю следующую партию
        loadOrderProducts();
    }
});


    async function openOrderModal(orderAll, productId) {
        orderRequestItems=null;
        orderProductOffset = 0;
        OrderLoading = false;
        allOrderLoaded = false;

        document.getElementById("order-carousel-buttons").style.display="";
        const itemQuantity = document.getElementById('orderItemQuantity');

        let items;

        if (orderAll === true) {
            bootstrap.Modal.getInstance(document.getElementById("orderConfirmModal")).hide();

            const response = await fetch('/api/cart/brief-items');
            if (!response.ok){
                alert('Ошибка при получении данных.');
                return;
            }

            items = await response.json();

            let counter = 0;

            items.forEach((item) => {
                counter+=item.productQuantity;
            });
            itemQuantity.textContent = counter;

        } else{
            itemQuantity.textContent = document.getElementById(`quantity-${productId}`).textContent;
        }

        document.getElementById("orderProductsContainer").innerHTML="";

         orderRequestItems = (orderAll===true? items:[
            {
                productId: productId,
                productQuantity: parseInt(itemQuantity.textContent)
            } ]);


         if(orderRequestItems.length===1){
             document.getElementById("order-carousel-buttons").style.display="none";
         }

        await loadOrderProducts();

        const priceResponse = await fetch('/api/catalogue/products/total-price',{
            method: 'POST',
            headers: {
                'Content-Type': 'application/json'
            }, body: JSON.stringify(orderRequestItems)
        });

        if (!priceResponse.ok){
            alert('Ошибка при получении данных.');
            return;
        }
        const priceJson = await priceResponse.json();
        document.getElementById('orderItemPrice').textContent = priceJson.priceView;


    try {
    const response = await fetch('/api/order/delivery/info');
    if (!response.ok) throw new Error('Ошибка при получении данных');

    const data = await response.json();

    document.getElementById('orderStorageAddress').textContent = data.storageAddress;
    document.getElementById('orderUserAddress').textContent = data.userAddress;
    document.getElementById('orderDeliveryDistanceView').textContent = data.deliveryDistanceView;
    document.getElementById('orderDeliveryTimeView').textContent = data.deliveryTimeView;
    document.getElementById("orderDeliveryPrice").textContent = data.deliveryPriceView;


    const modal = new bootstrap.Modal(document.getElementById('orderModal'));
    modal.show();

} catch (error) {
    console.error(error);
    alert('Не удалось загрузить данные о доставке.');
 }
}
let creatingOrder=false;
async function сreateNewOrder() {
    if(creatingOrder===true) return;
    try{
        creatingOrder=true;
        document.getElementById("confirmOrderBtn").textContent="Оформляем.."


   if(!orderRequestItems){
       alert("Произошла ошибка: не найдены товары для совершения заказа.");
       return;
   }

    const response = await fetch('/api/order/create',{
        method: 'POST',
        headers: {
            'Content-Type': 'application/json'
        }, body: JSON.stringify(orderRequestItems)
    });

    if (!response.ok){
        alert('Произошла ошибка при создании заказа. Пожалуйста, попробуйте позже.');
        return;
    }

    bootstrap.Modal.getInstance(document.getElementById("orderModal")).hide();

    const modal = new bootstrap.Modal(document.getElementById('orderSuccessModal'));
    modal.show();
    document.getElementById("closingCart").click();

    setInterval(() => { loadCartSize();}, 700);

    }finally {
        creatingOrder=false;
        document.getElementById("confirmOrderBtn").textContent="Оформить заказ"

    }


}

async function loadOrderProducts() {
    if (OrderLoading || allOrderLoaded) return;
    try {
        OrderLoading = true;
    // беру следующую партию из локального массива
    const batch = orderRequestItems.slice(orderProductOffset, orderProductOffset + OrderProductLimit);
    if (batch.length === 0) {
        allOrderLoaded = true;
        return;
    }
        // достаю id товаров для запроса на бэкенд
        const productIds = batch.map(item => item.productId);

        // запрашиваем детали товаров (название, цену, фото и т.д.)
        const response = await fetch("/api/catalogue/products/get-by-ids", {
            method: "POST",
            headers: { "Content-Type": "application/json" },
            body: JSON.stringify(productIds)
        });

        if(!response.ok){
            alert("Ошибка при подгрузке товаров.");
            return;
        }

        const products = await response.json();

        // объединяю данные с количеством из orderRequestItems
        const itemsToRender = batch.map(item => {
            const product = products.find(p => p.id === item.productId);
            return {
                ...product,
                quantity: item.productQuantity
            };
        });

        // добавляею карточки в карусель
        const container = document.getElementById("orderProductsContainer");
        itemsToRender.forEach((product, index) => {
            const activeClass = (orderProductOffset === 0 && index === 0) ? "active" : "";
            const itemHTML = `
        <div class="carousel-item ${activeClass}">
          <div class="d-flex flex-column align-items-center">
          <a href="/products/get/${product.id}" target="_blank"> 
            <img src="/api/media/get/${product.previewImageFileName}" class="d-block" style="max-height:200px; object-fit:contain;" alt="${product.title}">
          </a>  
            <div class="mt-2 text-center">
              <h6>${product.title}</h6>
              <p class="text-secondary">Количество: ${product.quantity}</p>
              <p class="text-secondary">${product.priceInt+ '₽ за единицу'}</p>
              <p class="text-secondary">${(product.priceInt*product.quantity) + '₽ общая стоимость'}</p>
            </div>
          </div>
        </div>`;
            container.insertAdjacentHTML("beforeend", itemHTML);
        });

        orderProductOffset += batch.length;
    } catch (error) {
        console.error("Ошибка при подгрузке товаров:", error);
    } finally {
        OrderLoading = false;
    }
}














































































