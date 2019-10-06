package ru.push.caudioplayer.controller;

import javafx.fxml.FXML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.push.caudioplayer.core.facades.MusicLibraryLogicFacade;

import javax.annotation.PostConstruct;

/**
 */
public class NotificationsPanelController {

  private static final Logger LOG = LoggerFactory.getLogger(NotificationsPanelController.class);

	@Autowired
	private MusicLibraryLogicFacade musicLibraryLogicFacade;


  @FXML
  public void initialize() {
		LOG.debug("initialize FXML for {}", this.getClass().getName());
  }

  @PostConstruct
  public void init() {
    LOG.debug("init bean {}", this.getClass().getName());
  }

}
