/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.template.service;

import java.util.List;

import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.data.CommandProcessingResult;
import org.obsplatform.template.domain.Template;
import org.obsplatform.template.domain.TemplateEntity;
import org.obsplatform.template.domain.TemplateType;

public interface TemplateService {

	CommandProcessingResult createTemplate(final JsonCommand command);

	CommandProcessingResult updateTemplate(final Long templateId,final JsonCommand command);

	CommandProcessingResult removeTemplate(final Long templateId);

	List<Template> getAll();

	List<Template> getAllByEntityAndType(TemplateEntity entity,TemplateType type);

	Template findOneById(Long id);

	//Template updateTemplate(Template template);
}
