let cartOffset = 0;
let cartLoading = false;
let cartHasMore = true;

function resetCart() {
    cartOffset = 0;
    cartHasMore = true;
    const tableBody = document.getElementById("cart-items-table");
    tableBody.innerHTML = "";
}

function loadCart(initial = false) {
    if (!initial&&(cartLoading || !cartHasMore)) return;

    cartLoading = true;
    const tableBody = document.getElementById("cart-items-table");

    if (initial) {
        resetCart();
        const row = document.createElement("tr");
        row.innerHTML = `
                <td colspan="5" class="text-center text-muted">Подождите, идет загрузка...</td>
            `;
        tableBody.appendChild(row);
    }

    fetch(`/api/cart/items?offset=${cartOffset}`, {
        credentials: "include"
    })
        .then(res => {
            if (!res.ok) {
                alert("Ошибка загрузки корзины.");
                return;
            }
            return res.json();
        })
        .then(items => {

            if (initial) {
                tableBody.innerHTML = "";
            }

            if (items.length === 0 && cartOffset === 0) {
                const row = document.createElement("tr");
                row.innerHTML = `
                        <td colspan="5" class="text-center text-muted">Корзина пуста</td>
                    `;
                tableBody.appendChild(row);
                cartHasMore = false;
                return;
            }

            items.forEach((item, index) => {
                const row = document.createElement("tr");
                row.setAttribute("cart-data-product-id", item.productId);


               row.innerHTML = ` 
        <th scope="row">${cartOffset + index + 1}</th>
        <td>
        <a href="/products/get/${item.productId}" target="_blank"> 
        <img src="/api/media/get/${item.previewImageFileName}" height="60">
        </a>
        </td>
        <td class="fw-bold">${item.title}</td>
         <td id="price-${item.productId}" class="fw-bold">${item.price * item.quantity}₽</td>
                <td class="text-end">
            <div class="d-flex align-items-center">
                <button class="btn btn-sm btn-outline-secondary" onclick="updateQuantityByOne(${item.productId},false,${item.price} )">-</button>
                <span id="quantity-${item.productId}" class="mx-2 fw-bold">${item.quantity}</span>
                <button class="btn btn-sm btn-outline-secondary" onclick="updateQuantityByOne(${item.productId}, true, ${item.price})">+</button>
            </div>
        </td>
        <td>
        <button class="btn btn-pink fw-bold">Купить</button>
       </td>
       
    `;
                tableBody.appendChild(row);
            });

            // если бек вернул меньше 40, значит больше нет
            if (items.length < 40) {
                cartHasMore = false;
            } else {
                cartOffset += 40;
            }
        })
        .finally(() => cartLoading = false);
}

function removeFromCart(productId) {
           const row = document.querySelector(`tr[cart-data-product-id="${productId}"]`);
            if (row) row.remove();

            const badge = document.getElementById("cart-count-badge");
            let count = parseInt(badge.textContent);
            if (count > 0) badge.textContent = (count - 1).toString();

            document.dispatchEvent(new CustomEvent('cartItemRemoved', {
                detail: { productId: productId }
            }));

            const tableBody = document.getElementById("cart-items-table");
            if (tableBody.children.length === 0) {
                const row = document.createElement("tr");
                row.innerHTML = `
                        <td colspan="5" class="text-center text-muted">Корзина пуста</td>
                    `;
                tableBody.appendChild(row);
            }
}

document.addEventListener("DOMContentLoaded", () => {
    const cartBody = document.querySelector("#offcanvasCart .offcanvas-body");
    cartBody.addEventListener("scroll", () => {
        if (cartBody.scrollTop + cartBody.clientHeight >= cartBody.scrollHeight - 50) {
            loadCart(false);
        }
    });
});

