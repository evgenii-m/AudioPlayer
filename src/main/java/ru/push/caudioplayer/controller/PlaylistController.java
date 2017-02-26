package ru.push.caudioplayer.controller;

import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.scene.input.MouseButton;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.collections4.IterableUtils;
import org.apache.commons.collections4.ListUtils;
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
import java.nio.file.Paths;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/12/17
 */
public class PlaylistController {

  private static final Logger LOG = LoggerFactory.getLogger(MainController.class);

  @FXML
  private ListView playlistBrowserContainer;
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

    List<PlaylistData> playlists = appConfigurationService.getPlaylists();
    playlistBrowserContainer.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
    fillPlaylistBrowserContainer(playlists);
    playlistContainer.setEditable(true);
    playlists.stream()
        .filter(PlaylistData::isActive).findFirst()
        .ifPresent(this::setPlaylistContainerItems);
    setPlaylistContainerColumns();
    playlistContainer.setEditable(false);

    playlistContainer.setOnMouseClicked(mouseEvent -> {
      if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && (mouseEvent.getClickCount() == 2)) {
        int playlistPosition = playlistContainer.getFocusModel().getFocusedCell().getRow();
        String trackPath = playlistComponent.getTrackPath(playlistPosition);
        playerComponent.playMedia(Paths.get(trackPath).toUri().toString());
      }
    });
  }

  private void fillPlaylistBrowserContainer(List<PlaylistData> playlists) {
    if (CollectionUtils.isNotEmpty(playlists)) {
      playlistBrowserContainer.getItems().clear();
      boolean activeSelected = false;
      Collections.sort(playlists, (p1, p2) -> Integer.compare(p1.getPosition(), p2.getPosition()));
      for (PlaylistData playlist : playlists) {
        playlistBrowserContainer.getItems().add(playlist.getName());
        if (!activeSelected && playlist.isActive()) {
          activeSelected = true;
          playlistBrowserContainer.getSelectionModel().select(playlist.getName());
        }
      }
    }
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

  private void setPlaylistContainerItems(PlaylistData playlistData) {
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
