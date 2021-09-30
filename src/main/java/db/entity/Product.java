package db.entity;

import java.sql.Timestamp;
import java.util.Objects;

public class Product {
    private int id;
    private String name;
    private int price;
    private ProductsStatus status;
    private Timestamp createdAt;

    public static Product createProduct(String name, int price, ProductsStatus status) {
        Product product = new Product();
        product.setName(name);
        product.setPrice(price);
        product.setStatus(status);
        product.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        return product;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getPrice() {
        return price;
    }

    public void setPrice(int price) {
        this.price = price;
    }

    public ProductsStatus getStatus() {
        return status;
    }

    public void setStatus(ProductsStatus status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp created_at) {
        this.createdAt = created_at;
    }

    @Override
    public String toString() {
        return String.format("| %15s | %5d | %12s |", name, price, status.getProductStatus());
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Product product = (Product) o;
        return id == product.id && price == product.price && Objects.equals(name, product.name) && status == product.status;
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, name, price, status, createdAt);
    }
}