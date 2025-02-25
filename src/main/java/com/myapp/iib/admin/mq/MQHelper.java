package com.myapp.iib.admin.mq;

import com.holonplatform.core.property.PropertyBox;
import com.ibm.mq.*;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.headers.MQDataException;
import com.ibm.mq.headers.pcf.PCFMessage;
import com.ibm.mq.headers.pcf.PCFMessageAgent;
import com.myapp.iib.my.CommonUtils;
import com.myapp.iib.model.mq.DISPLAYQ;
import com.myapp.iib.model.mq.QueuesDetails;


import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.time.Instant;
import java.time.ZoneId;
import java.util.*;

public class MQHelper {

    private String hostName;
    private Integer port;
    private int qDepth;
    private String channelName;
//    private String queue;

    private MQQueueManager queueManager;

    private MQQueue mqQueue;
    private String msgSize;

    public MQHelper(String hostName, Integer port, String channelName) {
        this.hostName = hostName;
        this.port = port;
        this.channelName = channelName;
    }

    public MQQueueManager getQueueManager() {
        return queueManager;
    }

    public void setQueueManager(MQQueueManager queueManager) {
        this.queueManager = queueManager;
    }

    public int createQueue(String qName) throws MQException, MQDataException, IOException {
        connect();
        int compCode = MQExplorer.createQueue(this.queueManager, qName).getCompCode();
        disconnect();
        return compCode;
    }

    public List<PropertyBox> listQDetails(boolean system) throws MQDataException, IOException {

        PCFMessageAgent agent = new PCFMessageAgent(this.hostName, this.port, this.channelName);
        // Create the PCF message type for the inquire.
        PCFMessage pcfCmd = new PCFMessage(MQConstants.MQCMD_INQUIRE_Q);

        // Add the inquire rules.
        // Queue name = wildcard.
        if (system) {
            pcfCmd.addParameter(MQConstants.MQCA_Q_NAME, "SYSTEM.*");
        } else {
            pcfCmd.addParameter(MQConstants.MQCA_Q_NAME, "*");
        }

        // Queue type = ALL.
        pcfCmd.addParameter(MQConstants.MQIA_Q_TYPE, MQConstants.MQQT_ALL);

        return listQDetails(agent, pcfCmd,system);
    }

    public List<PropertyBox> listQDetails(PCFMessageAgent agent, PCFMessage pcfCmd, boolean system) throws IOException, MQDataException {

        // Execute the command. The returned object is an array of PCF / messages.
        PCFMessage[] pcfResponses = agent.send(pcfCmd);
        List<PropertyBox> propertyBoxList = new ArrayList<>();

        int type, i = 0;
        String qName;

        for (PCFMessage pcfMessage : pcfResponses) {

            qName = String.valueOf(pcfMessage
                    .getParameterValue(MQConstants.MQCA_Q_NAME)).trim();

            if (system) {
                if (!qName.startsWith("SYSTEM")) {
                    continue;
                }
            } else {
                if (qName.startsWith("SYSTEM")
                        || qName.startsWith("AMQ")) {
                    continue;
                }
            }


                PropertyBox pb = PropertyBox.create(QueuesDetails.PROPERTY_SET);
                pb.setValue(QueuesDetails.SNO, ++i);
                pb.setValue(QueuesDetails.Q_NAME, qName.trim());

                type = pcfMessage
                        .getIntParameterValue(MQConstants.MQIA_Q_TYPE);

                switch (type) {
                    case MQConstants.MQQT_ALIAS:
                        pb.setValue(QueuesDetails.Q_TYPE, "Alias");
                        pb.setValue(QueuesDetails.BASE_Q, pcfMessage.getStringParameterValue(CMQC.MQCA_BASE_OBJECT_NAME).trim());
                        break;
                    case MQConstants.MQQT_REMOTE:
                        pb.setValue(QueuesDetails.Q_TYPE, "Remote");
                        pb.setValue(QueuesDetails.BASE_Q, pcfMessage.getStringParameterValue(MQConstants.MQCA_REMOTE_Q_NAME).trim());
                        break;
                    default:
                        pb.setValue(QueuesDetails.Q_TYPE, MQExplorer.getQueueType(type));
                        pb.setValue(QueuesDetails.BASE_Q, "NA");
                        break;
                }

//
                pb.setValue(QueuesDetails.CLUSTER_NAME, String.valueOf(pcfMessage
                        .getParameterValue(MQConstants.MQCA_CLUSTER_NAME)).trim());
                pb.setValue(QueuesDetails.CREATION_DATE, String.valueOf(pcfMessage
                        .getParameterValue(MQConstants.MQCA_CREATION_DATE)).trim());
//
                pb.setValue(QueuesDetails.CURRENT_Q_DEPTH, String.valueOf(pcfMessage
                        .getParameterValue(MQConstants.MQIA_CURRENT_Q_DEPTH)).trim());
//
                pb.setValue(QueuesDetails.OPEN_INPUT_COUNT, String.valueOf(pcfMessage
                        .getParameterValue(MQConstants.MQIA_OPEN_INPUT_COUNT)).trim());
                pb.setValue(QueuesDetails.OPEN_OUTPUT_COUNT, String.valueOf(pcfMessage
                        .getParameterValue(MQConstants.MQIA_OPEN_OUTPUT_COUNT)).trim());
                pb.setValue(QueuesDetails.MAX_Q_DEPTH, String.valueOf(pcfMessage
                        .getParameterValue(MQConstants.MQIA_MAX_Q_DEPTH)).trim());

                Optional<Object> msgLength = Optional.ofNullable(pcfMessage.getParameterValue(MQConstants.MQIA_MAX_MSG_LENGTH));

                pb.setValue(QueuesDetails.MAX_MSG_LENGTH, msgLength.isPresent() ? CommonUtils.humanReadableByteCount(Long.valueOf(String.valueOf(msgLength.get()))) : "");
                pb.setValue(QueuesDetails.INHIBIT_PUT, MQExplorer.getInhibitName((int) pcfMessage
                        .getParameterValue(MQConstants.MQIA_INHIBIT_PUT)));

                Optional.ofNullable(pcfMessage.getParameterValue(MQConstants.MQIA_INHIBIT_GET)).ifPresent(c -> {
                    pb.setValue(QueuesDetails.INHIBIT_GET, MQExplorer.getInhibitName((int) c));
                });

                propertyBoxList.add(pb);
        }
        agent.disconnect();
        return propertyBoxList;

    }


