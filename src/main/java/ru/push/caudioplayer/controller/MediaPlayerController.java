package ru.push.caudioplayer.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.push.caudioplayer.core.mediaplayer.CAudioMediaPlayerComponent;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.nio.file.Paths;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 23.11.16
 */
public class MediaPlayerController {
  private static final Logger LOGGER = LoggerFactory.getLogger(MediaPlayerController.class);

  @FXML
  private HBox mediaButtonsControl;
  @FXML
  private Button stopButton;
  @FXML
  private Button playButton;
  @FXML
  private Button pauseButton;

  @Autowired
  private CAudioMediaPlayerComponent audioMediaPlayerComponent;

  @FXML
  public void initialize() {
    LOGGER.debug("initialize");
  }

  @PostConstruct
  public void init() {
    LOGGER.debug("init");
//    audioMediaPlayerComponent = new CAudioMediaPlayerComponent();
  }

  @FXML
  void stopAction(ActionEvent event) {
    audioMediaPlayerComponent.getMediaPlayer().stop();
  }

  @FXML
  void playAction(ActionEvent event) {
    final URI resource = Paths.get("target/1. Just One Of Those Things.mp3").toUri();
    audioMediaPlayerComponent.getMediaPlayer().playMedia(resource.toString());
  }

  @FXML
  void pauseAction(ActionEvent event) {
    audioMediaPlayerComponent.getMediaPlayer().pause();
  }
}
