package com.connectasistemas.framework.models;

import com.connectasistemas.framework.utils.DateTimeUtils;

/**
 * Modelo de Histórico de Empréstimos
 */
public class BorrowHistory {

    private Integer id;
    private Integer userId;
    private Integer bookId;
    private String borrowedAt;
    private String dueAt;
    private String returnedAt;

    public BorrowHistory() {
        this.borrowedAt = DateTimeUtils.currentTimestamp();
    }

    public BorrowHistory(Integer userId, Integer bookId,
            String borrowedAt, String dueAt, String returnedAt) {

        this.userId = userId;
        this.bookId = bookId;
        this.borrowedAt = DateTimeUtils.normalizeTimestamp(borrowedAt);
        this.dueAt = DateTimeUtils.normalizeTimestamp(dueAt);
        this.returnedAt = DateTimeUtils.normalizeTimestamp(returnedAt);
    }

    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getUserId() {
        return userId;
    }

    public void setUserId(Integer userId) {
        this.userId = userId;
    }

    public Integer getBookId() {
        return bookId;
    }

    public void setBookId(Integer bookId) {
        this.bookId = bookId;
    }

    public String getBorrowedAt() {
        return borrowedAt;
    }

    public void setBorrowedAt(String borrowedAt) {
        this.borrowedAt = DateTimeUtils.normalizeTimestamp(borrowedAt);
    }

    public String getDueAt() {
        return dueAt;
    }

    public void setDueAt(String dueAt) {
        this.dueAt = DateTimeUtils.normalizeTimestamp(dueAt);
    }

    public String getReturnedAt() {
        return returnedAt;
    }

    public void setReturnedAt(String returnedAt) {
        this.returnedAt = DateTimeUtils.normalizeTimestamp(returnedAt);
    }
}
