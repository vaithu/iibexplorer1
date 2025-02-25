package com.myapp.iib.model.mq;

import com.holonplatform.core.property.*;

import java.time.LocalDateTime;
import java.util.concurrent.atomic.AtomicInteger;

public interface DISPLAYQ {

    public static final NumericProperty<Integer> SNO = NumericProperty.integerType("SNo").message("S.No");
    public static final StringProperty MSGSIZE = StringProperty.create("MessageSize").message("Size");
    public static final StringProperty MSGFMT = StringProperty.create("MessageFmt").message("MsgFmt");
    public static final StringProperty USERID = StringProperty.create("User").message("User");
    public static final StringProperty PUT_APPLN = StringProperty.create("PutAppl").message("PutApln");
    public static final StringProperty MSG_ID = StringProperty.create("MsgId").message("MsgId");
    public static final StringProperty CORRL_ID = StringProperty.create("CorrlId").message("CorId");
    public static final TemporalProperty.TemporalPropertyBuilder<LocalDateTime> PUTTIME = TemporalProperty.localDateTime("PutTime").message("Put Time");
//    public static final VirtualProperty<String> SHOW_MSG = VirtualProperty.create(String.class, propertyBox -> propertyBox.getValue(MSG_ID));
//    public static final VirtualProperty<String> PURGE_MSG = VirtualProperty.create(String.class, propertyBox -> propertyBox.getValue(MSG_ID));




    public static final PropertySet<?> PROPERTIES = PropertySet.builderOf(SNO, MSGSIZE, MSGFMT, USERID, PUTTIME, PUT_APPLN, MSG_ID, CORRL_ID)
            .build();
//            , SHOW_MSG, PURGE_MSG);

    /*static AtomicInteger at = new AtomicInteger(0);

    public static int getNextCountValue() {
        return at.incrementAndGet();
    }*/


}