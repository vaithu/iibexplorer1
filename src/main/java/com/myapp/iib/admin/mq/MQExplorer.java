package com.myapp.iib.admin.mq;

import com.ibm.mq.*;
import com.ibm.mq.constants.CMQC;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.headers.*;
import com.ibm.mq.headers.pcf.*;
import com.myapp.iib.my.CommonUtils;
import org.apache.commons.collections4.MultiValuedMap;
import org.apache.commons.collections4.multimap.ArrayListValuedHashMap;

import java.io.IOException;
import java.io.ObjectOutputStream;
import java.lang.reflect.Field;
import java.lang.reflect.Modifier;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.sql.Date;
import java.util.*;

public class MQExplorer {

//	private static final int

    public static PCFMessageAgent getPcfMessageAgent(String qMgrInfo[])
            throws NumberFormatException, MQException, MQDataException {
        PCFMessageAgent agent = null;
        if (qMgrInfo.length == 1) {
            // Local queue manager connection (queue manager name).

            agent = new PCFMessageAgent(qMgrInfo[0].trim());
        } else {
            // Client connection (host, port, channel).

            agent = new PCFMessageAgent(qMgrInfo[0].trim(),
                    Integer.parseInt(qMgrInfo[1]), qMgrInfo[2].trim());
        }

        return agent;
    }

    public static MQGetMessageOptions getMqGetMessageOptions() {
        MQGetMessageOptions getMessageOptions = new MQGetMessageOptions();
		/*getMessageOptions.options = MQConstants.MQGMO_BROWSE_FIRST
				| MQConstants.MQGMO_WAIT;*/
        getMessageOptions.matchOptions = MQConstants.MQMO_NONE;
        getMessageOptions.waitInterval = 1000;

        return getMessageOptions;
    }

    public static void closeQMgr(MQQueueManager qm) throws MQException {

        if (Optional.ofNullable(qm).isPresent()) {
            qm.close();
            qm.disconnect();
        }
    }

    /**
     * Connect, open queue, write a message, close queue and disconnect.
     *
     * @throws MQException
     * @throws IOException
     */
    public static void writeQ(MQQueueManager queueManager, String queueName, String filename, boolean file) throws MQException, IOException {

        // MQOO_OUTPUT = Open the queue to put messages. The queue is opened for
        // use with subsequent MQPUT calls.
        // MQOO_INPUT_AS_Q_DEF = Open the queue to get messages using the
        // queue-defined default.
        // The queue is opened for use with subsequent MQGET calls. The type of
        // access is either
        // shared or exclusive, depending on the value of the DefInputOpenOption
        // queue attribute.
        int openOptions = MQConstants.MQOO_OUTPUT
                | MQConstants.MQOO_INPUT_AS_Q_DEF
                | MQConstants.MQOO_FAIL_IF_QUIESCING | MQConstants.MQOO_INQUIRE;
        ;
        // specify the message options...
        MQPutMessageOptions pmo = new MQPutMessageOptions(); // default
        // MQPMO_ASYNC_RESPONSE = The MQPMO_ASYNC_RESPONSE option requests that
        // an MQPUT or MQPUT1 operation
        // is completed without the application waiting for the queue manager to
        // complete the call.
        // Using this option can improve messaging performance, particularly for
        // applications using client bindings.
        pmo.options = MQConstants.MQPMO_ASYNC_RESPONSE;
        MQQueue queue = null;
        MQMessage sendMsg = null;

        try {

            queue = queueManager.accessQueue(queueName, openOptions);

            sendMsg = new MQMessage();
            sendMsg.messageId = MQConstants.MQMI_NONE;
            sendMsg.correlationId = MQConstants.MQMI_NONE;
            sendMsg.characterSet = 1208;
            sendMsg.encoding = MQConstants.MQENC_NATIVE;
            sendMsg.format = MQConstants.MQFMT_STRING;
            sendMsg.putDateTime = new GregorianCalendar(Locale.US);
            if (file) {
                sendMsg.write(Files.readAllBytes(Paths.get(filename)));
            } else {
                sendMsg.writeString(filename);
            }
            // put the message on the queue
            queue.put(sendMsg, pmo);
        } catch (MQException e) {
            // e.printStackTrace();
            throw e;
        } catch (IOException e) {
            throw e;
        } finally {
            closeQueue(queue);
        }
    }


    public static List<byte[]> getAllMessagesFromQueue(MQQueueManager queueManager, String queueName) throws IOException, MQException {
        // TODO Auto-generated method stub

        int depth = 0;
        MQQueue queue = null;
        List<byte[]> messages = null;
        int openOptions = MQConstants.MQOO_INQUIRE
                | MQConstants.MQOO_BROWSE | MQConstants.MQOO_INPUT_AS_Q_DEF;


        queue = MQExplorer
                .getQueue(queueManager, queueName, openOptions);

        depth = queue.getCurrentDepth();
        int length;

        if (depth > 0) {

            messages = new ArrayList<byte[]>();

            /******************************************************/
            /* Set up our options to browse for the first message */
            /******************************************************/
            MQGetMessageOptions gmo = new MQGetMessageOptions();
            gmo.options = MQConstants.MQGMO_WAIT
                    | MQConstants.MQGMO_BROWSE_FIRST;
            MQMessage myMessage = new MQMessage();
            boolean loop = true;
            while (loop) {
                try {
                    /*****************************************/
                    /* Reset the message and IDs to be empty */
                    /*****************************************/
                    myMessage.clearMessage();
                    myMessage.correlationId = MQConstants.MQCI_NONE;
                    myMessage.messageId = MQConstants.MQMI_NONE;

                    /**************************************************/
                    /* Browse the message, display it, and ask if the */
                    /* message should actually be gotten */
                    /**************************************************/

                    queue.get(myMessage, gmo);
                    length = myMessage.getMessageLength();
                    byte[] b = new byte[length];
                    myMessage.readFully(b);
                    messages.add(b);
                    // System.out.println(new String(b));

                    // gmo.options = MQConstants.MQGMO_MSG_UNDER_CURSOR;
                    // queue.get(myMessage, gmo);

                    /************************************************/
                    /* Reset the options to browse the next message */
                    /************************************************/
                    gmo.options = MQConstants.MQGMO_WAIT
                            | MQConstants.MQGMO_BROWSE_NEXT;

                } catch (MQException e) {
                    if (e.completionCode == 1
                            && e.reasonCode == MQConstants.MQRC_TRUNCATED_MSG_ACCEPTED) {
                        // Just what we expected!!
                    } else {
                        loop = false;
                        if (e.completionCode == 2
                                && e.reasonCode == MQConstants.MQRC_NO_MSG_AVAILABLE) {
                            // Good, we are now done - no error!!
                        } else {
                            e.printStackTrace();
                        }
                    }
                }

            }
        }


        return messages;
    }


