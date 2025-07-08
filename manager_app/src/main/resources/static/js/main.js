
    document.addEventListener("DOMContentLoaded", function () {
    loadCartSize();
    loadWishListSize();
});


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
