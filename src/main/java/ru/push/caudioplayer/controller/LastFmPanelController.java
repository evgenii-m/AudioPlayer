package ru.push.caudioplayer.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.push.caudioplayer.core.facades.AudioPlayerFacade;

public class LastFmPanelController {

	private static final Logger LOG = LoggerFactory.getLogger(LastFmPanelController.class);

	@FXML
	public ListView recentTracksContainer;
	@FXML
	public ScrollPane trackInfoContainer;


	@Autowired
	private AudioPlayerFacade audioPlayerFacade;


	@FXML
	public void initialize() {
		LOG.debug("initialize FXML for {}", this.getClass().getName());

	}


}