    public static PCFMessage[] getQueueStatusHandle(String[] qMgr, String queue) throws IOException, NumberFormatException, MQException, MQDataException {

        PCFMessage request = new PCFMessage(MQConstants.MQCMD_INQUIRE_Q_STATUS);
        request.addParameter(MQConstants.MQCA_Q_NAME, queue);
//		request.addParameter(MQConstants.MQIACF_Q_STATUS_TYPE, MQConstants.MQIACF_Q_STATUS);
        // We want Q HANDLE attributes
        request.addParameter(MQConstants.MQIACF_Q_STATUS_TYPE, MQConstants.MQIACF_Q_HANDLE);
//		request.addParameter(MQConstants.MQIACF_Q_STATUS_ATTRS, MQConstants.MQCACH_CONNECTION_NAME);
//		request.addParameter(MQConstants.MQIACF_Q_STATUS_ATTRS, new int[] {
//		                  MQConstants.MQIA_CURRENT_Q_DEPTH,
//		                  MQConstants.MQCACF_LAST_PUT_DATE, MQConstants.MQCACF_LAST_PUT_TIME,
//		                  MQConstants.MQIA_OPEN_INPUT_COUNT,
//		                  MQConstants.MQIA_OPEN_OUTPUT_COUNT
//		                  });
        PCFMessageAgent agent = getPcfMessageAgent(qMgr);
        PCFMessage[] msgs = agent.send(request);
        agent.disconnect();
        return msgs;

    }


    public static PCFMessage[] getQueueStatus(String[] qMgr, String queue) throws MQException, IOException, MQDataException {

        PCFMessage request = new PCFMessage(MQConstants.MQCMD_INQUIRE_Q_STATUS);
        request.addParameter(MQConstants.MQCA_Q_NAME, queue);
        request.addParameter(MQConstants.MQIACF_Q_STATUS_TYPE, MQConstants.MQIACF_Q_STATUS);
        // We want Q HANDLE attributes
//		request.addParameter(MQConstants.MQIACF_Q_STATUS_TYPE, MQConstants.MQIACF_Q_HANDLE);
//		request.addParameter(MQConstants.MQIACF_Q_STATUS_ATTRS, MQConstants.MQCACH_CONNECTION_NAME);
//		request.addParameter(MQConstants.MQIACF_Q_STATUS_ATTRS, new int[] {
//		                  MQConstants.MQIA_CURRENT_Q_DEPTH,
//		                  MQConstants.MQCACF_LAST_PUT_DATE, MQConstants.MQCACF_LAST_PUT_TIME,
//		                  MQConstants.MQIA_OPEN_INPUT_COUNT,
//		                  MQConstants.MQIA_OPEN_OUTPUT_COUNT
//		                  });
        PCFMessageAgent agent = getPcfMessageAgent(qMgr);
        PCFMessage[] msgs = agent.send(request);
        agent.disconnect();
        return msgs;

    }

    public static int openPurgeOptions() {
        return MQConstants.MQOO_INQUIRE
                + MQConstants.MQOO_FAIL_IF_QUIESCING
                + MQConstants.MQOO_INPUT_SHARED;

    }

    public static int mqOpenOptions() {
        return MQConstants.MQOO_INQUIRE | MQConstants.MQOO_INPUT_AS_Q_DEF;
    }

    public static void writeMessageToStream(MQMessage message, ObjectOutputStream stream) throws IOException {

        for (Field field : message.getClass().getFields()) {
            if (Modifier.isPublic(field.getModifiers()) && !Modifier.isStatic(field.getModifiers()) && field.getName() != "unmappableAction") {
                try {
                    Object value = field.get(message);
                    stream.writeObject(value);
                } catch (IllegalArgumentException | IllegalAccessException | IOException ex) {
                    continue;
                }
            }
        }
        message.seek(0);
        stream.writeInt(message.getMessageLength());
        byte buff[] = new byte[message.getMessageLength()];
        message.readFully(buff);
        stream.write(buff);
    }




    public static List<String> listQueues(String[] qMgr) throws NumberFormatException, MQException, IOException, MQDataException {

        PCFMessage request = new PCFMessage(MQConstants.MQCMD_INQUIRE_Q);
        request.addParameter(MQConstants.MQCA_Q_NAME, "*");
        PCFMessageAgent agent = getPcfMessageAgent(qMgr);

        List<String> list = new ArrayList<>();
        String qName;
        PCFMessage[] pcfMessages = agent.send(request);

        for (PCFMessage response : pcfMessages) {
            qName = response.getStringParameterValue(MQConstants.MQCA_Q_NAME);
            if (qName.startsWith("SYSTEM") || qName.startsWith("AMQ")) {
                continue;
            }
            list.add(qName.trim());
        }
        Collections.sort(list);
        agent.disconnect();
        return list;
    }


    private static PCFMessageAgent getPCFMessageAgent(
            MQQueueManager queueManager) throws MQDataException, MQException {
        PCFMessageAgent agent = new PCFMessageAgent();
        MQQueue defaultModelQueue = null;

        defaultModelQueue = queueManager.accessQueue(
                "SYSTEM.DEFAULT.MODEL.QUEUE", MQConstants.MQOO_INQUIRE);
        if (defaultModelQueue != null) {
            if (!defaultModelQueue.isOpen()
                    || defaultModelQueue.getInhibitGet() == 1
                    || defaultModelQueue.getInhibitPut() == 1) {
                setTOMQExplorerReplyQueue(agent);
            }
        }
        if (defaultModelQueue != null) {
            defaultModelQueue.close();
        }
        agent.connect(queueManager);
        return agent;
    }

    private static void setTOMQExplorerReplyQueue(PCFMessageAgent agent) {
        agent.setModelQueueName("SYSTEM.MQEXPLORER.REPLY.MODEL");
        agent.setReplyQueuePrefix("AMQ.MQEXPLORER.");
    }

    public static PCFMessage createQueue(MQQueueManager queueManager, String qName) throws IOException, MQDataException, MQException {
        PCFMessageAgent agent = null;
        agent = getPCFMessageAgent(queueManager);
        PCFMessage command = new PCFMessage(MQConstants.MQCMD_CREATE_Q);
        command.addParameter(PCFConstants.MQCA_Q_NAME, qName);
        command.addParameter(PCFConstants.MQIA_Q_TYPE, PCFConstants.MQQT_LOCAL);
        command.addParameter(PCFConstants.MQCA_Q_DESC, qName);
        command.addParameter(PCFConstants.MQIA_DEF_PERSISTENCE, PCFConstants.MQPER_PERSISTENT);
        PCFMessage[] pcfResponse = agent.send(command);
        return pcfResponse[0];
    }

    public static String getQMgrName(String[] qMgrDetails) throws MQException, MQDataException {
        String qMgr = null;
//		for (String string : qMgrDetails) {
//			System.out.println(string);
//		}
        PCFMessageAgent agent = MQExplorer.getPcfMessageAgent(qMgrDetails);
//		System.out.println(agent.getQManagerName());
        qMgr = agent.getQManagerName();
//		MQQueueManager mqQueueManager = new MQQueueManager(qMgr);
//		System.out.println(mqQueueManager.getName());
//		System.out.println(new MQQueueManager(qMgr).getName());
        agent.disconnect();
//		System.out.println(qMgr);
        return qMgr;
    }


    public static String getBaseQ(String aliasQ, MQQueueManager mqQueueManager) throws MQException {
        int openOptions = MQConstants.MQOO_INQUIRE | MQConstants.MQOO_BROWSE | MQConstants.MQOO_INPUT_SHARED;

        MQQueue mqQueue = mqQueueManager.accessQueue(aliasQ, openOptions);
        int[] selectors = new int[1];
        int[] intAttrs = new int[1];
        byte[] charAttrs = new byte[64];
        selectors[0] = MQConstants.MQCA_BASE_OBJECT_NAME;
        mqQueue.inquire(selectors, intAttrs, charAttrs);
        mqQueue.close();
        return new String(charAttrs).trim();
    }


