package com.myapp.iib.admin.mq;

import com.ibm.mq.*;
import com.ibm.mq.constants.MQConstants;
import com.ibm.mq.headers.MQDataException;
import com.ibm.mq.headers.pcf.PCFMessageAgent;
import lombok.NoArgsConstructor;
import lombok.Setter;
import lombok.experimental.Accessors;
import org.apache.commons.lang3.StringUtils;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.*;


@lombok.Getter
@Setter
@NoArgsConstructor
@Accessors(fluent = true)
//@RequiredArgsConstructor
public class MQOperations {

    private final int writeOptions = MQConstants.MQOO_OUTPUT
            | MQConstants.MQOO_INPUT_AS_Q_DEF
            | MQConstants.MQOO_FAIL_IF_QUIESCING | MQConstants.MQOO_INQUIRE;
    //    public static final String COPY = "COPY";
//    public static final String MOVE = "MOVE";
//    public static final String PURGE = "PURGE";
    private final int MAX_MSG_LENGTH = 200;
    private final int openOptions = MQConstants.MQOO_INQUIRE
            | MQConstants.MQOO_BROWSE | MQConstants.MQOO_INPUT_AS_Q_DEF;
    private final int browseOptions = MQConstants.MQGMO_WAIT
            | MQConstants.MQGMO_BROWSE_FIRST;
    private final int listOptions = MQConstants.MQOO_INQUIRE + MQConstants.MQOO_FAIL_IF_QUIESCING + MQConstants.MQOO_BROWSE + MQConstants.MQOO_INPUT_SHARED;
    private final int readOptions = MQConstants.MQOO_INQUIRE
            + MQConstants.MQOO_FAIL_IF_QUIESCING
            + MQConstants.MQOO_INPUT_SHARED;
    private final int purgeOptions = readOptions
            + MQConstants.MQGMO_ACCEPT_TRUNCATED_MSG;
    private final int purgeGMOOptions = MQConstants.MQGMO_NO_WAIT
            + MQConstants.MQGMO_FAIL_IF_QUIESCING
            + MQConstants.MQGMO_ACCEPT_TRUNCATED_MSG;
    private final MQPutMessageOptions pmo = new MQPutMessageOptions();
    private final MQGetMessageOptions gmo = new MQGetMessageOptions();
    private String hostName;
    private Integer port;
    private String channelName;
    private String srcQueueName;
    private String toQueueName;
    private MQQueueManager queueManager;
    private String filePath;
    private int msgSize;
    private boolean browseOnly;
    private MQQueue srcQueue;
    private int msgReadLimit;
    private byte[] messageId;
    private byte[] correlationId;
	

	/*public MQOperations(String hostName, Integer port, String channelName) throws MQException {
		this.hostName = hostName;
		this.port = port;
		this.channelName = channelName;
	}*/

    public static byte[] stringToByteArray(String s) {
        int i = s.length() / 2;
        byte abyte0[] = new byte[i];
        for (int j = 0; j < i; j++)
            abyte0[j] = (byte) Integer.parseInt(s.substring(j * 2, j * 2 + 2), 16);

        return abyte0;
    }

    public static String getHexString(byte[] b) {
        String result = "";
        for (int i = 0; i < b.length; i++) {
            result += Integer.toString((b[i] & 0xff) + 0x100, 16).substring(1);
        }
        return result;
    }

    public MQOperations setBrowseOnly(boolean browseOnly) {

        if (browseOnly) {
            this.gmo.options = MQConstants.MQGMO_WAIT | MQConstants.MQGMO_BROWSE_FIRST;
        }

        this.browseOnly = browseOnly;
		return this;
    }


    public boolean isConnected() {
        return this.queueManager == null;
    }

    public boolean isQueueExists() {
        try {
            this.queueManager.accessQueue(this.srcQueueName, openOptions);
            return true;
        } catch (MQException e) {
        }
        return false;
    }

    public MQOperations connect() throws MQException, MQDataException {

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

		return this;
    }

    public void disconnect() throws MQException {
        if (this.queueManager != null) {
            this.queueManager.close();
            this.queueManager.disconnect();
        }
    }
	
