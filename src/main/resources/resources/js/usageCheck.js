$(document).ready(function(){
    var themeToggles = $("li.menu").has("input#themeToggle");
    var liSelector = $("#%s");
    if (liSelector.length && themeToggles.length && liSelector[0] !== themeToggles.first()[0]) {
        liSelector.find("div.toggle").hide();
    }else{
        liSelector.find('div.toggle').prev('span').css({"padding-right": "10px"});
    }

    //Only in preview, update the themeTogglers
    //Only the first themeTogglers should be available to be used
    if (%b){
        liSelector.on("mousedown", function(e, data){
            isDragging = true;
            $(document).on('mouseup.themeMenuDrag', onMouseUp);
        })

        function onMouseUp(e) {
            themeToggles = $("li.menu").has("input#themeToggle");
            if (!isDragging) return;
            isDragging = false;
            $(document).off('mouseup.themeMenuDrag', onMouseUp);

            // Handle drop logic here
            if (liSelector.length && themeToggles.length && liSelector.find("input#themeToggle").css('display') === 'none' && liSelector[0] === themeToggles.first()[0]){
                liSelector.find('div.toggle').prev('span').css({"padding-right": "10px"});
                liSelector.find("div.toggle").show();
                
                var filteredToggles = themeToggles.filter(function() {
                    return $(this).find("div.toggle").is(":visible");
                });

                if (filteredToggles.length === 2){
                    filteredToggles.eq(1).find("div.toggle").hide();
                }

            }else if (liSelector.length && themeToggles.length && liSelector.find("div.toggle").css('display') !== 'none' && liSelector[0] !== themeToggles.first()[0]){
                liSelector.find("div.toggle").hide();
                themeToggles.eq(0).find('div.toggle').prev('span').css({"padding-right": "10px"});
                themeToggles.eq(0).find("div.toggle").show();
            }
        }
    }
})