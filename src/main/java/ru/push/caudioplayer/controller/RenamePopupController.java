package ru.push.caudioplayer.controller;

import javafx.fxml.FXML;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.annotation.PostConstruct;
import java.awt.*;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 6/19/17
 */
public class RenamePopupController {

  private static final Logger LOG = LoggerFactory.getLogger(RenamePopupController.class);

  @FXML
  private TextField nameTextField;
  @FXML
  private Button cancelButton;
  @FXML
  private Button applyButton;

  @FXML
  public void initialize() {
    LOG.debug("initialize");
  }

  @PostConstruct
  public void init() {
    LOG.debug("init");
  }

}
