package db.entity;

import org.junit.jupiter.api.Test;

import java.sql.Timestamp;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class ProductTest {
    @Test
    public void testClassMethods() {
        Product vase = Product.createProduct("vase", 5, ProductsStatus.IN_STOCK);
        Product carpet = Product.createProduct("vase", 5, ProductsStatus.IN_STOCK);
        Timestamp now = new Timestamp(System.currentTimeMillis());
        vase.setCreatedAt(now);
        carpet.setCreatedAt(now);
        assertEquals(vase, carpet);
        assertEquals(vase.hashCode(), carpet.hashCode());
        assertEquals(vase.toString(), carpet.toString());
        assertEquals(vase.getId() + vase.getName() + vase.getPrice() + vase.getStatus() + vase.getCreatedAt(),
                carpet.getId() + carpet.getName() + carpet.getPrice() + carpet.getStatus() + carpet.getCreatedAt());
        vase.setId(2);
        vase.setName("candle");
        vase.setPrice(2);
        vase.setStatus(ProductsStatus.OUT_OF_STOCK);
        assertNotEquals(vase, carpet);
    }
}
