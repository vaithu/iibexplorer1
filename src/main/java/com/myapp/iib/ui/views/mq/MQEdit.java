package com.myapp.iib.ui.views.mq;

import com.github.underscore.lodash.U;
import com.hilerio.ace.AceEditor;
import com.hilerio.ace.AceMode;
import com.hilerio.ace.AceTheme;
import com.holonplatform.core.Validator;
import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.vaadin.flow.components.*;
import com.ibm.mq.MQException;
import com.ibm.mq.MQMessage;
import com.ibm.mq.MQQueueManager;
import com.ibm.mq.headers.MQDataException;
import com.myapp.iib.admin.mq.*;
import com.myapp.iib.my.CommonUtils;
import com.myapp.iib.my.MyComponents;
import com.myapp.iib.ui.MainLayout;
import com.myapp.iib.ui.components.FlexBoxLayout;
import com.myapp.iib.ui.views.SplitViewFrame;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.button.ButtonVariant;
import com.vaadin.flow.component.combobox.ComboBox;
import com.vaadin.flow.component.dialog.Dialog;
import com.vaadin.flow.component.formlayout.FormLayout;
import com.vaadin.flow.component.grid.GridVariant;
import com.vaadin.flow.component.html.Div;
import com.vaadin.flow.component.html.H2;
import com.vaadin.flow.component.html.H3;
import com.vaadin.flow.component.html.Label;
import com.vaadin.flow.component.icon.VaadinIcon;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.component.orderedlayout.VerticalLayout;
import com.vaadin.flow.component.tabs.Tab;
import com.vaadin.flow.component.textfield.TextField;
import com.vaadin.flow.component.textfield.TextFieldVariant;
import com.vaadin.flow.data.provider.DataProvider;
import com.vaadin.flow.data.provider.ListDataProvider;
import com.vaadin.flow.data.provider.Query;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.PreserveOnRefresh;
import com.vaadin.flow.router.Route;
import com.myapp.iib.model.mq.DISPLAYQ;
import com.myapp.iib.model.mq.MQEditModel;
import com.myapp.iib.model.mq.QueuesDetails;
import com.myapp.iib.ui.layout.size.Bottom;
import com.myapp.iib.ui.layout.size.Left;
import com.myapp.iib.ui.layout.size.Right;
import com.myapp.iib.ui.util.UIUtils;
import com.myapp.iib.ui.util.css.BoxSizing;
import org.apache.commons.lang3.StringUtils;
import org.vaadin.tabs.PagedTabs;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.*;
import java.util.stream.Collectors;


@PageTitle("MQEdit")
@Route(value = "mqedit", layout = MainLayout.class)
@PreserveOnRefresh
public class MQEdit extends SplitViewFrame {

    private ComboBox<String> inputQ;
    private ComboBox<String> outputQ;
    private ComboBox<String> queueMgrs;
    private ComboBox<String> host;
    private ComboBox<Integer> port;
    private ComboBox<String> channel;
    private Input<Integer> triggerTimes;
    private Input<Integer> mqrc;
    private Input<String> dataSize;
    private Input<Long> qDepth;
    private SingleSelect<String> prettyPrint;
    //    private Checkbox retainInput;
//    private Checkbox get;
//    private Checkbox put;
    private AceEditor msgEditor;
//    private AceEditor auditLogEditor;

    private Path mqEditPropFile;
    private List<String> queueInfo = new ArrayList<>();
    //    private String COMBOBOX_SIZE = "15%";
//    private HorizontalLayout contentArea;
//    private PropertyListing listing;
    private PropertyInputForm qInputForm;
//    private Input<String> searchField;
    private PagedTabs pagedTabs;

    public MQEdit() {

        initialize();
        loadQueueInfo();
        setViewContent(createContent());

    }

    private Component createContent() {
        FlexBoxLayout content = new FlexBoxLayout();
        content.removePadding();
        content.setSpacing(Bottom.XS);
        content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        content.add(firstRow());
        content.add(secondRow());
        content.add(thirdRow());
        content.add(fourthRow());
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setHeightFull();
        content.setPadding(Left.XS, Right.XS);
        return content;
    }

    private HorizontalLayout thirdRow() {

        return Components.hl()
                .spacing()
                .alignItems(FlexComponent.Alignment.BASELINE)
                .add(
                        createLabel("Data Size"), dataSize.getComponent(),
                        createLabel("QDepth"), qDepth.getComponent(),
                        createLabel("PrettyPrint"), prettyPrint.getComponent(),
//                        createLabel("Retain Input"), retainInput,
//                        createLabel("Inhibit"), get, createLabel("GET"),
//                        put, createLabel("PUT"),
                        createLabel("More Options"), moreOptions()
//                        createLabel("Search"),
                )


                .build();
    }

