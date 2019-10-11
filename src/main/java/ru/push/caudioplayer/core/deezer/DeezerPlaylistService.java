package ru.push.caudioplayer.core.deezer;


import ru.push.caudioplayer.core.playlist.PlaylistService;
import ru.push.caudioplayer.core.playlist.dto.TrackData;
import ru.push.caudioplayer.core.playlist.model.Playlist;

public interface DeezerPlaylistService extends PlaylistService {

	Playlist addTrackToDeezerPlaylist(String playlistUid, TrackData trackData);

	Playlist addTrackToDeezerFavoritesPlaylist(TrackData trackData);

	Playlist addTrackToDeezerPlaylist(Playlist playlist, TrackData trackData);
}
