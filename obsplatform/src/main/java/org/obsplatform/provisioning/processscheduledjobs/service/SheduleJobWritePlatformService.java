package org.obsplatform.provisioning.processscheduledjobs.service;

public interface SheduleJobWritePlatformService {


	void processInvoice();
	
	void processRequest();
	
	void processSimulator();
	
	void generateStatment();
	
	void processingMessages();
	
	void processingAutoExipryOrders();
	
	void processNotify();
	
	void processMiddleware();
	
	void eventActionProcessor();
	
	void reportEmail();
	
	void reportStatmentPdf();
    
	void processExportData();

	void processPartnersCommission();

	void reProcessEventAction();

	void processAgingDistribution();

	//void processingDisconnectUnpaidCustomers();

	void processingCustomerUsageCharges();

	void evoBatchProcess();
	
	void evoBatchProcessDownload();
	
	void suspendOrders();

	void disconnectAllServicesOfUnpaidCustomers();
	
	void followUpTickets();
	
	void followupToOpen();

	//void processOSD();
	
	void worldpayRecurringBatchProcess();
	
	void logFilesRemoving();
}
