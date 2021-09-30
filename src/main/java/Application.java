import db.DBManager;
import db.entity.Order;
import db.entity.OrderProduct;
import db.entity.Product;
import db.entity.ProductsStatus;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.Scanner;

public class Application {
    public static void main(String[] args) {
        int userId = 1000 + (int) (Math.random() * 999);
        DBManager dbManager = DBManager.getInstance();
        Scanner in = new Scanner(System.in);
        System.out.println("This application allows you to keep records of products and create orders.");
        while (true) {
            System.out.println("\n* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *");
            System.out.println("*     1. Create product                                         *");
            System.out.println("*     2. Show the list of products                              *");
            System.out.println("*     3. Show the list of products ordered at least once        *");
            System.out.println("*     4. Create order                                           *");
            System.out.println("*     5. Show the list of orders                                *");
            System.out.println("*     6. Update products quantity in order                      *");
            System.out.println("*     7. Remove product by id                                   *");
            System.out.println("*     8. Remove all products                                    *");
            System.out.println("*     9. Exit                                                   *");
            System.out.println("* * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * * *\n");
            System.out.print("\tPlease choose one of the options: ");
            String selection = in.nextLine();
            switch (selection) {
                case "1":
                    System.out.println("Please enter product name: ");
                    String name = in.nextLine();
                    System.out.println("Please enter price: ");
                    int price = getPositiveInteger(in);
                    String status;
                    do {
                        System.out.println("Please choose a status (1, 2, 3): ");
                        System.out.println("1) out of stock\t\t2) in stock\t\t3) running low");
                        status = in.nextLine();
                    } while (!status.equals("1") && !status.equals("2") && !status.equals("3"));
                    if (status.equals("1"))
                        status = "out_of_stock";
                    else if (status.equals("2"))
                        status = "in_stock";
                    else
                        status = "running_low";
                    dbManager.insertProduct(Product.createProduct(name, price, ProductsStatus.fromString(status)));
                    System.out.println("Product was successfully created!");
                    break;
                case "2":
                    System.out.println();
                    dbManager.printAllProducts(dbManager.findAllProducts());
                    break;
                case "3":
                    System.out.println();
                    dbManager.printOrderedProducts();
                    break;
                case "4":
                    List<OrderProduct> products = new ArrayList<>();
                    while (true) {
                        addProduct:
                        {
                            System.out.println("Please enter a product id: ");
                            int productId = getPositiveInteger(in);
                            Product product = dbManager.getProduct(productId);
                            if (product == null) {
                                System.out.println("The product with given id was not found.");
                                continue;
                            }
                            for (OrderProduct op : products)
                                if (productId == op.getProductId()) {
                                    System.out.println("The product with given id has already been added to order.");
                                    break addProduct;
                                }
                            if (product.getStatus() == ProductsStatus.OUT_OF_STOCK) {
                                System.out.println("The product is out of stock.");
                                if (products.isEmpty())
                                    continue;
                                break addProduct;
                            }
                            System.out.println("Please enter a quantity: ");
                            int quantity = getPositiveInteger(in);
                            if (product.getStatus() == ProductsStatus.RUNNING_LOW && quantity > 10) {
                                while (quantity > 10) {
                                    System.out.println("The product is running low. Please enter less quantity: ");
                                    quantity = getPositiveInteger(in);
                                }
                            }
                            products.add(new OrderProduct(productId, quantity));
                        }
                        String choice;
                        do {
                            System.out.println("Do you want to add one more product? (Yes / No)");
                            choice = in.nextLine();
                        } while (!choice.equalsIgnoreCase("yes") && !choice.equalsIgnoreCase("no"));
                        if (choice.equalsIgnoreCase("yes"))
                            continue;
                        else {
                            do {
                                System.out.println("Do you want to save the order? (Yes / No)");
                                choice = in.nextLine();
                                if (choice.equalsIgnoreCase("yes")) {
                                    try {
                                        dbManager.createOrderWithProducts(Order.createOrder(userId, "new"), products);
                                        System.out.println("Order was successfully created!");
                                    } catch (SQLException throwables) {
                                        throwables.printStackTrace();
                                    }
                                }
                            } while (!choice.equalsIgnoreCase("yes") && !choice.equalsIgnoreCase("no"));
                        }
                        if (choice.equalsIgnoreCase("no") || choice.equalsIgnoreCase("yes"))
                            break;
                    }
                    break;
                case "5":
                    System.out.println();
                    dbManager.printOrders();
                    break;
                case "6":
                    System.out.println("Please enter order id: ");
                    int orderId = getPositiveInteger(in);
                    if (dbManager.getOrder(orderId) == null) {
                        System.out.println("The order with given id was not found!");
                        break;
                    }
                    System.out.println("Please enter product id: ");
                    int productId = getPositiveInteger(in);
                    if (!dbManager.isProductInOrder(orderId, productId)) {
                        System.out.println("The product with given id was not found in chosen order!");
                        break;
                    }
                    System.out.println("Enter new quantity: ");
                    int quantity = getPositiveInteger(in);
                    try {
                        dbManager.updateOrderQuantity(orderId, productId, quantity);
                    } catch (SQLException throwables) {
                        throwables.printStackTrace();
                    }
                    System.out.println("Order was successfully updated!");
                    break;
                case "7":
                    System.out.println("Enter product id: ");
                    int id = getPositiveInteger(in);
                    if (dbManager.getProduct(id) == null) {
                        System.out.println("The product with given id was not found!");
                        break;
                    }
                    dbManager.deleteProduct(id);
                    System.out.println("Product was deleted!");
                    break;
                case "8":
                    System.out.println("Please enter the password: ");
                    while (true) {
                        String input = in.nextLine();
                        if (input.equalsIgnoreCase("cancel"))
                            break;
                        try {
                            if (!dbManager.deleteAllProducts(input))
                                System.out.println("Entered password does not match! Try again or enter 'Cancel'");
                            else {
                                System.out.println("All products were removed");
                                break;
                            }
                        } catch (SQLException throwables) {
                            throwables.printStackTrace();
                        }
                    }
                    break;
                case "9":
                    in.close();
                    return;
                default:
                    System.out.println("\nPlease enter a number from 1 to 9!");
                    break;
            }
        }
    }

    public static int getPositiveInteger(Scanner in) {
        while (true) {
            try {
                int value = in.nextInt();
                if (value <= 0) {
                    System.out.println("The number must be greater than 0. Enter correct value: ");
                    continue;
                }
                in.nextLine();
                return value;
            } catch (InputMismatchException e) {
                System.out.println("Enter positive number: ");
                in.nextLine();
            }
        }
    }
}
