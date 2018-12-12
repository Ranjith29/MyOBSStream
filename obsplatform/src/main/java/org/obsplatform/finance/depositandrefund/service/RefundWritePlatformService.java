package org.obsplatform.finance.depositandrefund.service;

import net.sf.ehcache.transaction.xa.commands.Command;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;

public interface RefundWritePlatformService {

	CommandProcessingResult createRefund(JsonCommand command, Long depositId);

}
