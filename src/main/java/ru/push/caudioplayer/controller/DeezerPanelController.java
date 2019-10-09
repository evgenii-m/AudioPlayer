package ru.push.caudioplayer.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.ContextMenu;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.MenuItem;
import javafx.scene.control.SelectionMode;
import javafx.scene.control.TableRow;
import javafx.scene.control.TableView;
import javafx.scene.control.TextField;
import javafx.scene.layout.AnchorPane;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.push.caudioplayer.core.facades.DeezerLogicFacade;
import ru.push.caudioplayer.core.facades.MusicLibraryLogicFacade;
import ru.push.caudioplayer.core.facades.PlaylistLogicFacade;
import ru.push.caudioplayer.core.facades.dto.PlaylistData;
import ru.push.caudioplayer.core.facades.dto.TrackData;

import javax.annotation.PostConstruct;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 */
public class DeezerPanelController extends PlaylistComponentBaseController {

  private static final Logger LOG = LoggerFactory.getLogger(DeezerPanelController.class);

  @FXML
  public TextField searchQueryTextField;
	@FXML
  private AnchorPane playlistPanelContainer;
	@FXML
	private ListView<PlaylistData> playlistBrowserContainer;
	@FXML
	private TableView<TrackData> playlistContentContainer;

	@Autowired
	private DeezerLogicFacade deezerLogicFacade;


  @FXML
  public void initialize() {
		LOG.debug("initialize FXML for {}", this.getClass().getName());

		playlistBrowserContainer.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);
		playlistContentContainer.setEditable(false);
		playlistContentContainer.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
  }

	@Override
	protected PlaylistLogicFacade getPlaylistLogicFacade() {
		return deezerLogicFacade;
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

	@PostConstruct
  public void init() {
    LOG.debug("init bean {}", this.getClass().getName());

		PlaylistAudioPlayerEventAdapter eventAdapter = new PlaylistAudioPlayerEventAdapter();
		audioPlayerFacade.addEventListener(eventAdapter);
		deezerLogicFacade.addEventListener(eventAdapter);

		super.init();

		searchQueryTextField.textProperty().addListener((observable, oldValue, newValue) -> {
			filterPlaylistsBySearchQuery(newValue);
		});
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

			MenuItem openInWebBrowserMenuItem = new MenuItem("Open in web browser");
			openInWebBrowserMenuItem.setOnAction(event -> openPlaylistInWebBrowserAction(event, cell));

			contextMenu.getItems().addAll(openInWebBrowserMenuItem);

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

	protected void openPlaylistInWebBrowserAction(ActionEvent event, ListCell<PlaylistData> cell) {
		PlaylistData playlistData = cell.getItem();
		String webPageUrl = deezerLogicFacade.getDeezerPlaylistWebPageUrl(playlistData.getUid());
		if (StringUtils.isNotEmpty(webPageUrl)) {
			appMain.openWebPage(webPageUrl);
		}
	}

	@Override
	protected void setPlaylistContentContainerRowFactory(TableView<TrackData> playlistContentContainer) {
		playlistContentContainer.setRowFactory(lv -> {
			TableRow<TrackData> tableRow = new TableRow<>();

			// prepare context menu
			ContextMenu contextMenu = new ContextMenu();

			contextMenu.getItems().addAll(
					getCommonPlaylistContentContainerMenuItems(playlistContentContainer)
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
	public void clearSearchQueryTextField(ActionEvent actionEvent) {
		searchQueryTextField.clear();
	}

	private void filterPlaylistsBySearchQuery(String newValue) {
//		List<PlaylistData> deezerPlaylists = deezerLogicFacade.getPlaylists();
//		setPlaylistBrowserContainerItems(playlistBrowserContainer, deezerPlaylists);
//		setPlaylistContentContainerItems(displayedPlaylist);
	}

	private final class PlaylistAudioPlayerEventAdapter extends BaseAudioPlayerEventAdapter {
		@Override
		public void changedPlaylist(PlaylistData playlistData) {
			if (playlistData.isDeezer()) {
				updateContainerItemPlaylistData(playlistData);
				if ((displayedPlaylist != null) && (displayedPlaylist.equals(playlistData))) {
					setPlaylistContentContainerItems(playlistData);
				}
			}
		}

		@Override
		public void createdNewPlaylist(PlaylistData playlistData) {
			if (playlistData.isDeezer()) {
				playlistBrowserContainer.getItems().add(playlistData);
				playlistBrowserContainer.getSelectionModel().select(playlistData);
				setPlaylistContentContainerItems(playlistData);
			}
		}

		@Override
		public void changedTrackData(PlaylistData playlistData, TrackData trackData) {
			if (playlistData.isDeezer()) {
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
		}

		@Override
		public void renamedPlaylist(PlaylistData playlistData) {
			if (playlistData.isDeezer()) {
				updateContainerItemPlaylistData(playlistData);
			}
		}

		@Override
		public void deletedPlaylist(PlaylistData playlistData) {
			if (playlistData.isDeezer()) {
				playlistBrowserContainer.getItems().stream()
						.filter(p -> p.getUid().equals(playlistData.getUid())).findFirst()
						.ifPresent(p -> playlistBrowserContainer.getItems().remove(p));
				playlistBrowserContainer.refresh();
			}
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
