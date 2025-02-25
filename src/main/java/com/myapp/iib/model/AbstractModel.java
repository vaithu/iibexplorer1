package com.myapp.iib.model;

import com.holonplatform.core.property.NumericProperty;
import com.holonplatform.core.property.StringProperty;

public interface AbstractModel {
    NumericProperty<Integer> SNO = NumericProperty.integerType("S.No")
            .message("S.No");

    StringProperty NAME = StringProperty.create("name")
            .message("Name")
            ;
}
