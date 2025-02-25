package com.myapp.iib.admin;

import com.holonplatform.core.internal.utils.ObjectUtils;
import com.ibm.broker.config.proxy.*;
import com.ibm.broker.rest.Response;
import com.ibm.broker.rest.*;
import com.myapp.iib.my.MyUtils;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;
import lombok.experimental.Accessors;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Accessors(fluent = true, chain = true)
@Getter
@Setter
@Service
public class FlowUtils {

    private BrokerProxy bp;
    private String brokerFile;
    private List<ExecutionGroupProxy> allEGs;
    private List<ApplicationProxy> allApplications;
    //    private List<LibraryProxy> libList;
    private List<MessageFlowProxy> allMFs;
    private List<SubFlowProxy> allSubFlows;
    private List<ServiceInterface> allSOAPServices;
    private List<RestApiProxy> allRestAPIs;
    private List<StaticLibraryProxy> allStaticLibs;
    private List<SharedLibraryProxy> allSharedLibs;
    private boolean remote;
    private String egName;
    private String applicationName;
    private String staticLibName;
    private String sharedLibName;
    private String restAPIName;
    private String integrationServiceName;
    private String mfName;
    private String subFlowName;

    private BrokerProxy brokerProxy;
    private ExecutionGroupProxy egProxy;
    private ApplicationProxy applicationProxy;
    private SharedLibraryProxy sharedLibraryProxy;
    private StaticLibraryProxy staticLibraryProxy;
    private SubFlowProxy subFlowProxy;
    private RestApiProxy restApiProxy;
    private List<ConfigurableService> configurableServices;

    public FlowUtils() {
        initialize();
    }


    @SneakyThrows
    public RestApiProxy restApiProxy() {
        return egProxy
                .getRestApiByName(restAPIName);
    }

    @SneakyThrows
    public ExecutionGroupProxy egProxy() {
        return brokerProxy.getExecutionGroupByName(egName);
    }

    @SneakyThrows
    public ApplicationProxy applicationProxy() {
        return egProxy.getApplicationByName(applicationName);
    }

    @SneakyThrows
    public SharedLibraryProxy sharedLibraryProxy() {
        return egProxy.getSharedLibraryByName(sharedLibName);
    }

    public static LinkedHashMap<String, String> getSubFlowProperties(SubFlowProxy sf)
            throws IllegalArgumentException, ConfigManagerProxyException {

        LinkedHashMap<String, String> map = new LinkedHashMap<>();
        ExecutionGroupProxy egProxy = sf.getExecutionGroup();

        String parentName = sf.getParent().getName();

        map.put("Flow name ", sf.getName());
        map.put("Type ", sf.getType());
        map.put("Parent Name ", parentName);
        map.put("Parent Type ", sf.getConfigurationObjectTypeOfParent()
                .getDisplayName());
        map.put("EG Name ", egProxy.getName());
        map.put("EG Running ",
                String.valueOf(sf.getExecutionGroup().isRunning()));
        map.put("Node Name ", egProxy.getParent().getName());

        map.put("Deployed Time ", sf.getDeployTime().toString());
        map.put("Modifled Time ", sf.getModifyTime().toString());
        map.put("Deployed From", sf.getBARFileName());
        map.put("UUID", sf.getUUID());
        map.put("Last Deployment Status", sf.getLastCompletionCode().toString());
        map.put("NumberOfSubcomponents",
                String.valueOf(sf.getNumberOfSubcomponents()));

        if (sf.getConfigurationObjectTypeOfParent() == ConfigurationObjectType.application) {

            ApplicationProxy applicationProxy = egProxy
                    .getApplicationByName(parentName);
            map.putAll(getSharedLibDependencies(applicationProxy
                    .getSharedLibraryDependencies()));

            map.put("Dependencies", getDependentFlows(applicationProxy));

        } else if (sf.getConfigurationObjectTypeOfParent() == ConfigurationObjectType.staticLibrary) {

            LibraryProxy libraryProxy = egProxy
                    .getStaticLibraryByName(parentName) == null ? egProxy
                    .getSharedLibraryByName(parentName) : egProxy
                    .getStaticLibraryByName(parentName);
            if (libraryProxy != null) {
                map.put("Static Library", libraryProxy.getName());
                map.put("Dependencies", getDependentFlows(libraryProxy));
            }
        }

        String[] userDefinedPropertyNames = sf.getUserDefinedPropertyNames();
        if (userDefinedPropertyNames.length > 0) {
            for (String userDefinedPropertyName : userDefinedPropertyNames) {
                map.put(userDefinedPropertyName, ((String) sf
                        .getUserDefinedProperty(userDefinedPropertyName)));
            }
        } else {
            map.put("User Defined Properties Used ", "NIL");
        }

        String[] queues = sf.getQueues();

        if (queues.length > 0) {
            int i = 0;
            for (String q : queues) {
                map.put("Queue " + ++i, q);
            }
            // details.append(terminator);
        } else {
            map.put("Queues Used ", "NIL");
        }

        map.putAll(getHTTPPortDetails(egProxy));

        Enumeration<com.ibm.broker.config.proxy.MessageFlowProxy.Node> nodeUsed = sf
                .getNodes();
        int i = 1, j;
        while (nodeUsed.hasMoreElements()) {
            MessageFlowProxy.Node node = nodeUsed
                    .nextElement();
            map.put(i + " ---------------------> Node Name : " + node.getName()
                    + "<---------------------", " Type : " + node.getType());
            Properties prop = node.getProperties();

            Enumeration<?> entry = prop.propertyNames();
            j = 1;
            while (entry.hasMoreElements()) {
                String key = (String) entry.nextElement();
                map.put(i + "." + j++ + " : " + key,
                        String.valueOf(prop.getProperty(key)));
                // System.out.println(key + " -- " + prop.getProperty(key));
            }

            i++;
            // for (Map.Entry<?, ?> entry : prop.entrySet()) {
            // String value = (String) entry.getValue();
            // if (value.length() > 0) {
            // map.put(
            // String.valueOf(entry.getKey()),
            // String.valueOf(entry.getValue()));
            // }
            // }

        }

        map.put(" ---------------------> Runtime Property : <---------------------",
                " Value : ");

        j = 1;

        for (String runTime : sf.getRuntimePropertyNames()) {

            map.put(i + "." + j++ + " : " + runTime,
                    String.valueOf(sf.getRuntimeProperty(runTime)));
        }

        map.put(formatKey("Node Connections"), "");
        Enumeration<MessageFlowProxy.NodeConnection> nodeConnections = sf.getNodeConnections();
        j = 1;
        while (nodeConnections.hasMoreElements()) {
            MessageFlowProxy.NodeConnection nodeConnection = nodeConnections.nextElement();
            map.put(String.valueOf(j++), nodeConnection.toString());
        }


        // System.out.println(map);

        return map;

    }

