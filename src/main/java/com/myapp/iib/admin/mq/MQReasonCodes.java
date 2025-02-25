package com.myapp.iib.admin.mq;

import com.ibm.mq.constants.CMQC;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class MQReasonCodes {

    /* Use reflection on the CMQC class to fetch a human readable description of the
       MQ reason code (e.g. getReason(2035) will return "MQRC_NOT_AUTHORIZED"). If the
       reason code rc is not found, null will be returned */
    public static String getReason(int rc) {
        String s = null;
        for (Field f : CMQC.class.getDeclaredFields())
            try {
//			System.out.println(f.getName());
                if (Modifier.isStatic(f.getModifiers()) && f.getType() == int.class
                        && f.getName().startsWith("MQRC") && f.getInt(null) == rc) {
                    s = f.getName();
                    break;
                }
            } catch (Exception e) {
                s = e.getMessage();
            }
        return s;
    }

    /*public static String getReason(MQException e) {
        String desc = getReason(e.reasonCode);
        return desc == null ? e.getMessage() : desc;
    }

    public static String getReason(PCFException e) {
        String desc = getReason(e.reasonCode);
        return desc == null ? e.getMessage() : desc;
    }*/

}