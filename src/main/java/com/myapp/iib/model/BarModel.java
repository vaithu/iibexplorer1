package com.myapp.iib.model;

import com.holonplatform.core.property.PropertySet;
import com.holonplatform.core.property.StringProperty;

public interface BarModel extends AbstractModel {

    StringProperty KEY = StringProperty.create("Key");
    StringProperty VALUE = StringProperty.create("Value");

    PropertySet<?> PROPERTIES = PropertySet.builderOf(SNO,KEY,VALUE)
            .withIdentifier(SNO)
            .build();

}
