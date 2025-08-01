let cartProductIds = new Set();
let wishProductIds = new Set();

document.addEventListener('DOMContentLoaded', () => {
    Promise.all([
        fetch('/api/cart/items').then(res => res.json()),
        fetch('/api/wish-list/items').then(res => res.json())
    ]).then(([cartItems, wishItems]) => {
        cartItems.forEach(item => cartProductIds.add(String(item.productId)));
        wishItems.forEach(item => wishProductIds.add(String(item.productId)));
    });
});

function bindProductButtons(element) {
    const cartBtn = element.querySelector('.cart-btn');
    const wishBtn = element.querySelector('.wish-btn');

    if (cartBtn) {
        const productId = cartBtn.getAttribute('data-product-id');
        if (cartProductIds.has(productId)) {
            cartBtn.textContent = 'Удалить из корзины';
            cartBtn.classList.add('in-cart', 'btn-danger');
            cartBtn.classList.remove('btn-dark');
        }

        cartBtn.addEventListener('click', () => {
            const isAdded = cartBtn.classList.contains('in-cart');
            if (!isAdded) {
                fetch(`/api/cart/add?productId=${productId}&quantity=1`, {
                    method: 'POST'
                }).then(res => {
                    if (res.ok) {
                        cartBtn.textContent = 'Удалить из корзины';
                        cartBtn.classList.add('in-cart', 'btn-danger');
                        cartBtn.classList.remove('btn-dark');
                        loadCartSize();
                    }
                });
            } else {
                fetch(`/api/cart/${productId}`, {
                    method: 'DELETE'
                }).then(res => {
                    if (res.ok) {
                        cartBtn.innerHTML = 'В корзину <i class="fa-solid fa-cart-shopping"></i>';
                        cartBtn.classList.remove('in-cart', 'btn-danger');
                        cartBtn.classList.add('btn-dark');
                        loadCartSize();
                    }
                });
            }
        });
    }

    if (wishBtn) {
        const productId = wishBtn.getAttribute('data-product-id');
        const icon = wishBtn.querySelector('i');
        if (wishProductIds.has(productId)) {
            icon.classList.add('text-danger');
        }

        wishBtn.addEventListener('click', () => {
            const isActive = icon.classList.contains('text-danger');
            if (!isActive) {
                fetch(`/api/wish-list/add?productId=${productId}`, {
                    method: 'POST'
                }).then(res => {
                    if (res.ok) {
                        icon.classList.add('text-danger');
                        loadWishListSize();
                    }
                });
            } else {
                fetch(`/api/wish-list/${productId}`, {
                    method: 'DELETE'
                }).then(res => {
                    if (res.ok) {
                        icon.classList.remove('text-danger');
                        loadWishListSize();
                    }
                });
            }
        });
    }
}

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