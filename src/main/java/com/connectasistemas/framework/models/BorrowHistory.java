package com.connectasistemas.framework.models;

/**
 * Modelo de Histórico de Empréstimos
 */
public class BorrowHistory {

    private Integer id;
    private Integer userId;
    private Integer bookId;
    private String borrowedAt;
    private String returnedAt;

    public BorrowHistory() {
    }

    public BorrowHistory(Integer userId, Integer bookId,
            String borrowedAt, String returnedAt) {

        this.userId = userId;
        this.bookId = bookId;
        this.borrowedAt = borrowedAt;
        this.returnedAt = returnedAt;
    }

    public Integer getId() {
        return id;
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
        this.borrowedAt = borrowedAt;
    }

    public String getReturnedAt() {
        return returnedAt;
    }

    public void setReturnedAt(String returnedAt) {
        this.returnedAt = returnedAt;
    }
}
