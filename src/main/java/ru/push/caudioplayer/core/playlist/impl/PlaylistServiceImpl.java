package ru.push.caudioplayer.core.playlist.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import ru.push.caudioplayer.core.config.ApplicationConfigService;
import ru.push.caudioplayer.core.config.dto.PlaylistItemData;
import ru.push.caudioplayer.core.deezer.DeezerApiErrorException;
import ru.push.caudioplayer.core.deezer.DeezerApiService;
import ru.push.caudioplayer.core.deezer.model.Track;
import ru.push.caudioplayer.core.medialoader.MediaInfoDataLoaderService;
import ru.push.caudioplayer.core.playlist.PlaylistService;
import ru.push.caudioplayer.core.playlist.dao.PlaylistItemRepository;
import ru.push.caudioplayer.core.playlist.model.MediaSourceType;
import ru.push.caudioplayer.core.playlist.dao.PlaylistRepository;
import ru.push.caudioplayer.core.playlist.dao.entity.PlaylistEntity;
import ru.push.caudioplayer.core.playlist.model.Playlist;
import ru.push.caudioplayer.core.playlist.model.PlaylistTrack;
import ru.push.caudioplayer.core.playlist.model.PlaylistType;
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
	private PlaylistRepository localPlaylistRepository;
	@Autowired
	private PlaylistItemRepository localPlaylistItemRepository;
	@Autowired
	private MediaInfoDataLoaderService mediaInfoDataLoaderService;
	@Autowired
	private PlaylistMapper playlistMapper;

	private Map<String, Playlist> playlistMap = new HashMap<>();
	private Playlist deezerFavoritesPlaylist;
	private Playlist activePlaylist;
	private PlaylistTrack activePlaylistTrack;

	@PostConstruct
	public void init() {
		LOG.debug("init bean {}", this.getClass().getName());
		reloadPlaylists();
	}

	private List<Playlist> loadLocalPlaylists() {

		List<PlaylistItemData> playlistItemsData = applicationConfigService.getLocalPlaylistItemsData();
		List<String> playlistsUid = playlistItemsData.stream()
				.map(PlaylistItemData::getPlaylistUid)
				.collect(Collectors.toList());
		List<PlaylistEntity> playlistsEntity = localPlaylistRepository.findAllById(playlistsUid);
		List<Playlist> playlists = playlistMapper.mapPlaylist(playlistsEntity, playlistItemsData);
		return playlists;
	}

	private List<Playlist> loadDeezerPlaylists() {
		try {
			List<ru.push.caudioplayer.core.deezer.model.Playlist> playlistsDeezer = deezerApiService.getPlaylists();
			Optional<Long> favoritesPlaylistId = playlistsDeezer.stream()
					.filter(p -> p.getIs_loved_track())
					.map(p -> p.getId()).findFirst();
			List<Playlist> playlists = playlistMapper.mapPlaylistDeezer(playlistsDeezer);

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

		setActivePlaylist(applicationConfigService.getActivePlaylistUid());
	}

	@Override
	public List<Playlist> getPlaylists() {
		return playlistMap.values().stream().collect(Collectors.toList());
	}

	@Override
	public Optional<Playlist> getActivePlaylist() {
		return Optional.ofNullable(activePlaylist);
	}

	@Override
	public Optional<Playlist> getPlaylist(String playlistUid) {
		return Optional.ofNullable(playlistMap.get(playlistUid));
	}

	@Override
	public Optional<Playlist> getPlaylist(PlaylistType type, String title) {
		return playlistMap.values().stream()
				.filter(p -> p.getType().equals(type))
				.filter(p -> p.getTitle().equals(title))
				.findFirst();
	}

	private Optional<Playlist> setActivePlaylist(String playlistUid) {
		if (!playlistMap.containsKey(playlistUid)) {
			LOG.error("Playlist not found: uid = {}", playlistUid);
			return Optional.empty();
		}

		Playlist playlist = playlistMap.get(playlistUid);
		activePlaylist = playlist;
		return Optional.of(playlist);
	}

	@Override
	public Optional<PlaylistTrack> setActivePlaylistTrack(String playlistUid, String trackUid) {
		if (!playlistMap.containsKey(playlistUid)) {
			LOG.error("Playlist not found: uid = {}", playlistUid);
			return Optional.empty();
		}

		Playlist playlist = playlistMap.get(playlistUid);
		Optional<PlaylistTrack> playlistTrack = playlist.getItems().stream()
				.filter(o -> o.getUid().equals(trackUid))
				.findFirst();
		if (playlistTrack.isPresent()) {
			activePlaylist = playlist;
			setActivePlaylistTrackNowPlaying(playlistTrack.get());
		} else {
			LOG.error("Playlist track not found: playlistUid = {}, trackUid = {}", playlistUid, trackUid);
		}

		return playlistTrack;
	}

	@Override
	public Optional<PlaylistTrack> getActivePlaylistTrack() {
		return Optional.ofNullable(activePlaylistTrack);
	}

	@Override
	public Optional<PlaylistTrack> nextActivePlaylistTrack() {
		if ((activePlaylist == null) || (activePlaylistTrack == null)) {
			return Optional.empty();
		}

		int activePlaylistTrackIndex = activePlaylist.getItems().indexOf(activePlaylistTrack);
		if (activePlaylistTrackIndex < (activePlaylist.getItems().size() - 1)) {
			activePlaylistTrackIndex++;
		} else {
			activePlaylistTrackIndex = 0;
		}

		setActivePlaylistTrackNowPlaying(activePlaylist.getItems().get(activePlaylistTrackIndex));
		return Optional.of(activePlaylistTrack);
	}

	@Override
	public Optional<PlaylistTrack> prevActivePlaylistTrack() {
		if ((activePlaylist == null) || (activePlaylistTrack == null)) {
			return Optional.empty();
		}

		int activePlaylistTrackIndex = activePlaylist.getItems().indexOf(activePlaylistTrack);
		if (activePlaylistTrackIndex > 0) {
			activePlaylistTrackIndex--;
		} else {
			activePlaylistTrackIndex = activePlaylist.getItems().size() - 1;
		}

		setActivePlaylistTrackNowPlaying(activePlaylist.getItems().get(activePlaylistTrackIndex));
		return Optional.of(activePlaylistTrack);
	}

	@Override
	public void resetActivePlaylistTrack() {
		if (activePlaylistTrack != null) {
			activePlaylistTrack.setNowPlaying(false);
			activePlaylistTrack = null;
		}
	}

	private void setActivePlaylistTrackNowPlaying(PlaylistTrack playlistTrack) {
		if (activePlaylistTrack != null) {
			activePlaylistTrack.setNowPlaying(false);
		}
		playlistTrack.setNowPlaying(true);
		activePlaylistTrack = playlistTrack;
	}

	@Override
	public Playlist createPlaylist(PlaylistType type) {
		String newPlaylistTitle = DEFAULT_PLAYLIST_TITLE + " " + DateTimeUtils.getCurrentTimestamp();
		return createPlaylist(type, newPlaylistTitle);
	}

	@Override
	public Playlist createPlaylist(PlaylistType type, String playlistTitle) {
		String uid;
		Playlist newPlaylist = null;

		switch (type) {
			case LOCAL:
				uid = UUID.randomUUID().toString();
				newPlaylist = new Playlist(uid, playlistTitle, PlaylistType.LOCAL, null);
				PlaylistEntity newPlaylistEntity = playlistMapper.inverseMapPlaylist(newPlaylist);
				localPlaylistRepository.save(newPlaylistEntity);
				playlistMap.put(uid, newPlaylist);
				applicationConfigService.appendPlaylist(newPlaylist.getUid(), newPlaylist.getTitle());
				break;

			case DEEZER:
				try {
					Long newPlaylistId = deezerApiService.createPlaylist(playlistTitle);
					uid = String.valueOf(newPlaylistId);
					newPlaylist = new Playlist(uid, playlistTitle, PlaylistType.DEEZER, null);
					playlistMap.put(uid, newPlaylist);
				} catch (DeezerApiErrorException e) {
					LOG.error("Deezer api error", e);
				}
				break;
		}

		return newPlaylist;

	}

	@Override
	public Playlist deletePlaylist(String playlistUid) {
		if (!playlistMap.containsKey(playlistUid)) {
			LOG.error("Playlist not found: uid = {}", playlistUid);
			return null;
		}

		Playlist playlist = playlistMap.get(playlistUid);
		if (playlist.isReadOnly()) {
			LOG.warn("Read only playlist cannot be deleted: uid = {}", playlistUid);
			return null;
		}

		boolean deleteResult = false;
		switch (playlist.getType()) {

			case LOCAL:
				localPlaylistRepository.deleteById(playlistUid);
				applicationConfigService.removePlaylist(playlistUid);
				deleteResult = true;
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
			return playlist;
		} else {
			LOG.warn("Delete playlist fails: {}", playlistUid);
			return null;
		}
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
				PlaylistEntity playlistEntity = playlistMapper.inverseMapPlaylist(playlist);
				playlistEntity.setTitle(newTitle);
				localPlaylistRepository.save(playlistEntity);
				applicationConfigService.renamePlaylist(playlistEntity.getUid(), playlistEntity.getTitle());
				renameResult = true;
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
		PlaylistEntity playlistEntity = playlistMapper.inverseMapPlaylist(playlist);
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
		List<PlaylistTrack> newItems = mediaInfoDataLoaderService.load(playlist, mediaPaths, MediaSourceType.FILE);
		localPlaylistItemRepository.saveAll(
				playlistMapper.inverseMapPlaylistItem(newItems, playlistMapper.inverseMapPlaylist(playlist))
		);

		playlist.getItems().addAll(newItems);

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

		List<PlaylistTrack> newItems = mediaInfoDataLoaderService.load(playlist, mediaPaths, MediaSourceType.HTTP_STREAM);
		if (CollectionUtils.isEmpty(newItems)) {
			LOG.error("No correct stream locations have been set");
			return null;
		}

		localPlaylistItemRepository.saveAll(
				playlistMapper.inverseMapPlaylistItem(newItems, playlistMapper.inverseMapPlaylist(playlist))
		);
		playlist.getItems().addAll(newItems);
		LOG.info("New items added to playlist: {}", playlist);
		return playlist;
	}

	@Override
	public Playlist addTrackToDeezerPlaylist(String playlistUid, TrackData trackData) {
		return addTrackToDeezerPlaylist(playlistUid, trackData, false);
	}

	@Override
	public Playlist addTrackToDeezerFavoritesPlaylist(TrackData trackData) {
		return deezerFavoritesPlaylist != null ?
				addTrackToDeezerPlaylist(deezerFavoritesPlaylist.getUid(), trackData, true) :
				null;
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
					PlaylistTrack newItem = playlistMapper.mapPlaylistItemDeezer(playlist, searchedTrack);
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
	public Playlist deleteItemsFromPlaylist(String playlistUid, List<String> tracksUid) {
		if (!playlistMap.containsKey(playlistUid)) {
			LOG.error("Playlist not found: uid = {}", playlistUid);
			return null;
		}

		Playlist playlist = playlistMap.get(playlistUid);
		if (playlist.isReadOnly()) {
			LOG.warn("Cannot delete items from read only playlist: uid = {}", playlistUid);
			return null;
		}

		List<PlaylistTrack> deletedTracks = tracksUid.stream()
				.map(uid -> {
					Optional<PlaylistTrack> track = playlist.getItems().stream()
							.filter(o -> Objects.equals(uid, o.getUid())).findFirst();
					if (track.isPresent()) {
						return track.get();
					} else {
						LOG.error("Deleted track not found in playlist: playlistUid = {}, trackUid = {}", playlistUid, uid);
						return null;
					}
				})
				.filter(Objects::nonNull)
				.collect(Collectors.toList());

		if (deletedTracks.isEmpty()) {
			return null;
		}

		boolean deleteResult = false;
		switch (playlist.getType()) {

			case LOCAL:
				PlaylistEntity playlistEntity = playlistMapper.inverseMapPlaylist(playlist);
				localPlaylistItemRepository.deleteAll(playlistEntity.getItems());
				playlistEntity.getItems().removeIf(o ->
						deletedTracks.stream().anyMatch(t -> Objects.equals(o.getUid(), t.getUid())));
				deleteResult = true;
				break;

			case DEEZER:
				Long playlistId = Long.valueOf(playlist.getUid());
				List<Long> removedTrackIds = deletedTracks.stream()
						.map(o -> Long.valueOf(o.getUid()))
						.collect(Collectors.toList());
				try {
					deleteResult = deezerApiService.removeTracksFromPlaylist(playlistId, removedTrackIds);
				} catch (DeezerApiErrorException e) {
					LOG.error("Remove track from Deezer error: playlist id = {}, track ids = {}, error = {}",
							playlist.getUid(), removedTrackIds, e);
				}
				break;
		}

		if (deleteResult) {
			playlist.getItems().removeAll(deletedTracks);
			LOG.info("Items deleted from playlist: playlistUid = {}, deleted tracks = {}", playlist.getUid(), deletedTracks);
			return playlist;
		} else {
			return null;
		}
	}


	private String formShortSearchQuery(TrackData trackData) {
		return trackData.getArtist() + " " + trackData.getTitle();
	}

	private String formExtendedSearchQuery(TrackData trackData) {
		return trackData.getArtist() + " " + trackData.getTitle() + " " + trackData.getAlbum();
	}
}
