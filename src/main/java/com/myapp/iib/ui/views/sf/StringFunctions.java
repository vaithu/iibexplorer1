package com.myapp.iib.ui.views.sf;

import com.github.underscore.lodash.U;
import com.hilerio.ace.AceEditor;
import com.hilerio.ace.AceMode;
import com.hilerio.ace.AceTheme;
import com.holonplatform.vaadin.flow.components.Components;
import com.holonplatform.vaadin.flow.components.SingleSelect;
import com.holonplatform.vaadin.flow.components.ValidatableInput;
import com.holonplatform.vaadin.flow.components.ValidatableSingleSelect;
import com.myapp.iib.ui.MainLayout;
import com.vaadin.flow.component.Component;
import com.vaadin.flow.component.button.Button;
import com.vaadin.flow.component.notification.Notification;
import com.vaadin.flow.component.notification.NotificationVariant;
import com.vaadin.flow.component.orderedlayout.FlexComponent;
import com.vaadin.flow.component.orderedlayout.FlexLayout;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import com.vaadin.flow.router.PageTitle;
import com.vaadin.flow.router.Route;
import com.myapp.iib.ui.components.FlexBoxLayout;
import com.myapp.iib.ui.layout.size.Horizontal;
import com.myapp.iib.ui.layout.size.Top;
import com.myapp.iib.ui.util.css.BoxSizing;
import com.myapp.iib.ui.views.ViewFrame;
import org.apache.commons.codec.DecoderException;
import org.apache.commons.codec.binary.Base64;
import org.apache.commons.codec.binary.Hex;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Source;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.stream.StreamResult;
import javax.xml.transform.stream.StreamSource;
import javax.xml.validation.Schema;
import javax.xml.validation.SchemaFactory;
import javax.xml.validation.Validator;
import javax.xml.xpath.XPath;
import javax.xml.xpath.XPathConstants;
import javax.xml.xpath.XPathExpressionException;
import javax.xml.xpath.XPathFactory;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * The main view contains a simple label element and a template element.
 */
@PageTitle("String Functions")
@Route(value = "sf", layout = MainLayout.class)
public class StringFunctions extends ViewFrame {

    private Button xmlPrettyPrint;
    private Button jsonPrettyPrint;
    private Button xmlDecoder;
    private Button blobToString;
    private Button stringToBlob;
    private Button decodeBase64;
    private Button encodeBase64;
    private Button xmlToJson;
    private Button jsonToXML;
    private Button validateXML;
    private ValidatableSingleSelect<String> xpathRtnType;
    private ValidatableInput<String> xpathExp;
    private Button evalulateXpath;

    private com.hilerio.ace.AceEditor leftEditor;
    private com.hilerio.ace.AceEditor rightEditor;
    private Button xslTransform;

    public StringFunctions() {
        setViewContent(createContent());
    }

    private Component createContent() {
        FlexBoxLayout content = new FlexBoxLayout();
        content.setFlexDirection(FlexLayout.FlexDirection.COLUMN);
        content.add(configureButtons());
        content.add(configureAceEditor());
        content.setBoxSizing(BoxSizing.BORDER_BOX);
        content.setHeightFull();
        content.setPadding(Horizontal.RESPONSIVE_X, Top.RESPONSIVE_X);
        return content;
    }

