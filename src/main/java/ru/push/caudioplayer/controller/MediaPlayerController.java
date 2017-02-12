package ru.push.caudioplayer.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.scene.control.Slider;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.push.caudioplayer.core.mediaplayer.CustomPlayerComponent;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.nio.file.Paths;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 23.11.16
 */
public class MediaPlayerController {
  private static final Logger LOG = LoggerFactory.getLogger(MediaPlayerController.class);

  @FXML
  private HBox mediaButtonsControl;
  @FXML
  private Button stopButton;
  @FXML
  private Button playButton;
  @FXML
  private Button pauseButton;
  @FXML
  private ProgressBar seekBar;
  @FXML
  private Slider volumeSlider;

  @Autowired
  private CustomPlayerComponent playerComponent;

  @FXML
  public void initialize() {
    LOG.debug("initialize");
  }

  @PostConstruct
  public void init() {
    LOG.debug("init");
    prepareMediaComponent();
    prepareUI();
  }

  private void prepareMediaComponent() {
  }

  private void prepareUI() {
    volumeSlider.setMax(playerComponent.getMaxVolume());
    volumeSlider.setValue(playerComponent.getVolume());

    volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
      playerComponent.setVolume(newValue.intValue());
    });
  }

  @FXML
  void stopAction(ActionEvent event) {
    playerComponent.stop();
  }

  @FXML
  void playAction(ActionEvent event) {
    final URI resource = Paths.get("target/1. Just One Of Those Things.mp3").toUri();
    playerComponent.playMedia(resource.toString());
  }

  @FXML
  void pauseAction(ActionEvent event) {
    playerComponent.pause();
  }
}
