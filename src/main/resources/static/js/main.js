/**
 * CLOTHING SHOP - MAIN JAVASCRIPT
 * Đồ Án Đại Học Java Spring MVC
 */

// ==========================================
// 1. AUTO-DISMISS ALERTS
// ==========================================
document.addEventListener("DOMContentLoaded", function () {
    const alerts = document.querySelectorAll(".alert.alert-dismissible");
    alerts.forEach(function (alert) {
        setTimeout(function () {
            const bsAlert = bootstrap.Alert.getOrCreateInstance(alert);
            if (bsAlert) bsAlert.close();
        }, 4000);
    });
});

// ==========================================
// 2. CONFIRM DELETE DIALOGS
// ==========================================
function confirmDelete(message) {
    return confirm(message || "Bạn có chắc muốn xóa không?");
}

// ==========================================
// 3. QUANTITY CONTROLS
// ==========================================
document.addEventListener("DOMContentLoaded", function () {
    // Nút tăng
    document.querySelectorAll(".qty-plus").forEach(function (btn) {
        btn.addEventListener("click", function () {
            var input = btn.parentElement.querySelector(".qty-input");
            var max = parseInt(input.getAttribute("max") || "999");
            var val = parseInt(input.value) || 1;
            if (val < max) input.value = val + 1;
        });
    });

    // Nút giảm
    document.querySelectorAll(".qty-minus").forEach(function (btn) {
        btn.addEventListener("click", function () {
            var input = btn.parentElement.querySelector(".qty-input");
            var val = parseInt(input.value) || 1;
            if (val > 1) input.value = val - 1;
        });
    });
});

// ==========================================
// 4. IMAGE PREVIEW KHI CHỌN FILE
// ==========================================
function previewImage(inputEl, imgEl) {
    var input = typeof inputEl === "string" ? document.getElementById(inputEl) : inputEl;
    var img = typeof imgEl === "string" ? document.getElementById(imgEl) : imgEl;
    if (!input || !img) return;

    input.addEventListener("change", function () {
        if (input.files && input.files[0]) {
            var reader = new FileReader();
            reader.onload = function (e) {
                img.src = e.target.result;
                img.style.display = "block";
            };
            reader.readAsDataURL(input.files[0]);
        }
    });
}

// Gán tự động nếu tồn tại element mặc định
document.addEventListener("DOMContentLoaded", function () {
    previewImage("imageFile", "imagePreview");
});

// ==========================================
// 5. PASSWORD TOGGLE
// ==========================================
document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll(".toggle-password").forEach(function (btn) {
        btn.addEventListener("click", function () {
            var targetId = btn.getAttribute("data-target");
            var input = document.getElementById(targetId);
            if (!input) return;
            if (input.type === "password") {
                input.type = "text";
                btn.querySelector("i") && (btn.querySelector("i").className = "bi bi-eye-slash");
            } else {
                input.type = "password";
                btn.querySelector("i") && (btn.querySelector("i").className = "bi bi-eye");
            }
        });
    });
});

// ==========================================
// 6. TOAST NOTIFICATION HELPER
// ==========================================
function showToast(message, type) {
    type = type || "success";
    var toastContainer = document.getElementById("toastContainer");
    if (!toastContainer) {
        toastContainer = document.createElement("div");
        toastContainer.id = "toastContainer";
        toastContainer.style.cssText =
            "position:fixed;top:20px;right:20px;z-index:9999;min-width:250px;";
        document.body.appendChild(toastContainer);
    }

    var bg = type === "success" ? "bg-success" : type === "error" ? "bg-danger" : "bg-info";
    var toastEl = document.createElement("div");
    toastEl.className = "toast align-items-center text-white " + bg + " border-0 show mb-2";
    toastEl.setAttribute("role", "alert");
    toastEl.innerHTML =
        '<div class="d-flex">' +
        '  <div class="toast-body">' + message + "</div>" +
        '  <button type="button" class="btn-close btn-close-white me-2 m-auto"' +
        '    data-bs-dismiss="toast"></button>' +
        "</div>";

    toastContainer.appendChild(toastEl);
    setTimeout(function () {
        toastEl.remove();
    }, 4000);
}

// ==========================================
// 7. LOADING OVERLAY
// ==========================================
function showLoading() {
    var el = document.querySelector(".spinner-overlay");
    if (el) el.style.display = "flex";
}

function hideLoading() {
    var el = document.querySelector(".spinner-overlay");
    if (el) el.style.display = "none";
}

// Hiển thị loading khi submit form lớn (upload ảnh)
document.addEventListener("DOMContentLoaded", function () {
    document.querySelectorAll("form.form-with-loading").forEach(function (form) {
        form.addEventListener("submit", showLoading);
    });
});