    private HorizontalLayout configureButtons() {

        SingleSelect<AceMode> contentType = Components.input.singleSelect(AceMode.class)
                .items(AceMode.xml, AceMode.json, AceMode.sql, AceMode.java, AceMode.javascript, AceMode.text, AceMode.properties, AceMode.yaml, AceMode.sh)
                .itemCaptionGenerator(aceMode -> aceMode.name())
                .label("Content-Type")
                .clearButtonVisible(true)
                .preventInvalidInput()
                .build();

        SingleSelect<AceTheme> theme = Components.input.singleSelect(AceTheme.class)
                .items(AceTheme.values())
                .preventInvalidInput()
                .label("Theme")
                .clearButtonVisible(true)
                .build();

        contentType.addValueChangeListener(event -> rightEditor.setMode(contentType.getValue()));
        theme.addValueChangeListener(event -> {
            leftEditor.setTheme(theme.getValue());
            rightEditor.setTheme(theme.getValue());
        });

        xmlPrettyPrint = Components.button()
                .text("PrettyPrintXML")
                .onClick(event -> prettyPrintXML())
                .build()
        ;

        jsonPrettyPrint = Components.button()
                .text("PrettyPrintJSON")
                .onClick(event -> prettyPrintJSON())
                .build();

        xmlDecoder = Components.button()
                .text("XMLDecoder")
                .onClick(event -> decodeXML())
                .build();

        blobToString = Components.button()
                .text("BlobtoString")
                .onClick(event -> blobToString())
                .build();

        stringToBlob = Components.button()
                .text("StringtoBlob")
                .onClick(event -> stringToBlob())
                .build();

        encodeBase64 = Components.button()
                .text("EncodeBase64")
                .onClick(event -> encodeBase64())
                .build();

        decodeBase64 = Components.button()
                .text("DecodeBase64")
                .onClick(event -> decodeBase64())
                .build();

        xmlToJson = Components.button()
                .text("XMLtoJSON")
                .onClick(event -> xmlToJson())
                .build();

        jsonToXML = Components.button()
                .text("JSONtoXML")
                .onClick(event -> jsonToXML())
                .build();

        validateXML = Components.button()
                .text("ValidateXMLWithXSD")
                .description("Put XSD on the left side and XML on the right side")
                .onClick(event -> validateXMLWithXSD())
                .build();

        xslTransform = Components.button()
                .text("XSLTransformer")
                .description("Put XSL stylesheet on the left side and XML on the right side")
                .onClick(event -> transformXSL())
                .build();

        xpathRtnType = Components.input.singleSelect(String.class)
                .validatable()
                .withValidator(com.holonplatform.core.Validator.create(s -> s != null && !s.trim().isEmpty(), "Return Type cannot be blank"))
//                .withValidator(value -> (value == null || value.isEmpty()))
//                .validateOnValueChange(false)
                .items("String", "Boolean", "Number", "NodeList", "Node")


                .build();

        xpathExp = Components.input.string()
                .validatable()
                .withValidator(com.holonplatform.core.Validator.create(s -> s != null && !s.trim().isEmpty(), "XPath expression cannot be blank"))
//                .validateOnValueChange(false)
                .clearButtonVisible(true)
                .build();

        evalulateXpath = Components.button()
                .text("EvaluateXPath")
                .description("Put XML on the left side and you'll see the results on the right side")
                .onClick(event -> {
                    write("Reaching validation");
                    xpathRtnType.validate();
                    xpathExp.validate();

                    if (xpathRtnType.isValid() && xpathExp.isValid()) {
                        write("Valid");
                        xpathTransformation();
                    } else {
                        write("invalid");
                    }
                })
                .build();


        HorizontalLayout hl = Components.hl()
                .spacing()
                .justifyContentMode(FlexComponent.JustifyContentMode.START)
                .add(contentType)
                .add(theme)
                .add(blobToString, stringToBlob, encodeBase64, decodeBase64, xmlDecoder)
                .add(xmlPrettyPrint)
                .add(jsonPrettyPrint)
                .add(xmlToJson, jsonToXML)
                .add(validateXML, xslTransform)
                .add(Components.label().text("ReturnType").build())
                .add(xpathRtnType.getComponent())
                .add(Components.label().text("XPathExpr").build())
                .add(xpathExp.getComponent(), evalulateXpath)
                .flexGrow(1, xpathExp.getComponent())
                .alignItems(FlexComponent.Alignment.BASELINE)
                .build();
        hl.getStyle().set("flex-wrap", "wrap");

        return hl;


    }

    private void write(String msg) {
        System.out.println(msg);
    }

