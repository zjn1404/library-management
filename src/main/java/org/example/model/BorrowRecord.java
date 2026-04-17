package org.example.model;

import java.time.LocalDate;
import java.util.List;

public class BorrowRecord {
    private String borrowId;
    private String readerId;
    private LocalDate borrowDate;
    private LocalDate expectedReturnDate;
    private LocalDate actualReturnDate;
    private List<String> isbns;
    private String status;

    public BorrowRecord(String borrowId, String readerId, LocalDate borrowDate, LocalDate actualReturnDate, List<String> isbns, String status) {
        this.borrowId = borrowId;
        this.readerId = readerId;
        this.borrowDate = borrowDate;
        this.expectedReturnDate = borrowDate.plusDays(7);
        this.actualReturnDate = actualReturnDate;
        this.isbns = isbns;
        this.status = status;
    }

    public String getBorrowId() { return borrowId; }
    public void setBorrowId(String borrowId) { this.borrowId = borrowId; }

    public String getReaderId() { return readerId; }
    public void setReaderId(String readerId) { this.readerId = readerId; }

    public LocalDate getBorrowDate() { return borrowDate; }
    public void setBorrowDate(LocalDate borrowDate) {
        this.borrowDate = borrowDate;
        this.expectedReturnDate = borrowDate.plusDays(7);
    }

    public LocalDate getExpectedReturnDate() { return expectedReturnDate; }
    public void setExpectedReturnDate(LocalDate expectedReturnDate) { this.expectedReturnDate = expectedReturnDate; }

    public LocalDate getActualReturnDate() { return actualReturnDate; }
    public void setActualReturnDate(LocalDate actualReturnDate) { this.actualReturnDate = actualReturnDate; }

    public List<String> getIsbns() { return isbns; }
    public void setIsbns(List<String> isbns) { this.isbns = isbns; }

    public String getStatus() { return status; }
    public void setStatus(String status) { this.status = status; }
}