	/*public static String displayQ(String qMgr,String queueName) throws MQException, IOException {


		MQQueueManager queueManager = new MQQueueManager(qMgr);
		MQQueue queue = queueManager.accessQueue(queueName, MQConstants.MQOO_INQUIRE | MQConstants.MQOO_BROWSE | MQConstants.MQOO_INPUT_SHARED); //| MQConstants.MQOO_INPUT_SHARED | MQConstants.MQOO_OUTPUT
		MQGetMessageOptions options = new MQGetMessageOptions();
        MQMessage message = new MQMessage();
        options.matchOptions = MQConstants.MQMO_NONE;
        options.options = MQConstants.MQGMO_BROWSE_NEXT;
        String messageId = null;
        StringBuilder sb = new StringBuilder();
        for (int i = 0; i < queue.getCurrentDepth(); i++) {
        	queue.get(message, options);
        	messageId = CommonUtils.bytesToHexString(message.messageId);
//			sb.append(BuildHTMLGUI.tableRowData(i+1,CommonUtils.humanReadableByteCount(message.getDataLength()),message.userId,message.putDateTime.getTime(),
//					CommonUtils.getBrowseMQHref(queueName, messageId, Prop.httpPortNo),message.putApplicationName,
//					messageId,CommonUtils.bytesToHexString(message.correlationId)
//					));
//        	System.out.println(new Date(messages.get(i).putDateTime.getTimeInMillis()));
		}



        queue.close();
        queueManager.close();
        queueManager.disconnect();

		return "";
//				BuildHTMLGUI.table(TableType.Responsive, Prop.displayQ, sb.toString());


//
//        for (MQMessage mqMessage : messages) {
//			System.out.println(mqMessage.putApplicationName);
//		}





//		PCFMessageAgent agent = null;
//		// Prepare PCF command to inquire queue status (status type)
//		PCFMessage request = new PCFMessage(MQConstants.MQCMD_INQUIRE_Q_STATUS);
//				request.addParameter(MQConstants.MQCA_Q_NAME, queueName);
//				request.addParameter(MQConstants.MQIACF_Q_STATUS_TYPE, MQConstants.MQIACF_Q_STATUS);
//				request.addParameter(MQConstants.MQIACF_Q_STATUS_ATTRS, new int[] {
//				                  MQConstants.MQCA_Q_NAME, MQConstants.MQIA_CURRENT_Q_DEPTH,
//				                  MQConstants.MQCACF_LAST_GET_DATE, MQConstants.MQCACF_LAST_GET_TIME,
//				                  MQConstants.MQCACF_LAST_PUT_DATE, MQConstants.MQCACF_LAST_PUT_TIME,
//				                  MQConstants.MQIACF_OLDEST_MSG_AGE, MQConstants.MQIA_OPEN_INPUT_COUNT,
//				                  MQConstants.MQIA_OPEN_OUTPUT_COUNT, MQConstants.MQIACF_UNCOMMITTED_MSGS });
//
//		agent = new PCFMessageAgent(qMgr);
//
//		PCFMessage[] pcfResponse = agent.send(request);
//
//		for (PCFMessage response : pcfResponse) {
//
//			System.out.println(response.getp);
//
//		}

	}*/


    public static List<List<Object>> listQueues(String qMgr, String filter)
            throws NumberFormatException, MQException, IOException, MQDataException {

        List<List<Object>> listOfLists = new ArrayList<List<Object>>();
        PCFMessageAgent agent = null;
        PCFMessage request = new PCFMessage(MQConstants.MQCMD_INQUIRE_Q);

        agent = new PCFMessageAgent(qMgr);
        switch (filter.toUpperCase()) {
            case "SYSTEM":
                request.addParameter(MQConstants.MQCA_Q_NAME, "SYSTEM.*");
                break;

            default:
                request.addParameter(MQConstants.MQCA_Q_NAME, "*");
                break;
        }
        // inquireQueueStatus.addParameter (MQConstants.MQIA_Q_TYPE,
        // MQConstants.MQQT_ALL);
        // request.addFilterParameter (MQConstants.MQIA_CURRENT_Q_DEPTH,
        // MQConstants.MQCFOP_GREATER, 0);

        PCFMessage[] pcfResponse = agent.send(request);
        // PCFMessage[] status = null;

        List<Object> row = new ArrayList<>();

        Object temp = null;
        for (PCFMessage response : pcfResponse) {

            row.add(response.getStringParameterValue(MQConstants.MQCA_Q_NAME));
            row.add(getQueueType((int) response
                    .getParameterValue(MQConstants.MQIA_Q_TYPE)));
            row.add(response.getParameterValue(MQConstants.MQIA_OPEN_INPUT_COUNT));
            row.add(response.getParameterValue(MQConstants.MQIA_OPEN_OUTPUT_COUNT));
            row.add(response.getParameterValue(MQConstants.MQIA_CURRENT_Q_DEPTH));
            // System.out.println(response);
            temp = response.getParameterValue(MQConstants.MQIA_DEF_PERSISTENCE);
            if (temp == null) {
                row.add("null");
            } else if (0 == (int) temp) {
                row.add("Non-Persistent");
            } else {
                row.add("Persistent");
            }
            row.add(response.getParameterValue(MQConstants.MQIA_DEF_PERSISTENCE));
            temp = response.getParameterValue(MQConstants.MQIA_INHIBIT_GET);
            if (temp == null) {
                row.add("null");
            } else if (0 == (int) temp) {
                row.add("Allowed");
            } else {
                row.add("Inhibited");
            }
            temp = response.getParameterValue(MQConstants.MQIA_INHIBIT_PUT);
            if (temp == null) {
                row.add("null");
            } else if (0 == (int) temp) {
                row.add("Allowed");
            } else {
                row.add("Inhibited");
            }

            row.add(response.getParameterValue(MQConstants.MQIA_BACKOUT_THRESHOLD));
            row.add(response.getParameterValue(MQConstants.MQIA_MAX_Q_DEPTH));
            row.add(response.getParameterValue(MQConstants.MQIA_MAX_MSG_LENGTH));
            row.add(response.getParameterValue(MQConstants.MQCA_BACKOUT_REQ_Q_NAME));
            listOfLists.add(row);
            row = new ArrayList<>();

        }
        disconnect(agent);
        return listOfLists;
    }

    public static int[] queueAttrbites() {
        int[] attrs = {MQConstants.MQCA_Q_NAME, MQConstants.MQCA_CLUSTER_NAME,
                MQConstants.MQIA_TRIGGER_CONTROL};

        return attrs;
    }

    public static PCFParameter[] pcfParamaters(int[] queueAttrbites) {
        PCFParameter[] parameters = {new MQCFST(MQConstants.MQCA_Q_NAME, "*"),
                new MQCFIN(MQConstants.MQIA_Q_TYPE, MQConstants.MQQT_ALL),
                new MQCFIL(MQConstants.MQIACF_Q_ATTRS, queueAttrbites)};
        return parameters;
    }

