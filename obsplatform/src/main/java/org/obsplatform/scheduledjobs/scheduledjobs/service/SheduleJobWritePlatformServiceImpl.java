package org.obsplatform.scheduledjobs.scheduledjobs.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.math.BigDecimal;
import java.math.MathContext;
import java.math.RoundingMode;
import java.net.URI;
import java.sql.Types;
import java.text.DateFormat;
import java.text.DecimalFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collection;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

import org.apache.activemq.broker.BrokerFactory;
import org.apache.activemq.broker.BrokerService;
import org.apache.commons.codec.binary.Base64;
import org.apache.http.HttpResponse;
import org.apache.http.client.HttpClient;
import org.apache.http.client.methods.HttpDelete;
import org.apache.http.client.methods.HttpGet;
import org.apache.http.client.methods.HttpPost;
import org.apache.http.entity.StringEntity;
import org.apache.http.impl.client.DefaultHttpClient;
import org.joda.time.LocalDate;
import org.joda.time.LocalTime;
import org.joda.time.format.DateTimeFormat;
import org.joda.time.format.DateTimeFormatter;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import org.obsplatform.crm.ticketmaster.api.TicketMasterApiResource;
import org.obsplatform.crm.ticketmaster.domain.TicketMaster;
import org.obsplatform.crm.ticketmaster.domain.TicketMasterRepository;
import org.obsplatform.crm.ticketmaster.service.TicketMasterReadPlatformService;
import org.obsplatform.finance.billingmaster.api.BillingMasterApiResourse;
import org.obsplatform.finance.billingorder.domain.Invoice;
import org.obsplatform.finance.billingorder.exceptions.BillingOrderNoRecordsFoundException;
import org.obsplatform.finance.billingorder.service.InvoiceClient;
import org.obsplatform.finance.paymentsgateway.data.PGPSecurityFileProcessor;
import org.obsplatform.finance.paymentsgateway.data.PaymentGatewayData;
import org.obsplatform.finance.paymentsgateway.domain.PaymentGateway;
import org.obsplatform.finance.paymentsgateway.domain.PaymentGatewayRepository;
import org.obsplatform.finance.paymentsgateway.recurring.data.EvoBatchProcessData;
import org.obsplatform.finance.paymentsgateway.recurring.domain.EvoBatchProcess;
import org.obsplatform.finance.paymentsgateway.recurring.domain.EvoBatchProcessRepository;
import org.obsplatform.finance.paymentsgateway.recurring.service.EvoRecurringBillingReadPlatformService;
import org.obsplatform.finance.paymentsgateway.recurring.service.EvoRecurringBillingWritePlatformService;
import org.obsplatform.finance.paymentsgateway.service.PaymentGatewayReadPlatformService;
import org.obsplatform.finance.paymentsgateway.service.PaymentGatewayWritePlatformService;
import org.obsplatform.finance.usagecharges.data.UsageChargesData;
import org.obsplatform.finance.usagecharges.service.UsageChargesWritePlatformService;
import org.obsplatform.infrastructure.configuration.domain.Configuration;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationConstants;
import org.obsplatform.infrastructure.configuration.domain.ConfigurationRepository;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.infrastructure.core.data.EnumOptionData;
import org.obsplatform.infrastructure.core.serialization.FromJsonHelper;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.infrastructure.core.service.FileUtils;
import org.obsplatform.infrastructure.core.service.RoutingDataSource;
import org.obsplatform.infrastructure.core.service.ThreadLocalContextUtil;
import org.obsplatform.infrastructure.dataqueries.service.ReadReportingService;
import org.obsplatform.infrastructure.jms.config.MessageConsumer;
import org.obsplatform.infrastructure.jms.config.MessageProducer;
import org.obsplatform.infrastructure.jobs.annotation.CronTarget;
import org.obsplatform.infrastructure.jobs.exception.JobParameterNotConfiguredException;
import org.obsplatform.infrastructure.jobs.service.JobName;
import org.obsplatform.infrastructure.jobs.service.RadiusJobConstants;
import org.obsplatform.infrastructure.security.service.PlatformSecurityContext;
import org.obsplatform.organisation.mcodevalues.api.CodeNameConstants;
import org.obsplatform.organisation.mcodevalues.data.MCodeData;
import org.obsplatform.organisation.mcodevalues.service.MCodeReadPlatformService;
import org.obsplatform.organisation.message.data.BillingMessageDataForProcessing;
import org.obsplatform.organisation.message.domain.BillingMessage;
import org.obsplatform.organisation.message.domain.BillingMessageRepository;
import org.obsplatform.organisation.message.domain.BillingMessageTemplate;
import org.obsplatform.organisation.message.domain.BillingMessageTemplateConstants;
import org.obsplatform.organisation.message.domain.BillingMessageTemplateRepository;
import org.obsplatform.organisation.message.service.BillingMessageDataWritePlatformService;
import org.obsplatform.organisation.message.service.BillingMesssageReadPlatformService;
import org.obsplatform.organisation.message.service.MessagePlatformEmailService;
import org.obsplatform.portfolio.client.data.ClientCardDetailsData;
import org.obsplatform.portfolio.client.domain.Client;
import org.obsplatform.portfolio.client.domain.ClientRepository;
import org.obsplatform.portfolio.client.exception.ClientNotFoundException;
import org.obsplatform.portfolio.client.service.ClientCardDetailsReadPlatformService;
import org.obsplatform.portfolio.order.data.OrderData;
import org.obsplatform.portfolio.order.domain.Order;
import org.obsplatform.portfolio.order.domain.OrderPrice;
import org.obsplatform.portfolio.order.domain.OrderRepository;
import org.obsplatform.portfolio.order.service.OrderAddOnsWritePlatformService;
import org.obsplatform.portfolio.order.service.OrderReadPlatformService;
import org.obsplatform.portfolio.order.service.OrderWritePlatformService;
import org.obsplatform.provisioning.entitlements.data.ClientEntitlementData;
import org.obsplatform.provisioning.entitlements.data.EntitlementsData;
import org.obsplatform.provisioning.entitlements.service.EntitlementReadPlatformService;
import org.obsplatform.provisioning.entitlements.service.EntitlementWritePlatformService;
import org.obsplatform.provisioning.preparerequest.data.PrepareRequestData;
import org.obsplatform.provisioning.preparerequest.service.PrepareRequestReadplatformService;
import org.obsplatform.provisioning.processrequest.data.ProcessingDetailsData;
import org.obsplatform.provisioning.processrequest.domain.ProcessRequest;
import org.obsplatform.provisioning.processrequest.domain.ProcessRequestRepository;
import org.obsplatform.provisioning.processrequest.service.ProcessRequestReadplatformService;
import org.obsplatform.provisioning.processrequest.service.ProcessRequestWriteplatformService;
import org.obsplatform.provisioning.processscheduledjobs.service.SheduleJobReadPlatformService;
import org.obsplatform.provisioning.processscheduledjobs.service.SheduleJobWritePlatformService;
import org.obsplatform.scheduledjobs.scheduledjobs.data.EventActionData;
import org.obsplatform.scheduledjobs.scheduledjobs.data.EvoBatchData;
import org.obsplatform.scheduledjobs.scheduledjobs.data.JobParameterData;
import org.obsplatform.scheduledjobs.scheduledjobs.data.ScheduleJobData;
import org.obsplatform.scheduledjobs.scheduledjobs.data.WorldpayBatchData;
import org.obsplatform.scheduledjobs.scheduledjobs.domain.BatchHistory;
import org.obsplatform.scheduledjobs.scheduledjobs.domain.BatchHistoryRepository;
import org.obsplatform.useradministration.domain.AppUser;
import org.obsplatform.workflow.eventaction.data.ActionDetaislData;
import org.obsplatform.workflow.eventaction.domain.EventAction;
import org.obsplatform.workflow.eventaction.domain.EventActionRepository;
import org.obsplatform.workflow.eventaction.service.ActionDetailsReadPlatformService;
import org.obsplatform.workflow.eventaction.service.ActiondetailsWritePlatformService;
import org.obsplatform.workflow.eventaction.service.EventActionConstants;
import org.obsplatform.workflow.eventaction.service.EventActionReadPlatformService;
import org.obsplatform.workflow.eventaction.service.ProcessEventActionService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataIntegrityViolationException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.namedparam.MapSqlParameterSource;
import org.springframework.jdbc.core.simple.SimpleJdbcCall;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Service;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonObject;

import me.legrange.mikrotik.ApiConnection;
import me.legrange.mikrotik.MikrotikApiException;

@SuppressWarnings("deprecation")
@Service	
public class SheduleJobWritePlatformServiceImpl implements SheduleJobWritePlatformService {
		
	private final static Logger logger = LoggerFactory.getLogger(SheduleJobWritePlatformServiceImpl.class);

	private final SheduleJobReadPlatformService sheduleJobReadPlatformService;
	private final InvoiceClient invoiceClient;
	private final BillingMasterApiResourse billingMasterApiResourse;
	private final FromJsonHelper fromApiJsonHelper;
	private final OrderReadPlatformService orderReadPlatformService;
	private final BillingMessageDataWritePlatformService billingMessageDataWritePlatformService;
	private final PrepareRequestReadplatformService prepareRequestReadplatformService;
	private final ProcessRequestReadplatformService processRequestReadplatformService;
	private final ProcessRequestWriteplatformService processRequestWriteplatformService;
	private final ProcessRequestRepository processRequestRepository;
	private final BillingMesssageReadPlatformService billingMesssageReadPlatformService;
	private final MessagePlatformEmailService messagePlatformEmailService;
	private final EntitlementReadPlatformService entitlementReadPlatformService;
	private final EntitlementWritePlatformService entitlementWritePlatformService;
	private final ActionDetailsReadPlatformService actionDetailsReadPlatformService;
	private final ProcessEventActionService actiondetailsWritePlatformService;
	private final ScheduleJobRunnerService scheduleJobRunnerService;
	private final ReadReportingService readExtraDataAndReportingService;
	private final TicketMasterApiResource ticketMasterApiResource;
	private final TicketMasterReadPlatformService ticketMasterReadPlatformService;
	private final OrderRepository orderRepository;
	private final MCodeReadPlatformService codeReadPlatformService;
	private final JdbcTemplate jdbcTemplate;
	private final OrderAddOnsWritePlatformService addOnsWritePlatformService;
	private String ReceiveMessage;
	private final PaymentGatewayRepository paymentGatewayRepository;
	private final PaymentGatewayWritePlatformService paymentGatewayWritePlatformService;
	private final EventActionRepository eventActionRepository;
	private final PaymentGatewayReadPlatformService paymentGatewayReadPlatformService;
	private final ConfigurationRepository configurationRepository;
	private final EventActionReadPlatformService eventActionReadPlatformService;
	private final PlatformSecurityContext context;
	private final BatchHistoryRepository batchHistoryRepository;
	private final UsageChargesWritePlatformService usageChargesWritePlatformService;
	private final ClientCardDetailsReadPlatformService clientCardDetailsReadPlatformService;
	private final EvoRecurringBillingReadPlatformService evoRecurringBillingReadPlatformService;
	private final EvoRecurringBillingWritePlatformService evoRecurringBillingWritePlatformService;
	private final EvoBatchProcessRepository evoBatchProcessRepository;
	private final ClientRepository clientRepository;
	private final BillingMessageTemplateRepository messageTemplateRepository;
	private final BillingMessageRepository messageDataRepository;
	private final ActiondetailsWritePlatformService actionDetailsWritePlatformService;
	private final TicketMasterRepository ticketMasterRepository;
	private final OrderWritePlatformService orderWritePlatformService;	

	private static final String DATEFORMAT = "yyyyMMdd";
	private static final String EVODATEFORMAT = "dd MMMM yyyy";
	
	
	private static final SimpleDateFormat FORMAT = new SimpleDateFormat(DATEFORMAT);
	private static final SimpleDateFormat EVOFORMAT = new SimpleDateFormat(EVODATEFORMAT);
	private static final MathContext mc = new MathContext(8, RoundingMode.HALF_EVEN);
	
	@Autowired
	private MessageProducer  messageProducer;
	
	@Autowired
	private MessageConsumer  messageConsumer;
	