    /*@SneakyThrows
    public FlowUtils restAPIName(String name) {
        ObjectUtils.argumentNotNull(egProxy, "EG cannot be null");
        this.restApiProxy = this.egProxy.getRestApiByName(name);
        return this;
    }*/

    @SneakyThrows
    public static boolean isMFMatching(MessageFlowProxy proxy, String value) {
        return StringUtils.containsIgnoreCase(proxy.getName(), value);
    }

    @SneakyThrows
    public static void updateUDP(MessageFlowProxy mf, String name, String value) {
        if (!mf.getUserDefinedProperty(name).equals(value)) {
            mf.setUserDefinedProperty(name, value);
        }
    }

    public static String getNodeType(MessageFlowProxy.Node node) {
        return StringUtils.substringAfter(node.getType(), "ComIbm");
    }

    private void initialize() {
        allEGs = new ArrayList<>();
        allApplications = new ArrayList<>();
//        libList = new ArrayList<>();
        allMFs = new ArrayList<>();
        allSubFlows = new ArrayList<>();
        allSOAPServices = new ArrayList<>();
    }

    @SneakyThrows
    public List<ConfigurableService> configurableServices(BrokerProxy brokerProxy) {
        this.brokerProxy = brokerProxy;
        return Arrays.asList(this.brokerProxy.getConfigurableServices(null));
    }

    public List<MessageFlowProxy> findMFProxyByName(String mf) {

        return this.allMFs.stream().filter(messageFlowProxy -> {
            try {
                return messageFlowProxy.getName().equals(mf);
            } catch (ConfigManagerProxyPropertyNotInitializedException e) {
                e.printStackTrace();
            }
            return false;
        })
                .collect(Collectors.toList());

    }

    public List<SubFlowProxy> findSubFlowProxyByName(String sf) {

        return this.allSubFlows.stream().filter(proxy -> {
            try {
                return proxy.getName().equals(sf);
            } catch (ConfigManagerProxyPropertyNotInitializedException e) {
                e.printStackTrace();
            }
            return false;
        })
                .collect(Collectors.toList());

    }

    @SneakyThrows
    public FlowUtils connect(String brokerFile) {

        this.brokerFile = brokerFile;

        return connect();

    }

    @SneakyThrows
    public FlowUtils connect() {

        BrokerConnectionParameters bp = new IntegrationNodeConnectionParameters(brokerFile);
        brokerProxy = BrokerProxy.getInstance(bp);
        return this;

    }


    @SneakyThrows
    public FlowUtils getEG(String egName) {
        egProxy = this.brokerProxy.getExecutionGroupByName(egName);
        return this;
    }

    public Set<String> allEGNames(List<ExecutionGroupProxy> proxies) {
        this.allEGs = proxies;
        return allEGNames();
    }

    public Set<String> allEGNames() {
        return this.allEGs.stream().flatMap(executionGroupProxy -> {
            try {
                return Stream.of(executionGroupProxy.getName());
            } catch (ConfigManagerProxyPropertyNotInitializedException e) {
                e.printStackTrace();
            }
            return null;
        }).collect(Collectors.toSet());
    }

    public List<ExecutionGroupProxy> allEGs(BrokerProxy bp) {
        this.brokerProxy = bp;
        return allEGs();
    }

    @SneakyThrows
    public List<ExecutionGroupProxy> allEGs() {
        return this.allEGs = Collections.list(brokerProxy.getExecutionGroups(null));
    }

    @SneakyThrows
    public List<ApplicationProxy> allApplications() {

        ObjectUtils.argumentNotNull(brokerProxy, "Node connection is not established. Possibly brokerProxy is null");
        this.allApplications = new ArrayList<>();

        for (ExecutionGroupProxy egProxy : allEGs()) {

            allApplications.addAll(Collections.list(egProxy.getApplications(null)));

        }


        return this.allApplications;
    }

    @SneakyThrows
    public List<StaticLibraryProxy> allStaticLibs(ApplicationProxy applicationProxy) {
        ObjectUtils.argumentNotNull(applicationProxy, "ApplicationProxy cannot be null");
        return this.allStaticLibs = Collections.list(applicationProxy.getStaticLibraries(null));
    }

    @SneakyThrows
    public List<StaticLibraryProxy> allStaticLibs(ExecutionGroupProxy egProxy) {
        return this.allStaticLibs = Collections.list(egProxy.getStaticLibraries(null));
    }

    @SneakyThrows
    public List<StaticLibraryProxy> allStaticLibs() {

        List<StaticLibraryProxy> list = new ArrayList<>();

        allEGs()
                .forEach(egProxy -> {
                    list.addAll(allStaticLibs(egProxy));
                    allApplications(egProxy)
                            .forEach(proxy -> list.addAll(allStaticLibs(proxy)));
                });

        return list;


    }

    @SneakyThrows
    public List<SharedLibraryProxy> allSharedLibs(ExecutionGroupProxy egProxy) {
        ObjectUtils.argumentNotNull(egProxy, "Execution Proxy cannot be null");
        this.egProxy = egProxy;
        return this.allSharedLibs = Collections.list(egProxy.getSharedLibraries(null));
    }

    @SneakyThrows
    public List<SharedLibraryProxy> allSharedLibs() {
        List<SharedLibraryProxy> list = new ArrayList<>();
        allEGs().forEach(egProxy -> list.addAll(allSharedLibs(egProxy)));
        return list;
    }

    @SneakyThrows
    public List<ApplicationProxy> allApplications(ExecutionGroupProxy egProxy) {
        return Collections.list(egProxy.getApplications(null));
    }

