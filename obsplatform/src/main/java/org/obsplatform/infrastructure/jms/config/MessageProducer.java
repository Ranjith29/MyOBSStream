package org.obsplatform.infrastructure.jms.config;


import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.Session;

import org.apache.activemq.command.ActiveMQQueue;
import org.json.JSONObject;
import org.obsplatform.organisation.message.data.BillingMessageDataForProcessing;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.jms.core.MessageCreator;
import org.springframework.stereotype.Component;

@Component
public class MessageProducer {
	
	@Autowired
	private JmsTemplate jmsTemplate;
	
	/*@Autowired
	private Destination destination;
	*/
	
	@Autowired
	private ActiveMQQueue messageDestination;
	
	
	public JmsTemplate getJmsTemplate() {
		return jmsTemplate;
	}

	public void setJmsTemplate(JmsTemplate jmsTemplate) {
		this.jmsTemplate = jmsTemplate;
	}
	
	/*public Destination getDestination() {
		return destination;
	}

	public void setDestination(Destination destination) {
		this.destination = destination;
	}*/

	
	public ActiveMQQueue getMessageDestination() {
		return messageDestination;
	}

	public void setMessageDestination(ActiveMQQueue messageDestination) {
		this.messageDestination = messageDestination;
	}

	
	//public void sendMessage(final BillingMessageDataForProcessing emailDetail) throws Exception {
	public void sendMessage(final String emailDetail) throws Exception {	
	
		//jmsTemplate.send(destination, new MessageCreator() {
		jmsTemplate.send(messageDestination, new MessageCreator() {
		
		@Override
			public Message createMessage(Session session) throws JMSException {
				//return session.createObjectMessage(emailDetail);
			return session.createTextMessage(emailDetail);
			}});		
		System.out.println("activemq producer"+messageDestination.toString());
	}
}