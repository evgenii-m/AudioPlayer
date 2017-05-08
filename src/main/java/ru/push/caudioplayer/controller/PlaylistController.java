package ru.push.caudioplayer.controller;

import javafx.beans.binding.Bindings;
import javafx.beans.value.ChangeListener;
import javafx.beans.value.ObservableValue;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.push.caudioplayer.core.facades.AudioPlayerFacade;
import ru.push.caudioplayer.core.mediaplayer.DefaultAudioPlayerEventAdapter;
import ru.push.caudioplayer.core.mediaplayer.pojo.MediaInfoData;
import ru.push.caudioplayer.core.mediaplayer.pojo.PlaylistData;
import ru.push.caudioplayer.ui.MediaTrackPlaylistItem;
import ru.push.caudioplayer.utils.TrackTimeLabelBuilder;

import javax.annotation.PostConstruct;
import java.util.Collections;
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
  private ListView playlistBrowserContainer;
  @FXML
  private TableView playlistContainer;

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
        String activePlaylistName = playlistBrowserContainer.getSelectionModel().getSelectedItem().toString();
        int trackPosition = playlistContainer.getFocusModel().getFocusedCell().getRow();
        audioPlayerFacade.playTrack(activePlaylistName, trackPosition);
      }
    });

    List<PlaylistData> playlists = audioPlayerFacade.getPlaylists();
    PlaylistData activePlaylist = audioPlayerFacade.getActivePlaylist();
    fillPlaylistBrowserContainer(playlists, activePlaylist);

    playlistBrowserContainer.getSelectionModel().selectedItemProperty().addListener(
        new ChangeListener<String>() {
          @Override
          public void changed(ObservableValue observable, String oldValue, String newValue) {
            PlaylistData playlist = audioPlayerFacade.showPlaylist(newValue);
            setPlaylistContainerItems(playlist);
          }
        });

    setPlaylistContainerItems(audioPlayerFacade.showActivePlaylist());
    preparePlaylistContextMenu();
    preparePlaylistBrowserContextMenu();
  }

  private void preparePlaylistBrowserContextMenu() {
    playlistBrowserContainer.setCellFactory(lv -> {
      ListCell<String> cell = new ListCell<>();

      ContextMenu contextMenu = new ContextMenu();

      MenuItem removeMenuItem = new MenuItem();
      removeMenuItem.textProperty().bind(Bindings.format("Delete"));
      removeMenuItem.setOnAction(event -> {
        String deletedPlaylistName = (String) playlistBrowserContainer.getSelectionModel().getSelectedItem();
        if (audioPlayerFacade.deletePlaylist(deletedPlaylistName)) {
          playlistBrowserContainer.getItems().remove(deletedPlaylistName);
        }
      });

      MenuItem renameMenuItem = new MenuItem("Rename");
//    removeMenuItem.setOnAction(event ->
//        audioPlayerFacade.renamePlaylist());

      contextMenu.getItems().addAll(removeMenuItem, renameMenuItem);

      cell.textProperty().bind(cell.itemProperty());
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

  private void preparePlaylistContextMenu() {
    playlistContainer.setRowFactory(lv -> {
      TableRow<MediaTrackPlaylistItem> tableRow = new TableRow<>();

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

  private void fillPlaylistBrowserContainer(List<PlaylistData> playlists, PlaylistData activePlaylist) {
    if (CollectionUtils.isNotEmpty(playlists)) {
      playlistBrowserContainer.getItems().clear();
      boolean activeSelected = false;
      for (PlaylistData playlist : playlists) {
        playlistBrowserContainer.getItems().add(playlist.getName());
        if (!activeSelected && activePlaylist.equals(playlist)) {
          activeSelected = true;
          playlistBrowserContainer.getSelectionModel().select(playlist.getName());
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
      playlistBrowserContainer.getItems().add(newPlaylist.getName());
      playlistBrowserContainer.getSelectionModel().select(newPlaylist.getName());
      setPlaylistContainerItems(newPlaylist);
    }

    @Override
    public void changedTrackPosition(String playlistName, int trackPosition) {
      if (playlistName.equals(playlistBrowserContainer.getSelectionModel().getSelectedItem())) {
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
