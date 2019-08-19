package ru.push.caudioplayer.core.deezer.domain;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

@JsonIgnoreProperties(ignoreUnknown = true)
public class Playlist {

    private Long id;
    private String title;
    private String description;
    private Integer duration;

    @JsonProperty(value = "public")
    private Boolean is_public;

    private Boolean is_loved_track;
    private Boolean collaborative;
    private Integer rating;
    private String link;
    private String picture;
    private String checksum;
    private Creator creator;
    private String type;
    private Tracks tracks;
    private Integer nb_tracks;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public Integer getDuration() {
        return duration;
    }

    public void setDuration(Integer duration) {
        this.duration = duration;
    }

    public Boolean getIs_loved_track() {
        return is_loved_track;
    }

    public void setIs_loved_track(Boolean is_loved_track) {
        this.is_loved_track = is_loved_track;
    }

    public Boolean getCollaborative() {
        return collaborative;
    }

    public void setCollaborative(Boolean collaborative) {
        this.collaborative = collaborative;
    }

    public Integer getRating() {
        return rating;
    }

    public void setRating(Integer rating) {
        this.rating = rating;
    }

    public String getLink() {
        return link;
    }

    public void setLink(String link) {
        this.link = link;
    }

    public String getPicture() {
        return picture;
    }

    public void setPicture(String picture) {
        this.picture = picture;
    }

    public String getChecksum() {
        return checksum;
    }

    public void setChecksum(String checksum) {
        this.checksum = checksum;
    }

    public Boolean getIs_public() {
        return is_public;
    }

    public void setIs_public(Boolean is_public) {
        this.is_public = is_public;
    }

    public String getType() {
        return type;
    }

    public void setType(String type) {
        this.type = type;
    }

    public Creator getCreator() {
        return creator;
    }

    public void setCreator(Creator creator) {
        this.creator = creator;
    }

    public Tracks getTracks() {
        return tracks;
    }

    public void setTracks(Tracks tracks) {
        this.tracks = tracks;
    }

    public Integer getNb_tracks() {
        return nb_tracks;
    }

    public void setNb_tracks(Integer nb_tracks) {
        this.nb_tracks = nb_tracks;
    }

	@Override
	public String toString() {
		return "Playlist{" +
				"id=" + id +
				", title='" + title + '\'' +
				", description='" + description + '\'' +
				", duration=" + duration +
				", is_public=" + is_public +
				", is_loved_track=" + is_loved_track +
				", collaborative=" + collaborative +
				", rating=" + rating +
				", link='" + link + '\'' +
				", picture='" + picture + '\'' +
				", checksum='" + checksum + '\'' +
				", creator=" + creator +
				", type='" + type + '\'' +
				", tracks=" + tracks +
				", nb_tracks=" + nb_tracks +
				'}';
	}
}