    public static String getQueueType(int type) {
        String queueType = null;
        switch (type) {
            case MQConstants.MQQT_LOCAL:
                queueType = "Local";
                break;
            case MQConstants.MQQT_REMOTE:
                queueType = "Remote";
                break;
            case MQConstants.MQQT_MODEL:
                queueType = "Model";
                break;
            case MQConstants.MQQT_ALIAS:
                queueType = "Alias";
                break;
            case MQConstants.MQQT_CLUSTER:
                queueType = "Cluster";
                break;
            default:
                queueType = "Unknown";
                break;
        }
        return queueType;
    }

    public static String DisplayRFH2(MQMessage message) throws IOException, MQDataException {

        StringBuilder sb = new StringBuilder();

        MQHeaderList headerList = new MQHeaderList(message);
        // TODO MQRFH, MQCIH, MQDLH, MQIIH, MQRMH, MQSAPH, MQWIH, MQXQH, MQDH, MQEPH headers support
        int index = headerList.indexOf("MQRFH2");
        if (index >= 0) {

            sb.append("MQRFH2 header detected (index " + index + ")");
            MQRFH2 rfh = (MQRFH2) headerList.get(index);
            sb.append("\nformat: " + rfh.getFormat());
            sb.append("\nstruct id: " + rfh.getStrucId());
            sb.append("\nencoding: " + rfh.getEncoding());
            sb.append("\ncoded charset id: " + rfh.getCodedCharSetId());
            sb.append("\nflags: " + rfh.getFlags());
            sb.append("\nversion: " + rfh.getVersion());
            MQRFH2.Element[] folders = rfh.getFolders();
            for (MQRFH2.Element folder : folders) {
                sb.append("\nfolder " + folder.getName() + ": " + folder.toXML());
            }
        } else {
            sb.append("Header Not found!");
        }


        MQHeaderIterator it = new MQHeaderIterator(message);

        while (it.hasNext()) {
            MQHeader header = it.nextHeader();
            sb.append(header.type() + ": " + header);
            sb.append("\n");
            System.out.println("Header type " + header.type() + ": " + header);
        }

        return sb.toString();
    }

    public static MultiValuedMap<Integer, String> queueStatusHandler(MQQueueManager queueManager, String queueName) throws MQDataException, MQException, IOException {

        // Create the PCF message type for the inquire.
        PCFMessage pcfCmd = new PCFMessage(MQConstants.MQCMD_INQUIRE_Q_STATUS);
        // Add queue name
        pcfCmd.addParameter(MQConstants.MQCA_Q_NAME, queueName);
        // We want Q HANDLE attributes
        pcfCmd.addParameter(MQConstants.MQIACF_Q_STATUS_TYPE, MQConstants.MQIACF_Q_HANDLE);
        // We want to retrieve only the connection name
        pcfCmd.addParameter(MQConstants.MQIACF_Q_STATUS_ATTRS, MQConstants.MQCACH_CONNECTION_NAME);
        // Execute the command. The returned object is an array of PCF messages.
        PCFMessageAgent agent = new PCFMessageAgent(queueManager);
        PCFMessage[] pcfResponses = agent.send(pcfCmd);

        MultiValuedMap<Integer, String> multiValuedMap = new ArrayListValuedHashMap<>();
        int i = 0;
        for (PCFMessage pcfResponse : pcfResponses) {

            multiValuedMap.putAll(++i, Arrays.asList(

                    String.valueOf(pcfResponse.getParameterValue(MQConstants.MQCACF_APPL_DESC)).trim(),
                    String.valueOf(pcfResponse.getParameterValue(MQConstants.MQCACF_APPL_TAG)).trim(),
                    String.valueOf(pcfResponse.getParameterValue(MQConstants.MQCACH_CHANNEL_NAME)).trim(),
                    String.valueOf(pcfResponse.getParameterValue(MQConstants.MQIACF_OPEN_OUTPUT)).trim(),
                    String.valueOf(pcfResponse.getParameterValue(MQConstants.MQCACF_USER_IDENTIFIER)).trim(),
                    String.valueOf(pcfResponse.getParameterValue(MQConstants.MQCACH_CONNECTION_NAME)).trim()

            ));
        }
        disconnect(agent);
        return multiValuedMap;
    }

    private static String queueStatusHandle(MQQueueManager queueManager, String queueName) throws MQDataException, MQException, IOException {

        PCFMessage request = null;
        PCFMessage[] responses = null;
        PCFMessageAgent agent = null;
        /**
         * You can explicitly set a queue name like "TEST.Q1" or
         * use a wild card like "TEST.*"
         */
        request = new PCFMessage(MQConstants.MQCMD_INQUIRE_Q_STATUS);
        request.addParameter(MQConstants.MQCA_Q_NAME, queueName);
        request.addParameter(MQConstants.MQIACF_Q_STATUS_TYPE, MQConstants.MQIACF_Q_HANDLE);
        request.addParameter(MQConstants.MQIACF_Q_STATUS_ATTRS, new int[]{MQConstants.MQIACF_ALL});

//        request.addParameter(MQConstants.MQIACF_Q_STATUS_ATTRS, MQConstants.MQCACH_CONNECTION_NAME);

        // Add a parameter that selects all of the attributes we want
        /*request.addParameter(MQConstants.MQIACF_Q_STATUS_ATTRS,
                             new int [] { MQConstants.MQCA_Q_NAME,
        		MQConstants.MQIACF_Q_STATUS_ATTRS, MQConstants.MQCACH_CONNECTION_NAME,
                                          MQConstants.MQCACF_LAST_PUT_DATE,
                                          MQConstants.MQCACF_LAST_PUT_TIME,
                                          MQConstants.MQCACF_LAST_GET_DATE,
                                          MQConstants.MQCACF_LAST_GET_TIME,
                                        });*/
        /**
         * Other attributes that can be used for TYPE(QUEUE)
         * - MQIA_MONITORING_Q
         * - MQCACF_MEDIA_LOG_EXTENT_NAME
         * - MQIACF_OLDEST_MSG_AGE
         * - MQIACF_Q_TIME_INDICATOR
         * - MQIACF_UNCOMMITTED_MSGS
         */
        agent = new PCFMessageAgent(queueManager);
        responses = agent.send(request);
        StringBuffer sb = new StringBuffer();
        for (int i = 0; i < responses.length; i++) {
            if (((responses[i]).getCompCode() == MQConstants.MQCC_OK) &&
                    ((responses[i]).getParameterValue(MQConstants.MQCA_Q_NAME) != null)) {
                String name = responses[i].getStringParameterValue(MQConstants.MQCA_Q_NAME);
                if (name != null)
                    name = name.trim();

                String connName = (String) responses[i].getParameterValue(MQConstants.MQCACH_CONNECTION_NAME);

                if (connName != null) {
                    sb.append("Connection Name :" + connName);
                    sb.append("\n");
                }

                String lastPutDate = responses[i].getStringParameterValue(MQConstants.MQCACF_LAST_PUT_DATE);
                if (lastPutDate != null) {
                    lastPutDate = lastPutDate.trim();
                    sb.append("lastPutDate :" + lastPutDate);
                    sb.append("\n");
                }

                String lastPutTime = responses[i].getStringParameterValue(MQConstants.MQCACF_LAST_PUT_TIME);
                if (lastPutTime != null) {
                    lastPutTime = lastPutTime.trim();
                    sb.append("lastPutTime :" + lastPutTime);
                    sb.append("\n");
                }

                String lastGetDate = responses[i].getStringParameterValue(MQConstants.MQCACF_LAST_GET_DATE);
                if (lastGetDate != null) {
                    lastGetDate = lastGetDate.trim();
                    sb.append("lastGetDate :" + lastGetDate);
                    sb.append("\n");
                }

                String lastGetTime = responses[i].getStringParameterValue(MQConstants.MQCACF_LAST_GET_TIME);
                if (lastGetTime != null) {
                    lastGetTime = lastGetTime.trim();
                    sb.append("lastGetTime :" + lastGetTime);
                    sb.append("\n");
                }

            }
        }

        queueManager.disconnect();
        agent.disconnect();

        return sb.toString();

    }

