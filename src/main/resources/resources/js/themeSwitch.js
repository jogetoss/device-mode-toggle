var currentTheme = "%s"
var desktopTheme = "%s";
var mobileTheme = "%s";
var contextPath = "%s";
var appId = "%s";
var appVersion = "%s";
var desktopMessage = "@@org.marketplace.ThemeSwitch.desktopToast@@";
var mobileMessage = "@@org.marketplace.ThemeSwitch.mobileToast@@";

$(document).ready(function () {
    $("#themeToggle").siblings(".toggle-group").click(function (e) {
        var toggle = $("#themeToggle").prop("checked");
        var theme = desktopTheme;
        localStorage.setItem("themeChangedManually", "true");
        if (toggle){
            toggleThemeSwitch(theme); 
        }else{
            location.reload();
        }
    });

    function toggleThemeSwitch(theme) {
        $.ajax({
            url: contextPath + "/web/json/plugin/org.marketplace.themeSwitch.ThemeSwitch/service?switch_theme=" + theme + "&appId=" + appId + "&appVersion=" + appVersion + "&uId=" + UI.userview_id,
            success: function (data) {
                location.reload();
            }
        });
    }
    
    //Check if mobile or not, if it is, switch to mobile theme
    var isMobile = window.matchMedia("only screen and (max-width: 480px)").matches;
    if (isMobile && currentTheme !== mobileTheme){
        if (localStorage.getItem("themeChangedManually") === 'true'){
            localStorage.removeItem("themeChangedManually");
            showToast(desktopMessage);
        }else{
            localStorage.setItem("switchedAutomatically", "true");
            toggleThemeSwitch(mobileTheme);
        }
    }
    
    //If current theme is mobile theme, shot toast, and set the toggle to off
    if (currentTheme === mobileTheme){
        if (localStorage.getItem('switchedAutomatically')){
            localStorage.removeItem('switchedAutomatically');
            showToast(mobileMessage);
        }else if (localStorage.getItem("themeChangedManually") === 'true'){
            localStorage.removeItem("themeChangedManually");
            showToast(mobileMessage);
        }

        $('#themeToggle').bootstrapToggle("off");
    }else{
        if (localStorage.getItem('themeChangedManually')){
            localStorage.removeItem('themeChangedManually');
            showToast(desktopMessage);
        }
        $('#themeToggle').bootstrapToggle("on");
    }
    
    //Once page is loaded, send back to back end, so the UI theme is revert back
    //so that it won't use the mobile theme for the UI Builder setting
    if (currentTheme !== desktopTheme){
        $.ajax({
            url: contextPath + "/web/json/plugin/org.marketplace.themeSwitch.ThemeSwitch/service?revert_theme=" + desktopTheme + "&appId=" + appId + "&appVersion=" + appVersion + "&uId=" + UI.userview_id,
            success: function (data) {
            }
        });
    }
    
    //Specfic for Onsen theme
    $(document).off('click', 'ons-list-item .menu-link').on('click', 'ons-list-item .menu-link', function(event) {
        if ($(event.target).find("#themeToggle").length === 1) {
            $(event.target).find("#themeToggle").siblings(".toggle-group").click();
        }
    });

    //Currently the toast message only work for DX8 Theme, because,
    //non DX8's bootstrap does not have toast
    function showToast(message) {
        var toastHtml = $('<div class="toast-container position-fixed p-3" style="top: 10%%; left: 50%%; transform: translateX(-50%%);" id="themeToast"><div class="toast align-items-center text-bg-primary border-0" role="alert" aria-live="assertive" aria-atomic="true"><div class="d-flex"><div class="toast-body">' + message + '</div><button type="button" class="btn-close btn-close-white me-2 m-auto" data-bs-dismiss="toast" aria-label="Close" style="border:0;background-color:transparent;"><i class="zmdi zmdi-close"></i></button></div></div></div>');
    
        // Append the toast to the body (or any container)
        $('body').append(toastHtml);
    
        // Initialize the toast
        var options = {
            delay: 1000
        };
        $('#themeToast .toast').toast(options);
        $('#themeToast .toast').toast('show');
        $('#themeToast .toast button').off('click').on('click', function(){
            $(this).closest('#themeToast').remove();
        });
    
        setTimeout(function () {
            $('#themeToast').remove();
        }, 1100);
    }
    
})

