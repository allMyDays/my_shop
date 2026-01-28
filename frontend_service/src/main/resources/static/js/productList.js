let offset = 0;
const limit = 40;
let loading = false;
let noMoreGoods=false;
let noSuchGoods=true;
let productsToProcess= new Set();


const categoryInput = document.getElementById("currentCategoryCode");
const categoryCode = categoryInput?categoryInput.value:null;

const filterInput = document.getElementById("currentFilter");
const filter = filterInput?filterInput.value:null;


function loadMoreProducts() {
    if (loading||noMoreGoods) return;
    loading = true;
    noMoreGoods=true;

    let currentOffset=offset;
    offset+=limit;

    const params = new URLSearchParams();

    if(filter) params.append("filter",filter);
    if (categoryCode) params.append("categoryCode", categoryCode);
    params.append("offset",currentOffset);

    let loadingIndicator = document.getElementById("products-loading-indicator");
    loadingIndicator.style.display = "";


    const eventSource = new EventSource("/products/lazy?"+params.toString());

    eventSource.onmessage = function (event) {
        const product = JSON.parse(event.data);
        appendProductCard(product);
        productsToProcess.add(product.id);
        noMoreGoods=false;
        noSuchGoods=false;
    };

    eventSource.onerror = function () {
        eventSource.close();
        startLoadRating(productsToProcess);
        productsToProcess.clear();
        loading = false;
        loadingIndicator.style.display = "none";
        if(noSuchGoods){
            document.getElementById("no-such-goods").style.display="";
        }
    };

    eventSource.onopen = function () {

    };

    eventSource.addEventListener("complete", () => {
        eventSource.close();
        startLoadRating(productsToProcess);
        productsToProcess.clear();
        loading = false;
        loadingIndicator.style.display = "none";
    });
}
function appendProductCard(product) {
    const container = document.getElementById("product-grid");

    const col = document.createElement("div");
    col.className = "col";

    col.innerHTML = `
    <div class="card h-100">
        <a href="/products/get/${product.id}" target="_blank">
            <img src="/api/media/get/${product.previewImageFileName}" 
                 class="card-img-top" 
                 alt="product_photo" 
                 width="200" 
                 height="370">
        </a>
        <div class="card-body d-flex flex-column">
            <h5 class="card-title">${product.priceView}</h5>
            <p class="card-text flex-grow-1">${product.title}</p> 
            
            <!-- Блок рейтинга и отзывов (изначально скрыт) -->
            <div class="product-rating-container" data-product-id="${product.id}" style="display: none;">
                <div class="rating-stars mb-1">
                    <div class="stars-wrapper">
                        ${Array(5).fill(0).map((_, i) => `
                            <i class="far fa-star" data-star-index="${i + 1}"></i>
                        `).join('')}
                    </div>
                    <div class="stars-active" style="width: 0%">
                        ${Array(5).fill(0).map((_, i) => `
                            <i class="fas fa-star" data-star-index="${i + 1}"></i>
                        `).join('')}
                    </div>
                </div>
                <span class="reviews-count text-muted small"></span>
            </div>

            <div class="d-flex justify-content-between mt-auto">
                ${userIsAuthenticated() ? `
                    <div>
                        <button class="btn btn-dark cart-btn" data-product-id="${product.id}">
                            В корзину <i class="fa-solid fa-cart-shopping"></i>
                        </button>
                        <button class="btn btn-light wish-btn" data-product-id="${product.id}">
                            <i class="fa-solid fa-heart"></i>
                        </button>
                    </div>
                ` : `
                    <a href="/users/welcome">
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