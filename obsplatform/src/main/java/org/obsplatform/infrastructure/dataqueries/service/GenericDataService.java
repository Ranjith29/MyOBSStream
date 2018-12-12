/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.infrastructure.dataqueries.service;

import java.util.List;

import org.obsplatform.infrastructure.dataqueries.data.GenericResultsetData;
import org.obsplatform.infrastructure.dataqueries.data.ResultsetColumnHeaderData;

/**
 * @author hugo
 * 
 */
public interface GenericDataService {

	GenericResultsetData fillGenericResultSet(final String sql);

	String generateJsonFromGenericResultsetData(GenericResultsetData grs);

	String replace(String str, String pattern, String replace);

	String wrapSQL(String sql);

	List<ResultsetColumnHeaderData> fillResultsetColumnHeaders(String datatable);
}