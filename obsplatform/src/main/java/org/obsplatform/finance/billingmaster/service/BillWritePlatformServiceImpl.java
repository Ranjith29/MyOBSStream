package org.obsplatform.finance.billingmaster.service;

import java.io.File;
import java.math.BigDecimal;
import java.sql.Connection;
import java.sql.SQLException;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;

import net.sf.jasperreports.engine.JRException;
import net.sf.jasperreports.engine.JRParameter;
import net.sf.jasperreports.engine.JRRuntimeException;
import net.sf.jasperreports.engine.JasperExportManager;
import net.sf.jasperreports.engine.JasperFillManager;
import net.sf.jasperreports.engine.JasperPrint;

import org.apache.commons.lang.LocaleUtils;
import org.obsplatform.finance.billingmaster.domain.BillDetail;
import org.obsplatform.finance.billingmaster.domain.BillMaster;
import org.obsplatform.finance.billingmaster.domain.BillMasterRepository;
import org.obsplatform.finance.billingorder.exceptions.BillingOrderNoRecordsFoundException;
import org.obsplatform.infrastructure.configuration.domain.Configuration;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationConstants;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationRepository;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.domain.ObsPlatformTenant;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.infrastructure.core.service.FileUtils;
import org.obsplatform.infrastructure.core.service.RoutingDataSource;
import org.obsplatform.infrastructure.core.service.ThreadLocalContextUtil;
import org.obsplatform.organisation.message.domain.BillingMessage;
import org.obsplatform.organisation.message.domain.BillingMessageRepository;
import org.obsplatform.organisation.message.domain.BillingMessageTemplate;
import org.obsplatform.organisation.message.domain.BillingMessageTemplateConstants;
import org.obsplatform.organisation.message.domain.BillingMessageTemplateRepository;
import org.obsplatform.organisation.message.exception.BillingMessageTemplateNotFoundException;
import org.obsplatform.portfolio.client.domain.Client;
import org.obsplatform.portfolio.client.domain.ClientRepository;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;


/**
 * @author ranjith
 * 
 */
@Service
public class BillWritePlatformServiceImpl implements BillWritePlatformService {
	
	private final static Logger logger = LoggerFactory.getLogger(BillWritePlatformServiceImpl.class);
	
	private final BillMasterRepository billMasterRepository;
	private final RoutingDataSource dataSource;
    private final ClientRepository clientRepository;
    private final BillingMessageTemplateRepository messageTemplateRepository;
    private final BillingMessageRepository messageDataRepository;
    private final ConfigurationRepository configurationRepository;

	
	@Autowired
	public BillWritePlatformServiceImpl(final BillMasterRepository billMasterRepository,final RoutingDataSource dataSource,
			final ClientRepository clientRepository,final BillingMessageTemplateRepository messageTemplateRepository,
		    final BillingMessageRepository messageDataRepository,final ConfigurationRepository configurationRepository) {

		this.dataSource = dataSource;
		this.billMasterRepository = billMasterRepository;
		this.clientRepository = clientRepository;
		this.messageTemplateRepository = messageTemplateRepository;
		this.messageDataRepository = messageDataRepository;
		this.configurationRepository = configurationRepository;
		
	}