	/*public void moveQ(MQQueue fromMq, MQQueue toMQ) throws MQException, IOException {
		
		this.queue = fromMq;
		toMQ.put(readMessageFromQueue());
		this.queue.close();
		toMQ.close();
		
	}
	
	public void moveAllMsgsQ(MQQueue fromMq, MQQueue toMQ) throws MQException, IOException {
		
		this.queueName = fromMq.getName();
		toMQ.openOptions = this.writeOptions;
		for (MQMessage mqMessage : readAllMessagesFromQueue()) {
			System.out.println(mqMessage.readLine());
			toMQ.put(mqMessage, getPmo());
			System.out.println(toMQ.getName().trim() +"\t" + toMQ.getCurrentDepth());
			
		}
		this.queue.close();
		toMQ.close();
	}*/

    public int moveAllMsgsQ(MQQueueManager srcQmgr, String srcQueueName, MQQueueManager tgtQmgr, String tgtQueueName, int msgCount) throws MQException, IOException {

        MQQueue srcQueue = srcQmgr.accessQueue(srcQueueName, this.listOptions);

        int depth = srcQueue.getCurrentDepth();
        int N = 0;
        if (depth > 0) {
            MQQueue tarQueue = tgtQmgr.accessQueue(tgtQueueName, 17);
            MQGetMessageOptions getMessageOptions = MQExplorer.getMqGetMessageOptions();

            MQPutMessageOptions putMessageOptions = new MQPutMessageOptions();

            N = msgCount == -1 ? depth : msgCount;
            MQMessage srcMessage = new MQMessage();
            for (int i = 1; i <= N; i++) {

                srcMessage.clearMessage();
                System.out.println("Moving message # :" + i);
                srcQueue.get(srcMessage, getMessageOptions);
                tarQueue.put(srcMessage, putMessageOptions);

            }
            tarQueue.close();
        }
        srcQueue.close();
        return N;

    }

    public MQOperations closeQueue() throws MQException {
        if (this.srcQueue.isOpen()) {
            this.srcQueue.close();
        }
		return this;
    }

    public MQOperations writeQ(MQMessage sendMsg) throws MQException, IOException {

        srcQueue = this.queueManager.accessQueue(this.srcQueueName, writeOptions);

        srcQueue.put(sendMsg, this.pmo);
        closeQueue();
		return this;
    }


    public MQOperations moveQ(String fromMq, String toMQ) throws MQException, IOException {
        this.srcQueueName = fromMq;
        this.srcQueue = this.queueManager.accessQueue(this.srcQueueName, openOptions);
        MQQueue toQ = this.queueManager.accessQueue(toMQ, openOptions);
        toQ.put(readMessageFromQueue());
        this.srcQueue.close();
        toQ.close();
		return this;
    }

    public MQOperations writeQ(String filePath) throws MQException, IOException {
        this.filePath = filePath;
        writeQ(Files.readAllBytes(Paths.get(this.filePath)));
		return this;
    }

    public void writeQ(byte[] msg) throws MQException, IOException {

        MQMessage sendMsg = null;

        srcQueue = this.queueManager.accessQueue(this.srcQueueName, writeOptions);

        sendMsg = new MQMessage();
        sendMsg.messageId = this.messageId == null ? MQConstants.MQMI_NONE : this.messageId;
        sendMsg.correlationId = this.correlationId == null ? MQConstants.MQMI_NONE : this.correlationId;
        sendMsg.characterSet = 1208;
        sendMsg.encoding = MQConstants.MQENC_NATIVE;
        sendMsg.format = MQConstants.MQFMT_STRING;
        sendMsg.putDateTime = new GregorianCalendar(Locale.US);

        sendMsg.write(msg);
        srcQueue.put(sendMsg, this.pmo);
        closeQueue();
    }

    public void writeQv1(byte[] msg) throws MQException, IOException {

        MQMessage sendMsg = null;

        sendMsg = new MQMessage();
        sendMsg.characterSet = 1208;
        sendMsg.encoding = MQConstants.MQENC_NATIVE;
        sendMsg.format = MQConstants.MQFMT_STRING;
        sendMsg.putDateTime = new GregorianCalendar(Locale.US);

        sendMsg.write(msg);
        srcQueue.put(sendMsg, new MQPutMessageOptions());
    }

    public MQOperations purgeQ(String queueName) throws MQException, IOException {
        this.srcQueueName = queueName;
        purgeQ();
		return this;
    }

