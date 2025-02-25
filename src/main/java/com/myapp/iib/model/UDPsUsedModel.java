package com.myapp.iib.model;

import com.holonplatform.core.property.PropertySet;
import com.holonplatform.core.property.StringProperty;

public interface UDPsUsedModel extends BrokerModel,AbstractModel{

    StringProperty SNO = StringProperty.create("S.No");
    StringProperty UDP_NAME = StringProperty.create("UDP Name");
    StringProperty UDP_PROP = StringProperty.create("UDP Prop");
    StringProperty FLOW_TYPE = StringProperty.create("Flow Type");

    PropertySet<?> PROPERTIES = PropertySet.builderOf(SNO,NAME,UDP_NAME,UDP_PROP,FLOW_TYPE,
            PARENT_NAME,EG_NAME)
            .withIdentifier(SNO)
            .build();
}