	@Override
	public CommandProcessingResult updateBillMaster(final List<BillDetail> billDetails, final BillMaster billMaster, final BigDecimal clientBalance) {
		
		try{
		BigDecimal chargeAmount = BigDecimal.ZERO;
		BigDecimal adjustmentAmount = BigDecimal.ZERO;
		BigDecimal paymentAmount = BigDecimal.ZERO;
		BigDecimal dueAmount = BigDecimal.ZERO;
		BigDecimal taxAmount = BigDecimal.ZERO;
		BigDecimal oneTimeSaleAmount = BigDecimal.ZERO;
		BigDecimal serviceTransferAmount =BigDecimal.ZERO;
		BigDecimal depositRefundAmount =BigDecimal.ZERO;
		
		for (final BillDetail billDetail : billDetails) {
			if ("SERVICE_CHARGES".equalsIgnoreCase(billDetail.getTransactionType()) 
					|| "REGISTRATION_FEE".equalsIgnoreCase(billDetail.getTransactionType())
					|| "TERMINATION_FEE".equalsIgnoreCase(billDetail.getTransactionType())
					|| "REACTIVATION_FEE".equalsIgnoreCase(billDetail.getTransactionType())
					|| "SETUP_FEE".equalsIgnoreCase(billDetail.getTransactionType())) {
				if (billDetail.getAmount() != null)
					chargeAmount = chargeAmount.add(billDetail.getAmount());
				
			} else if ("TAXES".equalsIgnoreCase(billDetail.getTransactionType())) {
				if (billDetail.getAmount() != null)
					taxAmount = taxAmount.add(billDetail.getAmount());

			} else if ("ADJUSTMENT".equalsIgnoreCase(billDetail.getTransactionType())) {
				if (billDetail.getAmount() != null)
					adjustmentAmount = adjustmentAmount.add(billDetail.getAmount());
				
			} else if (billDetail.getTransactionType().contains("PAYMENT")) {
				if (billDetail.getAmount() != null)
					paymentAmount = paymentAmount.add(billDetail.getAmount());

			} else if ("ONETIME_CHARGES".equalsIgnoreCase(billDetail.getTransactionType())) {
				if (billDetail.getAmount() != null)
					oneTimeSaleAmount = oneTimeSaleAmount.add(billDetail.getAmount());

			}else if ("SERVICE_TRANSFER".equalsIgnoreCase(billDetail.getTransactionType())) {
				if (billDetail.getAmount() != null)
					serviceTransferAmount = serviceTransferAmount.add(billDetail.getAmount());
					
			} else if ("DEPOSIT&REFUND".equalsIgnoreCase(billDetail.getTransactionType())) {
				if (billDetail.getAmount() != null)
					depositRefundAmount = depositRefundAmount.add(billDetail.getAmount());
				}
		}
	  dueAmount = chargeAmount.add(taxAmount).add(oneTimeSaleAmount).add(clientBalance).add(depositRefundAmount)
			      .add(serviceTransferAmount).subtract(paymentAmount).subtract(adjustmentAmount);
	  billMaster.setChargeAmount(chargeAmount.add(oneTimeSaleAmount).add(serviceTransferAmount));
	  billMaster.setAdjustmentAmount(adjustmentAmount);
	  billMaster.setTaxAmount(taxAmount);
	  billMaster.setPaidAmount(paymentAmount);
	  billMaster.setDueAmount(dueAmount);
	  billMaster.setPreviousBalance(clientBalance);
	  billMaster.setDepositRefundAmount(depositRefundAmount);
	  this.billMasterRepository.save(billMaster);
	  return new CommandProcessingResult(billMaster.getId(),billMaster.getClientId());
	}catch(DataIntegrityViolationException dve){
		logger.error("unable to retrieve data" + dve.getLocalizedMessage());
		return CommandProcessingResult.empty();
	}
}

