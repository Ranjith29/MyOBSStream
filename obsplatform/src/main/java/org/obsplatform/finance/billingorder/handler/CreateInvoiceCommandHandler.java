package org.obsplatform.finance.billingorder.handler;

import org.obsplatform.commands.handler.NewCommandSourceHandler;
import org.obsplatform.finance.billingorder.service.InvoiceClient;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class CreateInvoiceCommandHandler  implements NewCommandSourceHandler {
	
	private final InvoiceClient invoiceClient;
	
	@Autowired
    public CreateInvoiceCommandHandler(final InvoiceClient invoiceClient) {
        this.invoiceClient = invoiceClient;
    }
	
    @Transactional
    @Override
    public CommandProcessingResult processCommand(final JsonCommand command) {

        return this.invoiceClient.createInvoiceBill(command);
    }
    

}