    @SneakyThrows
    public List<MessageFlowProxy> allMFsIndepentPrj(ExecutionGroupProxy egProxy) {
        return Collections.list(egProxy.getMessageFlows(null));
    }

    @SneakyThrows
    public List<MessageFlowProxy> allMFs(ApplicationProxy applicationProxy) {
        ArrayList<MessageFlowProxy> list = Collections.list(applicationProxy.getMessageFlows(null));
        allStaticLibs(applicationProxy).forEach(staticLibraryProxy -> list.addAll(allMFs(staticLibraryProxy)));
        return list;
    }

    @SneakyThrows
    public List<MessageFlowProxy> allMFs(StaticLibraryProxy staticLibraryProxy) {
        return Collections.list(staticLibraryProxy.getMessageFlows(null));
    }

    @SneakyThrows
    public List<MessageFlowProxy> allMFs(RestApiProxy restApiProxy) {
        return Collections.list(restApiProxy.getMessageFlows(null));
    }

    /*@SneakyThrows
    public ArrayList<MessageFlowProxy> allMFs(ServiceInterface serviceInterface) {
        return Collections.list(serviceInterface.);
    }*/

    @SneakyThrows
    public List<SubFlowProxy> allSubFlows(ApplicationProxy applicationProxy) {
        return Collections.list(applicationProxy.getSubFlows(null));
    }

    @SneakyThrows
    public List<SubFlowProxy> allSubFlows(SharedLibraryProxy sharedLibraryProxy) {
        return Collections.list(sharedLibraryProxy.getSubFlows(null));

    }

    @SneakyThrows
    public List<SubFlowProxy> allSubFlows(StaticLibraryProxy staticLibraryProxy) {
        return Collections.list(staticLibraryProxy.getSubFlows(null));

    }

    @SneakyThrows
    public List<SubFlowProxy> allSubFlows(RestApiProxy restApiProxy) {
        return Collections.list(restApiProxy.getSubFlows(null));

    }

    @SneakyThrows
    public List<SubFlowProxy> allSubFlowsIndependPrj(ExecutionGroupProxy egProxy) {
        return Collections.list(egProxy.getSubFlows(null));

    }

    public List<MessageFlowProxy> allMFs(ExecutionGroupProxy egProxy) {
        this.egProxy = egProxy;
        List<MessageFlowProxy> list = allMFsIndepentPrj(egProxy);
        allApplications(this.egProxy)
                .forEach(proxy -> list.addAll(allMFs(proxy)));
        allRestAPIs(this.egProxy)
                .forEach(proxy -> list.addAll(allMFs(proxy)));
        return list;
    }

    public List<SubFlowProxy> allSubFlows(ExecutionGroupProxy egProxy) {
        this.egProxy = egProxy;
        List<SubFlowProxy> list = allSubFlowsIndependPrj(egProxy);
        allApplications(this.egProxy)
                .forEach(proxy -> list.addAll(allSubFlows(proxy)));
        allStaticLibs(this.egProxy)
                .forEach(proxy -> list.addAll(allSubFlows(proxy)));
        allSharedLibs(this.egProxy)
                .forEach(this::allSubFlows);
        allRestAPIs(this.egProxy)
                .forEach(proxy -> list.addAll(allSubFlows(proxy)));
//        MyUtils.write(list);
        return list;
    }

    public List<MessageFlowProxy> allMFs(BrokerProxy brokerProxy) {

        this.brokerProxy = brokerProxy;
        return this.allMFs = allMFs();
    }

    public List<MessageFlowProxy> allMFs() {
        List<MessageFlowProxy> list = new ArrayList<>();
        allEGs(this.brokerProxy)
                .forEach(proxy -> list.addAll(allMFs(proxy)));
        return this.allMFs = list;
    }

    public List<SubFlowProxy> allSubFlows() {
        List<SubFlowProxy> list = new ArrayList<>();
        allEGs(this.brokerProxy)
                .forEach(proxy -> list.addAll(allSubFlows(proxy)));
        return this.allSubFlows = list;
    }

    @SneakyThrows
    public List<RestApiProxy> allRestAPIs(ExecutionGroupProxy egProxy) {
        return Collections.list(egProxy.getRestApis(null));
    }

    @SneakyThrows
    public List<RestApiProxy> allRestAPIs() {
        ObjectUtils.argumentNotNull(brokerProxy, "Node is not connected. brokerProxy is null");
        allRestAPIs = new ArrayList<>();
        for (ExecutionGroupProxy egProxy : allEGs()) {
            allRestAPIs.addAll(Collections.list(egProxy.getRestApis(null)));
        }
        return allRestAPIs;
    }

    /*public static String getType(ConfigurationObjectType objectType) {

        String type;

        if (objectType == ConfigurationObjectType.application) {
            type = "Application";
        } else if (objectType == ConfigurationObjectType.staticLibrary) {
            type = "Static Libray";
        } else if (objectType == ConfigurationObjectType.sharedLibrary) {
            type = "Shared Libray";
        } else if (objectType == ConfigurationObjectType.messageflow) {
            type = "Message Flow";
        } else if (objectType == ConfigurationObjectType.subflow) {
            type = "Subflow";
        } else if (objectType == ConfigurationObjectType.restapi) {
            type = "RestAPI";
        } else {
            type = "Unknown";
        }

        return type;
    }*/

    public static String getParentDisplayName(MessageFlowProxy mf) {
        return mf.getConfigurationObjectTypeOfParent().getDisplayName();
    }

    public static String getParentDisplayName(SubFlowProxy sf) {
        return sf.getConfigurationObjectTypeOfParent().getDisplayName();
    }

    public static Optional<ApplicationProxy> getApplication(MessageFlowProxy mf)
            throws ConfigManagerProxyLoggedException,
            ConfigManagerProxyPropertyNotInitializedException {

//        ExecutionGroupProxy egproxy = mf.getExecutionGroup();
//        String parent = getParentDisplayName(mf);
        /*Optional<ApplicationProxy> app =
        if (parent.equalsIgnoreCase("Application")) {
            app = egproxy.getApplicationByName(mf.getParent().getName());

        }*/
        return Optional.ofNullable(mf.getExecutionGroup().getApplicationByName(mf.getParent().getName()));
    }

