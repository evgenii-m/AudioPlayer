package ru.push.caudioplayer.controller;

import javafx.fxml.FXML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 23.11.16
 */
public class MediaPlayerController {

  private static final Logger LOGGER = LoggerFactory.getLogger(MediaPlayerController.class);

  @FXML
  public void initialize() {
    LOGGER.debug("initialize");
  }

  @PostConstruct
  public void init() {
    LOGGER.debug("init");
  }
}
