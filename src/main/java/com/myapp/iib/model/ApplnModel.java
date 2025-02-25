package com.myapp.iib.model;

import com.holonplatform.core.property.BooleanProperty;
import com.holonplatform.core.property.PropertySet;
import com.holonplatform.core.property.StringProperty;

public interface ApplnModel extends AbstractModel, BrokerModel {

    StringProperty SHALED_LIB_DEPENDENCIES = StringProperty.create("SharedLibraryDependencies");
    StringProperty SHALED_LIB_DEPENDENTENTS = StringProperty.create("SharedLibraryDependents");
    StringProperty STATIC_LIB = StringProperty.create("Static Libraries");
    BooleanProperty STATUS = BooleanProperty.create("Status");

    PropertySet<?> PROPERTIES = PropertySet.builderOf(SNO, NAME, STATUS, SHALED_LIB_DEPENDENCIES, SHALED_LIB_DEPENDENTENTS, STATIC_LIB, EG_NAME)
            .withIdentifier(SNO)
            .build();
}
