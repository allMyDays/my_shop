let offset = 0;
const limit = 40;
let loading = false;
let noMoreGoods=false;
let noSuchGoods=true;


const filter = document.getElementById("main-search-input").value.trim();
const categoryId = new URLSearchParams(window.location.search).get('categoryId');

function loadMoreProducts() {
    if (loading||noMoreGoods) return;
    loading = true;
    noMoreGoods=true;

    let currentOffset=offset;
    offset+=limit;

    const params = new URLSearchParams();

    params.append("filter",filter);
    if (categoryId) params.append("categoryId", categoryId);
    params.append("offset",currentOffset);


    const eventSource = new EventSource("/products/lazy?"+params.toString());

    eventSource.onmessage = function (event) {
        const product = JSON.parse(event.data);
        appendProductCard(product);
        noMoreGoods=false;
        noSuchGoods=false;
    };

    eventSource.onerror = function () {
        eventSource.close();
        document.getElementById("products-loading-indicator").style.display = "none";
        loading = false;
        if(noSuchGoods){
            document.getElementById("no-such-goods").style.display="";
        }
    };

    eventSource.onopen = function () {
        document.getElementById("products-loading-indicator").style.display = "block";
    };

    eventSource.addEventListener("complete", () => {
        document.getElementById("products-loading-indicator").style.display = "none";
        eventSource.close();
        loading = false;
    });
}

function appendProductCard(product) {
    const container = document.getElementById("product-grid");

    const col = document.createElement("div");
    col.className = "col";

    col.innerHTML = `
        <div class="card h-100">
            <a href="/product_page/${product.id}" target="_blank">
                <img src="/api/images/get_product_image/${product.previewImageFileName}" 
                     class="card-img-top" 
                     alt="product_photo" 
                     width="200" 
                     height="370">
            </a>
            <div class="card-body d-flex flex-column">
                <h5 class="card-title">${product.price}₽</h5>
                <p class="card-text flex-grow-1">${product.title}</p>

                <div class="d-flex justify-content-between mt-auto">
                    ${isUserAuthenticated() ? `
                        <div>
                            <button class="btn btn-dark cart-btn" data-product-id="${product.id}">
                                В корзину <i class="fa-solid fa-cart-shopping"></i>
                            </button>
                            <button class="btn btn-light wish-btn" data-product-id="${product.id}">
                                <i class="fa-solid fa-heart"></i>
                            </button>
                        </div>
                    ` : `
                        <a href="/login">
                            <button type="button" class="btn btn-outline-danger">Войти на сайт</button>
                        </a>
                    `}
                </div>
            </div>
        </div>
    `;

    container.appendChild(col);
    bindProductButtons(col)
}

// Scroll handler вынесен отдельно, чтобы его можно было удалить
function scrollHandler() {
    if ((window.innerHeight + window.scrollY) >= document.body.offsetHeight - 300) {
        loadMoreProducts();
    }
}

document.addEventListener("DOMContentLoaded", () => {
    loadMoreProducts();
    window.addEventListener('scroll', scrollHandler);
});