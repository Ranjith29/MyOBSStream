package org.obsplatform.infrastructure.jms.config;

import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageListener;
import javax.jms.TextMessage;

import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.command.ActiveMQQueue;
import org.json.JSONObject;
import org.obsplatform.organisation.message.data.BillingMessageDataForProcessing;
import org.obsplatform.organisation.message.service.MessagePlatformEmailService;
import org.obsplatform.provisioning.processscheduledjobs.service.SheduleJobReadPlatformService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.annotation.JmsListener;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

import com.google.gson.Gson;

@Component
public class MessageConsumer  implements MessageListener{
	@Autowired
	private JmsTemplate jmsTemplate;
	
	/*@Autowired
	private Destination destination;
	*/
	
	@Autowired
	private ActiveMQQueue messageDestination;
	
	@Autowired
	ActiveMQConnectionFactory connectionFactory;
	
	
	@Autowired
	private MessagePlatformEmailService messagePlatformEmailService;
	
	@Autowired
	private SheduleJobReadPlatformService sheduleJobReadPlatformService;
	
	public JmsTemplate getJmsTemplate() {
		return jmsTemplate;
	}

	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}
	
	                                                               
	
	//@JmsListener(destination ="messageDestination")
	public void receiveMessage(String msg) throws JMSException {
		
			
        //BillingMessageDataForProcessing emailDetail = (BillingMessageDataForProcessing) jmsTemplate.receive(destination);	
		//BillingMessageDataForProcessing emailDetail = (BillingMessageDataForProcessing) jmsTemplate.receive(messageDestination);	
		
		//Message emailDetailtext = jmsTemplate.receive(messageDestination);
					
		Gson gson = new Gson();  
		//String emailDetailjson = gson.toJson(msg);
		BillingMessageDataForProcessing emailDetail = gson.fromJson(msg.toString(), BillingMessageDataForProcessing.class);
	  
	    
		System.out.println("Consumer receives " +emailDetail.getId());
		if(emailDetail.getMessageType()=='E'){
				
				String Result=this.messagePlatformEmailService.sendToUserEmail(emailDetail);
				
			    // String Result=this.messagePlatformEmailService.sendToUserEmail(emailDetail);
			   
				System.out.println("b_message_data processing id="+emailDetail.getId()+"-- and Result :"+Result+" ... \r\n");
			}else if(emailDetail.getMessageType()=='M'){		
				String message = this.sheduleJobReadPlatformService.retrieveMessageData(emailDetail.getId());
				String Result=this.messagePlatformEmailService.sendToUserMobile(message,emailDetail.getId(),emailDetail.getMessageTo(),emailDetail.getBody());	
				System.out.println("b_message_data processing id="+emailDetail.getId()+"-- and Result:"+Result+" ... \r\n");	
			}else{
				System.out.println("Message Type Unknown ..\r\n");
			}
	}

	@Override
	public void onMessage(Message message) {

		if (message instanceof TextMessage) {
            try {
            	String msg = ((TextMessage) message).getText();
            	System.out.println("Message in consumer " );
                receiveMessage(msg);
                System.out.println("Message has been consumed : " + msg);
            } catch (JMSException ex) {
                throw new RuntimeException(ex);
            }
        } else {
            throw new IllegalArgumentException("Message Error");
        }
		
	}
}