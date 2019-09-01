package ru.push.caudioplayer.core.converter.domain;


import javax.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {

	public ObjectFactory() {
	}

	public PlaylistExportData createPlaylistExportData() {
		return new PlaylistExportData();
	}
}
