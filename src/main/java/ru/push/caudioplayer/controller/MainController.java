package ru.push.caudioplayer.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.layout.VBox;
import javafx.stage.FileChooser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.push.caudioplayer.AppMain;
import ru.push.caudioplayer.ConfigurationControllers;
import ru.push.caudioplayer.core.facades.AudioPlayerFacade;

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
	@FXML
	@Resource(name = "lastfmPanelView")
	private ConfigurationControllers.View lastfmPanelView;

  @Resource
  private AudioPlayerFacade audioPlayerFacade;
  @Resource
	private AppMain appMain;

  @FXML
  public void initialize() {
		LOG.debug("initialize FXML for {}", this.getClass().getName());
  }

  @PostConstruct
  public void init() {
		LOG.debug("init bean {}", this.getClass().getName());

    mainContainer.getChildren().add(audioPlayerView.getView());
    mainContainer.getChildren().add(playlistView.getView());
    mainContainer.getChildren().add(lastfmPanelView.getView());
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
    // todo: add cancel action handling
    audioPlayerFacade.addFilesToPlaylist(files);
  }

  @FXML
	public void connectLastFm(ActionEvent actionEvent) {
		audioPlayerFacade.connectLastFm(
				(pageUrl) -> appMain.openWebPage(pageUrl)
		);
	}
}
