package db.entity;

import java.sql.Timestamp;
import java.util.Objects;

public class Order {
    private int id;
    private int userId;
    private String status;
    private Timestamp createdAt;

    public static Order createOrder(int userId, String status) {
        Order order = new Order();
        order.setUserId(userId);
        order.setStatus(status);
        order.setCreatedAt(new Timestamp(System.currentTimeMillis()));
        return order;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }

    public int getUserId() {
        return userId;
    }

    public void setUserId(int userId) {
        this.userId = userId;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public Timestamp getCreatedAt() {
        return createdAt;
    }

    public void setCreatedAt(Timestamp createdAt) {
        this.createdAt = createdAt;
    }

    @Override
    public String toString() {
        return "Order{" +
                "id=" + id +
                ", userId=" + userId +
                ", status='" + status + '\'' +
                ", createdAt=" + createdAt +
                '}';
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Order order = (Order) o;
        return id == order.id && userId == order.userId && Objects.equals(status, order.status);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, userId, status, createdAt);
    }
}
