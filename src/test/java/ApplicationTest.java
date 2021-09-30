import db.DBManager;
import db.SQLConstants;
import db.entity.ProductsStatus;
import org.junit.jupiter.api.*;

import java.io.*;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Properties;

import static org.junit.jupiter.api.Assertions.*;

public class ApplicationTest {
    private static DBManager dbManager;
    private static final String TEST_URL_CONNECTION = getDatabaseUrls()[1];
    private static final String URL_CONNECTION = getDatabaseUrls()[0];

    private static final InputStream originalIn = System.in;
    private static final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private static final PrintStream originalOut = System.out;
    private static final String EOL = System.lineSeparator();
    private static final String MENU = "\n* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *" + EOL +
            "*     1. Create product                                         *" + EOL +
            "*     2. Show the list of products                              *" + EOL +
            "*     3. Show the list of products ordered at least once        *" + EOL +
            "*     4. Create order                                           *" + EOL +
            "*     5. Show the list of orders                                *" + EOL +
            "*     6. Update products quantity in order                      *" + EOL +
            "*     7. Remove product by id                                   *" + EOL +
            "*     8. Remove all products                                    *" + EOL +
            "*     9. Exit                                                   *" + EOL +
            "* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\n" + EOL +
            "\tPlease choose one of the options: ";
    private static final String GREETING_MENU =
            "This application allows you to keep records of products and create orders." + EOL + MENU;

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

    @AfterEach
    public void clearSystemOut() {
        outContent.reset();
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
        /* restore streams */
        System.setIn(originalIn);
        System.setOut(originalOut);
    }

    @Test
    public void testProductCreationCorrectInput() {
        assertNull(dbManager.getProduct(1));
        System.setIn(new ByteArrayInputStream("1\nvase\n5\n2\n9\n".getBytes()));
        Application.main(new String[]{});
        String expected = "Please enter product name: " + EOL +
                "Please enter price: " + EOL +
                "Please choose a status (1, 2, 3): " + EOL +
                "1) out of stock\t\t2) in stock\t\t3) running low" + EOL +
                "Product was successfully created!";
        assertEquals(expected, clearMenuOutput(outContent.toString()));
        assertEquals(dbManager.getProduct(1).getStatus(), ProductsStatus.IN_STOCK);
    }

    @Test
    public void testProductCreationInvalidInput() {
        assertNull(dbManager.getProduct(1));
        System.setIn(new ByteArrayInputStream("1\nvase\n-2\nprice\n5\n12\n1)\n2\n9\n".getBytes()));
        Application.main(new String[]{});
        String expected = "Please enter product name: " + EOL +
                "Please enter price: " + EOL +
                "The number must be greater than 0. Enter correct value: " + EOL +
                "Enter positive number: " + EOL +
                "Please choose a status (1, 2, 3): " + EOL +
                "1) out of stock\t\t2) in stock\t\t3) running low" + EOL +
                "Please choose a status (1, 2, 3): " + EOL +
                "1) out of stock\t\t2) in stock\t\t3) running low" + EOL +
                "Please choose a status (1, 2, 3): " + EOL +
                "1) out of stock\t\t2) in stock\t\t3) running low" + EOL +
                "Product was successfully created!";
        assertEquals(expected, clearMenuOutput(outContent.toString()));
        assertEquals(dbManager.getProduct(1).getStatus(), ProductsStatus.IN_STOCK);
    }

    @Test
    public void testOrderCreationCorrectInput() {
        System.setIn(new ByteArrayInputStream("1\nvase\n5\n2\n4\n1\n15\nNo\nNo\n9\n".getBytes()));
        Application.main(new String[]{});
        String expected = "Please enter a product id: " + EOL +
                "Please enter a quantity: " + EOL +
                "Do you want to add one more product? (Yes / No)" + EOL +
                "Do you want to save the order? (Yes / No)";
        assertEquals(expected, clearMenuOutput(outContent.toString().substring(outContent.toString().indexOf("!") + 1)));
    }

    @Test
    public void testOrderCreationInvalidInput() {
        assertNull(dbManager.getOrder(1));
        String productsCreation = "1\nvase\n5\n3\n1\ncandle\n2\n1";
        System.setIn(new ByteArrayInputStream((productsCreation + "\n4\n2\n3\n1\n-5\n12\n8\nyes\n1\nnope\nno\nyep\nyes\n9\n").getBytes()));
        Application.main(new String[]{});
        String expected = "Please enter a product id: " + EOL +
                "The product is out of stock." + EOL +
                "Please enter a product id: " + EOL +
                "The product with given id was not found." + EOL +
                "Please enter a product id: " + EOL +
                "Please enter a quantity: " + EOL +
                "The number must be greater than 0. Enter correct value: " + EOL +
                "The product is running low. Please enter less quantity: " + EOL +
                "Do you want to add one more product? (Yes / No)" + EOL +
                "Please enter a product id: " + EOL +
                "The product with given id has already been added to order." + EOL +
                "Do you want to add one more product? (Yes / No)" + EOL +
                "Do you want to add one more product? (Yes / No)" + EOL +
                "Do you want to save the order? (Yes / No)" + EOL +
                "Do you want to save the order? (Yes / No)" + EOL +
                "Order was successfully created!";
        assertEquals(expected,
                clearMenuOutput(outContent.toString().substring(outContent.toString().indexOf("Please enter a product id"))));
        assertNotNull(dbManager.getOrder(1));
    }

