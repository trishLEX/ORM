package ru.bmstu.ORM.Service.Tables;

import ru.bmstu.ORM.Service.ColumnAnnotations.*;

import java.util.Objects;

@Table(db = "shopdb", schema = "shopschema", name = "shop")
public class Shop implements Entity {
    @PK
    @Column(name = "shopCode", unique = true, nullable = false)
    private Integer shopCode;

    @Column(name = "shopName", nullable = false, length = 25)
    private String shopName;

    @Default
    @Column(name = "isOutlet", nullable = false)
    private Boolean isOutlet = true;

    @Column(name = "address", nullable = false, length = 25)
    private String address;

    @Column(name = "city", nullable = false, length = 50)
    private String city;

    @Default
    @Column(name = "isClosed", nullable = false)
    private Boolean isClosed = false;

    @Column(name = "area", nullable = false)
    private Double area;

    @Column(name = "countOfVisitorsToday", nullable = false)
    private Integer countOfVisitorsToday;

    public int getShopCode() {
        return shopCode;
    }

    public void setShopCode(int shopCode) {
        this.shopCode = shopCode;
    }

    public String getShopName() {
        return shopName;
    }

    public void setShopName(String shopName) {
        this.shopName = shopName;
    }

    public boolean isOutlet() {
        return isOutlet;
    }

    public void setOutlet(boolean outlet) {
        isOutlet = outlet;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public boolean isClosed() {
        return isClosed;
    }

    public void setClosed(boolean closed) {
        isClosed = closed;
    }

    public double getArea() {
        return area;
    }

    public void setArea(double area) {
        this.area = area;
    }

    public int getCountOfVisitorsToday() {
        return countOfVisitorsToday;
    }

    public void setCountOfVisitorsToday(int countOfVisitorsToday) {
        this.countOfVisitorsToday = countOfVisitorsToday;
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (this.getClass() != obj.getClass())
            return false;

        Shop other = (Shop) obj;
        return Objects.equals(this.shopCode, other.shopCode);
    }

    @Override
    public int hashCode() {
        return Objects.hash(shopCode);
    }

    @Override
    public String toString() {
        return "Shop { " + "shopcode: " + shopCode + ", shopName: " + shopName + ", isOutlet: " + isOutlet +
                ", address: " + address + ", city: " + city + ", isClosed: " + isClosed + ", area: " + area +
                ", countOfVisitorsToday: " + countOfVisitorsToday + " } ";
    }
}
