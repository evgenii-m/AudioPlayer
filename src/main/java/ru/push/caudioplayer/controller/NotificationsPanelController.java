package ru.push.caudioplayer.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.push.caudioplayer.core.facades.AudioPlayerFacade;
import ru.push.caudioplayer.core.facades.MusicLibraryLogicFacade;
import ru.push.caudioplayer.core.facades.dto.NotificationData;
import ru.push.caudioplayer.core.mediaplayer.DefaultAudioPlayerEventAdapter;

import javax.annotation.PostConstruct;

/**
 */
public class NotificationsPanelController {

  private static final Logger LOG = LoggerFactory.getLogger(NotificationsPanelController.class);

  @FXML
  public TextArea notificationOutputTextArea;

	@Autowired
	private AudioPlayerFacade audioPlayerFacade;

	@Autowired
	private MusicLibraryLogicFacade musicLibraryLogicFacade;


  @FXML
  public void initialize() {
		LOG.debug("initialize FXML for {}", this.getClass().getName());
  }

  @PostConstruct
  public void init() {
    LOG.debug("init bean {}", this.getClass().getName());

		AudioPlayerEventAdapter eventAdapter = new AudioPlayerEventAdapter();
		audioPlayerFacade.addEventListener(eventAdapter);
		musicLibraryLogicFacade.addEventListener(eventAdapter);
  }

	private final class AudioPlayerEventAdapter extends DefaultAudioPlayerEventAdapter {
		@Override
		public void obtainedNotification(NotificationData notificationData) {
			Platform.runLater(() -> {
				notificationOutputTextArea.appendText(notificationData.getMessage() + "\n");
			});
		}
	}
}