    public static Optional<LibraryProxy> getLibraryProxy(MessageFlowProxy mf)
            throws ConfigManagerProxyLoggedException,
            ConfigManagerProxyPropertyNotInitializedException {

        /*ExecutionGroupProxy egproxy = mf.getExecutionGroup();
        String parent = getParentDisplayName(mf);
        LibraryProxy lib = null;
        if (parent.equalsIgnoreCase("Library")) {
            lib = egproxy.getStaticLibraryByName(mf.getParent().getName());

        }*/
        return Optional.ofNullable(mf.getExecutionGroup().getStaticLibraryByName(mf.getParent().getName()));
    }

    public static boolean isFileNode(String type) {
        return StringUtils.equalsAnyIgnoreCase(type, "ComIbmFileInputNode",
                "ComIbmFileOutputNode", "ComIbmFileReadNode");
    }

    public static boolean isWSNode(String type) {
        return StringUtils.equalsAnyIgnoreCase(type, "ComIbmWSInputNode"
                , "ComIbmSOAPRequestNode"
                , "ComIbmWSRequestNode"
                , "ComIbmHTTPAsyncRequestNode"
                , "ComIbmSOAPInputNode"
                , "ComIbmSOAPAsyncRequestNode");
    }

    public static boolean isDSN(String type) {
        return StringUtils.equalsAnyIgnoreCase(type, "ComIbmComputeNode"
                , "ComIbmDatabaseInputNode"
                , "ComIbmFilterNode"
                , "ComIbmDatabaseNode");

    }

    public static boolean isMQNode(String type) {
        return StringUtils.equalsAnyIgnoreCase(type, "ComIbmMQInputNode"
                , "ComIbmMQOutputNode"
                , "ComIbmMQGetNode")
                ;

    }

    public static void setRuntimeProperty(ExecutionGroupProxy eg, String propertyName, String propertyValue) throws ConfigManagerProxyLoggedException {
        eg.setRuntimeProperty(propertyName, propertyValue);
    }

    public static void setHTTPListenerRuntimeProperty(BrokerProxy broker, String propertyName, String propertyValue) throws ConfigManagerProxyLoggedException {
        broker.setHTTPListenerProperty(propertyName, propertyValue);

    }

    public static String getNodeProtocol(MessageFlowProxy mf, String inout)
            throws ConfigManagerProxyPropertyNotInitializedException {

        Enumeration<MessageFlowProxy.Node> nodeUsed = mf.getNodes();
        String type;
        StringBuilder protocol = new StringBuilder();
        MessageFlowProxy.Node node;
        int index;
        int defaultNodeTypeLength = "ComIbm".length();
        while (nodeUsed.hasMoreElements()) {
            node = nodeUsed.nextElement();
            type = node.getType();

            if (type.contains(inout)) {
                index = type.indexOf(inout);
                // System.out.print(type+"\t"+index +"\t");
                if (index != -1) {
                    protocol.append(index > defaultNodeTypeLength ? type
                            .substring(defaultNodeTypeLength, index) : type
                            .substring(index));
                    protocol.append(",");
                }

            }/*
             * else if(type.contains("WS") ) { //|| type.contains("ReplyNode")
             * return "HTTP"; }else if(type.contains("SAP")) { return "SAP";
             * }else if(type.contains("Time")) { return "Timer"; }else
             * if(type.contains("SOAP")) { return "SOAP"; }
             */

        }
        return protocol.length() > 0 ? protocol.deleteCharAt(
                protocol.length() - 1).toString() : "Unknown"; // type != null ?
        // type.substring("ComIbm".length(),4)
        // :
    }


    public static String getDomainName(MessageFlowProxy mf)
            throws ConfigManagerProxyPropertyNotInitializedException {

        Enumeration<MessageFlowProxy.Node> nodeUsed = mf
                .getNodes();
        MessageFlowProxy.Node node;
        String domain;
        while (nodeUsed.hasMoreElements()) {
            node = nodeUsed.nextElement();
            if (node.getType().contains("Input")) {
                domain = node.getProperties().getProperty(
                        "messageDomainProperty");
                return domain.equals("") ? "BLOB" : domain;
            } else if (node.getType().contains("SOAPAsyncRes")) {
                return "SOAP";
            } else if (node.getType().contains("HTTPAsyncRes")) {
                return "HTTP";
            }
        }
        return null;
    }

    public static String getMessageType(MessageFlowProxy mf)
            throws ConfigManagerProxyPropertyNotInitializedException {

        String type = getDomainName(mf);

        if (type != null) {
            switch (type) {
                case "XML":
                case "XMLNS":
                case "XMLNSC":
                case "DataObject":
                    return "XML";
                case "DFDL":
                case "MRM":
                    return "Binary/Text";
                case "":
                    return "BLOB";
                default:
                    return type;
            }
        }
        return "Unknown";
    }

    public static boolean isInputNode(MessageFlowProxy.Node node) {
        return node.getType().contains("Input")
                || node.getType().contains("Timeout");
    }

    @SneakyThrows
    public static DeployResult undeploy(MessageFlowProxy mf) {
        ExecutionGroupProxy egproxy = mf.getExecutionGroup();
        String parent = getParentDisplayName(mf);

        if (parent.equalsIgnoreCase("Application")) {
            ApplicationProxy app = egproxy.getApplicationByName(mf.getParent()
                    .getName());

            System.out.println("Found in Application " + app.getName());

            return egproxy.deleteDeployedObjectsByName(
                    new String[]{app.getFullName()}, 3000);

        } else if (parent.equalsIgnoreCase("Library")) {
            LibraryProxy lib = egproxy.getStaticLibraryByName(mf.getParent()
                    .getName());

            return egproxy.deleteDeployedObjectsByName(
                    new String[]{lib.getFullName()}, 3000);
        } else if (parent.equalsIgnoreCase("SharedLibrary")) {
            LibraryProxy lib = egproxy.getSharedLibraryByName(mf.getParent()
                    .getName());

            return egproxy.deleteDeployedObjectsByName(
                    new String[]{lib.getFullName()}, 3000);
        } else if (parent.equalsIgnoreCase("REST API")) {
            RestApiProxy restApiProxy = egproxy.getRestApiByName(mf.getParent()
                    .getName());

            return egproxy.deleteDeployedObjectsByName(
                    new String[]{restApiProxy.getFullName()}, 3000);
        } else {

            DeployedObjectGroupProxy localDeployedObjectGroupProxy = (DeployedObjectGroupProxy) mf
                    .getParent();
            return localDeployedObjectGroupProxy.deleteDeployedObjectsByName(
                    new String[]{mf.getFullName()}, 3000);


        }


    }

