package org.obsplatform.finance.paymentsgateway.recurring.service;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;

import org.obsplatform.finance.paymentsgateway.recurring.domain.EvoBatchProcess;
import org.obsplatform.finance.paymentsgateway.recurring.domain.EvoBatchProcessRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import com.jcraft.jsch.Channel;
import com.jcraft.jsch.ChannelSftp;
import com.jcraft.jsch.JSch;
import com.jcraft.jsch.JSchException;
import com.jcraft.jsch.Session;
import com.jcraft.jsch.SftpException;

@Service
public class EvoRecurringBillingWritePlatformServiceImpl implements EvoRecurringBillingWritePlatformService {

	
    private final static Logger logger = LoggerFactory.getLogger(EvoRecurringBillingWritePlatformServiceImpl.class);
    
    private final EvoBatchProcessRepository evoBatchProcessRepository;
	
    private static JSch jSch = null;
    private static Session  session = null;
    private static Channel  channel = null;
    private static ChannelSftp channelSftp = null;
    
    @Autowired
    public EvoRecurringBillingWritePlatformServiceImpl(final EvoBatchProcessRepository evoBatchProcessRepository) {

		this.evoBatchProcessRepository = evoBatchProcessRepository;
		
	}

	@Override
	public void intializeFTP(String SFTPHOST, int SFTPPORT, String SFTPUSER, String privateKey, String SFTPWORKINGDIR)  {
		
		 
		 jSch = new JSch();
		
		try {
			jSch.addIdentity(privateKey);
			session = jSch.getSession(SFTPUSER, SFTPHOST, SFTPPORT);
			java.util.Properties config = new java.util.Properties();
			config.put("StrictHostKeyChecking", "no");
			session.setConfig(config);
			session.connect();
			channel = session.openChannel("sftp");
			channel.connect();
			channelSftp = (ChannelSftp) channel;
			channelSftp.cd(SFTPWORKINGDIR);
		} catch (JSchException e) {
			logger.error("Exception in Connecting to SFTP Server", e.getMessage());
			e.printStackTrace();
		} catch (SftpException e) {
			logger.error("Exception in Connecting to SFTP Server", e.getMessage());
			e.printStackTrace();
		}
		 
	}

	@Override
	public void uploadFTPFile(String localFileFullName, String fileName) throws Exception  {
		try {
			logger.info("EVO Batch process file uploads to SFTP .......");
			File f = new File(localFileFullName);
			channelSftp.put(new FileInputStream(f), f.getName());
			logger.info("EVO Batch process file uploads to SFTP Successfully .......");
			EvoBatchProcess evoBatchProcess = new EvoBatchProcess(f.getName(), localFileFullName, "File Uploaded Successfully");
			evoBatchProcess.isuploaded();
			this.evoBatchProcessRepository.save(evoBatchProcess);
			
		} catch (NullPointerException e) {
			logger.error("Exception in Uploading File to SFTP Server", e.getMessage());
			throw new Exception("Exception in Uploading File to SFTP Server");
		}
	}

	@Override
	public boolean downloadFTPFile(String destinationFileName, String destinationPath) {
		
		byte[] buffer = new byte[1024];
		BufferedInputStream bis = null;
		BufferedOutputStream bos = null;
		try(FileOutputStream fos = new FileOutputStream(new File(destinationPath))){
			bis = new BufferedInputStream(channelSftp.get(destinationFileName));
			bos = new BufferedOutputStream(fos);
			int readCount;
			while ((readCount = bis.read(buffer)) > 0) {
				bos.write(buffer, 0, readCount);
			}
			bis.close();
			bos.close();
			return true;
		} catch (FileNotFoundException e) {
			logger.error("FileNotFoundException in downloading File from SFTP Server", e.getMessage());
			return false;
		} catch (IOException e) {
			logger.error("IOException in downloading File from SFTP Server", e.getMessage());
			return false;
		}  catch (NullPointerException e) {
			logger.error("NullPointerException in downloading File from SFTP Server", e.getMessage());
			return false;
		} catch (SftpException e) {
			logger.error("SFTPException in downloading File from SFTP Server", e.getMessage());
			return false;
		} finally{
			if(bis!=null)
				try {
					bis.close();
				} catch (IOException e) {
					e.printStackTrace();
				}
			if(bos!=null)
				try {
					bos.close();
				} catch (IOException e) {
					e.printStackTrace();
				}	
		}
	}
	
	@Override
	public void disconnect() {

		if(channelSftp!=null){
		     channelSftp.disconnect();
		     channelSftp.exit();
		 }
		 if(channel!=null) channel.disconnect();
		  
		 if(session!=null) session.disconnect();
		}

	
	}