    public static void purgeQ(MQQueueManager qMgr, String q) {


        MQQueue queue = null;
        int openOptions = CMQC.MQOO_INPUT_AS_Q_DEF + CMQC.MQOO_INQUIRE + CMQC.MQOO_FAIL_IF_QUIESCING;
        MQGetMessageOptions gmo = new MQGetMessageOptions();
        gmo.options = CMQC.MQGMO_FAIL_IF_QUIESCING + CMQC.MQGMO_ACCEPT_TRUNCATED_MSG;
        MQMessage receiveMsg = null;
        int msgCount = 0;
        boolean getMore = true;

    }

    public static StringBuilder copyMessageMQMD(MQMessage oldMessage) {
        StringBuilder sb = new StringBuilder();
        for (Field field : oldMessage.getClass().getFields()) {
            if (Modifier.isPublic(field.getModifiers()) && !Modifier.isStatic(field.getModifiers())) {
                try {
                    String name = field.getName();
                    Object value = field.get(oldMessage);

                    if (value instanceof GregorianCalendar) {
                        GregorianCalendar new_name = (GregorianCalendar) value;
                        sb.append(name.toUpperCase() + " : " + new Date(new_name.getTimeInMillis()) + "\n");
                    } else if (value instanceof byte[]) {
//						byte[] new_var =
                        sb.append(name.toUpperCase() + " : " + CommonUtils.getHexString((byte[]) value) + "\n");
                    } else {
                        sb.append(name.toUpperCase() + " : " + value + "\n");
                    }

                } catch (Exception ex) {
                    continue;
                }
            }
        }
        return sb;
    }

    public static String getInhibitName(int inhibit) {
        return inhibit == 0 ? "Allowed" : "Inhibit";
    }

    public static MQQueue accessQueue(MQQueueManager queueManager, String queueName) throws MQException, IOException {
        return queueManager.accessQueue(queueName, MQConstants.MQOO_INQUIRE | MQConstants.MQOO_BROWSE | MQConstants.MQOO_INPUT_SHARED); //| CMQC.MQOO_INPUT_SHARED | CMQC.MQOO_OUTPUT
    }

    public static List<List<Object>> listQueues(String qMgrInfo[], String filter)
            throws NumberFormatException, MQException, IOException, MQDataException {

        List<List<Object>> listOfLists = new ArrayList<List<Object>>();
        PCFMessageAgent agent = null;
        PCFMessage request = new PCFMessage(MQConstants.MQCMD_INQUIRE_Q);

        agent = getPcfMessageAgent(qMgrInfo);
        switch (filter.toUpperCase()) {
            case "SYSTEM":
                request.addParameter(MQConstants.MQCA_Q_NAME, "SYSTEM.*");
                break;

            default:
                request.addParameter(MQConstants.MQCA_Q_NAME, "*");
                break;
        }
        // inquireQueueStatus.addParameter (MQConstants.MQIA_Q_TYPE,
        // MQConstants.MQQT_ALL);
        // request.addFilterParameter (MQConstants.MQIA_CURRENT_Q_DEPTH,
        // MQConstants.MQCFOP_GREATER, 0);

        PCFMessage[] pcfResponse = agent.send(request);
        // PCFMessage[] status = null;

        List<Object> row = new ArrayList<>();

        Object temp = null;
        for (PCFMessage response : pcfResponse) {

            row.add(response.getStringParameterValue(MQConstants.MQCA_Q_NAME));
            row.add(getQueueType((int) response
                    .getParameterValue(MQConstants.MQIA_Q_TYPE)));
            row.add(response.getParameterValue(MQConstants.MQIA_OPEN_INPUT_COUNT));
            row.add(response.getParameterValue(MQConstants.MQIA_OPEN_OUTPUT_COUNT));
            row.add(response.getParameterValue(MQConstants.MQIA_CURRENT_Q_DEPTH));
            row.add(response.getParameterValue(MQConstants.MQ_PUT_DATE_LENGTH));
            // System.out.println(response);
            temp = response.getParameterValue(MQConstants.MQIA_DEF_PERSISTENCE);
            if (temp == null) {
                row.add("null");
            } else if (0 == (int) temp) {
                row.add("Non-Persistent");
            } else {
                row.add("Persistent");
            }
            row.add(response.getParameterValue(MQConstants.MQIA_DEF_PERSISTENCE));
            temp = response.getParameterValue(MQConstants.MQIA_INHIBIT_GET);
            if (temp == null) {
                row.add("null");
            } else if (0 == (int) temp) {
                row.add("Allowed");
            } else {
                row.add("Inhibited");
            }
            temp = response.getParameterValue(MQConstants.MQIA_INHIBIT_PUT);
            if (temp == null) {
                row.add("null");
            } else if (0 == (int) temp) {
                row.add("Allowed");
            } else {
                row.add("Inhibited");
            }

            row.add(response.getParameterValue(MQConstants.MQIA_MAX_Q_DEPTH));
            row.add(response.getParameterValue(MQConstants.MQIA_MAX_MSG_LENGTH));
            listOfLists.add(row);
            row = new ArrayList<>();

        }
        disconnect(agent);
        return listOfLists;
    }

    public static void disconnect(PCFMessageAgent agent) throws MQException, MQDataException {
        if (agent != null) {
            agent.disconnect();
            agent = null;
        }
    }

    public static void disconnect(MQQueueManager queueManager) throws MQException {
        if (queueManager != null) {
            queueManager.close();
            queueManager.disconnect();
            queueManager = null;
        }
    }

    public static MQQueueManager getRemoteQueueManager(String hostname, String channel,
                                                       int port) throws MQException, MQDataException {


        Hashtable<String, Object> mqht = new Hashtable<String, Object>();

        mqht.put(MQConstants.CHANNEL_PROPERTY, channel);
        mqht.put(MQConstants.HOST_NAME_PROPERTY, hostname);
        mqht.put(MQConstants.PORT_PROPERTY, new Integer(port));
        mqht.put(MQConstants.TRANSPORT_PROPERTY, MQConstants.TRANSPORT_MQSERIES_CLIENT);

//		// Set up MQ environment
//		MQEnvironment.hostname = hostname;
//		MQEnvironment.channel = channel;
//		MQEnvironment.port = port;

//		 String queueName = "MYAPP.REQUEST";
        String queueManagerName = getQMgrName(new String[]{hostname, String.valueOf(port), channel});
//		 @SuppressWarnings("unchecked")
//		Hashtable<String, String> properties = MQEnvironment.properties;
//		 properties.put(MQConstants.TRANSPORT_PROPERTY, MQConstants.TRANSPORT_MQSERIES_CLIENT);
        return new MQQueueManager(queueManagerName, mqht);
    }

