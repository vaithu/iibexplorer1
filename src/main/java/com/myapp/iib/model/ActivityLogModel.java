package com.myapp.iib.model;

import com.holonplatform.core.property.PropertySet;
import com.holonplatform.core.property.StringProperty;
import com.holonplatform.core.property.TemporalProperty;

import java.util.Date;

public interface ActivityLogModel extends AbstractModel{

//    BooleanProperty.BooleanPropertyBuilder IS_ERROR = BooleanProperty.create("Error");

    TemporalProperty<Date> TIMESTAMP = TemporalProperty.date("Timestamp");
    StringProperty DETAIL = StringProperty.create("Detail");
    StringProperty TAGS = StringProperty.create("Tags");
    StringProperty STATUS = StringProperty.create("Status");
    StringProperty SOURCE = StringProperty.create("Source");
    StringProperty INSERTS = StringProperty.create("Inserts");

    PropertySet<?> PROPERTIES = PropertySet.builderOf(SNO,TIMESTAMP, STATUS,SOURCE, DETAIL, TAGS, INSERTS)
            .withIdentifier(SNO)
            .build();
}
