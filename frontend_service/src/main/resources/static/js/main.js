
document.addEventListener("DOMContentLoaded", async function () {

    const urlParams = new URLSearchParams(window.location.search);
    const selectedCategoryCode = urlParams.get('categoryCode');

    fetch('/api/catalogue/categories', {
        method: "GET"
    })
        .then(response => response.json())
        .then(categories => {
            const select = document.getElementById('categorySelect');
            categories.forEach(cat => {
                const option = document.createElement('option');
                option.value = cat.code;
                option.textContent = cat.name;
                select.appendChild(option);
            });
            if (selectedCategoryCode) {
                select.value = selectedCategoryCode;
            }
        });

    const userSupportButtons = document.getElementById("support-user-buttons");
    const agentSupportButtons = document.getElementById("support-agent-buttons");

    if (userIsStaff()) {
            agentSupportButtons.style.display = "block";
            userSupportButtons.style.display = "none";
        } else {
            agentSupportButtons.style.display = "none";
            userSupportButtons.style.display = "block";
        }


   if(userIsAuthenticated()===true){
       loadCartSize();
       loadWishListSize();
   }

});


function mainSearchFind() {

    const input = document.getElementById("main-search-input");

    const categoryCode = document.getElementById("categorySelect").value;

    const inputValue = input.value.trim();

    if (!inputValue || inputValue.length < 2) {
        alert("Необходимо ввести запрос в строку поиска.");
        return;
    }

    const params = new URLSearchParams();

    params.append("filter", inputValue);
    if (categoryCode) params.append("categoryCode", categoryCode);

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
    const input = document.getElementById("isUserStaff");
    return input?input.value === "true":false;
}

function userIsAuthenticated() {
    if(document.getElementById("isUserAuthenticated")) return true;

}

function getCurrentUserId(){
    const userIdInput = document.getElementById("currentUserId");
    return userIdInput? parseInt(userIdInput.value):null;
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

















































































