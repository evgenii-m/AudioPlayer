package ru.push.caudioplayer.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Label;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 22.11.16
 */
public class MainController {

  private static final Logger LOG = LoggerFactory.getLogger(MainController.class);

  @FXML
  private Label labelMain;

  @FXML
  public void initialize() {
    LOG.debug("initialize()");
  }

  @PostConstruct
  public void init() {
    LOG.debug("init()");
    labelMain.setText("Hello world!");
  }

}
