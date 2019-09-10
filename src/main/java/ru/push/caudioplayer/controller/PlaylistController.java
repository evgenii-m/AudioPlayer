package ru.push.caudioplayer.controller;

import javafx.beans.property.SimpleStringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.HBox;
import javafx.stage.FileChooser;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.push.caudioplayer.ConfigurationControllers;
import ru.push.caudioplayer.core.facades.AudioPlayerFacade;
import ru.push.caudioplayer.core.facades.MusicLibraryLogicFacade;
import ru.push.caudioplayer.core.facades.dto.PlaylistData;
import ru.push.caudioplayer.core.facades.dto.PlaylistType;
import ru.push.caudioplayer.core.facades.dto.TrackData;
import ru.push.caudioplayer.core.mediaplayer.DefaultAudioPlayerEventAdapter;
import ru.push.caudioplayer.core.config.ApplicationConfigService;
import ru.push.caudioplayer.core.config.dto.PlaylistContainerViewConfigurations;
import ru.push.caudioplayer.utils.TrackTimeLabelBuilder;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/12/17
 */
@SuppressWarnings("unchecked")
public class PlaylistController {

  private static final Logger LOG = LoggerFactory.getLogger(PlaylistController.class);

  private static final String DEFAULT_PLAYLIST_EXPORT_FOLDER = "export/";

  @FXML
  public HBox playlistBlockContainer;
	@FXML
  private TabPane playlistBrowserTabPane;
	@FXML
	private Tab localPlaylistsTab;
	@FXML
	private Tab deezerPlaylistsTab;
	@FXML
	private ListView<PlaylistData> localPlaylistBrowserContainer;
	@FXML
	private ListView<PlaylistData> deezerPlaylistBrowserContainer;
	@FXML
	private TableView<TrackData> playlistContentContainer;
  @FXML
  @Resource(name = "renamePopupView")
  private ConfigurationControllers.View renamePopupView;
	@FXML
	@Resource(name = "confirmActionPopupView")
	private ConfigurationControllers.View confirmActionPopupView;
	@FXML
	@Resource(name = "textInputActionPopupView")
	private ConfigurationControllers.View textInputActionPopupView;

  @Autowired
  private AudioPlayerFacade audioPlayerFacade;
  @Autowired
	private MusicLibraryLogicFacade musicLibraryLogicFacade;
  @Autowired
  private TrackTimeLabelBuilder trackTimeLabelBuilder;
  @Autowired
  private ApplicationConfigService applicationConfigService;

  private PlaylistData displayedPlaylist;

  private Scene renamePopupScene;
  private Scene confirmActionPopupScene;
	private Scene textInputActionPopupScene;

  @FXML
  public void initialize() {
		LOG.debug("initialize FXML for {}", this.getClass().getName());

    localPlaylistBrowserContainer.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		deezerPlaylistBrowserContainer.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

		playlistContentContainer.setEditable(false);
		playlistContentContainer.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
  }

  @PostConstruct
  public void init() {
		LOG.debug("init bean {}", this.getClass().getName());

		List<PlaylistData> localPlaylists = musicLibraryLogicFacade.getLocalPlaylists();
		List<PlaylistData> deezerPlaylists = musicLibraryLogicFacade.getDeezerPlaylists();

		renamePopupScene = new Scene(renamePopupView.getView());
		confirmActionPopupScene = new Scene(confirmActionPopupView.getView());
		textInputActionPopupScene = new Scene(textInputActionPopupView.getView());

		AudioPlayerEventAdapter eventAdapter = new AudioPlayerEventAdapter();
		audioPlayerFacade.addEventListener(eventAdapter);
		musicLibraryLogicFacade.addEventListener(eventAdapter);

		String displayedPlaylistUid = applicationConfigService.getDisplayedPlaylistUid();
		displayedPlaylist = Stream.of(localPlaylists, deezerPlaylists)
				.flatMap(Collection::stream)
				.filter(o -> o.getUid().equals(displayedPlaylistUid))
				.findFirst().orElse(null);

		// switch browser container to tab with displayed playlist
		selectPlaylistBrowserTab(displayedPlaylist);

		// configure playlist browser container
		setPlaylistBrowserContainerCellFactory(localPlaylistBrowserContainer);
		setPlaylistBrowserContainerCellFactory(deezerPlaylistBrowserContainer);
		setPlaylistBrowserContainerItems(localPlaylistBrowserContainer, localPlaylists);
		setPlaylistBrowserContainerItems(deezerPlaylistBrowserContainer, deezerPlaylists);

		// configure playlist content container
    setPlaylistContainerColumns(playlistContentContainer,
				applicationConfigService.getPlaylistContainerViewConfigurations());
		setPlaylistContentContainerRowFactory();
		setPlaylistContentContainerItems(displayedPlaylist);

		// bind playlist container mouse click event to play track action
    playlistContentContainer.setOnMouseClicked(mouseEvent -> {
      if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && (mouseEvent.getClickCount() == 2)) {
      	if (displayedPlaylist != null) {
					TrackData trackData = playlistContentContainer.getFocusModel().getFocusedItem();
					audioPlayerFacade.playTrack(displayedPlaylist.getUid(), trackData.getTrackUid());
				}
      }
    });