function updateQuantityByOne(productId, increase, price) {
    fetch(`/api/cart/${productId}?increase=${increase}`, {
        method: "PUT",
        credentials: "include"
    }).then(res => {
        if (!res.ok) {
            alert("Ошибка при изменении количества товара.");
            return;
        } return res.json();
    }).then(newSize=>{
        const badge = document.getElementById("cart-count-badge");
        let count = parseInt(badge.textContent);
        if (!increase&&count > 0) badge.textContent = (count - 1).toString();
        if(increase) badge.textContent = (count + 1).toString();

        if(newSize===0){
            removeFromCart(productId);
            return;
        }
        document.getElementById(`quantity-${productId}`).textContent = newSize;

        document.getElementById(`price-${productId}`).textContent = (newSize*price) + "₽";


    });

}


let wishListOffset = 0;
let wishListLoading = false;
let wishListHasMore = true;

function resetWishList() {
    wishListOffset = 0;
    wishListHasMore = true;
    const tableBody = document.getElementById("wish-items-table");
    tableBody.innerHTML = "";
}

function loadWishList(initial = false) {
    if (!initial&&(wishListLoading || !wishListHasMore)) return;

    wishListLoading = true;
    const tableBody = document.getElementById("wish-items-table");

    if (initial) {
        resetWishList()
        const row = document.createElement("tr");
        row.innerHTML = `
                <td colspan="5" class="text-center text-muted">Подождите, идет загрузка...</td>
            `;
        tableBody.appendChild(row);
    }

    fetch(`/api/wish-list/items?offset=${wishListOffset}`, {
        credentials: "include"
    })
        .then(res => {
            if (!res.ok) {
                alert("Ошибка загрузки списка желаний.");
                return;
            }
            return res.json();
        })
        .then(items => {

            if (initial) {
                tableBody.innerHTML = "";
            }

            if (items.length === 0 && wishListOffset === 0) {
                const row = document.createElement("tr");
                row.innerHTML = `
                        <td colspan="5" class="text-center text-muted">Список желаний пуст</td>
                    `;
                tableBody.appendChild(row);
                wishListHasMore = false;
                return;
            }

            items.forEach((item, index) => {
                const row = document.createElement("tr");
                row.setAttribute("wish-data-product-id", item.productId);

                row.innerHTML = `
                        <th scope="row">${wishListOffset + index + 1}</th>
                        <td>
                        <a href="/products/get/${item.productId}" target="_blank"> 
                        <img src="/api/media/get/${item.previewImageFileName}" height="60" class="rounded">
                        </a>
                        </td>
                        <td class="fw-bold">${item.title}</td>
                        <td class="fw-bold">${item.price}₽</td>
                        <td><i class="fa-solid fa-trash text-danger" style="cursor:pointer"
                               onclick="removeFromWishList(${item.productId})"></i></td>
                    `;


                tableBody.appendChild(row);
            });

            // если бек вернул меньше 40, значит больше нет
            if (items.length < 40) {
                wishListHasMore = false;
            } else {
                wishListOffset += 40;
            }
        })
        .finally(() => wishListLoading = false);
}

function removeFromWishList(productId) {
    fetch(`/api/wish-list/${productId}`, {
        method: "DELETE",
        credentials: "include"
    }).then(res => {
        if (res.ok) {
            const row = document.querySelector(`tr[wish-data-product-id="${productId}"]`);
            if (row) row.remove();

            const badge = document.getElementById("wish-count-badge");
            let count = parseInt(badge.textContent);
            if (count > 0) badge.textContent = (count - 1).toString();

            document.dispatchEvent(new CustomEvent('wishItemRemoved', {
                detail: { productId: productId }
            }));

            const tableBody = document.getElementById("wish-items-table");
            if (tableBody.children.length === 0) {
                const row = document.createElement("tr");
                row.innerHTML = `
                        <td colspan="5" class="text-center text-muted">Список желаний пуст</td>
                    `;
                tableBody.appendChild(row);
            }
        } else {
            alert("Ошибка при удалении товара.");
        }
    });
}

document.addEventListener("DOMContentLoaded", () => {
    const cartBody = document.querySelector("#offcanvasWish .offcanvas-body");
    cartBody.addEventListener("scroll", () => {
        if (cartBody.scrollTop + cartBody.clientHeight >= cartBody.scrollHeight - 50) {
            loadWishList(false);
        }
    });
});











