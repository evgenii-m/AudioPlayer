package ru.push.caudioplayer.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.push.caudioplayer.core.facades.AudioPlayerFacade;
import ru.push.caudioplayer.core.mediaplayer.pojo.LastFmTrackData;

import javax.annotation.PostConstruct;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;

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
	private AudioPlayerFacade audioPlayerFacade;

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

//		updateRecentTracksScheduler.scheduleAtFixedRate(new UpdateUiRunnable(), 0L, 1L, TimeUnit.SECONDS);

		setRecentTracksContainerColumns();
		updateRecentTracksContainer();
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

	private final class UpdateUiRunnable implements Runnable {

		private UpdateUiRunnable() {
		}

		@Override
		public void run() {
			updateRecentTracksContainer();
		}
	}

	private void updateRecentTracksContainer() {
		List<LastFmTrackData> recentTracks = audioPlayerFacade.getRecentTracksFromLastFm();

		// update only when the recent tracks list is changed
		if (!recentTracks.equals(currentRecentTracks)) {
			recentTracksContainer.getItems().clear();
			recentTracksContainer.getItems().addAll(recentTracks);
			currentRecentTracks = recentTracks;
		}
	}


}