    public static MQQueueManager getLocalQueueManager(String queueManagerName
    ) throws MQException {

        return new MQQueueManager(queueManagerName);
    }

    public static MQMessage getMessage(MQQueueManager queueManager, String queueName, int openOptions, int gmo) throws MQException {

        MQQueue queue = MQExplorer.getQueue(queueManager,
                queueName, openOptions);

        MQGetMessageOptions getOptions = new MQGetMessageOptions();
        getOptions.options = gmo;

        MQMessage message = new MQMessage();
        queue.get(message, getOptions);
        queue.close();
        disconnect(queueManager);

        return message;

    }

	/*private static void printListofLists(List<List<Object>> listOfLists) {
		for (List<Object> list : listOfLists) {
			for (Object object : list) {
				System.out.print(String.valueOf(object).trim() + "\t");
			}
			System.out.println();
		}
	}*/

    public static MQQueue getQueue(MQQueueManager queueManager, String queueName, int openOptions) throws MQException {
//		 @SuppressWarnings("unchecked")
//		 Hashtable<String, String> properties = MQEnvironment.properties;
//		 properties.put(MQConstants.TRANSPORT_PROPERTY, MQConstants.TRANSPORT_MQSERIES_CLIENT);
//		 MQQueueManager queueManager = new MQQueueManager(queueManagerName);
//		 int openOptions = MQConstants.MQOO_INPUT_AS_Q_DEF | MQConstants.MQOO_OUTPUT;
//		 MQQueue queue = queueManager.accessQueue(queueName, openOptions,null, null, null);
        return queueManager.accessQueue(queueName, openOptions, null, null, null);
    }

    public static MQMessage readQ(MQQueueManager queueManager, String queueName) throws MQException {
        return getMessage(queueManager, queueName, MQConstants.MQGMO_BROWSE_NEXT);
    }

    public static MQMessage browseQ(MQQueueManager queueManager, String queueName) throws MQException {
        return getMessage(queueManager, queueName, MQConstants.MQGMO_BROWSE_NEXT);
    }

    public static MQMessage getMessage(MQQueueManager queueManager, String queueName, int attribute) throws MQException {
        MQQueue queue = null;
        MQMessage message = new MQMessage();
        MQGetMessageOptions options = new MQGetMessageOptions();
        options.options = attribute;
        try {
            queue = queueManager.accessQueue(queueName, MQConstants.MQOO_INQUIRE | MQConstants.MQOO_BROWSE);
            queue.get(message, options);

        } catch (MQException ex) {
            ex.printStackTrace();
            closeQueue(queue);
            throw ex;
        }
        closeQueue(queue);
        return message;
    }


//	public static void sendMQMessage(final MQQueue queue, String messageId, String payload) throws IOException, MQException {
//
//		 MQMessage putMessage = new MQMessage();
//
//		 putMessage.messageId = messageId.getBytes();
//		 putMessage.correlationId = messageId.getBytes();
//		 putMessage.characterSet = 1208; //UTF-8
//		 putMessage.format = CMQConstants.MQFMT_STRING;
//		 putMessage.persistence = CMQConstants.MQPER_PERSISTENCE_AS_Q_DEF; //durable message
//		 putMessage.expiry = 28800000; //in 1/10th of a second. 48 hours.
//
//		 putMessage.replyToQueueName = "REPLYTO.Q";
//		 putMessage.replyToQueueManagerName = "REPLYTO.QMGR";
//		 putMessage.writeString(payload);
//
//		 queue.put(putMessage); //put the message on to the queue
//		 }

    public static List<List<Object>> channelStatus(String qMgrInfo[])
            throws NumberFormatException, MQException, IOException, MQDataException {
        PCFMessage request;
        PCFMessageAgent agent = null;

        request = new PCFMessage(MQConstants.MQCMD_INQUIRE_CHANNEL_STATUS);
        request.addParameter(MQConstants.MQCACH_CHANNEL_NAME, "*");

        List<List<Object>> listOfLists = new ArrayList<List<Object>>();

        List<Object> row = new ArrayList<>();

        agent = getPcfMessageAgent(qMgrInfo);

        for (PCFMessage pcfMessage : agent.send(request)) {
//			System.out.println(pcfMessage);
            row.add(pcfMessage
                    .getParameterValue(MQConstants.MQCACH_CHANNEL_NAME));
            row.add(pcfMessage
                    .getParameterValue(MQConstants.MQCACH_CONNECTION_NAME));
            row.add(channelRunningStatus((int) pcfMessage
                    .getParameterValue(MQConstants.MQIACH_CHANNEL_STATUS)));
            row.add(getChannelSubState((int) pcfMessage
                    .getParameterValue(MQConstants.MQIACH_CHANNEL_SUBSTATE)));
            row.add(pcfMessage.getParameterValue(MQConstants.MQIACH_MSGS));
            row.add(pcfMessage.getParameterValue(MQConstants.MQCACH_LAST_MSG_DATE));
            row.add(pcfMessage.getParameterValue(MQConstants.MQCACH_LAST_MSG_TIME));
			/*row.add(pcfMessage
					.getParameterValue(MQConstants.MQIACH_BUFFERS_RECEIVED));
			row.add(pcfMessage
					.getParameterValue(MQConstants.MQIACH_BUFFERS_SENT));*/
            row.add(pcfMessage
                    .getParameterValue(MQConstants.MQIACH_BYTES_RECEIVED));
            row.add(pcfMessage.getParameterValue(MQConstants.MQIACH_BYTES_SENT));
            row.add(pcfMessage
                    .getParameterValue(MQConstants.MQCACH_MCA_USER_ID));

            row.add(pcfMessage
                    .getParameterValue(MQConstants.MQCACH_REMOTE_APPL_TAG));


            listOfLists.add(row);
            row = new ArrayList<>();
        }

        disconnect(agent);

        return listOfLists;
    }

    public static String getMQExceptionAsString(MQException e) {
        return new StringBuilder(e.getClass().getCanonicalName()
                + " exception occured. Reason Code = ").append(e.getReason())
                .append("; Error code = ").append(e.getErrorCode())
                .append("; Message = '").append(e.getMessage())
                .append("'; Stacktrace is '")
                .append(CommonUtils.getStackTraceString(e)).append("'").toString();
    }

    public static String getMQDataExceptionAsString(MQDataException e) {
        return new StringBuilder(e.getClass().getCanonicalName()
                + " exception occured. Reason Code = ").append(e.getReason())
                .append("; Error code = ").append(e.getErrorCode())
                .append("; Message = '").append(e.getMessage())
                .append("'; Stacktrace is '")
                .append(CommonUtils.getStackTraceString(e)).append("'").toString();
    }

