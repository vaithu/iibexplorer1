package com.myapp.iib.model;

import com.holonplatform.core.property.StringProperty;

public interface BrokerModel {

    StringProperty PARENT_NAME = StringProperty.create("parentName")
            .message("Parent Name")
            ;

    StringProperty EG_NAME = StringProperty.create("parentType")
            .message("Server Name")
            ;

    StringProperty NODE_NAME = StringProperty.create("Node Name");
    StringProperty NODE_TYPE = StringProperty.create("Node Type");

    StringProperty PARENT_TYPE = StringProperty.create("parentType")
            .message("Parent Type")
            ;
}