    private Button moreOptions() {

        Button button = Components.button().icon(VaadinIcon.ELLIPSIS_DOTS_V)
                .withThemeName("tertiary")

                .build();
        Components.contextMenu()
                .openOnClick(true)
                .withItem("ShowNonSystemQueues", event -> listQueues(false))
                .withItem("ShowSystemQueues", event -> listQueues(true))
                .withItem("QueueProperties", event -> {
                    showQProperties();
                })
                .withItem("CreateQ", event -> {
                    createQ();
                })
                .withItem("DeleteQ", event -> {
                    deleteQ();
                })
                .withItem("PurgeSelectedMsg", event -> {
                    purgeSelectedMsgs();
                })
                .withItem("MoveSelectedMsg", event -> {
                    moveSelectedMsgs();
                })
                .build(button);

        return button;

    }

    private Component firstRow() {

        qInputForm = Components.input.form(MQEditModel.MQPROPERTIES)
                .initializer(content -> {
                    content.setResponsiveSteps(new FormLayout.ResponsiveStep("0", 1),
                            new FormLayout.ResponsiveStep("300px", 2), new FormLayout.ResponsiveStep("800px", 5));
                })
                .bindField(MQEditModel.INPUTQ, inputQ)
                .bindField(MQEditModel.QMGR, queueMgrs)
                .bindField(MQEditModel.HOSTNAME, host)
                .bindField(MQEditModel.PORT, port)
                .bindField(MQEditModel.CHANNEL, channel)

                .build();

        return qInputForm.getComponent();

    }

    private void readQ(String qName) throws MQException, MQDataException, IOException {

        if (allParametersPresent()) {

            MQHelper mqHelper = new MQHelper(host.getValue(), port.getValue(), channel.getValue());

            MQMessage message = mqHelper.readQ(qName);

            qDepth.setValue(Long.valueOf(mqHelper.getqDepth()));

            if (qDepth.getValue() > 0) {
                msgEditor.setValue(mqHelper.convertMQMessage(message));
                dataSize.setValue(mqHelper.getMsgSize());
//                showEditor(true,"");
            } else {
                MyComponents.errorNotification("No messages found");
            }
        }
    }

    private void writeQ() throws MQException, MQDataException, IOException {

        if (allParametersPresent()) {

            MQHelper mqHelper = new MQHelper(host.getValue(), port.getValue(), channel.getValue());

            mqHelper.writeQ(inputQ.getValue(), msgEditor.getValue(), U.defaultTo(triggerTimes.getValue(), 1));

            dataSize.setValue(CommonUtils.humanReadableByteCount(msgEditor.getValue().length()));
            MyComponents.successNotify(StringUtils.join("Message sent to ", inputQ.getValue(), " in ", queueMgrs.getValue()));
            qDepth.setValue(Long.valueOf(mqHelper.getqDepth()));
        }

    }

    private HorizontalLayout secondRow() {

        return Components.hl()
                .alignItems(FlexComponent.Alignment.BASELINE)
                .spacing()
//               .margin()
                .fullWidth()
                .add(Components.button("ReadQ", click -> {

                    try {
                        readQ(inputQ.getValue());
                    } catch (MQException | MQDataException | IOException e) {
                        errorNotify(e);
                    }

                }))
                .add(Components.button("WriteQ", buttonClickEvent -> {
                    try {
                        writeQ();
                    } catch (MQException | MQDataException | IOException e) {
                        errorNotify(e);
                    }
                }))
                .add(Components.button("PurgeQ", buttonClickEvent -> {

                    Components.dialog
                            .question(confirmSelected -> {
                                if (confirmSelected) {
                                    purgeQ();
                                }
                            })
                            .text("Are you sure want to purge?")
                            .confirmButtonConfigurator(baseButtonConfigurator -> {
                                baseButtonConfigurator.icon(VaadinIcon.TRASH)
                                        .withThemeVariants(ButtonVariant.LUMO_ERROR)
                                        .iconAfterText(true)
                                        .text("Yes. PurgeQ");
                            })
                            .open();

                }))
                .add(Components.button("LoadQNames", buttonClickEvent -> {

                    if (validConnection()) {
                        try {
                            List<String> queues = MQExplorer.listQueues(
                                    new String[]
                                            {this.host.getValue(), String.valueOf(this.port.getValue()), this.channel.getValue()});
                            inputQ.setItems(queues);
                            outputQ.setItems(queues);
                            MyComponents.successNotify("Queues loaded");
                        } catch (MQException | MQDataException | IOException e) {
                            errorNotify(e);
                        }
                    }

                }))
                .add(Components.button("DisplayQ", buttonClickEvent -> {
                    try {
                        displayQ();
                    } catch (MQException | MQDataException e) {
                        errorNotify(e);
                    }
                }))
                .add(Components.button("MoveQ", buttonClickEvent -> {

                    moveQ();

                }))
                .addAndExpand(triggerTimes, 1)
                .addAndExpand(mqrc, 1)
                .add(Components.button("Explain RC", buttonClickEvent -> {
                    mqrc.getValueIfPresent().ifPresent(i -> {
                        Components.dialog
                                .showMessage(MQReasonCodes.getReason(i));
                    });
                }))
                .add(createLabel("Output Q"))
                .addAndExpand(outputQ, 1)
                .add(Components.button("Read Q", buttonClickEvent -> {

                    try {
                        readQ(outputQ.getValue());
                    } catch (MQException | MQDataException | IOException e) {
                        errorNotify(e);
                    }
                }))
                .add(Components.button("Switch QNames", buttonClickEvent -> {

                    inputQ.getOptionalValue().ifPresent(s -> {
                        outputQ.getOptionalValue().ifPresent(s1 -> {
                            inputQ.setValue(s1);
                            outputQ.setValue(s);
                        });
                    });

                }))
                .build();


    }