    public MQOperations purgeQ() throws MQException, IOException {

        srcQueue = this.queueManager.accessQueue(this.srcQueueName, this.purgeOptions);
        System.out.println("Purging starts for queue :" + this.srcQueueName);

        this.gmo.options = this.purgeGMOOptions;
        boolean loopAgain = true;
        MQMessage message;
        long msgCount = 0L;
        System.out.println("Queue Depth :" + srcQueue.getCurrentDepth());
        while (loopAgain) {
            message = new MQMessage();
            resetMQMessage(message);
            try {
                System.out.println("Purging Message # :" + ++msgCount);
                srcQueue.get(message, this.gmo, 1);

            } catch (MQException e) {
                if (e.completionCode == 1
                        && acceptTruncatedMsg(e)) {
                    // Just what we expected!!
//						System.err.println(e.getLocalizedMessage());
                    System.out.println("Queue Depth :" + srcQueue.getCurrentDepth());
                } else {
                    loopAgain = false;
                    if (e.completionCode == 2
                            && e.reasonCode == MQConstants.MQRC_NO_MSG_AVAILABLE) {
                        // Good, we are now done - no error!!
                    } else {
                        throw e;
                    }
                }
            }
        }

        srcQueue.close();
		return this;
    }

    public MQOperations purgeSpecificMsg(String queue, String msgId, String corrId) throws MQException, IOException {

        this.messageId = stringToByteArray(msgId);
        this.correlationId = stringToByteArray(corrId);
        purgeSpecificMsg();
		return this;
    }

    public MQOperations purgeSpecificMsg() throws MQException, IOException {

        srcQueue = this.queueManager.accessQueue(this.srcQueueName, this.purgeOptions);

        this.gmo.options = this.purgeGMOOptions;
        try {
            MQMessage message = new MQMessage();
            resetMQMessage(message);
            srcQueue.get(message, this.gmo, 1);
        } catch (MQException e) {
            if (e.completionCode == 1
                    && acceptTruncatedMsg(e)) {
                // Just what we expected!!
            } else {
                throw e;
            }
        } finally {
            srcQueue.close();
        }
        return this;
    }

    public boolean isQueueEmpty() throws MQException {
//		int openOptions = MQConstants.MQOO_INQUIRE
//				| MQConstants.MQOO_BROWSE | MQConstants.MQOO_INPUT_AS_Q_DEF;
//
//		return this.queueManager.accessQueue(this.queueName, openOptions).getCurrentDepth() > 0;
        return this.srcQueue.getCurrentDepth() <= 0;

    }

    public List<byte[]> getAllMessagesFromQueue(String queueName) throws IOException, MQException {
        this.srcQueueName = queueName;
        return getAllMessagesFromQueue();
    }

