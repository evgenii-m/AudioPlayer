package ru.push.caudioplayer.ui;

import java.util.ArrayList;
import java.util.List;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 6/27/17
 */
public class PlaylistContainerViewConfigurations {

  final private List<PlaylistContainerColumn> columns;

  public PlaylistContainerViewConfigurations(List<PlaylistContainerColumn> columns) {
    this.columns = columns;
  }

  public List<PlaylistContainerColumn> getColumns() {
    return columns;
  }


  public static class PlaylistContainerColumn {
    private final String name;
    private final String title;
    private final int width;

    public PlaylistContainerColumn(String name, String title, int width) {
      this.name = name;
      this.title = title;
      this.width = width;
    }

    public String getName() {
      return name;
    }

    public String getTitle() {
      return title;
    }

    public int getWidth() {
      return width;
    }
  }
}
