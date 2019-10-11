package ru.push.caudioplayer.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.AnchorPane;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.push.caudioplayer.AppMain;
import ru.push.caudioplayer.ConfigurationControllers;
import ru.push.caudioplayer.core.config.ApplicationConfigService;
import ru.push.caudioplayer.core.config.dto.PlaylistContainerViewConfigurations;
import ru.push.caudioplayer.core.facades.AudioPlayerFacade;
import ru.push.caudioplayer.core.facades.PlaylistLogicFacade;
import ru.push.caudioplayer.core.facades.dto.PlaylistData;
import ru.push.caudioplayer.core.facades.dto.TrackData;
import ru.push.caudioplayer.core.mediaplayer.DefaultAudioPlayerEventAdapter;
import ru.push.caudioplayer.utils.TrackTimeLabelBuilder;

import javax.annotation.Resource;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public abstract class PlaylistComponentBaseController {

	private static final Logger LOG = LoggerFactory.getLogger(PlaylistComponentBaseController.class);

	private static final String DEFAULT_PLAYLIST_EXPORT_FOLDER = "export/";
	private static final String NOW_PLAYING_MARKER = ">>";

	@FXML
	@Resource(name = "renamePopupView")
	protected ConfigurationControllers.View renamePopupView;
	@FXML
	@Resource(name = "confirmActionPopupView")
	protected ConfigurationControllers.View confirmActionPopupView;
	@FXML
	@Resource(name = "textInputActionPopupView")
	protected ConfigurationControllers.View textInputActionPopupView;

	@Autowired
	protected AppMain appMain;
	@Autowired
	protected AudioPlayerFacade audioPlayerFacade;
	@Autowired
	protected ApplicationConfigService applicationConfigService;
	@Autowired
	protected RenamePopupController renamePopupController;
	@Autowired
	protected ConfirmActionPopupController confirmActionPopupController;

	protected List<PlaylistData> playlists;
	protected PlaylistData displayedPlaylist;


	@FXML
	public void initialize() {
		LOG.debug("initialize FXML for {}", this.getClass().getName());
	}

	public void init() {
		// configure playlist browser container
		playlists = getPlaylistLogicFacade().getPlaylists();
		displayedPlaylist = playlists.stream().findFirst().orElse(null);
		setPlaylistBrowserContainerCellFactory(getPlaylistBrowserContainer());
		setPlaylistBrowserContainerItems(getPlaylistBrowserContainer(), playlists);

		// configure playlist content container
		setPlaylistContainerColumns(getPlaylistContentContainer(),
				applicationConfigService.getPlaylistContainerViewConfigurations());
		setPlaylistContentContainerRowFactory(getPlaylistContentContainer());
		setPlaylistContentContainerItems(displayedPlaylist);


		// bind selected item of playlist browser changes to show playlist action
		getPlaylistBrowserContainer().getSelectionModel().selectedItemProperty().addListener(
				(observable, oldValue, newValue) -> {
					if (newValue != null) {
						displayedPlaylist = newValue;
						setPlaylistContentContainerItems(displayedPlaylist);
					}
				});

		// skip unused mouse press action for playlist browser containers
		getPlaylistBrowserContainer().addEventFilter(MouseEvent.MOUSE_PRESSED,
				event -> {
					if (!event.isPrimaryButtonDown()) {
						event.consume();
					}
				});

	}

	protected abstract PlaylistLogicFacade getPlaylistLogicFacade();

	protected abstract AnchorPane getPlaylistPanelContainer();

	protected abstract ListView<PlaylistData> getPlaylistBrowserContainer();

	protected abstract TableView<TrackData> getPlaylistContentContainer();

	protected abstract void setPlaylistBrowserContainerCellFactory(ListView<PlaylistData> playlistBrowserContainer);

	protected abstract void setPlaylistContentContainerRowFactory(TableView<TrackData> playlistContentContainer);

	@FXML
	public void createNewPlaylist(ActionEvent actionEvent) {
		getPlaylistLogicFacade().createPlaylist();
	}

	@FXML
	public void refreshPlaylists(ActionEvent actionEvent) {
		playlists = getPlaylistLogicFacade().getPlaylists();
		displayedPlaylist = playlists.stream().findFirst().orElse(null);

		setPlaylistBrowserContainerItems(getPlaylistBrowserContainer(), playlists);
		setPlaylistContentContainerItems(displayedPlaylist);
	}

	protected List<MenuItem> getCommonPlaylistBrowserContainerMenuItems(ListCell<PlaylistData> cell) {
		MenuItem removeMenuItem = new MenuItem("Delete");
		removeMenuItem.setOnAction(event -> removePlaylistAction(event, cell));

		MenuItem renameMenuItem = new MenuItem("Rename");
		renameMenuItem.setOnAction(event -> renamePlaylistAction(event, cell));

		MenuItem exportMenuItem = new MenuItem("Export");
		exportMenuItem.setOnAction(event -> exportPlaylistAction(event, cell));

		return Arrays.asList(removeMenuItem, renameMenuItem, exportMenuItem);
	}

	private void renamePlaylistAction(ActionEvent event, ListCell<PlaylistData> cell) {
		PlaylistData playlistData = cell.getItem();
		if (playlistData.isReadOnly()) {
			LOG.warn("For read only playlists rename disabled");
			return;
		}

		Stage popupStage = createPopup("Rename", new Scene(renamePopupView.getView()));
		renamePopupController.setRenamedPlaylist(playlistData);
		popupStage.show();
	}

	private void removePlaylistAction(ActionEvent event, ListCell<PlaylistData> cell) {
		PlaylistData playlistData = cell.getItem();

		Stage popupStage = createPopup("Confirm action", new Scene(confirmActionPopupView.getView()));

		Consumer<Void> action = (o) -> {
			getPlaylistLogicFacade().deletePlaylist(playlistData.getUid());
		};
		String message = String.format("Remove playlist \'%s\'?", playlistData.getTitle());
		confirmActionPopupController.setAction(action, message);
		popupStage.show();

	}

	private void exportPlaylistAction(ActionEvent event, ListCell<PlaylistData> cell) {
		PlaylistData playlist = cell.getItem();

		try {
			Path exportFolderPath = Paths.get(DEFAULT_PLAYLIST_EXPORT_FOLDER);
			if (Files.notExists(exportFolderPath) || !Files.isDirectory(exportFolderPath)) {
				Files.createDirectories(exportFolderPath);
			}
			getPlaylistLogicFacade().exportPlaylistToFile(playlist.getUid(), DEFAULT_PLAYLIST_EXPORT_FOLDER);
		} catch (IOException e) {
			LOG.error("Export playlist error", e);
		}
	}

	protected List<MenuItem> getCommonPlaylistContentContainerMenuItems(TableView<TrackData> playlistContentContainer) {

		MenuItem showLastFmTrackInfoMenuItem = new MenuItem("Show Last.fm track info");

		MenuItem removeMenuItem = new MenuItem("Delete");
		removeMenuItem.setOnAction(event -> {
			if (displayedPlaylist != null) {
				List<String> selectedTracksUid = playlistContentContainer.getSelectionModel().getSelectedItems().stream()
						.map(TrackData::getTrackUid)
						.collect(Collectors.toList());
				getPlaylistLogicFacade().deleteItemsFromPlaylist(displayedPlaylist.getUid(), selectedTracksUid);
			}
		});

		return Arrays.asList(showLastFmTrackInfoMenuItem, removeMenuItem);
	}

	protected void setPlaylistBrowserContainerItems(ListView<PlaylistData> container, List<PlaylistData> playlists) {
		container.getItems().clear();
		if (playlists != null) {
			container.getItems().addAll(playlists);
			if ((displayedPlaylist != null) && playlists.contains(displayedPlaylist)) {
				container.getSelectionModel().select(displayedPlaylist);
			}
		}
	}

	protected void setPlaylistContentContainerItems(PlaylistData playlistData) {
		getPlaylistContentContainer().getItems().clear();
		if (playlistData != null) {
			getPlaylistContentContainer().getItems().addAll(playlistData.getTracks());
		}
	}

	private void setPlaylistContainerColumns(TableView<TrackData> playlistContainer,
																					 PlaylistContainerViewConfigurations viewConfigurations) {
		Map<String, PlaylistContainerViewConfigurations.PlaylistContainerColumn> columnsConfigurations =
				viewConfigurations.getColumns().stream()
						.collect(Collectors.toMap(
								PlaylistContainerViewConfigurations.PlaylistContainerColumn::getName,
								columnConf -> columnConf,
								(e1, e2) -> e1
						));

		playlistContainer.getColumns().clear();

		PlaylistContainerViewConfigurations.PlaylistContainerColumn columnConfiguration;

		columnConfiguration = columnsConfigurations.get(PlaylistContainerViewConfigurations.COLUMN_NOW_PLAYING_NAME);
		TableColumn<TrackData, String> nowPlayingCol = new TableColumn<>(columnConfiguration.getTitle());
		nowPlayingCol.setUserData(columnConfiguration.getName());
		nowPlayingCol.setPrefWidth(columnConfiguration.getWidth());
		nowPlayingCol.setCellValueFactory(data -> new SimpleStringProperty(
				data.getValue().isNowPlaying() ? NOW_PLAYING_MARKER : StringUtils.EMPTY
		));

		columnConfiguration = columnsConfigurations.get(PlaylistContainerViewConfigurations.COLUMN_NUMBER_NAME);
		TableColumn<TrackData, String> numberCol = new TableColumn<>(columnConfiguration.getTitle());
		numberCol.setUserData(columnConfiguration.getName());
		numberCol.setPrefWidth(columnConfiguration.getWidth());
		numberCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTrackNumber()));

		columnConfiguration = columnsConfigurations.get(PlaylistContainerViewConfigurations.COLUMN_ARTIST_NAME);
		TableColumn<TrackData, String> artistCol = new TableColumn<>(columnConfiguration.getTitle());
		artistCol.setUserData(columnConfiguration.getName());
		artistCol.setPrefWidth(columnConfiguration.getWidth());
		artistCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getArtist()));

		columnConfiguration = columnsConfigurations.get(PlaylistContainerViewConfigurations.COLUMN_ALBUM_NAME);
		TableColumn<TrackData, String> albumCol = new TableColumn<>(columnConfiguration.getTitle());
		albumCol.setUserData(columnConfiguration.getName());
		albumCol.setPrefWidth(columnConfiguration.getWidth());
		albumCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getAlbum()));

		columnConfiguration = columnsConfigurations.get(PlaylistContainerViewConfigurations.COLUMN_TITLE_NAME);
		TableColumn<TrackData, String> titleCol = new TableColumn<>(columnConfiguration.getTitle());
		titleCol.setUserData(columnConfiguration.getName());
		titleCol.setPrefWidth(columnConfiguration.getWidth());
		titleCol.setCellValueFactory(data -> new SimpleStringProperty(data.getValue().getTitle()));

		columnConfiguration = columnsConfigurations.get(PlaylistContainerViewConfigurations.COLUMN_LENGTH_NAME);
		TableColumn<TrackData, String> lengthCol = new TableColumn<>(columnConfiguration.getTitle());
		lengthCol.setUserData(columnConfiguration.getName());
		lengthCol.setPrefWidth(columnConfiguration.getWidth());
		lengthCol.setCellValueFactory(data ->
				new SimpleStringProperty(TrackTimeLabelBuilder.buildTimeLabel(data.getValue().getLength())));

		playlistContainer.getColumns().addAll(nowPlayingCol, numberCol, artistCol, albumCol, titleCol, lengthCol);
	}

	protected void savePlaylistContainerViewConfiguration() {
		List<PlaylistContainerViewConfigurations.PlaylistContainerColumn> columns = getPlaylistContentContainer().getColumns().stream()
				.map(tc ->
						new PlaylistContainerViewConfigurations.PlaylistContainerColumn(
								(String) tc.getUserData(),
								tc.getText(),
								tc.getWidth()
						)
				).collect(Collectors.toList());
		PlaylistContainerViewConfigurations viewConfigurations = new PlaylistContainerViewConfigurations(columns);
		applicationConfigService.savePlaylistContainerViewConfigurations(viewConfigurations);
	}

	protected Stage createPopup(String title, Scene scene) {
		Stage popupStage = new Stage();
		Stage primaryStage = (Stage) getPlaylistBrowserContainer().getScene().getWindow();
		popupStage.setTitle(title);
		popupStage.setResizable(false);
		popupStage.setScene(scene);
		popupStage.initModality(Modality.WINDOW_MODAL);
		popupStage.initOwner(primaryStage);
		return popupStage;
	}

	Optional<PlaylistData> getDisplayedPlaylist() {
		if (displayedPlaylist == null) {
			LOG.warn("Displayed playlist not set");
		}
		return Optional.ofNullable(displayedPlaylist);
	}

	protected void filterPlaylistsBySearchQuery(String searchQuery) {
		List<String> searchQueryWords = Stream.of(searchQuery.trim().split("\\s+"))
				.map(String::trim)
				.collect(Collectors.toList());

		ListView<PlaylistData> playlistBrowserContainer = getPlaylistBrowserContainer();
		List<PlaylistData> filteredPlaylists = CollectionUtils.isNotEmpty(searchQueryWords) ?
				playlists.stream()
						.filter(o -> searchQueryWords.stream().allMatch(s -> o.getTitle().toLowerCase().contains(s.toLowerCase())))
						.collect(Collectors.toList()) :
				playlists;
		setPlaylistBrowserContainerItems(playlistBrowserContainer, filteredPlaylists);
	}

	protected abstract class BaseAudioPlayerEventAdapter extends DefaultAudioPlayerEventAdapter {

		@Override
		public void changedPlaylist(PlaylistData playlistData) {
			updatePlaylistData(playlistData);
			updateContainerItemPlaylistData(playlistData);
			if ((displayedPlaylist != null) && (displayedPlaylist.equals(playlistData))) {
				setPlaylistContentContainerItems(playlistData);
			}
		}

		@Override
		public void createdNewPlaylist(PlaylistData playlistData) {
			playlists.add(playlistData);
			getPlaylistBrowserContainer().getItems().add(playlistData);
			getPlaylistBrowserContainer().getSelectionModel().select(playlistData);
			setPlaylistContentContainerItems(playlistData);
		}

		@Override
		public void changedTrackData(PlaylistData playlistData, TrackData trackData) {
			updatePlaylistData(playlistData);
			getPlaylistBrowserContainer().getItems().stream()
					.filter(o -> o.equals(playlistData)).findFirst()
					.ifPresent(p -> {
						p.getTracks().stream()
								.filter(o -> o.equals(trackData)).findFirst()
								.ifPresent(t -> {
									int trackIndex = p.getTracks().indexOf(t);
									p.getTracks().set(trackIndex, trackData);
								});
					});
			if ((displayedPlaylist != null) && (displayedPlaylist.equals(playlistData))) {
				updateContainerItemTrackData(trackData);
			}
		}

		@Override
		public void renamedPlaylist(PlaylistData playlistData) {
			updatePlaylistData(playlistData);
			updateContainerItemPlaylistData(playlistData);
		}

		@Override
		public void deletedPlaylist(PlaylistData playlistData) {
			playlists.remove(playlistData);
			getPlaylistBrowserContainer().getItems().stream()
					.filter(p -> p.getUid().equals(playlistData.getUid())).findFirst()
					.ifPresent(p -> getPlaylistBrowserContainer().getItems().remove(p));
			getPlaylistBrowserContainer().refresh();
		}

		@Override
		public void changedNowPlayingTrack(TrackData trackData) {
			getPlaylistBrowserContainer().getItems().stream()
					.map(PlaylistData::getTracks)
					.flatMap(Collection::stream)
					.forEach(o -> o.setNowPlaying(o.equals(trackData) && trackData.isNowPlaying()));
			if ((displayedPlaylist != null) && (displayedPlaylist.getUid().equals(trackData.getPlaylistUid()))) {
				getPlaylistBrowserContainer().refresh();
			}
		}

		protected void updatePlaylistData(PlaylistData playlistData) {
			int playlistIndex = playlists.indexOf(playlistData);
			if (playlistIndex >= 0) {
				playlists.set(playlistIndex, playlistData);
				if (displayedPlaylist.equals(playlistData)) {
					displayedPlaylist = playlistData;
				}
			} else {
				LOG.warn("Playlist not found in controller: playlistData = {}", playlistData);
			}
		}

		protected void updateContainerItemPlaylistData(PlaylistData playlistData) {
			getPlaylistBrowserContainer().getItems().stream()
					.filter(p -> p.getUid().equals(playlistData.getUid())).findFirst()
					.ifPresent(p -> {
						int itemIndex = getPlaylistBrowserContainer().getItems().indexOf(p);
						getPlaylistBrowserContainer().getItems().set(itemIndex, playlistData);
					});
		}

		protected void updateContainerItemTrackData(TrackData trackData) {
			getPlaylistContentContainer().getItems().stream()
					.filter(o -> o.equals(trackData)).findFirst()
					.ifPresent(o -> {
						int itemIndex = getPlaylistContentContainer().getItems().indexOf(o);
						getPlaylistContentContainer().getItems().set(itemIndex, trackData);
						getPlaylistContentContainer().getFocusModel().focus(itemIndex);
					});
		}
	}

}
