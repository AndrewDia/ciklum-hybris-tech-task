package db;

import db.entity.Order;
import db.entity.OrderProduct;
import db.entity.Product;
import db.entity.ProductsStatus;

import java.io.FileInputStream;
import java.io.IOException;
import java.sql.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DBManager {
    private static DBManager dbManager;
    private static String connectionURL;

    private DBManager() {
        Properties properties = new Properties();
        try (FileInputStream inputStream = new FileInputStream("local.properties")) {
            properties.load(inputStream);
        } catch (IOException e) {
            e.printStackTrace();
        }
        connectionURL = properties.getProperty("connection.url");
    }

    public static synchronized DBManager getInstance() {
        if (dbManager == null)
            dbManager = new DBManager();
        return dbManager;
    }

    public Connection getConnection() throws SQLException {
        try {
            return DriverManager.getConnection(connectionURL);
        } catch (SQLException e) {
            e.printStackTrace();
            throw new SQLException();
        }
    }

    public Product insertProduct(Product product) {
        ResultSet resultSet = null;
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     SQLConstants.ADD_NEW_PRODUCT, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setString(1, product.getName());
            preparedStatement.setInt(2, product.getPrice());
            preparedStatement.setString(3, product.getStatus().getProductStatus());
            preparedStatement.setTimestamp(4, product.getCreatedAt());
            preparedStatement.executeUpdate();
            resultSet = preparedStatement.getGeneratedKeys();
            if (resultSet.next())
                product.setId(resultSet.getInt(1));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null)
                    resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return product;
    }

    public Product getProduct(int id) {
        Product product = null;
        ResultSet resultSet = null;
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     SQLConstants.FIND_PRODUCT_BY_ID, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next())
                product = mapProduct(resultSet);
            else
                return null;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null)
                    resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return product;
    }

    public List<Product> findAllProducts() {
        List<Product> products = new ArrayList<>();
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement();
             ResultSet resultSet = statement.executeQuery(SQLConstants.FIND_ALL_PRODUCTS)) {
            while (resultSet.next())
                products.add(mapProduct(resultSet));
        } catch (SQLException e) {
            e.printStackTrace();
        }
        return products;
    }

    public void printAllProducts(List<Product> products) {
        if (products.isEmpty()) {
            System.out.println("No products found.");
            return;
        }
        System.out.println("|      name       | price |    status    |");
        products.forEach(System.out::println);
    }

    public boolean isProductInOrder(int orderId, int productId) {
        ResultSet resultSet = null;
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     SQLConstants.FIND_ORDER_ENTRY, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setInt(1, orderId);
            preparedStatement.setInt(2, productId);
            resultSet = preparedStatement.executeQuery();
            return resultSet.next();
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null)
                    resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return false;
    }

    public int insertOrder(Connection connection, Order order) {
        ResultSet resultSet = null;
        try (PreparedStatement preparedStatement = connection.prepareStatement(
                SQLConstants.ADD_NEW_ORDER, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setInt(1, order.getUserId());
            preparedStatement.setString(2, order.getStatus());
            preparedStatement.setTimestamp(3, order.getCreatedAt());
            preparedStatement.executeUpdate();
            resultSet = preparedStatement.getGeneratedKeys();
            if (resultSet.next())
                order.setId(resultSet.getInt(1));
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null)
                    resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return order.getId();
    }

    public Order getOrder(int id) {
        Order order = null;
        ResultSet resultSet = null;
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(
                     SQLConstants.FIND_ORDER_BY_ID, Statement.RETURN_GENERATED_KEYS)) {
            preparedStatement.setInt(1, id);
            resultSet = preparedStatement.executeQuery();
            if (resultSet.next())
                order = mapOrder(resultSet);
            else
                return null;
        } catch (SQLException e) {
            e.printStackTrace();
        } finally {
            try {
                if (resultSet != null)
                    resultSet.close();
            } catch (SQLException e) {
                e.printStackTrace();
            }
        }
        return order;
    }

    public void printOrders() {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
             ResultSet resultSet = statement.executeQuery(SQLConstants.FIND_ORDER_ENTRIES)) {
            if (resultSet.next()) {
                resultSet.previous();
                System.out.printf("| %3s | %4s | %15s | %8s | %19s |%n", "id", "sum", "name", "quantity", "created at");
                while (resultSet.next())
                    System.out.printf("| %3s | %4s | %15s | %8s | %19s |%n",
                            resultSet.getInt(1),
                            resultSet.getInt(2),
                            resultSet.getString(3),
                            resultSet.getInt(4),
                            resultSet.getString(5));
            } else
                System.out.println("No orders found.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void printOrderedProducts() {
        try (Connection connection = getConnection();
             Statement statement = connection.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
             ResultSet resultSet = statement.executeQuery(SQLConstants.FIND_ORDERED_PRODUCTS)) {
            if (resultSet.next()) {
                resultSet.previous();
                System.out.printf("| %3s | %15s | %5s | %12s | %14s |%n", "id", "name", "price", "status", "total quantity");
                while (resultSet.next())
                    System.out.printf("| %3d | %15s | %5d | %12s | %14d |%n",
                            resultSet.getInt(1),
                            resultSet.getString(2),
                            resultSet.getInt(3),
                            resultSet.getString(4),
                            resultSet.getInt(5));
            } else
                System.out.println("There are no products that have been ordered at least once.");
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void createOrderWithProducts(Order order, List<OrderProduct> orderProducts) throws SQLException {
        Connection connection = null;
        try {
            connection = getConnection();
            connection.setAutoCommit(false);
            int orderId = insertOrder(connection, order);
            for (OrderProduct product : orderProducts)
                addProductForOrder(connection, orderId, product.getProductId(), product.getQuantity());
            connection.commit();
        } catch (SQLException e) {
            if (connection != null)
                connection.rollback();
            e.printStackTrace();
        } finally {
            if (connection != null)
                connection.close();
        }
    }


    public void addProductForOrder(Connection connection, int orderId, int productId, int quantity) {
        try (PreparedStatement preparedStatement = connection.prepareStatement(SQLConstants.ADD_NEW_ORDER_ENTRY)) {
            preparedStatement.setInt(1, orderId);
            preparedStatement.setInt(2, productId);
            preparedStatement.setInt(3, quantity);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public void deleteProduct(int productId) {
        try (Connection connection = getConnection();
             PreparedStatement preparedStatement = connection.prepareStatement(SQLConstants.DELETE_PRODUCT)) {
            preparedStatement.setInt(1, productId);
            preparedStatement.executeUpdate();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    public boolean deleteAllProducts(String userPassword) throws SQLException {
        String key = "password=";
        String password = connectionURL.substring(connectionURL.indexOf(key) + key.length());
        Connection connection;
        if (userPassword.equals(password)) {
            connection = getConnection();
            connection.setAutoCommit(false);
            try (Statement statement = connection.createStatement()) {
                statement.execute(SQLConstants.DELETE_ALL_PRODUCTS);
                statement.execute(SQLConstants.RESET_AUTO_INCREMENT_FOR_PRODUCTS_TABLE);
                connection.commit();
                return true;
            } catch (SQLException e) {
                connection.rollback();
                e.printStackTrace();
            } finally {
                connection.close();
            }
        }
        return false;
    }

    public void updateOrderQuantity(int orderId, int productId, int quantity) throws SQLException {
        Connection connection = null;
        try {
            connection = getConnection();
            connection.setAutoCommit(false);
            PreparedStatement preparedStatement = connection.prepareStatement(SQLConstants.UPDATE_ORDER_ENTRY_QUANTITY);
            preparedStatement.setInt(1, quantity);
            preparedStatement.setInt(2, orderId);
            preparedStatement.setInt(3, productId);
            preparedStatement = connection.prepareStatement(SQLConstants.UPDATE_ORDER_ENTRY_STATUS);
            preparedStatement.setInt(1, orderId);
            preparedStatement.executeUpdate();
            connection.commit();
        } catch (SQLException e) {
            if (connection != null)
                connection.rollback();
            e.printStackTrace();
        } finally {
            if (connection != null)
                connection.close();
        }
    }

    private Product mapProduct(ResultSet resultSet) throws SQLException {
        Product product = new Product();
        product.setId(resultSet.getInt(1));
        product.setName(resultSet.getString(2));
        product.setPrice(resultSet.getInt(3));
        product.setStatus(ProductsStatus.fromString(resultSet.getString(4)));
        product.setCreatedAt(resultSet.getTimestamp(5));
        return product;
    }

    private Order mapOrder(ResultSet resultSet) throws SQLException {
        Order order = new Order();
        order.setId(resultSet.getInt(1));
        order.setUserId(resultSet.getInt(2));
        order.setStatus(resultSet.getString(3));
        order.setCreatedAt(resultSet.getTimestamp(4));
        return order;
    }
}