    public List<PropertyBox> displayQueue(String qName, int maxMsgs) throws MQException, IOException, MQDataException {
        int openOptions = MQConstants.MQOO_INQUIRE | MQConstants.MQOO_BROWSE | MQConstants.MQOO_INPUT_AS_Q_DEF;

        connect();
        mqQueue = this.queueManager.accessQueue(qName, openOptions);

        List<PropertyBox> propertyBoxes = new ArrayList<>();

        if (mqQueue.getCurrentDepth() > 0) {

            /******************************************************/
            /* Set up our options to browse for the first message */
            /******************************************************/
            MQGetMessageOptions gmo = new MQGetMessageOptions();
            gmo.options = MQConstants.MQGMO_WAIT | MQConstants.MQGMO_BROWSE_FIRST;
            MQMessage myMessage = new MQMessage();
            int length, i = 0;
            while (true) {
                try {
                    /*****************************************/
                    /* Reset the message and IDs to be empty */
                    /*****************************************/
                    myMessage.clearMessage();
                    myMessage.correlationId = MQConstants.MQCI_NONE;
                    myMessage.messageId = MQConstants.MQMI_NONE;

                    /**************************************************/
                    /* Browse the message, display it, and ask if the */
                    /* message should actually be gotten              */
                    /**************************************************/

                    mqQueue.get(myMessage, gmo);
                    length = myMessage.getMessageLength();

                    propertyBoxes.add(PropertyBox.builder(DISPLAYQ.PROPERTIES)
                            .set(DISPLAYQ.SNO, ++i)
                            .set(DISPLAYQ.MSGSIZE, CommonUtils.humanReadableByteCount(length))
                            .set(DISPLAYQ.MSGFMT, myMessage.format)
                            .set(DISPLAYQ.USERID, myMessage.userId)
                            .set(DISPLAYQ.PUTTIME,
                                    myMessage.putDateTime != null ?
                                    Instant.ofEpochMilli(myMessage.putDateTime.getTimeInMillis()).atZone(ZoneId.systemDefault()).toLocalDateTime() : null)
                            .set(DISPLAYQ.PUT_APPLN, myMessage.putApplicationName)
                            .set(DISPLAYQ.MSG_ID, CommonUtils.bytesToHex(myMessage.messageId))
                            .set(DISPLAYQ.CORRL_ID, CommonUtils.bytesToHex(myMessage.correlationId))

                            .build());


                    if (i == maxMsgs) {
                        break;
                    }

                    /************************************************/
                    /* Reset the options to browse the next message */
                    /************************************************/
                    gmo.options = MQConstants.MQGMO_WAIT | MQConstants.MQGMO_BROWSE_NEXT;
                } catch (MQException e) {
                    if (e.completionCode == 2 && e.reasonCode == MQConstants.MQRC_NO_MSG_AVAILABLE) {
                    }
                    break;
                }
            }
        }

        closeQueue();
        disconnect();

        return propertyBoxes;
    }

