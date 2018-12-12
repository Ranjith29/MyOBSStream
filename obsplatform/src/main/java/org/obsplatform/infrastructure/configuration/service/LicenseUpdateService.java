package org.obsplatform.infrastructure.configuration.service;

import javax.servlet.ServletRequest;

import org.obsplatform.infrastructure.configuration.data.LicenseData;
import org.obsplatform.infrastructure.core.domain.ObsPlatformTenant;

public interface LicenseUpdateService {

	void updateLicenseKey(ServletRequest req, ObsPlatformTenant tenant);
	
	boolean checkIfKeyIsValid(String licenseKey, ObsPlatformTenant tenant);

	LicenseData getLicenseDetails(String licenseKey);

}
