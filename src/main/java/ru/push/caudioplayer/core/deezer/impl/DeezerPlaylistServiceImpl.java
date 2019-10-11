package ru.push.caudioplayer.core.deezer.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import ru.push.caudioplayer.core.deezer.DeezerApiErrorException;
import ru.push.caudioplayer.core.deezer.DeezerApiService;
import ru.push.caudioplayer.core.deezer.DeezerPlaylistService;
import ru.push.caudioplayer.core.deezer.model.Track;
import ru.push.caudioplayer.core.playlist.dto.TrackData;
import ru.push.caudioplayer.core.playlist.model.Playlist;
import ru.push.caudioplayer.core.playlist.model.PlaylistTrack;
import ru.push.caudioplayer.utils.DateTimeUtils;

import javax.annotation.Resource;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Optional;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class DeezerPlaylistServiceImpl implements DeezerPlaylistService {

	private static final Logger LOG = LoggerFactory.getLogger(DeezerPlaylistServiceImpl.class);

	private static final String DEFAULT_PLAYLIST_TITLE = "New playlist";

	@Autowired
	private DeezerApiService deezerApiService;
	@Resource(name = "deezerPlaylistMapper")
	private PlaylistMapper playlistMapper;

	private final Map<String, String> playlistsTitleCache;	// <playlist uid, playlist title>
	private String deezerFavoritesPlaylistUid;


	public DeezerPlaylistServiceImpl() {
		playlistsTitleCache = new HashMap<>();
	}

	@Override
	public Optional<Playlist> getPlaylist(String playlistUid) {
		try {
			return Optional.ofNullable(deezerApiService.getPlaylist(Long.valueOf(playlistUid)))
					.map(p -> {
						Playlist playlist = playlistMapper.mapPlaylist(p);
						updatePlaylistsTitleCache(playlist);
						return playlist;
					});
		} catch (DeezerApiErrorException e) {
			LOG.error("Deezer playlist repository not initialized correctly");
			return Optional.empty();
		}
	}

	@Override
	public List<Playlist> getPlaylistByTitle(String title) {
		List<String> playlistsUid = playlistsTitleCache.entrySet().stream()
				.filter(e -> e.getValue().equals(title))
				.map(Map.Entry::getKey)
				.collect(Collectors.toList());

		if (!CollectionUtils.isEmpty(playlistsUid)) {
			return playlistsUid.stream()
					.map(this::getPlaylist)
					.filter(Optional::isPresent)
					.map(Optional::get)
					.collect(Collectors.toList());

		} else {
			LOG.debug("Title not found in playlists cache, all user playlists will need to be loaded: title = {}", title);
			return getPlaylists().stream()
					.filter(p -> p.getTitle().equals(title))
					.collect(Collectors.toList());
		}
	}

	@Override
	public List<Playlist> getPlaylists() {
		try {
			List<ru.push.caudioplayer.core.deezer.model.Playlist> playlistsDeezer = deezerApiService.getPlaylists();
			playlistsDeezer.stream()
					.filter(p -> p.getIs_loved_track())
					.map(p -> String.valueOf(p.getId()))
					.findFirst()
					.ifPresent(uid -> deezerFavoritesPlaylistUid = uid);
			List<Playlist> playlists = playlistMapper.mapPlaylist(playlistsDeezer);
			setPlaylistsTitleCache(playlists);
			return playlists;
		} catch (DeezerApiErrorException e) {
			LOG.error("Deezer playlist repository not initialized correctly");
			return new ArrayList<>();
		}
	}

	@Override
	public Playlist createPlaylist() {
		String newPlaylistTitle = DEFAULT_PLAYLIST_TITLE + " " + DateTimeUtils.getCurrentTimestamp();
		return createPlaylist(newPlaylistTitle);
	}

	@Override
	public Playlist createPlaylist(String title) {
		Long newPlaylistId = null;
		try {
			newPlaylistId = deezerApiService.createPlaylist(title);
			if (newPlaylistId != null) {
				ru.push.caudioplayer.core.deezer.model.Playlist playlist = deezerApiService.getPlaylist(newPlaylistId);
				Playlist newPlaylist = playlistMapper.mapPlaylist(playlist);
				updatePlaylistsTitleCache(newPlaylist);
				return newPlaylist;
			}
		} catch (DeezerApiErrorException e) {
			LOG.error("Deezer api error", e);
		}
		return null;
	}

	@Override
	public Playlist deletePlaylist(String playlistUid) {
		Long playlistId = Long.valueOf(playlistUid);
		try {
			Playlist playlist = Optional.ofNullable(deezerApiService.getPlaylist(playlistId))
					.map(p -> playlistMapper.mapPlaylist(p))
					.orElse(null);

			if (playlist == null) {
				LOG.error("Deezer playlist not found: {}", playlistUid);
				return null;
			}
			if (playlist.isReadOnly()) {
				LOG.warn("Read only playlist cannot be deleted: uid = {}", playlistUid);
				return null;
			}

			boolean deleteResult = deezerApiService.deletePlaylist(playlistId);
			if (deleteResult) {
				deleteFromPlaylistsTitleCache(playlistUid);
				return playlist;
			} else {
				LOG.error("Deezer playlist delete fails: {}", playlistUid);
			}
		} catch (DeezerApiErrorException e) {
			LOG.error("Deezer api error", e);
		}

		return null;
	}

	@Override
	public Playlist renamePlaylist(String playlistUid, String newTitle) {
		Long playlistId = Long.valueOf(playlistUid);
		try {
			Playlist playlist = Optional.ofNullable(deezerApiService.getPlaylist(playlistId))
					.map(p -> playlistMapper.mapPlaylist(p))
					.orElse(null);

			if (playlist == null) {
				LOG.error("Deezer playlist not found: {}", playlistUid);
				return null;
			}
			if (playlist.isReadOnly()) {
				LOG.warn("Read only playlist cannot be renamed: uid = {}", playlistUid);
				return null;
			}

			boolean renameResult = deezerApiService.renamePlaylist(playlistId, newTitle);
			if (renameResult) {
				playlist.setTitle(newTitle);
				updatePlaylistsTitleCache(playlist);
				return playlist;
			} else {
				LOG.error("Deezer playlist rename fails: uid = {}, new title = {}", playlistUid, newTitle);
			}
		} catch (DeezerApiErrorException e) {
			LOG.error("Deezer api error", e);
		}

		return null;
	}

	@Override
	public boolean exportPlaylistToFile(String playlistUid, String folderPath) {
		// TODO: add realisation
		return false;
	}

	@Override
	public Playlist deleteItemsFromPlaylist(String playlistUid, List<String> tracksUid) {
		Long playlistId = Long.valueOf(playlistUid);
		try {
			Playlist playlist = Optional.ofNullable(deezerApiService.getPlaylist(playlistId))
					.map(p -> playlistMapper.mapPlaylist(p))
					.orElse(null);

			if (playlist == null) {
				LOG.error("Deezer playlist not found: {}", playlistUid);
				return null;
			}
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

			List<Long> removedTrackIds = deletedTracks.stream()
					.map(o -> Long.valueOf(o.getUid()))
					.collect(Collectors.toList());
			boolean removeResult = deezerApiService.removeTracksFromPlaylist(playlistId, removedTrackIds);

			if (removeResult) {
				playlist.getItems().removeAll(deletedTracks);
				LOG.info("Items deleted from playlist: playlistUid = {}, deleted tracks = {}", playlist.getUid(), deletedTracks);
				return playlist;
			} else {
				LOG.error("Deezer playlist remove tracks fails: uid = {}, trackIds = {}", playlistUid, removedTrackIds);
			}
		} catch (DeezerApiErrorException e) {
			LOG.error("Deezer api error", e);
		}

		return null;
	}

	@Override
	public Playlist addTrackToDeezerPlaylist(String playlistUid, TrackData trackData) {
		return addTrackToDeezerPlaylist(playlistUid, trackData, false);
	}

	@Override
	public Playlist addTrackToDeezerFavoritesPlaylist(TrackData trackData) {
		return deezerFavoritesPlaylistUid != null ?
				addTrackToDeezerPlaylist(deezerFavoritesPlaylistUid, trackData, true) :
				null;
	}

	@Override
	public Playlist addTrackToDeezerPlaylist(Playlist playlist, TrackData trackData) {
		return addTrackToDeezerPlaylist(playlist, trackData, false);
	}

	private Playlist addTrackToDeezerPlaylist(String playlistUid, TrackData trackData, boolean force) {
		Playlist playlist = null;
		Long playlistId = Long.valueOf(playlistUid);
		try {
			playlist = Optional.ofNullable(deezerApiService.getPlaylist(playlistId))
					.map(p -> playlistMapper.mapPlaylist(p))
					.orElse(null);

			if (playlist == null) {
				LOG.error("Deezer playlist not found: {}", playlistUid);
				return null;
			}

			return addTrackToDeezerPlaylist(playlist, trackData, force);

		} catch (DeezerApiErrorException e) {
			LOG.error("Deezer api error", e);
		}

		return playlist;
	}

	private Playlist addTrackToDeezerPlaylist(Playlist playlist, TrackData trackData, boolean force) {
		if (playlist.isReadOnly() && !force) {
			LOG.warn("Cannot add items to read only playlist: uid = {}", playlist.getUid());
			return null;
		}

		try {
			List<Track> searchedTracksResult = deezerApiService.searchTracksQuery(
					formShortSearchQuery(trackData), formExtendedSearchQuery(trackData));
			LOG.info("Searched {} tracks: {}", searchedTracksResult.size(), searchedTracksResult);

			if (!CollectionUtils.isEmpty(searchedTracksResult)) {
				Track searchedTrack = searchedTracksResult.get(0);
				Long trackId = searchedTrack.getId();
				Long playlistId = Long.valueOf(playlist.getUid());
				boolean result = deezerApiService.addTrackToPlaylist(playlistId, trackId);
				if (result) {
					PlaylistTrack newItem = playlistMapper.mapPlaylistItem(playlist, searchedTrack);
					playlist.getItems().add(newItem);
					LOG.info("New items added to playlist: uid = {}, item = {}", playlist.getUid(), newItem);
				} else {
					LOG.error("New item could not be added to Deezer playlist: playlist = {}", playlist);
					playlist = null;
				}
			} else {
				LOG.error("Track not found on Deezer: trackData = {}", trackData);
				playlist = null;
			}
		} catch (DeezerApiErrorException e) {
			LOG.error("Deezer api error", e);
		}

		return null;
	}

	private String formShortSearchQuery(TrackData trackData) {
		return trackData.getArtist() + " " + trackData.getTitle();
	}

	private String formExtendedSearchQuery(TrackData trackData) {
		return trackData.getArtist() + " " + trackData.getTitle() + " " + trackData.getAlbum();
	}

	private void setPlaylistsTitleCache(List<Playlist> playlists) {
		playlistsTitleCache.clear();
		playlistsTitleCache.putAll(
				playlists.stream()
						.collect(Collectors.toMap(Playlist::getUid, Playlist::getTitle))
		);
		LOG.debug("Playlists title cache was set: cache content = {}", playlistsTitleCache);
	}

	private void updatePlaylistsTitleCache(Playlist playlist) {
		boolean updateEntry = playlistsTitleCache.containsKey(playlist.getUid());
		playlistsTitleCache.put(playlist.getUid(), playlist.getTitle());
		LOG.debug(updateEntry ?
						"Playlists title cache entry was updated: playlist uid = {}, cache content = {}" :
						"Playlists title cache entry was added: playlist uid = {}, cache content = {}",
				playlist.getUid(), playlistsTitleCache);
	}

	private void deleteFromPlaylistsTitleCache(String playlistUid) {
		playlistsTitleCache.remove(playlistUid);
		LOG.debug("Playlists title cache entry was deleted: playlist uid = {}, cache content = {}",
				playlistUid, playlistsTitleCache);
	}
}
