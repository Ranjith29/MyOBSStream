package org.obsplatform.logistics.item.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

public class ItemNotFoundException extends AbstractPlatformResourceNotFoundException {

public ItemNotFoundException(String itemId) {

super("error.msg.item.id.not.found","Item is Not Found",itemId);

}

}
