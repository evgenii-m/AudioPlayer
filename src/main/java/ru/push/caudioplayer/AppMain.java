package ru.push.caudioplayer;

import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Lazy;
import uk.co.caprica.vlcj.discovery.NativeDiscovery;

import javax.annotation.Resource;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 22.11.16
 */
@Lazy
@SpringBootApplication
public class AppMain extends AbstractJavaFxApplicationSupport {

  private static final double DEFAULT_WIDTH  = 400;
  private static final double DEFAULT_HEIGHT = 250;

  @Value("${ui.title:JavaFX приложение}")
  private String windowTitle;

  @Resource(name = "mainView")
  private ConfigurationControllers.View mainView;

  @Override
  public void start(Stage stage) throws Exception {
    new NativeDiscovery().discover();     // discover libvlc native libraries

    stage.setTitle(windowTitle);
    stage.setScene(new Scene(mainView.getView(), DEFAULT_WIDTH, DEFAULT_HEIGHT));
    stage.setResizable(true);
    stage.centerOnScreen();
    stage.show();
  }

  public static void main(String[] args) {
    launchApp(AppMain.class, args);
  }
}
