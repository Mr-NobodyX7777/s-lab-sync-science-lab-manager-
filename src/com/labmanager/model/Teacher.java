package com.labmanager.model;
//Aaand this is the class for teachers, which is a model class just like the others. Mr.NobodyX7777
public class Teacher {
    private int id;
    private String name, subject, phone, email;

    public Teacher() {}

    public Teacher(int id, String name, String subject, String phone, String email) {
        this.id = id; this.name = name; this.subject = subject; this.phone = phone; this.email = email;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getSubject() { return subject; }
    public void setSubject(String subject) { this.subject = subject; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }
    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    @Override
    public String toString() { return name; }
}
