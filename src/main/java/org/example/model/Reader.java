package org.example.model;

import java.time.LocalDate;

public class Reader {
    private String readerId;
    private String fullName;
    private String idCard;
    private LocalDate dob;
    private String gender;
    private String email;
    private String address;
    private LocalDate cardDate;
    private LocalDate expiryDate;

    public Reader(String readerId, String fullName, String idCard, LocalDate dob,
                  String gender, String email, String address, LocalDate cardDate) {
        this.readerId = readerId;
        this.fullName = fullName;
        this.idCard = idCard;
        this.dob = dob;
        this.gender = gender;
        this.email = email;
        this.address = address;
        this.cardDate = cardDate;
        this.expiryDate = cardDate.plusMonths(48);
    }

    public Reader(String readerId, String fullName, String idCard, LocalDate dob,
                  String gender, String email, String address, LocalDate cardDate, LocalDate expiryDate) {
        this.readerId = readerId;
        this.fullName = fullName;
        this.idCard = idCard;
        this.dob = dob;
        this.gender = gender;
        this.email = email;
        this.address = address;
        this.cardDate = cardDate;
        this.expiryDate = expiryDate;
    }

    public String getReaderId() { return readerId; }
    public void setReaderId(String readerId) { this.readerId = readerId; }

    public String getFullName() { return fullName; }
    public void setFullName(String fullName) { this.fullName = fullName; }

    public String getIdCard() { return idCard; }
    public void setIdCard(String idCard) { this.idCard = idCard; }

    public LocalDate getDob() { return dob; }
    public void setDob(LocalDate dob) { this.dob = dob; }

    public String getGender() { return gender; }
    public void setGender(String gender) { this.gender = gender; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getAddress() { return address; }
    public void setAddress(String address) { this.address = address; }

    public LocalDate getCardDate() { return cardDate; }
    public void setCardDate(LocalDate cardDate) {
        this.cardDate = cardDate;
        this.expiryDate = cardDate.plusMonths(48);
    }

    public LocalDate getExpiryDate() { return expiryDate; }
    public void setExpiryDate(LocalDate expiryDate) { this.expiryDate = expiryDate; }
}
