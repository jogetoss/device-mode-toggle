$(document).ready(function () {
    var menuId = "%s";
    var currentTheme = "%s";
    var desktopTheme = "%s";
    var mobileTheme = "%s";
    var contextPath = "%s";
    var appId = "%s";
    var appVersion = "%s";
    var desktopMessage = "@@org.joget.marketplace.viewmodetoggle.desktopToast@@";
    var mobileMessage = "@@org.joget.marketplace.viewmodetoggle.mobileToast@@";
    var errorMessage = "@@org.joget.marketplace.viewmodetoggle.errorMessage@@";

    $("#themeToggle").siblings(".toggle-group").click(function (e) {
        var toggle = $("#themeToggle").prop("checked");
        
        if (isMobile){
            if (localStorage.getItem("themeChangedManually") === 'true'){
                localStorage.removeItem("themeChangedManually");
            }else{
                localStorage.setItem("themeChangedManually", "true");
            }
        }
        
        if (toggle){
            toggleThemeSwitch(mobileTheme); 
        }
        else{
            toggleThemeSwitch(desktopTheme);
        }
    });

    function toggleThemeSwitch(theme) {
        if (theme === mobileTheme && (currentTheme !== mobileTheme || (mobileTheme === desktopTheme && !UI.userview_id.includes("_mobile")) )){
            if (currentTheme === "org.joget.marketplace.OnsenMobileTheme"){
                $("<div id='themeSwitchToast' style='visibility:hidden;opacity:0;z-index:1000;width:fit-content;min-width: 150px;box-sizing: border-box;height: 50px;position:fixed;top:15%%;left:50%%;transform:translate(-50%%, -50%%);background-color: #698a3a;color: #FFFFFF;display: flex;align-items: center;justify-content: center;border-radius: 2px;padding: 16px;text-align: center;'>" + mobileMessage + "</div>").appendTo("ons-splitter-content .page__content").css('visibility', 'visible').animate({opacity: 1}, 1000);
            }else{
                $("<div id='themeSwitchToast' style='visibility:hidden;opacity:0;z-index:1000;width:fit-content;min-width: 150px;box-sizing: border-box;height: 50px;position:fixed;top:10%%;left:50%%;transform:translate(-50%%, -50%%);background-color: #698a3a;color: #FFFFFF;display: flex;align-items: center;justify-content: center;border-radius: 2px;padding: 16px;text-align: center;'>" + mobileMessage + "</div>").appendTo("body").css('visibility', 'visible').animate({opacity: 1}, 1000);
            }

            setTimeout(function(){
                $("#themeSwitchToast").remove();
            }, 2000);

            $.ajax({
                url: contextPath + "/web/json/plugin/org.joget.marketplace.ViewModeToggle/service?switch_theme=" + theme + "&appId=" + appId + "&appVersion=" + appVersion + "&mobileTheme=true" + "&uId=" + UI.userview_id+"&menuId="+window.location.href.split('/').pop(),
                success: function(data) {
                    // Get the current URL
                    var currentUrl = window.location.href;
    
                    // Regular expression to match the appId/userviewId pattern
                    var regex = new RegExp(appId + "/" +UI.userview_id);

                    if (currentTheme === "org.joget.plugin.enterprise.XadminTheme") {
                        if ($("ul.layui-tab-title").children().length > 0) {
                            var tabId = $("ul.layui-tab-title > li.layui-this").attr("lay-id");
                            currentUrl = window.location.origin + $("div.layui-tab-content").find("iframe[tab-id='"+ tabId + "']").attr('src');
                        }
                    }

                    // Replace the matched part with the updated appId and userviewId
                    var newUrl = currentUrl.replace(regex, appId + "/" + data["uid"]);
    
                    window.location.href = newUrl;
                },
                error: function(data){
                    if (localStorage.getItem("themeChangedManually") === null){
                        toggleThemeSwitch(mobileTheme);
                    }

                    if (currentTheme === "org.joget.marketplace.OnsenMobileTheme"){
                        $("<div id='themeSwitchToast' style='visibility:hidden;opacity:0;z-index:1000;width:fit-content;min-width: 150px;box-sizing: border-box;height: 50px;position:fixed;top:15%%;left:50%%;transform:translate(-50%%, -50%%);background-color: #740112;color: #FFFFFF;display: flex;align-items: center;justify-content: center;border-radius: 2px;padding: 16px;text-align: center;'>" + errorMessage + "</div>").appendTo("ons-splitter-content .page__content").css('visibility', 'visible').animate({opacity: 1}, 1000);
                    }else{
                        $("<div id='themeSwitchToast' style='visibility:hidden;opacity:0;z-index:1000;width:fit-content;min-width: 150px;box-sizing: border-box;height: 50px;position:fixed;top:10%%;left:50%%;transform:translate(-50%%, -50%%);background-color: #740112;color: #FFFFFF;display: flex;align-items: center;justify-content: center;border-radius: 2px;padding: 16px;text-align: center;'>" + errorMessage + "</div>").appendTo("body").css('visibility', 'visible').animate({opacity: 1}, 1000);
                    }
        
                    setTimeout(function(){
                        $("#themeSwitchToast").remove();
                    }, 2000);
                }
            });
        }else if(theme === desktopTheme && currentTheme === mobileTheme){
            // Get the current URL
            var currentUrl = window.location.href;
    
            // Regular expression to match the appId/userviewId pattern
            var regex = new RegExp(appId + "/" +UI.userview_id); // Matches "/appId/anyUserviewId"

            if (currentTheme === "org.joget.plugin.enterprise.XadminTheme") {
                if ($("ul.layui-tab-title").children().length > 0) {
                    var tabId = $("ul.layui-tab-title > li.layui-this").attr("lay-id");
                    currentUrl = window.location.origin + $("div.layui-tab-content").find("iframe[tab-id='"+ tabId + "']").attr('src');
                }
            }

            // Replace the matched part with the updated appId and userviewId
            var newUrl = currentUrl.replace(regex, appId + "/" + UI.userview_id.replace("_mobile", ""));
            
            if (currentTheme === "org.joget.marketplace.OnsenMobileTheme"){
                $("<div id='themeSwitchToast' style='visibility:hidden;opacity:0;z-index:1000;width:fit-content;min-width: 150px;box-sizing: border-box;height: 50px;position:fixed;top:15%%;left:50%%;transform:translate(-50%%, -50%%);background-color: #698a3a;color: #FFFFFF;display: flex;align-items: center;justify-content: center;border-radius: 2px;padding: 16px;text-align: center;'>" + desktopMessage + "</div>").appendTo("ons-splitter-content .page__content").css('visibility', 'visible').animate({opacity: 1}, 1000);
            }else{
                $("<div id='themeSwitchToast' style='visibility:hidden;opacity:0;z-index:1000;width:fit-content;min-width: 150px;box-sizing: border-box;height: 50px;position:fixed;top:10%%;left:50%%;transform:translate(-50%%, -50%%);background-color: #698a3a;color: #FFFFFF;display: flex;align-items: center;justify-content: center;border-radius: 2px;padding: 16px;text-align: center;'>" + desktopMessage + "</div>").appendTo("body").css('visibility', 'visible').animate({opacity: 1}, 1000);
            }

            setTimeout(function(){
                $("#themeSwitchToast").remove();
            }, 2000);

            setTimeout(function(){
                window.location.href = newUrl;
            }, 3000);
        }
    }
    
    //Check if mobile or not, if it is, switch to mobile theme
    var isMobile = UI.isMobileUserAgent();
    
    if (isMobile && !UI.userview_id.includes("_mobile")){
        if (localStorage.getItem("themeChangedManually") === null){
            toggleThemeSwitch(mobileTheme);
        }
    }else if (!isMobile){
        $("li#"+menuId).hide();
        
        var visibleChildren = $("li#"+menuId).closest("ul.menu-container").children().filter(function() {
            var containsThemeToggle = $(this).find("input#themeToggle").length > 0;
            return containsThemeToggle;
        });

        var allChildrenHidden = visibleChildren.length === $("li#"+menuId).parent().children().length;
        
        if (allChildrenHidden){
            $("li#"+menuId).closest("li.category").hide();
        }
    }

    //Specfic for Onsen theme
    $(document).off('click', 'ons-list-item .menu-link').on('click', 'ons-list-item .menu-link', function(event) {
        if ($(event.target).find("#themeToggle").length === 1) {
            $(event.target).find("#themeToggle").siblings(".toggle-group").click();
        }
    });
    
    if (!isMobile && localStorage.getItem("themeChangedManually") === 'true'){
        localStorage.removeItem("themeChangedManually");
    }
    
    if(UI.userview_id.includes("_mobile")){
        $("#themeToggle").bootstrapToggle("off");
    }
});

