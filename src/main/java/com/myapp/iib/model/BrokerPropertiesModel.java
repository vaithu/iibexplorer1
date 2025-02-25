package com.myapp.iib.model;

import com.holonplatform.core.property.PropertySet;
import com.holonplatform.core.property.StringProperty;

public interface BrokerPropertiesModel extends AbstractModel, BrokerModel {

    StringProperty SNO = StringProperty.create("S.No");
    PropertySet<?> PROPERTIES = PropertySet.builderOf(SNO, NODE_NAME, NODE_TYPE)
            .withIdentifier(SNO)
            .build();
}
