package com.ibasco.glcdemu.utils;

import com.ibasco.glcdemu.Context;
import com.ibasco.glcdemu.Stages;
import javafx.scene.Group;
import javafx.scene.Node;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.scene.layout.Priority;
import javafx.stage.Window;

import java.io.PrintWriter;
import java.io.StringWriter;
import java.util.Optional;
import java.util.function.Consumer;

public class DialogUtil {

    public static final ButtonType REPORT_BUTTON = new ButtonType("Report Issue", ButtonBar.ButtonData.OK_DONE);

    public static boolean promptConfirmation(String header, String content) {
        return promptConfirmation(header, content, null);
    }

    public static boolean promptConfirmation(String header, String content, Window owner) {
        Alert confirmAlert = new Alert(Alert.AlertType.CONFIRMATION, content, ButtonType.YES, ButtonType.NO);
        confirmAlert.setHeaderText(header);
        confirmAlert.setTitle(header);
        if (owner == null)
            owner = Stages.getPrimaryStage();
        confirmAlert.initOwner(owner);
        confirmAlert.getDialogPane().getStylesheets().add(Context.getPrimaryStage().getScene().getStylesheets().get(0));
        Optional<ButtonType> answer = confirmAlert.showAndWait();
        return answer.isPresent() && answer.get().equals(ButtonType.YES);
    }

    public static Alert createExceptionDialog(String title, String header, Throwable ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR, "", ButtonType.OK, REPORT_BUTTON);
        alert.initOwner(Stages.getPrimaryStage());
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(ex.getCause() != null ? "Cause: " + ex.getCause().getMessage() : "");

        // Create expandable Exception.
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        ex.printStackTrace(pw);
        String exceptionText = sw.toString();

        Label label = new Label("The exception stacktrace was:");

        TextArea textArea = new TextArea(exceptionText);
        textArea.setStyle("-fx-font-family: \"Courier New\"; -fx-font-size: 12px;");
        textArea.setEditable(false);
        textArea.setWrapText(true);

        textArea.setMaxWidth(Double.MAX_VALUE);
        textArea.setMaxHeight(Double.MAX_VALUE);
        GridPane.setVgrow(textArea, Priority.ALWAYS);
        GridPane.setHgrow(textArea, Priority.ALWAYS);

        GridPane expContent = new GridPane();
        expContent.setMaxWidth(Double.MAX_VALUE);
        expContent.add(label, 0, 0);
        expContent.add(textArea, 0, 1);
        alert.getDialogPane().setExpandableContent(expContent);

        return alert;
    }

    public static void showInfo(String header, String content) {
        showInfo(header, content, Stages.getPrimaryStage());
    }

    public static void showInfo(String header, String content, Window owner) {
        Alert confirmAlert = new Alert(Alert.AlertType.INFORMATION, content, ButtonType.OK);
        confirmAlert.setHeaderText(header);
        if (owner == null)
            owner = Stages.getPrimaryStage();
        confirmAlert.initOwner(owner);
        confirmAlert.showAndWait();
    }

    public static Alert createAlertDialogWithCheckbox(Alert.AlertType type, String title, String header, String content, String optOutMessage, Consumer<Boolean> optOutAction, ButtonType... buttonTypes) {
        return createAlertDialogWithCheckbox(type, title, header, content, optOutMessage, optOutAction, Context.getPrimaryStage(), buttonTypes);
    }

    public static Alert createAlertDialogWithCheckbox(Alert.AlertType type, String title, String header, String content, String optOutMessage, Consumer<Boolean> optOutAction, Window owner, ButtonType... buttonTypes) {
        Alert alert = new Alert(type);
        // Need to force the alert to layout in order to grab the graphic,
        // as we are replacing the dialog pane with a custom pane
        alert.getDialogPane().applyCss();
        Node graphic = alert.getDialogPane().getGraphic();
        alert.setDialogPane(new DialogPane() {
            @Override
            protected Node createDetailsButton() {
                CheckBox optOut = new CheckBox();
                optOut.setText(optOutMessage);
                optOut.setOnAction(e -> optOutAction.accept(optOut.isSelected()));
                return optOut;
            }
        });
        alert.getDialogPane().getButtonTypes().addAll(buttonTypes);
        alert.getDialogPane().setExpandableContent(new Group());
        alert.getDialogPane().setGraphic(graphic);
        alert.getDialogPane().setExpanded(true);
        alert.initOwner(owner);
        alert.setTitle(title);
        alert.setHeaderText(header);
        alert.setContentText(content);
        return alert;
    }
}
