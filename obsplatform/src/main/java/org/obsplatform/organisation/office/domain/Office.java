/**
 * This Source Code Form is subject to the terms of the Mozilla Public
 * License, v. 2.0. If a copy of the MPL was not distributed with this file,
 * You can obtain one at http://mozilla.org/MPL/2.0/.
 */
package org.obsplatform.organisation.office.domain;

import java.util.Date;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.FetchType;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.OneToMany;
import javax.persistence.OneToOne;
import javax.persistence.Table;
import javax.persistence.Temporal;
import javax.persistence.TemporalType;
import javax.persistence.UniqueConstraint;

import org.apache.commons.lang.StringUtils;
import org.hibernate.annotations.LazyCollection;
import org.hibernate.annotations.LazyCollectionOption;
import org.joda.time.LocalDate;
import org.obsplatform.infrastructure.core.api.JsonCommand;
import org.obsplatform.infrastructure.core.service.DateUtils;
import org.obsplatform.organisation.office.exception.CannotUpdateOfficeWithParentOfficeSameAsSelf;
import org.obsplatform.organisation.office.exception.RootOfficeParentCannotBeUpdated;
import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "m_office", uniqueConstraints = { @UniqueConstraint(columnNames = { "name" }, name = "name_org"),
        @UniqueConstraint(columnNames = { "external_id" }, name = "externalid_org") })
public class Office extends AbstractPersistable<Long> {

    /**
	 * 
	 */
	private static final long serialVersionUID = -1760184005999519057L;

