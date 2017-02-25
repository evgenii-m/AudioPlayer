package ru.push.caudioplayer.controller;

import javafx.collections.FXCollections;
import javafx.event.EventHandler;
import javafx.event.EventType;
import javafx.fxml.FXML;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import javafx.scene.input.MouseEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.push.caudioplayer.core.mediaplayer.components.CustomAudioPlayerComponent;
import ru.push.caudioplayer.core.mediaplayer.components.CustomPlaylistComponent;
import ru.push.caudioplayer.core.mediaplayer.dto.PlaylistData;
import ru.push.caudioplayer.core.mediaplayer.services.AppConfigurationService;
import ru.push.caudioplayer.ui.MediaTrackPlaylistItem;
import ru.push.caudioplayer.utils.TrackTimeLabelBuilder;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.nio.file.Paths;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/12/17
 */
public class PlaylistController {

  private static final Logger LOG = LoggerFactory.getLogger(MainController.class);

  @FXML
  private TableView playlistContainer;

  @Autowired
  private TrackTimeLabelBuilder trackTimeLabelBuilder;
  @Autowired
  private CustomAudioPlayerComponent playerComponent;
  @Autowired
  private CustomPlaylistComponent playlistComponent;
  @Autowired
  private AppConfigurationService appConfigurationService;


  @FXML
  public void initialize() {
    LOG.debug("initialize");

  }

  @PostConstruct
  public void init() {
    LOG.debug("init");

    playlistContainer.setEditable(true);
    List<PlaylistData> playlists = appConfigurationService.getPlaylists();
    playlists.stream()
        .filter(PlaylistData::isActive)
        .findFirst()
        .ifPresent(this::loadPlaylist);

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
    playlistContainer.setEditable(false);

    playlistContainer.setOnMouseClicked(mouseEvent -> {
      if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && (mouseEvent.getClickCount() == 2)) {
        int playlistPosition = playlistContainer.getFocusModel().getFocusedCell().getRow();
        String trackPath = playlistComponent.getTrackPath(playlistPosition);
        playerComponent.playMedia(Paths.get(trackPath).toUri().toString());
      }
    });
  }

  private void loadPlaylist(PlaylistData playlistData) {
    PlaylistData playlistFullData = playlistComponent.loadPlaylist(playlistData);
    playlistContainer.getItems().clear();
    playlistContainer.getItems().addAll(
        playlistFullData.getTracks().stream()
            .map(mediaInfoData -> {
              if ((mediaInfoData != null) && (mediaInfoData.getTitle() != null)) {
                return new MediaTrackPlaylistItem(mediaInfoData.getTrackNumber(),
                    mediaInfoData.getArtist(), mediaInfoData.getAlbum(), mediaInfoData.getTitle(),
                    trackTimeLabelBuilder.buildTimeString(mediaInfoData.getLength()));
              } else {
                String trackPath = (mediaInfoData != null) ? mediaInfoData.getTrackPath() : "NULL";
                LOG.info("Media info not loaded for track '" + trackPath + "'");
                return new MediaTrackPlaylistItem("", "", "", trackPath, "");
              }
            }).collect(Collectors.toList())
    );
  }
}
