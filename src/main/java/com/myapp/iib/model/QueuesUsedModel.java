package com.myapp.iib.model;

import com.holonplatform.core.property.PropertySet;
import com.holonplatform.core.property.StringProperty;

public interface QueuesUsedModel extends BrokerModel,AbstractModel{
    StringProperty QUEUE_NAME = StringProperty.create("Queue Name");

    PropertySet<?> PROPERTIES = PropertySet.builderOf(SNO,NAME,QUEUE_NAME,NODE_NAME,NODE_TYPE,
            PARENT_NAME,EG_NAME)
            .withIdentifier(SNO)
            .build();
}