	@OneToMany(fetch = FetchType.EAGER)
    @JoinColumn(name = "parent_id")
    private final List<Office> children = new LinkedList<>();

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "parent_id")
    private Office parent;

    @Column(name = "name", nullable = false, length = 100)
    private String name;

    @Column(name = "hierarchy", nullable = true, length = 50)
    private String hierarchy;

    @Column(name = "opening_date", nullable = false)
    @Temporal(TemporalType.DATE)
    private Date openingDate;
    
    @Column(name="office_type",nullable = false)
    private Long officeType;

    @Column(name = "external_id", length = 100)
    private String externalId;
    
    @Column(name = "partner_type_id", length = 100)
    private Long partnerTypeId;
    
	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToOne(cascade = CascadeType.ALL, mappedBy = "office", orphanRemoval = true)
	private OfficeAddress officeAddress;
	
	@LazyCollection(LazyCollectionOption.FALSE)
	@OneToOne(cascade = CascadeType.ALL, mappedBy = "office", orphanRemoval = true)
	private OfficeAdditionalInfo officeAdditionalInfo;
	
    public static Office headOffice(final String name, final LocalDate openingDate, final String externalId) {
        return new Office(null, name, openingDate, externalId,null, null);
    }

    public static Office fromJson(final Office parentOffice, final JsonCommand command) {

        final String name = command.stringValueOfParameterNamed("name");
        final LocalDate openingDate = command.localDateValueOfParameterNamed("openingDate");
        final String externalId = command.stringValueOfParameterNamed("externalId");
        final Long officeType = command.longValueOfParameterNamed("officeType");
        return new Office(parentOffice, name, openingDate, externalId, officeType,null);
    }

    protected Office() {
        this.openingDate = null;
        this.parent = null;
        this.name = null;
        this.externalId = null;
    }

    public List<Office> getChildren() {
		return children;
	}

	public Office getParent() {
		return parent;
	}

	public Date getOpeningDate() {
		return openingDate;
	}

	public Long getOfficeType() {
		return officeType;
	}

	public String getExternalId() {
		return externalId;
	}

	private Office(final Office parent, final String name, final LocalDate openingDate, final String externalId, 
			final Long officeType, final Long partnerTypeId) {
        this.parent = parent;
        this.openingDate = openingDate.toDateMidnight().toDate();
        if (parent != null) {
            this.parent.addChild(this);
        }

        if (StringUtils.isNotBlank(name)) {
            this.name = name.trim();
        } else {
            this.name = null;
        }
        if (StringUtils.isNotBlank(externalId)) {
            this.externalId = externalId.trim();
        } else {
            this.externalId = null;
        }
        
        this.officeType=officeType;
        this.partnerTypeId = partnerTypeId;
    }

	private void addChild(final Office office) {
        this.children.add(office);
    }

    public Map<String, Object> update(final JsonCommand command) {

        final Map<String, Object> actualChanges = new LinkedHashMap<String, Object>(7);

        final String dateFormatAsInput = command.dateFormat();
        final String localeAsInput = command.locale();

        final String parentIdParamName = "parentId";

        if (command.parameterExists(parentIdParamName) && this.parent == null) { throw new RootOfficeParentCannotBeUpdated(); }

        if (this.parent != null && command.isChangeInLongParameterNamed(parentIdParamName, this.parent.getId())) {
            final Long newValue = command.longValueOfParameterNamed(parentIdParamName);
            actualChanges.put(parentIdParamName, newValue);
        }

        final String openingDateParamName = "openingDate";
        if (command.isChangeInLocalDateParameterNamed(openingDateParamName, getOpeningLocalDate())) {
            final String valueAsInput = command.stringValueOfParameterNamed(openingDateParamName);
            actualChanges.put(openingDateParamName, valueAsInput);
            actualChanges.put("dateFormat", dateFormatAsInput);
            actualChanges.put("locale", localeAsInput);

            final LocalDate newValue = command.localDateValueOfParameterNamed(openingDateParamName);
            this.openingDate = newValue.toDate();
        }

        final String nameParamName = "name";
        if (command.isChangeInStringParameterNamed(nameParamName, this.name)) {
            final String newValue = command.stringValueOfParameterNamed(nameParamName);
            actualChanges.put(nameParamName, newValue);
            this.name = newValue;
        }
        
        final String officeTypeParam = "officeType";
        if(command.isChangeInLongParameterNamed(officeTypeParam, this.officeType)){
        	
        	final Long newValue = command.longValueOfParameterNamed(officeTypeParam);
        	actualChanges.put(officeTypeParam, newValue);
        	this.officeType = newValue;
        	
        }

        final String externalIdParamName = "externalId";
        if (command.isChangeInStringParameterNamed(externalIdParamName, this.externalId)) {
            final String newValue = command.stringValueOfParameterNamed(externalIdParamName);
            actualChanges.put(externalIdParamName, newValue);
            this.externalId = StringUtils.defaultIfEmpty(newValue, null);
        }
        
        final String partnernameParamName = "partnerName";
        if (command.isChangeInStringParameterNamed(partnernameParamName, this.name)) {
            final String newValue = command.stringValueOfParameterNamed(partnernameParamName);
            actualChanges.put(partnernameParamName, newValue);
            this.name = newValue;
        }
        
        final String partnerTypeIdParamName = "partnerTypeId";
        if (command.isChangeInLongParameterNamed(partnerTypeIdParamName, this.partnerTypeId)) {
            final Long newValue = command.longValueOfParameterNamed(partnerTypeIdParamName);
            actualChanges.put(partnerTypeIdParamName, newValue);
            this.partnerTypeId = newValue;
        }

        return actualChanges;
    }

    public boolean isOpeningDateBefore(final LocalDate baseDate) {
        return getOpeningLocalDate().isBefore(baseDate);
    }

    public boolean isOpeningDateAfter(final LocalDate activationLocalDate) {
        return getOpeningLocalDate().isAfter(activationLocalDate);
    }

    private LocalDate getOpeningLocalDate() {
        LocalDate openingLocalDate = null;
        if (this.openingDate != null) {
            openingLocalDate = LocalDate.fromDateFields(this.openingDate);
        }
        return openingLocalDate;
    }

    public void update(final Office newParent) {

        if (this.parent == null) { throw new RootOfficeParentCannotBeUpdated(); }

        if (this.identifiedBy(newParent.getId())) { throw new CannotUpdateOfficeWithParentOfficeSameAsSelf(this.getId(), newParent.getId()); }

        this.parent = newParent;
        generateHierarchy();
    }

    public boolean identifiedBy(final Long id) {
        return getId().equals(id);
    }

    public void generateHierarchy() {

        if (parent != null) {
            this.hierarchy = this.parent.hierarchyOf(getId());
        } else {
            this.hierarchy = ".";
        }
    }

    private String hierarchyOf(final Long id) {
        return this.hierarchy + id.toString() + ".";
    }

    public String getName() {
        return this.name;
    }

    public String getHierarchy() {
        return hierarchy;
    }

    public boolean hasParentOf(final Office office) {
        boolean isParent = false;
        if (this.parent != null) {
            isParent = this.parent.equals(office);
        }
        return isParent;
    }

    public boolean doesNotHaveAnOfficeInHierarchyWithId(final Long officeId) {
        return !this.hasAnOfficeInHierarchyWithId(officeId);
    }

    private boolean hasAnOfficeInHierarchyWithId(final Long officeId) {

        boolean match = false;

        if (identifiedBy(officeId)) {
            match = true;
        }

        if (!match) {
            for (final Office child : this.children) {
                final boolean result = child.hasAnOfficeInHierarchyWithId(officeId);

                if (result) {
                    match = result;
                    break;
                }
            }
        }

        return match;
    }

	public static Office fromPartner(final Office parentOffice,final JsonCommand command) {

		final String name = command.stringValueOfParameterNamed("partnerName");
		final LocalDate openingDate = DateUtils.getLocalDateOfTenant();
		 final String externalId = command.stringValueOfParameterNamed("externalId");
		final Long officeType = command.longValueOfParameterNamed("officeType");
		final Long partnerTypeId = command.longValueOfParameterNamed("partnerTypeId");
		return new Office(parentOffice, name, openingDate, externalId,officeType, partnerTypeId);
	}

	
	public OfficeAddress getOfficeAddress() {
		return officeAddress;
	}

	
	public void setParent(Office parent) {
		this.parent = parent;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setHierarchy(String hierarchy) {
		this.hierarchy = hierarchy;
	}

	public void setOpeningDate(Date openingDate) {
		this.openingDate = openingDate;
	}

	public void setOfficeType(Long officeType) {
		this.officeType = officeType;
	}

	public void setExternalId(String externalId) {
		this.externalId = externalId;
	}

	public void setOfficeAddress(OfficeAddress officeAddress) {
		this.officeAddress = officeAddress;
	}

	public OfficeAdditionalInfo getOfficeAdditionalInfo() {
		return officeAdditionalInfo;
	}

	public void setOfficeAdditionalInfo(OfficeAdditionalInfo officeAdditionalInfo) {
		this.officeAdditionalInfo = officeAdditionalInfo;
	}

	public void setPartnerTypeId(Long partnerTypeId) {
		this.partnerTypeId = partnerTypeId;
	}
}
