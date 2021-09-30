package db.entity;

import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;
import static org.junit.jupiter.api.Assertions.assertNull;

public class ProductsStatusTest {
    @Test
    public void testEnumMethods() {
        assertNotEquals(ProductsStatus.IN_STOCK.getProductStatus(), ProductsStatus.RUNNING_LOW.getProductStatus());
        assertEquals(ProductsStatus.OUT_OF_STOCK, ProductsStatus.fromString("out_of_stock"));
        assertNull(ProductsStatus.fromString("invalid value"));
    }
}
