package ru.push.caudioplayer.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.Label;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextArea;
import javafx.scene.control.ToggleButton;
import javafx.scene.image.Image;
import javafx.scene.image.ImageView;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.Pane;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.push.caudioplayer.core.facades.MusicLibraryLogicFacade;
import ru.push.caudioplayer.core.facades.dto.LastFmTrackData;
import ru.push.caudioplayer.core.facades.dto.LastFmTrackInfoData;
import ru.push.caudioplayer.core.facades.dto.PlaylistData;
import ru.push.caudioplayer.utils.TrackTimeLabelBuilder;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.text.SimpleDateFormat;
import java.util.List;
import java.util.Optional;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.stream.Stream;

public class LastFmPanelController {

	private static final Logger LOG = LoggerFactory.getLogger(LastFmPanelController.class);

	private static final long UPDATE_RECENT_TRACKS_PERIOD = 10L;
	private static final TimeUnit UPDATE_RECENT_TRACKS_PERIOD_TIME_UNIT = TimeUnit.SECONDS;
	private static final String LASTFM_PANEL_COLUMN_TITLE_ARTIST = "Artist";
	private static final double LASTFM_PANEL_COLUMN_WIDTH_ARTIST = 135;
	private static final String LASTFM_PANEL_COLUMN_TITLE_TRACK_TITLE = "Title";
	private static final double LASTFM_PANEL_COLUMN_WIDTH_TRACK_TITLE = 200;
	private static final String LASTFM_PANEL_COLUMN_TITLE_SCROBBLE_DATE = "Date";
	private static final double LASTFM_PANEL_COLUMN_WIDTH_SCROBBLE_DATE = 125;
	private static final String LASTFM_PANEL_COLUMN_FORMAT_SCROBBLE_DATE = "dd-MM-yyyy  HH:mm:SS";
	private static final String LASTFM_PANEL_COLUMN_NOW_PLAYING_PLACEHOLDER = " ~~~~~now~~~~~ ";
	private static final String TRACK_INFO_IMAGE_STUB_URL = "content/images/image_stub_1.png";
	private static final String TRACK_INFO_EMPTY_LABEL_PLACEHOLDER = "-";
	private static final String LOVED_TRACK_TOGGLE_BUTTON_TEXT = "Loved Track";
	private static final String ADD_TO_LOVED_TRACK_TOGGLE_BUTTON_TEXT = "Add to Loved";
	private static final double TRACK_INFO_IMAGE_WIDTH = 160;
	private static final double TRACK_INFO_IMAGE_HEIGHT = 160;

	@FXML
	public TableView<LastFmTrackData> recentTracksContainer;
	@FXML
	public Pane trackInfoContainer;
	@FXML
	public Label artistLinkLabel;
	@FXML
	public Label albumLinkLabel;
	@FXML
	public Label titleLinkLabel;
	@FXML
	public Label trackDurationLabel;
	@FXML
	public Label listenersCountLabel;
	@FXML
	public Label totalScrobblesLabel;
	@FXML
	public Label userScrobblesLabel;
	@FXML
	public ToggleButton lovedTrackToggleButton;
	@FXML
	public ImageView trackInfoImage;
	@FXML
	public TextArea trackInfoDescriptionTextArea;
	@FXML
	public ListView trackInfoTagsContainer;

	@Autowired
	private MusicLibraryLogicFacade musicLibraryLogicFacade;
	@Autowired
	private PlaylistController playlistController;

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

		updateRecentTracksScheduler.scheduleAtFixedRate(new UpdateUiRunnable(), UPDATE_RECENT_TRACKS_PERIOD,
				UPDATE_RECENT_TRACKS_PERIOD, UPDATE_RECENT_TRACKS_PERIOD_TIME_UNIT);

		setRecentTracksContainerColumns();
		setRecentTracksContainerRowFactory();
		updateRecentTracksContainer(false);
		updateTrackInfoContainer(null);

