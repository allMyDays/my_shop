
document.addEventListener('DOMContentLoaded', () => {         // ждет пока занрузится вся страница
    // 1. Загрузка данных из корзины и wish-листа
    let cartProductIds = new Set();
    let wishProductIds = new Set();

    Promise.all([                // означат: жди пока придет оба ответа и потом работай с ними вместе
        fetch('/api/cart/items').then(res => res.json()),
        fetch('/api/wish-list/items').then(res => res.json())
    ]).then(([cartItems, wishItems]) => {               // ответы записываются в переменные
        cartItems.forEach(item => cartProductIds.add(String(item.productId)));
        wishItems.forEach(item => wishProductIds.add(String(item.productId)));

        // 2. Обработка кнопок корзины
        document.querySelectorAll('.cart-btn').forEach(btn => {    // берутся все кнопки с классом .cart-btn
            const productId = btn.getAttribute('data-product-id');    // получаем айди продукта

            if (cartProductIds.has(productId)) {
                btn.textContent = 'Удалить из корзины';
                btn.classList.add('in-cart', 'btn-danger');
                btn.classList.remove('btn-dark');
            }

            btn.addEventListener('click', () => {  //когда нажали на кнопку
                const isAdded = btn.classList.contains('in-cart');        // проверяем, есть ли товар в корзине?

                if (!isAdded) {
                    fetch(`/api/cart/add?productId=${productId}&quantity=1`, {
                        method: 'POST'
                    }).then(response => {
                        if (response.ok) {
                            btn.textContent = 'Удалить из корзины';
                            btn.classList.add('in-cart', 'btn-danger');
                            btn.classList.remove('btn-dark');
                            loadCartSize()
                        }
                    });
                } else {
                    fetch(`/api/cart/${productId}`, {
                        method: 'DELETE'
                    }).then(response => {
                        if (response.ok) {
                            btn.innerHTML = 'В корзину <i class="fa-solid fa-cart-shopping"></i>';
                            btn.classList.remove('in-cart', 'btn-danger');
                            btn.classList.add('btn-dark');
                            loadCartSize()
                        }
                    });
                }
            });
        });

        // 3. Обработка кнопок wish-list
        document.querySelectorAll('.wish-btn').forEach(btn => {
            const productId = btn.getAttribute('data-product-id');
            const icon = btn.querySelector('i');

            if (wishProductIds.has(productId)) {
                icon.classList.add('text-danger');
            }

            btn.addEventListener('click', () => {
                const isActive = icon.classList.contains('text-danger');

                if (!isActive) {
                    fetch(`/api/wish-list/add?productId=${productId}`, {
                        method: 'POST'
                    }).then(response => {
                        if (response.ok) {
                            icon.classList.add('text-danger');
                            loadWishListSize()
                        }
                    });
                } else {
                    fetch(`/api/wish-list/${productId}`, {
                        method: 'DELETE'
                    }).then(response => {
                        if (response.ok) {
                            icon.classList.remove('text-danger');
                            loadWishListSize()
                        }
                    });
                }
            });
        });
    });
})

document.addEventListener('cartItemRemoved', event => {
    const removedId = String(event.detail.productId);

    document.querySelectorAll('.cart-btn').forEach(btn => {
        const productId = btn.getAttribute('data-product-id');
        if (productId === removedId) {
            btn.innerHTML = 'В корзину <i class="fa-solid fa-cart-shopping"></i>';
            btn.classList.remove('in-cart', 'btn-danger');
            btn.classList.add('btn-dark');
        }
    });
});

document.addEventListener('wishItemRemoved', event => {
    const removedId = String(event.detail.productId);

    document.querySelectorAll('.wish-btn').forEach(btn => {
        const productId = btn.getAttribute('data-product-id');
        if (productId === removedId) {
            const icon = btn.querySelector('i');
            icon.classList.remove('text-danger');
        }
    });
});




