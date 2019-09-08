package ru.push.caudioplayer.core.playlist.dao.impl;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;
import ru.push.caudioplayer.core.playlist.dao.LocalPlaylistRepository;
import ru.push.caudioplayer.core.playlist.dao.entity.PlaylistEntity;
import ru.push.caudioplayer.utils.XmlUtils;

import javax.xml.bind.JAXBException;
import java.io.BufferedWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Component
public class LocalPlaylistRepositoryImpl implements LocalPlaylistRepository {

	private static final Logger LOG = LoggerFactory.getLogger(LocalPlaylistRepositoryImpl.class);

	private static final String DEFAULT_PLAYLISTS_FOLDER_PATH = "playlists/";
	private static final String DEFAULT_PLAYLIST_FILE_EXT = ".xml";

	// todo: make read from configuration file
	private String playlistStoreFolder = DEFAULT_PLAYLISTS_FOLDER_PATH;
	private Map<String, PlaylistEntity> playlistStoreMap = new HashMap<>();

	@Override
	public boolean refresh() {

		Path playlistsFolderPath = Paths.get(playlistStoreFolder);
		try {
			if (Files.notExists(playlistsFolderPath) || !Files.isDirectory(playlistsFolderPath)) {
				Files.createDirectories(playlistsFolderPath);
			}
		} catch (IOException e) {
			LOG.error("Create local playlist store folder error", e);
			return false;
		}

		playlistStoreMap = new HashMap<>();
		try (Stream<Path> paths = Files.walk(Paths.get(DEFAULT_PLAYLISTS_FOLDER_PATH))) {
			paths
					.filter(Files::isRegularFile)
					.forEach(p -> {
						LOG.debug("Playlist folder file detected: {}", p);
						try {
							String playlistFileContent = new String(Files.readAllBytes(p));
							PlaylistEntity playlist = XmlUtils.unmarshalDocumnet(playlistFileContent, PlaylistEntity.class.getPackage().getName());
							playlistStoreMap.put(playlist.getUid(), playlist);
						} catch (JAXBException | IOException e) {
							LOG.error("Playlist loading error: file path = {}, error = {}", p, e);
						}
					});
		} catch (IOException e) {
			LOG.error("Playlist loading error", e);
			return false;
		}

		return true;
	}

	@Override
	public PlaylistEntity findPlaylist(String uid) {
		return playlistStoreMap.get(uid);
	}

	@Override
	public List<PlaylistEntity> findPlaylists(List<String> uidList) {
		return uidList.stream()
				.filter(uid -> {
					if (!playlistStoreMap.containsKey(uid)) {
						LOG.warn("Playlist not found in local store: uid = {}", uid);
						return false;
					}
					return true;
				})
				.map(playlistStoreMap::get)
				.collect(Collectors.toList());
	}

	@Override
	public boolean savePlaylist(PlaylistEntity playlist) {
		Path filePath = constructPlaylistFilePath(playlist.getUid());
		try (BufferedWriter bufferedWriter = Files.newBufferedWriter(filePath, StandardCharsets.UTF_8)) {
			XmlUtils.marshalDocument(playlist, bufferedWriter, PlaylistEntity.class.getPackage().getName());
			LOG.debug("save playlist {}", playlist.getUid());
			playlistStoreMap.put(playlist.getUid(), playlist);
			return true;
		} catch (JAXBException | IOException e) {
			LOG.error("Error when save playlist configuration to file.", e);
			return false;
		}
	}

	@Override
	public boolean deletePlaylist(String uid) {
		if (!playlistStoreMap.containsKey(uid)) {
			LOG.warn("Playlist not found in local store: uid = {}", uid);
			return false;
		}

		try {
			Files.delete(constructPlaylistFilePath(uid));
			LOG.debug("Playlist successfully deleted: uid = {}", uid);
			playlistStoreMap.remove(uid);
			return true;
		} catch (IOException e) {
			LOG.error("Delete playlist file error: uid = {}, error = {}", uid, e);
			return false;
		}
	}

	private Path constructPlaylistFilePath(String playlistUid) {
		return Paths.get(playlistStoreFolder + playlistUid + DEFAULT_PLAYLIST_FILE_EXT);
	}
}
