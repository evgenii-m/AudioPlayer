package ru.push.caudioplayer.core.config.model;


import javax.xml.bind.annotation.XmlRegistry;

@XmlRegistry
public class ObjectFactory {

	public ObjectFactory() {
	}

	public Configuration createConfiguration() {
		return new Configuration();
	}

}
