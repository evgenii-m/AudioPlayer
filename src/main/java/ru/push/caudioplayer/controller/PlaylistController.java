package ru.push.caudioplayer.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import javafx.stage.Stage;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.push.caudioplayer.ConfigurationControllers;
import ru.push.caudioplayer.core.facades.AudioPlayerFacade;
import ru.push.caudioplayer.core.mediaplayer.DefaultAudioPlayerEventAdapter;
import ru.push.caudioplayer.core.mediaplayer.pojo.MediaInfoData;
import ru.push.caudioplayer.core.mediaplayer.pojo.PlaylistData;
import ru.push.caudioplayer.ui.MediaTrackPlaylistItem;
import ru.push.caudioplayer.utils.TrackTimeLabelBuilder;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/12/17
 */
@SuppressWarnings("unchecked")
public class PlaylistController {

  private static final Logger LOG = LoggerFactory.getLogger(PlaylistController.class);

  @FXML
  private ListView<PlaylistData> playlistBrowserContainer;
  @FXML
  private TableView playlistContainer;

  @FXML
  @Resource(name = "renamePopupView")
  private ConfigurationControllers.View renamePopupView;

  @Autowired
  private AudioPlayerFacade audioPlayerFacade;
  @Autowired
  private TrackTimeLabelBuilder trackTimeLabelBuilder;


  @FXML
  public void initialize() {
    LOG.debug("initialize");

    setPlaylistContainerColumns();
    playlistBrowserContainer.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    playlistContainer.setEditable(false);
    playlistContainer.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
  }

  @PostConstruct
  public void init() {
    LOG.debug("init");

    audioPlayerFacade.addEventListener(new AudioPlayerEventAdapter());

    playlistContainer.setOnMouseClicked(mouseEvent -> {
      if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && (mouseEvent.getClickCount() == 2)) {
        PlaylistData displayedPlaylist = playlistBrowserContainer.getSelectionModel().getSelectedItem();
        int trackPosition = playlistContainer.getFocusModel().getFocusedCell().getRow();
        audioPlayerFacade.playTrack(displayedPlaylist.getUid(), trackPosition);
      }
    });

    List<PlaylistData> playlists = audioPlayerFacade.getPlaylists();
    PlaylistData displayedPlaylist = audioPlayerFacade.getDisplayedPlaylist();
    fillPlaylistBrowserContainer(playlists, displayedPlaylist);

    playlistBrowserContainer.addEventFilter(MouseEvent.MOUSE_PRESSED, event -> {
      if (!event.isPrimaryButtonDown()) {
        event.consume();
      }
    });

    playlistBrowserContainer.getSelectionModel().selectedItemProperty().addListener(
        (observable, oldValue, newValue) -> {
          PlaylistData playlist = audioPlayerFacade.showPlaylist(newValue.getUid());
          setPlaylistContainerItems(playlist);
        });

