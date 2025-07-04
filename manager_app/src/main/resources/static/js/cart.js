
    function loadCart() {
    fetch("/api/cart")
        .then(res => res.json())
        .then(cartDTO => {
            const tableBody = document.getElementById("cart-items-table");
            const badge = document.getElementById("cart-count-badge");

            tableBody.innerHTML = ""; // очищаем корзину перед добавлением
            badge.textContent = cartDTO.totalQuantity;

            if (cartDTO.itemsDTOList.length === 0) {
                const row = document.createElement("tr");
                row.innerHTML = `
                        <td colspan="5" class="text-center text-muted">Корзина пуста</td>
                    `;
                tableBody.appendChild(row);
                return;
            }

            cartDTO.itemsDTOList.forEach((item, index) => {
                const row = document.createElement("tr");

                row.innerHTML = `
                        <th scope="row">${index + 1}</th>
                        <td><img src="/images/${item.productId}.webp" width="60"></td>
                        <td>${item.title}</td>
                        <td>${item.price}₽</td>
                        <td><i class="fa-solid fa-trash text-danger" style="cursor:pointer"
                               onclick="removeFromCart(${item.productId})"></i></td>
                    `;
                tableBody.appendChild(row);
            });
        });
}
    function loadWishList() {

        fetch("/api/wish-list")
            .then(res => res.json())
            .then(wishListDTO => {
                const tableBody = document.getElementById("wish-items-table");
                const badge = document.getElementById("wish-count-badge");

                tableBody.innerHTML = "";
                badge.textContent = wishListDTO.totalQuantity;

                if (wishListDTO.itemsDTOList.length === 0) {
                    const row = document.createElement("tr");
                    row.innerHTML = `
                        <td colspan="5" class="text-center text-muted">Список желаний пуст</td>
                    `;
                    tableBody.appendChild(row);
                    return;
                }

                wishListDTO.itemsDTOList.forEach((item, index) => {
                    const row = document.createElement("tr");

                    row.innerHTML = `
                        <th scope="row">${index + 1}</th>
                        <td><img src="/images/${item.productId}.webp" width="60"></td>
                        <td>${item.title}</td>
                        <td>${item.price}₽</td>
                        <td><i class="fa-solid fa-trash text-danger" style="cursor:pointer"
                               onclick="removeFromWishList(${item.productId})"></i></td>
                    `;
                    tableBody.appendChild(row);
                });
            });
    }

    function removeFromCart(productId) {
    fetch(`/api/cart/${productId}`, {
        method: "DELETE"
    }).then(res => {
        if (res.ok) {
            loadCart(); // заново загрузить корзину
        } else {
            alert("Ошибка при удалении товара.");
        }
    });
}
    function removeFromWishList(productId) {
        fetch(`/api/wish-list/${productId}`, {
            method: "DELETE"
        }).then(res => {
            if (res.ok) {
                loadWishList();
            } else {
                alert("Ошибка при удалении товара.");
            }
        });
    }