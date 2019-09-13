package ru.push.caudioplayer.core.facades.impl;

interface NotificationMessages {

	String LAST_FM_AUTHORIZATION_SUCCESS = "Last.fm authorization success";
	String LAST_FM_AUTHORIZATION_FAIL = "Last.fm authorization fail";
	String DEEZER_AUTHORIZATION_SUCCESS = "Deezer authorization success";
	String DEEZER_AUTHORIZATION_FAIL = "Deezer authorization fail";

	String CREATE_PLAYLIST_SUCCESS = "Created new playlist: uid = '%s', title = '%s'";
	String CREATE_PLAYLIST_FAIL = "Failed to create new playlist";
	String DELETE_PLAYLIST_SUCCESS = "Delete playlist: uid = '%s'";
	String DELETE_PLAYLIST_FAIL = "Failed to delete playlist: uid = '%s'";
	String RENAME_PLAYLIST_SUCCESS = "Renamed playlist: uid = '%s', title = '%s'";
	String RENAME_PLAYLIST_FAIL = "Failed to rename playlist: uid = '%s', title = '%s'";
	String ADD_TRACKS_TO_PLAYLIST_SUCCESS = "Tracks added to playlist: uid = '%s', title = '%s'";
	String ADD_TRACKS_TO_PLAYLIST_FAIL = "Could not add tracks to playlist: uid = '%s'";
	String ADD_TRACKS_TO_PLAYLIST_FAIL_BY_TITLE = "Could not add tracks to playlist: title = '%s'";
	String DELETE_TRACKS_FROM_PLAYLIST_SUCCESS = "Tracks deleted from playlist: uid = '%s'";
	String DELETE_TRACKS_FROM_PLAYLIST_FAIL = "Could not delete tracks from playlist: uid = '%s'";
	String ADD_TRACK_TO_DEEZER_LOVED_TRACKS_SUCCESS = "Tracks added to Deezer loved tracks";
	String ADD_TRACK_TO_DEEZER_LOVED_TRACKS_FAIL = "Could not add tracks to Deezer loved tracks";
	String TRACK_MEDIA_DATA_INCORRECT = "Incorrect track media data received";

}