	/*private void readQ(String hostname, String channel, String qMgr,
			int port, String inputQName) throws MQException {

		// Set up MQ environment
		MQEnvironment.hostname = hostname;
		MQEnvironment.channel = channel;
		MQEnvironment.port = port;

		MQQueueManager queueManager = new MQQueueManager(qMgr);

		int openOptions = MQConstants.MQOO_INQUIRE + MQConstants.MQOO_FAIL_IF_QUIESCING
				+ MQConstants.MQOO_INPUT_SHARED;

		MQQueue queue = queueManager.accessQueue(inputQName, openOptions, null, // default
																				// q
																				// manager
				null, // no dynamic q name
				null); // no alternate user id

		System.out.println("MQRead v1.0 connected.\n");

		int depth = queue.getCurrentDepth();
		System.out.println("Current depth: " + depth + "\n");
		if (depth == 0) {
			return;
		}

		MQGetMessageOptions getOptions = new MQGetMessageOptions();
		getOptions.options = MQConstants.MQGMO_NO_WAIT + MQConstants.MQGMO_FAIL_IF_QUIESCING;
//				+ MQConstants.MQGMO_CONVERT;
		while (true) {
			MQMessage message = new MQMessage();
			try {
				queue.get(message, getOptions);
				byte[] b = new byte[message.getMessageLength()];
				message.readFully(b);
				System.out.println(new String(b));
				message.clearMessage();
			} catch (IOException e) {
				System.out.println("IOException during GET: " + e.getMessage());
				break;
			} catch (MQException e) {
				if (e.completionCode == 2
						&& e.reasonCode == MQException.MQRC_NO_MSG_AVAILABLE) {
					if (depth > 0) {
						System.out.println("All messages read.");
					}
				} else {
					System.out.println("GET Exception: " + e);
				}
				break;
			}
		}
		closeQueue(queue);
		disconnect(queueManager);
	}*/

    public static List<List<Object>> qmStatus(String qMgrInfo[])
            throws NumberFormatException, MQException, IOException, MQDataException {
        PCFMessage request;
        PCFMessageAgent agent = getPcfMessageAgent(qMgrInfo);

        request = new PCFMessage(MQConstants.MQCMD_INQUIRE_Q_MGR_STATUS);
        request.addParameter(MQConstants.MQIACF_Q_MGR_STATUS_ATTRS,
                new int[]{MQConstants.MQIACF_ALL});

        List<List<Object>> listOfLists = new ArrayList<List<Object>>();

        List<Object> row = new ArrayList<>();

        for (PCFMessage pcfMessage : agent.send(request)) {
            row.add(pcfMessage.getParameterValue(MQConstants.MQCA_Q_MGR_NAME));
            row.add(qmRunningStatus((int) pcfMessage
                    .getParameterValue(MQConstants.MQIACF_Q_MGR_STATUS)));
            row.add(pcfMessage
                    .getParameterValue(MQConstants.MQIACF_CONNECTION_COUNT));
            row.add(pcfMessage
                    .getParameterValue(MQConstants.MQCA_INSTALLATION_NAME));
            row.add(pcfMessage
                    .getParameterValue(MQConstants.MQCA_INSTALLATION_PATH));
            row.add(pcfMessage.getParameterValue(MQConstants.MQCACF_LOG_PATH));
            row.add(pcfMessage
                    .getParameterValue(MQConstants.MQCACF_Q_MGR_START_DATE));
            row.add(pcfMessage
                    .getParameterValue(MQConstants.MQCACF_Q_MGR_START_TIME));
            listOfLists.add(row);
            row = new ArrayList<>();
        }
        disconnect(agent);
        return listOfLists;
    }

    private static String qmRunningStatus(int status) {
        String s = "UNKNOWN";
        switch (status) {
            case MQConstants.MQQMSTA_STARTING:
                s = "STARTING";
                break;
            case MQConstants.MQQMSTA_RUNNING:
                s = "RUNNING";
                break;
            case MQConstants.MQQMSTA_QUIESCING:
                s = "QUIESCING";
                break;
            case MQConstants.MQQMSTA_STANDBY:
                s = "STANDBY";
                break;
            default:
                break;
        }
        return s;

    }

    /**
     * Determine the channel SubStatus
     *
     * @param chlSubState
     * @return subStatus
     */
    private static String getChannelSubState(int chlSubState) {
        String subState = "Unknown";
        if (chlSubState == MQConstants.MQCHSSTATE_OTHER)
            subState = "Other";
        else if (chlSubState == MQConstants.MQCHSSTATE_END_OF_BATCH)
            subState = "End of Batch";
        else if (chlSubState == MQConstants.MQCHSSTATE_SENDING)
            subState = "Sending";
        else if (chlSubState == MQConstants.MQCHSSTATE_RECEIVING)
            subState = "Receiving";
        else if (chlSubState == MQConstants.MQCHSSTATE_SERIALIZING)
            subState = "Serializing";
        else if (chlSubState == MQConstants.MQCHSSTATE_RESYNCHING)
            subState = "Resyncing";
        else if (chlSubState == MQConstants.MQCHSSTATE_HEARTBEATING)
            subState = "Heartbeating";
        else if (chlSubState == MQConstants.MQCHSSTATE_IN_SCYEXIT)
            subState = "In Security Exit";
        else if (chlSubState == MQConstants.MQCHSSTATE_IN_RCVEXIT)
            subState = "In Receive Exit";
        else if (chlSubState == MQConstants.MQCHSSTATE_IN_SENDEXIT)
            subState = "In Send Exit";
        else if (chlSubState == MQConstants.MQCHSSTATE_IN_MSGEXIT)
            subState = "In Msg Exit";
        else if (chlSubState == MQConstants.MQCHSSTATE_IN_MREXIT)
            subState = "In MR Exit";
        else if (chlSubState == MQConstants.MQCHSSTATE_IN_CHADEXIT)
            subState = "In CHAD Exit";
        else if (chlSubState == MQConstants.MQCHSSTATE_NET_CONNECTING)
            subState = "Net Connecting";
        else if (chlSubState == MQConstants.MQCHSSTATE_SSL_HANDSHAKING)
            subState = "SSL Handshaking";
        else if (chlSubState == MQConstants.MQCHSSTATE_NAME_SERVER)
            subState = "Name Server";
        else if (chlSubState == MQConstants.MQCHSSTATE_IN_MQPUT)
            subState = "In MQPut";
        else if (chlSubState == MQConstants.MQCHSSTATE_IN_MQGET)
            subState = "In MQGet";
        else if (chlSubState == MQConstants.MQCHSSTATE_IN_MQI_CALL)
            subState = "In MQI Call";
        else if (chlSubState == MQConstants.MQCHSSTATE_COMPRESSING)
            subState = "Compressing";

        return subState;
    }

    private static String channelRunningStatus(int status) {
        String s = "UNKNOWN";
        switch (status) {
            case MQConstants.MQCHS_INACTIVE:
                s = "INACTIVE";
                break;
            case MQConstants.MQCHS_BINDING:
                s = "BINDING";
                break;
            case MQConstants.MQCHS_STARTING:
                s = "STARTING";
                break;
            case MQConstants.MQCHS_RUNNING:
                s = "RUNNING";
                break;
            case MQConstants.MQCHS_STOPPING:
                s = "STOPPING";
                break;
            case MQConstants.MQCHS_RETRYING:
                s = "RETRYING";
                break;
            case MQConstants.MQCHS_STOPPED:
                s = "STOPPED";
                break;
            case MQConstants.MQCHS_REQUESTING:
                s = "REQUESTING";
                break;
            case MQConstants.MQCHS_PAUSED:
                s = "PAUSED";
                break;
            case MQConstants.MQCHS_DISCONNECTED:
                s = "DISCONNECTED";
                break;
            case MQConstants.MQCHS_INITIALIZING:
                s = "INITIALIZING";
                break;
            case MQConstants.MQCHS_SWITCHING:
                s = "SWITCHING";
                break;
            default:
                break;
        }
        return s;

    }

