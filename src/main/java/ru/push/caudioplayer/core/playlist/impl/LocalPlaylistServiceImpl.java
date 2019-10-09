package ru.push.caudioplayer.core.playlist.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import ru.push.caudioplayer.core.config.ApplicationConfigService;
import ru.push.caudioplayer.core.config.dto.PlaylistItemData;
import ru.push.caudioplayer.core.deezer.DeezerApiService;
import ru.push.caudioplayer.core.medialoader.MediaInfoDataLoaderService;
import ru.push.caudioplayer.core.playlist.LocalPlaylistService;
import ru.push.caudioplayer.core.playlist.dao.PlaylistItemRepository;
import ru.push.caudioplayer.core.playlist.dao.entity.PlaylistItemEntity;
import ru.push.caudioplayer.core.playlist.model.MediaSourceType;
import ru.push.caudioplayer.core.playlist.dao.PlaylistRepository;
import ru.push.caudioplayer.core.playlist.dao.entity.PlaylistEntity;
import ru.push.caudioplayer.core.playlist.model.Playlist;
import ru.push.caudioplayer.core.playlist.model.PlaylistTrack;
import ru.push.caudioplayer.core.playlist.model.PlaylistType;
import ru.push.caudioplayer.utils.DateTimeUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;
import java.util.Objects;
import java.util.Optional;
import java.util.UUID;
import java.util.stream.Collectors;

@Component
public class LocalPlaylistServiceImpl implements LocalPlaylistService {

	private static final Logger LOG = LoggerFactory.getLogger(LocalPlaylistServiceImpl.class);

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
	@Resource(name = "localPlaylistMapper")
	private PlaylistMapper playlistMapper;

	private String activePlaylistUid;
	private String activePlaylistTrackUid;

	@PostConstruct
	public void init() {
		LOG.debug("init bean {}", this.getClass().getName());

		activePlaylistUid = applicationConfigService.getActivePlaylistUid();
	}

	@Override
	public List<Playlist> getPlaylists() {
		List<PlaylistItemData> playlistItemsData = applicationConfigService.getLocalPlaylistItemsData();
		List<String> playlistsUid = playlistItemsData.stream()
				.map(PlaylistItemData::getPlaylistUid)
				.collect(Collectors.toList());
		List<PlaylistEntity> playlistsEntity = localPlaylistRepository.findAllById(playlistsUid);
		List<Playlist> playlists = playlistMapper.mapPlaylist(playlistsEntity, playlistItemsData);
		return playlists;
	}

	@Override
	public Optional<Playlist> getActivePlaylist() {
		return getPlaylist(activePlaylistUid);
	}

	@Override
	public Optional<Playlist> getPlaylist(String playlistUid) {
		return localPlaylistRepository.findById(playlistUid).map(p -> {
			Playlist playlist = playlistMapper.mapPlaylist(p);
			setNowPlayingTrack(playlist);
			return playlist;
		});
	}

	private void setNowPlayingTrack(Playlist playlist) {
		if ((activePlaylistTrackUid != null) && playlist.getUid().equals(activePlaylistUid)) {
			playlist.getItems().stream()
					.filter(o -> o.getUid().equals(activePlaylistTrackUid)).findFirst()
					.ifPresent(o -> o.setNowPlaying(true));
		}
	}

	@Override
	public List<Playlist> getPlaylistByTitle(String title) {
		return playlistMapper.mapPlaylist(
				localPlaylistRepository.findByTitle(title)
		);
	}

	@Override
	public Optional<PlaylistTrack> setActivePlaylistTrack(String playlistUid, String trackUid) {
		Optional<Playlist> playlistOptional = getPlaylist(playlistUid);
		if (!playlistOptional.isPresent()) {
			LOG.error("Playlist not found: uid = {}", playlistUid);
			return Optional.empty();
		}
		activePlaylistUid = playlistUid;

		Optional<PlaylistTrack> playlistItemOptional = playlistOptional.get()
				.getItems().stream()
				.filter(o -> o.getUid().equals(trackUid))
				.findFirst();
		if (!playlistItemOptional.isPresent()) {
			LOG.error("Playlist track not found: playlistUid = {}, trackUid = {}", playlistUid, trackUid);
			return Optional.empty();
		}

		activePlaylistTrackUid = trackUid;
		playlistItemOptional.get().setNowPlaying(true);
		return playlistItemOptional;
	}

