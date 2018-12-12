package org.obsplatform.finance.paymentsgateway.recurring.service;


public interface EvoRecurringBillingWritePlatformService {

	public void intializeFTP(String host, int port, String username, String privateKey, String SFTPWORKINGDIR);
	public void uploadFTPFile(String path, String fileName) throws Exception;
	public boolean downloadFTPFile(String fileName, String path);
	public void disconnect();
}
