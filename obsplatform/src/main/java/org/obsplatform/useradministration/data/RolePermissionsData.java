/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.useradministration.data;

import java.util.Collection;

import org.obsplatform.useradministration.data.PermissionData;

/**
 * Immutable data object representing a role with associated permissions.
 */
public class RolePermissionsData {

	private final Long id;
	private final String name;
	private final String description;
	private final Boolean disabled;
	private final Collection<PermissionData> permissionUsageData;

	public RolePermissionsData(final Long id, final String name,final String description, final Boolean disabled,
			final Collection<PermissionData> permissionUsageData) {
		this.id = id;
		this.name = name;
		this.description = description;
		this.disabled = disabled;
		this.permissionUsageData = permissionUsageData;
	}

	public Long getId() {
		return id;
	}

	public String getName() {
		return name;
	}

	public String getDescription() {
		return description;
	}

	public Boolean getDisabled() {
		return disabled;
	}

	public Collection<PermissionData> getPermissionUsageData() {
		return permissionUsageData;
	}
	
}