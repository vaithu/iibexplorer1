package com.myapp.iib.test;

import com.ibm.broker.config.proxy.ConfigManagerProxyLoggedException;
import com.ibm.broker.config.proxy.ConfigManagerProxyPropertyNotInitializedException;
import com.myapp.iib.admin.FlowUtils;

import java.util.concurrent.atomic.AtomicInteger;

public class Test {

    public static void main(String[] args) {

        String brokerFile = "C:\\Users\\sxp267\\DIBNODE1.broker";

//        IUtils.brokerFiles().forEach(s -> System.out.println(s));
        AtomicInteger i = new AtomicInteger();
        FlowUtils flowUtils = new FlowUtils();
        flowUtils.brokerFile(brokerFile)
                .connect()
                .allMFs()
                .forEach(mf -> {
                    try {
                        System.out.println(i.incrementAndGet() +  "\t" + mf.getExecutionGroup().getName()+ "\t"
                                + "\t" + mf.getParent().getName()+ "\t" + mf.getName());
                    } catch (ConfigManagerProxyPropertyNotInitializedException | ConfigManagerProxyLoggedException e) {
                        e.printStackTrace();
                    }
                });
//        flowUtils.brokerFile("C:\\Users\\sxp267\\DIBNODE1.broker")
//                .connect()
//                .allEGs()




    }

}
