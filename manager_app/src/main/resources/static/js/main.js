
    document.addEventListener("DOMContentLoaded", function () {
    loadCartSize();
    loadWishListSize();

    const urlParams = new URLSearchParams(window.location.search);
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
                if(selectedCategoryId){
                    select.value=selectedCategoryId;
                }
            });







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
