package com.myapp.iib.model;

import com.holonplatform.core.property.PropertySet;
import com.holonplatform.core.property.StringProperty;

public interface EGComponenetsModel extends AbstractModel, BrokerModel {


    StringProperty SNO = StringProperty.create("S.No");

    StringProperty PID = StringProperty.create("PID");
    StringProperty BAR_FILE = StringProperty.create("Bar File");
    StringProperty DEPLOYED_TIME = StringProperty.create("Deployed Time");

    PropertySet<?> PROPERTIES = PropertySet.builderOf(SNO, EG_NAME, PID, NAME, PARENT_NAME,PARENT_TYPE, BAR_FILE, DEPLOYED_TIME)
            .withIdentifier(SNO)
            .build()

            ;
}
