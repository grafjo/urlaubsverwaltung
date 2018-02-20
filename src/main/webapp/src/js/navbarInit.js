$('.dropdown-toggle').on("click", function (event) {
    event.preventDefault();
    setTimeout($.proxy(function () {
        if ('ontouchstart' in document.documentElement) {
            $(this).siblings('.dropdown-backdrop').off().remove();
        }
    }, this), 0);
});