    private void xpathTransformation() {

        DocumentBuilderFactory builderFactory =
                DocumentBuilderFactory.newInstance();

        leftEditor.getOptionalValue().ifPresent(s -> {

            try {
                DocumentBuilder domParser;
                domParser = builderFactory.newDocumentBuilder();
                // Load the DOM Document from the XML data using the parser
                Document domDocument =
                        domParser.parse(IOUtils.toInputStream(s, StandardCharsets.UTF_8));

                // Instantiate an XPath object which compiles
                // and evaluates XPath expressions.
                XPath xPath = XPathFactory.newInstance().newXPath();
                String expr = xpathExp.getValue();
                switch (xpathRtnType.getValue()) {
                    case "String":
                        rightEditor.setValue(String.valueOf(xPath.compile(expr).
                                evaluate(domDocument, XPathConstants.STRING)));
                        break;
                    case "Number":
                        rightEditor.setValue(String.valueOf((Number) xPath.compile(expr).
                                evaluate(domDocument, XPathConstants.NUMBER)));
                        break;
                    case "Boolean":
                        rightEditor.setValue(String.valueOf((Boolean) xPath.compile(expr).
                                evaluate(domDocument, XPathConstants.BOOLEAN)));
                        break;
                    case "Node":
                        rightEditor.setValue(((Node) xPath.compile(expr).
                                evaluate(domDocument, XPathConstants.NODE)).toString());
                        break;
                    case "NodeList":
                        NodeList resNodeList = (NodeList) xPath.compile(expr).evaluate(domDocument, XPathConstants.NODESET);
                        if (resNodeList != null) {
                            int lenList = resNodeList.getLength();
                            Node resNode;
                            StringBuilder sb = new StringBuilder();
                            String str;
                            for (int i = 1; i <= lenList; i++) {
                                resNode = resNodeList.item(i - 1);
                                String resNodeNameStr = resNode.getNodeName();
                                String resNodeTextStr = resNode.getTextContent();
                                str = i + ": " + resNode + "  (NodeName:'" +
                                        resNodeNameStr + "'    NodeTextContent:'" +
                                        resNodeTextStr + "')";
                                sb.append(str).append("\n");
                            }

                            rightEditor.setValue(sb.toString());
                        }
                }
            } catch (ParserConfigurationException | SAXException | IOException | XPathExpressionException e) {
                showErrorNotification(e);
            }
        });


    }

    private void showErrorNotification(Exception e) {
        Notification.show(e.getMessage()).addThemeVariants(NotificationVariant.LUMO_ERROR);
    }

    private void transformXSL() {

        leftEditor.getOptionalValue().ifPresent(s -> {
            rightEditor.getOptionalValue().ifPresent(s1 -> {
                leftEditor.setMode(AceMode.xml);
                rightEditor.setMode(AceMode.xml);

                try {
                    StreamSource source = new StreamSource(IOUtils.toInputStream(s, "UTF8"));
                    StreamSource stylesource = new StreamSource(IOUtils.toInputStream(s1, "UTF8"));

                    TransformerFactory factory = TransformerFactory.newInstance();
                    Transformer transformer = factory.newTransformer(stylesource);

                    ByteArrayOutputStream bos = new ByteArrayOutputStream();
                    StreamResult result = new StreamResult(bos);
                    transformer.transform(source, result);
                    AceEditor aceEditor = new AceEditor();
                    aceEditor.setMode(AceMode.xml);
                    aceEditor.setValue(new String(bos.toByteArray()));
                    Components.dialog
                            .message()
                            .width("50%")
                            .height("50%")
                            .withComponent(aceEditor)
                            .open();
                } catch (TransformerException | IOException e) {
                    showErrorNotification(e);
                }

            });
        });


    }

    private void validateXMLWithXSD() {

        leftEditor.getOptionalValue().ifPresent(s -> {
            rightEditor.getOptionalValue().ifPresent(s1 -> {
                leftEditor.setMode(AceMode.xml);
                rightEditor.setMode(AceMode.xml);
                SchemaFactory schemaFactory = SchemaFactory.newInstance(XMLConstants.W3C_XML_SCHEMA_NS_URI);
                Source source = new StreamSource(IOUtils.toInputStream(s, StandardCharsets.UTF_8));
                Schema schema = null;
                String status;
                try {
                    schema = schemaFactory.newSchema(source);
                    Validator validator = schema.newValidator();
                    validator.validate(new StreamSource(IOUtils.toInputStream(s1, StandardCharsets.UTF_8)));
                    status = "Validation Successful";
                    Components.dialog.showMessage(status);
                } catch (SAXException | IOException e) {
                    showErrorNotification(e);
                }


            });
        });
    }

