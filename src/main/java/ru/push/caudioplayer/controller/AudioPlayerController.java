package ru.push.caudioplayer.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.Slider;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.push.caudioplayer.core.mediaplayer.components.CustomAudioPlayerComponent;
import ru.push.caudioplayer.core.mediaplayer.dto.MediaInfoData;
import ru.push.caudioplayer.utils.TrackTimeLabelBuilder;

import javax.annotation.PostConstruct;
import java.net.URI;
import java.nio.file.Paths;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

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

  private final ScheduledExecutorService playerScheduler = Executors.newSingleThreadScheduledExecutor();
  private boolean positionSliderMousePressed;

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

    positionSlider.setMax(POSITION_SLIDER_MAX_VALUE);
    updatePlaybackPosition(0, 0);
    addPositionSliderMouseListeners();

    playerScheduler.scheduleAtFixedRate(new UpdateUiRunnable(playerComponent), 0L, 1L, TimeUnit.SECONDS);
  }

  public void stopScheduler() {
    playerScheduler.shutdown();
  }

  private void addPositionSliderMouseListeners() {
    positionSlider.addEventHandler(MouseEvent.MOUSE_PRESSED, event -> {
      if (playerComponent.isPlaying()) {
        positionSliderMousePressed = true;
        playerComponent.pause();
      } else {
        positionSliderMousePressed = false;
      }
      changePlaybackPosition();
    });

    positionSlider.addEventHandler(MouseEvent.MOUSE_RELEASED, event -> {
      changePlaybackPosition();
      refreshUiState();
    });
  }

  private void changePlaybackPosition() {
    // reduce to media component required range [0.0, 1.0]
    float newPosition = positionSlider.valueProperty().floatValue() / POSITION_SLIDER_SCALE_COEF;
    playerComponent.changePlaybackPosition(newPosition);
  }

  private void refreshUiState() {
    if (!playerComponent.isPlaying()) {
      // Resume play or play a few frames then pause to show current position in video
      playerComponent.resume();
      if (!positionSliderMousePressed) {
        try {
          Thread.sleep(500); // Half a second probably gets an iframe
        } catch(InterruptedException e) {
          LOG.error("InterruptedException whe set volume: " + e);
        }
        playerComponent.pause();
      }
    }

    MediaInfoData mediaInfoData = playerComponent.getCurrentTrackInfo();
    float playbackPosition = playerComponent.getPlaybackPosition();

    updatePlaybackPosition(playbackPosition, mediaInfoData.getLength());
  }


  private final class UpdateUiRunnable implements Runnable {
    private final CustomAudioPlayerComponent playerComponent;

    private UpdateUiRunnable(CustomAudioPlayerComponent playerComponent) {
      this.playerComponent = playerComponent;
    }

    @Override
    public void run() {
      MediaInfoData mediaInfoData = playerComponent.getCurrentTrackInfo();
      float playbackPosition = playerComponent.getPlaybackPosition();

      Platform.runLater(() -> {
        if (playerComponent.isPlaying()) {
          updatePlaybackPosition(playbackPosition, mediaInfoData.getLength());
        }
      });
    }
  }

  private void updatePlaybackPosition(float playbackPosition, long trackDuration) {
    long trackCurrentTime = (long) ((float) trackDuration * playbackPosition);
    trackTimeLabel.setText(trackTimeLabelBuilder.buildTimeLabel(trackCurrentTime, trackDuration));
    positionSlider.setValue(playbackPosition * POSITION_SLIDER_SCALE_COEF);
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

}