    @SneakyThrows
    public MessageFlowProxy getMFlowProxyFromApp() {
        ObjectUtils.argumentNotNull(this.brokerProxy, "Broker Proxy cannot be null");
        return this.brokerProxy.getExecutionGroupByName(this.egName)
                .getApplicationByName(this.applicationName)
                .getMessageFlowByName(this.mfName)
                ;
    }

    @SneakyThrows
    public MessageFlowProxy getMFlowProxyFromStaticLib() {
        return this.brokerProxy.getExecutionGroupByName(this.egName)
                .getApplicationByName(this.applicationName)
                .getStaticLibraryByName(this.staticLibName)
                .getMessageFlowByName(this.mfName)
                ;
    }

    @SneakyThrows
    public MessageFlowProxy getMFlowProxyFromRestAPI() {
        return this.brokerProxy.getExecutionGroupByName(this.egName)
                .getRestApiByName(this.restAPIName)
                .getMessageFlowByName(this.mfName)
                ;
    }

    @SneakyThrows
    public SubFlowProxy getSFlowProxyFromApp() {
        return this.brokerProxy.getExecutionGroupByName(this.egName)
                .getApplicationByName(this.applicationName)
                .getSubFlowByName(this.subFlowName)
                ;
    }

    @SneakyThrows
    public SubFlowProxy getSFlowProxyFromStaticLib() {
        return this.brokerProxy.getExecutionGroupByName(this.egName)
//                .getApplicationByName(this.applicationName)
                .getStaticLibraryByName(this.staticLibName)
                .getSubFlowByName(this.subFlowName)
                ;
    }

    @SneakyThrows
    public SubFlowProxy getSFlowProxyFromSharedLib() {
        return this.brokerProxy.getExecutionGroupByName(this.egName)
                .getSharedLibraryByName(this.sharedLibName)
                .getSubFlowByName(this.subFlowName)
                ;
    }

    @SneakyThrows
    public SubFlowProxy getSFlowProxyFromRestAPI() {
        return this.brokerProxy.getExecutionGroupByName(this.egName)
                .getRestApiByName(this.restAPIName)
                .getSubFlowByName(this.subFlowName)
                ;
    }

    public void disconnect() {
        if (brokerProxy != null) {
            brokerProxy.disconnect();
        }
    }

    public void disconnect(BrokerProxy brokerProxy) {
        this.brokerProxy = brokerProxy;
        disconnect();
    }

    public static String getDependentFlows(
            DeployedObjectGroupProxy deployedObjectGroupProxy)
            throws ConfigManagerProxyException {

        Enumeration<DeployedObject> deployedObject = deployedObjectGroupProxy
                .getMessageFlowDependencies();
        StringBuilder dependecies = new StringBuilder();
        while (deployedObject.hasMoreElements()) {
            DeployedObject deployedObject2 = deployedObject
                    .nextElement();

            dependecies.append(deployedObject2.getFullName());
            dependecies.append("|");
        }

        return dependecies.toString();

    }

    public static Map<String, String> getSharedLibDependencies(
            Map<SharedLibraryReference, SharedLibraryProxy> sharedLibs) {

        Map<String, String> map = new LinkedHashMap<>();
        for (Map.Entry<SharedLibraryReference, SharedLibraryProxy> entry : sharedLibs
                .entrySet()) {
            map.put(entry.getKey().getLibraryName(), entry.getValue()
                    .getConfigurationObjectType().getDisplayName());
        }

        return map;
    }

    public static Map<String, String> getHTTPPortDetails(ExecutionGroupProxy egProxy) throws ConfigManagerProxyPropertyNotInitializedException, IllegalArgumentException, ConfigManagerProxyLoggedException {
        Map<String, String> map = new LinkedHashMap<>();
        map.put("EGLevel : HTTPConnector/port", egProxy.getRuntimeProperty("HTTPConnector/port"));
        map.put("EGLevel : HTTPSConnector/port", egProxy.getRuntimeProperty("HTTPSConnector/port"));
        BrokerProxy bp = (BrokerProxy) egProxy.getParent();
        map.put("Node Level : HTTPConnector/port", bp.getHTTPListenerProperty("HTTPConnector/port"));
        map.put("Node Level : HTTPSConnector/port", bp.getHTTPListenerProperty("HTTPSConnector/port"));

        return map;
    }

    public static String humanReadableByteCount(long v) {
        if (v < 1024)
            return v + " B";
        int z = (63 - Long.numberOfLeadingZeros(v)) / 10;
        return String.format("%.1f %sB", (double) v / (1L << (z * 10)),
                " KMGTPE".charAt(z));
    }