	@Autowired
	public SheduleJobWritePlatformServiceImpl(final InvoiceClient invoiceClient, final FromJsonHelper fromApiJsonHelper,
			final BillingMasterApiResourse billingMasterApiResourse,
			final ProcessRequestRepository processRequestRepository,
			final SheduleJobReadPlatformService sheduleJobReadPlatformService,
			final OrderReadPlatformService orderReadPlatformService,
			final BillingMessageDataWritePlatformService billingMessageDataWritePlatformService,
			final ActionDetailsReadPlatformService actionDetailsReadPlatformService,
			final ProcessEventActionService actiondetailsWritePlatformService,
			final PrepareRequestReadplatformService prepareRequestReadplatformService,
			final ProcessRequestReadplatformService processRequestReadplatformService,
			final ProcessRequestWriteplatformService processRequestWriteplatformService,
			final BillingMesssageReadPlatformService billingMesssageReadPlatformService,
			final MessagePlatformEmailService messagePlatformEmailService, 
			final ScheduleJobRunnerService scheduleJobRunnerService,
			final EntitlementReadPlatformService entitlementReadPlatformService,
			final EntitlementWritePlatformService entitlementWritePlatformService,
			final ReadReportingService readExtraDataAndReportingService, final OrderRepository orderRepository,
			final TicketMasterApiResource ticketMasterApiResource,
			final TicketMasterReadPlatformService ticketMasterReadPlatformService,
			final MCodeReadPlatformService codeReadPlatformService, final RoutingDataSource dataSource,
			final PaymentGatewayRepository paymentGatewayRepository,
			final PaymentGatewayWritePlatformService paymentGatewayWritePlatformService,
			final EventActionRepository eventActionRepository,
			final PaymentGatewayReadPlatformService paymentGatewayReadPlatformService,
			final ConfigurationRepository configurationRepository,
			final EventActionReadPlatformService eventActionReadPlatformService,
			final OrderAddOnsWritePlatformService addOnsWritePlatformService,
			final PlatformSecurityContext context,final BatchHistoryRepository batchHistoryRepository,
			final UsageChargesWritePlatformService usageChargesWritePlatformService,
			final ClientCardDetailsReadPlatformService clientCardDetailsReadPlatformService,
			final EvoRecurringBillingReadPlatformService evoRecurringBillingReadPlatformService,
			final EvoRecurringBillingWritePlatformService evoRecurringBillingWritePlatformService,
			final EvoBatchProcessRepository evoBatchProcessRepository,
			final ClientRepository clientRepository,
			final BillingMessageTemplateRepository messageTemplateRepository,
			final BillingMessageRepository messageDataRepository,
			final ActiondetailsWritePlatformService actionDetailsWritePlatformService,
			final TicketMasterRepository ticketMasterRepository,
			final OrderWritePlatformService orderWritePlatformService) {

		this.sheduleJobReadPlatformService = sheduleJobReadPlatformService;
		this.invoiceClient = invoiceClient;
		this.billingMasterApiResourse = billingMasterApiResourse;
		this.fromApiJsonHelper = fromApiJsonHelper;
		this.orderReadPlatformService = orderReadPlatformService;
		this.billingMessageDataWritePlatformService = billingMessageDataWritePlatformService;
		this.prepareRequestReadplatformService = prepareRequestReadplatformService;
		this.processRequestReadplatformService = processRequestReadplatformService;
		this.processRequestWriteplatformService = processRequestWriteplatformService;
		this.processRequestRepository = processRequestRepository;
		this.billingMesssageReadPlatformService = billingMesssageReadPlatformService;
		this.messagePlatformEmailService = messagePlatformEmailService;
		this.entitlementReadPlatformService = entitlementReadPlatformService;
		this.addOnsWritePlatformService = addOnsWritePlatformService;
		this.entitlementWritePlatformService = entitlementWritePlatformService;
		this.actionDetailsReadPlatformService = actionDetailsReadPlatformService;
		this.actiondetailsWritePlatformService = actiondetailsWritePlatformService;
		this.scheduleJobRunnerService = scheduleJobRunnerService;
		this.orderRepository = orderRepository;
		this.readExtraDataAndReportingService = readExtraDataAndReportingService;
		this.ticketMasterApiResource = ticketMasterApiResource;
		this.ticketMasterReadPlatformService = ticketMasterReadPlatformService;
		this.codeReadPlatformService = codeReadPlatformService;
		this.jdbcTemplate = new JdbcTemplate(dataSource);
		this.paymentGatewayRepository = paymentGatewayRepository;
		this.paymentGatewayWritePlatformService = paymentGatewayWritePlatformService;
		this.eventActionRepository = eventActionRepository;
		this.paymentGatewayReadPlatformService = paymentGatewayReadPlatformService;
		this.configurationRepository = configurationRepository;
		this.eventActionReadPlatformService = eventActionReadPlatformService;
		this.context = context;
		this.batchHistoryRepository = batchHistoryRepository;
		this.usageChargesWritePlatformService = usageChargesWritePlatformService;
		this.clientCardDetailsReadPlatformService = clientCardDetailsReadPlatformService;
		this.evoRecurringBillingReadPlatformService = evoRecurringBillingReadPlatformService;
		this.evoRecurringBillingWritePlatformService = evoRecurringBillingWritePlatformService;
		this.evoBatchProcessRepository = evoBatchProcessRepository;
		this.clientRepository = clientRepository;
		this.messageTemplateRepository = messageTemplateRepository;
		this.messageDataRepository = messageDataRepository;
		this.actionDetailsWritePlatformService = actionDetailsWritePlatformService;
		this.ticketMasterRepository = ticketMasterRepository;
		this.orderWritePlatformService = orderWritePlatformService;
	}
	
	
	@Override
	@CronTarget(jobName = JobName.INVOICE)
	public void processInvoice() {

		try {
			logger.info("Invoicing the customers.......\r\n");
			JobParameterData data = this.sheduleJobReadPlatformService.getJobParameters(JobName.INVOICE.toString());
			
			if (data != null) {
				final String path = FileUtils.generateLogFileDirectory()+JobName.INVOICE.toString()+File.separator
						     +"Invoice_"+DateUtils.getLocalDateTimeOfTenant().toString()+".log";
				File fileHandler = new File(path.trim());
				fileHandler.createNewFile();
				FileWriter fw = new FileWriter(fileHandler);
				FileUtils.BILLING_JOB_PATH = fileHandler.getAbsolutePath();
				
				List<ScheduleJobData> sheduleDatas = this.sheduleJobReadPlatformService.retrieveSheduleJobParameterDetails(data.getBatchName());

				if (!sheduleDatas.isEmpty()) {
					for (ScheduleJobData scheduleJobData : sheduleDatas) {
						fw.append("ScheduleJobData id= " + scheduleJobData.getId() + " ,BatchName= " + scheduleJobData.getBatchName() + " ,query=" + scheduleJobData.getQuery() + "\r\n");
						List<Long> clientIds = this.sheduleJobReadPlatformService.getClientIds(scheduleJobData.getQuery(), data);
						if (!clientIds.isEmpty()) {
							fw.append("Invoicing the customers..... \r\n");
							for (Long clientId : clientIds) {
								try {
									if ("Y".equalsIgnoreCase(data.isDynamic())) {
										Invoice invoice = this.invoiceClient.invoicingSingleClient(clientId, DateUtils.getLocalDateOfTenant());
										fw.append("ClientId: " + clientId + "\tAmount: " + invoice.getInvoiceAmount().toString() + "\r\n");

									} else {
										Invoice invoice = this.invoiceClient.invoicingSingleClient(clientId, data.getProcessDate());
										fw.append("ClientId: " + clientId + "\tAmount: " + invoice.getInvoiceAmount().toString() + "\r\n");
									}
								} catch (Exception dve) {
									handleCodeDataIntegrityIssues(null, dve);
								}
							}
						} else {
							fw.append("Invoicing clients are not found \r\n");
						}
					}
				} else {
					fw.append("ScheduleJobData Empty \r\n");
				}
				fw.append("Invoices are Generated....." + ThreadLocalContextUtil.getTenant().getTenantIdentifier() + "\r\n");
				fw.flush();
				fw.close();
			}
			logger.info("Invoices are Generated....." + ThreadLocalContextUtil.getTenant().getTenantIdentifier());
		} catch (DataIntegrityViolationException exception) {
			logger.error(exception.getMessage());
			exception.printStackTrace();
		} catch (Exception exception) {
			logger.error(exception.getMessage());
			exception.printStackTrace();
		}
	}
	
	private void handleCodeDataIntegrityIssues(Object object, Exception dve) {
		
	}

	@Override
	@CronTarget(jobName = JobName.REQUESTOR)
	public void processRequest() {

		try {
			logger.info("Processing Request Details.......");
			List<PrepareRequestData> data = this.prepareRequestReadplatformService.retrieveDataForProcessing();

			if (!data.isEmpty()) {
				final String path = FileUtils.generateLogFileDirectory()+JobName.REQUESTOR.toString()+File.separator
						+ "Requester_"+ DateUtils.getLocalDateTimeOfTenant().toString()+".log";
				File fileHandler = new File(path.trim());
				fileHandler.createNewFile();
				FileWriter fw = new FileWriter(fileHandler);
				FileUtils.BILLING_JOB_PATH = fileHandler.getAbsolutePath();
				fw.append("Processing Request Details....... \r\n");
				
				for (PrepareRequestData requestData : data) {

					fw.append("Prepare Request id=" + requestData.getRequestId() + " ,clientId="+requestData.getClientId()+ " ,orderId=" + requestData.getOrderId() 
							+ " ,HardwareId="+ requestData.getHardwareId() + " ,planName=" + requestData.getPlanName()+ " ,Provisiong system=" + requestData.getProvisioningSystem() + "\r\n");

					this.prepareRequestReadplatformService.processingClientDetails(requestData);
				}

				fw.append(" Requestor Job is Completed...." + ThreadLocalContextUtil.getTenant().getTenantIdentifier() + "\r\n");
				fw.flush();
				fw.close();
			}

			logger.info(" Requestor Job is Completed...." + ThreadLocalContextUtil.getTenant().getTenantIdentifier());
		} catch (Exception exception) {
			logger.error(exception.getMessage());
			exception.printStackTrace();
		}
	}

	@Override
	@CronTarget(jobName = JobName.SIMULATOR)
	public void processSimulator() {
		
		try {
			logger.info("Processing Simulator Details.......");
			JobParameterData data = this.sheduleJobReadPlatformService.getJobParameters(JobName.SIMULATOR.toString());
			
			if (data != null) {
				List<ProcessingDetailsData> processingDetails = this.processRequestReadplatformService.retrieveUnProcessingDetails();
				if (data.getUpdateStatus().equalsIgnoreCase("Y")) {
					if (!processingDetails.isEmpty()) {
						final String path = FileUtils.generateLogFileDirectory()+JobName.SIMULATOR.toString()+File.separator
							     +"Simulator_"+DateUtils.getLocalDateTimeOfTenant().toString()+".log";
					    File fileHandler = new File(path.trim());
					    fileHandler.createNewFile();
				 	    FileWriter fw = new FileWriter(fileHandler);
					    FileUtils.BILLING_JOB_PATH = fileHandler.getAbsolutePath();
						fw.append("Processing Simulator Details....... \r\n");

						for (ProcessingDetailsData detailsData : processingDetails) {
							fw.append("simulator Process Request id=" + detailsData.getId() + " ,orderId="+ detailsData.getOrderId() + 
									" ,Provisiong System="+ detailsData.getProvisionigSystem() + " ,RequestType="+ detailsData.getRequestType() + "\r\n");
							ProcessRequest processRequest = this.processRequestRepository.findOne(detailsData.getId());
							processRequest.setProcessStatus('Y');
							this.processRequestRepository.saveAndFlush(processRequest);
							this.processRequestWriteplatformService.notifyProcessingDetails(processRequest, 'Y');
						}
						fw.append("Simulator Job is Completed..." + ThreadLocalContextUtil.getTenant().getTenantIdentifier() + " \r\n");
						fw.flush();
						fw.close();
					}
				}
				if (data.getcreateTicket().equalsIgnoreCase("Y")) {

					for (ProcessingDetailsData detailsData : processingDetails) {
						ProcessRequest processRequest = this.processRequestRepository.findOne(detailsData.getId());
						Order order = this.orderRepository.findOne(processRequest.getOrderId());
						Collection<MCodeData> problemsData = this.codeReadPlatformService.getCodeValue(CodeNameConstants.CODE_PROBLEM_CODE);
						List<EnumOptionData> priorityData = this.ticketMasterReadPlatformService.retrievePriorityData();
						Long userId = 0L;
						JSONObject jsonobject = new JSONObject();
						DateTimeFormatter formatter1 = DateTimeFormat.forPattern("dd MMMM yyyy");
						DateTimeFormatter formatter2 = DateTimeFormat.fullTime();
						jsonobject.put("locale", "en");
						jsonobject.put("dateFormat", "dd MMMM yyyy");
						jsonobject.put("ticketTime", " " + new LocalTime().toString(formatter2));
						if (order != null) {
							jsonobject.put("description", "ClientId" + processRequest.getClientId() + " Order No:" + order.getOrderNo() 
							+ " Request Type:" + processRequest.getRequestType() + " Generated at:" + new LocalTime().toString(formatter2));
						}
						jsonobject.put("ticketDate", formatter1.print(DateUtils.getLocalDateOfTenant()));
						jsonobject.put("sourceOfTicket", "Phone");
						jsonobject.put("assignedTo", userId);
						jsonobject.put("priority", priorityData.get(0).getValue());
						jsonobject.put("problemCode", problemsData.iterator().next().getId());
						this.ticketMasterApiResource.processCreateTicket(processRequest.getClientId(), jsonobject.toString());
					}
				}
			}
			logger.info("Simulator Job is Completed..." + ThreadLocalContextUtil.getTenant().getTenantIdentifier());
		} catch (final DataIntegrityViolationException exception) {
			logger.info(exception.getMessage());
		} catch (Exception exception) {
			logger.info(exception.getMessage());
			exception.printStackTrace();
		}
	}
	
	@Override
	@CronTarget(jobName = JobName.EVENT_ACTION_PROCESSOR)
	public void eventActionProcessor() {
		
		try {
			logger.info("Processing Event Actions.....\r\n");
			final String path = FileUtils.generateLogFileDirectory()+JobName.EVENT_ACTION_PROCESSOR.toString()+File.separator
				     +"EventActions_"+DateUtils.getLocalDateTimeOfTenant().toString()+".log";
		    File fileHandler = new File(path.trim());
		    fileHandler.createNewFile();
		    FileWriter fw = new FileWriter(fileHandler);
		    FileUtils.BILLING_JOB_PATH = fileHandler.getAbsolutePath();

			List<EventActionData> actionDatas = this.actionDetailsReadPlatformService.retrieveAllActionsForProccessing();

			for (EventActionData eventActionData : actionDatas) {
				fw.append("Process Response id=" + eventActionData.getId() + " ,orderId=" + eventActionData.getOrderId()
						+ " ,Provisiong System=" + eventActionData.getActionName() + " \r\n");
				logger.info("Processing Event :"+eventActionData.getId()+"  orderId :"+eventActionData.getOrderId());
				this.actiondetailsWritePlatformService.processEventActions(eventActionData);
			}

			logger.info("Event Actions are Processed....");
			fw.append("Event Actions are Completed.... \r\n");
			fw.flush();
			fw.close();
		} catch (DataIntegrityViolationException e) {
			logger.error(e.getMessage());
		} catch (Exception exception) {
			logger.error(exception.getMessage());
			exception.printStackTrace();
		}
	}

	@Override
	@CronTarget(jobName = JobName.GENERATE_STATEMENT)
	public void generateStatment() {

		try {
			logger.info("Processing statement Details.......");
			JobParameterData data = this.sheduleJobReadPlatformService.getJobParameters(JobName.GENERATE_STATEMENT.toString());

			if (data != null) {
				final String path = FileUtils.generateLogFileDirectory()+JobName.GENERATE_STATEMENT.toString()+File.separator
					     +"Statement_"+DateUtils.getLocalDateTimeOfTenant().toString()+".log";
			    File fileHandler = new File(path.trim());
			    fileHandler.createNewFile();
			    FileWriter fw = new FileWriter(fileHandler);
			    FileUtils.BILLING_JOB_PATH = fileHandler.getAbsolutePath();
				fw.append("Processing statement Details....... \r\n");
				
				List<ScheduleJobData> sheduleDatas = this.sheduleJobReadPlatformService.retrieveSheduleJobParameterDetails(data.getBatchName());

				if (!sheduleDatas.isEmpty()) {
					for (ScheduleJobData scheduleJobData : sheduleDatas) {
						fw.append("ScheduleJobData id= " + scheduleJobData.getId() + " ,BatchName= " + scheduleJobData.getBatchName() + " ,query=" + scheduleJobData.getQuery() + "\r\n");
						List<Long> clientIds = this.sheduleJobReadPlatformService.getClientIds(scheduleJobData.getQuery(), data);
						if (!clientIds.isEmpty()) {
							String batchId=this.generateBatchId();
							Integer count=0;
							String formattedDate;
							fw.append("Generate New Statements to the customers..... \r\n");
							DateTimeFormatter dateTimeformatter = DateTimeFormat.forPattern("dd MMMM yyyy");
							if ("Y".equalsIgnoreCase(data.isDynamic())) {
								formattedDate = dateTimeformatter.print(DateUtils.getLocalDateOfTenant().plusDays(6));
							} else {
								formattedDate = dateTimeformatter.print(data.getDueDate());
							}
							JSONObject statementJson = new JSONObject();
							statementJson.put("dueDate", formattedDate);
							statementJson.put("locale", "en");
							statementJson.put("dateFormat", "dd MMMM YYYY");
							statementJson.put("message", data.getPromotionalMessage());
							statementJson.put("batchId", batchId);
							fw.append("sending jsonData for Statement Generation is: "+ statementJson.toString()+ " . \r\n");
							for (Long clientId : clientIds) {
								try {
									fw.append("processing customer: "+ clientId + " \r\n");
									this.billingMasterApiResourse.generateBillStatement(clientId,statementJson.toString());
								} catch (BillingOrderNoRecordsFoundException e) {
									e.getMessage();
								}
								count++;
				           }
						BatchHistory batch = new BatchHistory(DateUtils.getDateOfTenant(),"generate statement", count.toString(), batchId);
						this.batchHistoryRepository.saveAndFlush(batch);
						
					  }else{
						fw.append("no records are available for statement generation \r\n");
					  }
                   }
		    	}else{
				fw.append("ScheduleJobData Empty \r\n");
			}
				fw.append("statement Job is Completed..."+ ThreadLocalContextUtil.getTenant().getTenantIdentifier()+" . \r\n");
				fw.flush();
				fw.close();
			}
			logger.info("statement Job is Completed..." + ThreadLocalContextUtil.getTenant().getTenantIdentifier());
		} catch (Exception exception) {
			logger.error(exception.getMessage());
			exception.printStackTrace();
		}
	}
	
