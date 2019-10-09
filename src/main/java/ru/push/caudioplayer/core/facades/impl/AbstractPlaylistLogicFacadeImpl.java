package ru.push.caudioplayer.core.facades.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import ru.push.caudioplayer.core.facades.PlaylistLogicFacade;
import ru.push.caudioplayer.core.facades.dto.NotificationData;
import ru.push.caudioplayer.core.facades.dto.PlaylistData;
import ru.push.caudioplayer.core.mediaplayer.AudioPlayerEventListener;
import ru.push.caudioplayer.core.playlist.PlaylistService;
import ru.push.caudioplayer.core.playlist.model.Playlist;

import java.util.ArrayList;
import java.util.List;

public abstract class AbstractPlaylistLogicFacadeImpl implements PlaylistLogicFacade {

	private static final Logger LOG = LoggerFactory.getLogger(AbstractPlaylistLogicFacadeImpl.class);

	protected final List<AudioPlayerEventListener> eventListeners;

	@Autowired
	private DtoMapper dtoMapper;


	public AbstractPlaylistLogicFacadeImpl() {
		this.eventListeners = new ArrayList<>();
	}

	protected abstract PlaylistService getService();

	@Override
	public synchronized void addEventListener(AudioPlayerEventListener listener) {
		eventListeners.add(listener);
	}

	@Override
	public synchronized void removeEventListener(AudioPlayerEventListener listener) {
		eventListeners.remove(listener);
	}

	@Override
	public List<PlaylistData> getPlaylists() {
		return dtoMapper.mapPlaylistData(
				getService().getPlaylists()
		);
	}

	@Override
	public void createPlaylist() {
		Playlist result = getService().createPlaylist();
		if (result != null) {
			eventListeners.forEach(l -> l.createdNewPlaylist(dtoMapper.mapPlaylistData(result)));
			sendNotification(
					String.format(NotificationMessages.CREATE_PLAYLIST_SUCCESS, result.getUid(), result.getTitle())
			);
		} else {
			sendNotification(NotificationMessages.CREATE_PLAYLIST_FAIL);
		}
	}

	@Override
	public void deletePlaylist(String playlistUid) {
		Playlist result = getService().deletePlaylist(playlistUid);
		if (result != null) {
			eventListeners.forEach(l -> l.deletedPlaylist(dtoMapper.mapPlaylistData(result)));
			sendNotification(
					String.format(NotificationMessages.DELETE_PLAYLIST_SUCCESS, result.getUid())
			);
		} else {
			sendNotification(
					String.format(NotificationMessages.DELETE_PLAYLIST_FAIL, playlistUid)
			);
		}
	}

	@Override
	public void renamePlaylist(String playlistUid, String newTitle) {
		Playlist result = getService().renamePlaylist(playlistUid, newTitle);
		if (result != null) {
			eventListeners.forEach(l -> l.renamedPlaylist(dtoMapper.mapPlaylistData(result)));
			sendNotification(
					String.format(NotificationMessages.RENAME_PLAYLIST_SUCCESS, playlistUid, newTitle)
			);
		} else {
			sendNotification(
					String.format(NotificationMessages.RENAME_PLAYLIST_FAIL, playlistUid, newTitle)
			);
		}
	}

	@Override
	public void backupPlaylists(String folderName) {
		List<Playlist> playlists = getService().getPlaylists();
		playlists.forEach(p -> getService().exportPlaylistToFile(p.getUid(), folderName));
		LOG.info("Playlists backups to folder '{}'", folderName);
	}

	@Override
	public void exportPlaylistToFile(String playlistUid, String folderPath) {
		getService().exportPlaylistToFile(playlistUid, folderPath);
	}

	@Override
	public void deleteItemsFromPlaylist(String playlistUid, List<String> tracksUid) {
		Playlist result = getService().deleteItemsFromPlaylist(playlistUid, tracksUid);
		if (result != null) {
			eventListeners.forEach(listener -> listener.changedPlaylist(dtoMapper.mapPlaylistData(result)));
			sendNotification(
					String.format(NotificationMessages.DELETE_TRACKS_FROM_PLAYLIST_SUCCESS, playlistUid)
			);
		} else {
			sendNotification(
					String.format(NotificationMessages.DELETE_TRACKS_FROM_PLAYLIST_FAIL, playlistUid)
			);
		}
	}

	protected void sendNotification(String messageText) {
		eventListeners.forEach(l -> l.obtainedNotification(new NotificationData(messageText)));
	}
}
