package ru.push.caudioplayer.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.push.caudioplayer.AppMain;
import ru.push.caudioplayer.ConfigurationControllers;
import ru.push.caudioplayer.core.facades.MusicLibraryLogicFacade;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Date;
import java.util.List;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 22.11.16
 */
public class MainController {

  private static final Logger LOG = LoggerFactory.getLogger(MainController.class);

  private static final String DEFAULT_PLAYLIST_BACKUP_FOLDER_NAME = "export/backup_%s/";
  private static final String DEFAULT_PLAYLIST_BACKUP_FOLDER_TIMESTAMP_FORMAT = "yyyy-MM-dd_HH-mm-ss";

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

	@Autowired
	private MusicLibraryLogicFacade musicLibraryLogicFacade;
	@Autowired
	private AppMain appMain;

  @FXML
  public void initialize() {
		LOG.debug("initialize FXML for {}", this.getClass().getName());
  }

  @PostConstruct
  public void init() {
		LOG.debug("init bean {}", this.getClass().getName());

		System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

    mainContainer.getChildren().add(audioPlayerView.getView());
    mainContainer.getChildren().add(playlistView.getView());
    mainContainer.getChildren().add(lastfmPanelView.getView());
  }

  @FXML
  public void addLocation(ActionEvent actionEvent) {
//		musicLibraryLogicFacade.addLocationsToPlaylist(Collections.singletonList("http://ice1.somafm.com/groovesalad-128.mp3"));
  }

  @FXML
  public void createNewPlaylist(ActionEvent actionEvent) {
//		musicLibraryLogicFacade.createNewPlaylist();
  }

  @FXML
  public void openFiles(ActionEvent actionEvent) {
//    FileChooser fileChooser = new FileChooser();
//    fileChooser.setTitle("Open file(s)");
//    // WARNING: if this code throws JVM crashing, add JVM option '-DVLCJ_INITX=no'
//    List<File> files = fileChooser.showOpenMultipleDialog(mainContainer.getScene().getWindow());
//    // todo: add cancel action handling
//		musicLibraryLogicFacade.addFilesToPlaylist(files);
  }

  @FXML
	public void connectLastFm(ActionEvent actionEvent) {
		musicLibraryLogicFacade.connectLastFm(
				(pageUrl) -> appMain.openWebPage(pageUrl)
		);
	}

	@FXML
	public void connectDeezer(ActionEvent actionEvent) {
		final WebView browser = new WebView();
		final WebEngine webEngine = browser.getEngine();

		ScrollPane scrollPane = new ScrollPane();
		scrollPane.setContent(browser);

		webEngine.setJavaScriptEnabled(true);
		webEngine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36 Edge/18.18362");

		com.sun.javafx.webkit.WebConsoleListener.setDefaultListener(
				(webView, message, lineNumber, sourceId) ->
						LOG.info("JS Console: [" + sourceId + ":" + lineNumber + "] " + message)
		);

		webEngine.load(musicLibraryLogicFacade.getDeezerUserAuthorizationPageUrl());

		Scene webPageWindowScene = new Scene(browser);
		Stage webPageWindowStage = new Stage();
		Stage primaryStage = (Stage) mainContainer.getScene().getWindow();
		webPageWindowStage.setTitle("Deezer authorization");
		webPageWindowStage.setScene(webPageWindowScene);
		webPageWindowStage.initModality(Modality.WINDOW_MODAL);
		webPageWindowStage.setResizable(true);

		webPageWindowStage.initOwner(primaryStage);

		// append listener for web browser location URI changes for checking authorization code
		webEngine.locationProperty().addListener((observable, oldValue, newValue) -> {
			LOG.debug("Location change event: observable = {}, oldValue = {}, newValue = {}", observable, oldValue, newValue);
			boolean result = musicLibraryLogicFacade.processDeezerAuthorization(newValue);
			if (result) {
				webPageWindowStage.close();
			}
		});

		webPageWindowStage.show();
	}

	@FXML
	public void backupPlaylists(ActionEvent actionEvent) {
		String currentTimeString = new SimpleDateFormat(DEFAULT_PLAYLIST_BACKUP_FOLDER_TIMESTAMP_FORMAT).format(new Date());
		String backupFolderName = String.format(DEFAULT_PLAYLIST_BACKUP_FOLDER_NAME, currentTimeString);
		Path exportFolderPath = Paths.get(backupFolderName);
		try {
			if (Files.notExists(exportFolderPath) || !Files.isDirectory(exportFolderPath)) {
				Files.createDirectories(exportFolderPath);
			}
			musicLibraryLogicFacade.backupPlaylists(backupFolderName);
		} catch (IOException e) {
			LOG.error("Export playlist error", e);
		}
	}
}
