# Ciklum Hybris Technical Task

## Application description

This application allows you to keep records of products and display them in convenient format. Another function of this app is order creation based on the given products. The app is user-friendly. It provides hints in case of invalid input data and allows you to make step back in case of mistake.

### Build
- Java 8
- JDBC
- MySQL
- JUnit 5
- Maven

# How to start the application?

## Database preparation

The application will not work without a database. This app was implemented on MySQL Database server. Create a database and do not forget to update property 'connection.url' in [local.properties](local.properties) file with URL to your own database, username and password. You may find the database creation script in [db-create.sql](db-create.sql) file.

**Test Database preparation**

To run tests you need to create a new database and update property `test.connection.url` in [local.properties](local.properties) file with URL to your own database, username and password. Database creation [script](db-creation.sql) is the same. This is necessary so as not to damage the data of our main database, which our application works with. 

## Application running through command line

You need to have Maven downloaded on your computer.
```sh
git clone https://github.com/AndrewDia/ciklum-hybris-tech-task
cd ciklum-hybris-tech-task
mvn compile 
```

Enter
- `mvn exec:java` to run application;
- `mvn verify` to run application with unit tests;
- `mvn test` to run unit tests.

### How to open the project in Intellij IDEA?
Click `File -> New -> Project from Version Control...` and input `https://github.com/AndrewDia/ciklum-hybris-tech-task.git`. To run the application you should run [Application.java](src/main/java/Application.java) class.

In Intellij IDEA you might run all tests separately and simultaneously. To run a separate test choose a method marked with @Test annotation and run it. To run all tests at the same time right click on the `java` folder, which is located in `test` folder, on the panel with Project Structure and in the opened context menu choose option `Run 'All Tests'`. There you can also find the option `Run 'All Tests' with Coverage`.