    private boolean allParametersPresent() {
        this.qInputForm.validate();
        return this.qInputForm.isValid();
    }

    private void purgeQ() {

        if (allParametersPresent()) {

            try {
                MQOperations mqOperations = new MQOperations();

                mqOperations.hostName(host.getValue())
                        .port(port.getValue())
                        .channelName(channel.getValue())
                        .connect()
                        .srcQueueName(inputQ.getValue())
                        .purgeQ()
                        .disconnect();

                MyComponents.successNotify("Purge Completed");
            } catch (MQException | MQDataException | IOException e) {
                errorNotify(e);
            }
        }
    }


    private void moveQ() {

        if (allParametersPresent()) {

            ValidatableInput<String> tgtQName = Components.input
                    .string()
                    .label("Target Queue")
                    .validatable()
                    .required("Name cannot be empty")
                    .withValue(StringUtils.endsWithIgnoreCase(inputQ.getValue(), "FAILURE") ?
                            StringUtils.substringBefore(inputQ.getValue(), ".FAIL") : inputQ.getValue())
                    .withValidator(Validator.min(1))
                    .withValidator(Validator.notBlank())
                    .clearButtonVisible(true)
                    .build();

            List<String> dataProvider = this.queueMgrs.getDataProvider().fetch(new Query<>()).collect(Collectors.toList());

            ValidatableSingleSelect<String> targetQMgrs = Components.input.singleSelect(String.class)
                    .dataSource(DataProvider.ofCollection(dataProvider))
                    .label("Target QMgr")
                    .required()
                    .validatable()
//                    .validateOnValueChange(true)
                    .withValidator(Validator.notBlank())
                    .withValidator(Validator.notEmpty())
                    .withValidator(Validator.min(1))
                    .clearButtonVisible(true)
                    .build();

            Input<Integer> msgCount = Components.input.number(Integer.class)
                    .label("No of Messages to move ")
                    .withValue(-1)
                    .description("-1 means all messages")
                    .withThemeVariants(TextFieldVariant.LUMO_ALIGN_RIGHT)
                    .build();


            FormLayout formLayout = Components.formLayout()
                    .add(Components.input.string()
                            .label("Source Q")
                            .withValue(inputQ.getValue())
                            .readOnly().build())
                    .add(Components.input.string()
                            .label("Source QMgr")
                            .withValue(queueMgrs.getValue())
                            .readOnly().build())
                    .add(tgtQName, targetQMgrs)
                    .add(msgCount)
                    .sizeUndefined()
                    .build();

//            formLayout.setColspan(msgEditor, 2);

            Button yes = MyComponents.yesBtn();
            Button no = MyComponents.noBtn();

            HorizontalLayout horizontalLayout = MyComponents.dialogFooter(yes, no);

            Dialog dialog = Components.dialog
                    .message()
                    .width("25%")
                    .withComponent(formLayout)
                    .withComponent(new H2("Move Q"))
                    .withToolbarComponent(horizontalLayout)
                    .open();

            no.addClickListener(e -> dialog.close());

            yes.addClickListener(e -> {

                tgtQName.validate();
                targetQMgrs.validate();

                if (targetQMgrs.isValid() && tgtQName.isValid()) {
                    queueInfo.stream().filter(s -> StringUtils.containsIgnoreCase(s, targetQMgrs.getValue()))
                            .findFirst().ifPresent(tgtQmgrDetails -> {
                        MQOperations mqOperations = new MQOperations();
                        try {
                            String[] con = StringUtils.split(tgtQmgrDetails, ";");
                            MQQueueManager toMQgr = MQExplorer.getRemoteQueueManager(con[1], con[3], Integer.parseInt(con[2]));

                            mqOperations.hostName(host.getValue())
                                    .port(port.getValue())
                                    .channelName(channel.getValue())
                                    .connect();

                            int movedMsgCount = mqOperations.moveAllMsgsQ(mqOperations.queueManager(), inputQ.getValue(), toMQgr, tgtQName.getValue(), msgCount.getValue());
                            mqOperations.disconnect();
                            MQExplorer.disconnect(toMQgr);
                            MyComponents.successNotify(movedMsgCount + " messages moved");

                        } catch (MQException | MQDataException | IOException ex) {
                            ex.printStackTrace();
                            errorNotify(ex);
                        }
                    });

                    dialog.close();
                }
            });
        }
    }


