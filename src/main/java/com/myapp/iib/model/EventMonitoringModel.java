package com.myapp.iib.model;

import com.holonplatform.core.property.PropertySet;
import com.holonplatform.core.property.StringProperty;

public interface EventMonitoringModel extends AbstractModel{

    StringProperty EventSourceAddress = StringProperty.create("EventSourceAddress");
    StringProperty NodeType = StringProperty.create("NodeType");
    StringProperty EventName = StringProperty.create("EventName");
    StringProperty LocalId = StringProperty.create("LocalId");
    StringProperty ParentId = StringProperty.create("ParentId");
    StringProperty GlobalId = StringProperty.create("GlobalId");
    StringProperty Filter = StringProperty.create("Filter");
    StringProperty Value1 = StringProperty.create("Value1");
    StringProperty Value2 = StringProperty.create("Value2");
    StringProperty Value3 = StringProperty.create("Value3");
    StringProperty Value4 = StringProperty.create("Value4");
    StringProperty Value5 = StringProperty.create("Value5");
    StringProperty PayloadQuery = StringProperty.create("PayloadQuery");
    StringProperty LogPayload = StringProperty.create("LogPayload");

    PropertySet<?> PROPERTIES = PropertySet.builderOf(SNO,EventSourceAddress,NodeType, EventName, LocalId,
            ParentId, GlobalId, Filter, Value1, Value2,
            Value3, Value4, Value5, PayloadQuery,
            LogPayload)
            .withIdentifier(SNO)
            .build()
            ;


}
