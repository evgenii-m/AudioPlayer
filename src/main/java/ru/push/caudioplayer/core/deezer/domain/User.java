package ru.push.caudioplayer.core.deezer.domain;

import org.codehaus.jackson.annotate.JsonIgnoreProperties;

import java.util.Date;

@JsonIgnoreProperties(ignoreUnknown = true)
public class User {

    private Long id;
    private String name;
    private String link;
    private String picture;
    private String type;
    private String lastname;
    private String firstname;
    private Date birthday;
    private Date inscription_date;
    private Character gender;
    private String country;
    private String lang;
    private Integer status;
    private String tracklist;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
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

    public String getType() {
        return type;
    }

    public String getLastname() {
        return lastname;
    }

    public void setLastname(String lastname) {
        this.lastname = lastname;
    }

    public String getFirstname() {
        return firstname;
    }

    public void setFirstname(String firstname) {
        this.firstname = firstname;
    }

    public String getCountry() {
        return country;
    }

    public void setCountry(String country) {
        this.country = country;
    }

    public Date getBirthday() {

        return birthday;
    }

    public void setBirthday(Date birthday) {
        this.birthday = birthday;
    }

    public Date getInscription_date() {
        return inscription_date;
    }

    public void setInscription_date(Date inscription_date) {
        this.inscription_date = inscription_date;
    }

    public Character getGender() {
        return gender;
    }

    public Integer getStatus() {
        return status;
    }

    public void setStatus(Integer status) {
        this.status = status;
    }

    public String getLang() {
        return lang;
    }

    public void setLang(String lang) {
        this.lang = lang;
    }

    public void setGender(Character gender) {

        this.gender = gender;
    }

    public void setType(String type) {
        this.type = type;

    }

    public String getTracklist() {
        return tracklist;
    }

    public void setTracklist(String tracklist) {
        this.tracklist = tracklist;
    }

	@Override
	public String toString() {
		return "User{" +
				"id=" + id +
				", name='" + name + '\'' +
				", link='" + link + '\'' +
				", picture='" + picture + '\'' +
				", type='" + type + '\'' +
				", lastname='" + lastname + '\'' +
				", firstname='" + firstname + '\'' +
				", birthday=" + birthday +
				", inscription_date=" + inscription_date +
				", gender=" + gender +
				", country='" + country + '\'' +
				", lang='" + lang + '\'' +
				", status=" + status +
				", tracklist='" + tracklist + '\'' +
				'}';
	}
}