    private void moveSelectedMsgs() {

        if (allParametersPresent()) {

            ValidatableInput<String> tgtQName = Components.input
                    .string()
                    .label("Target Queue")
                    .validatable()
                    .required("Name cannot be empty")
                    .withValidator(Validator.min(1))
                    .withValidator(Validator.notBlank())
                    .clearButtonVisible(true)
                    .build();

            ValidatableInput<String> root_element_name = Components.input.string()
                    .validatable()
                    .validateOnValueChange(true)
                    .withValidator(Validator.notBlank())
                    .label("Root Element Name")
                    .clearButtonVisible(true)
                    .required()
                    .build();

            Input<Integer> msgCount = Components.input.number(Integer.class)
                    .label("No of Messages to move ")
                    .withValue(-1)
                    .description("-1 means all messages")
                    .withThemeVariants(TextFieldVariant.LUMO_ALIGN_RIGHT)
                    .build();


            FormLayout formLayout = Components.formLayout()
                    .add(Components.input.string()
                            .label("Source Q")
                            .withValue(inputQ.getValue())
                            .readOnly().build())
                    .add(Components.input.string()
                            .label("Source QMgr")
                            .withValue(queueMgrs.getValue())
                            .readOnly().build())
                    .add(tgtQName)
                    .add(root_element_name)
                    .add(msgCount)
                    .sizeUndefined()
                    .build();

//            formLayout.setColspan(msgEditor, 2);

            Button yes = MyComponents.yesBtn();
            Button no = MyComponents.noBtn();

            HorizontalLayout horizontalLayout = MyComponents.dialogFooter(yes, no);

            Dialog dialog = Components.dialog
                    .message()
                    .width("25%")
                    .withComponent(formLayout)
                    .withComponent(new H3("Move Selected Message(s)"))
                    .withToolbarComponent(horizontalLayout)
                    .open();

            no.addClickListener(e -> dialog.close());

            yes.addClickListener(e -> {

                tgtQName.validate();
                root_element_name.validate();

                if (tgtQName.isValid() && root_element_name.isValid()) {
                    MQOperations mqOperations = new MQOperations();
                    try {

                        mqOperations.hostName(host.getValue())
                                .port(port.getValue())
                                .channelName(channel.getValue())
                                .connect()
                                .srcQueueName(inputQ.getValue())
                                .toQueueName(tgtQName.getValue());

                        int movedMsgCount = mqOperations
                                .moveSelectedDocId(root_element_name.getValue());
                        mqOperations.disconnect();
                        MyComponents.successNotify(movedMsgCount + " messages moved");

                    } catch (MQException | MQDataException | IOException ex) {
                        ex.printStackTrace();
                        errorNotify(ex);
                    }


//                    mqOperations.

                    dialog.close();
                }
            });
        }
    }

    private Component fourthRow() {
        VerticalLayout vl = new VerticalLayout();
        vl.setSpacing(false);
        vl.setPadding(false);
        pagedTabs = new PagedTabs(vl);
        pagedTabs.add("Editor", msgEditor);
        return new Div(pagedTabs, vl);
    }

    private void purgeSelectedMsgs() {

        if (allParametersPresent()) {

            ValidatableInput<String> root_element_name = Components.input
                    .string()
                    .required()
                    .validatable()
                    .withValidator(Validator.notBlank())
                    .withValidator(Validator.min(1))
                    .label("Root Element Name")
                    .clearButtonVisible(true)
                    .build();

            Button yes = MyComponents.yesBtn();
            Button no = MyComponents.noBtn();
            HorizontalLayout horizontalLayout = MyComponents.dialogFooter(yes, no);
            Dialog dialog = Components.dialog
                    .message()
                    .withComponent(root_element_name)
                    .withToolbarComponent(horizontalLayout)
                    .open();

            no.addClickListener(buttonClickEvent -> dialog.close());

            yes.addClickListener(buttonClickEvent -> {

                root_element_name.validate();

                if (root_element_name.isValid()) {
                    MQOperations mqOperations = new MQOperations();
                    try {
                        int msgCount = mqOperations.hostName(host.getValue())
                                .port(port.getValue())
                                .channelName(channel.getValue())
                                .connect()
                                .srcQueueName(inputQ.getValue())
                                .purgeSelectedDocId(root_element_name.getValue());

                        mqOperations.disconnect();
                        MyComponents.successNotify(msgCount + " messages purged");
                        dialog.close();
                    } catch (MQException | MQDataException | IOException e) {
                        e.printStackTrace();
                        errorNotify(e);
                    }

                }

            });

        }

    }

