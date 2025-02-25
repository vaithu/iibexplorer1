package com.myapp.iib.model.mq;

import com.holonplatform.core.property.NumericProperty;
import com.holonplatform.core.property.PropertySet;
import com.holonplatform.core.property.StringProperty;

public interface QueuesDetails {

    public static StringProperty Q_TYPE = StringProperty.create("Q_TYPE").message("QType");
    public static StringProperty BASE_Q = StringProperty.create("BASE_Q").message("BaseQ");
    public static StringProperty Q_NAME = StringProperty.create("Q_NAME").message("QName");
    public static StringProperty CLUSTER_NAME = StringProperty.create("CLUSTER_NAME").message("ClusterName");
    public static StringProperty CREATION_DATE = StringProperty.create("CREATION_DATE").message("CreationDt");
    public static StringProperty CURRENT_Q_DEPTH = StringProperty.create("CURRENT_Q_DEPTH").message("CurQDepth");
    public static StringProperty OPEN_INPUT_COUNT = StringProperty.create("OPEN_INPUT_COUNT").message("OpenInpCount");
    public static StringProperty OPEN_OUTPUT_COUNT = StringProperty.create("OPEN_OUTPUT_COUNT").message("OpenOutpuCount");
    public static StringProperty MAX_Q_DEPTH = StringProperty.create("MAX_Q_DEPTH").message("MaxQDepth");
//    public static NumericProperty.NumericPropertyBuilder<Integer> CURRENT_Q_DEPTH = NumericProperty.integerType("CURRENT_Q_DEPTH");
//    public static NumericProperty.NumericPropertyBuilder<Integer> OPEN_INPUT_COUNT = NumericProperty.integerType("OPEN_INPUT_COUNT");
//    public static NumericProperty.NumericPropertyBuilder<Integer> OPEN_OUTPUT_COUNT = NumericProperty.integerType("OPEN_OUTPUT_COUNT");
//    public static NumericProperty.NumericPropertyBuilder<Integer> MAX_Q_DEPTH = NumericProperty.integerType("MAX_Q_DEPTH");
    public static NumericProperty.NumericPropertyBuilder<Integer> SNO = NumericProperty.integerType("SNO");

    public static StringProperty MAX_MSG_LENGTH = StringProperty.create("MAX_MSG_LENGTH").message("MaxLength");
    public static StringProperty INHIBIT_PUT = StringProperty.create("INHIBIT_PUT").message("Put");
    public static StringProperty INHIBIT_GET = StringProperty.create("INHIBIT_GET").message("Get");

    public static PropertySet<?> PROPERTY_SET = PropertySet.of(SNO,Q_NAME,Q_TYPE, BASE_Q,CLUSTER_NAME,CREATION_DATE,CURRENT_Q_DEPTH,
            OPEN_INPUT_COUNT,OPEN_OUTPUT_COUNT,MAX_Q_DEPTH,MAX_MSG_LENGTH,INHIBIT_PUT,INHIBIT_GET

            );


}