
    function loadCart() {
        const tableBody = document.getElementById("cart-items-table");
        tableBody.innerHTML="";
        const row = document.createElement("tr");
        row.innerHTML = `
                        
                  <td colspan="5" class="text-center text-muted">Подождите, идёт загрузка...</td>
                    `;
        tableBody.appendChild(row);

      fetch("/api/cart")
            .then(res => {
                if (!res.ok) {
                    alert("Ошибка загрузки корзины.");
                    return;
                }
                return res.json()
            })
            .then(cartDTO => {
                const badge = document.getElementById("cart-count-badge");

                tableBody.innerHTML = "";
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
                        <td><img src="/api/media/images/product/get/${item.previewImageFileName}" width="60"></td>
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

        const tableBody =  document.getElementById("wish-items-table");
        tableBody.innerHTML="";
        const row = document.createElement("tr");
        row.innerHTML = `
                        
                  <td colspan="5" class="text-center text-muted">Подождите, идёт загрузка...</td>
                    `;
        tableBody.appendChild(row);

        fetch("/api/wish-list")
            .then(res => {
                if(!res.ok){
                    alert("Ошибка загрузки списка желаний.");
                    return;
                }
                return res.json()
            })
            .then(wishListDTO => {
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
                        <td><img src="/api/media/images/product/get/${item.previewImageFileName}" width="60"></td>
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
        method: "DELETE",
        credentials: "include"
    }).then(res => {
        if (res.ok) {
            loadCart(); // заново загрузить корзину
            document.dispatchEvent(new CustomEvent('cartItemRemoved', {
                detail: { productId: productId }
            }));

        } else {
            alert("Ошибка при удалении товара.");
        }
    });
}
    function removeFromWishList(productId) {
        fetch(`/api/wish-list/${productId}`, {
            method: "DELETE",
            credentials: "include"
        }).then(res => {
            if (res.ok) {
                loadWishList();
                document.dispatchEvent(new CustomEvent('wishItemRemoved', {
                    detail: { productId: productId }
                }));
            } else {
                alert("Ошибка при удалении товара.");
            }
        });
    }