    private void blobToString() {
        leftEditor.getOptionalValue().ifPresent(s -> {
            try {
                rightEditor.setValue(StringUtils.toEncodedString(Hex.decodeHex(s), StandardCharsets.UTF_8));
                rightEditor.setMode(AceMode.text);
            } catch (DecoderException e) {
                showErrorNotification(e);
            }
        });
    }

    private void stringToBlob() {
        leftEditor.getOptionalValue().ifPresent(s -> {
            rightEditor.setValue(Hex.encodeHexString(s.getBytes()));
            rightEditor.setMode(AceMode.text);
        });

    }

    private void decodeBase64() {
        leftEditor.getOptionalValue().ifPresent(s -> {
            try {
                rightEditor.setValue(StringUtils.toEncodedString(Base64.decodeBase64(s), StandardCharsets.UTF_8));
            } catch (Exception e) {
                showErrorNotification(e);
            }
        });

    }

    private void encodeBase64() {
        leftEditor.getOptionalValue().ifPresent(s -> {
            try {
                rightEditor.setValue(StringUtils.toEncodedString(Base64.encodeBase64(s.getBytes()), StandardCharsets.UTF_8));
            } catch (Exception e) {
                showErrorNotification(e);
            }
        });

    }

    private void decodeXML() {
        leftEditor.getOptionalValue().ifPresent(s -> {
            try {
                rightEditor.setValue(U.unescape(s));
                rightEditor.setMode(AceMode.xml);
            } catch (Exception e) {
                showErrorNotification(e);
            }
        });
    }

    private void jsonToXML() {
        leftEditor.getOptionalValue().ifPresent(s -> {
            try {
                rightEditor.setValue(U.jsonToXml(s));
            } catch (Exception e) {
                showErrorNotification(e);
            }

            rightEditor.setMode(AceMode.xml);
            leftEditor.setMode(AceMode.json);
        });

    }

    private void xmlToJson() {
        leftEditor.getOptionalValue().ifPresent(s -> {
            try {
                rightEditor.setValue(U.xmlToJson(s));
            } catch (Exception e) {
                showErrorNotification(e);
            }

            rightEditor.setMode(AceMode.json);
            leftEditor.setMode(AceMode.xml);
        });

    }

    private HorizontalLayout configureAceEditor() {

        leftEditor = new AceEditor();
        leftEditor.setMinlines(40);
        leftEditor.setMaxlines(40);
        leftEditor.setWrap(true);
        leftEditor.setTabSize(5);
        leftEditor.setInitialFocus(true);
        leftEditor.setRequiredIndicatorVisible(true);
        leftEditor.focus();

        rightEditor = new AceEditor();
        rightEditor.setMinlines(40);
        rightEditor.setMaxlines(40);
        rightEditor.setWrap(true);
        rightEditor.setTabSize(5);

        return Components.hl()
                .spacing()
                .fullSize()
                .add(leftEditor)
                .add(rightEditor)
                .build()
                ;


    }

    private void prettyPrintJSON() {
        leftEditor.getOptionalValue().ifPresent(s -> {
            try {
                rightEditor.setValue(U.formatJson(s));
                rightEditor.setMode(AceMode.json);
                leftEditor.setMode(AceMode.json);
            } catch (Exception e) {
                showErrorNotification(e);
            }
        });

    }

    private void prettyPrintXML() {
        leftEditor.getOptionalValue().ifPresent(s -> {
            try {
                rightEditor.setValue(U.formatXml(s));
                rightEditor.setMode(AceMode.xml);
                leftEditor.setMode(AceMode.xml);
            } catch (Exception e) {
                showErrorNotification(e);
            }
        });
    }

}