package org.marketplace.themeSwitch;

import java.io.IOException;
import org.joget.apps.app.dao.UserviewDefinitionDao;
import org.joget.apps.app.model.AppDefinition;
import org.joget.apps.app.model.UserviewDefinition;
import org.joget.apps.app.service.AppPluginUtil;
import org.joget.apps.app.service.AppService;
import org.joget.apps.app.service.AppUtil;
import org.joget.apps.userview.model.UserviewCategory;
import org.joget.apps.userview.model.UserviewMenu;
import org.joget.apps.userview.model.UserviewSetting;
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

import java.util.Map;
import java.util.ResourceBundle;

public class ThemeSwitch extends UserviewMenu implements PluginWebSupport {
    private final static String MESSAGE_PATH = "messages/ThemeSwitch";

    public String getName() {
        return "Theme Switcher";
    }
    public String getVersion() {
        return "8.0.0";
    }
     
    public String getClassName() {
        return getClass().getName();
    }
     
    public String getLabel() {
        //support i18n
        return AppPluginUtil.getMessage("org.marketplace.ThemeSwitch.pluginLabel", getClassName(), MESSAGE_PATH);
    }
     
    public String getDescription() {
        //support i18n
        return AppPluginUtil.getMessage("org.marketplace.ThemeSwitch.pluginDesc", getClassName(), MESSAGE_PATH);
    }
  
    public String getPropertyOptions() {
        return AppUtil.readPluginResource(getClassName(), "/properties/themeSwitch.json", null, true, MESSAGE_PATH);
    }
    @Override
    public String getCategory() {
        return "Marketplace";
    }

    @Override
    public String getIcon() {
        return null;
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
            String label = this.getPropertyString("label");
            if (label != null) {
                label = StringUtil.stripHtmlRelaxed(label);
            }

            menu += "<a class='menu-link default'><span>" + label + "</span>\n" +
                    "  <input id=\"themeToggle\" data-on=\"<i class='fas fa-desktop'></i>\" data-off=\"<i class='fas fa-mobile-alt'></i>\" type=\"checkbox\" checked data-toggle=\"toggle\" data-style=\"ios\" data-size=\"mini\">\r\n" + //
                    "</a>\n";
            menu += "<script>" + AppUtil.readPluginResource(getClassName(), "/resources/js/bootstrap4-toggle.min.js", null, false , MESSAGE_PATH) + "</script>";
            menu += "<style>\n" + 
                    ".toggle.ios, .toggle-on.ios, .toggle-off.ios { border-radius: 20px; }.toggle.ios .toggle-handle { border-radius: 20px; }\n" +
                    AppUtil.readPluginResource(getClassName(), "/resources/css/bootstrap4-toggle.min.css", null, true , MESSAGE_PATH) +     
                    "\n</style>";
                    
            //Add the Script and Stylesheet
            if (!"true".equals(getRequestParameter("isPreview"))){
                String desktopTheme = "";
                String contextPath = AppUtil.getRequestContextPath(); 
                String appId = AppUtil.getCurrentAppDefinition().getAppId();
                String appVersion = AppUtil.getCurrentAppDefinition().getVersion().toString();

                Map<String, Object> themeProperties = (Map<String, Object>) properties.get("themeAlternative");
                UserviewSetting u = getUserview().getSetting();
                    
                Map<String, Object> themeProp = u.getProperties();
                themeProp = (Map<String, Object>) themeProp.get("theme");

                if (themeProperties != null && themeProperties.get("themeDesktop") == null){
                    desktopTheme = themeProp.get("className").toString();
                }
                else{
                    Map<String, Object> themeDesktopProperties = (Map<String, Object>) themeProperties.get("themeDesktop");
                    desktopTheme = themeDesktopProperties.get("className").toString();
                }

                Object[] arguments = {themeProp.get("className").toString(), desktopTheme, themeProperties.get("className"), contextPath, appId, appVersion};
                menu += "<script>" + AppUtil.readPluginResource(getClassName(), "/resources/js/themeSwitch.js", arguments, false , MESSAGE_PATH) + "</script>";                
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
        String action = SecurityUtil.validateStringInput(request.getParameter("switch_theme"));
        String action2 = SecurityUtil.validateStringInput(request.getParameter("revert_theme"));

        if (action != null && !action.isEmpty() || action2 != null && !action2.isEmpty()){
            // Set the response content type to JSON
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            String name = "";
            ApplicationContext ac = AppUtil.getApplicationContext();
            AppService appService = (AppService) ac.getBean("appService");
            WorkflowManager workflowManager = (WorkflowManager) ac.getBean("workflowManager");
            AppDefinition appDef = appService.getAppDefinition(request.getParameter("appId"), request.getParameter("appVersion"));
            
            UserviewDefinitionDao userviewDefinitionDao = (UserviewDefinitionDao) AppUtil.getApplicationContext().getBean("userviewDefinitionDao");
            UserviewDefinition userview = userviewDefinitionDao.loadById(request.getParameter("uId"), appDef);

            String json = userview.getJson();

            JSONObject userviewObj = new JSONObject(json);
            JSONObject settingObj = userviewObj.getJSONObject("setting");
            JSONObject themeObj = settingObj.getJSONObject("properties").getJSONObject("theme");
            JSONObject propertiesObj = settingObj.getJSONObject("properties");
            JSONObject themeAltObj = null;
            JSONArray categories = userviewObj.getJSONArray("categories");
            
            // Iterate through categories
            outerLoop:
            for (int i = 0; i < categories.length(); i++) {
                JSONObject category = categories.getJSONObject(i);
                JSONArray menus = category.getJSONArray("menus");
                
                // Iterate through menus
                for (int j = 0; j < menus.length(); j++) {
                    JSONObject menu = menus.getJSONObject(j);
                    
                    // Check if the className matches
                    if ("org.marketplace.themeSwitch.ThemeSwitch".equals(menu.getString("className"))) {
                        themeAltObj = menu.getJSONObject("properties").getJSONObject("themeAlternative");
                        break outerLoop;
                    }
                }
            }

            // Deep copy the theme object
            JSONObject themeCopy = new JSONObject(themeObj.toString());

            // Deep copy the themeAlternative object
            JSONObject themeAltCopy = new JSONObject(themeAltObj.toString());

            //Switch back to desktop theme
            if (action2 != null && themeAltCopy.has("themeDesktop")){
                JSONObject themeDesktop = new JSONObject(themeAltCopy.get("themeDesktop").toString());
                
                propertiesObj.put("theme", themeDesktop);
                settingObj.put("properties", propertiesObj);
                userviewObj.put("setting", settingObj);

                themeAltObj.remove("themeDesktop");
                
                userview.setJson(userviewObj.toString());
            }
            //Switch to mobile
            else if (themeAltObj != null && action != null) {
                propertiesObj.put("theme", themeAltCopy);
                themeAltObj.put("themeDesktop", themeCopy);

                settingObj.put("properties", propertiesObj);
                userviewObj.put("setting", settingObj);
                
                userview.setJson(userviewObj.toString());
            }

            String jsonResponse = "{\"success\": true}";

            // Write the response directly to the output stream
            response.getWriter().write(jsonResponse);
        }
    }
}
