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