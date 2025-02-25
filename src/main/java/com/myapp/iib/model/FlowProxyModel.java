package com.myapp.iib.model;

import com.holonplatform.core.property.BooleanProperty;
import com.holonplatform.core.property.PropertySet;

public interface FlowProxyModel extends BrokerModel,AbstractModel {




    BooleanProperty STATUS = BooleanProperty.create("status")
            .message("Status")
            ;






    PropertySet<?> PROPERTIES = PropertySet.builderOf(SNO,NAME,STATUS,EG_NAME,PARENT_NAME,PARENT_TYPE)
            .withIdentifier(SNO)
            .build();

}