		// skip unused mouse press action for playlist browser containers
		Stream.of(localPlaylistBrowserContainer, deezerPlaylistBrowserContainer).forEach(o ->
				o.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
					if (!event.isPrimaryButtonDown()) {
						event.consume();
					}
				})
		);
    // bind selected item of playlist browser changes to show playlist action
		Stream.of(localPlaylistBrowserContainer, deezerPlaylistBrowserContainer).forEach(o ->
				o.getSelectionModel().selectedItemProperty()
						.addListener((observable, oldValue, newValue) -> {
							if (newValue != null) {
								displayedPlaylist = newValue;
								setPlaylistContentContainerItems(displayedPlaylist);
							}
						})
		);

  }

  private void setPlaylistBrowserContainerCellFactory(ListView<PlaylistData> playlistBrowserContainer) {
    playlistBrowserContainer.setCellFactory(lv -> {
      ListCell<PlaylistData> cell = new ListCell<PlaylistData>() {
        @Override
        protected void updateItem(PlaylistData item, boolean empty) {
          super.updateItem(item, empty);
          if (empty || item == null || item.getTitle() == null) {
            setText(null);
          } else {
            setText(item.getTitle());
          }
        }
      };

      // prepare context menu
      ContextMenu contextMenu = new ContextMenu();

			MenuItem removeMenuItem = new MenuItem("Delete");
			removeMenuItem.setOnAction(event -> removePlaylistAction(event, cell));

			MenuItem renameMenuItem = new MenuItem("Rename");
			renameMenuItem.setOnAction(event -> renamePlaylistAction(event, cell));


			MenuItem exportMenuItem = new MenuItem("Export");
			exportMenuItem.setOnAction(event -> exportPlaylistAction(event, cell));

			contextMenu.getItems().addAll(removeMenuItem, renameMenuItem, exportMenuItem);

      cell.emptyProperty().addListener((obs, wasEmpty, isNowEmpty) -> {
        if (isNowEmpty) {
          cell.setContextMenu(null);
        } else {
          cell.setContextMenu(contextMenu);
        }
      });
      return cell;
    });
  }

	@PreDestroy
	public void stop() {
		// save active and displayed playlists UID
		musicLibraryLogicFacade.getActivePlaylist()
				.ifPresent(o -> applicationConfigService.saveActivePlaylist(o.getUid()));
		if (displayedPlaylist != null) {
			applicationConfigService.saveDisplayedPlaylist(displayedPlaylist.getUid());
		}

		// save view configuration
		savePlaylistContainerViewConfiguration();
	}

	private Stage createPopup(String title, Scene scene) {
		Stage popupStage = new Stage();
		Stage primaryStage = (Stage) localPlaylistBrowserContainer.getScene().getWindow();
		popupStage.setTitle(title);
		popupStage.setResizable(false);
		popupStage.setScene(scene);
		popupStage.initModality(Modality.WINDOW_MODAL);
		popupStage.initOwner(primaryStage);
		return popupStage;
	}

  private void renamePlaylistAction(ActionEvent event, ListCell<PlaylistData> cell) {
    assert renamePopupView.getController() instanceof RenamePopupController;

		PlaylistData playlistData = cell.getItem();
		if (playlistData.isReadOnly()) {
			LOG.warn("For read only playlists rename disabled");
			return;
		}

		Stage popupStage = createPopup("Rename", renamePopupScene);
		((RenamePopupController) renamePopupView.getController()).setRenamedPlaylist(playlistData);
    popupStage.show();
  }

  private void removePlaylistAction(ActionEvent event, ListCell<PlaylistData> cell) {
		PlaylistData playlistData = cell.getItem();

		if (playlistData.isReadOnly()) {
			LOG.warn("For read only playlists delete disabled");
			return;
		}

		Stage popupStage = createPopup("Confirm action", confirmActionPopupScene);

		Consumer<Void> action = (o) -> {
			musicLibraryLogicFacade.deletePlaylist(playlistData.getUid());
		};
		String message = String.format("Remove playlist \'%s\'?", playlistData.getTitle());
		((ConfirmActionPopupController) confirmActionPopupView.getController()).setAction(action, message);
		popupStage.show();

  }

  private void exportPlaylistAction(ActionEvent event, ListCell<PlaylistData> cell) {
		PlaylistData playlist = cell.getItem();

		try {
			Path exportFolderPath = Paths.get(DEFAULT_PLAYLIST_EXPORT_FOLDER);
			if (Files.notExists(exportFolderPath) || !Files.isDirectory(exportFolderPath)) {
				Files.createDirectories(exportFolderPath);
			}
			musicLibraryLogicFacade.exportPlaylistToFile(playlist.getUid(), DEFAULT_PLAYLIST_EXPORT_FOLDER);
		} catch (IOException e) {
			LOG.error("Export playlist error", e);
		}
	}

	private void setPlaylistBrowserContainerItems(ListView<PlaylistData> container, List<PlaylistData> playlists) {
		if (CollectionUtils.isNotEmpty(playlists)) {
			container.getItems().clear();
			container.getItems().addAll(playlists);
			if ((displayedPlaylist != null) && playlists.contains(displayedPlaylist)) {
				container.getSelectionModel().select(displayedPlaylist);
			}
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
				new SimpleStringProperty(trackTimeLabelBuilder.buildTimeString(data.getValue().getLength())));

		playlistContainer.getColumns().addAll(numberCol, artistCol, albumCol, titleCol, lengthCol);
	}

  private void setPlaylistContentContainerRowFactory() {
    playlistContentContainer.setRowFactory(lv -> {
      TableRow<TrackData> tableRow = new TableRow<>();

      // prepare context menu
      ContextMenu contextMenu = new ContextMenu();
      MenuItem lookupOnLastfmMenuItem = new MenuItem("Lookup on last.fm");

			MenuItem removeMenuItem = new MenuItem("Delete");
			removeMenuItem.setOnAction(event -> {
				if (displayedPlaylist != null) {
					List<String> selectedTracksUid = playlistContentContainer.getSelectionModel().getSelectedItems().stream()
							.map(TrackData::getTrackUid)
							.collect(Collectors.toList());
					musicLibraryLogicFacade.deleteItemsFromPlaylist(displayedPlaylist.getUid(), selectedTracksUid);
				}
			});

      MenuItem propertiesMenuItem = new MenuItem("Properties");
      MenuItem moveToNewMenuItem = new MenuItem("Move to new playlist");

      contextMenu.getItems().addAll(lookupOnLastfmMenuItem, removeMenuItem, propertiesMenuItem,
          moveToNewMenuItem);

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

  private void setPlaylistContentContainerItems(PlaylistData playlistData) {
  	if (playlistData != null) {
			playlistContentContainer.getItems().clear();
			playlistContentContainer.getItems().addAll(playlistData.getTracks());
		}
  }

  private void savePlaylistContainerViewConfiguration() {
    List<PlaylistContainerViewConfigurations.PlaylistContainerColumn> columns = playlistContentContainer.getColumns().stream()
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

  @FXML
	public void createNewPlaylist(ActionEvent actionEvent) {
  	if ((displayedPlaylist == null) || displayedPlaylist.isLocal()) {
			musicLibraryLogicFacade.createLocalPlaylist();
		} else if (displayedPlaylist.isDeezer()) {
  		musicLibraryLogicFacade.createDeezerPlaylist();
		}
	}

	@FXML
	public void refreshPlaylists(ActionEvent actionEvent) {
		musicLibraryLogicFacade.reloadPlaylists();

		List<PlaylistData> localPlaylists = musicLibraryLogicFacade.getLocalPlaylists();
		List<PlaylistData> deezerPlaylists = musicLibraryLogicFacade.getDeezerPlaylists();

		selectPlaylistBrowserTab(displayedPlaylist);
		setPlaylistBrowserContainerItems(localPlaylistBrowserContainer, localPlaylists);
		setPlaylistBrowserContainerItems(deezerPlaylistBrowserContainer, deezerPlaylists);
		setPlaylistContentContainerItems(displayedPlaylist);
	}

	private ListView<PlaylistData> getCurrentPlaylistContainer(PlaylistData playlistData) {
  	return playlistData.isLocal() ? localPlaylistBrowserContainer : deezerPlaylistBrowserContainer;
	}

	private void selectPlaylistBrowserTab(PlaylistData playlistData) {
  	if (playlistData != null) {
			Tab activeTab = playlistData.isLocal() ? localPlaylistsTab : deezerPlaylistsTab;
			playlistBrowserTabPane.getSelectionModel().select(activeTab);
		}
	}

	@FXML
	public void addFilesToPlaylist(ActionEvent actionEvent) {
  	if ((displayedPlaylist != null) && (displayedPlaylist.isLocal())) {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Open file(s)");
			// WARNING: if this code throws JVM crashing, need to add JVM option '-DVLCJ_INITX=no'
			List<File> files = fileChooser.showOpenMultipleDialog(playlistBlockContainer.getScene().getWindow());
			if (CollectionUtils.isNotEmpty(files)) {
				musicLibraryLogicFacade.addFilesToPlaylist(displayedPlaylist.getUid(), files);
			}
		}
	}

	@FXML
	public void addStreamToPlaylist(ActionEvent actionEvent) {
		if ((displayedPlaylist != null) && (displayedPlaylist.isLocal())) {
			Stage popupStage = createPopup("Add HTTP stream(s) source", textInputActionPopupScene);
			Consumer<String> action = inputText -> {
				String[] inputLines = inputText.split("\n");
				musicLibraryLogicFacade.addLocationsToPlaylist(displayedPlaylist.getUid(), Arrays.asList(inputLines));
			};
			((TextInputActionPopupController) textInputActionPopupView.getController()).setAction(action);
			popupStage.show();
		}
	}

	private final class AudioPlayerEventAdapter extends DefaultAudioPlayerEventAdapter {

    @Override
    public void changedPlaylist(PlaylistData playlistData) {
			updateContainerItemPlaylistData(playlistData);
			if ((displayedPlaylist != null) && (displayedPlaylist.equals(playlistData))) {
				setPlaylistContentContainerItems(playlistData);
			}
    }

    @Override
    public void createdNewPlaylist(PlaylistData playlistData) {
			selectPlaylistBrowserTab(playlistData);
    	getCurrentPlaylistContainer(playlistData).getItems().add(playlistData);
			getCurrentPlaylistContainer(playlistData).getSelectionModel().select(playlistData);
			setPlaylistContentContainerItems(playlistData);
    }

    @Override
    public void changedTrackData(PlaylistData playlistData, TrackData trackData) {
			updateContainerItemPlaylistData(playlistData);
			if ((displayedPlaylist != null) && (displayedPlaylist.equals(playlistData))) {
				playlistContentContainer.getItems().stream()
						.filter(o -> o.equals(trackData)).findFirst()
						.ifPresent(o -> {
							int itemIndex = playlistContentContainer.getItems().indexOf(o);
							playlistContentContainer.getItems().set(itemIndex, trackData);
							playlistContentContainer.getFocusModel().focus(itemIndex);
						});
			}
    }

    @Override
    public void renamedPlaylist(PlaylistData playlistData) {
			updateContainerItemPlaylistData(playlistData);
    }

		@Override
		public void deletedPlaylist(PlaylistData playlistData) {
			ListView<PlaylistData> container = getCurrentPlaylistContainer(playlistData);
			container.getItems().stream()
					.filter(p -> p.getUid().equals(playlistData.getUid())).findFirst()
					.ifPresent(p -> container.getItems().remove(p));
			container.refresh();
		}

		private void updateContainerItemPlaylistData(PlaylistData playlistData) {
			ListView<PlaylistData> container = getCurrentPlaylistContainer(playlistData);
			container.getItems().stream()
					.filter(p -> p.getUid().equals(playlistData.getUid())).findFirst()
					.ifPresent(p -> {
						int itemIndex = container.getItems().indexOf(p);
						container.getItems().set(itemIndex, playlistData);
					});
			container.refresh();
		}
	}
}
