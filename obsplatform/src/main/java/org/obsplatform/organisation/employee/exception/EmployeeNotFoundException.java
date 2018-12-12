package org.obsplatform.organisation.employee.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformDomainRuleException;

public class EmployeeNotFoundException extends AbstractPlatformDomainRuleException {

	private static final long serialVersionUID = 1L;

	/**
	 * @param chargeCodeId
	 */
	public EmployeeNotFoundException(final String employeeId) {
		super("error.msg.employee.not.found", "employee with this id" + employeeId + "not exist", employeeId);

	}
}
