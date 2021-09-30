package db.entity;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.*;

class OrderTest {
    @Test
    public void testClassMethods() {
        Order order1 = Order.createOrder(1, "new");
        Order order2 = Order.createOrder(1, "new");
        Timestamp now = new Timestamp(System.currentTimeMillis());
        order1.setCreatedAt(now);
        order2.setCreatedAt(now);
        assertEquals(order1, order2);
        assertEquals(order1.hashCode(), order2.hashCode());
        assertEquals(order1.toString(), order2.toString());
        assertEquals(order1.getId() + order1.getUserId() + order1.getStatus() + order1.getCreatedAt(),
                order2.getId() + order2.getUserId() + order2.getStatus() + order2.getCreatedAt());
        order1.setId(2);
        order1.setUserId(2);
        order1.setStatus("edited");
        assertNotEquals(order1, order2);
    }
}