    @SneakyThrows
    public static LinkedHashMap<String, String> getRestAPIProperties(RestApiProxy restApi) {

        LinkedHashMap<String, String> map = new LinkedHashMap<>();

        Api api = restApi.getApi();

        map.put(formatKey("API Properties"), "");

        map.put("BasePath", api.getBasePath());
        map.put("BaseURL", api.getBaseURL());
        map.put("Description()", api.getDescription());
        map.put("FileName", api.getFileName());
        map.put("Version", api.getVersion());
        map.put("Host", api.getHost());
        map.put("Protocol", api.getProtocol());
        map.put("Port", String.valueOf(api.getPort()));
        map.put("Title", api.getTitle());

        int i = 1, j = 1;
        map.put(i + formatKey("DynamicContent Properties"), "");
        for (DynamicContent dynamicContent : api.getDynamicContent()) {
            map.put(i + "." + j++ + ". " + "Content", dynamicContent.getContent());
            map.put(i + "." + j++ + ". " + "ContentType", dynamicContent.getContentType());
            map.put(i + "." + j++ + ". " + "Path", dynamicContent.getPath());
        }
        i = 1;
        j = 1;
        map.put(i + formatKey("Model"), "");
        for (Model model : api.getModels()) {
            map.put(i + "." + j++ + ". " + "Name", model.getName());
//            map.put(i + "." + j++ +". " + "Schema Name", model.getSchema().getType().name());
        }

//        api.getModelReferences().;
        i = 1;
        j = 1;
        map.put(i + formatKey("Operations"), "");

        for (Operation operation : api.getOperations()) {

            map.put(i + "." + j++ + ". " + "Name", operation.getName());
            map.put(i + "." + j++ + ". " + "isOperationImplemented",
                    String.valueOf(restApi.isOperationImplemented(operation.getName())));
            map.put(i + "." + j++ + ". " + "Description", operation.getDescription());
            map.put(i + "." + j++ + ". " + "Summary", operation.getSummary());
            map.put(i + "." + j++ + ". " + "Method Name", operation.getMethod().name());
            map.put(i + "." + j++ + ". " + "Ordinal", String.valueOf(operation.getMethod().ordinal()));
            map.put(i + "." + j++ + ". " + "Class Name", operation.getMethod().getDeclaringClass().getName());

            if (operation.getRequest() != null) {
                map.put(i + "." + j++ + ". " + "Request Name", String.valueOf(operation.getRequest().getSchema()));
                map.put(i + "." + j++ + ". " + "Request Description", operation.getRequest().getDescription());
                map.put(i + "." + j++ + ". " + "Request ->IsRequired", String.valueOf(operation.getRequest().isRequired()));
            }

            map.put(i + "." + j++ + ". " + "Path", operation.getResource().getPath());

            int k = 1, l = 1;
            map.put(k++ + formatKey("Parameters"), "");
            for (Parameter parameter : operation.getParameters()) {

                map.put(k + "." + l++ + ". " + "Parameter Name", parameter.getName());
                map.put(k + "." + l++ + ". " + "Parameter Format", parameter.getFormat());
                map.put(k + "." + l++ + ". " + "Parameter Description", parameter.getDescription());
                map.put(k + "." + l++ + ". " + "Parameter Type", parameter.getType().name());
                map.put(k + "." + l++ + ". " + "Parameter DataType", parameter.getDataType().name());
                map.put(k + "." + l++ + ". " + "Parameter IsRequired", String.valueOf(parameter.isRequired()));
            }

            k = 1;
            l = 1;
            map.put(k++ + formatKey("Responses"), "");

            for (Response response : operation.getResponses()) {
                map.put(k + "." + l++ + ". " + "Response Description", response.getDescription());
                map.put(k + "." + l++ + ". " + "Response Status Code", String.valueOf(response.getStatusCode()));
            }

            k = 1;
            map.put(k++ + formatKey("SecurityRequirement"), "");

            for (List<SecurityRequirement> requirement : operation.getSecurityRequirements()) {
                k++;
                l = 1;
                for (SecurityRequirement securityRequirement : requirement) {
                    map.put(k + "." + l++ + ". " + "SecurityRequirement", securityRequirement.getName());
                }
            }
        }

        i = 1;
        j = 1;
        map.put(i + formatKey("SecurityScheme"), "");
        for (SecurityScheme scheme : api.getSecuritySchemes()) {
            map.put(i + "." + j++ + ". " + "SecurityScheme Name", scheme.getName());
            map.put(i + "." + j++ + ". " + "SecurityScheme Description", scheme.getDescription());
            map.put(i + "." + j++ + ". " + "SecurityScheme Schema Type", scheme.getType().name());
        }

        i = 1;
        j = 1;
        map.put(i + formatKey("StaticContent"), "");
        for (StaticContent staticContent : api.getStaticContent()) {
            map.put(i + "." + j++ + ". " + "StaticContent Name", staticContent.getContentType());
            map.put(i + "." + j++ + ". " + "StaticContent Path", staticContent.getPath());
            map.put(i + "." + j++ + ". " + "Static Content", new String(staticContent.getContent()));
        }

        map.put("FileExtension", restApi.getFileExtension());
        map.put("BARFileName", restApi.getBARFileName());
        map.put("RestAPI Name", restApi.getName());
        map.put("ServiceDescriptor", restApi.getServiceDescriptor());
        map.put("ServiceName", restApi.getServiceName());
        map.put("ServiceQueryURL", String.valueOf(restApi.getServiceQueryURL()));
        map.put("ServiceURL", String.valueOf(restApi.getServiceURL()));

        i = 1;
        j = 1;
        map.put(i + formatKey("ServiceOperations"), "");
        Enumeration<ServiceOperation> serviceOperations = restApi.getServiceOperations();
        if (serviceOperations != null) {
            while (serviceOperations.hasMoreElements()) {
                ServiceOperation serviceOperation = serviceOperations.nextElement();
                map.put(i + "." + j++ + ". " + serviceOperation.getName(), serviceOperation.getType());
            }
        }
        i = 1;
        j = 1;
        map.put(i + formatKey("Message Flows"), "");
        Enumeration<MessageFlowProxy> messageFlows = restApi.getMessageFlows(null);
        while (messageFlows.hasMoreElements()) {
            MessageFlowProxy mf = messageFlows.nextElement();
            map.put(i + "." + j++, mf.getName());
        }

        j = 1;
        i = 1;
        map.put(i + formatKey("Sub Flows"), "");
        Enumeration<SubFlowProxy> subFlows = restApi.getSubFlows(null);
        while (subFlows.hasMoreElements()) {
            SubFlowProxy mf = subFlows.nextElement();
            map.put(i + "." + j++, mf.getName());
        }

        map.put("DeployTime", restApi.getDeployTime().toString());
        map.put("ModifyTime", restApi.getModifyTime().toString());
       /* i = 1;
        for (String s : restApi.getErrorHandlerNames()) {
            map.put(String.valueOf(i), s);
        }*/

        i = 1;
        map.put(i + formatKey("Basic Properties"), "");

        restApi.getBasicProperties().forEach((key, value) -> map.put(String.valueOf(key), String.valueOf(value)));

        i = 1;

        map.put(i + formatKey("Advanced Properties"), "");

        restApi.getAdvancedProperties().forEach((key, value) -> map.put(String.valueOf(key), String.valueOf(value)));

        map.put(i + formatKey("Deployed Properties"), "");

        restApi.getDeployProperties().forEach((key, value) -> map.put(String.valueOf(key), String.valueOf(value)));

        map.put(i + formatKey("Queues Used"), "");
        j = 1;

        for (String q : restApi.getQueues()) {
            map.put(String.valueOf(++j), q);
        }

        map.put(i + formatKey("Properties"), "");

        restApi.getProperties().forEach((key, value) -> map.put(String.valueOf(key), String.valueOf(value)));
        /*map.put(i + formatKey("InterfaceFiles"), "");
        restApi.getInterfaceFiles().entrySet().forEach(entry -> {
            map.put(entry.getKey(), entry.getValue());
        });*/

        map.put(i + formatKey("Run Time Properties"), "");

        for (String s : restApi.getRuntimePropertyNames()) {
            map.put(s, restApi.getRuntimeProperty(s));
        }

        return map;

    }