	@Transactional
	@Override
	public String generateStatementPdf(final Long billId)  {
		
		Connection connection = null;
		try {
			BillMaster billMaster = this.billMasterRepository.findOne(billId);

			if ("invoice".equalsIgnoreCase(billMaster.getFileName())) {
				final String fileLocation = FileUtils.OBS_BASE_DIR;
				/** Recursively create the directory if it does not exist **/
				if (!new File(fileLocation).isDirectory()) {
					new File(fileLocation).mkdirs();
				}
				final String statementDetailsLocation = fileLocation+ File.separator +"StatementPdfFiles";
				if (!new File(statementDetailsLocation).isDirectory()) {
					new File(statementDetailsLocation).mkdirs();
				}
				final String printStatementLocation = statementDetailsLocation+ File.separator +billMaster.getClientId()+"_"+billId+"_"+DateUtils.getLocalDateOfTenant()+".pdf";
				final String jpath = fileLocation + File.separator + "jasper";
				final ObsPlatformTenant tenant = ThreadLocalContextUtil.getTenant();
				final String jfilepath = jpath + File.separator + "Statement_"+ tenant.getTenantIdentifier() + ".jasper";
				File destinationFile = new File(jfilepath);
				if (!destinationFile.exists()) {
					File sourceFile = new File(this.getClass().getClassLoader().getResource("Files/Statement.jasper").getFile());
					FileUtils.copyFileUsingApacheCommonsIO(sourceFile,destinationFile);
				}
				connection = this.dataSource.getConnection();
				Map<String, Object> parameters = new HashMap<String, Object>();
				final Integer id = Integer.valueOf(billMaster.getId().toString());
				parameters.put("param1", id);
				parameters.put("SUBREPORT_DIR", jpath + "" + File.separator);
				parameters.put(JRParameter.REPORT_LOCALE, getLocale(tenant)); 
				final JasperPrint jasperPrint = JasperFillManager.fillReport(jfilepath, parameters, connection);
				JasperExportManager.exportReportToPdfFile(jasperPrint,printStatementLocation);
				billMaster.setFileName(printStatementLocation);
				this.billMasterRepository.save(billMaster);
				logger.info("Filling report successfully...");
				
				final Configuration statementNotify=this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_STATEMENT_NOTIFY);
				if(statementNotify != null && statementNotify.isEnabled()){
					sendPdfToEmail(billMaster.getFileName(),billMaster.getClientId(),BillingMessageTemplateConstants.MESSAGE_TEMPLATE_STATEMENT);
				}
			}
			
			return billMaster.getFileName();
		} catch (final DataIntegrityViolationException ex) {

			logger.error("Filling report failed...\r\n" + ex.getLocalizedMessage());
			ex.printStackTrace();
			return null;

		} catch (final JRException | JRRuntimeException e) {

			logger.error("Filling report failed...\r\n" + e.getLocalizedMessage());
			e.printStackTrace();
			return null;

		} catch (final Exception e) {

			logger.error("Filling report failed...\r\n" + e.getLocalizedMessage());
			e.printStackTrace();
			return null;
		} finally {
			
		if(connection != null)	{
			try {
				connection.close();
			} catch (SQLException e) {
				e.printStackTrace();
			}
		 }
	   }
		
	}

	@Transactional
	@Override
	public String generateInovicePdf(final Long invoiceId) {
		
		final String fileLocation = FileUtils.OBS_BASE_DIR ;
		/** Recursively create the directory if it does not exist **/
		if (!new File(fileLocation).isDirectory()) {
			new File(fileLocation).mkdirs();
		}
		final String InvoiceDetailsLocation = fileLocation + File.separator +"InvoicePdfFiles";
		if (!new File(InvoiceDetailsLocation).isDirectory()) {
			 new File(InvoiceDetailsLocation).mkdirs();
		}
		final String printInvoiceLocation = InvoiceDetailsLocation +File.separator +invoiceId+"_"+DateUtils.getLocalDateOfTenant()+".pdf";
		final Integer id = Integer.valueOf(invoiceId.toString());
		Connection connection = null;
		try {
			
			final String jpath = fileLocation+File.separator+"jasper"; 
			final ObsPlatformTenant tenant = ThreadLocalContextUtil.getTenant();
			final String jasperfilepath =jpath+File.separator+"Invoicereport_"+tenant.getTenantIdentifier()+".jasper";
			File destinationFile=new File(jasperfilepath);
		      if(!destinationFile.exists()){
		    	File sourceFile=new File(this.getClass().getClassLoader().getResource("Files/Invoicereport.jasper").getFile());
		    	FileUtils.copyFileUsingApacheCommonsIO(sourceFile,destinationFile);
		       }
		    connection = this.dataSource.getConnection();
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("param1", id);
			parameters.put(JRParameter.REPORT_LOCALE, getLocale(tenant)); 
		   final JasperPrint jasperPrint = JasperFillManager.fillReport(jasperfilepath, parameters, connection);
		   JasperExportManager.exportReportToPdfFile(jasperPrint,printInvoiceLocation);
	       System.out.println("Filling report successfully...");
	       
		   }catch (final DataIntegrityViolationException ex) {
			 logger.error("Filling report failed...\r\n" + ex.getLocalizedMessage());
			 ex.printStackTrace();
		   } catch (final JRException  | JRRuntimeException e) {
			logger.error("Filling report failed...\r\n" + e.getLocalizedMessage());
		 	e.printStackTrace();
		  } catch (final Exception e) {
			logger.error("Filling report failed...\r\n" + e.getLocalizedMessage());
			e.printStackTrace();
		}finally{
			
			if(connection != null){
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
		}
		
		return printInvoiceLocation;	
   }
	
	@Transactional
	@Override
	public String generatePaymentPdf(final Long paymentId)  {
		
		final String fileLocation = FileUtils.OBS_BASE_DIR ;
		/** Recursively create the directory if it does not exist **/
		if (!new File(fileLocation).isDirectory()) {
			new File(fileLocation).mkdirs();
		}
		final String PaymentDetailsLocation = fileLocation + File.separator +"PaymentPdfFiles";
		if (!new File(PaymentDetailsLocation).isDirectory()) {
			 new File(PaymentDetailsLocation).mkdirs();
		}
		final String printPaymentLocation = PaymentDetailsLocation +File.separator +paymentId+"_"+DateUtils.getLocalDateOfTenant()+".pdf";
		final Integer id = Integer.valueOf(paymentId.toString());
		Connection connection = null;
		try {
			
			final String jpath = fileLocation+File.separator+"jasper"; 
			final ObsPlatformTenant tenant = ThreadLocalContextUtil.getTenant();
			final String jasperfilepath =jpath+File.separator+"Paymentreport_"+tenant.getTenantIdentifier()+".jasper";
			File destinationFile=new File(jasperfilepath);
		      if(!destinationFile.exists()){
		    	File sourceFile=new File(this.getClass().getClassLoader().getResource("Files/Paymentreport.jasper").getFile());
		    	FileUtils.copyFileUsingApacheCommonsIO(sourceFile,destinationFile);
		      }
			 connection = this.dataSource.getConnection();
			Map<String, Object> parameters = new HashMap<String, Object>();
			parameters.put("param1", id);
			parameters.put(JRParameter.REPORT_LOCALE, getLocale(tenant));
		   final JasperPrint jasperPrint = JasperFillManager.fillReport(jasperfilepath, parameters, connection);
		   JasperExportManager.exportReportToPdfFile(jasperPrint,printPaymentLocation);
	       System.out.println("Filling report successfully...");
	       
		   }catch (final DataIntegrityViolationException ex) {
			 logger.error("Filling report failed...\r\n" + ex.getLocalizedMessage());
			 ex.printStackTrace();
		   } catch (final JRException  | JRRuntimeException e) {
			logger.error("Filling report failed...\r\n" + e.getLocalizedMessage());
		 	e.printStackTrace();
		  } catch (final Exception e) {
			logger.error("Filling report failed...\r\n" + e.getLocalizedMessage());
			e.printStackTrace();
		}finally{
			
			if(connection != null){
				try {
					connection.close();
				} catch (SQLException e) {
					e.printStackTrace();
				}
			}
			
		}
		return printPaymentLocation;	
	}
	

	@Transactional
	@Override
	public void sendPdfToEmail(final String printFileName, final Long clientId,final String templateName) {
		
		//context.authenticatedUser();
		final Client client = this.clientRepository.findOne(clientId);
		final String clientEmail = client.getEmail();
		if(clientEmail == null){
			final String msg = "Please provide email first";
			throw new BillingOrderNoRecordsFoundException(msg, client);
		}
		final BillingMessageTemplate messageTemplate = this.messageTemplateRepository.findByTemplateDescription(templateName);
		if(messageTemplate !=null){
		  String header = messageTemplate.getHeader().replace("<customerName>", client.getDisplayName().isEmpty()?client.getFirstname():client.getDisplayName());
		  BillingMessage  billingMessage = new BillingMessage(header, messageTemplate.getBody(), messageTemplate.getFooter(), clientEmail, clientEmail, 
		    		messageTemplate.getSubject(), "N", messageTemplate, messageTemplate.getMessageType(), printFileName);
		    this.messageDataRepository.save(billingMessage);
	    }else{
	    	throw new BillingMessageTemplateNotFoundException(templateName);
	    }
	  }
	
	/**
	 * @param tenant
	 * @return Locale 
	 */
	 public Locale getLocale(ObsPlatformTenant tenant) {

		Locale locale = LocaleUtils.toLocale(tenant.getLocaleName());
		if (locale == null) {
			locale = Locale.getDefault();
		}
		return locale;
	}
	
	}

