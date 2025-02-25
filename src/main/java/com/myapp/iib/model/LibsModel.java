package com.myapp.iib.model;

import com.holonplatform.core.property.PropertySet;
import com.holonplatform.core.property.StringProperty;

public interface LibsModel extends AbstractModel, BrokerModel {

    StringProperty TYPE = StringProperty.create("Type");
    PropertySet<?> PROPERTIES = PropertySet.builderOf(SNO, NAME,TYPE,PARENT_NAME, PARENT_TYPE, EG_NAME)
            .withIdentifier(SNO)
            .build();

}