    private void listQueues(boolean system) {

        Input<String> searchField = MyComponents.searchField("Search");

        if (validConnection()) {

            ListDataProvider<PropertyBox> dataProvider = null;
            try {

                dataProvider = new ListDataProvider<>(
                        new MQHelper(host.getValue(), port.getValue(), channel.getValue()).listQDetails(system));
                dataProvider.addFilter(propertyBox -> {
                    return searchField.getValue() == null ? true : StringUtils.containsIgnoreCase(propertyBox.getValue(QueuesDetails.Q_NAME), searchField.getValue());
                });

//                i = 0;
                MyComponents.successNotify("Showing Q details " + queueMgrs.getValue());
                PropertyListing listing = Components.listing.properties(QueuesDetails.PROPERTY_SET)
                        .sizeUndefined()
                        .columnReorderingAllowed(true)
                        .resizable(true)
                        .dataSource(dataProvider)
                        .renderAsViewComponent(QueuesDetails.SNO)
                        .hidden(QueuesDetails.MAX_Q_DEPTH)
                        .hidden(QueuesDetails.MAX_MSG_LENGTH)
                        .hidden(QueuesDetails.INHIBIT_PUT)
                        .hidden(QueuesDetails.INHIBIT_GET)
                        .columnsAutoWidth()
                        .itemDetailsText(propertyBox -> {
                            return QueuesDetails.MAX_Q_DEPTH.getName() + " : " + propertyBox.getValue(QueuesDetails.MAX_Q_DEPTH) +";  "+
                                    QueuesDetails.MAX_MSG_LENGTH.getName() + " : " + propertyBox.getValue(QueuesDetails.MAX_MSG_LENGTH) +";  "+
                                    QueuesDetails.INHIBIT_PUT.getName() + " : " + propertyBox.getValue(QueuesDetails.INHIBIT_PUT) +";  "+
                                    QueuesDetails.INHIBIT_GET.getName() + " : " + propertyBox.getValue(QueuesDetails.INHIBIT_GET);
                        })
                        .autoWidth(QueuesDetails.Q_NAME)
                        .withThemeVariants(GridVariant.LUMO_COMPACT,
                                GridVariant.LUMO_WRAP_CELL_CONTENT,
                                GridVariant.LUMO_COLUMN_BORDERS)
                        .build();
//                showEditor(false,"Search By Queue Name");
                searchField.addValueChangeListener(e -> listing.refresh());
                MyComponents.addToTab(pagedTabs,"Q Details "+queueMgrs.getValue(),new VerticalLayout(searchField.getComponent(),listing.getComponent()),true);
//                pagedTabs.add("Q Details",new VerticalLayout(searchField.getComponent(),listing.getComponent()))
//                .setSelected(true);
            } catch (MQDataException | IOException e) {
                e.printStackTrace();
                errorNotify(e);
            }

        }
    }



    private boolean validConnection() {
        return MyComponents.validateComponent(host)
                && MyComponents.validateComponent(port) && MyComponents.validateComponent(channel);
    }

    private void createQ() {

        ValidatableInput<String> queueName = Components.input.string()
                .label("Queue Name")
                .autofocus(true)
                .blankValuesAsNull(true)
                .emptyValuesAsNull(true)
                .validatable()
                .clearButtonVisible(true)
                .required("Queue name cannot be blank")
                .build();

        if (validConnection()) {

            Button yes = MyComponents.yesBtn();
            Button no = MyComponents.noBtn();

            HorizontalLayout horizontalLayout = MyComponents.dialogFooter(yes, no);

            Dialog dialog = Components.dialog
                    .message()
                    .withComponent(queueName)
                    .withToolbarComponent(horizontalLayout)
                    .text("Enter the queue name")
                    .open();

            no.addClickListener(e -> dialog.close());
            yes.addClickListener(event -> {
                if (queueName.isValid()) {
                    MQHelper mqHelper = new MQHelper(host.getValue(), port.getValue(), channel.getValue());
                    try {
                        int rc = mqHelper.createQueue(queueName.getValue());
                        if (rc == 0) {
                            MyComponents.successNotify("Queue created successfully");
                        } else {
                            MyComponents.errorNotification("Unable to create queue");
                        }
                    } catch (MQDataException | IOException | MQException e) {
                        e.printStackTrace();
                        MyComponents.errorNotification(e.getMessage());
                    }
                    dialog.close();
                }
            });
        }
    }

    private void deleteQ() {

        ValidatableInput<String> queueName = Components.input.string()
                .label("Queue Name")
                .autofocus(true)
                .blankValuesAsNull(true)
                .emptyValuesAsNull(true)
                .validatable()
                .clearButtonVisible(true)
                .required("Queue name cannot be blank")
                .build();

        if (validConnection()) {

            Button yes = MyComponents.trashButton();
            Button no = MyComponents.noBtn();

            HorizontalLayout horizontalLayout = MyComponents.dialogFooter(yes, no);

            Dialog dialog = Components.dialog
                    .message()
                    .withComponent(queueName)
                    .withToolbarComponent(horizontalLayout)
                    .text("Enter the queue name to delete")
                    .open();

            no.addClickListener(e -> dialog.close());
            yes.addClickListener(event -> {
                if (queueName.isValid()) {
                    PCFCommons commons = new PCFCommons();
                    try {
                        UIUtils.showNotification(commons.deleteQueue(host.getValue(), port.getValue(), queueName.getValue(), channel.getValue()));
                    } catch (MQDataException | IOException e) {
                        e.printStackTrace();
                        MyComponents.errorNotification(e.getMessage());
                    }
                    dialog.close();
                }
            });
        }
    }



    private void showQProperties() {
        try {
            queueProperties();
        } catch (MQException | MQDataException e) {
//                       errorNotify(e);
            errorNotify(e);
        }
    }


    private ValidatableSingleSelect<String> singleSelect(String label) {

        return Components.input.singleSelect(String.class)
                .label(label)
                .clearButtonVisible(true)
                .allowCustomValue(true)
                .validatable()
                .withValidator(Validator.notBlank())
                .build();

    }

