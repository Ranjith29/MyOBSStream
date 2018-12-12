package org.obsplatform.organisation.department.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when department resources are not found.
 */
public class DepartmentNotFoundException extends AbstractPlatformResourceNotFoundException {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public DepartmentNotFoundException(final Long id) {
		super("error.msg.department.id.invalid", "Department with identifier " + id + " does not exist", id);
	}

	public DepartmentNotFoundException(String deptName) {
		super("error.msg.department.id.invalid", "Department with identifier " + deptName + " does not exist", deptName);
	}
}
