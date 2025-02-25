package com.myapp.iib.my;

import com.holonplatform.core.property.PropertyBox;
import com.holonplatform.vaadin.flow.components.Components;
import com.holonplatform.vaadin.flow.navigator.Navigator;
import com.myapp.iib.ui.MainLayout;
import com.myapp.iib.ui.components.navigation.bar.AppBar;
import com.vaadin.flow.component.html.Span;
import com.myapp.iib.model.ActivityLogModel;
import com.myapp.iib.ui.views.IIBExplorer;
import lombok.SneakyThrows;
import org.apache.commons.io.FileUtils;
import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FilenameFilter;
import java.nio.charset.Charset;
import java.util.Arrays;
import java.util.List;

public class BrokerUtils {

    public static final String USER_DIR = System.getProperty("user.home");

    public static String prepBrokerFile(String brokerFile) {

        if (!StringUtils.endsWith(brokerFile, "broker")) {
            brokerFile += ".broker";
        }

        return USER_DIR + File.separator + brokerFile;
    }

    public static List<String> brokerFiles() {
        FilenameFilter filter = (dir, name) -> name.endsWith("broker");
        File dir = new File(USER_DIR);

        return Arrays.asList(dir.list(filter));


    }

    @SneakyThrows
    public static String getHostFromBrokerFile(String brokerFile) {

        return FileUtils.readLines(new File(prepBrokerFile(brokerFile)), Charset.defaultCharset())
                .stream().filter(s -> !s.isEmpty() && StringUtils.contains(s, "host"))
                .map(s -> StringUtils.substringBetween(s, "host=", " ").replaceAll("\"", ""))
                .findFirst()
                .orElse("HostNotFoundFromBrokerFile");

    }



    public static AppBar initAppBar(String title, String node) {

        AppBar appBar = MainLayout.get().getAppBar();
        appBar.setNaviMode(AppBar.NaviMode.CONTEXTUAL);
        appBar.getContextIcon().addClickListener(e -> Navigator.get()
                .navigation(IIBExplorer.class)
                .withQueryParameter("nodeName", StringUtils.substringBefore(node, "."))
                .navigate()
        );
        appBar.setTitle(title);
        return appBar;
    }

    public static Span formatLogStatus(PropertyBox propertyBox) {
        String badge;
        String level = StringUtils.left(propertyBox.getValueIfPresent(ActivityLogModel.STATUS).orElse("I"),1);
        switch (level) {
            case "E":
                badge = "badge error";
                break;
            case "W":
                badge = "badge contrast";
                break;
            default:
                badge = "badge success";
        }
        return Components.span()
                .withThemeName(badge)
                .description(level)
                .build();
    }

    public static String getRunStatus(boolean running) {
        return running ? "Running" : "Stopped";
    }

    public static boolean addLogFilter(String message, String logLevel) {

        return logLevel == null || logLevel.isEmpty() || StringUtils.endsWithIgnoreCase(message, StringUtils.left(logLevel, 1));
    }
}
