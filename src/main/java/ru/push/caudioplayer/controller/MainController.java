package ru.push.caudioplayer.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.push.caudioplayer.ConfigurationControllers;
import ru.push.caudioplayer.core.mediaplayer.CustomAudioPlayerComponent;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.net.MalformedURLException;
import java.net.URL;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 22.11.16
 */
public class MainController {

  private static final Logger LOG = LoggerFactory.getLogger(MainController.class);

  @FXML
  private VBox root;

  @Resource(name = "playerComponent")
  private CustomAudioPlayerComponent playerComponent;

  @FXML
  @Resource(name = "audioPlayerView")
  private ConfigurationControllers.View audioPlayerView;

  @FXML
  public void initialize() {
    LOG.debug("initialize");
  }

  @PostConstruct
  public void init() {
    LOG.debug("init");
    root.getChildren().add(audioPlayerView.getView());
  }

  public void addLocation(ActionEvent actionEvent) {
    String mediaLocation = "http://ice1.somafm.com/groovesalad-128.mp3";
    try {
      URL mediaUrl = new URL(mediaLocation);
      playerComponent.playMedia(mediaUrl.toString());
    } catch (MalformedURLException e) {
      e.printStackTrace();
    }
  }
}
