package com.myapp.iib.model;

import com.holonplatform.core.property.NumericProperty;
import com.holonplatform.core.property.PropertySet;

public interface WLMModel extends AbstractModel{

    NumericProperty<Integer> ADDL_INS = NumericProperty.integerType("AdditionalInstances");
    NumericProperty<Integer> MAX_RATE = NumericProperty.integerType("MaximumRateMsgsPerSe");
    NumericProperty<Integer> NOTIFY_THRESHOLD = NumericProperty.integerType("NotificationThresholdMsgsPerSec");

    PropertySet<?> PROPERTIES = PropertySet.builderOf(NAME, ADDL_INS, MAX_RATE, NOTIFY_THRESHOLD)
            .withIdentifier(NAME)
            .build();

}
