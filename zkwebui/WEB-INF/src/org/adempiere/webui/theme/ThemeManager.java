/******************************************************************************
 * Copyright (C) 2009 Low Heng Sin                                            *
 * Copyright (C) 2009 Idalica Corporation                                     *
 * This program is free software; you can redistribute it and/or modify it    *
 * under the terms version 2 of the GNU General Public License as published   *
 * by the Free Software Foundation. This program is distributed in the hope   *
 * that it will be useful, but WITHOUT ANY WARRANTY; without even the implied *
 * warranty of MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.           *
 * See the GNU General Public License for more details.                       *
 * You should have received a copy of the GNU General Public License along    *
 * with this program; if not, write to the Free Software Foundation, Inc.,    *
 * 59 Temple Place, Suite 330, Boston, MA 02111-1307 USA.                     *
 *****************************************************************************/
package org.adempiere.webui.theme;

import java.io.File;
import java.io.FileOutputStream;
import java.util.logging.Level;
import org.zkoss.zul.theme.Themes;
import org.adempiere.webui.AdempiereWebUI;
import org.compiere.model.MClient;
import org.compiere.model.MClientInfo;
import org.compiere.model.MImage;
import org.compiere.model.MSysConfig;
import org.compiere.util.CCache;
import org.compiere.util.CLogger;
import org.compiere.util.Env;
import org.zkoss.zk.ui.Executions;

/**
 *
 * @author hengsin
 *
 * Replaced by ThemeUtils.java, MTheme.java, DefaultTheme.java
 *
 */
@Deprecated
public final class ThemeManager {

    //--> Ashley
    /** Logo Cache           */
    private static CCache<String, String> logoCache = new CCache<String, String>("ZKLogo", 40, 0);
    /** Static Logger   */
    private static CLogger  logger   = CLogger.getCLogger(ThemeManager.class);
    
    private static String getLogo(int clientId, String type, String logoFile, String defaultValue)
    {
        MClientInfo clientInfo = MClientInfo.get(Env.getCtx(), clientId);
        int imageId = 0;
        String retLogoPath = defaultValue;
        
        if ("ZK_LOGO_LARGE".equals(type))
        {
            imageId = clientInfo.getLogoWeb_ID();
        }
        else if ("ZK_LOGO_SMALL".equals(type))
        {
            imageId = clientInfo.get_ValueAsInt("LogoWebHeader_ID");
        }
        else
        {
            return retLogoPath;
        }
        
        if (imageId <= 0)
        {
            if (clientId > 0)
            {
                // Return the System logo if configured
                return getLogo(0, type, logoFile, defaultValue);
            }
            else
            {
                return retLogoPath;
            }
        }
        
        try
        {
            MImage image = MImage.get(Env.getCtx(), imageId);
            String logoFilePath = Executions.getCurrent().getDesktop().getWebApp().getRealPath("") + File.separator + logoFile;
            FileOutputStream outStream = new FileOutputStream(logoFilePath);
            outStream.write(image.getBinaryData());
            outStream.close();
            retLogoPath = logoFile;
        }
        catch (Exception ex)
        {
            logger.log(Level.SEVERE, "Could not write logo file, using default", ex);
        }
        
        return retLogoPath;
    }
    
    private static String getLogo(String type, String defaultValue)
    {
        String key = MClient.get(Env.getCtx()).getValue();
        
        String logoFile = key + type + ".png";
        
        if (logoCache.get(logoFile) == null)
        {
            String loadedFile = getLogo(Env.getAD_Client_ID(Env.getCtx()), type, logoFile, defaultValue);
            logoCache.put(logoFile, loadedFile);
        }
        
        return logoCache.get(logoFile);
    }
    //<--

	/**
	 * @return url for large logo
	 * Use ThemeUtils.getLargeLogo()
	 */
	@Deprecated
	public static String getLargeLogo() {
		return ThemeUtils.getLargeLogo();
	}

	/**
	 * @return url for small logo
	 * Use ThemeUtils.getSmallLogo()
	 */
	@Deprecated
	public static String getSmallLogo() {
		return ThemeUtils.getSmallLogo();
	}

	/**
	 * @return name of active theme
	 * Use Themes.getCurrentTheme();
	 */
	@Deprecated
	public static String getTheme() {
		return Themes.getCurrentTheme();
	}

	/**
	 * @return url of theme stylesheet
	 * See the AdempiereThemeProvider.java and MThemeResources.java
	 */
	@Deprecated
	public static String getStyleSheet() {
		return DefaultTheme.ZK_DEFAULT_THEME_URI;
	}

	/**
	 * @return url of theme stylesheet by browser
	 * See the AdempiereThemeProvider.java and MThemeResources.java
	 */
	@Deprecated
	public static String getStyleSheetByBrowser() {
		return DefaultTheme.ZK_DEFAULT_THEME_URI;
	}

	/**
	 * @return title text for the browser window
	 * Replaced by ThemeUtils function;
	 */
	@Deprecated
	public static String getBrowserTitle() {
		return DefaultTheme.ZK_BROWSER_TITLE;
	}

	/**
	 * @return url for right panel
	 * Replaced by ThemeUtils function;
	 */
	@Deprecated
	public static String getLoginRightPanel() {
		return ThemeUtils.getLoginRightPanel();
	}

	/**
	 * @return url for left panel
	 * Replaced by ThemeUtils function;
	 */
	@Deprecated
	public static String getLoginLeftPanel() {
		return ThemeUtils.getLoginLeftPanel();
	}

	/**
	 * @return url for top panel
	 * Replaced by ThemeUtils function;
	 */
	@Deprecated
	public static String getLoginTopPanel() {
		return ThemeUtils.getLoginTopPanel();
	}

	/**
	 * @return url for bottom panel
	 * Replaced by ThemeUtils function;
	 */
	@Deprecated
	public static String getLoginBottomPanel() {
		return ThemeUtils.getLoginBottomPanel();
	}

	/**
	 * @return url for browser icon
	 * Replaced by ThemeUtils.getBrowserIcon();
	 */
	@Deprecated
	public static String getBrowserIcon() {
		return ThemeUtils.getBrowserIcon();
	}
//	public static String getThemeResource(String name) {
//		StringBuilder builder = new StringBuilder(ITheme.THEME_PATH_PREFIX);
//		builder.append(getTheme());
//		builder.append("/").append(name);
//		String url = builder.toString().intern();
//		return  url;
//	}
}