	@Override
	@CronTarget(jobName = JobName.MAKE_STATMENT_PDF)
	public void reportStatmentPdf() {
		try {
			logger.info("Prepair pdf files to statements....");
			JobParameterData data = this.sheduleJobReadPlatformService.getJobParameters(JobName.MAKE_STATMENT_PDF.toString());

			if (data != null) {
				final String path = FileUtils.generateLogFileDirectory()+JobName.MAKE_STATMENT_PDF.toString()+File.separator
					     +"Pdf_"+DateUtils.getLocalDateTimeOfTenant().toString()+".log";
			    File fileHandler = new File(path.trim());
			    fileHandler.createNewFile();
			    FileWriter fw = new FileWriter(fileHandler);
			    FileUtils.BILLING_JOB_PATH = fileHandler.getAbsolutePath();

				List<ScheduleJobData> sheduleDatas = this.sheduleJobReadPlatformService.retrieveSheduleJobParameterDetails(data.getBatchName());

				if (!sheduleDatas.isEmpty()) {
					for (ScheduleJobData scheduleJobData : sheduleDatas) {
						fw.append("ScheduleJobData id= " + scheduleJobData.getId() + " ,BatchName= " + scheduleJobData.getBatchName() + ",query=" + scheduleJobData.getQuery() + "\r\n");
						List<Long> billIds = this.sheduleJobReadPlatformService.getBillIds(scheduleJobData.getQuery(), data);
						if (!billIds.isEmpty()) {
							fw.append("Generate pdf files for the  statment bills..... \r\n");
							for (Long billId : billIds) {
								fw.append("processing statement: " + billId + " \r\n");
								this.billingMasterApiResourse.printStatement(billId);
							}
						} else {
							fw.append("no records are available for generate statement pdf files \r\n");
						}
					}
				} else {
					fw.append("ScheduleJobData Empty \r\n");
				}
				fw.append("statement pdf files Job is Completed..." + ThreadLocalContextUtil.getTenant().getTenantIdentifier() + " . \r\n");
				fw.flush();
				fw.close();
			}
			logger.info("statement  pdf file Job is Completed..." + ThreadLocalContextUtil.getTenant().getTenantIdentifier());
		} catch (Exception exception) {
			logger.error(exception.getMessage());
			exception.printStackTrace();
		}
	}
	

	private String generateBatchId() {

		final Long time = System.currentTimeMillis();
		final String uniqueVal = String.valueOf(time) + getUserId();
		final String BatchId = Long.toHexString(Long.parseLong(uniqueVal));
		return BatchId;
	}

	private Long getUserId() {

		Long userId = null;
		SecurityContext context = SecurityContextHolder.getContext();
		if (context.getAuthentication() != null) {
			AppUser appUser = this.context.authenticatedUser();
			userId = appUser.getId();
		} else {
			userId = new Long(1L);
		}

		return userId;
	}

	@Override
	@CronTarget(jobName = JobName.MESSAGE_MERGE)
	public void processingMessages() {
		
		try {
			logger.info("Processing the messanger......");
			JobParameterData data = this.sheduleJobReadPlatformService.getJobParameters(JobName.MESSAGE_MERGE.toString());

			if (data != null) {
				final String path = FileUtils.generateLogFileDirectory()+JobName.MESSAGE_MERGE.toString()+File.separator
					     +"Messanger_"+DateUtils.getLocalDateTimeOfTenant().toString()+".log";
			    File fileHandler = new File(path.trim());
			    fileHandler.createNewFile();
			    FileWriter fw = new FileWriter(fileHandler);
			    FileUtils.BILLING_JOB_PATH = fileHandler.getAbsolutePath();
				fw.append("Processing the messanger....... \r\n");

				final JsonArray jsonArray = fromApiJsonHelper.parse(data.getBatchName()).getAsJsonArray();

				for (JsonElement jsonElement : jsonArray) {

					final String batchName = fromApiJsonHelper.extractStringNamed("batchName", jsonElement);
					final String messageTemplate = fromApiJsonHelper.extractStringNamed("messageTemplate", jsonElement);

					List<ScheduleJobData> sheduleDatas = this.sheduleJobReadPlatformService.retrieveSheduleJobDetails(batchName);

					if (sheduleDatas.isEmpty()) {

						fw.append("ScheduleJobData Empty \r\n");
					}

					for (ScheduleJobData scheduleJobData : sheduleDatas) {

						fw.append("ScheduleJobData id= " + scheduleJobData.getId() + " ,BatchName= " + scheduleJobData.getBatchName() + " ,query=" + scheduleJobData.getQuery() + "\r\n");
						fw.append("Selected Message Template Name is :" + data.getMessageTemplate() + " \r\n");
						Long messageId = this.sheduleJobReadPlatformService.getMessageId(messageTemplate);
						fw.append("Selected Message Template id is :" + messageId + " \r\n");
						/*
						 if (messageId != null) {
							
							fw.append("generating the message....... \r\n");
							this.billingMessageDataWritePlatformService.createMessageData(messageId, scheduleJobData.getQuery());
							fw.append("messages are generated successfully....... \r\n" );
						}
						 */
						if (messageId != null) {
							
							fw.append("generating the message....... \r\n");
							
							List<String> clientDatas = this.sheduleJobReadPlatformService.getClientData(scheduleJobData.getQuery());
							if(!clientDatas.isEmpty()){
							Integer limit = 100;
							Integer offset =0;
							
							logger.info("value of total split no ......"+Math.ceil((double)clientDatas.size()/limit));
							for(int i =0;i <= (Math.ceil((double)clientDatas.size()/limit)) ;i++){
								logger.info("message with split no ......"+i);
								
								String query = scheduleJobData.getQuery() +" limit "+limit+ " offset "+offset;
								this.billingMessageDataWritePlatformService.createMessageData(messageId, query);
								Thread.sleep(20000);
								offset = offset+limit;
								}
							}else{
								this.billingMessageDataWritePlatformService.createMessageData(messageId, scheduleJobData.getQuery());
							}
							
							
							fw.append("messages are generated successfully....... \r\n" );
						}
						if(scheduleJobData.getBatchName().equalsIgnoreCase(ConfigurationConstants.Reactivation_Fee)){
						List<Long> clientIds = this.sheduleJobReadPlatformService.getClientIds(scheduleJobData.getQuery());
						
						if(!clientIds.isEmpty()){
								
							for(Long clientId : clientIds){
								List<ActionDetaislData> actionDetailesDatas = this.actionDetailsReadPlatformService.retrieveActionDetails(EventActionConstants.EVENT_ORDER_REACTIVATION_FEE);
								if (actionDetailesDatas.size() != 0) {
									this.actionDetailsWritePlatformService.AddNewActions(actionDetailesDatas, clientId, clientId.toString(), null);
								}
								List<Long> orderIds = this.orderReadPlatformService.retrieveCustomerTalkSuspendedOrders(clientId);
								if(!orderIds.isEmpty()){
									for (Long orderId : orderIds) {
										this.scheduleJobRunnerService.ProcessDisconnectUnPaidCustomers(orderId, fw, clientId);
									}
								}
															
							}
						}
						
						
						
					  }
					}
				}

				fw.append("Messanger Job is Completed..." + ThreadLocalContextUtil.getTenant().getTenantIdentifier() + " . \r\n");
				fw.flush();
				fw.close();
				logger.info("Messanger Job is Completed..." + ThreadLocalContextUtil.getTenant().getTenantIdentifier() + " \r\n");
			}

		} catch (Exception dve) {
			logger.error(dve.getMessage());
			dve.printStackTrace();
		}
	}

	@Override
	@CronTarget(jobName = JobName.AUTO_EXIPIRY)
	public void processingAutoExipryOrders() {

		try {

			logger.info("Processing Auto Exipiry Details.......");
			JobParameterData data = this.sheduleJobReadPlatformService.getJobParameters(JobName.AUTO_EXIPIRY.toString());
			
			if (data != null) {
				final String path = FileUtils.generateLogFileDirectory()+JobName.AUTO_EXIPIRY.toString()+File.separator
					     +"AutoExipiry_"+DateUtils.getLocalDateTimeOfTenant().toString()+".log";
			    File fileHandler = new File(path.trim());
			    fileHandler.createNewFile();
			    FileWriter fw = new FileWriter(fileHandler);
			    FileUtils.BILLING_JOB_PATH = fileHandler.getAbsolutePath();
				fw.append("Processing Auto Exipiry Details....... \r\n");
				
				List<ScheduleJobData> sheduleDatas = this.sheduleJobReadPlatformService.retrieveSheduleJobParameterDetails(data.getBatchName());
				LocalDate exipirydate = null;
				if (sheduleDatas.isEmpty()) {
					fw.append("ScheduleJobData Empty \r\n");
				}
				if (data.isDynamic().equalsIgnoreCase("Y")) {
					exipirydate = DateUtils.getLocalDateOfTenant();
				} else {
					exipirydate = data.getExipiryDate();
				}
				for (ScheduleJobData scheduleJobData : sheduleDatas) {
					fw.append("ScheduleJobData id= " + scheduleJobData.getId() + " ,BatchName= " + scheduleJobData.getBatchName() + " ,query=" + scheduleJobData.getQuery() + "\r\n");
					List<Long> clientIds = this.sheduleJobReadPlatformService.getClientIds(scheduleJobData.getQuery(), data);

					if (clientIds.isEmpty()) {
						fw.append("no records are available for Auto Expiry \r\n");
					}
					for (Long clientId : clientIds) {

						fw.append("processing client id :" + clientId + "\r\n");
						List<OrderData> orderDatas = this.orderReadPlatformService.retrieveClientOrderDetails(clientId);
						if (orderDatas.isEmpty()) {
							fw.append("No Orders are Found for :" + clientId + "\r\n");
						}
						for (OrderData orderData : orderDatas) {
							Order order = this.orderRepository.findOne(orderData.getId());
							List<OrderPrice> orderPrice = order.getPrice();
							boolean isSufficientAmountForRenewal = this.scheduleJobRunnerService.checkClientBalanceForOrderrenewal(orderData, clientId, orderPrice);
							this.scheduleJobRunnerService.ProcessAutoExipiryDetails(orderData, fw, exipirydate, data, clientId, isSufficientAmountForRenewal);
						}
					}
				}

				if (data.getAddonExipiry().equalsIgnoreCase("Y")) {

					fw.append("Processing Order Addons for disconnection..." + ThreadLocalContextUtil.getTenant().getTenantIdentifier() + " . \r\n");
					List<Long> addonIds = this.sheduleJobReadPlatformService.retrieveAddonsForDisconnection(DateUtils.getLocalDateOfTenant());
					for (Long addonId : addonIds) {
						fw.append("Addon Id..." + addonId + " . \r\n");
						this.addOnsWritePlatformService.disconnectOrderAddon(null, addonId);
					}
					fw.append("Order Addons processing is done ..." + ThreadLocalContextUtil.getTenant().getTenantIdentifier() + " . \r\n");
				}
				fw.append("Auto Exipiry Job is Completed..." + ThreadLocalContextUtil.getTenant().getTenantIdentifier() + " . \r\n");

				fw.flush();
				fw.close();
				logger.info("Auto Exipiry Job is Completed..." + ThreadLocalContextUtil.getTenant().getTenantIdentifier());
			}
		} catch (IOException exception) {
			logger.error(exception.getMessage(), exception);
		} catch (Exception dve) {
			logger.error(dve.getMessage());
		}
	}
	
	
	
