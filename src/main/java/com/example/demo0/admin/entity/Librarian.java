package com.example.demo0.admin.entity;

import jakarta.persistence.*;

@Entity
@Table(name = "librarian")
public class Librarian {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "librarianid")
    private Integer librarianId;

    @Column(name = "staffno", nullable = false, length = 20)
    private String staffNo;

    @Column(name = "password", nullable = false, length = 120)
    private String password;

    @Column(name = "name", nullable = false, length = 40)
    private String name;

    @Column(name = "permission", nullable = false, length = 10)
    private String permission;

    // === Constructors ===
    public Librarian() {
    }

    public Librarian(String staffNo, String password, String name, String permission) {
        this.staffNo = staffNo;
        this.password = password;
        this.name = name;
        this.permission = permission;
    }

    // === Getters & Setters ===
    public Integer getLibrarianId() {
        return librarianId;
    }

    public void setLibrarianId(Integer librarianId) {
        this.librarianId = librarianId;
    }

    public String getStaffNo() {
        return staffNo;
    }

    public void setStaffNo(String staffNo) {
        this.staffNo = staffNo;
    }

    public String getPassword() {
        return password;
    }

    public void setPassword(String password) {
        this.password = password;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getPermission() {
        return permission;
    }

    public void setPermission(String permission) {
        this.permission = permission;
    }

    // === toString() ===
    @Override
    public String toString() {
        return "Librarian{" +
                "librarianId=" + librarianId +
                ", staffNo='" + staffNo + '\'' +
                ", password='" + password + '\'' +
                ", name='" + name + '\'' +
                ", permission='" + permission + '\'' +
                '}';
    }
}
