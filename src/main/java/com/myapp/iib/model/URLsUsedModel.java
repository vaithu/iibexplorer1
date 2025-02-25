package com.myapp.iib.model;

import com.holonplatform.core.property.PropertySet;
import com.holonplatform.core.property.StringProperty;

public interface URLsUsedModel extends BrokerModel,AbstractModel{

    StringProperty URL = StringProperty.create("URL");
    StringProperty PROXY = StringProperty.create("Proxy");

    PropertySet<?> PROPERTIES = PropertySet.builderOf(SNO,NAME, URL, PROXY, NODE_NAME,NODE_TYPE,
            PARENT_NAME,EG_NAME)
            .withIdentifier(SNO)
            .build();
}
