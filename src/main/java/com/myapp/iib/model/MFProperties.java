package com.myapp.iib.model;

import com.holonplatform.core.property.PropertySet;

public interface MFProperties extends AbstractModel,BrokerModel {

    PropertySet<?> PROPERTIES = PropertySet.builderOf(SNO,NODE_NAME, NODE_TYPE)

            .withIdentifier(SNO)
            .build();

}
