package com.myapp.iib.model.mq;

import com.holonplatform.core.Validator;
import com.holonplatform.core.i18n.Localizable;
import com.holonplatform.core.property.*;
import com.holonplatform.vaadin.flow.components.converters.StringToNumberConverter;

public interface MQEditModel {

    StringProperty INPUTQ = StringProperty.create("InputQ")
            .message("InputQ")
            .withValidator(Validator.notBlank(Localizable.of("Input Queue Name cannot be blank")));
    StringProperty OUTPUTQ = StringProperty.create("OutputQ");
    StringProperty QMGR = StringProperty.create("QueueMgr")
            .message("QueueMgr")
            .withValidator(Validator.notBlank(Localizable.of("QueueMgr Name cannot be blank")));
    StringProperty CHANNEL = StringProperty.create("Channel")
            .withValidator(Validator.notBlank(Localizable.of("Channel Name cannot be blank")));

    StringProperty HOSTNAME = StringProperty.create("HostName")
            .withValidator(Validator.notBlank(Localizable.of("Host Name cannot be blank")));

    NumericProperty<Integer> PORT = NumericProperty.integerType("Port")
            .withValidator(Validator.notNull())
            .withValidator(Validator.min(4));

    StringProperty PRETTY_PRINT = StringProperty.create("Pretty Print");

    BooleanProperty SHOW_EDITOR = BooleanProperty.create("Show Editor");
    BooleanProperty RETAIN_INPUT = BooleanProperty.create("Retain Input");
    BooleanProperty BASIC_QINFO = BooleanProperty.create("Basic QInfo");
    BooleanProperty GET_INHIBIT = BooleanProperty.create("GET");
    BooleanProperty PUT_INHIBIT = BooleanProperty.create("PUT");

    PropertySet<?> MQPROPERTIES = PropertySet.builderOf(INPUTQ,QMGR,HOSTNAME,PORT,CHANNEL)
            .build();

    PropertySet<?> MQEDIT = PropertySet.builderOf(PRETTY_PRINT,SHOW_EDITOR,RETAIN_INPUT
    ,BASIC_QINFO,GET_INHIBIT,PUT_INHIBIT)
            .build();


}