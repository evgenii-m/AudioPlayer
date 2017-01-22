package ru.push.caudioplayer.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import javafx.scene.layout.BorderPane;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import ru.push.caudioplayer.ConfigurationControllers;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 22.11.16
 */
public class MainController {

  private static final Logger LOGGER = LoggerFactory.getLogger(MainController.class);

  @FXML
  private BorderPane root;

  @FXML
  @Resource(name = "mediaPlayerView")
  private ConfigurationControllers.View mediaPlayerView;

  @FXML
  public void initialize() {
    LOGGER.debug("initialize");
  }

  @PostConstruct
  public void init() {
    LOGGER.debug("init");
    root.setTop(mediaPlayerView.getView());
  }

}
