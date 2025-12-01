package com.connectasistemas.framework.models;

import com.connectasistemas.framework.utils.DateTimeUtils;

/**
 * Modelo de Livro
 */
public class Book {

    private Integer id;
    private Integer groupCode;
    private Integer sequence;
    private String title;
    private String author;
    private String publisher;
    private Integer year;
    private String isbn;
    private Integer pages;
    private String coverPath;
    private Boolean available;
    private String createdAt;

    public Book() {
        this.createdAt = DateTimeUtils.currentTimestamp();
    }

    public Book(String title, String author, String publisher,
            Integer year, String isbn, Integer pages,
            String coverPath, Boolean available) {

        this.title = title;
        this.author = author;
        this.publisher = publisher;
        this.year = year;
        this.isbn = isbn;
        this.pages = pages;
        this.coverPath = coverPath;
        this.available = available;
        this.createdAt = DateTimeUtils.currentTimestamp();
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getGroupCode() {
        return groupCode;
    }

    public void setGroupCode(Integer groupCode) {
        this.groupCode = groupCode;
    }

    public Integer getSequence() {
        return sequence;
    }

    public void setSequence(Integer sequence) {
        this.sequence = sequence;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getPublisher() {
        return publisher;
    }

    public void setPublisher(String publisher) {
        this.publisher = publisher;
    }

    public Integer getYear() {
        return year;
    }

    public void setYear(Integer year) {
        this.year = year;
    }

    public String getIsbn() {
        return isbn;
    }

    public void setIsbn(String isbn) {
        this.isbn = isbn;
    }

    public Integer getPages() {
        return pages;
    }

    public void setPages(Integer pages) {
        this.pages = pages;
    }

    public String getCoverPath() {
        return coverPath;
    }

    public void setCoverPath(String coverPath) {
        this.coverPath = coverPath;
    }

    public Boolean getAvailable() {
        return available;
    }

    public void setAvailable(Boolean available) {
        this.available = available;
    }

    public String getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(String createdAt) {
        this.createdAt = DateTimeUtils.normalizeTimestamp(createdAt);
    }

    public String getCompositeCode() {
        if (groupCode == null || sequence == null) {
            return id == null ? null : id.toString();
        }
        return groupCode + String.format("%03d", sequence);
    }
}
