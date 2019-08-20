package ru.push.caudioplayer.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.configuration2.ex.ConfigurationException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.push.caudioplayer.ConfigurationControllers;
import ru.push.caudioplayer.core.facades.AudioPlayerFacade;
import ru.push.caudioplayer.core.mediaplayer.DefaultAudioPlayerEventAdapter;
import ru.push.caudioplayer.core.facades.domain.AudioTrackData;
import ru.push.caudioplayer.core.facades.domain.PlaylistData;
import ru.push.caudioplayer.core.config.ApplicationConfigService;
import ru.push.caudioplayer.ui.AudioTrackPlaylistItem;
import ru.push.caudioplayer.core.facades.domain.configuration.PlaylistContainerViewConfigurations;
import ru.push.caudioplayer.utils.TrackTimeLabelBuilder;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import javax.annotation.Resource;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/12/17
 */
@SuppressWarnings("unchecked")
public class PlaylistController {

  private static final Logger LOG = LoggerFactory.getLogger(PlaylistController.class);

	@FXML
  private TabPane playlistsTabPane;
	@FXML
	private Tab localPlaylistsTab;
	@FXML
	private Tab deezerPlaylistsTab;
	@FXML
  private ListView<PlaylistData> localPlaylistBrowserContainer;
  @FXML
  private TableView<AudioTrackPlaylistItem> localPlaylistContainer;
	@FXML
	private ListView<PlaylistData> deezerPlaylistBrowserContainer;
	@FXML
	private TableView<AudioTrackPlaylistItem> deezerPlaylistContainer;

  @FXML
  @Resource(name = "renamePopupView")
  private ConfigurationControllers.View renamePopupView;

  @Autowired
  private AudioPlayerFacade audioPlayerFacade;
  @Autowired
  private TrackTimeLabelBuilder trackTimeLabelBuilder;
  @Autowired
  private ApplicationConfigService applicationConfigService;

  private Scene renamePopupScene;


  @FXML
  public void initialize() {
		LOG.debug("initialize FXML for {}", this.getClass().getName());

		playlistsTabPane.getSelectionModel().select(deezerPlaylistsTab);

    localPlaylistBrowserContainer.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    localPlaylistContainer.setEditable(false);
    localPlaylistContainer.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

		deezerPlaylistBrowserContainer.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		deezerPlaylistContainer.setEditable(false);
		deezerPlaylistContainer.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
  }

  @PostConstruct
  public void init() throws ConfigurationException {
		LOG.debug("init bean {}", this.getClass().getName());

    setPlaylistContainerColumns(localPlaylistContainer, applicationConfigService.getPlaylistContainerViewConfigurations());
		setPlaylistContainerRowFactory();

    renamePopupScene = new Scene(renamePopupView.getView());

    audioPlayerFacade.addEventListener(new AudioPlayerEventAdapter());

    localPlaylistContainer.setOnMouseClicked(mouseEvent -> {
      if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && (mouseEvent.getClickCount() == 2)) {
        PlaylistData displayedPlaylist = localPlaylistBrowserContainer.getSelectionModel().getSelectedItem();
        int trackPosition = localPlaylistContainer.getFocusModel().getFocusedCell().getRow();
        audioPlayerFacade.playTrack(displayedPlaylist.getUid(), trackPosition);
      }
    });

    List<PlaylistData> playlists = audioPlayerFacade.getPlaylists();
    PlaylistData displayedPlaylist = audioPlayerFacade.getDisplayedPlaylist();
    fillPlaylistBrowserContainer(playlists, displayedPlaylist);

