function startLoadRating(productsToProcess){

    if (productsToProcess.size>0){

        fetch('/api/review/get_products_info', {
            method: 'POST',
            headers: {
                'Content-Type': 'application/json',
            },
            body: JSON.stringify(Array.from(productsToProcess))
        })
            .then(response => {
                if (!response.ok) {
                    throw new Error(`HTTP error! status: ${response.status}`);
                }
                return response.json();
            })
            .then(data => {
                console.log('Product info:', data);

                // Обновляю рейтинги для каждого товара
                data.forEach(info => {
                    updateProductRating(info.productId, info.averageRating, info.reviewQuantity);
                });

                productsToProcess.clear();

            })
            .catch(error => {
                console.error('Request failed:', error);
            });
    }

}
// Функция для обновления рейтинга товара
function updateProductRating(productId, averageRating, reviewQuantity) {
    const ratingContainer = document.querySelector(`.product-rating-container[data-product-id="${productId}"]`);

    if (!ratingContainer) return;

    const starsActive = ratingContainer.querySelector('.stars-active');
    const reviewsCount = ratingContainer.querySelector('.reviews-count');

    // Рассчитываю ширину для активных звезд (0-100%)
    const ratingPercent = (averageRating / 5) * 100;
    starsActive.style.width = `${ratingPercent}%`;

    // Обновляю количество отзывов

    const quant = getReviewsText(reviewQuantity);

    const cont = document.getElementById("reviewsContainer");

    if(cont) reviewsCount.innerHTML = `<a href="#reviewsContainer">${quant}</a>`;
    else reviewsCount.innerHTML = `<h6>${quant}</h6>`;

    // Показываю блок рейтинга
    ratingContainer.style.display = 'block';
    setTimeout(()=>{
        ratingContainer.classList.add('show');
    },50);
}

// Функция для красивого отображения количества отзывов
function getReviewsText(count) {
    if (count === 0) return 'нет отзывов';
    if (count === 1) return '1 отзыв';
    if (count >= 2 && count <= 4) return `${count} отзыва`;
    return `${count} отзывов`;
}