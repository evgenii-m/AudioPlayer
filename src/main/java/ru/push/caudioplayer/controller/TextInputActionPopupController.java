package ru.push.caudioplayer.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.function.Consumer;

public class TextInputActionPopupController {

	private static final Logger LOG = LoggerFactory.getLogger(TextInputActionPopupController.class);

	@FXML
	public TextArea textInputArea;

	private Consumer<String> action;


	public void setAction(Consumer<String> action) {
		this.action = action;
	}

	@FXML
	public void cancelAction(ActionEvent actionEvent) {
		closePopup();
	}

	@FXML
	public void applyAction(ActionEvent actionEvent) {
		if (action != null) {
			action.accept(textInputArea.getText());
			closePopup();
		} else {
			LOG.error("Action consumer not defined");
		}
	}

	private void closePopup() {
		textInputArea.clear();
		Stage popupStage = (Stage) textInputArea.getScene().getWindow();
		popupStage.close();
	}
}
