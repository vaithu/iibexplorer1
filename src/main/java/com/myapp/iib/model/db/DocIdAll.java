package com.myapp.iib.model.db;

import com.holonplatform.core.property.StringProperty;

public interface DocIdAll {

    StringProperty MQ_DOC_ID = StringProperty.create("doc_id");
    StringProperty MQ_INTERFACE = StringProperty.create("name");
    StringProperty IGLASS_INTERFACE = StringProperty.create("interface");
    StringProperty CORRL_ID = StringProperty.create("correlid");
    StringProperty ROOT_ELEMENT = StringProperty.create("rootelement");

}
