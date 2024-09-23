package org.joget.marketplace;

import java.io.IOException;
import org.joget.apps.app.dao.UserviewDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.UserviewDefinition;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.userview.model.Userview;
import org.joget.apps.userview.model.UserviewCategory;
import org.joget.apps.userview.model.UserviewMenu;
import org.joget.apps.userview.model.UserviewSetting;
import org.joget.apps.userview.service.UserviewService;
import org.joget.apps.userview.service.UserviewThemeProcesser;
import org.joget.apps.userview.service.UserviewUtil;
import org.joget.commons.util.ResourceBundleUtil;
import org.joget.commons.util.SecurityUtil;
import org.joget.commons.util.StringUtil;
import org.joget.plugin.base.PluginWebSupport;
import org.joget.workflow.model.service.WorkflowManager;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.json.JSONObject;
import org.json.JSONArray;
import org.springframework.context.ApplicationContext;

import java.util.HashMap;
import java.util.Map;
import java.util.ResourceBundle;

public class ViewModeToggle extends UserviewMenu implements PluginWebSupport {
    private final static String MESSAGE_PATH = "messages/ViewModeToggle";
    private String themeAltProperties = "";

    public String getName() {
        return "View Mode Toggler";
    }
    public String getVersion() {
        return "8.0.0";
    }
     
    public String getClassName() {
        return getClass().getName();
    }
     
    public String getLabel() {
        //support i18n
        return AppPluginUtil.getMessage("org.joget.marketplace.viewmodetoggle.pluginLabel", getClassName(), MESSAGE_PATH);
    }
     
    public String getDescription() {
        //support i18n
        return AppPluginUtil.getMessage("org.joget.marketplace.viewmodetoggle.pluginDesc", getClassName(), MESSAGE_PATH);
    }
  
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
            boolean isPreview = "true".equals(getRequestParameter("isPreview"));
            themeAltProperties = this.getPropertyString("themeAlternative");
            String label = this.getPropertyString("label");
            if (label != null) {
                label = StringUtil.stripHtmlRelaxed(label);
            }

            menu += "<a class='menu-link default'><span>" + label + "</span>\n" +
                    "  <input id=\"themeToggle\" data-width=\"35\" data-height=\"22\" data-on=\"<i class='fas fa-desktop'></i>\" data-size=\"mini\" data-offstyle=\"danger\" data-off=\"<i class='fas fa-mobile-alt'></i>\" type=\"checkbox\" checked data-toggle=\"toggle\" data-style=\"ios\">\r\n" + //
                    "</a>\n";
            menu += "<script>" + AppUtil.readPluginResource(getClassName(), "/resources/js/bootstrap4-toggle.min.js", null, false , MESSAGE_PATH) + "</script>";
            menu += "<style>\n" + 
                    AppUtil.readPluginResource(getClassName(), "/resources/css/rtl_style.css", null, true , MESSAGE_PATH) +     
                    AppUtil.readPluginResource(getClassName(), "/resources/css/bootstrap4-toggle.min.css", null, true , MESSAGE_PATH) +     
                    ".toggle-group label.toggle-on{\r\n" + //
                    "        right:50%;\r\n" + //
                    "        margin: 0px !important;" +
                    "    }\r\n" + //
                    "    .toggle-group label.toggle-off{\r\n" + //
                    "        left:55%;\r\n" + //
                    "        margin: 0px !important;" +
                    "    }\r\n" + //
                    "    .toggle-group span.toggle-handle{\r\n" + //
                    "        border-radius:90px !important;\r\n" + //
                    "        margin: 0px !important;" +
                    "    }\r\n" + //
                    "    .toggle-group label i{\r\n" + //
                    "        padding: 0px !important;" +
                    "    }\r\n" + //
                    "    .toggle.ios, .toggle-on.ios, .toggle-off.ios { border-radius: 20px !important; }" +
                    "\n</style>\n";
            String id = getPropertyString("id");
            menu += "<script>" + AppUtil.readPluginResource(getClassName(), "/resources/js/usageCheck.js", new Object[]{id, isPreview}, false , MESSAGE_PATH) + "</script>\n";

            //Add the Script and Stylesheet
            if (!isPreview){
                String desktopTheme = "";
                String mobileTheme = "";
                String contextPath = AppUtil.getRequestContextPath(); 
                String appId = AppUtil.getCurrentAppDefinition().getAppId();
                String appVersion = AppUtil.getCurrentAppDefinition().getVersion().toString();

                Map<String, Object> themeProperties = (Map<String, Object>) properties.get("themeAlternative");
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
            WorkflowManager workflowManager = (WorkflowManager) ac.getBean("workflowManager");
            AppDefinition appDef = appService.getAppDefinition(request.getParameter("appId"), request.getParameter("appVersion"));
            
            UserviewDefinitionDao userviewDefinitionDao = (UserviewDefinitionDao) AppUtil.getApplicationContext().getBean("userviewDefinitionDao");
            UserviewDefinition userview = userviewDefinitionDao.loadById(request.getParameter("uId"), appDef);
            UserviewService userviewService = (UserviewService) AppUtil.getApplicationContext().getBean("userviewService");

            UserviewDefinition copy = userviewDefinitionDao.loadById(request.getParameter("uId"), appDef);
            copy = userviewService.combinedUserviewDefinition(copy, true);

            String json = copy.getJson();

            JSONObject userviewObj = new JSONObject(json);
            JSONObject propertiesObj = userviewObj.getJSONObject("properties");
            JSONObject settingObj = userviewObj.getJSONObject("setting");
            JSONObject settingPropertiesObj = settingObj.getJSONObject("properties");
            Map<String, Object> prop = getProperties();
            JSONObject themeAltObject = null;
            JSONObject themeAltPropObject = null;

            //Create mobile App
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