    @Test
    public void testQuantityUpdateCorrectInput() {
        assertNull(dbManager.getOrder(1));
        String insertProductAndCreateOrder = "1\nvase\n5\n2\n4\n1\n15\nNo\nYes\n";
        System.setIn(new ByteArrayInputStream((insertProductAndCreateOrder + "6\n1\n1\n18\n9\n").getBytes()));
        Application.main(new String[]{});
        String expected = "Please enter order id: " + EOL +
                "Please enter product id: " + EOL +
                "Enter new quantity: " + EOL +
                "Order was successfully updated!";
        assertEquals(expected,
                clearMenuOutput(outContent.toString().substring(outContent.toString().indexOf("Please enter order id"))));
        assertEquals("edited", dbManager.getOrder(1).getStatus());
    }

    @Test
    public void testQuantityUpdateInvalidInput() {
        assertNull(dbManager.getOrder(1));
        String insertProductAndCreateOrder = "1\nvase\n5\n2\n4\n1\n15\nNo\nYes\n";
        System.setIn(new ByteArrayInputStream((insertProductAndCreateOrder + "6\n2\n6\n1\n4\n6\n1\n1\n-8\n-1\n18\n9\n").getBytes()));
        Application.main(new String[]{});
        String expected = "Please enter order id: " + EOL +
                "The order with given id was not found!" + EOL +
                "Please enter order id: " + EOL +
                "Please enter product id: " + EOL +
                "The product with given id was not found in chosen order!" + EOL +
                "Please enter order id: " + EOL +
                "Please enter product id: " + EOL +
                "Enter new quantity: " + EOL +
                "The number must be greater than 0. Enter correct value: " + EOL +
                "The number must be greater than 0. Enter correct value: " + EOL +
                "Order was successfully updated!";
        String actual = clearMenuOutput(outContent.toString()).replace(MENU, "").replace(MENU, "");
        assertEquals(expected, actual.substring(actual.indexOf("Please enter order id")));
        assertEquals("edited", dbManager.getOrder(1).getStatus());
    }

    @Test
    public void testProductRemoval() {
        System.setIn(new ByteArrayInputStream("1\nvase\n5\n2\n7\n1\n9\n".getBytes()));
        Application.main(new String[]{});
        String expected = "Enter product id: " + EOL +
                "Product was deleted!";
        assertEquals(expected, clearMenuOutput(outContent.toString().substring(outContent.toString().indexOf("Enter product id"))));
        assertNull(dbManager.getProduct(1));
    }

    @Test
    public void testProductRemovalIfProductDoesNotExist() {
        System.setIn(new ByteArrayInputStream("7\n1\n9\n".getBytes()));
        Application.main(new String[]{});
        String expected = "Enter product id: " + EOL +
                "The product with given id was not found!";
        assertEquals(expected, clearMenuOutput(outContent.toString()));
        assertNull(dbManager.getProduct(1));
    }

    @Test
    public void testAllProductsRemoval() {
        String productsCreation = "1\nvase\n5\n3\n1\ncandle\n2\n1";
        System.setIn(new ByteArrayInputStream((productsCreation + "\n8\n12345a\n9\n").getBytes()));
        Application.main(new String[]{});
        String expected = "Please enter the password: " + EOL +
                "All products were removed";
        assertEquals(expected,
                clearMenuOutput(outContent.toString().substring(outContent.toString().indexOf("Please enter the password"))));
        assertNull(dbManager.getProduct(1));
        assertNull(dbManager.getProduct(2));
    }

    @Test
    public void testAllProductsRemovalInvalidInput() {
        String productsCreation = "1\nvase\n5\n3\n1\ncandle\n2\n1";
        System.setIn(new ByteArrayInputStream((productsCreation + "\n8\n12345678\npassword\ncancel\n9\n").getBytes()));
        Application.main(new String[]{});
        String expected = "Please enter the password: " + EOL +
                "Entered password does not match! Try again or enter 'Cancel'" + EOL +
                "Entered password does not match! Try again or enter 'Cancel'";
        assertEquals(expected,
                clearMenuOutput(outContent.toString().substring(outContent.toString().indexOf("Please enter the password"))));
        assertNotNull(dbManager.getProduct(1));
        assertNotNull(dbManager.getProduct(2));
    }

    @Test
    public void testDefaultCase() {
        System.setIn(new ByteArrayInputStream("10\n9\n".getBytes()));
        Application.main(new String[]{});
        assertEquals("Please enter a number from 1 to 9!", clearMenuOutput(outContent.toString()));
    }

    private static String clearMenuOutput(String output) {
        String result = output.replace(GREETING_MENU, "");
        return result.replace(MENU, "").trim();
    }
}
