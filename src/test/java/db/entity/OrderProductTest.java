package db.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class OrderProductTest {
    @Test
    public void testClassMethods() {
        OrderProduct orderProduct1 = new OrderProduct();
        orderProduct1.setProductId(1);
        orderProduct1.setQuantity(5);
        OrderProduct orderProduct2 = new OrderProduct(1, 5);
        assertEquals(orderProduct1, orderProduct2);
        assertEquals(orderProduct1.hashCode(), orderProduct2.hashCode());
        assertEquals(orderProduct1.toString(), orderProduct2.toString());
        assertEquals(orderProduct1.getProductId() + orderProduct1.getQuantity(),
                orderProduct2.getProductId() + orderProduct2.getQuantity());
        orderProduct1.setQuantity(1);
        assertNotEquals(orderProduct1, orderProduct2);
    }
}
