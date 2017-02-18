package ru.push.caudioplayer.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.push.caudioplayer.core.mediaplayer.CustomAudioPlayerComponent;
import ru.push.caudioplayer.core.mediaplayer.CustomAudioPlayerEventListener;
import ru.push.caudioplayer.core.mediaplayer.dto.TrackInfoData;
import ru.push.caudioplayer.utils.TrackTimeLabelBuilder;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.nio.file.Paths;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 23.11.16
 */
public class AudioPlayerController {
  private static final Logger LOG = LoggerFactory.getLogger(AudioPlayerController.class);

  private static final double POSITION_SLIDER_MAX_VALUE = 100;  // track position determined in percentage
  private static final float POSITION_SLIDER_SCALE_COEF = 100;

  @FXML
  private HBox mediaButtonsControl;
  @FXML
  private Button stopButton;
  @FXML
  private Button playButton;
  @FXML
  private Button pauseButton;
  @FXML
  private Slider positionSlider;
  @FXML
  private Label trackTimeLabel;
  @FXML
  private Slider volumeSlider;

  @Autowired
  private CustomAudioPlayerComponent playerComponent;
  @Autowired
  private TrackTimeLabelBuilder trackTimeLabelBuilder;

  private boolean positionSliderMousePressed;
  private TrackInfoData currentTrackInfoData;

  @FXML
  public void initialize() {
    LOG.debug("initialize");
  }

  @PostConstruct
  public void init() {
    LOG.debug("init");
    positionSliderMousePressed = false;

    volumeSlider.setMax(playerComponent.getMaxVolume());
    volumeSlider.setValue(playerComponent.getVolume());
    volumeSlider.valueProperty().addListener((observable, oldValue, newValue) -> {
      playerComponent.setVolume(newValue.intValue());
    });

    trackTimeLabel.setText(trackTimeLabelBuilder.buildTimeLabel(0, 0));
    positionSlider.setMax(POSITION_SLIDER_MAX_VALUE);
    positionSlider.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
      positionSliderMousePressed = true;
    });
    positionSlider.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
      // reduce to media component required range [0.0, 1.0]
      float newPosition = positionSlider.valueProperty().floatValue() / POSITION_SLIDER_SCALE_COEF;
      playerComponent.changePosition(newPosition);
      positionSliderMousePressed = false;
    });

    playerComponent.addEventListener(new DefaultCustomAudioPlayerEventListener());
  }

  @FXML
  void stopAction(ActionEvent event) {
    playerComponent.stop();
  }

  @FXML
  void playAction(ActionEvent event) {
    final URI resource = Paths.get("target/1. Just One Of Those Things.mp3").toUri();
    playerComponent.playMedia(resource.toString());
    positionSlider.setValue(0);

  }

  @FXML
  void pauseAction(ActionEvent event) {
    playerComponent.pause();
  }

  private class DefaultCustomAudioPlayerEventListener implements CustomAudioPlayerEventListener {
    @Override
    public void playbackStarts(TrackInfoData trackInfo) {
      currentTrackInfoData = trackInfo;
    }

    @Override
    public void positionChanged(float newPosition) {
      Platform.runLater(() -> {
        if (!positionSliderMousePressed) {
          positionSlider.setValue(newPosition * POSITION_SLIDER_SCALE_COEF);  // transform to slider scale
        }
        if (currentTrackInfoData != null) {
          long trackEndTime = currentTrackInfoData.getDuration();
          long trackCurrentTime = (long) ((float) trackEndTime * newPosition);
          trackTimeLabel.setText(trackTimeLabelBuilder.buildTimeLabel(trackCurrentTime, trackEndTime));
        }
      });
    }
  }
}
