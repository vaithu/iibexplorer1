package com.myapp.iib.model;

import com.holonplatform.core.property.PropertySet;
import com.holonplatform.core.property.StringProperty;

public interface ConfigSvcModel extends AbstractModel {

    StringProperty CS_NAME = StringProperty.create("Name");
    StringProperty SNO = StringProperty.create("S.No");
    StringProperty CS_TYPE = StringProperty.create("Type");
    StringProperty PROP_NAME = StringProperty.create("Property Name");
    StringProperty PROP_TYPE = StringProperty.create("Property Type");

    PropertySet<?> PROPERTIES = PropertySet.builderOf(SNO, CS_NAME, CS_TYPE, PROP_NAME, PROP_TYPE)
            .withIdentifier(SNO)
            .build();
}
