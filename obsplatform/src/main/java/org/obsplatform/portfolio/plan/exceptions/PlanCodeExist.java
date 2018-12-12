package org.obsplatform.portfolio.plan.exceptions;
import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

@SuppressWarnings("serial")
public class PlanCodeExist extends AbstractPlatformDomainRuleException {

		public PlanCodeExist(final String planCode) {
			super("service.is.already.exists.with.plan.code", "service is already existed with plan code:"+planCode, planCode);
		}

	}