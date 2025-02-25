package com.myapp.iib.ui.views;

import com.hilerio.ace.AceEditor;
import com.holonplatform.core.Validator;
import com.holonplatform.vaadin.flow.components.Components;
import com.holonplatform.vaadin.flow.components.Input;
import com.holonplatform.vaadin.flow.components.SingleSelect;
import com.holonplatform.vaadin.flow.components.ValidatableInput;
import com.myapp.iib.ui.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.router.Route;
import com.myapp.iib.my.CommonUtils;
import com.myapp.iib.my.MyComponents;
import com.myapp.iib.ui.components.FlexBoxLayout;
import com.myapp.iib.ui.components.MyAceEditor;
import com.myapp.iib.ui.layout.size.Left;
import com.myapp.iib.ui.layout.size.Right;
import lombok.SneakyThrows;
import org.apache.commons.lang3.StringUtils;
import org.eclipse.paho.client.mqttv3.*;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;

import javax.annotation.PostConstruct;
import java.util.Date;

@Route(value = "MQTT", layout = MainLayout.class)

public class MQTT extends ViewFrame {

    private ValidatableInput<String> host;
    private ValidatableInput<Integer> port;
    private Input<String> clientId;
    private ValidatableInput<String> pubTopic;
    private ValidatableInput<String> subTopic;
    private ValidatableInput<String> userName;
    private ValidatableInput<String> password;
    private SingleSelect<Integer> qos;
    private Button btnPublish;
    private Button btnConnect;
    private Button btnDisconnect;
    private Button btnSubscribe;
    private Button btnUnSubscribe;
    private Input<Boolean> retained;
    private Input<Boolean> cleanSession;
    private MqttAsyncClient mqttClient;
    private SingleSelect<String> protocol;
    private MyAceEditor aceEditor;
    private AceEditor leftEditor;
    private AceEditor rightEditor;
    private StringBuffer sb;
    private Dialog dialog;
    private MqttTopic mqttTopic;
    private MqttToken mqttToken;

    private void initialize() {

        sb = new StringBuffer();

        host = Components.input
                .string()
                .label("Host")
                .validatable()
                .withValidator(Validator.notBlank())
                .clearButtonVisible(true)
                .required("Host name cannot be blank")
                .withValue("localhost")
                .build();

        port = Components.input
                .number(Integer.class)
                .label("Port")
                .allowNegative(false)
                .validatable()
                .required("Port name cannot be blank")
                .clearButtonVisible(true)
                .width("10%")
                .withValue(11883)
                .build();

        clientId = Components.input
                .string()
                .label("ClientId")
                .clearButtonVisible(true)
                .withValue(MqttClient.generateClientId())
                .build();

        pubTopic = Components.input
                .string()
                .label("Publish Topic")
                .validatable()
                .withValidator(Validator.notBlank())
                .clearButtonVisible(true)
                .required("Topic name cannot be blank")
                .build();

        subTopic = Components.input
                .string()
                .label("Subscribe Topic")
                .validatable()
                .withValidator(Validator.notBlank())
                .clearButtonVisible(true)
                .required("Subscribe name cannot be blank")
                .build();

        userName = Components.input
                .string()
                .label("User Name")
                .clearButtonVisible(true)
                .validatable()
                .withValidator(Validator.notBlank())
                .required("User Name cannot be blank when protocol is SSL")
                .build();

        password = Components.input
                .secretString()
                .label("Password")
                .clearButtonVisible(true)
                .revealButtonVisible(true)
                .validatable()
                .withValidator(Validator.notBlank())
                .required("Password cannot be blank when protocol is SSL")
                .build();

        qos = Components.input
                .singleSelect(Integer.class)
                .label("QoS")
                .items(0, 1, 2)
                .itemCaption(0, "At most once")
                .itemCaption(1, "At least once")
                .itemCaption(2, "Exactly once")
                .build();

        btnPublish = Components.button()
                .text("Publish")
                .withClickListener(buttonClickEvent -> {
                    publishMsg();
                })
//                .disabled()
                .build();

        btnConnect = Components.button()
                .text("Connect")
                .withClickListener(buttonClickEvent -> {
                    connect();
                })
                .withThemeVariants(ButtonVariant.LUMO_PRIMARY)
                .build();

        btnDisconnect = Components.button()
                .text("Disconnect")
                .withClickListener(buttonClickEvent -> {
                    disconnect();
                })
//                .disabled()
                .build();

        btnSubscribe = Components.button()
                .text("Subscribe")
                .withClickListener(buttonClickEvent -> {
                    subscribehMsg();
                })
//                .disabled()
                .build();

        btnUnSubscribe = Components.button()
                .text("UnSubscribe")
                .withClickListener(buttonClickEvent -> {
                    unSubscribehMsg();
                })
//                .disabled()
                .build();
        retained = Components.input
                .boolean_()
                .label("Retained")
                .withValue(false)
                .build();

        cleanSession = Components.input
                .boolean_()
                .label("Clean Session")
                .withValue(false)
                .build();

        protocol = Components.input
                .singleSelect(String.class)
                .label("Protocol")
                .items("SSL", "TCP")
                .width("10%")
                .withSelectionListener(selectionEvent -> {
                    selectionEvent.getFirstSelectedItem().ifPresent(s -> {
                        if (StringUtils.equals(s, "SSL")) {
                            showCredentialsDialog();
                        }
                    });
                })

                .build();

        protocol.setValue("TCP");
    }