	@Override
	public Optional<PlaylistTrack> getActivePlaylistTrack() {
		if ((activePlaylistUid == null) || (activePlaylistTrackUid == null)) {
			return Optional.empty();
		}

		Playlist playlist = localPlaylistRepository.findById(activePlaylistUid)
				.map(p -> playlistMapper.mapPlaylist(p))
				.orElse(null);

		return localPlaylistItemRepository.findById(activePlaylistTrackUid).map(p -> {
			PlaylistTrack t = playlistMapper.mapPlaylistItem(playlist, p);
			t.setNowPlaying(true);
			return t;
		});
	}

	@Override
	public void resetActivePlaylistTrack() {
		activePlaylistTrackUid = null;
	}

	@Override
	public Optional<PlaylistTrack> nextActivePlaylistTrack() {
		if ((activePlaylistUid == null) || (activePlaylistTrackUid == null)) {
			return Optional.empty();
		}

		Playlist activePlaylist = getActivePlaylist().orElse(null);
		if (activePlaylist == null) {
			LOG.error("Active playlist not found: uid = {}", activePlaylistUid);
			return Optional.empty();
		}

		PlaylistTrack activePlaylistTrack = activePlaylist.getItems().stream()
				.filter(o -> o.getUid().equals(activePlaylistTrackUid))
				.findFirst().orElse(null);
		if (activePlaylistTrack == null) {
			LOG.error("Active playlist track not found: playlistUid = {}, trackUid = {}", activePlaylistUid, activePlaylistTrackUid);
			return Optional.empty();
		}

		int activePlaylistTrackIndex = activePlaylist.getItems().indexOf(activePlaylistTrack);
		if (activePlaylistTrackIndex < (activePlaylist.getItems().size() - 1)) {
			activePlaylistTrackIndex++;
		} else {
			activePlaylistTrackIndex = 0;
		}

		PlaylistTrack newActivePlaylistTrack = activePlaylist.getItems().get(activePlaylistTrackIndex);
		activePlaylistTrackUid = newActivePlaylistTrack.getUid();
		newActivePlaylistTrack.setNowPlaying(true);
		return Optional.of(newActivePlaylistTrack);
	}

	@Override
	public Optional<PlaylistTrack> prevActivePlaylistTrack() {
		if ((activePlaylistUid == null) || (activePlaylistTrackUid == null)) {
			return Optional.empty();
		}

		Playlist activePlaylist = getActivePlaylist().orElse(null);
		if (activePlaylist == null) {
			LOG.error("Active playlist not found: uid = {}", activePlaylistUid);
			return Optional.empty();
		}

		PlaylistTrack activePlaylistTrack = activePlaylist.getItems().stream()
				.filter(o -> o.getUid().equals(activePlaylistTrackUid))
				.findFirst().orElse(null);
		if (activePlaylistTrack == null) {
			LOG.error("Active playlist track not found: playlistUid = {}, trackUid = {}", activePlaylistUid, activePlaylistTrackUid);
			return Optional.empty();
		}

		int activePlaylistTrackIndex = activePlaylist.getItems().indexOf(activePlaylistTrack);
		if (activePlaylistTrackIndex > 0) {
			activePlaylistTrackIndex--;
		} else {
			activePlaylistTrackIndex = activePlaylist.getItems().size() - 1;
		}

		PlaylistTrack newActivePlaylistTrack = activePlaylist.getItems().get(activePlaylistTrackIndex);
		activePlaylistTrackUid = newActivePlaylistTrack.getUid();
		newActivePlaylistTrack.setNowPlaying(true);
		return Optional.of(newActivePlaylistTrack);
	}

	@Override
	public Playlist createPlaylist() {
		String newPlaylistTitle = DEFAULT_PLAYLIST_TITLE + " " + DateTimeUtils.getCurrentTimestamp();
		return createPlaylist(newPlaylistTitle);
	}

	@Override
	public Playlist createPlaylist(String title) {
		String uid = UUID.randomUUID().toString();
		Playlist newPlaylist = new Playlist(uid, title, PlaylistType.LOCAL, null);
		PlaylistEntity newPlaylistEntity = playlistMapper.inverseMapPlaylist(newPlaylist);
		localPlaylistRepository.save(newPlaylistEntity);
		applicationConfigService.appendPlaylist(newPlaylist.getUid(), newPlaylist.getTitle());
		return newPlaylist;
	}

