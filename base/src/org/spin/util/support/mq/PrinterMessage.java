package org.spin.util.support.mq;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.InputStream;

import org.adempiere.exceptions.AdempiereException;

public class PrinterMessage implements IMessageQueue {

	/**
	 * Constructor used for communicate file to print: the file must be PDF format
	 * @param fileToPrint
	 */
	public PrinterMessage(File fileToPrint) {
		//	Validate null file
		if(fileToPrint == null) {
			throw new AdempiereException("@File@ @NotFound@");
		}
		//	Convert File
		try {
			stream = new FileInputStream(fileToPrint);
		} catch (FileNotFoundException e) {
			throw new AdempiereException(e);
		}
	}
	
	private InputStream stream = null;
	
	@Override
	public int getType() {
		return IMessageQueue.FILE;
	}

	@Override
	public Object getMessage() {
		return null;
	}

	@Override
	public String getMessageAsText() {
		return null;
	}

	@Override
	public InputStream getMessageAsInputStream() {
		return stream;
	}

}
