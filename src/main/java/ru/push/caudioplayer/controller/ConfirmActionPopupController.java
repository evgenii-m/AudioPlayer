package ru.push.caudioplayer.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.push.caudioplayer.core.facades.domain.PlaylistData;

import javax.annotation.PostConstruct;
import java.util.function.Supplier;

public class ConfirmActionPopupController {
	private static final Logger LOG = LoggerFactory.getLogger(ConfirmActionPopupController.class);

	private static final String DEFAULT_MESSAGE = "Confirm action?";

	@FXML
	public Label messageLabel;

	private Supplier<PlaylistData> actionSupplier;


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

	public void setAction(Supplier<PlaylistData> actionSupplier, String message) {
		this.actionSupplier = actionSupplier;
		messageLabel.setText(message);
	}

	public void setAction(Supplier<PlaylistData> actionSupplier) {
		this.actionSupplier = actionSupplier;
	}

	@FXML
	public void cancelAction(ActionEvent actionEvent) {
		closePopup();
	}

	@FXML
	public void confirmAction(ActionEvent actionEvent) {
		if (actionSupplier != null) {
			PlaylistData playlistData = actionSupplier.get();
			if (playlistData != null) {
				closePopup();
			} else {
				LOG.error("Failed to playlist delete");
			}
		} else {
			LOG.error("Action supplier not defined");
		}
	}

	private void closePopup() {
		Stage popupStage = (Stage) messageLabel.getScene().getWindow();
		popupStage.close();
	}
}
