
package org.obsplatform.billing.chargevariant.exception;

import org.obsplatform.infrastructure.core.exception.AbstractPlatformResourceNotFoundException;

/**
 * A {@link RuntimeException} thrown when a code is not found.
 */
@SuppressWarnings("serial")
public class ChargeVariantNotFountException extends AbstractPlatformResourceNotFoundException {

    /**
     * @param discountId
     */
    public ChargeVariantNotFountException(final Long variantId) {
        super("error.msg.chargevariant.not.found", "ChargeVariant with this id"+variantId+"not exist", variantId);
        
    }
    
    public ChargeVariantNotFountException() {
        super("error.msg.chargevariant.not.found", "No chargevariant found");
    }
   
}