package com.myapp.iib.admin.wrapper;

import com.holonplatform.core.property.PropertyBox;
import com.ibm.broker.config.proxy.*;
import com.myapp.iib.admin.FlowUtils;
import com.myapp.iib.model.*;
import com.myapp.iib.my.BrokerUtils;
import com.myapp.iib.my.CommonUtils;
import com.myapp.iib.my.MyComponents;
import com.myapp.iib.my.MyUtils;
import com.myapp.iib.ui.util.UIUtils;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.time.DateUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class FlowUtilsWrapper {

    private static final Logger logger = LoggerFactory.getLogger(FlowUtilsWrapper.class);

    public static final String USER_DIR = CommonUtils.userDir();
    public static final String PROP_FILE_NAME = "QuickDeploy.properties";
    public static Properties properties;

    static {
        try {
            properties = CommonUtils.loadPropFile(Paths.get(USER_DIR, PROP_FILE_NAME));
        } catch (IOException e) {
            e.printStackTrace();
            logger.error(e.getLocalizedMessage(),e);
        }
    }

    public static Stream<PropertyBox> basicMFListing(List<MessageFlowProxy> all) {

        AtomicInteger i = new AtomicInteger();
        return all.stream()
                .map(mf -> {
                    PropertyBox pb = PropertyBox.builder(FlowProxyModel.PROPERTIES)
                            .build();

                    pb.setValue(FlowProxyModel.SNO, i.incrementAndGet());
//                    pb.setValue(FlowProxyModel.UUID, mf.getUUID());
                    try {
                        pb.setValue(FlowProxyModel.NAME, mf.getName());
                        pb.setValue(FlowProxyModel.STATUS, mf.isRunning());
                        pb.setValue(FlowProxyModel.PARENT_NAME, mf.getParent().getName());
                        pb.setValue(FlowProxyModel.PARENT_TYPE, FlowUtils.getParentDisplayName(mf));
                        pb.setValue(FlowProxyModel.EG_NAME, mf.getExecutionGroup().getName());

                    } catch (ConfigManagerProxyPropertyNotInitializedException | ConfigManagerProxyLoggedException e) {
                        e.printStackTrace();
                    }

                    return pb;
                });

    }

    public static Stream<PropertyBox> basicSFListing(List<SubFlowProxy> all) {

        AtomicInteger i = new AtomicInteger();
        return all.stream()
                .map(sf -> {
                    PropertyBox pb = PropertyBox.builder(FlowProxyModel.PROPERTIES)
                            .build();

                    pb.setValue(FlowProxyModel.SNO, i.incrementAndGet());
//                    pb.setValue(FlowProxyModel.UUID, sf.getUUID());

                    try {
                        pb.setValue(FlowProxyModel.NAME, sf.getName());
                        pb.setValue(FlowProxyModel.PARENT_NAME, sf.getParent().getName());
                        pb.setValue(FlowProxyModel.PARENT_TYPE, sf.getConfigurationObjectTypeOfParent().getDisplayName());
                        pb.setValue(FlowProxyModel.EG_NAME, sf.getExecutionGroup().getName());

                    } catch (ConfigManagerProxyPropertyNotInitializedException | ConfigManagerProxyLoggedException e) {
                        e.printStackTrace();
                    }
                    return pb;
                });
    }

    public static List<PropertyBox> queuesUsedListing(List<MessageFlowProxy> all) {

        AtomicInteger i = new AtomicInteger();
        List<PropertyBox> pbs = new ArrayList<>();
        all
                .forEach(mf -> {
                    try {
                        Enumeration<MessageFlowProxy.Node> nodesUsed = mf
                                .getNodes();

                        while (nodesUsed.hasMoreElements()) {
                            MessageFlowProxy.Node node = nodesUsed
                                    .nextElement();
                            if (FlowUtils.isMQNode(node.getType())) {
                                Properties nodeProperties = node.getProperties();
                                if (nodeProperties != null
                                        && nodeProperties.getProperty("queueName") != null
                                        && Arrays.asList(mf.getQueues())
                                        .contains(nodeProperties.getProperty("queueName"))) {

                                    try {
                                        PropertyBox pb = PropertyBox.builder(QueuesUsedModel.PROPERTIES)
                                                .build();
                                        pb.setValue(QueuesUsedModel.SNO, i.incrementAndGet());
//                                        pb.setValue(QueuesUsedModel.UUID, mf.getUUID());
                                        pb.setValue(QueuesUsedModel.NAME, mf.getName());
                                        pb.setValue(QueuesUsedModel.PARENT_NAME, mf.getParent().getName());
                                        pb.setValue(QueuesUsedModel.EG_NAME, mf.getExecutionGroup().getName());
                                        pb.setValue(QueuesUsedModel.QUEUE_NAME, node.getProperties().getProperty("queueName"));
                                        pb.setValue(QueuesUsedModel.NODE_NAME, node.getName());
                                        pb.setValue(QueuesUsedModel.NODE_TYPE,
                                                StringUtils.substringAfter(node.getType(), "ComIbm"));

                                        pbs.add(pb);
                                    } catch (ConfigManagerProxyPropertyNotInitializedException | ConfigManagerProxyLoggedException e) {
                                        e.printStackTrace();
                                    }


                                }
                            }
                        }
                    } catch (ConfigManagerProxyPropertyNotInitializedException
                            | IllegalArgumentException e) {

                        e.printStackTrace();
                    }
                });
        return pbs;
    }

    public static List<PropertyBox> udpsUsedListing(List<MessageFlowProxy> all) {

        /*AtomicInteger i = new AtomicInteger();
        DoubleAdder adder = new DoubleAdder();
        adder.add(1);
        double j = 0.1;*/
        List<PropertyBox> pbs = new ArrayList<>();
        int i = 0, j, k;
        for (MessageFlowProxy proxy : all) {

            k = 1;
            j = 1;
            try {

                for (String pName : proxy.getUserDefinedPropertyNames()) {
                    PropertyBox pb = PropertyBox.builder(UDPsUsedModel.PROPERTIES)
                            .build();

//                    pb.setValue(UDPsUsedModel.UUID, proxy.getUUID());

                    pb.setValue(UDPsUsedModel.UDP_NAME, pName);
                    pb.setValue(UDPsUsedModel.UDP_PROP, proxy.getUserDefinedProperty(pName).toString());

                    if (k == 1) {
                        k += 1;
                        pb.setValue(UDPsUsedModel.SNO, String.valueOf(++i));
                        pb.setValue(UDPsUsedModel.NAME, proxy.getName());
                        pb.setValue(UDPsUsedModel.FLOW_TYPE, proxy.getConfigurationObjectType().getDisplayName());

                        pb.setValue(UDPsUsedModel.PARENT_NAME, proxy.getParent().getName());
                        pb.setValue(UDPsUsedModel.EG_NAME, proxy.getExecutionGroup().getName());
                    } else {
                        pb.setValue(UDPsUsedModel.SNO, i + "." + j++);
                        pb.setValue(UDPsUsedModel.NAME, "");
                        pb.setValue(UDPsUsedModel.FLOW_TYPE, "");

                        pb.setValue(UDPsUsedModel.PARENT_NAME, "");
                        pb.setValue(UDPsUsedModel.EG_NAME, "");
                    }
                    pbs.add(pb);
                }

            } catch (ConfigManagerProxyPropertyNotInitializedException
                    | ConfigManagerProxyLoggedException
                    | IllegalArgumentException e) {

                e.printStackTrace();
            }
        }
        return pbs;
    }

    public static Stream<PropertyBox> staticLibsUsedListing(List<StaticLibraryProxy> allLibs) {
        AtomicInteger i = new AtomicInteger();
        return allLibs.stream()
                .map(proxy -> {
                    try {
                        return PropertyBox.builder(LibsModel.PROPERTIES)
                                .set(LibsModel.SNO, i.incrementAndGet())
                                .set(LibsModel.NAME, proxy.getName())
                                .set(LibsModel.TYPE, proxy.getConfigurationObjectType().toString())
                                .set(LibsModel.PARENT_NAME, proxy.getParent().getName())
                                .set(LibsModel.PARENT_TYPE, proxy.getParent().getConfigurationObjectType().toString())
                                .set(LibsModel.EG_NAME, proxy.getExecutionGroup().getName())

                                .build();
                    } catch (ConfigManagerProxyPropertyNotInitializedException | ConfigManagerProxyLoggedException e) {
                        e.printStackTrace();
                    }
                    return null;
                });
    }

    public static Stream<PropertyBox> sharedLibsUsedListing(List<SharedLibraryProxy> allLibs) {
        AtomicInteger i = new AtomicInteger();
        return allLibs.stream()
                .map(proxy -> {
                    try {
                        return PropertyBox.builder(LibsModel.PROPERTIES)
                                .set(LibsModel.SNO, i.incrementAndGet())
                                .set(LibsModel.NAME, proxy.getName())
                                .set(LibsModel.TYPE, proxy.getConfigurationObjectType().toString())
                                .set(LibsModel.PARENT_NAME, proxy.getParent().getName())
                                .set(LibsModel.PARENT_TYPE, proxy.getParent().getConfigurationObjectType().toString())
                                .set(LibsModel.EG_NAME, proxy.getExecutionGroup().getName())

                                .build();
                    } catch (ConfigManagerProxyPropertyNotInitializedException | ConfigManagerProxyLoggedException e) {
                        e.printStackTrace();
                    }
                    return null;
                });
    }

    @SneakyThrows
    public static List<PropertyBox> securityIdentities(BrokerProxy brokerProxy) {
        AtomicInteger i = new AtomicInteger();
        List<PropertyBox> list = new ArrayList<>();

        for (SecurityIdentity sid : brokerProxy.getSecurityIdentities()) {
            list.add(PropertyBox.builder(SecurityIdentitiesModel.PROPERTIES)
                    .set(SecurityIdentitiesModel.SNO, i.incrementAndGet())
                    .set(SecurityIdentitiesModel.NAME, sid.getName())
                    .set(SecurityIdentitiesModel.TYPE, sid.getType().name())
                    .set(SecurityIdentitiesModel.RESOURCE, sid.getResource())


                    .build());
        }

        return list;


    }

    public static List<PropertyBox> sfUrlsUsedListing(List<SubFlowProxy> allSFs) {

        allSFs
                .forEach(sf -> {

                    try {
                        Collections.list(sf.getNodes())
                                .stream()
                                .filter(node -> FlowUtils.isWSNode(node.getType()))
                                .forEach(node -> {
                                    
                                });
                    } catch (ConfigManagerProxyPropertyNotInitializedException e) {
                        e.printStackTrace();
                    }


                });

        return  null;

    }


    public static Collection<? extends PropertyBox> urlsUsedListing(String brokerFile, List<MessageFlowProxy> allMFs) {

        AtomicInteger i = new AtomicInteger();
        List<PropertyBox> pbs = new ArrayList<>();
        String host = BrokerUtils.getHostFromBrokerFile(brokerFile);

        allMFs.forEach(mf -> {
            try {
                Collections.list(mf.getNodes())
                        .stream().filter(node -> FlowUtils.isWSNode(node.getType()))
                        .forEach(node -> {

                            try {
                                Properties prop = node.getProperties();

                                PropertyBox pb = PropertyBox.builder(URLsUsedModel.PROPERTIES).build();

                                pb.setValue(URLsUsedModel.SNO, i.incrementAndGet());
                                pb.setValue(URLsUsedModel.NAME, mf.getName());

                                if (prop.getProperty("webServiceURL") != null) {
                                    pb.setValue(URLsUsedModel.URL, prop.getProperty("webServiceURL"));
                                } else {
                                    String url = prop.getProperty("URLSpecifier") != null ? prop.getProperty("URLSpecifier") : prop.getProperty("urlSelector");
                                    if (url != null && !url.isEmpty()) {
                                        if (prop.getProperty("useHTTPS") != null && prop.getProperty("useHTTPS").equalsIgnoreCase("YES")) {
                                            url = "Input URL : https://" + host + ":" + mf.getExecutionGroup()
                                                    .getRuntimeProperty(
                                                            "HTTPSConnector/port") + url;
                                            pb.setValue(URLsUsedModel.URL, url);
                                        } else if (url.startsWith("http")) {
                                            pb.setValue(URLsUsedModel.URL, url);
                                        } else {
                                            pb.setValue(URLsUsedModel.URL, "http://"
                                                    + host
                                                    + ":"
                                                    + mf.getExecutionGroup()
                                                    .getRuntimeProperty(
                                                            "HTTPConnector/port")
                                                    + url);
                                        }
                                    }
                                }

                                pb.setValue(URLsUsedModel.PROXY, prop.getProperty("httpProxyLocation") == null
                                        ? "" : prop.getProperty("httpProxyLocation"));
                                pb.setValue(URLsUsedModel.NODE_NAME, node.getName());
                                pb.setValue(URLsUsedModel.NODE_TYPE, StringUtils.substringAfter(node.getType(), "ComIbm"));
                                pb.setValue(URLsUsedModel.PARENT_NAME, mf.getParent().getName());
                                pb.setValue(URLsUsedModel.EG_NAME, mf.getExecutionGroup().getName());

                                pbs.add(pb);
                            } catch (ConfigManagerProxyPropertyNotInitializedException | ConfigManagerProxyLoggedException e) {
                                e.printStackTrace();
                            }
                        });
            } catch (ConfigManagerProxyPropertyNotInitializedException e) {
                e.printStackTrace();
            }
        });
        return pbs;
    }

    public static void updateWLM(MessageFlowProxy mf, Optional<PropertyBox> propertyBox) {

        propertyBox.ifPresent(pb -> {
            try {
                if (!pb.getValue(WLMModel.ADDL_INS).equals(mf.getAdditionalInstances())) {
                    mf.setAdditionalInstances(pb.getValue(WLMModel.ADDL_INS));
                }

                if (!pb.getValue(WLMModel.NOTIFY_THRESHOLD).equals(mf.getNotificationThresholdMsgsPerSec())) {
                    mf.setNotificationThresholdMsgsPerSec(pb.getValue(WLMModel.NOTIFY_THRESHOLD));
                }

                if (!pb.getValue(WLMModel.NAME).equals(mf.getName())) {
                    mf.setName(pb.getValue(WLMModel.NAME));
                }

                if (!pb.getValue(WLMModel.MAX_RATE).equals(mf.getMaximumRateMsgsPerSec())) {
                    mf.setMaximumRateMsgsPerSec(pb.getValue(WLMModel.MAX_RATE));
                }
            } catch (ConfigManagerProxyPropertyNotInitializedException | ConfigManagerProxyLoggedException e) {
                e.printStackTrace();
            }

        });
    }

    @SneakyThrows
    public static Collection<? extends PropertyBox> showEGComponenets(List<MessageFlowProxy> allMFs) {
        int i = 0, j = 1;

        HashSet<String> unique = new HashSet<>();
        String egName;
        ExecutionGroupProxy egProxy;
        List<PropertyBox> pbs = new ArrayList<>();
        for (MessageFlowProxy mf : allMFs) {
            PropertyBox pb = PropertyBox.create(EGComponenetsModel.PROPERTIES);
            egProxy = mf.getExecutionGroup();
            egName = egProxy.getName();
            if (unique.add(egName)) {
                pb.setValue(EGComponenetsModel.SNO, String.valueOf(++i));
                pb.setValue(EGComponenetsModel.EG_NAME, egName);
                pb.setValue(EGComponenetsModel.PID, egProxy.getAdvancedProperties().getProperty("processId"));
                j = 1;
            } else {
                pb.setValue(EGComponenetsModel.SNO, i + "." + j++);
                pb.setValue(EGComponenetsModel.EG_NAME, "");
                pb.setValue(EGComponenetsModel.PID, "");
            }

            pb.setValue(EGComponenetsModel.NAME, mf.getName());
            pb.setValue(EGComponenetsModel.PARENT_NAME, mf.getParent().getName());
            pb.setValue(EGComponenetsModel.PARENT_TYPE, mf.getParent().getConfigurationObjectType().getDisplayName());
            pb.setValue(EGComponenetsModel.BAR_FILE, mf.getBARFileName());
            pb.setValue(EGComponenetsModel.DEPLOYED_TIME, MyUtils.dateToLocalDateTime(mf.getDeployTime()));
            pbs.add(pb);
        }
        return pbs;
    }

    @SneakyThrows
    public static List<PropertyBox> fetchUDPs(MessageFlowProxy mf) {
        AtomicInteger i = new AtomicInteger();
        List<PropertyBox> pb = new ArrayList<>();

        for (String udp : mf.getUserDefinedPropertyNames()) {

            pb.add(PropertyBox.builder(UDPsUsedModel.PROPERTIES)
                    .set(UDPsUsedModel.SNO, String.valueOf(i.incrementAndGet()))
                    .set(UDPsUsedModel.UDP_NAME, udp)
                    .set(UDPsUsedModel.UDP_PROP, String.valueOf(mf.getUserDefinedProperty(udp)))


                    .build());
        }
        return pb;

    }

    public static Collection<? extends PropertyBox> dsnsUsedListing(List<MessageFlowProxy> allMFs) {

        List<PropertyBox> pbs = new ArrayList<>();
        int i = 0;
        for (MessageFlowProxy proxy : allMFs) {

            try {
                Enumeration<MessageFlowProxy.Node> nodesUsed = proxy
                        .getNodes();

                while (nodesUsed.hasMoreElements()) {
                    MessageFlowProxy.Node node = nodesUsed
                            .nextElement();
                    if (FlowUtils.isDSN(node.getType())) {
                        Properties prop = node.getProperties();

                        if (prop != null
                                && prop.getProperty("dataSource") != null
                                && !prop.getProperty("dataSource").equals(
                                "")) {

                            pbs.add(PropertyBox.builder(DSNModel.PROPERTIES)
                                    .set(DSNModel.SNO, ++i)
                                    .set(DSNModel.DSN, prop.getProperty("dataSource"))
                                    .set(DSNModel.NODE_NAME, node.getName())
                                    .set(DSNModel.NODE_TYPE, FlowUtils.getNodeType(node))
                                    .set(DSNModel.NAME, proxy.getName())
                                    .set(DSNModel.PARENT_NAME, proxy.getParent().getName())
                                    .set(DSNModel.EG_NAME, proxy.getExecutionGroup().getName())


                                    .build());

                        }
                    }
                }
            } catch (ConfigManagerProxyPropertyNotInitializedException
                    | IllegalArgumentException
                    | ConfigManagerProxyLoggedException e) {

                e.printStackTrace();

            }
        }

        return pbs;
    }

    public static Collection<? extends PropertyBox> filesNodesUsedListing(List<MessageFlowProxy> allMFs) {
        int i = 0;
        Properties prop;
        String type;
        List<PropertyBox> pbs = new ArrayList<>();
        for (MessageFlowProxy proxy : allMFs) {

            try {

                Enumeration<MessageFlowProxy.Node> nodesUsed = proxy
                        .getNodes();

                while (nodesUsed.hasMoreElements()) {
                    MessageFlowProxy.Node node = nodesUsed
                            .nextElement();
                    type = node.getType();
                    if (FlowUtils.isFileNode(type)) {
                        prop = node.getProperties();

                        if (null != prop) {

                            PropertyBox pb = PropertyBox.create(FilesNodeModel.PROPERTIES);
                            pb.setValue(FilesNodeModel.SNO, ++i);
                            pb.setValue(FilesNodeModel.NAME, proxy.getName());
                            pb.setValue(FilesNodeModel.NODE_NAME, node.getName());
                            pb.setValue(FilesNodeModel.NODE_TYPE, FlowUtils.getNodeType(node));

                            if (null != prop.getProperty("inputDirectory")) {
                                pb.setValue(FilesNodeModel.LOCAL_DIR, prop.getProperty("inputDirectory"));
                            }

                            if (null != prop.getProperty("outputDirectory")) {
                                pb.setValue(FilesNodeModel.LOCAL_DIR, prop.getProperty("outputDirectory"));
                            }

                            if (null != prop.getProperty("readDirectory")) {
                                pb.setValue(FilesNodeModel.LOCAL_DIR, prop.getProperty("readDirectory"));
                            }

                            pb.setValue(FilesNodeModel.PATTERN, prop.getProperty("filenamePattern"));

                            pb.setValue(FilesNodeModel.FTP, prop.getProperty("fileFtp"));
                            pb.setValue(FilesNodeModel.FTP_TYPE, prop.getProperty("remoteTransferType"));
                            pb.setValue(FilesNodeModel.FTP_USER, prop.getProperty("fileFtpUser"));
                            pb.setValue(FilesNodeModel.FTP_SERVER, prop.getProperty("fileFtpServer"));

                            pb.setValue(FilesNodeModel.FTP_DIR, prop.getProperty("fileFtpDirectory"));
                            pb.setValue(FilesNodeModel.FTP_MODE, prop.getProperty("fileFtpTransferMode"));
                            pb.setValue(FilesNodeModel.FTP_SCAN_DELAY, prop.getProperty("fileFtpScanDelay"));
                            pb.setValue(FilesNodeModel.PARENT_NAME, proxy.getParent().getName());
                            pb.setValue(FilesNodeModel.PARENT_TYPE, proxy.getParent().getConfigurationObjectType().getDisplayName());
                            pb.setValue(FilesNodeModel.EG_NAME, proxy.getExecutionGroup().getName());

                            pbs.add(pb);

                        }
                    }
                }
            } catch (ConfigManagerProxyPropertyNotInitializedException
                    | IllegalArgumentException
                    | ConfigManagerProxyLoggedException e) {

                e.printStackTrace();
            }
        }

        return pbs;
    }

    @SneakyThrows
    public static List<PropertyBox> activityLog(MessageFlowProxy mf, Integer hours, String logLevel) {

        String mfName = mf.getName();
        Date date = DateUtils.addHours(new Date(), hours != null ? hours * -1 : -24);
        if (mf.isRunning()) {
            AtomicInteger i = new AtomicInteger();
            return Collections.list(mf.getActivityLog().elements())
                    .stream().filter(logEntry -> logEntry.getTimestamp().compareTo(date) >= 0)
                    .filter(logEntry -> BrokerUtils.addLogFilter(logEntry.getMessage(), logLevel))
                    .map(logEntry -> {
                        StringBuilder sb = new StringBuilder();
                        StringBuilder inserts = new StringBuilder();
                        Enumeration<String> tags = logEntry
                                .getTagNames();

                        while (tags.hasMoreElements()) {

                            String tag = tags.nextElement();
                            if (!logEntry.getTagValue(tag)
                                    .equalsIgnoreCase(mfName)) {
                                sb.append(tag).append("=").append(logEntry
                                        .getTagValue(tag))
                                        .append("|");
                            }
                        }

                        for (int j = 0; j < logEntry.getInsertsSize(); j++) {

                            if (!logEntry.getInsert(j)
                                    .equalsIgnoreCase(mfName)) {
                                inserts.append(logEntry.getInsert(j))
                                        .append("|");
                            }
                        }

                        return PropertyBox.builder(ActivityLogModel.PROPERTIES)
                                .set(ActivityLogModel.SNO, i.incrementAndGet())
                                .set(ActivityLogModel.TIMESTAMP, logEntry.getTimestamp())
                                .set(ActivityLogModel.STATUS, StringUtils.right(logEntry.getMessage(), 1))
                                .set(ActivityLogModel.SOURCE, logEntry.getSource())
                                .set(ActivityLogModel.DETAIL, logEntry.getDetail())
                                .set(ActivityLogModel.TAGS, sb.toString())
                                .set(ActivityLogModel.INSERTS, inserts.toString())
                                .build();
                    }).collect(Collectors.toList());


        } else {
            return Collections.singletonList(PropertyBox.builder(ActivityLogModel.PROPERTIES)
                    .set(ActivityLogModel.SNO, 1)
                    .set(ActivityLogModel.STATUS, "E")
                    .set(ActivityLogModel.DETAIL, "Flow is in stopped state. Cannot fetch activity log")

                    .build());
        }

    }

    @SneakyThrows
    public static List<PropertyBox> discoverEGProperties(ExecutionGroupProxy eg) {

        List<PropertyBox> pbs = new ArrayList<>();
        int i = 0, j = 0;

        pbs.add(PropertyBox.builder(BrokerPropertiesModel.PROPERTIES)
                .set(BrokerPropertiesModel.SNO, String.valueOf(++i))
                .set(BrokerPropertiesModel.NODE_NAME, MyUtils.formatKey("Basic Properties"))
                .set(BrokerPropertiesModel.NODE_TYPE, "")

                .build());

        for (Map.Entry<Object, Object> prop : eg.getBasicProperties().entrySet()) {
            pbs.add(PropertyBox.builder(BrokerPropertiesModel.PROPERTIES)
                    .set(BrokerPropertiesModel.SNO, i + "." + ++j)
                    .set(BrokerPropertiesModel.NODE_NAME, String.valueOf(prop.getKey()))
                    .set(BrokerPropertiesModel.NODE_TYPE, String.valueOf(prop.getValue()))

                    .build());
        }
        j = 0;
        pbs.add(PropertyBox.builder(BrokerPropertiesModel.PROPERTIES)
                .set(BrokerPropertiesModel.SNO, String.valueOf(++i))
                .set(BrokerPropertiesModel.NODE_NAME, MyUtils.formatKey("Advanced Properties"))
                .set(BrokerPropertiesModel.NODE_TYPE, "")

                .build());

        for (Map.Entry<Object, Object> prop : eg.getAdvancedProperties().entrySet()) {
            pbs.add(PropertyBox.builder(BrokerPropertiesModel.PROPERTIES)
                    .set(BrokerPropertiesModel.SNO, i + "." + ++j)
                    .set(BrokerPropertiesModel.NODE_NAME, String.valueOf(prop.getKey()))
                    .set(BrokerPropertiesModel.NODE_TYPE, String.valueOf(prop.getValue()))

                    .build());
        }
        j = 0;

        pbs.add(PropertyBox.builder(BrokerPropertiesModel.PROPERTIES)
                .set(BrokerPropertiesModel.SNO, String.valueOf(++i))
                .set(BrokerPropertiesModel.NODE_NAME, MyUtils.formatKey("Runtime Properties"))
                .set(BrokerPropertiesModel.NODE_TYPE, "")

                .build());

        for (String prop : eg.getRuntimePropertyNames()) {

            pbs.add(PropertyBox.builder(BrokerPropertiesModel.PROPERTIES)
                    .set(BrokerPropertiesModel.SNO, i + "." + ++j)
                    .set(BrokerPropertiesModel.NODE_NAME, prop)
                    .set(BrokerPropertiesModel.NODE_TYPE, eg.getRuntimeProperty(prop))

                    .build());

        }

        pbs.add(PropertyBox.builder(BrokerPropertiesModel.PROPERTIES)
                .set(BrokerPropertiesModel.SNO, i + "." + ++j)
                .set(BrokerPropertiesModel.NODE_NAME, "UUID")
                .set(BrokerPropertiesModel.NODE_TYPE, eg.getUUID())

                .build());

        return pbs;

    }

    @SneakyThrows
    public static List<PropertyBox> discoverBrkProperties(BrokerProxy bp) {

        List<PropertyBox> pbs = new ArrayList<>();
        int i = 0, j = 0;

        pbs.add(PropertyBox.builder(BrokerPropertiesModel.PROPERTIES)
                .set(BrokerPropertiesModel.SNO, String.valueOf(++i))
                .set(BrokerPropertiesModel.NODE_NAME, MyUtils.formatKey("Basic Properties"))
                .set(BrokerPropertiesModel.NODE_TYPE, "")

                .build());


        for (Map.Entry<Object, Object> prop : bp.getBasicProperties().entrySet()) {
            pbs.add(PropertyBox.builder(BrokerPropertiesModel.PROPERTIES)
                    .set(BrokerPropertiesModel.SNO, i + "." + ++j)
                    .set(BrokerPropertiesModel.NODE_NAME, String.valueOf(prop.getKey()))
                    .set(BrokerPropertiesModel.NODE_TYPE, String.valueOf(prop.getValue()))

                    .build());
        }
        j = 0;
        pbs.add(PropertyBox.builder(BrokerPropertiesModel.PROPERTIES)
                .set(BrokerPropertiesModel.SNO, String.valueOf(++i))
                .set(BrokerPropertiesModel.NODE_NAME, MyUtils.formatKey("Advanced Properties"))
                .set(BrokerPropertiesModel.NODE_TYPE, "")

                .build());

        for (Map.Entry<Object, Object> prop : bp.getAdvancedProperties().entrySet()) {
            pbs.add(PropertyBox.builder(BrokerPropertiesModel.PROPERTIES)
                    .set(BrokerPropertiesModel.SNO, i + "." + ++j)
                    .set(BrokerPropertiesModel.NODE_NAME, String.valueOf(prop.getKey()))
                    .set(BrokerPropertiesModel.NODE_TYPE, String.valueOf(prop.getValue()))

                    .build());
        }
        j = 0;
        pbs.add(PropertyBox.builder(BrokerPropertiesModel.PROPERTIES)
                .set(BrokerPropertiesModel.SNO, String.valueOf(++i))
                .set(BrokerPropertiesModel.NODE_NAME, MyUtils.formatKey("Cache Manager Properties"))
                .set(BrokerPropertiesModel.NODE_TYPE, "")

                .build());

        for (String cache : bp.getCacheManagerPropertyNames()) {
            pbs.add(PropertyBox.builder(BrokerPropertiesModel.PROPERTIES)
                    .set(BrokerPropertiesModel.SNO, i + "." + ++j)
                    .set(BrokerPropertiesModel.NODE_NAME, cache)
                    .set(BrokerPropertiesModel.NODE_TYPE, bp.getCacheManagerProperty(cache))

                    .build());
        }
        j = 0;
        pbs.add(PropertyBox.builder(BrokerPropertiesModel.PROPERTIES)
                .set(BrokerPropertiesModel.SNO, String.valueOf(++i))
                .set(BrokerPropertiesModel.NODE_NAME, MyUtils.formatKey("HTTPListener Properties"))
                .set(BrokerPropertiesModel.NODE_TYPE, "")

                .build());

        for (String name : bp.getHTTPListenerPropertyNames()) {
            pbs.add(PropertyBox.builder(BrokerPropertiesModel.PROPERTIES)
                    .set(BrokerPropertiesModel.SNO, i + "." + ++j)
                    .set(BrokerPropertiesModel.NODE_NAME, name)
                    .set(BrokerPropertiesModel.NODE_TYPE, bp.getHTTPListenerProperty(name))

                    .build());
        }

        j = 0;
        pbs.add(PropertyBox.builder(BrokerPropertiesModel.PROPERTIES)
                .set(BrokerPropertiesModel.SNO, String.valueOf(++i))
                .set(BrokerPropertiesModel.NODE_NAME, MyUtils.formatKey("Registry Properties"))
                .set(BrokerPropertiesModel.NODE_TYPE, "")

                .build());

        for (String name : bp.getRegistryPropertyNames()) {
            pbs.add(PropertyBox.builder(BrokerPropertiesModel.PROPERTIES)
                    .set(BrokerPropertiesModel.SNO, i + "." + ++j)
                    .set(BrokerPropertiesModel.NODE_NAME, name)
                    .set(BrokerPropertiesModel.NODE_TYPE, bp.getRegistryProperty(name))

                    .build());
        }

        j = 0;
        pbs.add(PropertyBox.builder(BrokerPropertiesModel.PROPERTIES)
                .set(BrokerPropertiesModel.SNO, String.valueOf(++i))
                .set(BrokerPropertiesModel.NODE_NAME, MyUtils.formatKey("Security Identities"))
                .set(BrokerPropertiesModel.NODE_TYPE, "")

                .build());

        for (SecurityIdentity sid : bp.getSecurityIdentities()) {
            pbs.add(PropertyBox.builder(BrokerPropertiesModel.PROPERTIES)
                    .set(BrokerPropertiesModel.SNO, i + "." + ++j)
                    .set(BrokerPropertiesModel.NODE_NAME, sid.getName())
                    .set(BrokerPropertiesModel.NODE_TYPE, sid.getType().name() + " | " + sid.getResource())

                    .build());
        }

        j = 0;
        pbs.add(PropertyBox.builder(BrokerPropertiesModel.PROPERTIES)
                .set(BrokerPropertiesModel.SNO, String.valueOf(++i))
                .set(BrokerPropertiesModel.NODE_NAME, MyUtils.formatKey("Web Users"))
                .set(BrokerPropertiesModel.NODE_TYPE, "")

                .build());

        Enumeration<WebUserProxy> webUsers = bp.getWebAdminProxy().getWebUsers();

        while (webUsers.hasMoreElements()) {
            WebUserProxy userProxy = webUsers.nextElement();
            pbs.add(PropertyBox.builder(BrokerPropertiesModel.PROPERTIES)
                    .set(BrokerPropertiesModel.SNO, i + "." + ++j)
                    .set(BrokerPropertiesModel.NODE_NAME, userProxy.getName())
                    .set(BrokerPropertiesModel.NODE_TYPE, userProxy.getRole())

                    .build());

        }


        return pbs;
    }

    @SneakyThrows
    public static List<PropertyBox> readBar(byte[] content, String barFileName) {

        AtomicInteger i = new AtomicInteger();

        List<PropertyBox> pbs = new ArrayList<>();

        Collections.list(BarFile.loadBarFile(content,barFileName).getBarEntries())
                .stream()
                .filter(barEntry -> barEntry.isApplication() || barEntry.isLibrary() || barEntry.isSharedLibrary())
                .forEach(barEntry -> {

                    try {
                        BarFile barFile = BarFile.loadBarFile(barEntry.getBytes(), barEntry.getFullName());
                        Optional<DeploymentDescriptor> deploymentdescriptor = Optional.ofNullable(barFile.getDeploymentDescriptor());
                        deploymentdescriptor.ifPresent(deploymentDescriptor -> {
                            Collections.list(deploymentdescriptor.get().getPropertyIdentifiers())
                                    .stream().filter(propertyId -> propertyId.contains("#"))
                                    .forEach(propertyId -> {
                                        pbs.add(PropertyBox.builder(BarModel.PROPERTIES)
                                                .set(BarModel.SNO, i.incrementAndGet())
                                                .set(BarModel.KEY, propertyId)
                                                .set(BarModel.VALUE, deploymentdescriptor.get().getOverride(propertyId))
                                                .build());
                                    });
                        });
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                });

        return pbs;
    }

    @SneakyThrows
    public static List<PropertyBox> adminLog(BrokerProxy brokerProxy, Integer hours, String logLevel) {

        AtomicInteger i = new AtomicInteger();
        Date date = DateUtils.addHours(new Date(), hours != null ? hours * -1 : -24);

        return Collections.list(brokerProxy.getLog().elements())
                .stream()
                .filter(logEntry -> logEntry.getTimestamp().after(date))
                .filter(logEntry -> {
                    return BrokerUtils.addLogFilter(logEntry.getMessage(), logLevel);
                })
                .map(logEntry -> {

                    StringBuilder sb = new StringBuilder();

                    for (int j = 0; j < logEntry.getInsertsSize(); j++) {
                        sb.append(logEntry.getInsert(j)).append("|");
                    }
                    return PropertyBox.builder(ActivityLogModel.PROPERTIES)
                            .set(ActivityLogModel.SNO, i.incrementAndGet())
                            .set(ActivityLogModel.TIMESTAMP, logEntry.getTimestamp())
                            .set(ActivityLogModel.STATUS, StringUtils.right(logEntry.getMessage(), 1))
                            .set(ActivityLogModel.SOURCE, logEntry.getSource())
                            .set(ActivityLogModel.DETAIL, logEntry.getDetail())
                            .set(ActivityLogModel.TAGS, "NA")
                            .set(ActivityLogModel.INSERTS, sb.toString())
                            .build();
                }).collect(Collectors.toList());

    }



    @SneakyThrows
    public static Collection<? extends PropertyBox> adminLog(BrokerProxy brokerProxy, MessageFlowProxy mf, Date date, String logLevel) {
        AtomicInteger i = new AtomicInteger();
        String name = mf.getName();
        return Collections.list(brokerProxy.getLog().elements())
                .stream()
                .filter(logEntry -> DateUtils.isSameDay(logEntry.getTimestamp(), date)
                        && BrokerUtils.addLogFilter(logEntry.getMessage(), logLevel)
                        && StringUtils.contains(logEntry.getDetail(), name))
                /*.filter(logEntry -> {
                    return BrokerUtils.addLogFilter(logEntry.getMessage(), logLevel);
                })*/
                .map(logEntry -> {

                    StringBuilder sb = new StringBuilder();

                    for (int j = 0; j < logEntry.getInsertsSize(); j++) {
                        sb.append(logEntry.getInsert(j)).append("|");
                    }
                    return PropertyBox.builder(ActivityLogModel.PROPERTIES)
                            .set(ActivityLogModel.SNO, i.incrementAndGet())
                            .set(ActivityLogModel.TIMESTAMP, logEntry.getTimestamp())
                            .set(ActivityLogModel.STATUS, StringUtils.left(logLevel, 1))
                            .set(ActivityLogModel.SOURCE, logEntry.getSource())
                            .set(ActivityLogModel.DETAIL, logEntry.getDetail())
                            .set(ActivityLogModel.TAGS, "NA")
                            .set(ActivityLogModel.INSERTS, sb.toString())
                            .build();
                }).collect(Collectors.toList());
    }

    @SneakyThrows
    public static List<PropertyBox> getMFProperties(MessageFlowProxy proxy) {
        AtomicInteger i = new AtomicInteger();
        return FlowUtils.getFlowProperties(proxy)
                .entrySet()
                .stream().map(entry -> PropertyBox.builder(MFProperties.PROPERTIES)
                        .set(MFProperties.SNO, i.incrementAndGet())
                        .set(MFProperties.NODE_NAME, entry.getKey())
                        .set(MFProperties.NODE_TYPE, entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    @SneakyThrows
    public static List<PropertyBox> getSFProperties(SubFlowProxy proxy) {
        AtomicInteger i = new AtomicInteger();
        return FlowUtils.getSubFlowProperties(proxy)
                .entrySet()
                .stream().map(entry -> PropertyBox.builder(MFProperties.PROPERTIES)
                        .set(MFProperties.SNO, i.incrementAndGet())
                        .set(MFProperties.NODE_NAME, entry.getKey())
                        .set(MFProperties.NODE_TYPE, entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    @SneakyThrows
    public static List<PropertyBox> getRestAPIProperties(RestApiProxy proxy) {
        AtomicInteger i = new AtomicInteger();
        return FlowUtils.getRestAPIProperties(proxy)
                .entrySet()
                .stream().map(entry -> PropertyBox.builder(MFProperties.PROPERTIES)
                        .set(MFProperties.SNO, i.incrementAndGet())
                        .set(MFProperties.NODE_NAME, entry.getKey())
                        .set(MFProperties.NODE_TYPE, entry.getValue())
                        .build())
                .collect(Collectors.toList());
    }

    @SneakyThrows
    public static List<PropertyBox> applnListing(List<ApplicationProxy> applicationProxies) {
        AtomicInteger i = new AtomicInteger();

        List<PropertyBox> pbs = new ArrayList<>();

        for (ApplicationProxy proxy : applicationProxies) {

            StringBuilder sLibDependents = new StringBuilder();

            for (Map.Entry<SharedLibraryReference, SharedLibraryProxy> entry : proxy
                    .getSharedLibraryDependencies().entrySet()) {
                sLibDependents.append(MyUtils
                        .formatObjectEnumeration(entry.getValue()
                                .getSharedLibraryDependents()));
            }

            pbs.add(PropertyBox.builder(ApplnModel.PROPERTIES)
                    .set(ApplnModel.SNO, i.incrementAndGet())
                    .set(ApplnModel.NAME, proxy.getName())
                    .set(ApplnModel.STATUS, proxy.isRunning())
                    .set(ApplnModel.SHALED_LIB_DEPENDENCIES, MyUtils.formatObjectMap(proxy
                            .getSharedLibraryDependencies()))
                    .set(ApplnModel.SHALED_LIB_DEPENDENTENTS, sLibDependents.toString())
                    .set(ApplnModel.STATIC_LIB, MyUtils.formatObjectEnumeration(proxy
                            .getStaticLibraries(null)))
                    .set(ApplnModel.EG_NAME, proxy.getExecutionGroup().getName())
                    .build());
        }

        return pbs;
    }

    @SneakyThrows
    public static List<PropertyBox> configSvces(BrokerProxy brokerProxy) {
        List<PropertyBox> pbs = new ArrayList<>();
        int i = 0;

        for (ConfigurableService cs : brokerProxy.getConfigurableServices(null)) {

            pbs.add(PropertyBox.builder(ConfigSvcModel.PROPERTIES)
                    .set(ConfigSvcModel.SNO, String.valueOf(++i))
                    .set(ConfigSvcModel.CS_NAME, cs.getName())
                    .set(ConfigSvcModel.CS_TYPE, cs.getType())
                    .build()
            );

            int j = 1;

            for (Map.Entry<?, ?> entry : cs.getProperties().entrySet()) {
                pbs.add(PropertyBox.builder(ConfigSvcModel.PROPERTIES)
                        .set(ConfigSvcModel.SNO, i + "." + (j++))
                        .set(ConfigSvcModel.PROP_NAME, String.valueOf(entry.getKey()))
                        .set(ConfigSvcModel.PROP_TYPE, String.valueOf(entry.getValue()))
                        .build()
                );
            }

        }

        return pbs;
    }

    public static MessageFlowProxy findMf(PropertyBox propertyBox, FlowUtils flowUtils) {

        String egName = propertyBox.getValue(FlowProxyModel.EG_NAME);
        String parentType = propertyBox.getValue(FlowProxyModel.PARENT_TYPE);
        String parentName = propertyBox.getValue(FlowProxyModel.PARENT_NAME);
        String mfName = propertyBox.getValue(FlowProxyModel.NAME);

        MessageFlowProxy mf = null;

        switch (parentType) {
            case "Application":
                mf = flowUtils.egName(egName)
                        .applicationName(parentName)
                        .mfName(mfName)
                        .getMFlowProxyFromApp();
                break;
            case "REST API":
                mf = flowUtils.egName(egName)
                        .restAPIName(parentName)
                        .mfName(mfName)
                        .getMFlowProxyFromRestAPI();
                break;
            case "Static Libray":
                mf = flowUtils.egName(egName)
                        .staticLibName(parentName)
                        .mfName(mfName)
                        .getMFlowProxyFromStaticLib();
                break;
        }

        return mf;
    }

    public static LinkedHashMap<String, String[]> buildEventMonitoringConfigScreen(MessageFlowProxy mf) throws ConfigManagerProxyPropertyNotInitializedException, ConfigManagerProxyLoggedException, IOException {

        Map<String, String> source = new LinkedHashMap<>();

        for (MessageFlowProxy.Node node : FlowUtils.getInputNodes(mf)) {
            for (String add : FlowUtils.prepInputNodeEventSourceAddress(node)) {
                source.put(add, node.getType());
            }
            source.putAll(FlowUtils.getEventSourceAddress(mf, node));
        }

        Enumeration<MessageFlowProxy.Node> nodes = mf.getNodes();

        SubFlowProxy sf = null;
        AdministeredObject adObj = mf.getParent();
        String name = adObj.getName();
        ExecutionGroupProxy eg = mf.getExecutionGroup();
        ApplicationProxy app = eg.getApplicationByName(name);
        SharedLibraryProxy sh = eg.getSharedLibraryByName(name);
        StaticLibraryProxy st = eg.getStaticLibraryByName(name);
        String subflowURI = null;

        while (nodes.hasMoreElements()) {
            MessageFlowProxy.Node node = nodes
                    .nextElement();
//            name = node.getName();

            if (node.getType().equalsIgnoreCase("SubFlowNode")) {
                subflowURI = node.getProperties().getProperty("subflowURI");
                name = StringUtils.substringAfterLast(subflowURI, "subflows/");
                if (app != null) {
                    st = app.getStaticLibraryByName(StringUtils.substringBetween(subflowURI, "libraries/", "/subflows"));
                    if (st != null) {
                        sf = st.getSubFlowByName(name);
                    } else {
                        sf = app.getSubFlowByName(name);
                    }
                } else if(sh != null) {
                    sf = sh.getSubFlowByName(name);
                }else if(st != null) {
                    sf = st.getSubFlowByName(name);
                }else {
                    sf = eg.getSubFlowByName(name);
                }

                if (sf != null) {
                    source.putAll(FlowUtils.getEventSourceAddress(sf, node));
                }
            }else if (!source.containsValue(node)) {
                source.putAll(FlowUtils.getEventSourceAddress(mf, node));
            }
        }

//        System.out.println(source);

        LinkedHashMap<String, String[]> csv = new LinkedHashMap<>();
        for (Map.Entry<String, String> entry : source.entrySet()) {
            csv.put(entry.getKey(),new String[] {entry.getKey(), entry.getValue()});
        }

        Path profilePath = Paths.get(properties.getProperty("MON_PROFILE_DIR"),mf.getExecutionGroup().getParent().getName(), prepMonitoringProfileName(mf)+".csv");
        if (Files.exists(profilePath)) {
            MyComponents.successNotify("Profile already exists and is loaded from file "+profilePath.getFileName());

            for (String line : Files.readAllLines(profilePath, Charset.defaultCharset())) {
                csv.put(line.split(",")[0],line.split(","));
            }
        }else{
            UIUtils.showNotification("Profile already does not exist ");//so creating profile file "+profilePath.getFileName() + " at "+profilePath.toString());
        }

        System.out.println(csv);

        return csv;
    }

    private static String prepMonitoringProfileName(MessageFlowProxy mf) throws ConfigManagerProxyPropertyNotInitializedException, ConfigManagerProxyLoggedException {

        StringBuilder sb = new StringBuilder();

        sb.append(mf.getExecutionGroup().getName());
        sb.append(".");
        sb.append(mf.getParent().getName());
        sb.append(".");
        sb.append(mf.getName());

        return sb.toString();
    }

    public static SubFlowProxy findSf(PropertyBox propertyBox, FlowUtils flowUtils) {

        String egName = propertyBox.getValue(FlowProxyModel.EG_NAME);
        String parentType = propertyBox.getValue(FlowProxyModel.PARENT_TYPE);
        String parentName = propertyBox.getValue(FlowProxyModel.PARENT_NAME);
        String sfName = propertyBox.getValue(FlowProxyModel.NAME);

        SubFlowProxy sf;

        switch (parentType) {
            case "Application":
                sf = flowUtils.egName(egName)
                        .applicationName(parentName)
                        .subFlowName(sfName)
                        .getSFlowProxyFromApp();
                break;
            case "REST API":
                sf = flowUtils.egName(egName)
                        .restAPIName(parentName)
                        .subFlowName(sfName)
                        .getSFlowProxyFromRestAPI();
                break;
            case "Library":
                sf = flowUtils.egName(egName)
                        .staticLibName(parentName)
                        .subFlowName(sfName)
                        .getSFlowProxyFromStaticLib();
                break;
            default:
                sf = flowUtils.egName(egName)
                        .sharedLibName(parentName)
                        .subFlowName(sfName)
                        .getSFlowProxyFromSharedLib();
        }
        return sf;
    }

    @SneakyThrows
    public static List<PropertyBox> restAPIsListing(List<RestApiProxy> restApiProxies) {

        List<PropertyBox> pbs = new ArrayList<>();
        int i = 0;

        for (RestApiProxy restApiProxy : restApiProxies) {

            pbs.add(PropertyBox.builder(RestApiModel.PROPERTIES)
//                    .set(RestApiModel.UUID, restApiProxy.getUUID())
                    .set(RestApiModel.SNO, ++i)
                    .set(RestApiModel.NAME, restApiProxy.getName())
//                    .set(RestApiModel.API_PORT, restApiProxy.getApi().getPort())
                    .set(RestApiModel.BASE_URL, restApiProxy.getBaseURL().toString())
                    .set(RestApiModel.LOCAL_BASE_URL, restApiProxy.getLocalBaseURL().toString())
                    .set(RestApiModel.LOCAL_DEF_URL, restApiProxy.getDefinitionsURL().toString())
                    .set(RestApiModel.LOCAL_DEF_URL, restApiProxy.getLocalDefinitionsURL().toString())
                    .set(RestApiModel.OPS_COUNT, restApiProxy.getOperations().size())
                    .set(RestApiModel.EG_NAME, restApiProxy.getExecutionGroup().getName())

                    .build());
        }

        return pbs;
    }
}
