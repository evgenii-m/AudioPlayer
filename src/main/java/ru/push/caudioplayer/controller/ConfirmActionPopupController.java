package ru.push.caudioplayer.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.util.function.Consumer;

public class ConfirmActionPopupController {
	private static final Logger LOG = LoggerFactory.getLogger(ConfirmActionPopupController.class);

	private static final String DEFAULT_MESSAGE = "Confirm action?";

	@FXML
	public Label messageLabel;

	private Consumer<Void> action;


	public ConfirmActionPopupController() {
	}

	@FXML
	public void initialize() {
		LOG.debug("initialize FXML for {}", this.getClass().getName());
		messageLabel.setText(DEFAULT_MESSAGE);
	}

	@PostConstruct
	public void init() {
		LOG.debug("init bean {}", this.getClass().getName());
	}

	public void setAction(Consumer<Void> action, String message) {
		this.action = action;
		messageLabel.setText(message);
	}

	public void setAction(Consumer<Void> action) {
		this.action = action;
	}

	@FXML
	public void cancelAction(ActionEvent actionEvent) {
		closePopup();
	}

	@FXML
	public void confirmAction(ActionEvent actionEvent) {
		if (action != null) {
			action.accept(null);
			closePopup();
		} else {
			LOG.error("Action consumer not defined");
		}
	}

	private void closePopup() {
		Stage popupStage = (Stage) messageLabel.getScene().getWindow();
		popupStage.close();
	}
}
