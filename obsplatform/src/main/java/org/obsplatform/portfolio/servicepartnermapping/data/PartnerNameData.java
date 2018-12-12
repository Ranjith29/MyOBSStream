package org.obsplatform.portfolio.servicepartnermapping.data;

/**
 * 
 * @author Naresh
 * 
 */
public class PartnerNameData {

	private Long id;
	private String name;

	public PartnerNameData(final Long id, final String name) {

		this.id = id;
		this.name = name;
	}

	public Long getId() {
		return id;
	}

	public void setId(Long id) {
		this.id = id;
	}

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

}