    private void showCredentialsDialog() {

        dialog = Components.dialog
                .question(b -> {
                    if (b) {

                        if (userName.isValid() && password.isValid()) {
                            dialog.close();
                        } else {
                            dialog.open();
                            userName.validate();
                            password.validate();
                        }
                    }
                })
                .modal(true)
                .text("Enter the credentials here")
                .withComponent(Components.formLayout()
                        .add(userName, password).build())
                .open();
    }

    @PostConstruct
    public void init() {
        initialize();
        setViewContent(createContent());
    }

    private Component createContent() {

        FlexBoxLayout content = new FlexBoxLayout();
        content.setSizeFull();
//        content.setSpacing(Bottom.XS);
        content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        content.removePadding();
//        content.setPadding(Top.XS, Bottom.XS);
        content.add(firstRow());
        content.add(secondRow());
        content.add(thirdRow());

        return content;
    }

    private Component firstRow() {

        FlexBoxLayout layout = new FlexBoxLayout();
        layout.setWidthFull();
        layout.setAlignItems(FlexComponent.Alignment.BASELINE);
        layout.removePadding();
        layout.setPadding(Left.M, Right.M);
        layout.setSpacing(Right.XS);
        layout.setFlexDirection(FlexLayout.FlexDirection.ROW);
        layout.add(host.getComponent());
        layout.add(port.getComponent());
        layout.add(clientId.getComponent());
        layout.add(protocol.getComponent());
        layout.add(cleanSession.getComponent(), btnConnect, btnDisconnect);
        return layout;
    }

    private Component secondRow() {
        FlexBoxLayout layout = new FlexBoxLayout();
        layout.setWidthFull();
        layout.setAlignItems(FlexComponent.Alignment.BASELINE);
        layout.removePadding();
        layout.setPadding(Left.M, Right.M);
        layout.setSpacing(Right.XS);
        layout.setFlexDirection(FlexLayout.FlexDirection.ROW);
        layout.add(pubTopic.getComponent(), subTopic.getComponent(), qos.getComponent(), retained.getComponent());
        layout.add(btnPublish, btnSubscribe, btnUnSubscribe);
        return layout;
    }

    private Component thirdRow() {
        aceEditor = new MyAceEditor();
        leftEditor = aceEditor.getLeftEditor();
        rightEditor = aceEditor.getRightEditor();

        return aceEditor.configureAceEditor();
    }

    private String getProtocol() {
        return StringUtils.equals(protocol.getValue(), "TCP") ? "TCP" : "SSL";
    }

    private String getHost() {
        return String.format(getProtocol() + "://%s:%d", host.getValue(), port.getValue());
    }

