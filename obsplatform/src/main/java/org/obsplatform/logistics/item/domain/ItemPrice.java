package org.obsplatform.logistics.item.domain;

import java.math.BigDecimal;

import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.JoinColumn;
import javax.persistence.ManyToOne;
import javax.persistence.Table;
import javax.persistence.UniqueConstraint;

import org.springframework.data.jpa.domain.AbstractPersistable;

@Entity
@Table(name = "b_item_price", uniqueConstraints = { @UniqueConstraint(columnNames = { "item_id", "region_id"}, name = "itemid_with_region_uniquekey") })
public class ItemPrice extends AbstractPersistable<Long>{

	@Column(name = "region_id")
	private String regionId;

	@Column(name = "price")
	private BigDecimal price;
	
	@Column(name = "charge_variant")
	private Long chargeVariant;
	
	@Column(name = "is_deleted")
	private String isDeleted="N";
	
	@ManyToOne
    @JoinColumn(name="item_id")
	private ItemMaster itemMaster;
	
	public ItemPrice(){}

	public ItemPrice(String regionId, BigDecimal price,Long chargeVariant) {
		
		this.regionId = regionId;
		this.price = price;
		this.chargeVariant = chargeVariant;
	}

	public void update(ItemMaster itemMaster) {
		
		this.itemMaster = itemMaster;
	}

	public String getRegionId() {
		return regionId;
	}

	public void setRegionId(String regionId) {
		this.regionId = regionId;
	}

	public BigDecimal getPrice() {
		return price;
	}

	public void setPrice(BigDecimal price) {
		this.price = price;
	}
	
	public Long getChargeVariant() {
		return chargeVariant;
	}

	public void setChargeVariant(Long chargeVariant) {
		this.chargeVariant = chargeVariant;
	}

	public String getIsDeleted() {
		return isDeleted;
	}

	public void setIsDeleted(String isDeleted) {
		this.isDeleted = isDeleted;
	}

	public void delete() {

		this.isDeleted = "Y";
		this.regionId = this.regionId + "_" + this.getId() + "_Y";
	}

}