    public static LinkedHashMap<String, String> getFlowProperties(MessageFlowProxy mf)
            throws IllegalArgumentException, ConfigManagerProxyException {

        LinkedHashMap<String, String> map = new LinkedHashMap<String, String>();
        ExecutionGroupProxy egProxy = mf.getExecutionGroup();

        String parentName = mf.getParent().getName();
        String parentType = mf.getConfigurationObjectTypeOfParent()
                .getDisplayName();
        map.put(formatKey("Flow Properties"), "");

        map.put("Flow name ", mf.getName());
        map.put("Flow type ", mf.getType());
        map.put("Input Protocol ", FlowUtils.getNodeProtocol(mf, "Input"));
        map.put("Message Type ", FlowUtils.getMessageType(mf));
        map.put("Output Protocol ", FlowUtils.getNodeProtocol(mf, "Output"));
        map.put("Parent Name ", parentName);
        map.put("Parent Type ", parentType);
        map.put("EG Name ", egProxy.getName());
        map.put("Node/Broker Name", egProxy.getParent().getName());
        map.put("Deployed Time ", mf.getDeployTime().toString());
        map.put("Modifled Time ", mf.getModifyTime().toString());
        map.put("Deployed From", mf.getBARFileName());

        Path barPath = Paths.get(mf.getBARFileName());

        map.put("Bar File Size ", Files.exists(barPath) ? humanReadableByteCount(FileUtils.sizeOf(barPath.toFile())) : "");

        map.put("Additional Instances",
                String.valueOf(mf.getAdditionalInstances()));
        map.put("InjectionMode status ", egProxy.getInjectionMode());
        map.put("TestRecordMode  status ", egProxy.getTestRecordMode());
        map.put("Deployed From", mf.getBARFileName());
        map.put("UUID", mf.getUUID());
        map.put("Last Deployment Status", mf.getLastCompletionCode().toString());
        map.put("NumberOfSubcomponents",
                String.valueOf(mf.getNumberOfSubcomponents()));

        map.putAll(getHTTPPortDetails(egProxy));

        map.put(formatKey("Flow Running Status"), "");

        map.put("Flow Running ", mf.isRunning() ? "YES" : "NO");
        ApplicationProxy app = egProxy.getApplicationByName(parentName);

        if (app != null) {
            map.put(parentType + ":" + parentName + " Running",
                    String.valueOf(app.isRunning()));
        }

        map.put("EG Running ",
                mf.getExecutionGroup().isRunning() ? "YES" : "NO");

        map.put(formatKey("WLM Properties"), "");
        map.put("MaximumRateMsgsPerSec ",
                String.valueOf(mf.getMaximumRateMsgsPerSec()));
        map.put("NotificationThresholdMsgsPerSec",
                String.valueOf(mf.getNotificationThresholdMsgsPerSec()));

        if (mf.getConfigurationObjectTypeOfParent() == ConfigurationObjectType.application) {

            ApplicationProxy applicationProxy = egProxy
                    .getApplicationByName(parentName);
            map.putAll(getSharedLibDependencies(applicationProxy
                    .getSharedLibraryDependencies()));

            map.put("Dependencies", getDependentFlows(applicationProxy));

        } else if (mf.getConfigurationObjectTypeOfParent() == ConfigurationObjectType.staticLibrary) {

            LibraryProxy libraryProxy = egProxy
                    .getStaticLibraryByName(parentName);
            map.put("Static Library", libraryProxy.getName());
            map.put("Dependencies", getDependentFlows(libraryProxy));
        }

        String[] userDefinedPropertyNames = mf.getUserDefinedPropertyNames();
        if (userDefinedPropertyNames.length > 0) {
            map.put(formatKey("User Defined Properties"), "");
            for (String userDefinedPropertyName : userDefinedPropertyNames) {
                map.put(userDefinedPropertyName, ((String) mf
                        .getUserDefinedProperty(userDefinedPropertyName)));
            }
        } else {
            map.put(formatKey("User Defined Properties Used "), "NIL");
        }

        String[] queues = mf.getQueues();

        if (queues.length > 0) {
            map.put(formatKey("Queues Used"), "");
            int i = 0;
            for (String q : queues) {
                map.put("Queue " + ++i, q);
            }
            // details.append(terminator);
        } else {
            map.put(formatKey("Queues Used"), "NIL");
        }

        Enumeration<com.ibm.broker.config.proxy.MessageFlowProxy.Node> nodeUsed = mf
                .getNodes();
        int i = 1, j;
        while (nodeUsed.hasMoreElements()) {
            MessageFlowProxy.Node node = nodeUsed
                    .nextElement();
            map.put(i + " ---------------------> Node Name : " + node.getName()
                    + "<---------------------", " Type : " + node.getType());
            Properties nodeProperties = node.getProperties();
            Enumeration<?> propertiesEnum = nodeProperties.keys();
            j = 1;
            while (propertiesEnum.hasMoreElements()) {
                String propertiesKey = "" + propertiesEnum.nextElement();
                String propertiesValue = nodeProperties
                        .getProperty(propertiesKey);
                map.put(i + "." + j++ + " : " + propertiesKey, propertiesValue);
            }

            i++;

            // if (node.getType().equalsIgnoreCase("SubFlowNode")) {
            // mf.gets
            // }
            // for (Map.Entry<?, ?> entry : prop.entrySet()) {
            // String value = (String) entry.getValue();
            // if (value.length() > 0) {
            // map.put(
            // String.valueOf(entry.getKey()),
            // String.valueOf(entry.getValue()));
            // }
            // }

        }

        map.put(" ---------------------> Runtime Property : <---------------------",
                " Value : ");

        j = 1;

        for (String runTime : mf.getRuntimePropertyNames()) {

            map.put(i + "." + j++ + " : " + runTime,
                    mf.getRuntimeProperty(runTime));
        }
        if (mf.getKeywords().length > 0) {
            map.put(formatKey("Keywords"), "");
            j = 1;
            i++;
            for (String key : mf.getKeywords()) {
                map.put(i + "." + j++ + " : " + key, mf.getKeywordValue(key));
            }
        }

        map.put(formatKey("Node Name"), formatKey("Node Type"));
        j = 1;
        i++;
        Enumeration<MessageFlowProxy.Node> nodesUsed = mf.getNodes();
        while (nodesUsed.hasMoreElements()) {
            MessageFlowProxy.Node node2 = nodesUsed
                    .nextElement();
            map.put(i + "." + j++ + " :" + node2.getName(), node2.getType());
        }

        map.put(formatKey("Node Connections"), "");
        j = 1;
        i++;
        Enumeration<MessageFlowProxy.NodeConnection> nodeConnections = mf
                .getNodeConnections();
        while (nodeConnections.hasMoreElements()) {
            MessageFlowProxy.NodeConnection nc = nodeConnections
                    .nextElement();
            if (nc != null) {
                map.put(i + "." + j++ + " : " + nc.getSourceNode() + "("
                                + nc.getSourceOutputTerminal() + ") --> ",
                        nc.getTargetNode() + "(" + nc.getTargetInputTerminal()
                                + ")");
            } else {
                map.put(i + "." + j++ + " : ", "");
            }
        }

        map.put(formatKey("Misc"), "");
        j = 1;
        i++;
        map.put(i + "." + j++ + " : " + "File Extension", mf.getFileExtension());
        map.put(i + "." + j++ + " : " + "Version", mf.getVersion());
        map.put(i + "." + j++ + " : " + "Wlm Policy", mf.getWlmPolicy());
        map.put(i + "." + j++ + " : " + "Start Mode", mf.getStartMode());
        map.put(i + "." + j++ + " : " + "ShortDesc", mf.getShortDescription());
        map.put(i + "." + j++ + " : " + "Long Desc", mf.getLongDescription());
        map.put(i + "." + j + " : " + "TestRecordMode",
                mf.getTestRecordMode());


        return map;

    }