    public List<MQMessage> readAllMessagesFromQueue() throws MQException, IOException {
        srcQueue = this.queueManager.accessQueue(this.srcQueueName, this.listOptions);

        List<MQMessage> msgs = new ArrayList<>();

        int count = this.srcQueue.getCurrentDepth() > this.msgReadLimit ? this.msgReadLimit : this.srcQueue.getCurrentDepth();
        gmo.options = this.browseOptions;
        for (int i = 0; i < count; i++) {
            try {
                MQMessage message = new MQMessage();
                //Reset the message object
                resetMQMessage(message);
                // Browse the message, display it
                srcQueue.get(message, gmo);
                msgs.add(message);
                // Read the message from the queue - this will dequeue the message
                gmo.options = MQConstants.MQGMO_MSG_UNDER_CURSOR;
                srcQueue.get(message, gmo);

                // Reset the options to browse the next message
                resetOptionToMoveNext();
            } catch (MQException e) {
                if (e.completionCode == 1 && acceptTruncatedMsg(e)) {
                    // Just what we expected!!
                } else {

                    if (e.completionCode == 2
                            && e.reasonCode == MQConstants.MQRC_NO_MSG_AVAILABLE) {
                        // Good, we are now done - no error!!
                    } else {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
        closeQueue();
        return msgs;
    }

    public int purgeSelectedDocId(String docId) throws MQException, IOException {

//		int openOptions = MQConstants.MQOO_INPUT_AS_Q_DEF + MQConstants.MQOO_INQUIRE + MQConstants.MQOO_FAIL_IF_QUIESCING;

        srcQueue = this.queueManager.accessQueue(this.srcQueueName, this.listOptions);

        int count = 1;

        int depth = this.srcQueue.getCurrentDepth();
        System.out.println("Looking for messages :" + docId);
        if (depth > 0) {

            /******************************************************/
            /* Set up our options to browse for the first message */
            /******************************************************/
//			MQGetMessageOptions gmo = new MQGetMessageOptions();
//			this.gmo.options = this.browseOptions | MQConstants.MQGMO_ACCEPT_TRUNCATED_MSG;

            boolean loop = true;

            for (int i = 1; i <= depth && loop; i++) {

                try {

//					MQGetMessageOptions gmo = new MQGetMessageOptions();
//				    gmo.options = this.browseOptions + MQConstants.MQGMO_ACCEPT_TRUNCATED_MSG;

                    MQMessage myMessage = new MQMessage();
//					myMessage.clearMessage();
					/*myMessage.clearMessage();
					myMessage.correlationId = MQConstants.MQCI_NONE;
					myMessage.messageId = MQConstants.MQMI_NONE;*/

                    /**************************************************/
                    /* Browse the message, display it, and ask if the */
                    /* message should actually be gotten */
                    /**************************************************/
//					this.gmo.options = this.gmo.options + MQConstants.MQGMO_ACCEPT_TRUNCATED_MSG;;
					/*this.gmo.options = this.gmo.options + MQConstants.MQGMO_SYNCPOINT;
					this.gmo.options = this.gmo.options + MQConstants.MQGMO_WAIT;
					this.gmo.options = this.gmo.options + MQConstants.MQGMO_FAIL_IF_QUIESCING;*/
                    srcQueue.get(myMessage, gmo, 10 * 10 * 1024);

                    this.msgSize = myMessage.getMessageLength() > MAX_MSG_LENGTH ? MAX_MSG_LENGTH : myMessage.getMessageLength();
//					System.out.println(this.msgSize);
                    byte[] b = new byte[this.msgSize];
                    myMessage.readFully(b);

//                    System.out.println(i + "\t" + new String(b));

                    if (this.msgSize == 0 || StringUtils.containsIgnoreCase(new String(b), docId)) {

                        this.gmo.options = this.purgeGMOOptions;
                        try {
                            srcQueue.get(myMessage, this.gmo, 1);
                            count++;
//                            System.out.println(String.format("Purging docId %s ; Count : %d", docId, count));
                        } catch (MQException e) {
                            if (e.completionCode == 1
                                    && acceptTruncatedMsg(e)) {
                                count++;
//                                System.out.println(String.format("Purging docId %s ; Count : %d", docId, count));
                                // Just what we expected!!
//								e.printStackTrace();
                            } else {
                                System.out.println(e.getMessage());
                            }
                        }
                    }

                    myMessage.clearMessage();

                    resetOptionToMoveNext();

                } catch (MQException e) {
                    if (e.completionCode == 1
                            && acceptTruncatedMsg(e)) {
                        // Just what we expected!!
                        e.printStackTrace();
                    } else {
                        loop = false;
                        if (e.completionCode == 2
                                && e.reasonCode == MQConstants.MQRC_NO_MSG_AVAILABLE) {
                            // Good, we are now done - no error!!
                            System.out.println("MQRC_NO_MSG_AVAILABLE");
                        } else {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        closeQueue();
        return count;
    }
	
/*	public MQMessage readSpecificMsgFromQueue() throws IOException, MQException {
		
		queue = this.queueManager.accessQueue(this.queueName, this.openOptions);
		
		MQMessage message = new MQMessage();
		resetMQMessage(message);
		System.out.println(queue.getName());
		queue.get(message, gmo);
		
		closeQueue();
		return message;
	}*/

    public int moveSelectedDocId(String docId) throws MQException, IOException {

//		int openOptions = MQConstants.MQOO_INPUT_AS_Q_DEF + MQConstants.MQOO_INQUIRE + MQConstants.MQOO_FAIL_IF_QUIESCING;
        srcQueue = this.queueManager.accessQueue(this.srcQueueName, this.listOptions);
        MQQueue toQueue = this.queueManager.accessQueue(this.toQueueName, this.writeOptions);
        int count = 0;

        int depth = this.srcQueue.getCurrentDepth();
        System.out.println("Looking for messages :" + docId);
        if (depth > 0) {

            /******************************************************/
            /* Set up our options to browse for the first message */
            /******************************************************/
//			MQGetMessageOptions gmo = new MQGetMessageOptions();
//			this.gmo.options = this.browseOptions | MQConstants.MQGMO_ACCEPT_TRUNCATED_MSG;

            boolean loop = true, strFormat = false, matchingDocId = false;
            MQMessage myMessage = new MQMessage();
            for (int i = 1; i <= depth && loop; i++) {

                try {

					/*MQGetMessageOptions gmo = new MQGetMessageOptions();
				    gmo.options = this. + MQConstants.MQGMO_ACCEPT_TRUNCATED_MSG;*/


//					myMessage.clearMessage();
                    myMessage.clearMessage();
                    myMessage.correlationId = MQConstants.MQCI_NONE;
                    myMessage.messageId = MQConstants.MQMI_NONE;

                    /**************************************************/
                    /* Browse the message, display it, and ask if the */
                    /* message should actually be gotten */
                    /**************************************************/
//					this.gmo.options = this.gmo.options + MQConstants.MQGMO_ACCEPT_TRUNCATED_MSG;;
					/*this.gmo.options = this.gmo.options + MQConstants.MQGMO_SYNCPOINT;
					this.gmo.options = this.gmo.options + MQConstants.MQGMO_WAIT;
					this.gmo.options = this.gmo.options + MQConstants.MQGMO_FAIL_IF_QUIESCING;*/

                    srcQueue.get(myMessage, gmo);
                    System.out.println("Checking message # :" + i);
                    this.msgSize = myMessage.getMessageLength() > MAX_MSG_LENGTH ? MAX_MSG_LENGTH : myMessage.getMessageLength();
                    strFormat = MQConstants.MQFMT_STRING.equals(myMessage.format);
                    if (strFormat) {
                        matchingDocId = StringUtils.containsIgnoreCase(myMessage.readStringOfByteLength(myMessage.getMessageLength()), docId);
                    } else {
                        byte[] b = new byte[this.msgSize];
                        myMessage.readFully(b);
                        matchingDocId = StringUtils.containsIgnoreCase(new String(b), docId);
                    }

                    System.out.println(i + "\t Matching docId found : " + matchingDocId);
//					System.out.println(this.msgSize);


                    if (this.msgSize != 0 && matchingDocId) {

                        toQueue.put(myMessage, this.pmo);

                        this.gmo.options = this.purgeGMOOptions;
                        try {
                            srcQueue.get(myMessage, this.gmo, 1);
                            count++;
                            System.out.println(String.format("Moving docId %s ; Count : %d", docId, count));
                        } catch (MQException e) {
                            if (e.completionCode == 1
                                    && acceptTruncatedMsg(e) || e.getReason() == MQConstants.MQRC_NO_MSG_AVAILABLE) {
                                count++;
                                System.out.println(String.format("Moving docId %s ; Count : %d", docId, count));
                                // Just what we expected!!
//								e.printStackTrace();
                            } else {
                                System.out.println(e.getMessage());
                            }
                        }
                    }
//					myMessage.clearMessage();

                    resetOptionToMoveNext();

                } catch (MQException e) {
                    if (e.completionCode == 1
                            && acceptTruncatedMsg(e)) {
                        // Just what we expected!!
                        e.printStackTrace();
                    } else {
                        loop = false;
                        if (e.completionCode == 2
                                && e.reasonCode == MQConstants.MQRC_NO_MSG_AVAILABLE) {
                            // Good, we are now done - no error!!
                            System.out.println("MQRC_NO_MSG_AVAILABLE");
                        } else {
                            e.printStackTrace();
                        }
                    }
                }
            }
        }
        toQueue.close();
        closeQueue();
        return count;
    }

    public List<MQMessage> browseAllMessagesFromQueue() throws MQException, IOException {
        srcQueue = this.queueManager.accessQueue(this.srcQueueName, this.listOptions);

        List<MQMessage> msgs = new ArrayList<>();

        int count = this.srcQueue.getCurrentDepth() > this.msgReadLimit ? this.msgReadLimit : this.srcQueue.getCurrentDepth();

        gmo.options = this.browseOptions;
        for (int i = 0; i < count; i++) {
            try {
                MQMessage message = new MQMessage();
                //Reset the message object
                message.clearMessage();
                message.correlationId = MQConstants.MQCI_NONE;
                message.messageId = MQConstants.MQMI_NONE;
                // Browse the message, display it
                srcQueue.get(message, gmo);
                msgs.add(message);
                // Reset the options to browse the next message
                resetOptionToMoveNext();
            } catch (MQException e) {
                if (e.completionCode == 1 && acceptTruncatedMsg(e)) {
                    // Just what we expected!!
                } else {

                    if (e.completionCode == 2
                            && e.reasonCode == MQConstants.MQRC_NO_MSG_AVAILABLE) {
                        // Good, we are now done - no error!!
                    } else {
                        e.printStackTrace();
                    }
                    break;
                }
            }
        }
        closeQueue();
        return msgs;
    }

    public MQMessage browseMessageFromQueue() throws IOException, MQException {

        srcQueue = this.queueManager.accessQueue(this.srcQueueName,
                this.openOptions);
        // this.gmo.options = this.purgeGMOOptions;
        MQMessage message = new MQMessage();
        message.clearMessage();
        message.correlationId = this.correlationId;
        message.messageId = this.messageId;
        // System.out.println(queue.getName());
        srcQueue.get(message, gmo);

        closeQueue();
        return message;
    }

    public MQMessage readMessageFromQueue() throws IOException, MQException {

        srcQueue = this.queueManager.accessQueue(this.srcQueueName, this.openOptions);
        this.gmo.options = this.purgeGMOOptions;
        MQMessage message = new MQMessage();
        message.clearMessage();
        message.correlationId = this.correlationId;
        message.messageId = this.messageId;
//		System.out.println(queue.getName());
        srcQueue.get(message, gmo);

        closeQueue();
        return message;
    }

    public void resetMQMessage(MQMessage message) throws IOException {

        /*****************************************/
        /* Reset the message and IDs to be empty */
        /*****************************************/

        message.clearMessage();
//		message.correlationId = MQConstants.MQCI_NONE ;
//		message.messageId = MQConstants.MQMI_NONE ;
        message.correlationId = this.correlationId == null ? MQConstants.MQCI_NONE : this.correlationId;
        message.messageId = this.messageId == null ? MQConstants.MQMI_NONE : this.messageId;
    }

    public void resetOptionToMoveNext() {
        /************************************************/
        /* Reset the options to browse the next message */
        /************************************************/
        gmo.options = MQConstants.MQGMO_WAIT
                | MQConstants.MQGMO_BROWSE_NEXT | MQConstants.MQGMO_ACCEPT_TRUNCATED_MSG;
    }

    private boolean acceptTruncatedMsg(MQException e) {
        return e.reasonCode == MQConstants.MQRC_TRUNCATED_MSG_ACCEPTED;
    }

    public List<byte[]> getAllMessagesFromQueue() throws IOException, MQException {
        // TODO Auto-generated method stub

        int depth = 0;

        List<byte[]> messages = new ArrayList<byte[]>();

        srcQueue = this.queueManager.accessQueue(this.srcQueueName, openOptions);

        depth = srcQueue.getCurrentDepth();

        if (depth > 0) {


            /******************************************************/
            /* Set up our options to browse for the first message */
            /******************************************************/
//				MQGetMessageOptions gmo = new MQGetMessageOptions();
//				gmo.options = this.browseOptions;

            boolean loop = true;
            while (loop) {
                try {
                    MQMessage myMessage = new MQMessage();
                    resetMQMessage(myMessage);

                    /**************************************************/
                    /* Browse the message, display it, and ask if the */
                    /* message should actually be gotten */
                    /**************************************************/

                    srcQueue.get(myMessage, gmo);
                    this.msgSize = this.msgSize <= 0 ? myMessage.getMessageLength() : this.msgSize;
                    byte[] b = new byte[this.msgSize];
                    myMessage.readFully(b);
                    messages.add(b);
                    // System.out.println(new String(b));

                    // gmo.options = MQConstants.MQGMO_MSG_UNDER_CURSOR;
                    // queue.get(myMessage, gmo);

                    resetOptionToMoveNext();

                } catch (MQException e) {
                    if (e.completionCode == 1
                            && acceptTruncatedMsg(e)) {
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
        closeQueue();
        return messages;
    }


}