    public static MQMessage GetMessage(MQQueueManager queueManager, String queueName, byte[] messageId, byte[] correlationId) throws MQException {
        MQQueue queue = null;
        MQMessage message = new MQMessage();
        message.messageId = messageId;
        message.correlationId = correlationId;
        MQGetMessageOptions options = new MQGetMessageOptions();
        options.options = MQConstants.MQGMO_BROWSE_NEXT;
        options.matchOptions = MQConstants.MQMO_MATCH_MSG_ID | MQConstants.MQMO_MATCH_CORREL_ID;
        queue = queueManager.accessQueue(queueName, MQConstants.MQOO_INQUIRE | MQConstants.MQOO_BROWSE);
        queue.get(message, options);

        closeQueue(queue);
        disconnect(queueManager);
        //String aa = getDefaultCharSet();
        return message;
    }

    public static MQMessage GetMessage(MQQueueManager queueManager, String queueName, byte[] messageId) throws MQException {
        MQQueue queue = null;
        MQMessage message = new MQMessage();
        message.messageId = messageId;
        MQGetMessageOptions options = new MQGetMessageOptions();
        options.options = MQConstants.MQGMO_BROWSE_NEXT;
        options.matchOptions = MQConstants.MQMO_MATCH_MSG_ID;
        queue = queueManager.accessQueue(queueName, MQConstants.MQOO_INQUIRE | MQConstants.MQOO_BROWSE);
        queue.get(message, options);
        closeQueue(queue);
        disconnect(queueManager);
        return message;
    }

    public static String convertMQMessageToString(MQMessage message) throws IOException {

        byte[] b = new byte[message.getMessageLength()];
        message.readFully(b);

        return new String(b);


    }

    public static void closeQueue(MQQueue queue) throws MQException {
        if (queue != null) {
            queue.close();
            queue = null;
        }
    }

    /**
     * @param args
     * @throws IOException
     * @throws MQException
     * @throws NumberFormatException
     */
    public static void main(String[] args) throws NumberFormatException,
            MQException, IOException, MQDataException {


//		System.out.println(displayQ("IB9QMGR", "ERROR.Q"));;

//		printListofLists(qmStatus(new String[] {"IB9QMGR"}));


//		String str[] = { "cgeaiq1", "1515", "BROKER.CHANNEL" };
//		System.out.println(get("cgeaiq1",  "BROKER.CHANNEL",1515).getName());
//		System.out.println(getQMgrName(str));
        System.out.println(listQueues("IB9QMGR", "SYSTEM"));
//		printListofLists(channelStatus(str));
//		printListofLists(listQueues(str,""));

//		for (String string : listQueues("IB9QMGR")) {
//			System.out.println(string);
//		}
    }

    public static void publishMsg(MQQueueManager queueManager, String queueName, String topicString, byte[] data) throws MQException, IOException {

        int openOutputOptions = CMQC.MQOO_OUTPUT + CMQC.MQOO_FAIL_IF_QUIESCING;
        MQTopic publisher = null;
        MQMessage mqMsg = null;

        try {
            publisher = queueManager.accessTopic(topicString,
                    null,
                    CMQC.MQTOPIC_OPEN_AS_PUBLICATION,
                    openOutputOptions);

            mqMsg = new MQMessage();
            mqMsg.messageId = CMQC.MQMI_NONE;
            mqMsg.correlationId = CMQC.MQCI_NONE;
            mqMsg.write(data);
            publisher.put(mqMsg, new MQPutMessageOptions());


        } finally {
            if (publisher != null)
                publisher.close();
            closeQMgr(queueManager);
        }


    }

    public static int writeQ(MQQueueManager queueManager, String queueName, byte[] data) throws MQException, IOException {

        int openOptions = MQConstants.MQOO_OUTPUT
                | MQConstants.MQOO_INPUT_AS_Q_DEF
                | MQConstants.MQOO_FAIL_IF_QUIESCING | MQConstants.MQOO_INQUIRE;
        ;
        // specify the message options...
        MQPutMessageOptions pmo = new MQPutMessageOptions(); // default
        // applications using client bindings.
        pmo.options = MQConstants.MQPMO_ASYNC_RESPONSE;
        MQQueue queue = null;
        MQMessage sendMsg;

        try {

            queue = queueManager.accessQueue(queueName, openOptions);

            sendMsg = new MQMessage();
            sendMsg.messageId = MQConstants.MQMI_NONE;
            sendMsg.correlationId = MQConstants.MQMI_NONE;
            sendMsg.characterSet = 1208;
            sendMsg.encoding = MQConstants.MQENC_NATIVE;
            sendMsg.format = MQConstants.MQFMT_STRING;
            sendMsg.putDateTime = new GregorianCalendar(Locale.US);
            sendMsg.write(data);

            // put the message on the queue
            queue.put(sendMsg, pmo);
            return queue.getCurrentDepth();
        } catch (MQException e) {
            // e.printStackTrace();
            throw e;
        } catch (IOException e) {
            throw e;
        } finally {
            closeQueue(queue);
        }
    }


    public MQMessage setMQRFH2(MQQueue queue) throws IOException {
        MQMessage sendmsg = new MQMessage();
        MQRFH2 rfh2 = new MQRFH2();
        rfh2.setEncoding(MQConstants.MQENC_NATIVE);
        rfh2.setCodedCharSetId(MQConstants.MQCCSI_INHERIT);
        rfh2.setFormat(MQConstants.MQFMT_STRING);
        rfh2.setFlags(0);
        rfh2.setNameValueCCSID(1208);
        // type of JMS message
        rfh2.setFieldValue("mcd", "Msd", "jms_text");
        // set named properties in the "usr" folder
        rfh2.setFieldValue("usr", "SomeNum", 123);
        rfh2.setFieldValue("usr", "SomeText", "TEST");
        // set named properties in the "other" folder
        rfh2.setFieldValue("other", "thingA", 789);
        rfh2.setFieldValue("other", "thingB", "XYZ");
        // Set the MQRFH2 structure to the message
        rfh2.write(sendmsg);
        sendmsg.writeString("This is a test message.");
        // IMPORTANT: Set the format to MQRFH2 aka JMS Message.
        sendmsg.format = MQConstants.MQFMT_RF_HEADER_2;
        return sendmsg;
    }

    public int purgeQueue(String qmgrHost, int qmgrPort, String qName, String qChannel)
            throws MQDataException, IOException, MQException {

        PCFMessageAgent agent = new PCFMessageAgent(qmgrHost, qmgrPort,
                qChannel);

        PCFMessage pcfCmd = new PCFMessage(MQConstants.MQCMD_CLEAR_Q);
        pcfCmd.addParameter(MQConstants.MQCA_Q_NAME, qName);
        PCFMessage[] pcfResponse = agent.send(pcfCmd);
        agent.disconnect();
        return pcfResponse[0].getCompCode();

    }
}