    public static Properties discoverNodeConnections(MessageFlowProxy mf)
            throws ConfigManagerProxyPropertyNotInitializedException {

        Collections.list(mf.getNodeConnections())
                .stream()
                .filter(nodeConnection -> nodeConnection != null)
                .forEach(nodeConnection -> {
                    MyUtils.write(nodeConnection);
                });

        return null;

    }

    public static String formatKey(String key) {
        return " ---------------------> " + key + " : <---------------------";
    }

    public static Set<MessageFlowProxy.Node> getInputNodes(MessageFlowProxy mf)
            throws ConfigManagerProxyPropertyNotInitializedException {
        Enumeration<MessageFlowProxy.Node> nodes = mf.getNodes();
        Set<MessageFlowProxy.Node> inputNodes = new LinkedHashSet<>();

        while (nodes.hasMoreElements()) {
            MessageFlowProxy.Node node = (MessageFlowProxy.Node) nodes
                    .nextElement();

            if (FlowUtils.isInputNode(node)) {
                inputNodes.add(node);
            }
        }

        return inputNodes;
    }

    public static Vector<String> prepInputNodeEventSourceAddress(
            MessageFlowProxy.Node node) {
        Vector<String> v = new Vector<>();
        String name = node.getName();
        v.add(name + ".transaction.Start");
        v.add(name + ".transaction.End");
        v.add(name + ".transaction.Rollback");

        return v;
    }

    public static Map<String, String> getEventSourceAddress(FlowProxy mf,
                                                            MessageFlowProxy.Node node)
            throws ConfigManagerProxyPropertyNotInitializedException {
        Map<String, String> source = new LinkedHashMap<>();
        if (mf instanceof MessageFlowProxy) {
            for (String terminal : getTerminals(mf, node)) {
                source.put(node.getName() + ".terminal." + terminal, node.getType());
            }
        } else {
            source.putAll(getSubflowTerminals(mf,node));
        }

        return source;
    }

    public static Vector<String> getTerminals(FlowProxy mf,
                                              MessageFlowProxy.Node node)
            throws ConfigManagerProxyPropertyNotInitializedException {

        Vector<String> v = new Vector<String>();

        if (mf instanceof MessageFlowProxy) {
            Enumeration<MessageFlowProxy.NodeConnection> ncs = ((MessageFlowProxy) mf)
                    .getNodeConnections() ;

            while (ncs.hasMoreElements()) {
                MessageFlowProxy.NodeConnection n = (MessageFlowProxy.NodeConnection) ncs
                        .nextElement();

                if (node.equals(n.getSourceNode())) {
                    v.add(n.getSourceOutputTerminal());
                }

                if (node.equals(n.getTargetNode())) {
                    v.add(n.getTargetInputTerminal());
                }
            }
        }

        return v;

    }

    public static Map<String, String> getSubflowTerminals(FlowProxy mf, MessageFlowProxy.Node node) throws ConfigManagerProxyPropertyNotInitializedException {
        Enumeration<MessageFlowProxy.NodeConnection> ncs =  ((SubFlowProxy) mf)
                .getNodeConnections();

        Map<String, String> map = new LinkedHashMap<>();


        while (ncs.hasMoreElements()) {
            MessageFlowProxy.NodeConnection n = (MessageFlowProxy.NodeConnection) ncs
                    .nextElement();
//			if (n.getSourceNode().getType().equalsIgnoreCase("OutputNode") || n.getTargetNode()().getType().equalsIgnoreCase("InputNode")) {
//				continue;
//			}
            map.put(node+"."+n.getSourceNode()+".terminal." +n.getSourceOutputTerminal(), n.getSourceNode().getType());
            map.put(node+"."+n.getTargetNode()+".terminal." +n.getTargetInputTerminal(),n.getTargetNode().getType());
        }

        return map;
    }



}




