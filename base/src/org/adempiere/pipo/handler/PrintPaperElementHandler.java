/**
 * 
 */
package org.adempiere.pipo.handler;

import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

import javax.xml.transform.sax.TransformerHandler;

import org.adempiere.pipo.AbstractElementHandler;
import org.adempiere.pipo.AttributeFiller;
import org.adempiere.pipo.Element;
import org.adempiere.pipo.PackOut;
import org.adempiere.pipo.exception.POSaveFailedException;
import org.compiere.model.I_AD_PrintPaper;
import org.compiere.model.X_AD_PrintPaper;
import org.compiere.util.Env;
import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.AttributesImpl;

/**
 * @author teo.sarca@gmail.com
 * 		<li>FR [ 2867966 ] Export PrintPaper
 * 			https://sourceforge.net/tracker/?func=detail&aid=2867966&group_id=176962&atid=879335
 */
public class PrintPaperElementHandler extends AbstractElementHandler
{
	public static final String TAG_Name = "printpaper";

	private final List<Integer> papers = new ArrayList<Integer>();
	
	protected String getTagName() {
		return TAG_Name;
	}

	public void startElement(Properties ctx, Element element) throws SAXException {
		String elementValue = element.getElementValue();
		Attributes atts = element.attributes;
		String uuid = getUUIDValue(atts, I_AD_PrintPaper.Table_Name);
		log.info(elementValue + " " + uuid);
		int id = getIdFromUUID(ctx, I_AD_PrintPaper.Table_Name, uuid);
		X_AD_PrintPaper printPaper = new X_AD_PrintPaper(ctx, id, getTrxName(ctx));
		if (id <= 0 && getIntValue(atts, I_AD_PrintPaper.COLUMNNAME_AD_PrintPaper_ID) > 0 && getIntValue(atts, I_AD_PrintPaper.COLUMNNAME_AD_PrintPaper_ID) <= PackOut.MAX_OFFICIAL_ID) {
			printPaper.setAD_PrintPaper_ID(getIntValue(atts, I_AD_PrintPaper.COLUMNNAME_AD_PrintPaper_ID));
			printPaper.setIsDirectLoad(true);
		}
		int backupId;
		String objectStatus;
		if (id > 0) {		
			backupId = copyRecord(ctx, I_AD_PrintPaper.Table_Name, printPaper);
			objectStatus = "Update";
		} else {
			objectStatus = "New";
			backupId = 0;
		}
		printPaper.setUUID(uuid);
		//	Standard Attributes
		printPaper.setCode(getStringValue(atts, I_AD_PrintPaper.COLUMNNAME_Code));
		printPaper.setName(getStringValue(atts, I_AD_PrintPaper.COLUMNNAME_Name));
		printPaper.setDescription(getStringValue(atts, I_AD_PrintPaper.COLUMNNAME_Description));
		printPaper.setDimensionUnits(getStringValue(atts, I_AD_PrintPaper.COLUMNNAME_DimensionUnits));
		printPaper.setIsActive(getBooleanValue(atts, I_AD_PrintPaper.COLUMNNAME_IsActive));
		printPaper.setIsDefault(getBooleanValue(atts, I_AD_PrintPaper.COLUMNNAME_IsDefault));
		printPaper.setIsLandscape(getBooleanValue(atts, I_AD_PrintPaper.COLUMNNAME_IsLandscape));
		printPaper.setMarginBottom(getIntValue(atts, I_AD_PrintPaper.COLUMNNAME_MarginBottom));
		printPaper.setMarginLeft(getIntValue(atts, I_AD_PrintPaper.COLUMNNAME_MarginLeft));
		printPaper.setMarginRight(getIntValue(atts, I_AD_PrintPaper.COLUMNNAME_MarginRight));
		printPaper.setMarginTop(getIntValue(atts, I_AD_PrintPaper.COLUMNNAME_MarginTop));
		printPaper.setSizeX(getBigDecimalValue(atts, I_AD_PrintPaper.COLUMNNAME_SizeX));
		printPaper.setSizeY(getBigDecimalValue(atts, I_AD_PrintPaper.COLUMNNAME_SizeY));
		//	Save
		try {
			printPaper.saveEx(getTrxName(ctx));
			recordLog(ctx, 0, printPaper.getUUID(), "PrintPaper",
					printPaper.get_ID(), backupId, objectStatus,
					"AD_PrintPaper", get_IDWithColumn(ctx, "AD_Table",
							"TableName", "AD_PrintPaper"));
		} catch (Exception e) {
			recordLog(ctx, 0, printPaper.getUUID(), "PrintPaper",
					printPaper.get_ID(), backupId, objectStatus,
					"AD_PrintPaper", get_IDWithColumn(ctx, "AD_Table",
							"TableName", "AD_PrintPaper"));
			throw new POSaveFailedException(e);
		}
		
	}

	public void endElement(Properties ctx, Element element) throws SAXException {
		
	}

	public void create(Properties ctx, TransformerHandler document) throws SAXException {
		int printPaperId = Env.getContextAsInt(ctx, X_AD_PrintPaper.COLUMNNAME_AD_PrintPaper_ID);
		if (papers.contains(printPaperId)) {
			return;
		}
		papers.add(printPaperId);
		X_AD_PrintPaper printPaper = new X_AD_PrintPaper(ctx, printPaperId, null);
		AttributesImpl atts = new AttributesImpl();
		createMessageBinding(atts, printPaper);	
		document.startElement("", "", getTagName(), atts);
		document.endElement("", "", getTagName());
	}

	private AttributesImpl createMessageBinding(AttributesImpl atts, X_AD_PrintPaper printPaper) {
		atts.clear();
		AttributeFiller filler = new AttributeFiller(atts, printPaper);
		if (printPaper.getAD_PrintPaper_ID() <= PackOut.MAX_OFFICIAL_ID) {
			filler.add(I_AD_PrintPaper.COLUMNNAME_AD_PrintPaper_ID);
		}
		filler.addUUID();
		//	Standard Attributes
		filler.add(I_AD_PrintPaper.COLUMNNAME_Code);
		filler.add(I_AD_PrintPaper.COLUMNNAME_Name);
		filler.add(I_AD_PrintPaper.COLUMNNAME_Description);
		filler.add(I_AD_PrintPaper.COLUMNNAME_DimensionUnits);
		filler.add(I_AD_PrintPaper.COLUMNNAME_IsActive);
		filler.add(I_AD_PrintPaper.COLUMNNAME_IsDefault);
		filler.add(I_AD_PrintPaper.COLUMNNAME_IsLandscape);
		filler.add(I_AD_PrintPaper.COLUMNNAME_MarginBottom);
		filler.add(I_AD_PrintPaper.COLUMNNAME_MarginLeft);
		filler.add(I_AD_PrintPaper.COLUMNNAME_MarginRight);
		filler.add(I_AD_PrintPaper.COLUMNNAME_MarginTop);
		filler.add(I_AD_PrintPaper.COLUMNNAME_SizeX);
		filler.add(I_AD_PrintPaper.COLUMNNAME_SizeY);
		return atts;
	}
}
