/* =============================================
   SmakRoom — Cart AJAX helpers
   ============================================= */

/**
 * Add a product to the cart via AJAX.
 * @param {number} productId
 * @param {number} quantity
 * @param {function} [onSuccess]
 */
function addToCart(productId, quantity, onSuccess) {
    quantity = quantity || 1;
    $.post('/cart/add', { productId: productId, quantity: quantity })
        .done(function (resp) {
            updateCartBadge(resp.count);
            showToast('<i class="bi bi-cart-check"></i> ' + (resp.message || 'Добавлено в корзину'), 'success');
            if (typeof onSuccess === 'function') onSuccess(resp);
        })
        .fail(function (xhr) {
            var msg = (xhr.responseJSON && xhr.responseJSON.message) ? xhr.responseJSON.message : 'Ошибка';
            showToast('<i class="bi bi-exclamation-triangle"></i> ' + msg, 'danger');
        });
}

// ---- Bind "Add to cart" buttons on any page ----
$(function () {
    $(document).on('click', '.add-to-cart', function (e) {
        e.preventDefault();
        var productId = $(this).data('product-id');
        var qty = 1;
        // If there's a qty input on the same page, use its value
        var qtyInput = $('#qty');
        if (qtyInput.length) {
            qty = parseInt(qtyInput.val()) || 1;
        }
        addToCart(productId, qty);
    });
});