    setPlaylistContainerItems(audioPlayerFacade.getDisplayedPlaylist());
    setPlaylistContainerRowFactory();
    setPlaylistBrowserContainerCellFactory();
  }

  private void setPlaylistBrowserContainerCellFactory() {
    playlistBrowserContainer.setCellFactory(lv -> {
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
    Stage popupStage = new Stage();
    popupStage.setTitle("Rename");
    popupStage.setResizable(false);
    popupStage.setScene(new Scene(renamePopupView.getView()));
    popupStage.show();
  }

  private void removePlaylistAction(ActionEvent event, ListCell<PlaylistData> cell) {
    PlaylistData deletedPlaylist = cell.getItem();
    if (audioPlayerFacade.deletePlaylist(deletedPlaylist.getUid())) {
      playlistBrowserContainer.getItems().remove(deletedPlaylist);
    }
  }

  private void setPlaylistContainerRowFactory() {
    playlistContainer.setRowFactory(lv -> {
      TableRow<MediaTrackPlaylistItem> tableRow = new TableRow<>();

      // prepare context menu
      ContextMenu contextMenu = new ContextMenu();
      MenuItem lookupOnLastfmMenuItem = new MenuItem("Lookup on last.fm");

      MenuItem removeMenuItem = new MenuItem("Delete");
      removeMenuItem.setOnAction(event ->
          audioPlayerFacade.deleteItemsFromPlaylist(playlistContainer.getSelectionModel().getSelectedIndices())
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

  private void setPlaylistContainerColumns() {
    playlistContainer.getColumns().clear();
    TableColumn numberCol = new TableColumn("#");
    numberCol.setCellValueFactory(new PropertyValueFactory<MediaTrackPlaylistItem, String>("number"));
    TableColumn artistCol = new TableColumn("Artist");
    artistCol.setCellValueFactory(new PropertyValueFactory<MediaTrackPlaylistItem, String>("artist"));
    TableColumn albumCol = new TableColumn("Album");
    albumCol.setCellValueFactory(new PropertyValueFactory<MediaTrackPlaylistItem, String>("album"));
    TableColumn titleCol = new TableColumn("Title");
    titleCol.setCellValueFactory(new PropertyValueFactory<MediaTrackPlaylistItem, String>("title"));
    TableColumn lengthCol = new TableColumn("Length");
    lengthCol.setCellValueFactory(new PropertyValueFactory<MediaTrackPlaylistItem, String>("length"));
    playlistContainer.getColumns().addAll(numberCol, artistCol, albumCol, titleCol, lengthCol);
  }

  private void fillPlaylistBrowserContainer(List<PlaylistData> playlists, PlaylistData displayedPlaylist) {
    if (CollectionUtils.isNotEmpty(playlists)) {
      playlistBrowserContainer.getItems().clear();
      boolean activeSelected = false;
      for (PlaylistData playlist : playlists) {
        playlistBrowserContainer.getItems().add(playlist);
        if (!activeSelected && displayedPlaylist.equals(playlist)) {
          activeSelected = true;
          playlistBrowserContainer.getSelectionModel().select(playlist);
        }
      }
    }
  }

  private void setPlaylistContainerItems(PlaylistData playlistData) {
    playlistContainer.getItems().clear();
    playlistContainer.getItems().addAll(
        playlistData.getTracks().stream()
            .map(mediaInfoData -> {
              if ((mediaInfoData != null) && (mediaInfoData.getTitle() != null)) {
                return new MediaTrackPlaylistItem(mediaInfoData.getTrackNumber(),
                    mediaInfoData.getArtist(), mediaInfoData.getAlbum(), mediaInfoData.getTitle(),
                    trackTimeLabelBuilder.buildTimeString(mediaInfoData.getLength()));
              } else {
                String trackPath = (mediaInfoData != null) ? mediaInfoData.getTrackPath() : "NULL";
                LOG.info("Media info not loaded for track '" + trackPath + "'");
                return new MediaTrackPlaylistItem("", "", "", trackPath, trackTimeLabelBuilder.buildTimeString(0));
              }
            }).collect(Collectors.toList())
    );
  }


  private final class AudioPlayerEventAdapter extends DefaultAudioPlayerEventAdapter {

    @Override
    public void changedPlaylist(PlaylistData playlist) {
      setPlaylistContainerItems(playlist);
    }

    @Override
    public void createdNewPlaylist(PlaylistData newPlaylist) {
      playlistBrowserContainer.getItems().add(newPlaylist);
      playlistBrowserContainer.getSelectionModel().select(newPlaylist);
      setPlaylistContainerItems(newPlaylist);
    }

    @Override
    public void changedTrackPosition(PlaylistData playlist, int trackPosition) {
      if (playlist.equals(playlistBrowserContainer.getSelectionModel().getSelectedItem())) {
//        playlistContainer.getSelectionModel().select(trackPosition);
        LOG.debug("Track position changed!");
      }
    }

    @Override
    public void refreshTrackMediaInfo(int trackPosition, MediaInfoData mediaInfo) {
      if ((trackPosition > 0) && (trackPosition < playlistContainer.getItems().size())) {
        MediaTrackPlaylistItem playlistItem = (MediaTrackPlaylistItem) playlistContainer.getItems().get(trackPosition);
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
    public void stopAudioPlayer() {
    }
  }
}
