package com.myapp.iib.model;

import com.holonplatform.core.property.PropertySet;
import com.holonplatform.core.property.StringProperty;

public interface SecurityIdentitiesModel extends AbstractModel, BrokerModel {

    StringProperty TYPE = StringProperty.create("Type");
    StringProperty RESOURCE = StringProperty.create("Parent Type");
    PropertySet<?> PROPERTIES = PropertySet.builderOf(SNO, NAME,TYPE, RESOURCE)
            .withIdentifier(SNO)
            .build();

}
