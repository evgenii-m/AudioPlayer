package ru.push.caudioplayer.core.playlist.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import ru.push.caudioplayer.core.config.ApplicationConfigService;
import ru.push.caudioplayer.core.deezer.DeezerApiErrorException;
import ru.push.caudioplayer.core.deezer.DeezerApiService;
import ru.push.caudioplayer.core.deezer.domain.Track;
import ru.push.caudioplayer.core.medialoader.MediaInfoDataLoaderService;
import ru.push.caudioplayer.core.mediaplayer.domain.MediaSourceType;
import ru.push.caudioplayer.core.playlist.PlaylistService;
import ru.push.caudioplayer.core.playlist.dao.LocalPlaylistRepository;
import ru.push.caudioplayer.core.playlist.dao.model.PlaylistEntity;
import ru.push.caudioplayer.core.playlist.domain.Playlist;
import ru.push.caudioplayer.core.playlist.domain.PlaylistItem;
import ru.push.caudioplayer.core.playlist.domain.PlaylistType;
import ru.push.caudioplayer.core.playlist.dto.TrackData;
import ru.push.caudioplayer.utils.DateTimeUtils;
import ru.push.caudioplayer.utils.XmlUtils;

import javax.annotation.PostConstruct;
import javax.xml.bind.JAXBException;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class PlaylistServiceImpl implements PlaylistService {

	private static final Logger LOG = LoggerFactory.getLogger(PlaylistServiceImpl.class);

	private static final String DEFAULT_PLAYLIST_TITLE = "New playlist";

	@Autowired
	private DeezerApiService deezerApiService;
	@Autowired
	private ApplicationConfigService applicationConfigService;
	@Autowired
	private LocalPlaylistRepository localPlaylistRepository;
	@Autowired
	private MediaInfoDataLoaderService mediaInfoDataLoaderService;

	private PlaylistConverter playlistConverter = new PlaylistConverter();

	private Map<String, Playlist> playlistMap = new HashMap<>();
	private Playlist deezerFavoritesPlaylist;
	private Playlist activePlaylist;
	private Integer activePlaylistTrackIndex;

	@PostConstruct
	public void init() {
		LOG.debug("init bean {}", this.getClass().getName());
		reloadPlaylists();
	}

	private List<Playlist> loadLocalPlaylists() {
		boolean localPlaylistRepositoryInitResult = localPlaylistRepository.refresh();
		if (!localPlaylistRepositoryInitResult) {
			LOG.error("Local playlist repository not initialized correctly");
			return new ArrayList<>();
		}

		List<String> localPlaylistsUid = applicationConfigService.getLocalPlaylistsUid();
		List<PlaylistEntity> playlistsEntity = localPlaylistRepository.findPlaylists(localPlaylistsUid);
		List<Playlist> playlists = playlistConverter.mapPlaylist(playlistsEntity);
		return playlists;
	}

	private List<Playlist> loadDeezerPlaylists() {
		try {
			List<ru.push.caudioplayer.core.deezer.domain.Playlist> playlistsDeezer = deezerApiService.getPlaylists();
			Optional<Long> favoritesPlaylistId = playlistsDeezer.stream()
					.filter(p -> p.getIs_loved_track())
					.map(p -> p.getId()).findFirst();
			List<Playlist> playlists = playlistConverter.mapPlaylistDeezer(playlistsDeezer);

			favoritesPlaylistId.ifPresent(
					id -> playlists.stream()
							.filter(p -> p.getUid().equals(String.valueOf(id)))
							.findFirst().ifPresent(
									p -> deezerFavoritesPlaylist = p
							)
			);
			return playlists;
		} catch (DeezerApiErrorException e) {
			LOG.error("Deezer playlist repository not initialized correctly");
			return new ArrayList<>();
		}
	}

	@Override
	public void reloadPlaylists() {
		List<Playlist> localPlaylists = loadLocalPlaylists();
//		List<Playlist> deezerPlalists = loadDeezerPlaylists();
		List<Playlist> deezerPlalists = new ArrayList<>();
		playlistMap = Stream.of(localPlaylists, deezerPlalists)
				.flatMap(Collection::stream)
				.collect(Collectors.toMap(Playlist::getUid, o -> o));

		setActivePlaylistTrack(applicationConfigService.getActivePlaylistUid(), 0);
	}

	@Override
	public List<Playlist> getLocalPlaylists() {
		return playlistMap.values().stream()
				.filter(Playlist::isLocal)
				.collect(Collectors.toList());
	}

	@Override
	public List<Playlist> getDeezerPlaylists() {
		return playlistMap.values().stream()
				.filter(Playlist::isDeezer)
				.collect(Collectors.toList());
	}

	@Override
	public List<Playlist> getAllPlaylists() {
		return (List<Playlist>) playlistMap.values();
	}

	@Override
	public Playlist getActivePlaylist() {
		return activePlaylist;
	}

	@Override
	public PlaylistItem setActivePlaylistTrack(String playlistUid, int trackIndex) {
		if (!playlistMap.containsKey(playlistUid)) {
			LOG.error("Playlist not found: uid = {}", playlistUid);
			return null;
		}

		Playlist playlist = playlistMap.get(playlistUid);
		int playlistSize = playlist.getItems().size();
		if ((trackIndex < 0) || (trackIndex >= playlistSize)) {
			LOG.error("Invalid track index for playlist: playlist = {}, trackIndex = {}", playlist, trackIndex);
			return null;
		}

		activePlaylist = playlist;
		activePlaylistTrackIndex = trackIndex;

		return activePlaylist.getItems().get(activePlaylistTrackIndex);
	}

	@Override
	public PlaylistItem getActivePlaylistTrack() {
		if ((activePlaylist == null) || (activePlaylistTrackIndex == null)) {
			return null;
		}
		return activePlaylist.getItems().get(activePlaylistTrackIndex);
	}

	@Override
	public PlaylistItem nextActivePlaylistTrack() {
		if ((activePlaylist == null) || (activePlaylistTrackIndex == null)) {
			return null;
		}

		if (activePlaylistTrackIndex < (activePlaylist.getItems().size() - 1)) {
			activePlaylistTrackIndex++;
		} else {
			activePlaylistTrackIndex = 0;
		}

		return activePlaylist.getItems().get(activePlaylistTrackIndex);
	}

	@Override
	public PlaylistItem prevActivePlaylistTrack() {
		if ((activePlaylist == null) || (activePlaylistTrackIndex == null)) {
			return null;
		}

		if (activePlaylistTrackIndex > 0) {
			activePlaylistTrackIndex--;
		} else {
			activePlaylistTrackIndex = activePlaylist.getItems().size() - 1;
		}

		return activePlaylist.getItems().get(activePlaylistTrackIndex);
	}

	@Override
	public Playlist createPlaylist(PlaylistType type) {
		String newPlaylistTitle = DEFAULT_PLAYLIST_TITLE + " " + DateTimeUtils.getCurrentTimestamp();
		String uid;
		Playlist newPlaylist = null;

		switch (type) {
			case LOCAL:
				uid = UUID.randomUUID().toString();
				newPlaylist = new Playlist(uid, newPlaylistTitle, PlaylistType.LOCAL, null);
				PlaylistEntity newPlaylistEntity = playlistConverter.inverseMapPlaylist(newPlaylist);
				boolean saveResult = localPlaylistRepository.savePlaylist(newPlaylistEntity);
				if (saveResult) {
					playlistMap.put(uid, newPlaylist);
					applicationConfigService.appendPlaylist(uid);
				} else {
					newPlaylist = null;
				}
				break;

			case DEEZER:
				try {
					Long newPlaylistId = deezerApiService.createPlaylist(newPlaylistTitle);
					uid = String.valueOf(newPlaylistId);
					newPlaylist = new Playlist(uid, newPlaylistTitle, PlaylistType.DEEZER, null);
					playlistMap.put(uid, newPlaylist);
				} catch (DeezerApiErrorException e) {
					LOG.error("Deezer api error", e);
				}
				break;
		}

		return newPlaylist;
	}

	@Override
	public boolean deletePlaylist(String playlistUid) {
		if (!playlistMap.containsKey(playlistUid)) {
			LOG.error("Playlist not found: uid = {}", playlistUid);
			return false;
		}

		Playlist playlist = playlistMap.get(playlistUid);
		if (playlist.isReadOnly()) {
			LOG.warn("Read only playlist cannot be deleted: uid = {}", playlistUid);
			return false;
		}

		boolean deleteResult = false;
		switch (playlist.getType()) {

			case LOCAL:
				deleteResult = localPlaylistRepository.deletePlaylist(playlistUid);
				if (deleteResult) {
					applicationConfigService.removePlaylist(playlistUid);
				}
				break;

			case DEEZER:
				Long playlistId = Long.valueOf(playlistUid);
				try {
					deleteResult = deezerApiService.deletePlaylist(playlistId);
				} catch (DeezerApiErrorException e) {
					LOG.error("Deezer api error", e);
				}
				break;
		}

		if (deleteResult) {
			playlistMap.remove(playlistUid);
			LOG.info("Deleted playlist: {}", playlist);
		}
		return deleteResult;
	}

	@Override
	public Playlist renamePlaylist(String playlistUid, String newTitle) {
		if (!playlistMap.containsKey(playlistUid)) {
			LOG.error("Playlist not found: uid = {}", playlistUid);
			return null;
		}

		Playlist playlist = playlistMap.get(playlistUid);
		if (playlist.isReadOnly()) {
			LOG.warn("Read only playlist cannot be deleted: uid = {}", playlistUid);
			return null;
		}

		boolean renameResult = false;
		switch (playlist.getType()) {

			case LOCAL:
				PlaylistEntity playlistEntity = playlistConverter.inverseMapPlaylist(playlist);
				playlistEntity.setTitle(newTitle);
				renameResult = localPlaylistRepository.savePlaylist(playlistEntity);
				break;

			case DEEZER:
				Long playlistId = Long.valueOf(playlistUid);
				try {
					renameResult = deezerApiService.renamePlaylist(playlistId, newTitle);
				} catch (DeezerApiErrorException e) {
					LOG.error("Deezer api error", e);
				}
				break;
		}

		if (renameResult) {
			playlist.setTitle(newTitle);
			LOG.info("Renamed playlist: {}", playlist);
			return playlist;
		} else {
			return null;
		}
	}

	@Override
	public boolean exportPlaylistToFile(String playlistUid, String folderPath) {
		Playlist playlist = playlistMap.get(playlistUid);
		PlaylistEntity playlistEntity = playlistConverter.inverseMapPlaylist(playlist);
		File file = new File(folderPath + playlist.getExportFileName());
		try {
			XmlUtils.marshalDocument(playlistEntity, file, PlaylistEntity.class.getPackage().getName());
			LOG.info("Export playlist to file: uid = {}, filePath = {}", playlistUid, file.getAbsolutePath());
			return true;
		} catch (JAXBException e) {
			LOG.error("Export playlist error", e);
			return false;
		}
	}

	@Override
	public Playlist addFilesToLocalPlaylist(String playlistUid, List<File> files) {
		if (!playlistMap.containsKey(playlistUid)) {
			LOG.error("Playlist not found: uid = {}", playlistUid);
			return null;
		}

		Playlist playlist = playlistMap.get(playlistUid);
		if (!playlist.isLocal()) {
			LOG.error("Only Local playlist supports file items: playlist = {}", playlist);
			return null;
		}
		if (playlist.isReadOnly()) {
			LOG.warn("Cannot add items to read only playlist: uid = {}", playlistUid);
			return null;
		}

		List<String> mediaPaths = files.stream()
				.map(File::getAbsolutePath)
				.collect(Collectors.toList());
		List<PlaylistItem> newItems = mediaInfoDataLoaderService.load(playlist, mediaPaths, MediaSourceType.FILE);

		playlist.getItems().addAll(newItems);
		localPlaylistRepository.savePlaylist(playlistConverter.inverseMapPlaylist(playlist));

		LOG.info("New items added to playlist: playlist = {}", playlist);
		return playlist;
	}

	@Override
	public Playlist addLocationsToLocalPlaylist(String playlistUid, List<String> locations) {
		if (!playlistMap.containsKey(playlistUid)) {
			LOG.error("Playlist not found: uid = {}", playlistUid);
			return null;
		}

		Playlist playlist = playlistMap.get(playlistUid);
		if (!playlist.isLocal()) {
			LOG.error("Only Local playlist supports location items: playlist = {}", playlist);
			return null;
		}
		if (playlist.isReadOnly()) {
			LOG.warn("Cannot add items to read only playlist: uid = {}", playlistUid);
			return null;
		}

		List<String> mediaPaths = locations.stream()
				.map(location -> {
					URL locationUrl = null;
					try {
						locationUrl = new URL(location);
					} catch (MalformedURLException e) {
						LOG.info("Bad URL [" + location + "].", e);
					}
					return (locationUrl != null) ? locationUrl.toString() : null;
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toList());
		List<PlaylistItem> newItems = mediaInfoDataLoaderService.load(playlist, mediaPaths, MediaSourceType.HTTP_STREAM);

		playlist.getItems().addAll(newItems);
		localPlaylistRepository.savePlaylist(playlistConverter.inverseMapPlaylist(playlist));

		LOG.info("New items added to playlist: {}", playlist);
		return playlist;
	}

	@Override
	public Playlist addTrackToDeezerPlaylist(String playlistUid, TrackData trackData) {
		return addTrackToDeezerPlaylist(playlistUid, trackData, false);
	}

	@Override
	public Playlist addTrackToDeezerFavoritesPlaylist(TrackData trackData) {
		return addTrackToDeezerPlaylist(deezerFavoritesPlaylist.getUid(), trackData, true);
	}

	private Playlist addTrackToDeezerPlaylist(String playlistUid, TrackData trackData, boolean force) {
		if (!playlistMap.containsKey(playlistUid)) {
			LOG.error("Playlist not found: uid = {}", playlistUid);
			return null;
		}

		Playlist playlist = playlistMap.get(playlistUid);
		if (!force) {
			if (!playlist.isDeezer()) {
				LOG.error("Only Local playlist supports trackData items: playlist = {}", playlist);
				return null;
			}
			if (playlist.isReadOnly()) {
				LOG.warn("Cannot add items to read only playlist: uid = {}", playlistUid);
				return null;
			}
		}

		try {
			List<Track> searchedTracksResult = deezerApiService.searchTracksQuery(
					formShortSearchQuery(trackData), formExtendedSearchQuery(trackData));
			LOG.info("Searched {} tracks: {}", searchedTracksResult.size(), searchedTracksResult);

			if (!CollectionUtils.isEmpty(searchedTracksResult)) {
				Track searchedTrack = searchedTracksResult.get(0);
				Long playlistId = Long.valueOf(playlist.getUid());
				Long trackId = searchedTrack.getId();
				boolean result = deezerApiService.addTrackToPlaylist(playlistId, trackId);
				if (result) {
					PlaylistItem newItem = playlistConverter.mapPlaylistItemDeezer(searchedTrack);
					playlist.getItems().add(newItem);
					LOG.info("New items added to playlist: {}", playlist);
				} else {
					LOG.error("New item could not be added to Deezer playlist: playlist = {}", playlist);
					playlist = null;
				}
			} else {
				LOG.error("Track not found on Deezer: trackData = {}", trackData);
				playlist = null;
			}
		} catch (DeezerApiErrorException e) {
			LOG.error("Add track to Deezer error: playlist = {}, trackData = {}, error = {}", playlist, trackData, e);
			playlist = null;
		}

		return playlist;
	}

	@Override
	public Playlist deleteItemsFromPlaylist(String playlistUid, List<Integer> itemsIndexes) {
		if (!playlistMap.containsKey(playlistUid)) {
			LOG.error("Playlist not found: uid = {}", playlistUid);
			return null;
		}

		Playlist playlist = playlistMap.get(playlistUid);
		if (playlist.isReadOnly()) {
			LOG.warn("Cannot delete items from read only playlist: uid = {}", playlistUid);
			return null;
		}

		switch (playlist.getType()) {

			case LOCAL:
				itemsIndexes.stream()
						.filter(itemIndex -> (itemIndex >= 0) && (itemIndex < playlist.getItems().size()))
						.forEach(itemIndex -> playlist.getItems().remove(itemIndex.intValue()));
				localPlaylistRepository.savePlaylist(playlistConverter.inverseMapPlaylist(playlist));
				break;

			case DEEZER:
				Long playlistId = Long.valueOf(playlist.getUid());
				List<Long> removedTrackIds = itemsIndexes.stream()
						.filter(idx -> idx < playlist.getItems().size())
						.map(idx -> playlist.getItems().get(idx).getTrackId())
						.filter(Objects::nonNull)
						.map(Long::valueOf)
						.collect(Collectors.toList());
				try {
					deezerApiService.removeTracksFromPlaylist(playlistId, removedTrackIds);
				} catch (DeezerApiErrorException e) {
					LOG.error("Remove track from Deezer error: playlist id = {}, track ids = {}, error = {}",
							playlist.getUid(), removedTrackIds, e);
					return null;
				}
				break;
		}

		LOG.info("Items deleted from playlist: {}", playlist);
		return playlist;
	}


	private String formShortSearchQuery(TrackData trackData) {
		return trackData.getArtist() + " " + trackData.getTitle();
	}

	private String formExtendedSearchQuery(TrackData trackData) {
		return trackData.getArtist() + " " + trackData.getTitle() + " " + trackData.getAlbum();
	}
}