    public void connect() throws MQException, MQDataException {

        if (this.queueManager != null && this.queueManager.isConnected()) {
            this.queueManager.disconnect();
        }

        PCFMessageAgent agent = new PCFMessageAgent(this.hostName, this.port, this.channelName);

        Hashtable<String, Object> mqht = new Hashtable<String, Object>();

        mqht.put(MQConstants.CHANNEL_PROPERTY, this.channelName);
        mqht.put(MQConstants.HOST_NAME_PROPERTY, this.hostName);
        mqht.put(MQConstants.PORT_PROPERTY, this.port);
        mqht.put(MQConstants.TRANSPORT_PROPERTY, MQConstants.TRANSPORT_MQSERIES_CLIENT);

        this.queueManager = new MQQueueManager(agent.getQManagerName(), mqht);
        agent.disconnect();
    }

    public void disconnect() throws MQException {
        if (this.queueManager != null) {
            this.queueManager.close();
            this.queueManager.disconnect();
        }
    }

    public void closeQueue() throws MQException {
        if (this.mqQueue.isOpen()) {
            this.mqQueue.close();
        }
    }

    public MQMessage readQ(String queue) throws MQException, MQDataException {

        int openOptions = MQConstants.MQOO_INQUIRE
                + MQConstants.MQOO_FAIL_IF_QUIESCING
                + MQConstants.MQOO_INPUT_SHARED;

        connect();

        this.mqQueue = this.queueManager.accessQueue(queue, openOptions);

        MQMessage message = new MQMessage();

        if (this.mqQueue.getCurrentDepth() > 0) {

            MQGetMessageOptions getOptions = new MQGetMessageOptions();
            getOptions.options = MQConstants.MQGMO_NO_WAIT
                    + MQConstants.MQGMO_FAIL_IF_QUIESCING;
            this.mqQueue.get(message, getOptions);
            this.qDepth = this.mqQueue.getCurrentDepth();
        }
        closeQueue();
        disconnect();

        return message;

    }

    public void writeQ(String queue, String text, int n) throws MQException, MQDataException, IOException {

        connect();
        for (int i = 0; i < n; i++) {
            this.qDepth = MQExplorer.writeQ(this.queueManager, queue, text.getBytes(StandardCharsets.UTF_8));
        }
        disconnect();
    }

    private void resetMQMessage(MQMessage sendMsg) throws IOException {
        sendMsg.clearMessage();
        sendMsg.messageId = MQConstants.MQMI_NONE;
        sendMsg.correlationId = MQConstants.MQCI_NONE;
        sendMsg.characterSet = 1208;
        sendMsg.encoding = MQConstants.MQENC_NATIVE;
        sendMsg.format = MQConstants.MQFMT_STRING;
        sendMsg.putDateTime = new GregorianCalendar(Locale.US);
    }

    public int getqDepth() {
        return qDepth;
    }

    public int getqDepth(String qName) throws MQException, MQDataException {
        connect();
        int openOptions = MQConstants.MQOO_INQUIRE | MQConstants.MQOO_BROWSE | MQConstants.MQOO_INPUT_AS_Q_DEF;
        this.mqQueue = this.queueManager.accessQueue(qName, openOptions);
        qDepth = mqQueue.getCurrentDepth();
        closeQueue();
        disconnect();
        return qDepth;
    }

    public String convertMQMessage(MQMessage message) throws IOException {

        byte[] b = new byte[message.getMessageLength()];
        message.readFully(b);
        this.msgSize = CommonUtils.humanReadableByteCount(b.length);
        return new String(b, StandardCharsets.UTF_8);

    }

    public Map<String, String> queueProperties(String queue) throws MQException, MQDataException {

        connect();

        this.mqQueue = this.queueManager.accessQueue(queue, MQConstants.MQOO_INQUIRE
                | MQConstants.MQOO_BROWSE);
        Map<String, String> map = new LinkedHashMap<>();
        map.put("Queue Name", mqQueue.getName());
        map.put("Queue Type", MQExplorer.getQueueType(mqQueue.getQueueType()));
        map.put("Open Input Count", String.valueOf(mqQueue.getOpenInputCount()));
        map.put("Open Output Count", String.valueOf(mqQueue.getOpenOutputCount()));
        map.put("Current QDepth", String.valueOf(mqQueue.getCurrentDepth()));
        map.put("Inhibit Get", MQExplorer.getInhibitName(mqQueue.getInhibitGet()));
        map.put("Inhibit Put", MQExplorer.getInhibitName(mqQueue.getInhibitPut()));
        map.put("MaximumDepth", String.valueOf(mqQueue.getMaximumDepth()));
        map.put("MaximumMessageLength", CommonUtils.humanReadableByteCount(mqQueue
                .getMaximumMessageLength()));

        closeQueue();
        disconnect();
        return map;
    }

//    public MQQueueManager getQueueManager() {
//        return queueManager;
//    }


    public String getMsgSize() {
        return msgSize;
    }


}