		recentTracksContainer.setOnMouseClicked(mouseEvent -> {
			if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && (mouseEvent.getClickCount() == 2)) {
				LastFmTrackData trackData = recentTracksContainer.getFocusModel().getFocusedItem();
				LastFmTrackInfoData trackInfoData = musicLibraryLogicFacade.getLastFmTrackInfo(trackData);
				updateTrackInfoContainer(trackInfoData);
			}
		});
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
				Optional<PlaylistData> displayedPlaylist = playlistController.getDisplayedPlaylist();
				displayedPlaylist.ifPresent(playlist -> {
					LastFmTrackData data = tableRow.getItem();
					musicLibraryLogicFacade.addLastFmTrackDeezerPlaylist(playlist.getUid(), data);
				});
			});

			MenuItem addToDeezerLovedTracksMenuItem = new MenuItem("Add to Deezer loved tracks");
			addToDeezerLovedTracksMenuItem.setOnAction(event -> {
				LastFmTrackData data = tableRow.getItem();
				musicLibraryLogicFacade.addLastFmTrackToDeezerLovedTracksAndMonthlyPlaylist(data);
			});

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

	@FXML
	public void loadMoreRecentTracks(ActionEvent actionEvent) {
		updateRecentTracksContainer(true);
	}

	@FXML
	public void refreshRecentTracks(ActionEvent actionEvent) {
		updateRecentTracksContainer(false);
	}

	synchronized private void updateRecentTracksContainer(boolean fetchMore) {
		List<LastFmTrackData> recentTracks = musicLibraryLogicFacade.getRecentTracksFromLastFm(fetchMore);

		// update only when the recent tracks list is changed
		if (!recentTracks.equals(currentRecentTracks)) {
			recentTracksContainer.getItems().clear();
			recentTracksContainer.getItems().addAll(recentTracks);
			currentRecentTracks = recentTracks;
		}
	}

	private void updateTrackInfoContainer(LastFmTrackInfoData trackInfoData) {
		if (trackInfoData != null) {
			Stream.of(
					artistLinkLabel, albumLinkLabel, titleLinkLabel, listenersCountLabel,
					totalScrobblesLabel, userScrobblesLabel, trackDurationLabel
			).forEach(o -> o.setDisable(false));

			artistLinkLabel.setText(trackInfoData.getArtistName());
			albumLinkLabel.setText((trackInfoData.getAlbumName() != null) ?
					trackInfoData.getAlbumName() : TRACK_INFO_EMPTY_LABEL_PLACEHOLDER
			);
			titleLinkLabel.setText(trackInfoData.getTrackName());
			trackDurationLabel.setText(TrackTimeLabelBuilder.buildTimeLabel(trackInfoData.getDuration()));
			listenersCountLabel.setText(String.valueOf(trackInfoData.getListenersCount()));
			totalScrobblesLabel.setText(String.valueOf(trackInfoData.getPlayCount()));
			userScrobblesLabel.setText((trackInfoData.getUserPlayCount() != null) ?
					String.valueOf(trackInfoData.getUserPlayCount()) : TRACK_INFO_EMPTY_LABEL_PLACEHOLDER
			);

			trackInfoDescriptionTextArea.setText((trackInfoData.getDescription() != null) ?
					trackInfoData.getDescription() : StringUtils.EMPTY
			);

			String imageUrl = TRACK_INFO_IMAGE_STUB_URL;
			if (trackInfoData.getLargeImageUrl() != null) {
				imageUrl = trackInfoData.getLargeImageUrl();
			} else if (trackInfoData.getMediumImageUrl() != null) {
				imageUrl = trackInfoData.getMediumImageUrl();
			} else if (trackInfoData.getSmallImageUrl() != null) {
				imageUrl = trackInfoData.getSmallImageUrl();
			}
			trackInfoImage.setImage(new Image(imageUrl, TRACK_INFO_IMAGE_WIDTH, TRACK_INFO_IMAGE_HEIGHT, false, false));

			lovedTrackToggleButton.setDisable(false);
			if (trackInfoData.isLovedTrack()) {
				lovedTrackToggleButton.setText(LOVED_TRACK_TOGGLE_BUTTON_TEXT);
				lovedTrackToggleButton.setSelected(true);
			} else {
				lovedTrackToggleButton.setText(ADD_TO_LOVED_TRACK_TOGGLE_BUTTON_TEXT);
				lovedTrackToggleButton.setSelected(false);
			}

		} else {
			Stream.of(
					artistLinkLabel, albumLinkLabel, titleLinkLabel, listenersCountLabel,
					totalScrobblesLabel, userScrobblesLabel, trackDurationLabel
			).forEach(o -> {
				o.setText(TRACK_INFO_EMPTY_LABEL_PLACEHOLDER);
				o.setDisable(true);
			});

			trackInfoImage.setImage(new Image(TRACK_INFO_IMAGE_STUB_URL, TRACK_INFO_IMAGE_WIDTH,
					TRACK_INFO_IMAGE_HEIGHT, false, false));
			trackInfoDescriptionTextArea.setText(StringUtils.EMPTY);

			lovedTrackToggleButton.setDisable(true);
			lovedTrackToggleButton.setText(ADD_TO_LOVED_TRACK_TOGGLE_BUTTON_TEXT);
			lovedTrackToggleButton.setSelected(false);
		}
	}

	private final class UpdateUiRunnable implements Runnable {

		private UpdateUiRunnable() {
		}

		@Override
		public void run() {
			updateRecentTracksContainer(false);
		}
	}

}
