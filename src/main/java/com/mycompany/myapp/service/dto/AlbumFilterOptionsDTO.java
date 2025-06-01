package com.mycompany.myapp.service.dto;

import java.util.List;
import java.util.Objects;

/**
 * A DTO for Album filter options.
 */
public class AlbumFilterOptionsDTO {

    private List<String> events;
    private List<Integer> years;
    private List<String> tags;
    private List<String> contributors;

    public AlbumFilterOptionsDTO() {}

    public AlbumFilterOptionsDTO(List<String> events, List<Integer> years, List<String> tags, List<String> contributors) {
        this.events = events;
        this.years = years;
        this.tags = tags;
        this.contributors = contributors;
    }

    public List<String> getEvents() {
        return events;
    }

    public void setEvents(List<String> events) {
        this.events = events;
    }

    public AlbumFilterOptionsDTO events(List<String> events) {
        this.setEvents(events);
        return this;
    }

    public List<Integer> getYears() {
        return years;
    }

    public void setYears(List<Integer> years) {
        this.years = years;
    }

    public AlbumFilterOptionsDTO years(List<Integer> years) {
        this.setYears(years);
        return this;
    }

    public List<String> getTags() {
        return tags;
    }

    public void setTags(List<String> tags) {
        this.tags = tags;
    }

    public AlbumFilterOptionsDTO tags(List<String> tags) {
        this.setTags(tags);
        return this;
    }

    public List<String> getContributors() {
        return contributors;
    }

    public void setContributors(List<String> contributors) {
        this.contributors = contributors;
    }

    public AlbumFilterOptionsDTO contributors(List<String> contributors) {
        this.setContributors(contributors);
        return this;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) {
            return true;
        }
        if (!(o instanceof AlbumFilterOptionsDTO)) {
            return false;
        }

        AlbumFilterOptionsDTO albumFilterOptionsDTO = (AlbumFilterOptionsDTO) o;
        return (
            Objects.equals(this.events, albumFilterOptionsDTO.events) &&
            Objects.equals(this.years, albumFilterOptionsDTO.years) &&
            Objects.equals(this.tags, albumFilterOptionsDTO.tags) &&
            Objects.equals(this.contributors, albumFilterOptionsDTO.contributors)
        );
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.events, this.years, this.tags, this.contributors);
    }

    @Override
    public String toString() {
        return (
            "AlbumFilterOptionsDTO{" +
            "events=" +
            getEvents() +
            ", years=" +
            getYears() +
            ", tags=" +
            getTags() +
            ", contributors=" +
            getContributors() +
            "}"
        );
    }
}
