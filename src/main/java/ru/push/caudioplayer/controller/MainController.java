package ru.push.caudioplayer.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.push.caudioplayer.ConfigurationControllers;
import ru.push.caudioplayer.core.facades.AudioPlayerFacade;
import ru.push.caudioplayer.core.mediaplayer.components.CustomAudioPlayerComponent;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.util.Collections;
import java.util.List;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 22.11.16
 */
public class MainController {

  private static final Logger LOG = LoggerFactory.getLogger(MainController.class);

  @FXML
  private VBox mainContainer;
  @FXML
  @Resource(name = "audioPlayerView")
  private ConfigurationControllers.View audioPlayerView;
  @FXML
  @Resource(name = "playlistView")
  private ConfigurationControllers.View playlistView;

  // TODO: must be removed!
  @Resource
  private CustomAudioPlayerComponent playerComponent;

  @Resource
  private AudioPlayerFacade audioPlayerFacade;

  @FXML
  public void initialize() {
    LOG.debug("initialize");
  }

  @PostConstruct
  public void init() {
    LOG.debug("init");

    mainContainer.getChildren().add(audioPlayerView.getView());
    mainContainer.getChildren().add(playlistView.getView());
  }

  @FXML
  public void addLocation(ActionEvent actionEvent) {
    audioPlayerFacade.addLocationsToPlaylist(Collections.singletonList("http://ice1.somafm.com/groovesalad-128.mp3"));
  }

  @FXML
  public void createNewPlaylist(ActionEvent actionEvent) {
    audioPlayerFacade.createNewPlaylist();
  }

  @FXML
  public void openFiles(ActionEvent actionEvent) {
    FileChooser fileChooser = new FileChooser();
    fileChooser.setTitle("Open file(s)");
    // WARNING: if this code throws JVM crashing, add JVM option '-DVLCJ_INITX=no'
    List<File> files = fileChooser.showOpenMultipleDialog(mainContainer.getScene().getWindow());
    audioPlayerFacade.addFilesToPlaylist(files);
  }
}
