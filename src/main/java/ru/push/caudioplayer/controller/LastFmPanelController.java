package ru.push.caudioplayer.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.MenuItem;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.push.caudioplayer.core.facades.MusicLibraryLogicFacade;
import ru.push.caudioplayer.core.mediaplayer.domain.LastFmTrackData;
import ru.push.caudioplayer.ui.AudioTrackPlaylistItem;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LastFmPanelController {

	private static final Logger LOG = LoggerFactory.getLogger(LastFmPanelController.class);

	private static final String LASTFM_PANEL_COLUMN_TITLE_ARTIST = "Artist";
	private static final double LASTFM_PANEL_COLUMN_WIDTH_ARTIST = 135;
	private static final String LASTFM_PANEL_COLUMN_TITLE_TRACK_TITLE = "Title";
	private static final double LASTFM_PANEL_COLUMN_WIDTH_TRACK_TITLE = 200;
	private static final String LASTFM_PANEL_COLUMN_TITLE_SCROBBLE_DATE = "Date";
	private static final double LASTFM_PANEL_COLUMN_WIDTH_SCROBBLE_DATE = 125;
	private static final String LASTFM_PANEL_COLUMN_FORMAT_SCROBBLE_DATE = "dd-MM-yyyy  HH:mm:SS";
	private static final String LASTFM_PANEL_COLUMN_NOW_PLAYING_PLACEHOLDER = " ~~~~~now~~~~~ ";

	@FXML
	public TableView<LastFmTrackData> recentTracksContainer;
	@FXML
	public ScrollPane trackInfoContainer;

	@Autowired
	private MusicLibraryLogicFacade musicLibraryLogicFacade;

	private final ScheduledExecutorService updateRecentTracksScheduler = Executors.newSingleThreadScheduledExecutor();
	private List<LastFmTrackData> currentRecentTracks;


	@FXML
	public void initialize() {
		LOG.debug("initialize FXML for {}", this.getClass().getName());

		recentTracksContainer.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		recentTracksContainer.setEditable(false);

	}

	@PostConstruct
	public void init() {
		LOG.debug("init bean {}", this.getClass().getName());

		updateRecentTracksScheduler.scheduleAtFixedRate(new UpdateUiRunnable(), 0L, 10L, TimeUnit.SECONDS);

		setRecentTracksContainerColumns();
		setRecentTracksContainerRowFactory();
		updateRecentTracksContainer();
	}

	@PreDestroy
	public void stop() {
		updateRecentTracksScheduler.shutdown();
	}

	private void setRecentTracksContainerColumns() {
		recentTracksContainer.getColumns().clear();

		TableColumn<LastFmTrackData, String> artistColumn = new TableColumn<>(LASTFM_PANEL_COLUMN_TITLE_ARTIST);
		artistColumn.setPrefWidth(LASTFM_PANEL_COLUMN_WIDTH_ARTIST);
		artistColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getArtist()));

		TableColumn<LastFmTrackData, String> trackTitleColumn = new TableColumn<>(LASTFM_PANEL_COLUMN_TITLE_TRACK_TITLE);
		trackTitleColumn.setPrefWidth(LASTFM_PANEL_COLUMN_WIDTH_TRACK_TITLE);
		trackTitleColumn.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));

		TableColumn<LastFmTrackData, String> scrobbleDateColumn = new TableColumn<>(LASTFM_PANEL_COLUMN_TITLE_SCROBBLE_DATE);
		scrobbleDateColumn.setPrefWidth(LASTFM_PANEL_COLUMN_WIDTH_SCROBBLE_DATE);
		scrobbleDateColumn.setCellValueFactory(data -> {
			if (data.getValue().isNowPlaying()) {
				return new SimpleStringProperty(LASTFM_PANEL_COLUMN_NOW_PLAYING_PLACEHOLDER);
			} else {
				SimpleDateFormat formatter = new SimpleDateFormat(LASTFM_PANEL_COLUMN_FORMAT_SCROBBLE_DATE);
				String scrobbleDateString = formatter.format(data.getValue().getScrobbleDate());
				return new SimpleStringProperty(scrobbleDateString);
			}
		});

		recentTracksContainer.getColumns().addAll(artistColumn, trackTitleColumn, scrobbleDateColumn);
	}

	private void setRecentTracksContainerRowFactory() {
		recentTracksContainer.setRowFactory(lv -> {
			TableRow<LastFmTrackData> tableRow = new TableRow<>();

			// prepare context menu
			ContextMenu contextMenu = new ContextMenu();

			MenuItem addToDeezerPlaylistMenuItem = new MenuItem("Add to Deezer playlist");
			addToDeezerPlaylistMenuItem.setOnAction(event -> {
				LastFmTrackData data = tableRow.getItem();
				boolean result = musicLibraryLogicFacade.addLastFmTrackToCurrentDeezerPlaylist(data);
				if (!result) {
					LOG.error("Failed to add track to Deezer playlist: track = {}", data);
				}
			});

			MenuItem addToDeezerLovedTracksMenuItem = new MenuItem("Add to Deezer loved tracks");

			contextMenu.getItems().addAll(
					addToDeezerPlaylistMenuItem, addToDeezerLovedTracksMenuItem
			);

			tableRow.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
				if (isNowEmpty) {
					tableRow.setContextMenu(null);
				} else {
					tableRow.setContextMenu(contextMenu);
				}
			});
			return tableRow;
		});
	}

	private final class UpdateUiRunnable implements Runnable {

		private UpdateUiRunnable() {
		}

		@Override
		public void run() {
			updateRecentTracksContainer();
		}
	}

	private void updateRecentTracksContainer() {
		List<LastFmTrackData> recentTracks = musicLibraryLogicFacade.getRecentTracksFromLastFm();

		// update only when the recent tracks list is changed
		if (!recentTracks.equals(currentRecentTracks)) {
			recentTracksContainer.getItems().clear();
			recentTracksContainer.getItems().addAll(recentTracks);
			currentRecentTracks = recentTracks;
		}
	}


}