    private ComboBox<String> createComboBox() {
        ComboBox<String> comboBox = new ComboBox<>();
        comboBox.setClearButtonVisible(true);
        comboBox.setAllowCustomValue(true);
        comboBox.addCustomValueSetListener(event -> {
            comboBox.setValue(event.getDetail().trim().toUpperCase());
        });

        return comboBox;
    }

    private void initialize() {

        inputQ = createComboBox();
//        inputQ.setWidth(COMBOBOX_SIZE);
        outputQ = createComboBox();
//        outputQ.setWidth(COMBOBOX_SIZE);
        host = createComboBox();
//        host.setWidth(COMBOBOX_SIZE);
        queueMgrs = createComboBox();

        queueMgrs.addValueChangeListener(event -> {
            updateConnectionDetails(event.getValue());
        });
        port = new ComboBox<>();
        port.setPreventInvalidInput(true);
        port.addCustomValueSetListener(e -> port.setValue(Integer.parseInt(e.getDetail())));
//        port.setWidth("90px");
        port.setAllowCustomValue(true);
        channel = createComboBox();
        channel.setSizeUndefined();
        triggerTimes = Components.input.number(Integer.class)
                .width("60px")
                .allowNegative(false)
                .clearButtonVisible(true)
                .validatable()
                .withValidator(Validator.max(100))
                .validateOnValueChange(true)
                .description("No of time to trigger message")
                .placeholder("Trigger Times")
                .build();

        qDepth = Components.input.number(Long.class)
                .width("60px")
                .allowNegative(false)
                .readOnly()
                .build();

        mqrc = Components.input.number(Integer.class)
                .allowNegative(false)
                .width("60px")
                .clearButtonVisible(true)
                .placeholder("MQRC")
                .build();

        dataSize = Components.input.string()
                .width("100px")
                .readOnly()
                .build();

        prettyPrint = Components.input.singleOptionSelect(String.class)
                .items("XML", "JSON", "Text")
                .withValueChangeListener(val -> {
                    if (val != null && val.getValue() != null) {
                        prettyPrintMsg(val.getValue());
                    }
                })
//                .withSelectionListener(selectionEvent -> prettyPrintMsg(selectionEvent.getFirstSelectedItem().get()))
                .build();

       /* retainInput = new Checkbox();
        get = new Checkbox();
        put = new Checkbox();*/

        msgEditor = new AceEditor();
        msgEditor.setTheme(AceTheme.eclipse);
        msgEditor.setMode(AceMode.xml);
//msgEditor.setMaxHeight("100%");
        msgEditor.setHeight("500px");
//        msgEditor.setMinlines(150);
        msgEditor.setWrap(true);
//        msgEditor.setMaxlines(50);

        /*auditLogEditor = new AceEditor();
        auditLogEditor.setWidth("100%");
        auditLogEditor.setMinlines(8);
        auditLogEditor.setTheme(AceTheme.eclipse);
        auditLogEditor.setMode(AceMode.text);
        auditLogEditor.setMaxlines(8);*/

        /*this.listing = Components.listing.properties(MQEditModel.MQPROPERTIES)
                .fullSize()
                .build();*/

    }

    private void prettyPrintMsg(String value) {

        msgEditor.getOptionalValue().ifPresent(s -> {
//            successNotify(value);
            switch (value) {
                case "XML":
                    msgEditor.setMode(AceMode.xml);
                    msgEditor.setValue(U.formatXml(s));
                    break;
                case "JSON":
                    msgEditor.setMode(AceMode.json);
                    msgEditor.setValue(U.formatJson(s));
                    break;
                default:
                    msgEditor.setMode(AceMode.text);
            }
        });
        if (value != null) {
            prettyPrint.deselect(value);
        }
    }

    private Label createLabel(String labelName) {
        return new Label(labelName);
    }

    private void loadQueueInfo() {

        this.mqEditPropFile = Paths.get(System.getProperty("user.home"), "QueueInfo.txt");

        if (Files.exists(this.mqEditPropFile)) {
            try {
                this.queueInfo = Files.readAllLines(mqEditPropFile);
                Set<String> host = new TreeSet<>();
                Set<Integer> port = new TreeSet<>();
                Set<String> channel = new TreeSet<>();
                Set<String> qMgr = new TreeSet<>();

                queueInfo.stream().filter(s -> !s.trim().isEmpty()).forEach(s -> {

                    String[] str = s.split(";");

                    switch (str.length) {
                        case 4:
                            qMgr.add(str[0]);
                            host.add(str[1]);
                            port.add(Integer.parseInt(str[2]));
                            channel.add(str[3]);
                            break;
                        case 3:
                            host.add(str[0]);
                            port.add(Integer.parseInt(str[1]));
                            channel.add(str[2]);
                            break;

                        case 1:
                            qMgr.add(str[0]);
                            break;
                    }

                    this.queueMgrs.setItems(qMgr);
                    this.queueMgrs.setPageSize(5);
                    this.host.setPageSize(5);
                    this.host.setItems(host);

                    this.port.setItems(port);
                    this.channel.setItems(channel);
                });
            } catch (IOException e) {
//           errorNotify(e);
                System.err.println("Check QueueInfo.txt in " + System.getProperty("user.home"));
                errorNotify(e);
            }
        }



    }

