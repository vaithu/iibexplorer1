package com.myapp.iib.model;

import com.holonplatform.core.property.NumericProperty;
import com.holonplatform.core.property.PropertySet;
import com.holonplatform.core.property.StringProperty;

public interface RestApiModel extends AbstractModel, BrokerModel {

    NumericProperty<Integer> API_PORT = NumericProperty.integerType("Port");
    StringProperty BASE_URL = StringProperty.create("BaseURL");
    StringProperty DEF_URL = StringProperty.create("DefinitionsURL");
    StringProperty LOCAL_DEF_URL = StringProperty.create("LocalDefinitionsURL");
    StringProperty LOCAL_BASE_URL = StringProperty.create("LocalBaseURL");

    NumericProperty<Integer> OPS_COUNT = NumericProperty.integerType("Operations Count");

    PropertySet<?> PROPERTIES = PropertySet.builderOf(SNO, NAME, EG_NAME, API_PORT, BASE_URL, DEF_URL, LOCAL_BASE_URL,
            LOCAL_DEF_URL, OPS_COUNT)
            .withIdentifier(SNO)
            .build();

}
