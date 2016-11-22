package ru.push.caudioplayer;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import ru.push.caudioplayer.controller.MainController;

import java.io.IOException;
import java.io.InputStream;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 22.11.16
 */
@Configuration
public class ConfigurationControllers {

  @Bean(name = "mainView")
  public View getMainView() throws IOException {
    return loadView("view/main.fxml");
  }

  @Bean
  public MainController getMainController() throws IOException {
    return (MainController) getMainView().getController();
  }

  protected View loadView(String url) throws IOException {
    InputStream fxmlStream = null;
    try {
      fxmlStream = getClass().getClassLoader().getResourceAsStream(url);
      FXMLLoader loader = new FXMLLoader();
      loader.load(fxmlStream);
      return new View(loader.getRoot(), loader.getController());
    } finally {
      if (fxmlStream != null) {
        fxmlStream.close();
      }
    }
  }

  public class View {
    private Parent view;
    private Object controller;

    public View(Parent view, Object controller) {
      this.view = view;
      this.controller = controller;
    }

    public Parent getView() {
      return view;
    }

    public void setView(Parent view) {
      this.view = view;
    }

    public Object getController() {
      return controller;
    }

    public void setController(Object controller) {
      this.controller = controller;
    }
  }

}
