package ru.push.caudioplayer.ui.configuration;

import java.util.List;

/**
 * @author push <mez.e.s@yandex.ru>
 * @date 6/27/17
 */
public class PlaylistContainerViewConfigurations {
  public final static String COLUMN_NUMBER_NAME = "number";
  public final static String COLUMN_ARTIST_NAME = "artist";
  public final static String COLUMN_ALBUM_NAME = "album";
  public final static String COLUMN_TITLE_NAME = "title";
  public final static String COLUMN_LENGTH_NAME = "length";

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
    private final double width;

    public PlaylistContainerColumn(String name, String title, double width) {
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

    public double getWidth() {
      return width;
    }
  }
}