    private String getClientId() {
        String c = clientId.getValueIfPresent().orElse(MqttClient.generateClientId());
//        c = c.length() == 0 ? MqttClient.generateClientId() : c;
        clientId.setValue(c);
        return c;
    }

    private void initMQTT() {

        try {
            String host = getHost();

            mqttClient = new MqttAsyncClient(host, clientId.getValue(), new MemoryPersistence());
            MqttConnectOptions connOpts = new MqttConnectOptions();
            connOpts.setCleanSession(cleanSession.getValue());
            if (protocol.isSelected("SSL")) {

                if (userName.isValid() && password.isValid()) {
                    String un = userName.getValue();

                    if (un.length() > 0) {
                        connOpts.setUserName(un);
                        connOpts.setPassword(password.getValue().toCharArray());
                    }
                } else {
                    MyComponents.errorNotification("You've not entered the credentials");
                }
            }
            MyComponents.successNotify("Connecting to broker: " + host);
            IMqttToken iMqttToken = mqttClient.connect(connOpts);
            iMqttToken.setActionCallback(new IMqttActionListener() {
                @SneakyThrows
                @Override
                public void onSuccess(IMqttToken iMqttToken) {
                    iMqttToken.waitForCompletion();
                    MyComponents.successNotify(new String(iMqttToken.getResponse().getPayload()));
                    MyComponents.successNotify("Connection successful");
                }

                @Override
                public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                    MyComponents.errorNotification(throwable.getLocalizedMessage());
                }
            });

        } catch (MqttException e) {
            MyComponents.errorNotification(e.getMessage());
            e.printStackTrace();
        }
    }

    private void connect() {

        if (this.mqttClient == null || !this.mqttClient.isConnected()) {
            initMQTT();
           /* if (this.mqttClient.isConnected()) {
                btnConnect.setEnabled(false);
                btnDisconnect.setEnabled(true);
                btnPublish.setEnabled(true);
                if (!btnSubscribe.isEnabled()) {
                    btnSubscribe.setEnabled(true);
                }
            }*/
        } else {
            MyComponents.errorNotification("Unable to connect");

        }
    }

    private void disconnect() {
        if (this.mqttClient != null && this.mqttClient.isConnected()) {
            try {
                this.mqttClient.close();
                this.mqttClient.disconnect();

                MyComponents.successNotify("Disconnected from host " + getHost());

            } catch (MqttException e) {
                MyComponents.errorNotification(e.getMessage());
                e.printStackTrace();
            }
        } else {
            MyComponents.errorNotification("Not connected to host " + getHost());
        }

        /*btnConnect.setEnabled(true);
        btnDisconnect.setEnabled(false);
        btnPublish.setEnabled(false);
        btnSubscribe.setEnabled(false);
        btnUnSubscribe.setEnabled(false);*/
    }

    private void unSubscribehMsg() {
        // TODO Auto-generated method stub
        try {
            this.mqttClient.unsubscribe(subTopic.getValue());
//            btnSubscribe.setEnabled(true);
//            btnUnSubscribe.setEnabled(false);
            MyComponents.successNotify("UnSubscribed from topic " + subTopic.getValue());
        } catch (MqttException e) {
            MyComponents.errorNotification(e.getMessage());
        }
    }

    private void subscribehMsg() {

        subTopic.validate();

        if (subTopic.isValid()) {
            try {
//			MqttClient client = new MqttClient(getHost(), MqttClient.generateClientId());
                mqttClient.setCallback(new MqttCallback() {

                    @Override
                    public void deliveryComplete(IMqttDeliveryToken iMqttDeliveryToken) {
                        String topic = iMqttDeliveryToken.getTopics()[0];
                        String client = iMqttDeliveryToken.getClient().getClientId();

                        try {
                            if (iMqttDeliveryToken.isComplete()) {
                                if (iMqttDeliveryToken.getMessage() != null) {
                                    String message = iMqttDeliveryToken.getMessage().toString();
                                    MyComponents.successNotify("Message to client [" + client + "] under topic (" + topic +
                                            ") was delivered successfully with the delivery message: '" + message + "'");
                                } else {
                                    MyComponents.successNotify("Message to client [" + client + "] under topic (" + topic +
                                            ") was delivered successfully.");
                                }
                            } else {
                                MyComponents.errorNotification("FAILED: Delivery of MQTT message to [" + client + "] under topic [" + topic + "] failed.");
                            }
                        } catch (MqttException e) {
                            MyComponents.errorNotification("Error occurred whilst trying to read the message from the MQTT delivery token.");
                            MyComponents.errorNotification(e.getMessage());
                        }
                    }

                    @Override
                    public void connectionLost(Throwable ex) {
                        MyComponents.errorNotification(ex.getLocalizedMessage());
                        ex.printStackTrace();
                    }

                    @Override
                    public void messageArrived(String topic,
                                               MqttMessage msg) throws Exception {
                        String payload = new String(msg.getPayload());
MyComponents.write(payload);

                        sb.append("--------------------");
                        sb.append(new Date());
                        sb.append("--------------------");
                        sb.append("\n");
                        sb.append("Message Received :");
                        sb.append(payload);

                        rightEditor.setValue(sb.toString());
                        sb.append("\n");
                        sb.append("--------------------------------------------------\n");

                        MyComponents.successNotify(String.format("Message Received from Topic: %s Qos: %d Id: %d Msg Size: %s",
                                subTopic.getValue(), msg.getQos(), msg.getId(), CommonUtils.humanReadableByteCount(payload.length())));
                    }
                });


//			mqttClient.connect();
//			auditMsgPanel.write("Connected to " + getHost());
                IMqttToken iMqttToken = mqttClient.subscribe(subTopic.getValue(), qos.getSelectedItem().orElse(0));
                iMqttToken.waitForCompletion();
                iMqttToken.setActionCallback(new IMqttActionListener() {
                    @SneakyThrows
                    @Override
                    public void onSuccess(IMqttToken iMqttToken) {
                        MyComponents.successNotify(new String(iMqttToken.getResponse().getPayload()));
                    }

                    @Override
                    public void onFailure(IMqttToken iMqttToken, Throwable throwable) {
                        MyComponents.errorNotification(throwable.getLocalizedMessage());
                    }
                });
                /*IMqttToken token = mqttClient.subscribeWithResponse(subTopic.getValue(), qos.getSelectedItem().orElse(0));
                MqttWireMessage wireMessage = token.getResponse();
                MyComponents.successNotify(String.valueOf(wireMessage.getMessageId()));*/

//                btnUnSubscribe.setEnabled(true);
//                btnSubscribe.setEnabled(false);
                MyComponents.successNotify("Subscribed to topic " + subTopic.getValue());
//			mqttClient.disconnect();

            } catch (IllegalArgumentException | MqttException e) {
                MyComponents.errorNotification(e.getMessage());
            }
        } else {
            MyComponents.errorNotification("Unable to subscribe");
        }

//		disconnect();
    }

    private void publishMsg() {

        try {
            pubTopic.validate();
            if (pubTopic.isValid()) {

                if (mqttClient == null || !mqttClient.isConnected()) {
                    connect();
                }

                String content = leftEditor.getValue();
//                mqttTopic = mqttClient.getTopic(pubTopic.getValue());
                mqttClient.publish(pubTopic.getValue(), content.getBytes(), qos.getSelectedItem().orElse(0), retained.getValue());
//                deliveryToken.waitForCompletion();
                MyComponents.successNotify("Content Length : " + CommonUtils.humanReadableByteCount(content.length()) + " is published to "
                        + this.host.getValue() + " using topic name :" + pubTopic.getValue());
            } else {
                MyComponents.errorNotification("Unable to publish message.Check the topic name & connection");
                MyComponents.write(mqttClient.isConnected());
            }

        } catch (MqttException e) {
            MyComponents.errorNotification(e.getMessage());
            e.printStackTrace();
        }

//		disconnect();
    }


}
