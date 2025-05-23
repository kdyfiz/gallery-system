package com.mycompany.myapp.service.dto;

import jakarta.persistence.Lob;
import jakarta.validation.constraints.*;
import java.io.Serializable;
import java.time.Instant;
import java.util.Objects;

/**
 * A DTO for the {@link com.mycompany.myapp.domain.Album} entity.
 */
@SuppressWarnings("common-java:DuplicatedBlocks")
public class AlbumDTO implements Serializable {

    private Long id;

    @NotNull
    @Size(min = 3, max = 255)
    private String name;

    @Size(max = 255)
    private String event;

    @NotNull
    private Instant creationDate;

    private Instant overrideDate;

    @Lob
    private byte[] thumbnail;

    private String thumbnailContentType;

    private UserDTO user;

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

    public String getEvent() {
        return event;
    }

    public void setEvent(String event) {
        this.event = event;
    }

    public Instant getCreationDate() {
        return creationDate;
    }

    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }

    public Instant getOverrideDate() {
        return overrideDate;
    }

    public void setOverrideDate(Instant overrideDate) {
        this.overrideDate = overrideDate;
    }

    public byte[] getThumbnail() {
        return thumbnail;
    }

    public void setThumbnail(byte[] thumbnail) {
        this.thumbnail = thumbnail;
    }

    public String getThumbnailContentType() {
        return thumbnailContentType;
    }

    public void setThumbnailContentType(String thumbnailContentType) {
        this.thumbnailContentType = thumbnailContentType;
    }

    public UserDTO getUser() {
        return user;
    }

    public void setUser(UserDTO user) {
        this.user = user;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AlbumDTO)) {
            return false;
        }

        AlbumDTO albumDTO = (AlbumDTO) o;
        if (this.id == null) {
            return false;
        }
        return Objects.equals(this.id, albumDTO.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.id);
    }

    // prettier-ignore
    @Override
    public String toString() {
        return "AlbumDTO{" +
            "id=" + getId() +
            ", name='" + getName() + "'" +
            ", event='" + getEvent() + "'" +
            ", creationDate='" + getCreationDate() + "'" +
            ", overrideDate='" + getOverrideDate() + "'" +
            ", thumbnail='" + getThumbnail() + "'" +
            ", user=" + getUser() +
            "}";
    }
}
