package com.myapp.iib.ui.components;

import com.hilerio.ace.AceEditor;
import com.holonplatform.vaadin.flow.components.Components;
import com.vaadin.flow.component.orderedlayout.HorizontalLayout;
import lombok.Getter;

@Getter
public class MyAceEditor extends AceEditor {


    private AceEditor leftEditor;
    private AceEditor rightEditor;

    public MyAceEditor() {
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
    }

    public HorizontalLayout configureAceEditor() {

        return Components.hl()
                .spacing()
                .fullSize()
                .add(leftEditor)
                .add(rightEditor)
                .build()
                ;


    }


}
