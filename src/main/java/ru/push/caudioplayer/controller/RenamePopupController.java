package ru.push.caudioplayer.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.util.Assert;
import ru.push.caudioplayer.core.facades.MusicLibraryLogicFacade;
import ru.push.caudioplayer.core.facades.dto.PlaylistData;

import javax.annotation.PostConstruct;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 6/19/17
 */
public class RenamePopupController {

  private static final Logger LOG = LoggerFactory.getLogger(RenamePopupController.class);

  @FXML
  private TextField nameTextField;

	@Autowired
	private MusicLibraryLogicFacade musicLibraryLogicFacade;

  private PlaylistData renamedPlaylist;

  @FXML
  public void initialize() {
		LOG.debug("initialize FXML for {}", this.getClass().getName());
  }

  @PostConstruct
  public void init() {
    LOG.debug("init bean {}", this.getClass().getName());
  }

  public void setRenamedPlaylist(PlaylistData playlist) {
    Assert.notNull(playlist);

    renamedPlaylist = playlist;
    nameTextField.setText(renamedPlaylist.getTitle());
  }

  public void cancelRename(ActionEvent actionEvent) {
    closePopup();
  }

  public void applyRename(ActionEvent actionEvent) {
		musicLibraryLogicFacade.renamePlaylist(renamedPlaylist.getUid(), nameTextField.getText());
		closePopup();
  }

  private void closePopup() {
    Stage popupStage = (Stage) nameTextField.getScene().getWindow();
    popupStage.close();
  }
}
