package com.connectasistemas.framework.models;

public class BorrowHistory {
    private Integer id;
    private String userId;     // FK → user.id
    private String bookId;     // FK → book.id
    private String borrowedAt;
    private String returnedAt;

    public BorrowHistory() {
    }

    public BorrowHistory(String userId, String bookId,
                         String borrowedAt, String returnedAt) {

        this.userId = userId;
        this.bookId = bookId;
        this.borrowedAt = borrowedAt;
        this.returnedAt = returnedAt;
    }

    public Integer getId() {
        return id;
    }

    public String getUserId() {
        return userId;
    }

    public void setUserId(String userId) {
        this.userId = userId;
    }

    public String getBookId() {
        return bookId;
    }

    public void setBookId(String bookId) {
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

