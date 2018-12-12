package org.obsplatform.finance.paymentsgateway.data;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;

import org.obsplatform.finance.paymentsgateway.exception.PGPFIleProcessorException;
import org.obsplatform.infrastructure.core.service.FileUtils;
import org.obsplatform.infrastructure.jobs.service.JobName;

public class PGPSecurityFileProcessor {

	private String passphrase;

	private String keyFile;

	private String inputFile;

	private String outputFile;

	private boolean asciiArmored = false;

	private boolean integrityCheck = true;
	
	public static final String ENCRYPT = "encrypt";
	public static final String DECRYPT = "decrypt";
	
	private PGPSecurityFileProcessor(final String keyFile, final String inputFile, final String outputFile) {
		this.keyFile = keyFile;
		this.inputFile = inputFile;
		this.outputFile = outputFile;
	}
	private PGPSecurityFileProcessor(final String keyFile, final String inputFile, final String outputFile, final String passphrase) {
		this.keyFile = keyFile;
		this.inputFile = inputFile;
		this.outputFile = outputFile;
		this.passphrase = passphrase;
	}
	
	public static PGPSecurityFileProcessor intializePGPFP(String actionType, String inputFile, String outputFile, String passphrase) {
		
		actionType = actionType == null ? ENCRYPT : actionType;
		
		String keyFileName;
				
		if(actionType.equalsIgnoreCase(ENCRYPT)) {
			keyFileName = FileUtils.OBS_BASE_DIR  + File.separator + "publickey.asc";
			return new PGPSecurityFileProcessor(keyFileName, inputFile, outputFile);
		} else if (actionType.equalsIgnoreCase(DECRYPT)) {
			keyFileName = FileUtils.OBS_BASE_DIR  + File.separator + "privatekey.asc";
			return new PGPSecurityFileProcessor(keyFileName, inputFile, outputFile, passphrase);
		} else {
			throw new PGPFIleProcessorException();
		}
		
		//return new PGPSecurityFileProcessor(keyFileName, inputFile, outputFile);
	}

	public boolean encrypt() throws Exception {
		FileInputStream keyIn = new FileInputStream(keyFile);
        FileOutputStream out = new FileOutputStream(outputFile);
        PGPUtilDetails.encryptFile(out, inputFile, PGPUtilDetails.readPublicKey(keyIn),
        	asciiArmored, integrityCheck);
        out.close();
        keyIn.close();
        return true;
	}

	public boolean decrypt() throws Exception {
		 FileInputStream in = new FileInputStream(inputFile);
         FileInputStream keyIn = new FileInputStream(keyFile);
         FileOutputStream out = new FileOutputStream(outputFile);
         PGPUtilDetails.decryptFile(in, out, keyIn, passphrase.toCharArray());
         in.close();
         out.close();
         keyIn.close();
         return true;
	}

	public boolean isAsciiArmored() {
		return asciiArmored;
	}

	public void setAsciiArmored(boolean asciiArmored) {
		this.asciiArmored = asciiArmored;
	}

	public boolean isIntegrityCheck() {
		return integrityCheck;
	}

	public void setIntegrityCheck(boolean integrityCheck) {
		this.integrityCheck = integrityCheck;
	}

	public String getPassphrase() {
		return passphrase;
	}

	public void setPassphrase(String passphrase) {
		this.passphrase = passphrase;
	}

	public String getKeyFile() {
		return keyFile;
	}

	public void setKeyFile(String keyFile) {
		this.keyFile = keyFile;
	}

	public String getInputFile() {
		return inputFile;
	}

	public void setInputFile(String inputFile) {
		this.inputFile = inputFile;
	}

	public String getOutputFile() {
		return outputFile;
	}

	public void setOutputFile(String outputFile) {
		this.outputFile = outputFile;
	}

}