	@Override
	@CronTarget(jobName = JobName.PUSH_NOTIFICATION)
	public void processNotify() {
	
	  try {
		  logger.info("Processing Notify Details.......");
		 
			
			
			
		  List<BillingMessageDataForProcessing> billingMessageDataForProcessings=this.billingMesssageReadPlatformService.retrieveMessageDataForProcessing(null);
		  
		  	if(!billingMessageDataForProcessings.isEmpty()){
		  		final String path = FileUtils.generateLogFileDirectory()+JobName.PUSH_NOTIFICATION.toString()+File.separator
					     +"PushNotification_"+DateUtils.getLocalDateTimeOfTenant().toString()+".log";
			    File fileHandler = new File(path.trim());
			    fileHandler.createNewFile();
			    FileWriter fw = new FileWriter(fileHandler);
			    FileUtils.BILLING_JOB_PATH = fileHandler.getAbsolutePath();
		  		fw.append("Processing Notify Details....... \r\n");
		  		for(BillingMessageDataForProcessing emailDetail : billingMessageDataForProcessings){
		  			 logger.info("emailDetail/...."+emailDetail.getId());
		  			fw.append("BillingMessageData id="+emailDetail.getId()+" ,MessageTo="+emailDetail.getMessageTo()+" ,MessageType="
		  					+emailDetail.getMessageType()+" ,MessageFrom="+emailDetail.getMessageFrom()+" ,Message="+emailDetail.getBody()+"\r\n");
		  			
		  			Gson gson = new Gson();  
		  			String emailDetailjson = gson.toJson(emailDetail);
		  			messageProducer.sendMessage(emailDetailjson);
		  			
		  				/*if(emailDetail.getMessageType()=='E'){
		  					
		  				String Result=this.messagePlatformEmailService.sendToUserEmail(messageQueue.element());
		  				
		  			    // String Result=this.messagePlatformEmailService.sendToUserEmail(emailDetail);
		  			   
		  				fw.append("b_message_data processing id="+emailDetail.getId()+"-- and Result :"+Result+" ... \r\n");
		  			}else if(emailDetail.getMessageType()=='M'){		
		  				String message = this.sheduleJobReadPlatformService.retrieveMessageData(emailDetail.getId());
		  				String Result=this.messagePlatformEmailService.sendToUserMobile(message,emailDetail.getId(),emailDetail.getMessageTo(),emailDetail.getBody());	
		  				fw.append("b_message_data processing id="+emailDetail.getId()+"-- and Result:"+Result+" ... \r\n");	
		  			}else{
		  				fw.append("Message Type Unknown ..\r\n");
		  			}*/	
		  		}
		  		fw.append("Notify Job is Completed.... \r\n");
		  		fw.flush();
		  		fw.close();
		  	}else{
	             logger.info("push Notification data is empty...");
		  	}
		  	logger.info("Notify Job is Completed..."+ ThreadLocalContextUtil.getTenant().getTenantIdentifier());
	  	} catch (Exception exception) {
	  		logger.error(exception.getMessage());
	  	}
	}
	
	
	@SuppressWarnings("resource")
	private static String processRadiusRequests(String url, String encodePassword, String data, FileWriter fw)
			throws IOException {

		HttpClient httpPostClient = new DefaultHttpClient();
		fw.append("data Sending to Server is: " + data + " \r\n");
		StringEntity se = new StringEntity(data.trim());
		fw.append("Request Send to :" + url + "\r\n");

		HttpPost postRequest = new HttpPost(url);
		postRequest.setHeader("Authorization", "Basic " + encodePassword);
		postRequest.setHeader("Content-Type", "application/json");
		postRequest.setEntity(se);

		HttpResponse response = httpPostClient.execute(postRequest);

		if (response.getStatusLine().getStatusCode() == 404) {
			logger.info("ResourceNotFoundException : HTTP error code : " + response.getStatusLine().getStatusCode());
			fw.append("ResourceNotFoundException : HTTP error code : " + response.getStatusLine().getStatusCode()
					+ ", Request url:" + url + "is not Found. \r\n");

			return "ResourceNotFoundException";

		} else if (response.getStatusLine().getStatusCode() == 401) {
			logger.info(" Unauthorized Exception : HTTP error code : " + response.getStatusLine().getStatusCode());
			fw.append(" Unauthorized Exception : HTTP error code : " + response.getStatusLine().getStatusCode()
					+ " , The UserName or Password you entered is incorrect." + "\r\n");

			return "UnauthorizedException";

		} else if (response.getStatusLine().getStatusCode() != 200) {
			logger.info("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
			fw.append("Failed : HTTP error code : " + response.getStatusLine().getStatusCode() + " \r\n");
		} else {
			fw.append("Request executed Successfully... \r\n");
		}

		BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
		String output, output1 = "";

		while ((output = br.readLine()) != null) {
			output1 = output1 + output;
		}

		br.close();
		httpPostClient.getConnectionManager().shutdown();

		return output1;

	}

	@SuppressWarnings("resource")
	private static String processRadiusDeleteRequests(String url, String encodePassword, FileWriter fw) throws IOException {

		HttpClient httpDeleteClient = new DefaultHttpClient();

		fw.append("Request Send to :" + url + "\r\n");

		HttpDelete deleteRequest = new HttpDelete(url);
		deleteRequest.setHeader("Authorization", "Basic " + encodePassword);
		deleteRequest.setHeader("Content-Type", "application/json");

		HttpResponse response = httpDeleteClient.execute(deleteRequest);

		if (response.getStatusLine().getStatusCode() == 404) {
			logger.info("ResourceNotFoundException : HTTP error code : " + response.getStatusLine().getStatusCode());
			fw.append("ResourceNotFoundException : HTTP error code : " + response.getStatusLine().getStatusCode()
					+ ", Request url:" + url + "is not Found. \r\n");

			return "ResourceNotFoundException";

		} else if (response.getStatusLine().getStatusCode() == 401) {
			logger.info(" Unauthorized Exception : HTTP error code : " + response.getStatusLine().getStatusCode());
			fw.append(" Unauthorized Exception : HTTP error code : " + response.getStatusLine().getStatusCode()
					+ " , The UserName or Password you entered is incorrect." + "\r\n");

			return "UnauthorizedException";

		} else if (response.getStatusLine().getStatusCode() != 200) {
			logger.info("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
			fw.append("Failed : HTTP error code : " + response.getStatusLine().getStatusCode() + " \r\n");
		} else {
			fw.append("Request executed Successfully... \r\n");
		}

		BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
		String output, output1 = "";

		while ((output = br.readLine()) != null) {
			output1 = output1 + output;
		}

		br.close();
		httpDeleteClient.getConnectionManager().shutdown();

		return output1;
	}

	@SuppressWarnings("resource")
	private static String processRadiusGetRequests(String url, String encodePassword, FileWriter fw) throws IOException {

		HttpClient httpDeleteClient = new DefaultHttpClient();

		fw.append("Request Send to :" + url + "\r\n");

		HttpGet getRequest = new HttpGet(url);
		getRequest.setHeader("Authorization", "Basic " + encodePassword);
		getRequest.setHeader("Content-Type", "application/json");

		HttpResponse response = httpDeleteClient.execute(getRequest);

		if (response.getStatusLine().getStatusCode() == 404) {
			logger.info("ResourceNotFoundException : HTTP error code : " + response.getStatusLine().getStatusCode());
			fw.append("ResourceNotFoundException : HTTP error code : " + response.getStatusLine().getStatusCode()
					+ ", Request url:" + url + "is not Found. \r\n");

			return "ResourceNotFoundException";

		} else if (response.getStatusLine().getStatusCode() == 401) {
			logger.info(" Unauthorized Exception : HTTP error code : " + response.getStatusLine().getStatusCode());
			fw.append(" Unauthorized Exception : HTTP error code : " + response.getStatusLine().getStatusCode()
					+ " , The UserName or Password you entered is incorrect." + "\r\n");

			return "UnauthorizedException";

		} else if (response.getStatusLine().getStatusCode() != 200) {
			logger.info("Failed : HTTP error code : " + response.getStatusLine().getStatusCode());
			fw.append("Failed : HTTP error code : " + response.getStatusLine().getStatusCode() + " \r\n");
		} else {
			fw.append("Request executed Successfully... \r\n");
		}

		BufferedReader br = new BufferedReader(new InputStreamReader((response.getEntity().getContent())));
		String output, output1 = "";

		while ((output = br.readLine()) != null) {
			output1 = output1 + output;
		}

		br.close();
		httpDeleteClient.getConnectionManager().shutdown();

		return output1;
	}
		
	private void processRadiusSessionOperation(String mikrotikData, String userName, String requestType, String value) {

		try {

			if (null != mikrotikData && null != userName) {
				String prefixCommand = null;
				JSONObject object = new JSONObject(mikrotikData);
				String hostAddress = object.getString("ip");
				String hostUName = object.getString("userName");
				String password = object.getString("password");
				String type = object.getString("type");
				int port = Integer.parseInt(object.getString("port"));

				ApiConnection con = ApiConnection.connect(hostAddress, port);
				con.login(hostUName, password);

				if (type != null && type.equalsIgnoreCase(RadiusJobConstants.RADIUS_HOTSPOT))
					prefixCommand = "/ip/hotspot/";

				if (type != null && type.equalsIgnoreCase(RadiusJobConstants.RADIUS_PPPOE))
					prefixCommand = "/ppp/";

				if (prefixCommand != null && requestType != null && userName != null && !userName.isEmpty()) {

					if (requestType.equalsIgnoreCase(RadiusJobConstants.DisConnection)) {
						List<Map<String, String>> res = con
								.execute(prefixCommand + "active/print where name=" + userName);
						for (Map<String, String> attr : res) {
							String id = attr.get(".id");
							con.execute(prefixCommand + "active/remove .id=" + id);
							logger.info("Session Deleted For " + userName);
						}
					} else if (requestType.equalsIgnoreCase(RadiusJobConstants.ChangePlan)) {
						if (null != value) {
							String name = "<" + type + "-" + userName + ">";
							String printCommand = "/queue/simple/print where name='" + name + "'";
							logger.info("Specific user command : " + printCommand);
							List<Map<String, String>> res = con.execute(printCommand);

							for (Map<String, String> attr : res) {
								String id = attr.get(".id");
								String command = "/queue/simple/set max-limit=" + value + " limit-at=" + value + " .id="
										+ id;
								logger.info("Executing command : " + command);
								con.execute(command);
								logger.info("plan changed Successfully. bandwidth=" + value);
							}
						}
					}

				} else {
					logger.info("Please Configure the Mikrotik Data Properly");

				}

			}

		} catch (JSONException e) {
			logger.info("Mikrotik JobParameter is not a JsonObject");
		} catch (MikrotikApiException e) {
			logger.info("Mikrotik Api Exception:" + e.getCause().getMessage());
		} catch (InterruptedException e) {
			logger.info("Interrupted Exception:" + e.getCause().getMessage());
		}
	}

	@SuppressWarnings("resource")
	@Override
	@CronTarget(jobName = JobName.RADIUS)
	public void processMiddleware() {
		try {
			logger.info("Processing Radius Details.......");
			JobParameterData data = this.sheduleJobReadPlatformService.getJobParameters(JobName.RADIUS.toString());
			if (data != null) {
				
				String credentials = data.getUsername().trim() + ":" + data.getPassword().trim();
				byte[] encoded = Base64.encodeBase64(credentials.getBytes());
				String encodePassword = new String(encoded);
				HttpClient httpClient = new DefaultHttpClient();

				List<EntitlementsData> entitlementDataForProcessings = this.entitlementReadPlatformService
						.getProcessingData(new Long(100), RadiusJobConstants.ProvisioningSystem, null);

				if (!entitlementDataForProcessings.isEmpty()) {
					
					final String path = FileUtils.generateLogFileDirectory()+JobName.RADIUS.toString()+File.separator
						     +"radius"+DateUtils.getLocalDateTimeOfTenant().toString()+".log";
				    File fileHandler = new File(path.trim());
				    fileHandler.createNewFile();
				    FileWriter fw = new FileWriter(fileHandler);
				    FileUtils.BILLING_JOB_PATH = fileHandler.getAbsolutePath();
					fw.append("Processing Radius Details....... \r\n");
					fw.append("Radius Server Details.....\r\n");
					fw.append("UserName of Radius:" + data.getUsername() + " \r\n");
					fw.append("password of Radius: ************** \r\n");
					fw.append("url of Radius:" + data.getUrl() + " \r\n");

					for (EntitlementsData entitlementsData : entitlementDataForProcessings) {
						fw.append("EntitlementsData id=" + entitlementsData.getId() + " ,clientId="
								+ entitlementsData.getClientId() + " ,RequestType=" + entitlementsData.getRequestType() + "\r\n");
						Long clientId = entitlementsData.getClientId();
						if (clientId == null || clientId == 0) {
							throw new ClientNotFoundException(clientId);
						}
						ReceiveMessage = "";
						ClientEntitlementData clientdata = this.entitlementReadPlatformService.getClientData(clientId);

						if (clientdata == null || clientdata.getSelfcareUsername() == null) {
							String output = "Selfcare Not Created with this ClientId: " + clientId + " Properly.";
							fw.append(output + " \r\n");
							ReceiveMessage = RadiusJobConstants.FAILURE + output;

						} else if (entitlementsData.getRequestType().equalsIgnoreCase(RadiusJobConstants.ChangePlan)) {
							try {
								String planIdentification = null;
								JSONObject jsonObject = new JSONObject(entitlementsData.getProduct());

								if (jsonObject != null && jsonObject.has("planIdentification")) {
									planIdentification = jsonObject.getString("planIdentification");
								}
								if (planIdentification == null || planIdentification.isEmpty() || planIdentification.equalsIgnoreCase("")) {
									fw.append("Plan Identification should Not Mapped Properly, Plan Identification=" + planIdentification + " \r\n");
									ReceiveMessage = RadiusJobConstants.FAILURE + "Plan Identification "
											+ " should Not Mapped Properly and Plan Identification=" + planIdentification;
								}
								if (data.getProvSystem().equalsIgnoreCase(RadiusJobConstants.RADIUS_VERSION_ONE)) {

									String userName = clientdata.getSelfcareUsername();
									String deletePlanUrl = data.getUrl() + "raduser/" + userName;
									String deleteOutput = processRadiusDeleteRequests(deletePlanUrl, encodePassword, fw);

									fw.append("Output from Server For Delete Plan: " + deleteOutput + " \r\n");

									if (deleteOutput.trim().contains(RadiusJobConstants.RADIUS_DELETE_OUTPUT)) {

										JSONObject object = new JSONObject();
										object.put("username", clientdata.getSelfcareUsername());
										object.put("groupname", planIdentification);
										object.put("priority", Long.valueOf(1));
										String url = data.getUrl() + "raduser";
										String output = processRadiusRequests(url, encodePassword, object.toString(), fw);

										if (output.trim().equalsIgnoreCase(RadiusJobConstants.RADUSER_CREATE_OUTPUT)) {

											String groupUrl = data.getUrl() + "radservice/group?groupname=" + planIdentification;
											String groupData = processRadiusGetRequests(groupUrl, encodePassword, fw);
											String value = null;
											JSONObject groupObject = (JSONObject) new JSONArray(groupData).get(0);

											if (groupObject.has("value")) {
												value = groupObject.getString("value");
											}

											processRadiusSessionOperation(data.getMikrotikApi().trim(), userName, RadiusJobConstants.ChangePlan, value);
											ReceiveMessage = "Success";

										} else if (output.equalsIgnoreCase("UnauthorizedException") || output.equalsIgnoreCase("ResourceNotFoundException"))
											return;

										else
											ReceiveMessage = RadiusJobConstants.FAILURE + output;

										fw.append("Output from Server For Create Plan: " + output + " \r\n");

									} else if (deleteOutput.equalsIgnoreCase("UnauthorizedException") || deleteOutput.equalsIgnoreCase("ResourceNotFoundException"))
										return;
									else
										ReceiveMessage = RadiusJobConstants.FAILURE + deleteOutput;

								} else if (data.getProvSystem().equalsIgnoreCase(RadiusJobConstants.RADIUS_VERSION_TWO)) {

								} else {
									String output = "UNKNOWN Radius Version, Please check in RadiusJobConstants.java";
									fw.append(output + " \r\n");
									ReceiveMessage = RadiusJobConstants.FAILURE + output;
								}
							} catch (JSONException e) {
								fw.append("JSON Exeception throwing. StockTrace:" + e.getMessage() + " \r\n");
								ReceiveMessage = RadiusJobConstants.FAILURE + e.getMessage();
							}
						} else if (entitlementsData.getRequestType().equalsIgnoreCase(RadiusJobConstants.Activation)
								|| entitlementsData.getRequestType().equalsIgnoreCase(RadiusJobConstants.ReConnection)
								|| entitlementsData.getRequestType().equalsIgnoreCase(RadiusJobConstants.RENEWAL_AE)) {
							try {
								JSONObject jsonObject = new JSONObject(entitlementsData.getProduct());
								String planIdentification = jsonObject.getString("planIdentification");
								if (planIdentification == null || planIdentification.isEmpty()
										|| planIdentification.equalsIgnoreCase("")) {

									fw.append("Plan Identification should Not Mapped Properly, Plan Identification=" + planIdentification + " \r\n");
									ReceiveMessage = RadiusJobConstants.FAILURE + "Plan Identification "
											+ " should Not Mapped Properly and Plan Identification=" + planIdentification;
								}

								if (data.getProvSystem().equalsIgnoreCase(RadiusJobConstants.RADIUS_VERSION_ONE)) {
									JSONObject createUser = new JSONObject();
									createUser.put("username", clientdata.getSelfcareUsername());
									createUser.put("attribute", "Cleartext-Password");
									createUser.put("op", ":=");
									createUser.put("value", clientdata.getSelfcarePassword());
									String createUrl = data.getUrl() + "radcheck";

									String createOutput = processRadiusRequests(createUrl, encodePassword, createUser.toString(), fw);

									if (createOutput.trim().contains(RadiusJobConstants.RADCHECK_OUTPUT)) {

										JSONObject object = new JSONObject();
										object.put("username", clientdata.getSelfcareUsername());
										object.put("groupname", planIdentification);
										object.put("priority", Long.valueOf(1));
										String url = data.getUrl() + "raduser";

										String output = processRadiusRequests(url, encodePassword, object.toString(), fw);

										if (output.trim().equalsIgnoreCase(RadiusJobConstants.RADUSER_CREATE_OUTPUT))
											ReceiveMessage = "Success";
										else if (output.equalsIgnoreCase("UnauthorizedException")
												|| output.equalsIgnoreCase("ResourceNotFoundException"))
											return;
										else
											ReceiveMessage = RadiusJobConstants.FAILURE + output;

										fw.append("Output from Server For Create Plan: " + output + " \r\n");

									} else if (createOutput.equalsIgnoreCase("UnauthorizedException")
											|| createOutput.equalsIgnoreCase("ResourceNotFoundException"))
										return;
									else
										ReceiveMessage = RadiusJobConstants.FAILURE + createOutput;

									fw.append("Output from Server For Create User: " + createOutput + " \r\n");

								} else if (data.getProvSystem().equalsIgnoreCase(RadiusJobConstants.RADIUS_VERSION_TWO)) {

									JSONObject object = new JSONObject();
									object.put("username", clientdata.getSelfcareUsername());
									object.put("password", clientdata.getSelfcarePassword());
									object.put("srvid", planIdentification);
									object.put("firstname", clientdata.getFirstName());
									object.put("lastname", clientdata.getLastName());
									object.put("expiration", "");
									object.put("createdon", data.getUsername());
									object.put("email", clientdata.getEmailId());
									String createUrl = data.getUrl() + "raduser2";

									String createOutput = processRadiusRequests(createUrl, encodePassword, object.toString(), fw);

									if (createOutput.trim().equalsIgnoreCase(RadiusJobConstants.RADCHECK_V2_CREATE_OUTPUT))
										ReceiveMessage = "Success";
									else if (createOutput.equalsIgnoreCase("UnauthorizedException")
											|| createOutput.equalsIgnoreCase("ResourceNotFoundException"))
										return;
									else
										ReceiveMessage = RadiusJobConstants.FAILURE + createOutput;

									fw.append("Output from Server For Create User With Plan: " + createOutput + " \r\n");

								} else {
									String output = "UNKNOWN Radius Version, Please check in RadiusJobConstants.java";
									fw.append(output + " \r\n");
									ReceiveMessage = RadiusJobConstants.FAILURE + output;
								}
							} catch (JSONException e) {
								fw.append("JSON Exeception throwing. StockTrace:" + e.getMessage() + " \r\n");
								ReceiveMessage = RadiusJobConstants.FAILURE + e.getMessage();
							}

						} else if (entitlementsData.getRequestType().equalsIgnoreCase(RadiusJobConstants.DisConnection)) {

							try {
								if (data.getProvSystem().equalsIgnoreCase(RadiusJobConstants.RADIUS_VERSION_ONE)) {
									String userName = clientdata.getSelfcareUsername();
									String deletePlanUrl = data.getUrl() + "raduser/" + userName;
									String deleteUserUrl = data.getUrl() + "radcheck/" + userName;
									String deleteOutput = processRadiusDeleteRequests(deletePlanUrl, encodePassword, fw);

									if (deleteOutput.trim().contains(RadiusJobConstants.RADIUS_DELETE_OUTPUT)) {

										String deleteUserOutput = processRadiusDeleteRequests(deleteUserUrl, encodePassword, fw);

										if (deleteUserOutput.trim().contains(RadiusJobConstants.RADIUS_DELETE_OUTPUT)) {
											processRadiusSessionOperation(data.getMikrotikApi().trim(), userName, RadiusJobConstants.DisConnection, null);
											ReceiveMessage = "Success";
										} else if (deleteUserOutput.equalsIgnoreCase("UnauthorizedException")
												|| deleteUserOutput.equalsIgnoreCase("ResourceNotFoundException"))
											return;
										else
											ReceiveMessage = RadiusJobConstants.FAILURE + deleteUserOutput;

										fw.append("Output from Server For Delete User: " + deleteUserOutput + " \r\n");

									} else if (deleteOutput.equalsIgnoreCase("UnauthorizedException")
											|| deleteOutput.equalsIgnoreCase("ResourceNotFoundException"))
										return;
									else
										ReceiveMessage = RadiusJobConstants.FAILURE + deleteOutput;

									fw.append("Output from Server For Delete Plan: " + deleteOutput + " \r\n");

								} else if (data.getProvSystem().equalsIgnoreCase(RadiusJobConstants.RADIUS_VERSION_TWO)) {

									String userName = clientdata.getSelfcareUsername();
									String deleteUserUrl = data.getUrl() + "raduser2/" + userName;
									String deleteOutput = processRadiusDeleteRequests(deleteUserUrl, encodePassword, fw);

									if (deleteOutput.trim().contains(RadiusJobConstants.RADIUS_V2_DELETE_OUTPUT)) {
										processRadiusSessionOperation(data.getMikrotikApi().trim(), userName, RadiusJobConstants.DisConnection, null);
										ReceiveMessage = "Success";
									}

									else if (deleteOutput.equalsIgnoreCase("UnauthorizedException")
											|| deleteOutput.equalsIgnoreCase("ResourceNotFoundException"))
										return;
									else
										ReceiveMessage = RadiusJobConstants.FAILURE + deleteOutput;

									fw.append("Output from Server For Delete Plan: " + deleteOutput + " \r\n");

								} else {
									String output = "UNKNOWN Radius Version, Please check in RadiusJobConstants.java";
									fw.append(output + " \r\n");
									ReceiveMessage = RadiusJobConstants.FAILURE + output;
								}

							} catch (Exception e) {
								fw.append("Exeception throwing. StockTrace:" + e.getMessage() + " \r\n");
								ReceiveMessage = RadiusJobConstants.FAILURE + e.getMessage();
							}

						} else {
							try {
								fw.append("Request Type is:" + entitlementsData.getRequestType());
								fw.append("Unknown Request Type for Server (or) This Request Type is Not Handle in Radius Job");
								ReceiveMessage = RadiusJobConstants.FAILURE + "Unknown Request Type for Server";
							} catch (Exception e) {
								fw.append("Exeception throwing. StockTrace:" + e.getMessage() + " \r\n");
								ReceiveMessage = RadiusJobConstants.FAILURE + e.getMessage();
							}
						}
						// Updating the Response and status in b_process_request.
						JsonObject object = new JsonObject();
						object.addProperty("prdetailsId", entitlementsData.getPrdetailsId());
						object.addProperty("receivedStatus", new Long(1));
						object.addProperty("receiveMessage", ReceiveMessage);
						String entityName = "ENTITLEMENT";
						fw.append("sending json data to EntitlementApi is:" + object.toString() + "\r\n");
						final JsonElement element1 = fromApiJsonHelper.parse(object.toString());
						JsonCommand comm = new JsonCommand(null, object.toString(), element1, fromApiJsonHelper,
								entityName, entitlementsData.getId(), null, null, null, null, null, null, null, null,null, null);
						CommandProcessingResult result = this.entitlementWritePlatformService.create(comm);
						fw.append("Result From the EntitlementApi is:" + result.resourceId() + " \r\n");
					}

					fw.append("Radius Job is Completed..." + ThreadLocalContextUtil.getTenant().getTenantIdentifier() + " /r/n");
					fw.flush();
					fw.close();

				} else {
					logger.info("Radius data is Empty...");
				}
				httpClient.getConnectionManager().shutdown();
				logger.info("Radius Job is Completed...");
			}

		} catch (DataIntegrityViolationException exception) {
			logger.info("catching the DataIntegrityViolationException, StockTrace: " + exception.getMessage());
		} catch (IOException e) {
			logger.info("catching the IOException, StockTrace: " + e.getMessage());
		} catch (Exception exception) {
			logger.info(exception.getMessage());
		}
	}

	@Override
	@CronTarget(jobName = JobName.REPORT_EMAIL)
	public void reportEmail() {

		try {
			logger.info("Processing report email.....");
			JobParameterData data = this.sheduleJobReadPlatformService.getJobParameters(JobName.REPORT_EMAIL.toString());
			if (data != null) {
				
				final String fileLocation = FileUtils.OBS_BASE_DIR + File.separator + JobName.REPORT_EMAIL.toString()
						+ File.separator + "ReportEmail_"+DateUtils.getLocalDateTimeOfTenant().toString();

				final String path = FileUtils.generateLogFileDirectory()+JobName.REPORT_EMAIL.toString()+File.separator
					     +"ReportEmail_"+DateUtils.getLocalDateTimeOfTenant().toString()+".log";
			    File fileHandler = new File(path.trim());
			    fileHandler.createNewFile();
			    FileWriter fw = new FileWriter(fileHandler);
			    FileUtils.BILLING_JOB_PATH = fileHandler.getAbsolutePath();
				List<ScheduleJobData> sheduleDatas = this.sheduleJobReadPlatformService.retrieveSheduleJobDetails(data.getBatchName());

				if (sheduleDatas.isEmpty()) {
					fw.append("ScheduleJobData Empty with this Stretchy_report :" + data.getBatchName() + "\r\n");
				}
				for (ScheduleJobData scheduleJobData : sheduleDatas) {
					fw.append("Processing report email.....\r\n");
					fw.append("ScheduleJobData id= " + scheduleJobData.getId() + " ,BatchName= " + scheduleJobData.getBatchName() + " ,query=" + scheduleJobData.getQuery() + "\r\n");
					Map<String, String> reportParams = new HashMap<String, String>();
					reportParams.put("${officeId}", "-1");
					reportParams.put("${paymode_id}", "-1");
					reportParams.put("${endDate}", DateUtils.getLocalDateOfTenant().toString());
					reportParams.put("${startDate}", DateUtils.getLocalDateOfTenant().minusMonths(1).toString());
					String pdfFileName = this.readExtraDataAndReportingService
							.generateEmailReport(scheduleJobData.getBatchName(), "report", reportParams, fileLocation);

					if (pdfFileName != null) {

						fw.append("PDF file location is :" + pdfFileName + " \r\n");
						fw.append("Sending the Email....... \r\n");
						String result = this.messagePlatformEmailService.createEmail(pdfFileName, data.getEmailId());

						if (result.equalsIgnoreCase("Success")) {
							fw.append("Email sent successfully to " + data.getEmailId() + " \r\n");
						} else {
							fw.append("Email sending failed to " + data.getEmailId() + ", \r\n");

						}
						if (pdfFileName.isEmpty()) {
							fw.append("PDF file name is Empty and PDF file doesnot Create Properly \r\n");
						}
					} else {
						fw.append("PDF file Creation Failed \r\n");
					}
				}
				fw.append("Report Emails Job is Completed....\r\n");
				fw.flush();
				fw.close();
			}
			logger.info("Report Emails Job is Completed....");
		} catch (Exception e) {
			logger.info(e.getMessage());
		}
	}

	@Override
	@CronTarget(jobName = JobName.EXPORT_DATA)
	public void processExportData() {

		try {
			logger.info("Processing export data....");
			JobParameterData data = this.sheduleJobReadPlatformService.getJobParameters(JobName.EXPORT_DATA.toString());
			
			if (data != null) {	
				final String path = FileUtils.generateLogFileDirectory()+JobName.EXPORT_DATA.toString()+File.separator
					     +"ExportData_"+DateUtils.getLocalDateTimeOfTenant().toString()+".log";
			    File fileHandler = new File(path.trim());
			    fileHandler.createNewFile();
			    FileWriter fw = new FileWriter(fileHandler);
			    FileUtils.BILLING_JOB_PATH = fileHandler.getAbsolutePath();
				fw.append("Processing export data....\r\n");

				// procedure calling
				SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(this.jdbcTemplate);
				MapSqlParameterSource parameterSource = new MapSqlParameterSource();
				simpleJdbcCall.setProcedureName("p_int_fa");// p --> procedure int --> integration fa --> financial account s/w {p_todt=2014-12-30}

				if ("Y".equalsIgnoreCase(data.isDynamic())) {
					parameterSource.addValue("p_todt", DateUtils.getLocalDateOfTenant().toString(), Types.DATE);
				} else {
					parameterSource.addValue("p_todt", data.getProcessDate().toString(), Types.DATE);
				}

				Map<String, Object> output = simpleJdbcCall.execute(parameterSource);

				if (output.isEmpty()) {
					fw.append("Exporting data failed....." + ThreadLocalContextUtil.getTenant().getTenantIdentifier() + "\r\n");
				} else {
					fw.append("No of records inserted :" + output.values() + "\r\n");
					fw.append("Exporting data successfully....." + ThreadLocalContextUtil.getTenant().getTenantIdentifier() + "\r\n");
				}
				fw.flush();
				fw.close();
			}
			logger.info("Exporting data successfully done....." + ThreadLocalContextUtil.getTenant().getTenantIdentifier());
		} catch (DataIntegrityViolationException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}
		
	@Override
	@CronTarget(jobName = JobName.RESELLER_COMMISSION)
	public void processPartnersCommission() {

		try {
			logger.info("Processing reseller commission data....");
			JobParameterData data = this.sheduleJobReadPlatformService.getJobParameters(JobName.RESELLER_COMMISSION.toString());
			
			if (data != null) {	
				final String path = FileUtils.generateLogFileDirectory()+JobName.RESELLER_COMMISSION.toString()+File.separator
					     +"Commission_"+DateUtils.getLocalDateTimeOfTenant().toString()+".log";
			    File fileHandler = new File(path.trim());
			    fileHandler.createNewFile();
			    FileWriter fw = new FileWriter(fileHandler);
			    FileUtils.BILLING_JOB_PATH = fileHandler.getAbsolutePath();
				fw.append("Processing reseller commission data....\r\n");

				// procedure calling
				SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(this.jdbcTemplate);
				String sql = "SELECT count(*) FROM Information_schema.Routines WHERE Routine_schema ='" + ThreadLocalContextUtil.getTenant().getSchemaName() + 
						     "'AND specific_name = 'proc_office_commission' AND Routine_Type = 'PROCEDURE'";
				String procdeureStatus = simpleJdbcCall.getJdbcTemplate().queryForObject(sql, String.class);
				if (Integer.valueOf(procdeureStatus) >= 1) {
					simpleJdbcCall.setProcedureName("proc_office_commission");
					Map<String, Object> output = simpleJdbcCall.execute();
					if (output.isEmpty()) {
						fw.append("Reseller commission process failed....." + ThreadLocalContextUtil.getTenant().getTenantIdentifier() + "\r\n");
					} else {
						fw.append("No of records inserted :" + output.values() + "\r\n");
						fw.append("Reseller commission processed successfully....." + ThreadLocalContextUtil.getTenant().getTenantIdentifier() + "\r\n");
					}
					fw.flush();
					fw.close();
					logger.info("Reseller commission processed successfully....." + ThreadLocalContextUtil.getTenant().getTenantIdentifier());
				} else {
					fw.append("Procedure with name 'proc_office_commission' not exists....." + ThreadLocalContextUtil.getTenant().getTenantIdentifier() + "\r\n");
					fw.flush();
					fw.close();
					logger.info("Reseller commission processed failed....." + ThreadLocalContextUtil.getTenant().getTenantIdentifier());
				}
			}
		} catch (DataIntegrityViolationException e) {
			logger.error(e.getMessage());
		} catch (Exception e) {
			logger.error(e.getMessage());
		}
	}

	@Override
	@CronTarget(jobName = JobName.AGING_DISTRIBUTION)
	public void processAgingDistribution() {

		try {
			logger.info("Processing aging distribution data....");
			final String path = FileUtils.generateLogFileDirectory()+JobName.AGING_DISTRIBUTION.toString()+File.separator
				     +"Distribution_"+DateUtils.getLocalDateTimeOfTenant().toString()+".log";
		    File fileHandler = new File(path.trim());
		    fileHandler.createNewFile();
		    FileWriter fw = new FileWriter(fileHandler);
		    FileUtils.BILLING_JOB_PATH = fileHandler.getAbsolutePath();
			fw.append("Processing aging distribution data....\r\n");

			// procedure calling
			SimpleJdbcCall simpleJdbcCall = new SimpleJdbcCall(this.jdbcTemplate);
			String sql = "SELECT count(*) FROM Information_schema.Routines WHERE Routine_schema ='"	+ ThreadLocalContextUtil.getTenant().getSchemaName() + 
					     "'AND specific_name = 'proc_distrib' AND Routine_Type = 'PROCEDURE'";
			String procdeureStatus = simpleJdbcCall.getJdbcTemplate().queryForObject(sql, String.class);
			if (Integer.valueOf(procdeureStatus) >= 1) {
				simpleJdbcCall.setProcedureName("proc_distrib");
				Map<String, Object> output = simpleJdbcCall.execute();
				if (output.isEmpty()) {
					fw.append("Aging Distribution data failed....." + ThreadLocalContextUtil.getTenant().getTenantIdentifier() + "\r\n");
				} else {
					fw.append("No of records inserted :" + output.values() + "\r\n");
					fw.append("Aging Distribution data successfully completed....." + ThreadLocalContextUtil.getTenant().getTenantIdentifier() + "\r\n");
				}
				fw.flush();
				fw.close();
				logger.info("Aging Distribution data successfully completed....." + ThreadLocalContextUtil.getTenant().getTenantIdentifier());
			} else {
				fw.append("Procedure with name 'proc_distrib' not exists....." + ThreadLocalContextUtil.getTenant().getTenantIdentifier() + "\r\n");
				fw.flush();
				fw.close();
				logger.info("Aging Distribution data failed....." + ThreadLocalContextUtil.getTenant().getTenantIdentifier());
			}
		} catch (DataIntegrityViolationException e) {
			logger.info(e.getMessage());
			e.printStackTrace();
		} catch (Exception e) {
			logger.info(e.getMessage());
			e.printStackTrace();
		}
	}

	@Override
	@CronTarget(jobName = JobName.REPROCESS)
	public void reProcessEventAction() {
		
		try {

			logger.info("Processing ReProcess Request Job .....");
			final String path = FileUtils.generateLogFileDirectory()+JobName.REPROCESS.toString()+File.separator
				     +"ReProcess_"+DateUtils.getLocalDateTimeOfTenant().toString()+".log";
		    File fileHandler = new File(path.trim());
		    fileHandler.createNewFile();
		    FileWriter fw = new FileWriter(fileHandler);
		    FileUtils.BILLING_JOB_PATH = fileHandler.getAbsolutePath();
		    
			List<PaymentGatewayData> datas = this.paymentGatewayReadPlatformService.retrievePendingDetails();
			SimpleDateFormat dateformat = new SimpleDateFormat("dd MMM yyyy HH:mm:ss");

			for (PaymentGatewayData data : datas) {

				JSONObject reProcessObject;
				boolean processingFlag;

				Configuration reProcessInterval = this.configurationRepository.findOneByName(ConfigurationConstants.CONFIG_PROPERTY_REPROCESS_INTERVAL);
				int intervalTime = 0;

				if (reProcessInterval != null && reProcessInterval.getValue() != null 
						&& !reProcessInterval.getValue().isEmpty()) {
					intervalTime = Integer.parseInt(reProcessInterval.getValue());
				}

				PaymentGateway paymentGateway = this.paymentGatewayRepository.findOne(data.getId());

				if (null == paymentGateway.getReProcessDetail() || paymentGateway.getReProcessDetail().isEmpty()) {
					processingFlag = true;
					reProcessObject = new JSONObject();
					reProcessObject.put("id", 0);
					reProcessObject.put("response", "");
					reProcessObject.put("processTime", "");

				} else {
					reProcessObject = new JSONObject(data.getReprocessDetail());

					Date reProcessingDate = dateformat.parse(reProcessObject.get("processTime").toString());
					Date newDate = DateUtils.getDateOfTenant();
					long diff = newDate.getTime() - reProcessingDate.getTime();
					long hours = diff / (60 * 60 * 1000);

					if (intervalTime <= hours)
						processingFlag = true;
					else
						processingFlag = false;
				}

				if (processingFlag) {

					if (paymentGateway.getSource().equalsIgnoreCase(ConfigurationConstants.GLOBALPAY_PAYMENTGATEWAY)) {

						final String formattedDate = dateformat.format(DateUtils.getDateOfTenant());

						int id = reProcessObject.getInt("id");
						reProcessObject.remove("id");
						reProcessObject.remove("response");
						reProcessObject.remove("processTime");
						reProcessObject.put("id", id + 1);

						JSONObject object = new JSONObject(paymentGateway.getRemarks());

						String transactionId = object.getString("transactionId");

						String output = this.paymentGatewayWritePlatformService.globalPayProcessing(transactionId,
								paymentGateway.getRemarks());

						final JSONObject json = new JSONObject(output);

						String status = json.getString("status");
						String error = json.getString("error");

						reProcessObject.put("response", error);
						reProcessObject.put("processTime", formattedDate);

						if (status.equalsIgnoreCase(ConfigurationConstants.PAYMENTGATEWAY_SUCCESS)) {

							List<EventActionData> eventActionDatas = this.eventActionReadPlatformService
									.retrievePendingActionRequest(paymentGateway.getId());

							for (EventActionData eventActionData : eventActionDatas) {

								fw.append("Process Response id=" + eventActionData.getId() + " ,PaymentGatewayId="
										+ eventActionData.getResourceId() + " ,Provisiong System="
										+ eventActionData.getActionName() + " \r\n");
								logger.info("EventAction Id:" + eventActionData.getId() + ", PaymentGatewayId:"
										+ eventActionData.getResourceId());

								EventAction eventAction = this.eventActionRepository.findOne(eventActionData.getId());

								if (eventAction.getActionName()
										.equalsIgnoreCase(EventActionConstants.EVENT_CREATE_PAYMENT)) {

									JSONObject paymentObject = new JSONObject(eventActionData.getJsonData());
									paymentObject.put("paymentDate", formattedDate);

									eventAction.updateStatus('N');
									eventAction.setTransDate(DateUtils.getLocalDateOfTenant().toDate());
									eventAction.setCommandAsJson(paymentObject.toString());

								} else if (eventAction.getActionName()
										.equalsIgnoreCase(EventActionConstants.ACTION_NEW)) {

									JSONObject createOrder = new JSONObject(eventAction.getCommandAsJson());
									createOrder.remove("start_date");
									eventAction.updateStatus('N');
									eventAction.setTransDate(DateUtils.getLocalDateOfTenant().toDate());
									createOrder.put("start_date", DateUtils.getLocalDateOfTenant().toDate());
									eventAction.setCommandAsJson(createOrder.toString());

								} else {
									logger.info("Does Not Implement the Code....");
								}

								this.eventActionRepository.save(eventAction);
							}

						} else if (status.equalsIgnoreCase(ConfigurationConstants.PAYMENTGATEWAY_FAILURE)) {

							List<EventActionData> eventActionDatas = this.eventActionReadPlatformService
									.retrievePendingActionRequest(paymentGateway.getId());

							for (EventActionData eventActionData : eventActionDatas) {

								fw.append("Process Response id=" + eventActionData.getId() + " ,PaymentGatewayId="
										+ eventActionData.getResourceId() + " ,Provisiong System="
										+ eventActionData.getActionName() + " \r\n");
								logger.info("EventAction Id:" + eventActionData.getId() + ", PaymentGatewayId:"
										+ eventActionData.getResourceId());

								EventAction eventAction = this.eventActionRepository.findOne(eventActionData.getId());

								if (eventAction.getActionName()
										.equalsIgnoreCase(EventActionConstants.EVENT_CREATE_PAYMENT)
										|| eventAction.getActionName()
												.equalsIgnoreCase(EventActionConstants.EVENT_CREATE_ORDER)) {

									eventAction.updateStatus('F');
									eventAction.setTransDate(DateUtils.getLocalDateOfTenant().toDate());

								} else {
									logger.info("Does Not Implement the Code....");
								}

								this.eventActionRepository.save(eventAction);

							}

						} else {
							logger.info("Still get Pending Response from PaymentGateway");
						}

						paymentGateway.setStatus(status);

						paymentGateway.setReProcessDetail(reProcessObject.toString());

						this.paymentGatewayRepository.save(paymentGateway);
					}

				}
			}

			logger.info("ReProcess Requests are Processed....");
			fw.append("ReProcess Requests are Completed.... \r\n");
			fw.flush();
			fw.close();

		} catch (DataIntegrityViolationException e) {
			logger.error(e.getMessage());
		} catch (Exception exception) {
			logger.error(exception.getMessage());
		}
	}
		
	/*@Override
	@CronTarget(jobName = JobName.DISCONNECT_UNPAID_CUSTOMERS)
	public void processingDisconnectUnpaidCustomers() {

		try {
			logger.info("Processing Unpaid Customers Details.......");
			final JobParameterData data = this.sheduleJobReadPlatformService.getJobParameters(JobName.DISCONNECT_UNPAID_CUSTOMERS.toString());

			if (data != null) {
				final String path = FileUtils.generateLogFileDirectory()+JobName.DISCONNECT_UNPAID_CUSTOMERS.toString()+File.separator
					     +"UnpaidCustomers_"+DateUtils.getLocalDateTimeOfTenant().toString()+".log";
			    File fileHandler = new File(path.trim());
			    fileHandler.createNewFile();
			    FileWriter fw = new FileWriter(fileHandler);
			    FileUtils.BILLING_JOB_PATH = fileHandler.getAbsolutePath();
				fw.append("Processing Unpaid Customers Details....... \r\n");
				
				List<ScheduleJobData> sheduleDatas = this.sheduleJobReadPlatformService.retrieveSheduleJobParameterDetails(data.getBatchName());

				if (!sheduleDatas.isEmpty()) {
					for (ScheduleJobData scheduleJobData : sheduleDatas) {
						fw.append("ScheduleJobData id=" + scheduleJobData.getId() + " ,BatchName=" + scheduleJobData.getBatchName() + " ,query=" + scheduleJobData.getQuery() + "\r\n");
						List<Long> clientIds = this.sheduleJobReadPlatformService.getClientIds(scheduleJobData.getQuery(), data);
						if (!clientIds.isEmpty()) {
							for (Long clientId : clientIds) {
								fw.append("processing Unpaid Customer :" + clientId + "\r\n");
								List<OrderData> orders = this.orderReadPlatformService.retrieveCustomerActiveOrders(clientId);
								if (!orders.isEmpty()) {
									for (OrderData order : orders) {
										this.scheduleJobRunnerService.ProcessDisconnectUnPaidCustomers(order.getId(), fw, clientId);
									}
								} else {
									fw.append("No Orders are Found for :" + clientId + "\r\n");
								}
							}
						} else {
							fw.append("no records are available for Disconnect Unpaid Customers \r\n");
						}
					}
				} else {
					fw.append("Unpaid Customers ScheduleJobData Empty \r\n");
				}
				fw.append("Disconnect Unpaid Customers Job is Completed..." + ThreadLocalContextUtil.getTenant().getTenantIdentifier() + " . \r\n");
				fw.flush();
				fw.close();
			}
			logger.info("Disconnect Unpaid Customers Job is Completed..." + ThreadLocalContextUtil.getTenant().getTenantIdentifier());
		} catch (DataIntegrityViolationException | IOException e) {
			logger.error(e.getMessage(),e);
		} catch (Exception dve) {
			logger.error(dve.getMessage());
		}
	}*/

	@Override
	@CronTarget(jobName = JobName.USAGE_CHARGES)
	public void processingCustomerUsageCharges() {

		try {
			logger.info("Processing Customers Usage Charges.......");
			JobParameterData data = this.sheduleJobReadPlatformService.getJobParameters(JobName.USAGE_CHARGES.toString());
			if (data != null) {
				final String path = FileUtils.generateLogFileDirectory()+JobName.USAGE_CHARGES.toString()+File.separator
					     +"Cdr_"+DateUtils.getLocalDateTimeOfTenant().toString()+".log";
			    File fileHandler = new File(path.trim());
			    fileHandler.createNewFile();
			    FileWriter fw = new FileWriter(fileHandler);
			    FileUtils.BILLING_JOB_PATH = fileHandler.getAbsolutePath();
				fw.append("Processing Customers Usage Charges....... \r\n");
	
				final List<ScheduleJobData> sheduleDatas = this.sheduleJobReadPlatformService.retrieveSheduleJobParameterDetails(data.getBatchName());

				if (!sheduleDatas.isEmpty()) {
					for (ScheduleJobData scheduleJobData : sheduleDatas) {
						 fw.append("ScheduleJobData id="+ scheduleJobData.getId() + " ,BatchName="+ scheduleJobData.getBatchName() + " ,query="+ scheduleJobData.getQuery() + "\r\n");
						 List<UsageChargesData> customerDatas = this.sheduleJobReadPlatformService.getCustomerUsageRawData(scheduleJobData.getQuery(), data);
						 if (!customerDatas.isEmpty()) {
							for (UsageChargesData customerData : customerDatas) {
								fw.append("processing Customer Id :"+ customerData.getClientId() + "\r\n");
								this.usageChargesWritePlatformService.processCustomerUsageRawData(customerData);
							}
						} else {
							fw.append("no records are available for processing Usage Charges \r\n");
						}
					}
				} else {
					fw.append("Usage Charges ScheduleJobData Empty \r\n");
				}
				fw.append("Usage Charges Job is Completed..."+ ThreadLocalContextUtil.getTenant().getTenantIdentifier() + " . \r\n");
				fw.flush();
				fw.close();
			}
			logger.info("Usage Charges Job is Completed..."+ ThreadLocalContextUtil.getTenant().getTenantIdentifier());
		} catch (DataIntegrityViolationException | IOException e) {
			logger.error(e.getMessage());
		} catch (Exception dve) {
			logger.error(dve.getMessage());
		}
	}
		
	@Override
	@CronTarget(jobName = JobName.EVO_BATCH_PROCESS)
	public void evoBatchProcess() {
		
		logger.info("Processing EVO Batch process upload .......");
		JobParameterData data = null;
		File fileHandler, filelocation;
		FileWriter fileWriter, fw;
		JsonElement element;
		String dateString, fileName, fileDirectory, path, Header, footer, invoiceDate;
		String host, username, merchantId, privateKey, SFTPWORKINGDIR, passphrase;
		host = username = merchantId = privateKey = SFTPWORKINGDIR = passphrase = null;	
		int port = 0;

		List<EvoBatchData> evoBatchDatas;
				
		BigDecimal finalAmount = BigDecimal.ZERO;
		int count = 0;
		ClientCardDetailsData clientIdentifiers = null;
		final List<Long> clientIds = new ArrayList<Long>();
		
		try {
			
			data = this.sheduleJobReadPlatformService.getJobParameters(JobName.EVO_BATCH_PROCESS.toString());

			if (null == data) {
				throw new JobParameterNotConfiguredException(JobName.EVO_BATCH_PROCESS.toString());
			}
			
			final String filePath = FileUtils.generateLogFileDirectory()+JobName.EVO_BATCH_PROCESS.toString()+File.separator
				     +"EvoBatchProcessing_"+DateUtils.getLocalDateTimeOfTenant().toString()+".log";
		    fileHandler = new File(filePath.trim());
		    fileHandler.createNewFile();
		    fw = new FileWriter(fileHandler);
		    FileUtils.BILLING_JOB_PATH = fileHandler.getAbsolutePath();

			evoBatchDatas = this.sheduleJobReadPlatformService.getUnProcessedRecurringData();
			
			if(evoBatchDatas.size()>0) {
				
				element = this.fromApiJsonHelper.parse(data.getBatchName());
				JsonObject evoElement = element.getAsJsonObject();
				evoElement.addProperty("locale", "en");
				
				LocalDate date = DateUtils.getLocalDateOfTenant();
				dateString = FORMAT.format(date.toDate());
				
				if(this.fromApiJsonHelper.parameterExists("host", evoElement)) {
					host = this.fromApiJsonHelper.extractStringNamed("host", evoElement);
				}
				if(this.fromApiJsonHelper.parameterExists("username", evoElement)) {
					username = this.fromApiJsonHelper.extractStringNamed("username", evoElement);
				}
				if(this.fromApiJsonHelper.parameterExists("privateKey", evoElement)) {
					privateKey = this.fromApiJsonHelper.extractStringNamed("privateKey", evoElement);
				}
				if(this.fromApiJsonHelper.parameterExists("merchantId", evoElement)) {
					merchantId = this.fromApiJsonHelper.extractStringNamed("merchantId", evoElement);
				}
				if(this.fromApiJsonHelper.parameterExists("SFTPWORKINGDIR", evoElement)) {
					SFTPWORKINGDIR = this.fromApiJsonHelper.extractStringNamed("SFTPWORKINGDIR", evoElement);
				}
				if(this.fromApiJsonHelper.parameterExists("port", evoElement)) {
					port = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("port", evoElement);
				}
				if(this.fromApiJsonHelper.parameterExists("passphrase", evoElement)) {
					passphrase = this.fromApiJsonHelper.extractStringNamed("passphrase", evoElement);
				}
								 
				File tempfile = File.createTempFile("pgp", null); 
				fileWriter = new FileWriter(tempfile.getAbsoluteFile());
				final BufferedWriter bw = new BufferedWriter(fileWriter);
				Header = "HEAD," + merchantId + "," + dateString + ",1.2";
				bw.write(Header);
				bw.write("\n");
				

				DecimalFormat noDecimalFormat = new DecimalFormat("0.#");
				DateFormat df = new SimpleDateFormat("yyMMddHHmmss");
				
				for (EvoBatchData evoBatchData : evoBatchDatas) {
					
					try {
						clientIdentifiers = this.clientCardDetailsReadPlatformService.retrieveClient(null, ConfigurationConstants.PSEUDOCARD, evoBatchData.getClientId());
					} catch (Exception exception) {
						logger.error(exception.getClass().getSimpleName() + " is throwing by ClientCardDetails Api. ", exception);
					}
					
					if(null != clientIdentifiers) {
						
						invoiceDate = EVOFORMAT.format(evoBatchData.getInVoiceDate());
						final StringBuilder builder = new StringBuilder();								
						builder.append("CC,Sale,");
						builder.append(noDecimalFormat.format(evoBatchData.getAmount().doubleValue()*100)).append(",GBP," + evoBatchData.getClientId() + "," +df.format(new Date())+evoBatchData.getChargeId() + ",");
						builder.append(clientIdentifiers.getCardType()).append(",");
						builder.append(clientIdentifiers.getCardNumber()).append(",");
						builder.append(clientIdentifiers.getCardExpiryDate()).append(",");
						builder.append("Bill Generated Date: " + invoiceDate + ".");
						builder.append(",,,").append(clientIdentifiers.getRtftype());					
						bw.write(builder.toString());
						bw.write("\n");
						
						count = count + 1;
          						
						finalAmount = finalAmount.add(BigDecimal.valueOf((evoBatchData.getAmount().doubleValue())*100), mc);
						
					} else {
						clientIds.add(evoBatchData.getClientId());
					}
				}
				
				footer = "FOOT," + count + "," + noDecimalFormat.format(finalAmount.doubleValue());
				bw.write(footer);
				bw.flush();
				bw.close();
				
				String chars = "0123456789";
			    int string_length = 3;
				String randomstring = "";
				for (int i=0; i<string_length; i++) {
					int rnum = (int) Math.floor(Math.random() * chars.length());
					randomstring += chars.substring(rnum,rnum+1);	
				}	
				
				fileName = "W" + dateString + randomstring + merchantId + ".dat.pgp";
			    fileDirectory = FileUtils.generateLogFileDirectory() + JobName.EVO_BATCH_PROCESS.toString() + File.separator + dateString;
			    logger.info("EVO Batch process fileDirectory: "+ fileDirectory);
			    filelocation = new File(fileDirectory);			
				if(!filelocation.isDirectory()){
					filelocation.mkdirs();
				}
			    
			    path = fileDirectory + File.separator + fileName;	
			    logger.info("EVO Batch process upload path: "+ path);
			    PGPSecurityFileProcessor pGPSecurityFileProcessor = PGPSecurityFileProcessor
			    		.intializePGPFP("encrypt", tempfile.getAbsolutePath(), path, passphrase);
			    pGPSecurityFileProcessor.encrypt();
			    
			    this.evoRecurringBillingWritePlatformService.intializeFTP(host, port, username, privateKey, SFTPWORKINGDIR);
			    this.evoRecurringBillingWritePlatformService.uploadFTPFile(path, fileName);
				
				tempfile.deleteOnExit();
			}
			
			fw.append("OSM are Generated....." + ThreadLocalContextUtil.getTenant().getTenantIdentifier() + "\r\n");
			fw.flush();
			fw.close();

			logger.info("OSM are Generated....." + ThreadLocalContextUtil.getTenant().getTenantIdentifier());
			
		} catch (DataIntegrityViolationException exception) {
			exception.printStackTrace();
		} catch (Exception exception) {
			logger.error("Exception in Uploading File to SFTP Server"+exception.getMessage());
			exception.printStackTrace();
		} 
	}
	
	
	@Override
	@CronTarget(jobName = JobName.EVO_BATCH_PROCESS_DOWNLOAD)
	public void evoBatchProcessDownload() {
		
		logger.info("Processing EVO Batch Download Process .......");
		JobParameterData data = null;
		File fileHandler;
		FileWriter fw;
		JsonElement element;
		String destinationPath, destinationFileName;
		String host, username, merchantId, privateKey, SFTPWORKINGDIR, passphrase;
		host = username = merchantId = privateKey = SFTPWORKINGDIR = passphrase = null;	
		destinationFileName = destinationPath = null;
		int port=0;
		
		try {
			data = this.sheduleJobReadPlatformService.getJobParameters(JobName.EVO_BATCH_PROCESS.toString());

			if (null == data) {
				throw new JobParameterNotConfiguredException(JobName.EVO_BATCH_PROCESS.toString());
			}
			
			final String filePath = FileUtils.generateLogFileDirectory()+JobName.EVO_BATCH_PROCESS.toString()+File.separator
				     +"EvoBatchProcessing_"+DateUtils.getLocalDateTimeOfTenant().toString()+".log";
		    fileHandler = new File(filePath.trim());
		    fileHandler.createNewFile();
		    fw = new FileWriter(fileHandler);
		    FileUtils.BILLING_JOB_PATH = fileHandler.getAbsolutePath();
			
			element = this.fromApiJsonHelper.parse(data.getBatchName());
			JsonObject evoElement = element.getAsJsonObject();
			evoElement.addProperty("locale", "en");
		
		
			if(this.fromApiJsonHelper.parameterExists("host", evoElement)) {
				host = this.fromApiJsonHelper.extractStringNamed("host", evoElement);
			}
			if(this.fromApiJsonHelper.parameterExists("username", evoElement)) {
				username = this.fromApiJsonHelper.extractStringNamed("username", evoElement);
			}
			if(this.fromApiJsonHelper.parameterExists("privateKey", evoElement)) {
				privateKey = this.fromApiJsonHelper.extractStringNamed("privateKey", evoElement);
			}
			if(this.fromApiJsonHelper.parameterExists("merchantId", evoElement)) {
				merchantId = this.fromApiJsonHelper.extractStringNamed("merchantId", evoElement);
			}
			if(this.fromApiJsonHelper.parameterExists("SFTPWORKINGDIR", evoElement)) {
				SFTPWORKINGDIR = this.fromApiJsonHelper.extractStringNamed("SFTPWORKINGDIR", evoElement);
			}
			if(this.fromApiJsonHelper.parameterExists("port", evoElement)) {
				port = this.fromApiJsonHelper.extractIntegerWithLocaleNamed("port", evoElement);
			}
			if(this.fromApiJsonHelper.parameterExists("passphrase", evoElement)) {
				passphrase = this.fromApiJsonHelper.extractStringNamed("passphrase", evoElement);
			}
			
			List<EvoBatchProcessData> evoBatchProcessData = this.evoRecurringBillingReadPlatformService.getUploadedFile();
			
			for(EvoBatchProcessData  evobatchprocess : evoBatchProcessData){
				
				destinationFileName = evobatchprocess.getFileName();
				destinationPath = evobatchprocess.getFilepath();
				logger.info("EVO Batch Download Process destinationPath: "+destinationPath);
				this.evoRecurringBillingWritePlatformService.intializeFTP(host, port, username, privateKey, SFTPWORKINGDIR);
				downloadFileProcess(destinationFileName, destinationPath, passphrase,fw);
				
			}
			
			fw.append("OSM are Generated....." + ThreadLocalContextUtil.getTenant().getTenantIdentifier() + "\r\n");
			fw.flush();
			fw.close();

			logger.info("OSM are Generated....." + ThreadLocalContextUtil.getTenant().getTenantIdentifier());
			
		} catch (DataIntegrityViolationException exception) {
			exception.printStackTrace();
		} catch (Exception exception) {
			logger.error("Exception in Downloading File from SFTP Server"+exception.getMessage());
			exception.printStackTrace();
		} 
		
	}


	private void downloadFileProcess(final String destfileName, final String destination, final String passphrase, FileWriter fw) throws Exception {
		
		logger.info("Processing EVO Batch File Download Process..... ");
		String destfile = destfileName.replaceFirst("W","P");
		String destpath = destination.split("W"+Calendar.getInstance().get(Calendar.YEAR))[0]+destfile;
		//String destinationFilePath = destpath.split(".pgp")[0];
		File tempfile = File.createTempFile("pgp", null);
		
		boolean value = this.evoRecurringBillingWritePlatformService.downloadFTPFile(destfile, destpath);

		if (value) {
			logger.info("EVO Batch File Download from SFTP Successfully..... ");
			EvoBatchProcess evoprocess = this.evoBatchProcessRepository.findOneByFileName(destfileName);
			evoprocess.isdownloaded();
			evoprocess.status();
			this.evoBatchProcessRepository.save(evoprocess);
			
			decryptFile(destpath,tempfile.getAbsolutePath(),passphrase);
			updateIntoPaymentTable(tempfile.getAbsolutePath(),fw);
		}
		tempfile.deleteOnExit();
	}

	private void decryptFile(final String destpath,final String tempararyfile,final String passphrase)  {

		try {
			PGPSecurityFileProcessor pGPSecurityFileProcessor = PGPSecurityFileProcessor
					.intializePGPFP("decrypt", destpath, tempararyfile, passphrase);
			pGPSecurityFileProcessor.decrypt();
		} catch (Exception e) {
			logger.error("Exception While Decrypting File "+tempararyfile+" "+e.getMessage());
			e.printStackTrace();
		}
		logger.info("File has been decrypted");
	}
		
	private void updateIntoPaymentTable(final String tempararyfile, FileWriter fw) {
		
		logger.info("Calling Payment to OBS..... ");
		Scanner scanner;
		List<String> list = new ArrayList<String>();
		JsonObject object = null;
		try {
			scanner = new Scanner(new File(tempararyfile));
			while (scanner.hasNext()) {
				list.add(scanner.nextLine());
			}
			for (int i = 1; i < list.size() - 1; i++) {
				String line = list.get(i);
				String evoresponse[] = line.split(",");

				object = new JsonObject();
				
				object.addProperty("source", ConfigurationConstants.EVO_PAYMENTGATEWAY);
				BigDecimal totalAmount = new BigDecimal(evoresponse[2]).divide(BigDecimal.valueOf(100));
				object.addProperty("total_amount", totalAmount);
				object.addProperty("currency", evoresponse[3]);
				object.addProperty("clientId", evoresponse[4]);
				object.addProperty("statmentId", evoresponse[5].substring(12));
				object.addProperty("cardType", evoresponse[6]);
				object.addProperty("cardNumber", evoresponse[7]);
				
				object.addProperty("locale", "en");
				object.addProperty("transactionId", evoresponse[13]);
				String transStatus = "Success";
			    if(!"ok".equalsIgnoreCase(evoresponse[14])){
			       transStatus = evoresponse[14];
			    }
			    object.addProperty("status", transStatus);
			    
				this.paymentGatewayWritePlatformService.onlinePaymentGateway(object.toString());
				
				fw.append("File Processed and updated Payment in OBS with amount:"+totalAmount+" clientId :"+evoresponse[4]+" statmentId :"+evoresponse[5].substring(12)
						+" transactionId:"+evoresponse[13]+" status:"+evoresponse[14]+" cardType:"+evoresponse[6]+" cardNumber:"+evoresponse[7]
						+" RefNr :"+evoresponse[5]+"\r\n");
				
				logger.info("File Processed and updated Payment in OBS with amount:"+totalAmount+" clientId :"+evoresponse[4]+" statmentId :"+evoresponse[5].substring(12)
						+" transactionId:"+evoresponse[13]+" status:"+evoresponse[14]+" cardType:"+evoresponse[6]+" cardNumber:"+evoresponse[7]+" RefNr :"+evoresponse[5]);
			}
		} catch (IOException  e) {
			logger.error("File Not Found while updating Payment into OBS "+e.getMessage());
			e.printStackTrace();
		}
	}	


	@Override
	@CronTarget(jobName = JobName.SUSPENSION_Of_SERVICE)
	public void suspendOrders() {
		
		try {
			logger.info("Processing Order Suspension Details.......");
			final JobParameterData data = this.sheduleJobReadPlatformService.getJobParameters(JobName.SUSPENSION_Of_SERVICE.toString());

			if (data != null) {
				final String filePath = FileUtils.generateLogFileDirectory()+JobName.SUSPENSION_Of_SERVICE.toString()+File.separator
					     +"OrderSuspension_"+DateUtils.getLocalDateTimeOfTenant().toString()+".log";
			    File fileHandler = new File(filePath.trim());
			    fileHandler.createNewFile();
			    FileWriter fw = new FileWriter(fileHandler);
			    FileUtils.BILLING_JOB_PATH = fileHandler.getAbsolutePath();
			
				
				fw.append("Processing Order Suspension Details....... \r\n");
				List<ScheduleJobData> sheduleDatas = this.sheduleJobReadPlatformService.retrieveSheduleJobParameterDetails(data.getBatchName());
				if (!sheduleDatas.isEmpty()) {
					for (ScheduleJobData scheduleJobData : sheduleDatas) {
						fw.append("ScheduleJobData id=" + scheduleJobData.getId() + " ,BatchName=" + scheduleJobData.getBatchName() + " ,query=" + scheduleJobData.getQuery() + "\r\n");
						List<Long> clientIds = this.sheduleJobReadPlatformService.getClientIds(scheduleJobData.getQuery());
						if (!clientIds.isEmpty()) {
							final BillingMessageTemplate messageTemplate = this.messageTemplateRepository.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SUSPENSION_SERVICE);
							for (Long clientId : clientIds) {
								fw.append("processing Order Suspension for Customer id :" + clientId + "\r\n");
								List<Long> orderIds = this.orderReadPlatformService.retrieveCustomerActiveOrderIds(clientId);
								if (!orderIds.isEmpty()) {
									for (Long orderId : orderIds) {
										this.scheduleJobRunnerService.suspendOrder(orderId, fw, clientId);
									}
									//prepare message data
									final Client client = this.clientRepository.findOne(clientId);
									final String officeMail = client.getOffice().getOfficeAddress().getEmail();
									if((client.getEmail()!=null) && (messageTemplate!=null)){
										String header = messageTemplate.getHeader().replace("<customerName>", client.getFirstname());
										  BillingMessage  billingMessage = new BillingMessage(header, messageTemplate.getBody(), messageTemplate.getFooter(), officeMail, client.getEmail(),  
										    		messageTemplate.getSubject(), "N", messageTemplate, messageTemplate.getMessageType(), null);
										    this.messageDataRepository.save(billingMessage);
									}else{
										fw.append("Please Provide Valid Email for Customer : "+client.getId());
										fw.append("Message Template Not Found With This Template : "+BillingMessageTemplateConstants.MESSAGE_TEMPLATE_SUSPENSION_SERVICE);
								    }
								} else {
									fw.append("No Orders Found for :" + clientId + "\r\n");
								}
							}
						} else {
							fw.append("no records are available for Order Suspension \r\n");
						}
					}
				} else {
					fw.append("Order Suspension ScheduleJobData Empty \r\n");
				}
				fw.append("Order Suspension Job is Completed..." + ThreadLocalContextUtil.getTenant().getTenantIdentifier() + " . \r\n");
				fw.flush();
				fw.close();
			}
			logger.info("Order Suspension Job is Completed..." + ThreadLocalContextUtil.getTenant().getTenantIdentifier());
		} catch (DataIntegrityViolationException | IOException e) {
			logger.error(e.getMessage(),e);
		} catch (Exception dve) {
			logger.error(dve.getMessage());
		}
	}
	
	@Override
	@CronTarget(jobName = JobName.DISCONNECT_SERVICES_OF_UNPAID_CUSTOMERS)
	public void disconnectAllServicesOfUnpaidCustomers() {

		try {
			logger.info("Processing Unpaid Customers Details.......");
			final JobParameterData data = this.sheduleJobReadPlatformService.getJobParameters(JobName.DISCONNECT_SERVICES_OF_UNPAID_CUSTOMERS.toString());

			if (data != null) {
				final String filePath = FileUtils.generateLogFileDirectory()+JobName.DISCONNECT_SERVICES_OF_UNPAID_CUSTOMERS.toString()+File.separator
					     +"UnpaidCustomerServices_"+DateUtils.getLocalDateTimeOfTenant().toString()+".log";
			    File fileHandler = new File(filePath.trim());
			    fileHandler.createNewFile();
			    FileWriter fw = new FileWriter(fileHandler);
			    FileUtils.BILLING_JOB_PATH = fileHandler.getAbsolutePath();
				
				final BillingMessageTemplate messageTemplate = this.messageTemplateRepository.findByTemplateDescription(BillingMessageTemplateConstants.MESSAGE_TEMPLATE_DISCONNECTION_OF_SERVICES);

				fw.append("Processing Unpaid Customers Details....... \r\n");
				List<ScheduleJobData> sheduleDatas = this.sheduleJobReadPlatformService.retrieveSheduleJobParameterDetails(data.getBatchName());

				if (!sheduleDatas.isEmpty()) {
					for (ScheduleJobData scheduleJobData : sheduleDatas) {
						fw.append("ScheduleJobData id=" + scheduleJobData.getId() + " ,BatchName=" + scheduleJobData.getBatchName() + " ,query=" + scheduleJobData.getQuery() + "\r\n");
						List<Long> clientIds = this.sheduleJobReadPlatformService.getClientIds(scheduleJobData.getQuery());
						if (!clientIds.isEmpty()) {
							for (Long clientId : clientIds) {
								fw.append("processing Unpaid Customer id :" + clientId + "\r\n");
								List<Long> orders = this.orderReadPlatformService.retrieveCustomerSuspendedOrderIds(clientId);
								if (!orders.isEmpty()) {
									for (Long order : orders) {
										this.scheduleJobRunnerService.ProcessDisconnectUnPaidCustomers(order, fw, clientId);
									}
								//prepare message data
									final Client client = this.clientRepository.findOne(clientId);
									final String officeMail = client.getOffice().getOfficeAddress().getEmail();
									if((client.getEmail() != null) && (messageTemplate!=null)){
										String header = messageTemplate.getHeader().replace("<customerName>", client.getFirstname());
										  BillingMessage  billingMessage = new BillingMessage(header, messageTemplate.getBody(), messageTemplate.getFooter(), officeMail, client.getEmail(),  
										    		messageTemplate.getSubject(), "N", messageTemplate, messageTemplate.getMessageType(), null);
										    this.messageDataRepository.save(billingMessage);
									}else{
										logger.error("Please Provide Valid Email for Customer : "+client.getId());
										logger.error("Message Template Not Found With This Template : "+BillingMessageTemplateConstants.MESSAGE_TEMPLATE_DISCONNECTION_OF_SERVICES);
								    }
								} else {
									fw.append("No Orders are Found for :" + clientId + "\r\n");
								}
							}
						} else {
							fw.append("no records are available for Disconnect Services of Unpaid Customers \r\n");
						}
					}
				} else {
					fw.append("Disconnect Services of Unpaid Customers ScheduleJobData Empty \r\n");
				}
				fw.append("Disconnect Services of Unpaid Customers Job is Completed..." + ThreadLocalContextUtil.getTenant().getTenantIdentifier() + " . \r\n");
				fw.flush();
				fw.close();
			}
			logger.info("Disconnect Services of Unpaid Customers Job is Completed..." + ThreadLocalContextUtil.getTenant().getTenantIdentifier());
		} catch (DataIntegrityViolationException | IOException e) {
			logger.error(e.getMessage(),e);
			e.printStackTrace();
		} catch (Exception dve) {
			logger.error(dve.getMessage());
		}
	}


	@Override
	@CronTarget(jobName = JobName.FOLLOWUP_TICKETS)
	public void followUpTickets() {

	try {
		logger.info("Open Tickets Moving To FollowUp  is Processing.... ");
		final JobParameterData data = this.sheduleJobReadPlatformService.getJobParameters(JobName.FOLLOWUP_TICKETS.toString());
		
		if (data != null) {
			final String filePath = FileUtils.generateLogFileDirectory()+JobName.DISCONNECT_SERVICES_OF_UNPAID_CUSTOMERS.toString()+File.separator
				     +"UnpaidCustomerServices_"+DateUtils.getLocalDateTimeOfTenant().toString()+".log";
		    File fileHandler = new File(filePath.trim());
				fileHandler.createNewFile();
		    FileWriter fw = new FileWriter(fileHandler);
		    FileUtils.BILLING_JOB_PATH = fileHandler.getAbsolutePath();
		    
		    fw.append("Open Tickets Moving To FollowUp is Processing.... \r\n");
			List<ScheduleJobData> sheduleDatas = this.sheduleJobReadPlatformService.retrieveSheduleJobParameterDetails(data.getBatchName());
			if (!sheduleDatas.isEmpty()) {
				for (ScheduleJobData scheduleJobData : sheduleDatas) {
					fw.append("ScheduleJobData id=" + scheduleJobData.getId() + " ,BatchName=" + scheduleJobData.getBatchName() + " ,query=" + scheduleJobData.getQuery() + "\r\n");
					List<Long> ticketIds = this.sheduleJobReadPlatformService.getTicketIds(scheduleJobData.getQuery());
					if(!ticketIds.isEmpty()){
						for(Long ticketId : ticketIds){
						   TicketMaster ticket = this.ticketMasterRepository.findOne(ticketId);
						   if(ticket.getStatus().equalsIgnoreCase("open")){
							   ticket.setStatus("FollowUp");
						   }
						   this.ticketMasterRepository.save(ticket);
						   //Send Email To Customer as well as AssignedUser When Ticket Moving To FollowUp status
						   this.orderWritePlatformService.processNotifyMessages(EventActionConstants.EVENT_EDIT_TICKET, ticket.getClientId(), ticket.getId().toString(), null);
						}
					}else {
						fw.append("no records are available for Open Tickets Moving To FollowUp \r\n");
					}
				}
			}else{
				fw.append("FollowUp Ticket ScheduleJobData Empty \r\n");
			}
			fw.append("Open Tickets Moving To FollowUp Job is Completed..." + ThreadLocalContextUtil.getTenant().getTenantIdentifier() + " . \r\n");
			fw.flush();
			fw.close();
		}
		logger.info("Open Tickets Moving To FollowUp Job is Completed..." + ThreadLocalContextUtil.getTenant().getTenantIdentifier());  
		
	}catch (DataIntegrityViolationException | IOException e) {
		logger.error(e.getMessage(),e);
		e.printStackTrace();
	}
		
		
	}
	
	@Override
	@CronTarget(jobName = JobName.FOLLOWUP_TO_OPEN)
	public void followupToOpen() {

	try {
		logger.info("Followup tickets moving to open is Processing..... ");
		final JobParameterData data = this.sheduleJobReadPlatformService.getJobParameters(JobName.FOLLOWUP_TO_OPEN.toString());
		
		if (data != null) {
			final String filePath = FileUtils.generateLogFileDirectory()+JobName.DISCONNECT_SERVICES_OF_UNPAID_CUSTOMERS.toString()+File.separator
				     +"UnpaidCustomerServices_"+DateUtils.getLocalDateTimeOfTenant().toString()+".log";
		    File fileHandler = new File(filePath.trim());
				fileHandler.createNewFile();
		    FileWriter fw = new FileWriter(fileHandler);
		    FileUtils.BILLING_JOB_PATH = fileHandler.getAbsolutePath();
		    
		    fw.append("Followup tickets moving to open is Processing.... \r\n");
			List<ScheduleJobData> sheduleDatas = this.sheduleJobReadPlatformService.retrieveSheduleJobParameterDetails(data.getBatchName());
			if (!sheduleDatas.isEmpty()) {
				for (ScheduleJobData scheduleJobData : sheduleDatas) {
					fw.append("ScheduleJobData id=" + scheduleJobData.getId() + " ,BatchName=" + scheduleJobData.getBatchName() + " ,query=" + scheduleJobData.getQuery() + "\r\n");
					List<Long> ticketIds = this.sheduleJobReadPlatformService.getTicketIds(scheduleJobData.getQuery());
					if(!ticketIds.isEmpty()){
						for(Long ticketId : ticketIds){
						   TicketMaster ticket = this.ticketMasterRepository.findOne(ticketId);
						   if(ticket.getStatus().equalsIgnoreCase("FollowUp")){
							   ticket.setStatus("open");
							   ticket.setPriority("HIGH");
						   }
						   this.ticketMasterRepository.save(ticket);
						   //Send Email To Customer as well as AssignedUser When Ticket Moving To FollowUp status
						   this.orderWritePlatformService.processNotifyMessages(EventActionConstants.EVENT_EDIT_TICKET, ticket.getClientId(), ticket.getId().toString(), null);
						}
					}else {
						fw.append("no records are available for followup Tickets Moving To open \r\n");
					}
				}
			}else{
				fw.append("FOLLOWUP_TO_OPEN Ticket ScheduleJobData Empty \r\n");
			}
			fw.append("FollowUp Tickets Moving To Open Job is Completed..." + ThreadLocalContextUtil.getTenant().getTenantIdentifier() + " . \r\n");
			fw.flush();
			fw.close();
		}
		logger.info("FollowUp Tickets Moving To Open Job is Completed..." + ThreadLocalContextUtil.getTenant().getTenantIdentifier());  
		
	}catch (DataIntegrityViolationException | IOException e) {
		logger.error(e.getMessage(),e);
		e.printStackTrace();
	}
		
		
	}

	@Override
	@CronTarget(jobName = JobName.WORLDPAY_RECURRING_BATCH_PROCESS)
	public void worldpayRecurringBatchProcess() {
			
			logger.info("****------Processing WorldPay Recurring  Batch process started--------*******");
			JobParameterData data = null;

			List<WorldpayBatchData> worldpayBatchDatas;
			
			try {
				
				data = this.sheduleJobReadPlatformService.getJobParameters(JobName.WORLDPAY_RECURRING_BATCH_PROCESS.toString());
				if (null == data) {
					throw new JobParameterNotConfiguredException(JobName.WORLDPAY_RECURRING_BATCH_PROCESS.toString());
				}
				
				worldpayBatchDatas = this.sheduleJobReadPlatformService.getUnProcessedWorldPayRecurringData();
				logger.info("****-----No of Un Process records For Worldpay Recurring Payments-****"+worldpayBatchDatas.size());
				if(worldpayBatchDatas.size()>0) {
					
					for (WorldpayBatchData worldpayBatchData : worldpayBatchDatas) {
						logger.info("****Processing Paymet...For Client Id --------*****"+worldpayBatchData.getClientId());
						JsonObject jsonData=new JsonObject();
						jsonData.addProperty("gettoken", worldpayBatchData.getW_token());
						jsonData.addProperty("totalamount", worldpayBatchData.getAmount());
						jsonData.addProperty("orderType", worldpayBatchData.getR_type());
						jsonData.addProperty("orderCode", worldpayBatchData.getChargeId());
						jsonData.addProperty("name", worldpayBatchData.getName());
						CommandProcessingResult result=this.paymentGatewayWritePlatformService.createOrderWorldpayRecurring(worldpayBatchData.getClientId(), jsonData.toString());
						logger.info("****Sucess fully updated client payment******"+result.getClientId());
					}
				}

				logger.info("..........Worldpay Recurring Completed sucessfully .............." + ThreadLocalContextUtil.getTenant().getTenantIdentifier());
				
			} catch (DataIntegrityViolationException exception) {
				exception.printStackTrace();
			} catch (Exception exception) {
				logger.error("Exception Worldpay Recurring Process "+exception.getMessage());
				exception.printStackTrace();
			} 
		}


	@Override
	@CronTarget(jobName = JobName.LOG_FILES_REMOVE)
	public void logFilesRemoving() {
		
	 logger.info("****------Processing Log Files Remove Job started--------*******");
	 
		SimpleDateFormat sf = new SimpleDateFormat("yyyy-MM");
		Date today = new Date();
		String currentDate = sf.format(today);

		String path = FileUtils.generateLogFileDirectory();
		
		File[] files = new File(path).listFiles();
		
		for(File file : files){
			if(file.isDirectory()){
				if(file.getName().equalsIgnoreCase("REQUESTOR")){
					String path1=path+"REQUESTOR";
					//String fileName = "Requester";
					File[] files1 = new File(path1).listFiles();
					for(File file1 : files1){
						//if(file1.isFile() && !(currentDate.equals(file1.getName().substring(fileName.length()+1, fileName.length()+8)))){
						if(file1.isFile()){
							boolean f = file1.delete();
						}
					}
				}if(file.getName().equalsIgnoreCase("EVENT_ACTIONS")){
					String path2=path+"EVENT_ACTIONS";
					File[] files1 = new File(path2).listFiles();
					for(File file1 : files1){
						if(file1.isFile() && !(currentDate.equals(file1.getName().substring(file.getName().length(), file.getName().length()+7)))){
							file1.delete();
						}
					}
				}else if(file.getName().equalsIgnoreCase("MERGE_MESSAGE")){
					String path3=path+"MERGE_MESSAGE";
					String fileName = "Messanger";
					File[] files1 = new File(path3).listFiles();
					for(File file1 : files1){
						if(file1.isFile() && !(currentDate.equals(file1.getName().substring(fileName.length()+1, fileName.length()+8)))){
							file1.delete();
						}
					}
				}else if(file.getName().equalsIgnoreCase("MESSAGING")){
					String path4=path+"MESSAGING";
					//String fileName = "PushNotification";
					File[] files1 = new File(path4).listFiles();
					for(File file1 : files1){
						//if(file1.isFile() && !(currentDate.equals(file1.getName().substring(fileName.length()+1, fileName.length()+8)))){
						if(file1.isFile()){
							boolean f = file1.delete();
						}
					}
				}else if(file.getName().equalsIgnoreCase("INVOICING")){
					String path5=path+"INVOICING";
					String fileName = "Invoice";
					File[] files1 = new File(path5).listFiles();
					for(File file1 : files1){
						if(file1.isFile() && !(currentDate.equals(file1.getName().substring(fileName.length()+1, fileName.length()+8)))){
							file1.delete();
						}
					}
				}else if(file.getName().equalsIgnoreCase("STATEMENT")){
					String path6=path+"STATEMENT";
					String fileName = "Ststement";
					File[] files1 = new File(path6).listFiles();
					for(File file1 : files1){
						if(file1.isFile() && !(currentDate.equals(file1.getName().substring(fileName.length()+1, fileName.length()+8)))){
							file1.delete();
						}
					}
				}else if(file.getName().equalsIgnoreCase("DISCONNECT_SERVICES_OF_UNPAID_CUSTOMERS")){
					String path7=path+"DISCONNECT_SERVICES_OF_UNPAID_CUSTOMERS";
					String fileName = "UnpaidCustomerServices";
					File[] files1 = new File(path7).listFiles();
					for(File file1 : files1){
						if(file1.isFile() && !(currentDate.equals(file1.getName().substring(fileName.length()+1, fileName.length()+8)))){
							file1.delete();
						}
					}
				}else if(file.getName().equalsIgnoreCase("SUSPENSION_Of_SERVICE")){
					String path8=path+"SUSPENSION_Of_SERVICE";
					String fileName = "OrderSuspension";
					File[] files1 = new File(path8).listFiles();
					for(File file1 : files1){
						if(file1.isFile() && !(currentDate.equals(file1.getName().substring(fileName.length()+1, fileName.length()+8)))){
							file1.delete();
						}
					}
				}
			}
		}
		logger.info("****------Log Files Remove Job Completed--------*******");
	}
}