    private void errorNotify(Exception e) {
        MyComponents.errorNotification(e.getMessage());
    }

    /* private void write(String msg) {

     *//*if (!auditLogEditor.getValue().trim().isEmpty()) {
            auditLogEditor.setValue(StringUtils.join(auditLogEditor.getValue(), "\n", CommonUtils.getCurrentTime(), " : ", msg));
        } else {
            auditLogEditor.setValue(StringUtils.join(CommonUtils.getCurrentTime(), " : ", msg, "\n"));
        }*//*
        successNotify(StringUtils.join(CommonUtils.getCurrentTime(), " : ", msg));
    }*/

    private void updateConnectionDetails(String qMgrName) {

        if (!this.queueInfo.isEmpty()) {
            for (String line : this.queueInfo) {

                String[] con = StringUtils.split(line, ";");
                if (con.length == 4 && con[0].equals(qMgrName)) {

                    this.host.setValue(con[1]);
                    this.port.setValue(Integer.parseInt(con[2]));
                    this.channel.setValue(con[3]);
                    break;
                }
            }
        }
    }

    private void queueProperties() throws MQException, MQDataException {

        if (allParametersPresent()) {

            MQHelper mqHelper = new MQHelper(host.getValue(), port.getValue(), channel.getValue());
            Map<String, String> map = mqHelper.queueProperties(inputQ.getValue());

            FormLayout layout = new FormLayout();
            layout.setResponsiveSteps(MyComponents.getResponsiveStep("350px",3));
//            layout.setSizeUndefined();

            final TextField[] textField = new TextField[1];
            int i = 1;
            for (Map.Entry<String, String> entry : map.entrySet()) {

                textField[0] = new TextField(entry.getKey(), entry.getValue().trim(), "");
                textField[0].setReadOnly(true);
                layout.add(textField[0]);

                if (i == 1) {
                    i++;
                    layout.setColspan(textField[0], 2);
                }
            }

//            layout.setColspan(textField[0],2);

//            layout.add(new HorizontalLayout(new Divider()));

            Button ok = Components.button().text("OK")
                    .autofocus()
                    .build();
            Dialog dialog = Components.dialog.message()
                    .width("50%")
                    .withComponent(layout)
                    .withComponent(new H2("Queue Properties"))
                    .withToolbarComponent(ok)
                    .open();

            dialog.setResizable(true);
            dialog.setDraggable(true);

            ok.addClickListener(e -> dialog.close());

//            dialog.setResizable(true);

            /*Components.dialog.message()
                    .text("Queue Properties :" + inputQ.getValue())
//                    .withComponent(new Button("Hello"))
                    .withComponent(
                            Components.input.form()
                                    .initializer(formLayout -> {
                                        try {
                                            formLayout.add(new TextField("Queue Name", mqQueue.getName(), ""));
                                            formLayout.setResponsiveSteps(new FormLayout.ResponsiveStep("50%", 1));
                                        } catch (MQException e) {
                                            e.printStackTrace();
                                            errorNotify(e.getMessage());
                                        }
                                    })
                                    .build().getComponent()
                    )
                    .build()
                    .open();
*/

        }

    }

    /*private Input<String> serachField() {
      return  Components.input.string()
                .fullWidth()
                .blankValuesAsNull(true)
                .emptyValuesAsNull(true)
                .prefixComponent(VaadinIcon.SEARCH.create())
                .valueChangeMode(ValueChangeMode.LAZY)
                .clearButtonVisible(true)
                .withValueChangeListener(event -> {
                    this.listing.refresh();
                })
                .placeholder("Search")
                .build();
    }*/



