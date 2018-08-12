package ru.bmstu.ORM.Service.Tables;

import ru.bmstu.ORM.Service.ColumnAnnotations.*;

import java.sql.Date;
import java.util.Objects;

@Table(db = "shopdb", schema = "shopschema", name = "employee")
public class Employee implements Entity {
    @PK
    @Column(name = "employeeCode", unique = true, nullable = false)
    private Integer employeeCode;

    @Column(name = "firstName", nullable = false, length = 25)
    private String firstName;

    @Column(name = "lastName", nullable = false, length = 25)
    private String lastName;

    @Column(name = "middleName", length = 25)
    private String middleName;

    @Column(name = "dateOfBirth", nullable = false)
    private Date dateOfBirth;

    @Column(name = "phone", unique = true, nullable = false, length = 11)
    private String phone;

    @Column(name = "position", nullable = false)
    private String position;

    @Default
    @Column(name = "isFired", nullable = false)
    private Boolean isFired = true;

    @Column(name = "salary", nullable = false)
    private Float salary;

    @Column(name = "sex", nullable = false)
    private String sex;

    @Column(name = "chief")
    private Integer chief;

    @FK(table = "shop", referencedColumn = "shopCode")
    @Column(name = "shopCode")
    private Integer shopCode;

    @FO(table = "shop")
    private Shop shop;

    public int getEmployeeCode() {
        return employeeCode;
    }

    public void setEmployeeCode(int employeeCode) {
        this.employeeCode = employeeCode;
    }

    public String getFirstName() {
        return firstName;
    }

    public void setFirstName(String firstName) {
        this.firstName = firstName;
    }

    public String getLastName() {
        return lastName;
    }

    public void setLastName(String lastName) {
        this.lastName = lastName;
    }

    public String getMiddleName() {
        return middleName;
    }

    public void setMiddleName(String middleName) {
        this.middleName = middleName;
    }

    public Date getDateOfBirth() {
        return dateOfBirth;
    }

    public void setDateOfBirth(Date dateOfBirth) {
        this.dateOfBirth = dateOfBirth;
    }

    public String getPhone() {
        return phone;
    }

    public void setPhone(String phone) {
        this.phone = phone;
    }

    public String getPosition() {
        return position;
    }

    public void setPosition(String position) {
        this.position = position;
    }

    public boolean isFired() {
        return isFired;
    }

    public void setFired(boolean fired) {
        isFired = fired;
    }

    public float getSalary() {
        return salary;
    }

    public void setSalary(float salary) {
        this.salary = salary;
    }

    public String getSex() {
        return sex;
    }

    public void setSex(String sex) {
        this.sex = sex;
    }

    public int getChief() {
        return chief;
    }

    public void setChief(int chief) {
        this.chief = chief;
    }

    public int getShopCode() {
        return shopCode;
    }

    public void setShopCode(int shopCode) {
        this.shopCode = shopCode;
    }

    public Shop getShop() {
        return shop;
    }

    public void setShop(Shop shop) {
        this.shop = shop;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (this.getClass() != obj.getClass())
            return false;

        Employee other = (Employee) obj;
        return Objects.equals(this.employeeCode, other.employeeCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(this.employeeCode);
    }

    @Override
    public String toString() {
        return "Employee { " + "employeeCode: " + employeeCode + ", firstName: " + firstName + ", lastName: " + lastName +
                ", middleName: " + middleName + ", dateOfBirth: " + dateOfBirth + ", phone: " + phone +
                ", position: " + position + ", isFired: " + isFired + ", salary: " + salary + ", sex: " + sex +
                ", chief: " + chief + ", shopCode: " + shopCode + " } ";
    }
}
