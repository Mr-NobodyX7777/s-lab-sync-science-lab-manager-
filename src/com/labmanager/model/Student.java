package com.labmanager.model;
//And this is for the Student class
public class Student {
    private int id;
    private String name, classGrade, rollNo, phone;

    public Student() {}

    public Student(int id, String name, String classGrade, String rollNo, String phone) {
        this.id = id; this.name = name; this.classGrade = classGrade; this.rollNo = rollNo; this.phone = phone;
    }

    public int getId() { return id; }
    public void setId(int id) { this.id = id; }
    public String getName() { return name; }
    public void setName(String name) { this.name = name; }
    public String getClassGrade() { return classGrade; }
    public void setClassGrade(String classGrade) { this.classGrade = classGrade; }
    public String getRollNo() { return rollNo; }
    public void setRollNo(String rollNo) { this.rollNo = rollNo; }
    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    @Override
    public String toString() { return name + " (" + rollNo + ")"; }
}