    private void displayQ() throws MQException, MQDataException {

        if (allParametersPresent()) {

            Input<String> searchField = MyComponents.searchField("Search");

            MQHelper mqHelper = new MQHelper(host.getValue(), port.getValue(), channel.getValue());
            final int depth = mqHelper.getqDepth(inputQ.getValue());
            qDepth.setValue(Long.valueOf(depth));
            if (depth == 0) {
                MyComponents.errorNotification("No messages found in queue");
            } else {
                Components.dialog.showQuestion(confirmSelected -> {
                    int N = confirmSelected || 10 > depth ? depth : 10;
                    MyComponents.successNotify("Reading " + N + " messages");
                    try {
                        MyComponents.successNotify("Displaying Q");
//                        List<PropertyBox> propertyBoxes = new MQHelper(host.getValue(), port.getValue(), channel.getValue()).displayQueue(inputQ.getValue(), 10);
//                        propertyBoxes.forEach(propertyBox -> System.out.println(propertyBox));

                        ListDataProvider<PropertyBox> dataProvider = new ListDataProvider<>(
                                new MQHelper(host.getValue(), port.getValue(), channel.getValue()).displayQueue(inputQ.getValue(),
                                        N));
                        dataProvider.addFilter(propertyBox -> {
                            return searchField.getValue() == null ? true : StringUtils.containsIgnoreCase(propertyBox.getValue(DISPLAYQ.MSGSIZE), searchField.getValue());
                        });

//                        i = 0;
//                        final AtomicInteger fired = new AtomicInteger(0);

                        PropertyListing listing = Components.listing.properties(DISPLAYQ.PROPERTIES)
//                                .fullSize()
                                .columnReorderingAllowed(true)
                                .resizable(true)
                                .hidden(DISPLAYQ.CORRL_ID)
                                .hidden(DISPLAYQ.USERID)
                                .dataSource(dataProvider)
                                .renderAsViewComponent(DISPLAYQ.SNO)
//                                .valueProvider(DISPLAYQ.SNO,propertyBox -> String.valueOf(propertyBox.getValue(DISPLAYQ.SNO)))
                                /*.withColumn(propertyBox -> ++i)
                                .header("S.No")
                                .width("80px")
                                .displayAsFirst()
                                .add()*/
//                                .frozen(DISPLAYQ.SNO,true)
                                .autoWidth(DISPLAYQ.MSG_ID)
                                .autoWidth(DISPLAYQ.PUT_APPLN)
                                .autoWidth(DISPLAYQ.PUTTIME)
                                .withComponentColumn(propertyBox -> {
                                    return Components.button()
                                            .icon(VaadinIcon.EYE)
                                            .iconAfterText(true)
                                            .withThemeVariants(ButtonVariant.LUMO_SUCCESS)
                                            .onClick(event -> {
                                                showMsg(propertyBox.getValue(DISPLAYQ.MSG_ID));
                                            })
                                            .build();
                                })
                                .header("ShowMsg")
                                .add()
                                .withComponentColumn(propertyBox -> {
                                    return Components.button()
                                            .icon(VaadinIcon.TRASH)
                                            .iconAfterText(true)
                                            .withThemeVariants(ButtonVariant.LUMO_ERROR)
                                            .onClick(event -> {
                                                Components.dialog
                                                        .question(okToDelete -> {
                                                            if (okToDelete) {
                                                                try {
                                                                    purgeSpecificMsg(propertyBox.getValue(DISPLAYQ.MSG_ID), propertyBox.getValue(DISPLAYQ.CORRL_ID));
                                                                    MyComponents.successNotify("Message deleted successfully");
                                                                    displayQ();
                                                                } catch (MQException | MQDataException | IOException e) {
                                                                    e.printStackTrace();
                                                                    errorNotify(e);
                                                                }
                                                            }
                                                        })
                                                        .text("Are you sure?")
                                                        .open();
                                            })
                                            .build();
                                })
                                .header("Purge")
                                .add()
                                .itemDetailsText(propertyBox -> {
                                    return "CorrelationId :" + propertyBox.getValue(DISPLAYQ.CORRL_ID);
                                })
//                                .fullSize()
                                .withThemeVariants(GridVariant.LUMO_COMPACT, GridVariant.LUMO_WRAP_CELL_CONTENT, GridVariant.LUMO_COLUMN_BORDERS)
                                .build();
                        searchField.addValueChangeListener(e -> listing.refresh());
                        pagedTabs.add(inputQ.getValue(), new VerticalLayout(searchField.getComponent(), listing.getComponent()));
//                        showEditor(false,"Search By Message Size");
                    } catch (MQException | IOException | MQDataException e) {
                        e.printStackTrace();
                        errorNotify(e);
                    }


                }, "There are " + depth
                        + "messages in the queue. Do you want to read all ? The default is 10");
            }

        }


//        this.contentArea.add(this.listing.getComponent());

//        setShowEditor(false);
    }

    private void purgeSpecificMsg(String msgId, String corrlId) throws MQException, MQDataException, IOException {

        if (allParametersPresent()) {
            MQOperations mqOperations = new MQOperations();
            mqOperations.hostName(host.getValue())
                    .port(port.getValue())
                    .channelName(channel.getValue())
                    .connect()
                    .srcQueueName(inputQ.getValue())
                    .messageId(CommonUtils.hexStringToByteArray(msgId))
                    .correlationId(CommonUtils.hexStringToByteArray(corrlId))
                    .purgeSpecificMsg()
                    .disconnect();
        }

    }

    private void showMsg(String msgId) {

        try {
            MQQueueManager queueManager = MQExplorer.getRemoteQueueManager(host.getValue(), channel.getValue(), port.getValue());
            String oldText = MQExplorer.
                    convertMQMessageToString(MQExplorer.
                            GetMessage(queueManager, inputQ.getValue(), CommonUtils.hexStringToByteArray(msgId)));
            MQExplorer.disconnect(queueManager);
            dataSize.setValue(CommonUtils.humanReadableByteCount(oldText.length()));

            msgEditor.setValue(oldText);
            Tab editor = pagedTabs.get("Editor");
                    editor.setSelected(true);
            pagedTabs.select(editor);
//            showEditor(true,"");
        } catch (MQException | MQDataException | IOException e) {
            e.printStackTrace();
            errorNotify(e);
        }


    }



}
