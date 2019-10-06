package ru.push.caudioplayer.controller;

import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import javafx.scene.control.TextArea;
import javafx.scene.layout.Pane;
import javafx.scene.layout.VBox;
import javafx.scene.web.WebEngine;
import javafx.scene.web.WebView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.push.caudioplayer.ConfigurationControllers;
import ru.push.caudioplayer.core.facades.AudioPlayerFacade;
import ru.push.caudioplayer.core.facades.MusicLibraryLogicFacade;
import ru.push.caudioplayer.core.facades.dto.NotificationData;
import ru.push.caudioplayer.core.mediaplayer.DefaultAudioPlayerEventAdapter;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.function.Function;

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
	public Pane audioPlayerComponentPane;
	@FXML
	public Tab playlistComponentTab;
	@FXML
	public Tab lastfmPanelTab;
	@FXML
	public Tab notificationsPanelTab;
	@FXML
	public TabPane panelsTabPane;
	@FXML
	public TextArea notificationOutputTextArea;
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
	private AudioPlayerFacade audioPlayerFacade;
	@Autowired
	private MusicLibraryLogicFacade musicLibraryLogicFacade;
	@Autowired
	private PlaylistController playlistController;

  @FXML
  public void initialize() {
		LOG.debug("initialize FXML for {}", this.getClass().getName());
  }

  @PostConstruct
  public void init() {
		LOG.debug("init bean {}", this.getClass().getName());

		System.setProperty("sun.net.http.allowRestrictedHeaders", "true");

		AudioPlayerEventAdapter eventAdapter = new AudioPlayerEventAdapter();
		audioPlayerFacade.addEventListener(eventAdapter);
		musicLibraryLogicFacade.addEventListener(eventAdapter);

		audioPlayerComponentPane.getChildren().add(audioPlayerView.getView());
		playlistComponentTab.setContent(playlistView.getView());
		lastfmPanelTab.setContent(lastfmPanelView.getView());
  }

  @FXML
  public void addStreamToActivePlaylist(ActionEvent actionEvent) {
		playlistController.addStreamToPlaylist(actionEvent);
  }

  @FXML
  public void createNewPlaylist(ActionEvent actionEvent) {
		playlistController.createNewPlaylist(actionEvent);
  }

  @FXML
  public void addFilesToPlaylist(ActionEvent actionEvent) {
		playlistController.addFilesToPlaylist(actionEvent);
  }

  @FXML
	public void connectLastFm(ActionEvent actionEvent) {
  	String lastfmToken = musicLibraryLogicFacade.getLastFmToken();
		String webPageUrl = musicLibraryLogicFacade.getLastFmAuthorizationPageUrl(lastfmToken);
		Function<String, Boolean> actionHandler = (value) ->
				musicLibraryLogicFacade.processLastFmAuthorization(lastfmToken, value);
		displayWebPageWindow(webPageUrl, "Last.fm authorization", false, actionHandler);
	}

	@FXML
	public void connectDeezer(ActionEvent actionEvent) {
		String webPageUrl = musicLibraryLogicFacade.getDeezerUserAuthorizationPageUrl();
		Function<String, Boolean> actionHandler = (value) -> musicLibraryLogicFacade.processDeezerAuthorization(value);
		displayWebPageWindow(webPageUrl, "Deezer authorization", true, actionHandler);
	}

	private void displayWebPageWindow(String pageUrl, String windowTitle, boolean changeLocationHandling,
																		Function<String, Boolean> actionHandler) {
		final WebView browser = new WebView();
		final WebEngine webEngine = browser.getEngine();

		ScrollPane scrollPane = new ScrollPane();
		scrollPane.setContent(browser);

		webEngine.setJavaScriptEnabled(true);
		webEngine.setUserAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/70.0.3538.102 Safari/537.36 Edge/18.18362");

		com.sun.javafx.webkit.WebConsoleListener.setDefaultListener(
				(webView, message, lineNumber, sourceId) ->
						LOG.debug("JS Console: [" + sourceId + ":" + lineNumber + "] " + message)
		);

		webEngine.load(pageUrl);

		Scene webPageWindowScene = new Scene(browser);
		Stage webPageWindowStage = new Stage();
		Stage primaryStage = (Stage) mainContainer.getScene().getWindow();
		webPageWindowStage.setTitle(windowTitle);
		webPageWindowStage.setScene(webPageWindowScene);
		webPageWindowStage.initModality(Modality.WINDOW_MODAL);
		webPageWindowStage.setResizable(true);

		webPageWindowStage.initOwner(primaryStage);

		if (changeLocationHandling) {
			// append listener for web browser location URI changes for process authorization
			webEngine.locationProperty().addListener((observable, oldValue, newValue) -> {
				LOG.debug("Location change event: observable = {}, oldValue = {}, newValue = {}", observable, oldValue, newValue);
				boolean result = actionHandler.apply(newValue);
				if (result) {
					webPageWindowStage.close();
				}
			});
		} else {
			// append listener for web window close for process authorization
			webPageWindowStage.setOnCloseRequest(event -> {
				LOG.debug("Last.fm authorization page close with URL: {}", webEngine.getLocation());
				actionHandler.apply(webEngine.getLocation());
			});
		}

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

	private final class AudioPlayerEventAdapter extends DefaultAudioPlayerEventAdapter {

		@Override
		public void obtainedNotification(NotificationData notificationData) {
			Platform.runLater(() -> {
				notificationOutputTextArea.appendText(notificationData.getMessage() + "\n");
			});
		}
	}
}
