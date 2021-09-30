package db;

public interface SQLConstants {
    String TABLE_PRODUCTS = "products";
    String TABLE_ORDERS = "orders";
    String TABLE_ORDER_ITEMS = "order_items";

    String ADD_NEW_PRODUCT = "INSERT INTO products (name, price, status, created_at) VALUES (?, ?, ?, ?)";
    String FIND_PRODUCT_BY_ID = "SELECT * FROM products WHERE id = ?";
    String FIND_ORDER_ENTRY = "SELECT * FROM order_items WHERE order_id = ? AND product_id = ?";
    String FIND_ALL_PRODUCTS = "SELECT * FROM " + TABLE_PRODUCTS;
    String ADD_NEW_ORDER = "INSERT INTO orders (user_id, status, created_at) VALUES (?, ?, ?)";
    String ADD_NEW_ORDER_ENTRY = "INSERT INTO order_items VALUES (?, ?, ?)";
    String FIND_ORDER_BY_ID = "SELECT * FROM orders WHERE id = ?";
    String FIND_ORDERED_PRODUCTS = "SELECT id, name, price, status, SUM(quantity) AS total_quantity " +
            "FROM products JOIN order_items ON id = order_items.product_id " +
            "GROUP BY id ORDER BY total_quantity DESC";
    String FIND_ORDER_ENTRIES = "SELECT order_id, quantity * products.price AS total_sum, " +
            "products.name, quantity, orders.created_at " +
            "FROM order_items " +
            "JOIN products ON product_id = products.id " +
            "JOIN orders ON order_id = orders.id " +
            "ORDER BY order_id, total_sum DESC";
    String UPDATE_ORDER_ENTRY_QUANTITY = "UPDATE order_items SET quantity = ? WHERE order_id = ? AND product_id = ?";
    String UPDATE_ORDER_ENTRY_STATUS = "UPDATE orders SET status = 'edited' WHERE id = ?";
    String DELETE_PRODUCT = "DELETE FROM products WHERE id = ?";
    String DELETE_ALL_PRODUCTS = "DELETE FROM " + TABLE_PRODUCTS;
    String RESET_AUTO_INCREMENT_FOR_PRODUCTS_TABLE = "ALTER TABLE " + TABLE_PRODUCTS + " AUTO_INCREMENT = 0";
    String DELETE_ALL_ORDERS = "DELETE FROM " + TABLE_ORDERS;
    String RESET_AUTO_INCREMENT_FOR_ORDERS_TABLE = "ALTER TABLE " + TABLE_ORDERS + " AUTO_INCREMENT = 0";
    String DELETE_ALL_ORDER_ENTRIES = "DELETE FROM " + TABLE_ORDER_ITEMS;

    String TEST_CREATE_TABLE_PRODUCTS = "CREATE TABLE IF NOT EXISTS `products` (" +
            "  `id` int UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY," +
            "  `name` varchar(20) NOT NULL," +
            "  `price` int UNSIGNED NOT NULL," +
            "  `status` enum('out_of_stock','in_stock','running_low') NOT NULL," +
            "  `created_at` datetime NOT NULL)";
    String TEST_CREATE_TABLE_ORDERS = "CREATE TABLE IF NOT EXISTS `orders` (" +
            "  `id` int UNSIGNED NOT NULL AUTO_INCREMENT PRIMARY KEY," +
            "  `user_id` int UNSIGNED NOT NULL," +
            "  `status` varchar(20) NOT NULL," +
            "  `created_at` datetime NOT NULL)";
    String TEST_CREATE_TABLE_ORDER_ITEMS = "CREATE TABLE IF NOT EXISTS `order_items` (" +
            " `order_id` int UNSIGNED NOT NULL," +
            " `product_id` int UNSIGNED NOT NULL," +
            " `quantity` int UNSIGNED NOT NULL," +
            " FOREIGN KEY (`order_id`) REFERENCES `orders`(`id`) ON DELETE CASCADE ON UPDATE CASCADE," +
            " FOREIGN KEY (`product_id`) REFERENCES `products`(`id`) ON DELETE CASCADE ON UPDATE CASCADE)";
}