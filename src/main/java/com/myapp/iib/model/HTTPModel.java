package com.myapp.iib.model;

import com.holonplatform.core.property.PropertySet;
import com.holonplatform.core.property.StringProperty;

public interface HTTPModel {

    StringProperty EG_HTTP = StringProperty.create("EGLevel : HTTPConnector/port");
    StringProperty EG_HTTPS = StringProperty.create("EGLevel : HTTPSConnector/port");
    StringProperty NODE_HTTP = StringProperty.create("NodeLevel : HTTPConnector/port");
    StringProperty NODE_HTTPS = StringProperty.create("NodeLevel : HTTPSConnector/port");

    PropertySet<?> PROPERTIES = PropertySet.of(EG_HTTP,EG_HTTPS,NODE_HTTP,NODE_HTTPS);

}