    localPlaylistBrowserContainer.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
      if (!event.isPrimaryButtonDown()) {
        event.consume();
      }
    });

    localPlaylistBrowserContainer.getSelectionModel().selectedItemProperty().addListener(
        (observable, oldValue, newValue) -> {
          PlaylistData playlist = audioPlayerFacade.showPlaylist(newValue.getUid());
          setPlaylistContainerItems(playlist);
        });

    setPlaylistContainerItems(audioPlayerFacade.getDisplayedPlaylist());
    setPlaylistBrowserContainerCellFactory();
  }

	@PreDestroy
	public void stop() {
		savePlaylistContainerViewConfiguration();
	}

  private void setPlaylistBrowserContainerCellFactory() {
    localPlaylistBrowserContainer.setCellFactory(lv -> {
      ListCell<PlaylistData> cell = new ListCell<PlaylistData>() {
        @Override
        protected void updateItem(PlaylistData item, boolean empty) {
          super.updateItem(item, empty);
          if (empty || item == null || item.getName() == null) {
            setText(null);
          } else {
            setText(item.getName());
          }
        }
      };

      // prepare context menu
      ContextMenu contextMenu = new ContextMenu();

      MenuItem removeMenuItem = new MenuItem("Delete");
      removeMenuItem.setOnAction(event -> removePlaylistAction(event, cell));

      MenuItem renameMenuItem = new MenuItem("Rename");
      renameMenuItem.setOnAction(event -> renamePlaylistAction(event, cell));

      contextMenu.getItems().addAll(removeMenuItem, renameMenuItem);

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

  private void renamePlaylistAction(ActionEvent event, ListCell<PlaylistData> cell) {
    assert renamePopupView.getController() instanceof RenamePopupController;

    Stage popupStage = new Stage();
    Stage primaryStage = (Stage) localPlaylistBrowserContainer.getScene().getWindow();
    popupStage.setTitle("Rename");
    popupStage.setResizable(false);
    popupStage.setScene(renamePopupScene);
    popupStage.initModality(Modality.WINDOW_MODAL);
    popupStage.initOwner(primaryStage);
    ((RenamePopupController) renamePopupView.getController()).setRenamedPlaylist(cell.getItem());
    popupStage.show();
  }

  private void removePlaylistAction(ActionEvent event, ListCell<PlaylistData> cell) {
    PlaylistData deletedPlaylist = cell.getItem();
    if (audioPlayerFacade.deletePlaylist(deletedPlaylist.getUid())) {
      localPlaylistBrowserContainer.getItems().remove(deletedPlaylist);
    }
  }

	private void fillPlaylistBrowserContainer(List<PlaylistData> playlists, PlaylistData displayedPlaylist) {
		if (CollectionUtils.isNotEmpty(playlists)) {
			localPlaylistBrowserContainer.getItems().clear();
			boolean activeSelected = false;
			for (PlaylistData playlist : playlists) {
				localPlaylistBrowserContainer.getItems().add(playlist);
				if (!activeSelected && displayedPlaylist.equals(playlist)) {
					activeSelected = true;
					localPlaylistBrowserContainer.getSelectionModel().select(playlist);
				}
			}
		}
	}

	private void setPlaylistContainerColumns(TableView<AudioTrackPlaylistItem> playlistContainer,
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
		TableColumn<AudioTrackPlaylistItem, String> numberCol = new TableColumn<>(columnConfiguration.getTitle());
		numberCol.setUserData(columnConfiguration.getName());
		numberCol.setPrefWidth(columnConfiguration.getWidth());
		numberCol.setCellValueFactory(new PropertyValueFactory<>(columnConfiguration.getName()));

		columnConfiguration = columnsConfigurations.get(PlaylistContainerViewConfigurations.COLUMN_ARTIST_NAME);
		TableColumn<AudioTrackPlaylistItem, String> artistCol = new TableColumn<>(columnConfiguration.getTitle());
		artistCol.setUserData(columnConfiguration.getName());
		artistCol.setPrefWidth(columnConfiguration.getWidth());
		artistCol.setCellValueFactory(new PropertyValueFactory<>(columnConfiguration.getName()));

		columnConfiguration = columnsConfigurations.get(PlaylistContainerViewConfigurations.COLUMN_ALBUM_NAME);
		TableColumn<AudioTrackPlaylistItem, String> albumCol = new TableColumn<>(columnConfiguration.getTitle());
		albumCol.setUserData(columnConfiguration.getName());
		albumCol.setPrefWidth(columnConfiguration.getWidth());
		albumCol.setCellValueFactory(new PropertyValueFactory<>(columnConfiguration.getName()));

		columnConfiguration = columnsConfigurations.get(PlaylistContainerViewConfigurations.COLUMN_TITLE_NAME);
		TableColumn<AudioTrackPlaylistItem, String> titleCol = new TableColumn<>(columnConfiguration.getTitle());
		titleCol.setUserData(columnConfiguration.getName());
		titleCol.setPrefWidth(columnConfiguration.getWidth());
		titleCol.setCellValueFactory(new PropertyValueFactory<>(columnConfiguration.getName()));

		columnConfiguration = columnsConfigurations.get(PlaylistContainerViewConfigurations.COLUMN_LENGTH_NAME);
		TableColumn<AudioTrackPlaylistItem, String> lengthCol = new TableColumn<>(columnConfiguration.getTitle());
		lengthCol.setUserData(columnConfiguration.getName());
		lengthCol.setPrefWidth(columnConfiguration.getWidth());
		lengthCol.setCellValueFactory(new PropertyValueFactory<>(columnConfiguration.getName()));

		playlistContainer.getColumns().addAll(numberCol, artistCol, albumCol, titleCol, lengthCol);
	}

  private void setPlaylistContainerRowFactory() {
    localPlaylistContainer.setRowFactory(lv -> {
      TableRow<AudioTrackPlaylistItem> tableRow = new TableRow<>();

      // prepare context menu
      ContextMenu contextMenu = new ContextMenu();
      MenuItem lookupOnLastfmMenuItem = new MenuItem("Lookup on last.fm");

      MenuItem removeMenuItem = new MenuItem("Delete");
      removeMenuItem.setOnAction(event ->
          audioPlayerFacade.deleteItemsFromPlaylist(localPlaylistContainer.getSelectionModel().getSelectedIndices())
      );

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

  private void setPlaylistContainerItems(PlaylistData playlistData) {
    localPlaylistContainer.getItems().clear();
    localPlaylistContainer.getItems().addAll(
        playlistData.getTracks().stream()
            .map(mediaInfoData -> {
              if ((mediaInfoData != null) && (mediaInfoData.getTitle() != null)) {
                return new AudioTrackPlaylistItem(mediaInfoData.getTrackNumber(),
                    mediaInfoData.getArtist(), mediaInfoData.getAlbum(), mediaInfoData.getTitle(),
                    trackTimeLabelBuilder.buildTimeString(mediaInfoData.getLength()));
              } else {
                String trackPath = (mediaInfoData != null) ? mediaInfoData.getTrackPath() : "NULL";
                LOG.warn("Media info not loaded for track '" + trackPath + "'");
                return new AudioTrackPlaylistItem("", "", "", trackPath, trackTimeLabelBuilder.buildTimeString(0));
              }
            }).collect(Collectors.toList())
    );
  }

  private void savePlaylistContainerViewConfiguration() {
    List<PlaylistContainerViewConfigurations.PlaylistContainerColumn> columns = localPlaylistContainer.getColumns().stream()
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


  private final class AudioPlayerEventAdapter extends DefaultAudioPlayerEventAdapter {

    @Override
    public void changedPlaylist(PlaylistData playlist) {
      setPlaylistContainerItems(playlist);
    }

    @Override
    public void createdNewPlaylist(PlaylistData newPlaylist) {
      localPlaylistBrowserContainer.getItems().add(newPlaylist);
      localPlaylistBrowserContainer.getSelectionModel().select(newPlaylist);
      setPlaylistContainerItems(newPlaylist);
    }

    @Override
    public void changedTrackPosition(PlaylistData playlist, int trackPosition) {
      if (playlist.equals(localPlaylistBrowserContainer.getSelectionModel().getSelectedItem())) {
//        playlistContainer.getSelectionModel().select(trackPosition);
        LOG.debug("Track position changed!");
      }
    }

    @Override
    public void refreshTrackMediaInfo(int trackPosition, AudioTrackData mediaInfo) {
      if ((trackPosition > 0) && (trackPosition < localPlaylistContainer.getItems().size())) {
        AudioTrackPlaylistItem playlistItem = (AudioTrackPlaylistItem) localPlaylistContainer.getItems().get(trackPosition);
        playlistItem.setNumber(mediaInfo.getTrackNumber());
        playlistItem.setArtist(mediaInfo.getArtist());
        playlistItem.setAlbum(mediaInfo.getAlbum());
        playlistItem.setTitle(mediaInfo.getTitle());
        playlistItem.setLength(trackTimeLabelBuilder.buildTimeString(mediaInfo.getLength()));
      } else {
        LOG.error("Invalid track position [trackPosition = " + trackPosition + "], refresh track media info skipped");
      }
    }

    @Override
    public void renamedPlaylist(PlaylistData playlistData) {
      localPlaylistBrowserContainer.refresh();
    }

  }
}
