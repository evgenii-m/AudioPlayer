package ru.push.caudioplayer.controller;

import javafx.fxml.FXML;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.control.ScrollPane;
import javafx.scene.control.SelectionMode;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.push.caudioplayer.core.facades.AudioPlayerFacade;
import ru.push.caudioplayer.core.mediaplayer.pojo.LastFmTrackData;

import javax.annotation.PostConstruct;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class LastFmPanelController {

	private static final Logger LOG = LoggerFactory.getLogger(LastFmPanelController.class);

	@FXML
	public ListView<LastFmTrackData> recentTracksContainer;
	@FXML
	public ScrollPane trackInfoContainer;


	@Autowired
	private AudioPlayerFacade audioPlayerFacade;

	private final ScheduledExecutorService updateRecentTracksScheduler = Executors.newSingleThreadScheduledExecutor();
	private List<LastFmTrackData> currentRecentTracks;


	@FXML
	public void initialize() {
		LOG.debug("initialize FXML for {}", this.getClass().getName());

		recentTracksContainer.getSelectionModel().setSelectionMode(SelectionMode.SINGLE);

	}

	@PostConstruct
	public void init() {
		LOG.debug("init bean {}", this.getClass().getName());

		setRecentTracksContainerCellFactory();

//		updateRecentTracksScheduler.scheduleAtFixedRate(new UpdateUiRunnable(), 0L, 1L, TimeUnit.SECONDS);
		updateRecentTracksContainer();
	}

	private void setRecentTracksContainerCellFactory() {
		recentTracksContainer.setCellFactory(lv -> {
			ListCell<LastFmTrackData> cell = new ListCell<LastFmTrackData>() {
				@Override
				protected void updateItem(LastFmTrackData item, boolean empty) {
					super.updateItem(item, empty);
					if (empty || (item == null) || !item.isValid()) {
						setText(null);
					} else {
						String itemText = String.format("%s  -  %s  -  %s", item.getArtist(), item.getTitle(), item.getScrobbleDate());
						setText(itemText);
					}
				}
			};
			return cell;
		});
	}


	private final class UpdateUiRunnable implements Runnable {

		private UpdateUiRunnable() {
		}

		@Override
		public void run() {
			updateRecentTracksContainer();
		}
	}

	private void updateRecentTracksContainer() {
		List<LastFmTrackData> recentTracks = audioPlayerFacade.getRecentTracksFromLastFm();
		// update only when the recent tracks list is changed
		if (!recentTracks.equals(currentRecentTracks)) {
			recentTracksContainer.getItems().clear();
			recentTracksContainer.getItems().addAll(recentTracks);
			currentRecentTracks = recentTracks;
		}
	}


}
