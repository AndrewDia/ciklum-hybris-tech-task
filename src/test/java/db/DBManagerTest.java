package db;

import db.entity.Order;
import db.entity.OrderProduct;
import db.entity.Product;
import db.entity.ProductsStatus;
import org.junit.jupiter.api.*;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class DBManagerTest {
    private static DBManager dbManager;
    private static final String TEST_URL_CONNECTION = getDatabaseUrls()[1];
    private static final String URL_CONNECTION = getDatabaseUrls()[0];

    private static final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private static final PrintStream originalOut = System.out;
    private static final String EOL = System.lineSeparator();

    private static String[] getDatabaseUrls() {
        Properties properties = new Properties();
        try (FileInputStream inputStream = new FileInputStream("local.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return new String[]{properties.getProperty("connection.url"), properties.getProperty("test.connection.url")};
    }

    @BeforeAll
    public static void beforeTest() throws SQLException {
        try (OutputStream output = new FileOutputStream("local.properties")) {
            Properties properties = new Properties();
            properties.setProperty("connection.url", TEST_URL_CONNECTION);
            properties.store(output, null);
        } catch (IOException io) {
            io.printStackTrace();
        }

        dbManager = DBManager.getInstance();

        try (Connection con = DriverManager.getConnection(TEST_URL_CONNECTION);
             Statement statement = con.createStatement()) {
            statement.executeUpdate(SQLConstants.TEST_CREATE_TABLE_PRODUCTS);
            statement.executeUpdate(SQLConstants.TEST_CREATE_TABLE_ORDERS);
            statement.executeUpdate(SQLConstants.TEST_CREATE_TABLE_ORDER_ITEMS);
        }

        System.setOut(new PrintStream(outContent));
    }

    @BeforeEach
    public void clear() {
        try (Connection connection = dbManager.getConnection();
             Statement statement = connection.createStatement()) {
            statement.executeUpdate(SQLConstants.DELETE_ALL_ORDER_ENTRIES);
            statement.executeUpdate(SQLConstants.DELETE_ALL_PRODUCTS);
            statement.executeUpdate(SQLConstants.RESET_AUTO_INCREMENT_FOR_PRODUCTS_TABLE);
            statement.executeUpdate(SQLConstants.DELETE_ALL_ORDERS);
            statement.executeUpdate(SQLConstants.RESET_AUTO_INCREMENT_FOR_ORDERS_TABLE);
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    @AfterAll
    public static void afterTest() {
        try (OutputStream output = new FileOutputStream("local.properties")) {
            Properties properties = new Properties();
            properties.setProperty("connection.url", URL_CONNECTION);
            properties.setProperty("test.connection.url", TEST_URL_CONNECTION);
            properties.store(output, null);
        } catch (IOException io) {
            io.printStackTrace();
        }
        System.setOut(originalOut);
    }

    @AfterEach
    public void clearSystemOut() {
        outContent.reset();
    }

    @Test
    public void productTest() {
        Product vase = dbManager.insertProduct(Product.createProduct("vase", 5, ProductsStatus.IN_STOCK));
        Product carpet = dbManager.insertProduct(Product.createProduct("carpet", 50, ProductsStatus.OUT_OF_STOCK));
        Product cushion = dbManager.insertProduct(Product.createProduct("cushion", 20, ProductsStatus.RUNNING_LOW));
        List<Product> expected = new ArrayList<>(Arrays.asList(vase, carpet, cushion));
        assertEquals(vase, dbManager.getProduct(1));
        assertEquals(carpet, dbManager.getProduct(2));
        assertEquals(expected, dbManager.findAllProducts());
        dbManager.deleteProduct(3);
        assertNull(dbManager.getProduct(3));
        try {
            assertTrue(dbManager.deleteAllProducts("12345a"));
            assertFalse(dbManager.deleteAllProducts("wrong password"));
        } catch (SQLException throwables) {
            throwables.printStackTrace();
        }
    }

    @Test
    public void orderTest() throws SQLException {
        dbManager.insertProduct(Product.createProduct("vase", 5, ProductsStatus.IN_STOCK));
        dbManager.insertProduct(Product.createProduct("carpet", 50, ProductsStatus.OUT_OF_STOCK));
        dbManager.insertProduct(Product.createProduct("cushion", 20, ProductsStatus.RUNNING_LOW));
        List<OrderProduct> products = new ArrayList<>();
        products.add(new OrderProduct(1, 15));
        products.add(new OrderProduct(3, 9));
        dbManager.createOrderWithProducts(Order.createOrder(1111, "new"), products);
        assertTrue(dbManager.isProductInOrder(1, 1));
        assertFalse(dbManager.isProductInOrder(1, 2));
        dbManager.updateOrderQuantity(1, 1, 12);
        assertEquals("edited", dbManager.getOrder(1).getStatus());
    }

    @Test
    public void productsListOutputTest() {
        List<Product> productsList = new ArrayList<>();
        dbManager.printAllProducts(productsList);
        assertEquals("No products found.", outContent.toString().trim());
        Product vase = dbManager.insertProduct(Product.createProduct("vase", 5, ProductsStatus.IN_STOCK));
        Product carpet = dbManager.insertProduct(Product.createProduct("carpet", 50, ProductsStatus.OUT_OF_STOCK));
        Product cushion = dbManager.insertProduct(Product.createProduct("cushion", 20, ProductsStatus.RUNNING_LOW));
        productsList = new ArrayList<>(Arrays.asList(vase, carpet, cushion));
        dbManager.printAllProducts(productsList);
        String expected = "No products found." + EOL +
                "|      name       | price |    status    |" + EOL +
                "|            vase |     5 |     in_stock |" + EOL +
                "|          carpet |    50 | out_of_stock |" + EOL +
                "|         cushion |    20 |  running_low |";
        assertEquals(expected, outContent.toString().trim());
    }

    @Test
    public void ordersListOutputTest() throws SQLException {
        dbManager.printOrders();
        assertEquals("No orders found.", outContent.toString().trim());
        dbManager.insertProduct(Product.createProduct("vase", 5, ProductsStatus.IN_STOCK));
        dbManager.insertProduct(Product.createProduct("carpet", 50, ProductsStatus.OUT_OF_STOCK));
        dbManager.insertProduct(Product.createProduct("cushion", 20, ProductsStatus.RUNNING_LOW));
        Order firstOrder = Order.createOrder(1111, "new");
        List<OrderProduct> products = new ArrayList<>();
        products.add(new OrderProduct(1, 15));
        products.add(new OrderProduct(3, 9));
        dbManager.createOrderWithProducts(firstOrder, products);
        Order secondOrder = Order.createOrder(1112, "new");
        products = new ArrayList<>();
        products.add(new OrderProduct(1, 5));
        dbManager.createOrderWithProducts(secondOrder, products);
        dbManager.printOrders();
        String firstOrderCreatedAt = dbManager.getOrder(1).getCreatedAt().toString().substring(0, 19);
        String secondOrderCreatedAt = dbManager.getOrder(2).getCreatedAt().toString().substring(0, 19);
        String expected = "No orders found." + EOL +
                "|  id |  sum |            name | quantity |          created at |" + EOL +
                "|   1 |  180 |         cushion |        9 | " + firstOrderCreatedAt + " |" + EOL +
                "|   1 |   75 |            vase |       15 | " + firstOrderCreatedAt + " |" + EOL +
                "|   2 |   25 |            vase |        5 | " + secondOrderCreatedAt + " |";
        assertEquals(expected, outContent.toString().trim());
    }

    @Test
    public void orderedProductsOutputTest() throws SQLException {
        dbManager.printOrderedProducts();
        assertEquals("There are no products that have been ordered at least once.", outContent.toString().trim());
        dbManager.insertProduct(Product.createProduct("vase", 5, ProductsStatus.IN_STOCK));
        dbManager.insertProduct(Product.createProduct("carpet", 50, ProductsStatus.OUT_OF_STOCK));
        dbManager.insertProduct(Product.createProduct("cushion", 20, ProductsStatus.RUNNING_LOW));
        Order firstOrder = Order.createOrder(1111, "new");
        List<OrderProduct> products = new ArrayList<>();
        products.add(new OrderProduct(1, 15));
        products.add(new OrderProduct(3, 9));
        dbManager.createOrderWithProducts(firstOrder, products);
        Order secondOrder = Order.createOrder(1112, "new");
        products = new ArrayList<>();
        products.add(new OrderProduct(1, 5));
        dbManager.createOrderWithProducts(secondOrder, products);
        dbManager.printOrderedProducts();
        String expected = "There are no products that have been ordered at least once." + EOL +
                "|  id |            name | price |       status | total quantity |" + EOL +
                "|   1 |            vase |     5 |     in_stock |             20 |" + EOL +
                "|   3 |         cushion |    20 |  running_low |              9 |";
        assertEquals(expected, outContent.toString().trim());
    }
}
