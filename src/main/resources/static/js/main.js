/* =============================================
   SmakRoom — Main JavaScript
   ============================================= */

$(function () {

    // ---- CSRF setup for all AJAX requests ----
    var csrfToken  = $('meta[name="_csrf"]').attr('content');
    var csrfHeader = $('meta[name="_csrf_header"]').attr('content');

    $.ajaxSetup({
        beforeSend: function (xhr) {
            if (csrfToken && csrfHeader) {
                xhr.setRequestHeader(csrfHeader, csrfToken);
            }
        }
    });

    // ---- Cart badge on page load ----
    updateCartBadgeFromServer();

    // ---- Toast helper ----
    window.showToast = function (message, type) {
        type = type || 'success';
        var id = 'toast-' + Date.now();
        var html = '<div id="' + id + '" class="toast align-items-center text-bg-' + type +
                   ' border-0 position-fixed bottom-0 end-0 m-3" style="z-index:9999" role="alert">' +
                   '<div class="d-flex"><div class="toast-body">' + message + '</div>' +
                   '<button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast"></button>' +
                   '</div></div>';
        $('body').append(html);
        var toastEl = document.getElementById(id);
        var toast = new bootstrap.Toast(toastEl, { delay: 3000 });
        toast.show();
        toastEl.addEventListener('hidden.bs.toast', function () { $(this).remove(); });
    };

    // ---- Cart badge update ----
    window.updateCartBadge = function (count) {
        var badge = $('#cartBadge');
        if (count > 0) {
            badge.text(count).show();
        } else {
            badge.hide();
        }
    };

    function updateCartBadgeFromServer() {
        // We read badge count from a lightweight endpoint
        $.get('/cart/count', function (data) {
            updateCartBadge(data.count || 0);
        }).fail(function () { /* not logged in — ignore */ });
    }

    // ---- Auto-dismiss alerts after 5s ----
    setTimeout(function () {
        $('.alert-dismissible').alert('close');
    }, 5000);

});
