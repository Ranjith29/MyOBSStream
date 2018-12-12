package org.obsplatform.portfolio.order.data;

import org.obsplatform.infrastructure.core.data.EnumOptionData;
import org.obsplatform.portfolio.order.domain.ConnectionTypeEnum;

public class ConnectionTypeEnumaration {

	public static EnumOptionData ConnectionType(final int id) {
		return ConnectionType(ConnectionTypeEnum.fromInt(id));
	}

	public static EnumOptionData ConnectionType(final ConnectionTypeEnum type) {
		EnumOptionData optionData = null;
		switch (type) {
		case PRIMARY:
			optionData = new EnumOptionData(ConnectionTypeEnum.PRIMARY.getValue().longValue(),ConnectionTypeEnum.PRIMARY.getCode(), "PRIMARY");
			break;
		case SECONDARY:
			optionData = new EnumOptionData(ConnectionTypeEnum.SECONDARY.getValue().longValue(),ConnectionTypeEnum.SECONDARY.getCode(), "SECONDARY");
			break;
		default:
			optionData = new EnumOptionData(ConnectionTypeEnum.REGULAR.getValue().longValue(),ConnectionTypeEnum.REGULAR.getCode(), "REGULAR");
			break;
		}
		return optionData;
	}

}
