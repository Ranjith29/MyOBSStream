package org.obsplatform.portfolio.order.domain;


public enum ConnectionTypeEnum {

	REGULAR(1, "ConnectionType.regular"), //
	PRIMARY(2, "ConnectionType.primary"),
	SECONDARY(3,"ConnectionType.secondary");

    private final Integer value;
	private final String code;

    private ConnectionTypeEnum(final Integer value, final String code) {
        this.value = value;
		this.code = code;
    }

    public Integer getValue() {
        return this.value;
    }

	public String getCode() {
		return code;
	}

	public static ConnectionTypeEnum fromInt(final Integer frequency) {

		ConnectionTypeEnum connectionTypeEnum = ConnectionTypeEnum.REGULAR;
		switch (frequency) {
		case 1:
			connectionTypeEnum = ConnectionTypeEnum.PRIMARY;
			break;
		case 2:
			connectionTypeEnum = ConnectionTypeEnum.SECONDARY;
			break;
		default:
			connectionTypeEnum = ConnectionTypeEnum.REGULAR;
			break;
		}
		return connectionTypeEnum;
	}
}
