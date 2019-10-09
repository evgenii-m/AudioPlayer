package ru.push.caudioplayer.core.facades;

import ru.push.caudioplayer.core.facades.dto.PlaylistData;
import ru.push.caudioplayer.core.mediaplayer.AudioPlayerEventListener;

import java.util.List;

public interface PlaylistLogicFacade {

	void addEventListener(AudioPlayerEventListener listener);

	void removeEventListener(AudioPlayerEventListener listener);

	List<PlaylistData> getPlaylists();

	void createPlaylist();

	void deletePlaylist(String playlistUid);

	void renamePlaylist(String playlistUid, String newTitle);

	void backupPlaylists(String folderName);

	void exportPlaylistToFile(String playlistUid, String folderPath);

	void deleteItemsFromPlaylist(String playlistUid, List<String> tracksUid);
}
