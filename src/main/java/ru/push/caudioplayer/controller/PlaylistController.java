package ru.push.caudioplayer.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.AnchorPane;
import javafx.stage.FileChooser;
import javafx.stage.Stage;
import org.apache.commons.collections4.CollectionUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.push.caudioplayer.core.facades.MusicLibraryLogicFacade;
import ru.push.caudioplayer.core.facades.PlaylistLogicFacade;
import ru.push.caudioplayer.core.facades.dto.PlaylistData;
import ru.push.caudioplayer.core.facades.dto.TrackData;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;
import java.io.File;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Optional;
import java.util.function.Consumer;
import java.util.stream.Collectors;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 2/12/17
 */
@SuppressWarnings("unchecked")
public class PlaylistController extends PlaylistComponentBaseController {

  private static final Logger LOG = LoggerFactory.getLogger(PlaylistController.class);

	@FXML
	private AnchorPane playlistPanelContainer;
	@FXML
	private ListView<PlaylistData> playlistBrowserContainer;
	@FXML
	private TableView<TrackData> playlistContentContainer;

	@Autowired
	private MusicLibraryLogicFacade musicLibraryLogicFacade;


	@FXML
  public void initialize() {
		LOG.debug("initialize FXML for {}", this.getClass().getName());

    playlistBrowserContainer.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		playlistContentContainer.setEditable(false);
		playlistContentContainer.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
  }

	@PostConstruct
	public void init() {
		LOG.debug("init bean {}", this.getClass().getName());

		PlaylistAudioPlayerEventAdapter eventAdapter = new PlaylistAudioPlayerEventAdapter();
		audioPlayerFacade.addEventListener(eventAdapter);
		musicLibraryLogicFacade.addEventListener(eventAdapter);

		super.init();

		// bind playlist container mouse click event to play track action
		getPlaylistContentContainer().setOnMouseClicked(mouseEvent -> {
			if (mouseEvent.getButton().equals(MouseButton.PRIMARY) && (mouseEvent.getClickCount() == 2)) {
				if (displayedPlaylist != null) {
					TrackData trackData = getPlaylistContentContainer().getFocusModel().getFocusedItem();
					audioPlayerFacade.playTrack(displayedPlaylist.getUid(), trackData.getTrackUid());
				}
			}
		});
	}

	@PreDestroy
	public void stop() {
		// save active and displayed playlists UID
		// TODO: think about delete this fragment
		PlaylistData activePlaylist = musicLibraryLogicFacade.getActivePlaylist();
		if (activePlaylist != null) {
			applicationConfigService.saveActivePlaylist(activePlaylist.getUid());
		}
		if (displayedPlaylist != null) {
			applicationConfigService.saveDisplayedPlaylist(displayedPlaylist.getUid());
		}

		// save view configuration
		savePlaylistContainerViewConfiguration();
	}

	@Override
	protected PlaylistLogicFacade getPlaylistLogicFacade() {
		return musicLibraryLogicFacade;
	}

	@Override
	protected AnchorPane getPlaylistPanelContainer() {
		return playlistPanelContainer;
	}

	@Override
	protected ListView<PlaylistData> getPlaylistBrowserContainer() {
		return playlistBrowserContainer;
	}

	@Override
	protected TableView<TrackData> getPlaylistContentContainer() {
		return playlistContentContainer;
	}

  @Override
	protected void setPlaylistBrowserContainerCellFactory(ListView<PlaylistData> playlistBrowserContainer) {
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
			contextMenu.getItems().addAll(getCommonPlaylistBrowserContainerMenuItems(cell));

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


	@Override
	protected void setPlaylistContentContainerRowFactory(TableView<TrackData> playlistContentContainer) {
		playlistContentContainer.setRowFactory(lv -> {
			TableRow<TrackData> tableRow = new TableRow<>();

			// prepare context menu
			ContextMenu contextMenu = new ContextMenu();
			contextMenu.getItems().addAll(getCommonPlaylistContentContainerMenuItems(playlistContentContainer));

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
	public void addFilesToPlaylist(ActionEvent actionEvent) {
  	if ((displayedPlaylist != null) && (displayedPlaylist.isLocal())) {
			FileChooser fileChooser = new FileChooser();
			fileChooser.setTitle("Open file(s)");
			// WARNING: if this code throws JVM crashing, need to add JVM option '-DVLCJ_INITX=no'
			List<File> files = fileChooser.showOpenMultipleDialog(playlistPanelContainer.getScene().getWindow());
			if (CollectionUtils.isNotEmpty(files)) {
				musicLibraryLogicFacade.addFilesToPlaylist(displayedPlaylist.getUid(), files);
			}
		}
	}

	@FXML
	public void addStreamToPlaylist(ActionEvent actionEvent) {
		if ((displayedPlaylist != null) && (displayedPlaylist.isLocal())) {
			Stage popupStage = createPopup("Add HTTP stream(s) source", new Scene(textInputActionPopupView.getView()));
			Consumer<String> action = inputText -> {
				String[] inputLines = inputText.split("\n");
				musicLibraryLogicFacade.addLocationsToPlaylist(displayedPlaylist.getUid(), Arrays.asList(inputLines));
			};
			((TextInputActionPopupController) textInputActionPopupView.getController()).setAction(action);
			popupStage.show();
		}
	}

	private final class PlaylistAudioPlayerEventAdapter extends BaseAudioPlayerEventAdapter {

		@Override
		public void changedPlaylist(PlaylistData playlistData) {
			updateContainerItemPlaylistData(playlistData);
			if ((displayedPlaylist != null) && (displayedPlaylist.equals(playlistData))) {
				setPlaylistContentContainerItems(playlistData);
			}
		}

		@Override
		public void createdNewPlaylist(PlaylistData playlistData) {
			playlistBrowserContainer.getItems().add(playlistData);
			playlistBrowserContainer.getSelectionModel().select(playlistData);
			setPlaylistContentContainerItems(playlistData);
		}

		@Override
		public void changedTrackData(PlaylistData playlistData, TrackData trackData) {
			playlistBrowserContainer.getItems().stream()
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
			updateContainerItemPlaylistData(playlistData);
		}

		@Override
		public void deletedPlaylist(PlaylistData playlistData) {
			playlistBrowserContainer.getItems().stream()
					.filter(p -> p.getUid().equals(playlistData.getUid())).findFirst()
					.ifPresent(p -> playlistBrowserContainer.getItems().remove(p));
			playlistBrowserContainer.refresh();
		}

		@Override
		public void changedNowPlayingTrack(TrackData trackData) {
			playlistBrowserContainer.getItems().stream()
					.map(PlaylistData::getTracks)
					.flatMap(Collection::stream)
					.forEach(o -> o.setNowPlaying(o.equals(trackData) && trackData.isNowPlaying()));
			if ((displayedPlaylist != null) && (displayedPlaylist.getUid().equals(trackData.getPlaylistUid()))) {
				playlistContentContainer.refresh();
			}
		}
	}

}
