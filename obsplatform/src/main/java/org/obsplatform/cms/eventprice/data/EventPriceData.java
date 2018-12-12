/**
 * 
 */
package org.obsplatform.cms.eventprice.data;

import java.math.BigDecimal;
import java.util.Collection;
import java.util.List;

import org.obsplatform.billing.discountmaster.data.DiscountMasterData;
import org.obsplatform.cms.mediadetails.data.MediaAssetLocationDetails;
import org.obsplatform.infrastructure.core.data.EnumOptionData;
import org.obsplatform.organisation.mcodevalues.data.MCodeData;

/**
 * POJO for {@link EventPricing}
 * 
 * @author pavani
 *
 */
public class EventPriceData {

	private Long id;
	private Long eventId;
	private String formatType;
	private String optType;
	private Long clientTypeId;
	private String discount;
	private BigDecimal price;
	private String customerCategory;
	private String eventName;
	private List<EnumOptionData> optTypes;
	//private List<MediaAssetLocationDetails> format;
	private List<DiscountMasterData> discountdata;
	private List<ClientTypeData> clientTypes;
	@SuppressWarnings("unused")
	private String clientType;
	@SuppressWarnings("unused")
	private Long discountId;
	private Collection<MCodeData> format;
	
	public EventPriceData(final Long id, final String eventName, final String formatType, final String optType, final Long clientTypeId,
			final String discount, final BigDecimal price, final Long eventId, final String clientType, final Long discountId) {
		this.id =id;
		this.eventName = eventName;
		this.optType =optType;
		this.formatType = formatType;
		this.clientTypeId = clientTypeId;
		this.discount = discount;
		this.price = price;
		this.eventId=eventId;
		this.clientType=clientType;
		this.discountId=discountId;
	}
	
	public EventPriceData(final List<EnumOptionData> optTypes, final Collection<MCodeData> format,
							final List<DiscountMasterData> discountdata, final List<ClientTypeData> clientTypes , final Long eventId,
							final String eventName) {
		this.optTypes = optTypes;
		this.format = format;
		this.discountdata = discountdata;
		this.eventId = eventId;
		this.clientTypes = clientTypes;
		this.eventName = eventName;
	}
	
	/**
	 * @return the id
	 */
	public Long getId() {
		return id;
	}
	/**
	 * @param id the id to set
	 */
	public void setId(final Long id) {
		this.id = id;
	}
	/**
	 * @return the eventId
	 */
	public Long getEventId() {
		return eventId;
	}
	/**
	 * @param eventId the eventId to set
	 */
	public void setEventId(final Long eventId) {
		this.eventId = eventId;
	}
	/**
	 * @return the formatType
	 */
	public String getFormatType() {
		return formatType;
	}
	/**
	 * @param formatType the formatType to set
	 */
	public void setFormatType(final String formatType) {
		this.formatType = formatType;
	}
	/**
	 * @return the optType
	 */
	public String getOptType() {
		return optType;
	}
	/**
	 * @param optType the optType to set
	 */
	public void setOptType(final String OptType) {
		optType = OptType;
	}
	
	/**
	 * @return the optTypes
	 */
	public List<EnumOptionData> getOptTypes() {
		return optTypes;
	}

	/**
	 * @param optTypes the optTypes to set
	 */
	public void setOptTypes(final List<EnumOptionData> optTypes) {
		this.optTypes = optTypes;
	}

	/**
	 * @return the format
	 */
	public Collection<MCodeData> getFormat() {
		return format;
	}

	/**
	 * @param format the format to set
	 */
	public void setFormat(final Collection<MCodeData> format) {
		this.format = format;
	}

	/**
	 * @return the discountdata
	 */
	public List<DiscountMasterData> getDiscountdata() {
		return discountdata;
	}

	/**
	 * @param discountdata the discountdata to set
	 */
	public void setDiscountdata(final List<DiscountMasterData> discountdata) {
		this.discountdata = discountdata;
	}

	/**
	 * @return the clientTypes
	 */
	public List<ClientTypeData> getClientTypes() {
		return clientTypes;
	}

	/**
	 * @param clientTypes the clientTypes to set
	 */
	public void setClientTypes(final List<ClientTypeData> clientTypes) {
		this.clientTypes = clientTypes;
	}

	/**
	 * @return the clientType
	 */
	public Long getClientType() {
		return clientTypeId;
	}

	/**
	 * @param clientType the clientType to set
	 */
	public void setClientType(final Long clientType) {
		this.clientTypeId = clientType;
	}

	/**
	 * @return the discount
	 */
	public String getDiscount() {
		return discount;
	}

	/**
	 * @param discount the discount to set
	 */
	public void setDiscount(final String discount) {
		this.discount = discount;
	}

	/**
	 * @return the price
	 */
	public BigDecimal getPrice() {
		return price;
	}

	/**
	 * @param price the price to set
	 */
	public void setPrice(final BigDecimal price) {
		this.price = price;
	}

	/**
	 * @return the customerCategory
	 */
	public String getCustomerCategory() {
		return customerCategory;
	}

	/**
	 * @param customerCategory the customerCategory to set
	 */
	public void setCustomerCategory(final String customerCategory) {
		this.customerCategory = customerCategory;
	}

	/**
	 * @return the eventName
	 */
	public String getEventName() {
		return eventName;
	}

	/**
	 * @param eventName the eventName to set
	 */
	public void setEventName(final String eventName) {
		this.eventName = eventName;
	}
}
