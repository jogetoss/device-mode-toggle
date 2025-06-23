package org.joget.marketplace;

import java.io.IOException;
import java.util.Map;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.joget.apps.app.dao.UserviewDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.UserviewDefinition;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.userview.model.UserviewMenu;
import org.joget.apps.userview.model.UserviewSetting;
import org.joget.apps.userview.service.UserviewService;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.StringUtil;
import org.joget.plugin.base.PluginWebSupport;
import org.json.JSONArray;
import org.json.JSONObject;
import org.springframework.context.ApplicationContext;

public class ViewModeToggle extends UserviewMenu implements PluginWebSupport {
    private final static String MESSAGE_PATH = "messages/ViewModeToggle";

    @Override
    public String getName() {
        return "View Mode Toggler";
    }
    @Override
    public String getVersion() {
        return "8.0.0";
    }
     
    @Override
    public String getClassName() {
        return getClass().getName();
    }
     
    @Override
    public String getLabel() {
        //support i18n
        return AppPluginUtil.getMessage("org.joget.marketplace.viewmodetoggle.pluginLabel", getClassName(), MESSAGE_PATH);
    }
     
    @Override
    public String getDescription() {
        //support i18n
        return AppPluginUtil.getMessage("org.joget.marketplace.viewmodetoggle.pluginDesc", getClassName(), MESSAGE_PATH);
    }
  
