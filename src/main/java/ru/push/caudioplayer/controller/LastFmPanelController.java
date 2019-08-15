package ru.push.caudioplayer.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
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
		LOG.debug("initialize");
	}


}
