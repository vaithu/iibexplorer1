package com.myapp.iib.model;

import com.holonplatform.core.property.PropertySet;
import com.holonplatform.core.property.StringProperty;

public interface DSNModel extends AbstractModel,BrokerModel{

    StringProperty DSN = StringProperty.create("DSN");


    PropertySet<?> PROPERTIES = PropertySet.builderOf(SNO, DSN, NODE_NAME, NODE_TYPE, NAME, PARENT_NAME, EG_NAME)
            .withIdentifier(SNO)
            .build();
}