    @Override
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClassName(), "/properties/ViewModeToggle.json", null, true, MESSAGE_PATH);
    }
    @Override
    public String getCategory() {
        return "Marketplace";
    }

    @Override
    public String getIcon() {
        return "<i class=\"fas fa-toggle-on\"></i>";
    }

    @Override
    public boolean isHomePageSupported() {
        return false;
    }
     
     @Override
    public String getDecoratedMenu() {
        String menu = "";
       
        if (getPropertyString("themeAlternative") != null && !getPropertyString("themeAlternative").isEmpty())
        {

            Map<String, Object> themeProperties = (Map<String, Object>) properties.get("themeAlternative");

            boolean isPreview = "true".equals(getRequestParameter("isPreview"));
            String label = this.getPropertyString("label");
            if (label != null) {
                label = StringUtil.stripHtmlRelaxed(label);
            }

            menu += "<a class='menu-link default' style='display:flex;align-items:center;column-gap:8px'><span>" + label + "</span>\n" +
                    "<label class=\"toggle-theme-switch\" id=\"themeToggle\">\r\n" + //
                    "  <input type=\"checkbox\">\r\n" + //
                    "  <span class=\"slider\"></span>\r\n" + //
                    "</label>" +
                    "</a>\n";
            menu += "<script>" + AppUtil.readPluginResource(getClassName(), "/resources/js/bootstrap4-toggle.min.js", null, false , MESSAGE_PATH) + "</script>";
            menu += "<style>\n" + 
                    AppUtil.readPluginResource(getClassName(), "/resources/css/toggleTheme.css", null, true , MESSAGE_PATH) +
                    "\n</style>\n";
            String id = getPropertyString("id");
            menu += "<script>" + AppUtil.readPluginResource(getClassName(), "/resources/js/usageCheck.js", new Object[]{id, themeProperties.get("className"), isPreview}, false , MESSAGE_PATH) + "</script>\n";

            //Add the Script and Stylesheet
            if (!isPreview){
                String desktopTheme = "";
                String mobileTheme = "";
                String contextPath = AppUtil.getRequestContextPath(); 
                String appId = AppUtil.getCurrentAppDefinition().getAppId();
                String appVersion = AppUtil.getCurrentAppDefinition().getVersion().toString();

                UserviewSetting u = getUserview().getSetting();
                    
                Map<String, Object> themeProp = u.getProperties();
                themeProp = (Map<String, Object>) themeProp.get("theme");

                if (!u.getPropertyString("userviewId").contains("_mobile")){
                    desktopTheme = themeProp.get("className").toString();
                    mobileTheme = themeProperties.get("className").toString();
                }
                else{
                    desktopTheme = themeProperties.get("className").toString();
                    mobileTheme = themeProp.get("className").toString();
                }  

                Object[] arguments = {id, themeProp.get("className").toString(), desktopTheme, mobileTheme, contextPath, appId, appVersion};
                menu += "<script>" + AppUtil.readPluginResource(getClassName(), "/resources/js/ViewModeToggle.js", arguments, false , MESSAGE_PATH) + "</script>";                
            }

            return menu;
        }
        return null; 
    }
  
    @Override
    public String getRenderPage() {
        return null;
    }

    @Override
    public void webService(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String theme = SecurityUtil.validateStringInput(request.getParameter("switch_theme"));
        String isMobile = request.getParameter("mobileTheme");
        if(theme != null && !theme.isEmpty() && isMobile != null && !isMobile.isEmpty()){
            ApplicationContext ac = AppUtil.getApplicationContext();
            AppService appService = (AppService) ac.getBean("appService");
            AppDefinition appDef = appService.getAppDefinition(request.getParameter("appId"), request.getParameter("appVersion"));
            
            UserviewDefinitionDao userviewDefinitionDao = (UserviewDefinitionDao) AppUtil.getApplicationContext().getBean("userviewDefinitionDao");
            UserviewService userviewService = (UserviewService) AppUtil.getApplicationContext().getBean("userviewService");

            UserviewDefinition copy = userviewDefinitionDao.loadById(request.getParameter("uId"), appDef);
            copy = userviewService.combinedUserviewDefinition(copy, true);

            String json = copy.getJson();

            JSONObject userviewObj = new JSONObject(json);
            JSONObject propertiesObj = userviewObj.getJSONObject("properties");
            JSONObject settingObj = userviewObj.getJSONObject("setting");
            JSONObject settingPropertiesObj = settingObj.getJSONObject("properties");
            JSONObject themeAltObject = null;
            JSONObject themeAltPropObject = null;

            //Create mobile userview
            // Iterate through categories
            JSONArray categories = userviewObj.getJSONArray("categories");
            outerLoop:
            for (int i = 0; i < categories.length(); i++) {
                JSONObject category = categories.getJSONObject(i);
                JSONArray menus = category.getJSONArray("menus");
                
                // Iterate through menus
                for (int j = 0; j < menus.length(); j++) {
                    JSONObject menu = menus.getJSONObject(j);
                    
                    // Check if the className matches
                    if ("org.joget.marketplace.ViewModeToggle".equals(menu.getString("className"))) {
                        themeAltPropObject = menu.getJSONObject("properties");
                        themeAltObject = themeAltPropObject.getJSONObject("themeAlternative");
                        break outerLoop;
                    }
                }
            }

            //Replace the themeObj to the plugin's
            if (themeAltObject != null){
                settingPropertiesObj.put("userviewId", settingPropertiesObj.getString("userviewId")+"_mobile");    
                propertiesObj.put("id", settingPropertiesObj.getString("userviewId"));
                propertiesObj.put("hideThisUserviewInAppCenter", "true");

                UserviewDefinition uDef = new UserviewDefinition();
                uDef.setAppDefinition(appDef);
                uDef.setAppVersion(appDef.getVersion());
                
                uDef.setId(settingPropertiesObj.getString("userviewId"));     
                uDef.setName(settingPropertiesObj.getString("userviewId"));           

                themeAltPropObject.put("themeAlternative", settingPropertiesObj.getJSONObject("theme"));
                settingPropertiesObj.put("theme", themeAltObject);
                settingObj.put("properties", settingPropertiesObj);
                userviewObj.put("setting", settingObj);
                userviewObj.put("properties", propertiesObj);
                
                String userviewJson = userviewService.saveUserviewPages(userviewObj.toString(), uDef.getId(), appDef);
                //if does not exit, add
                if (userviewDefinitionDao.loadById(uDef.getId(), appDef) == null){
                    uDef.setJson(userviewJson);
                    userviewDefinitionDao.add(uDef);
                }else {//If exists, update
                    uDef.setJson(userviewJson);
                    userviewDefinitionDao.update(uDef);
                }
                response.setContentType("application/json;charset=UTF-8");
                JSONObject jsonResponse = new JSONObject();
                jsonResponse.put("json", uDef.getJson());
                jsonResponse.put("uid", propertiesObj.getString("id"));
                response.getWriter().write(jsonResponse.toString());
            }
        }
    }
}