	@Override
	public Playlist deletePlaylist(String playlistUid) {
		Playlist playlist = getPlaylist(playlistUid).orElse(null);
		if (playlist == null) {
			LOG.error("Playlist not found: uid = {}", playlistUid);
			return null;
		}
		if (playlist.isReadOnly()) {
			LOG.warn("Read only playlist cannot be deleted: uid = {}", playlistUid);
			return null;
		}

		localPlaylistRepository.deleteById(playlistUid);
		applicationConfigService.removePlaylist(playlistUid);
		LOG.info("Deleted playlist: {}", playlist);
		return playlist;
	}

	@Override
	public Playlist renamePlaylist(String playlistUid, String newTitle) {
		Playlist playlist = getPlaylist(playlistUid).orElse(null);
		if (playlist == null) {
			LOG.error("Playlist not found: uid = {}", playlistUid);
			return null;
		}
		if (playlist.isReadOnly()) {
			LOG.warn("Read only playlist cannot be deleted: uid = {}", playlistUid);
			return null;
		}

		playlist.setTitle(newTitle);
		PlaylistEntity playlistEntity = playlistMapper.inverseMapPlaylist(playlist);
		localPlaylistRepository.save(playlistEntity);
		applicationConfigService.renamePlaylist(playlistEntity.getUid(), playlistEntity.getTitle());
		return playlist;
	}

	@Override
	public boolean exportPlaylistToFile(String playlistUid, String folderPath) {
//		Playlist playlist = getPlaylist(playlistUid).orElse(null);
//		if (playlist == null) {
//			LOG.error("Playlist not found: uid = {}", playlistUid);
//			return false;
//		}
//
//		PlaylistEntity playlistEntity = playlistMapper.inverseMapPlaylist(playlist);
//		File file = new File(folderPath + playlist.getExportFileName());
//		try {
//			XmlUtils.marshalDocument(playlistEntity, file, PlaylistEntity.class.getPackage().getName());
//			LOG.info("Export playlist to file: uid = {}, filePath = {}", playlistUid, file.getAbsolutePath());
//			return true;
//		} catch (JAXBException e) {
//			LOG.error("Export playlist error", e);
//			return false;
//		}
		// TODO: actualize realisation
		return false;
	}

	@Override
	public Playlist addFilesToLocalPlaylist(String playlistUid, List<File> files) {
		Playlist playlist = getPlaylist(playlistUid).orElse(null);
		if (playlist == null) {
			LOG.error("Playlist not found: uid = {}", playlistUid);
			return null;
		}
		if (playlist.isReadOnly()) {
			LOG.warn("Read only playlist cannot be deleted: uid = {}", playlistUid);
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
		Playlist playlist = getPlaylist(playlistUid).orElse(null);
		if (playlist == null) {
			LOG.error("Playlist not found: uid = {}", playlistUid);
			return null;
		}
		if (playlist.isReadOnly()) {
			LOG.warn("Read only playlist cannot be deleted: uid = {}", playlistUid);
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
	public Playlist deleteItemsFromPlaylist(String playlistUid, List<String> tracksUid) {
		Playlist playlist = getPlaylist(playlistUid).orElse(null);
		if (playlist == null) {
			LOG.error("Playlist not found: uid = {}", playlistUid);
			return null;
		}
		if (playlist.isReadOnly()) {
			LOG.warn("Read only playlist cannot be deleted: uid = {}", playlistUid);
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

		PlaylistEntity playlistEntity = playlistMapper.inverseMapPlaylist(playlist);
		List<PlaylistItemEntity> playlistItemEntities = playlistEntity.getItems().stream()
				.filter(o -> deletedTracks.stream().anyMatch(t -> Objects.equals(o.getUid(), t.getUid())))
				.collect(Collectors.toList());
		localPlaylistItemRepository.deleteAll(playlistItemEntities);

		playlist.getItems().removeAll(deletedTracks);
		LOG.info("Items deleted from playlist: playlistUid = {}, deleted tracks = {}", playlist.getUid(), deletedTracks);
		return playlist;
	}
}
