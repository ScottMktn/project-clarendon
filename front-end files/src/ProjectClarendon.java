import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.ResultSetMetaData;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.Scanner;

/**
 * This class houses the main method that will run the front end Project Clarendon
 */
public class ProjectClarendon {
  private static Scanner scanner = new Scanner(System.in);
  private static Connection con;
  private static String url = "jdbc:mysql://localhost:3306/project_clarendon?serverTimezone=UTC";
  private static ArrayList<String> DBstocks = new ArrayList<>();

  /**
   * Main method that will handle user input as well as interact with backend SQL code.
   * @param args command line arguments
   */
  public static void main(String args[]) {
    try {
      validateCredentials();
      commandHandler();
    } catch (SQLException e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  /**
   * Handles input commands from system.in and delegates to the appropriate method.
   * If the command is invalid, prompts the user to enter in a valid command.
   */
  private static void commandHandler() throws SQLException {
    String cmd;
    System.out.println("\nPlease enter in a valid command " +
            "(CREATE, DELETE, READ, UPDATE, BUILD_REPORT, FIND_HIGH, FIND_LOW, QUIT): ");
    cmd = scanner.next();

    switch (cmd) {
      case "CREATE":
        createHandler();
        commandHandler();
        break;
      case "DELETE":
        deleteHandler();
        commandHandler();
        break;
      case "READ":
        readHandler();
        commandHandler();
        break;
      case "UPDATE":
        updateHandler();
        commandHandler();
        break;
      case "BUILD_REPORT":
        buildReport();
        commandHandler();
        break;
      case "FIND_HIGH":
        findHighHandler();
        commandHandler();
        break;
      case "FIND_LOW":
        findLowHandler();
        commandHandler();
        break;
      case "QUIT":
        con.close();
        validateClosedConnection();
        break;
      default: commandHandler();
    }
  }

  /**
   * Handler for finding the 52w high on an input stock.
   */
  private static void findHighHandler() throws SQLException {
    String stockTicker;
    System.out.println("\nPlease enter in a stock ticker (lowercase): ");
    stockTicker = scanner.next();

    initStocksInDatabase();

    if (!DBstocks.contains(stockTicker)) {
      System.out.println("Stock not found in the database. Please enter a valid stock ticker: ");
      findHighHandler();
    }

    String sql = "SELECT find_52w_high('" + stockTicker + "');";
    ResultSet rs = con.prepareStatement(sql).executeQuery();

    while(rs.next()) {
      System.out.println(rs.getString(1));
    }
  }

  /**
   * Handler for finding the 52 week low
   */

  private static void findLowHandler() throws SQLException {
    String stockTicker;
    System.out.println("\nPlease enter in a stock ticker (lowercase) for 52w low: ");
    stockTicker = scanner.next();

    initStocksInDatabase();

    if (!DBstocks.contains(stockTicker)) {
      System.out.println("Stock not found in the database. Please enter a valid stock ticker: ");
      findLowHandler();
    }

    String sql = "SELECT find_52w_low('" + stockTicker + "');";
    ResultSet rs = con.prepareStatement(sql).executeQuery();

    while(rs.next()) {
      System.out.println(rs.getString(1));
    }
  }


  /**
   * Checks to see if the connection is successfully closed.
   * @throws SQLException if the connection is unsuccessfully closed.
   */
  private static void validateClosedConnection() throws SQLException {
    if (con.isClosed()) {
      System.out.println("\nConnection closed successfully");
    }
    else {
      throw new IllegalArgumentException("Connection closed unsuccessfully");
    }
  }

  /**
   * Handler for the delete command for our database.
   * Note: As of now, our database only allows deletions on analyst ratings based on date.
   */
  private static void deleteHandler() throws SQLException {
    System.out.println("Please enter in the date where all analyst" +
            " ratings prior to the date are deleted (format: YYYY-MM-DD): ");
    String date = scanner.next();
    String sql = "DELETE FROM analyst_ratings WHERE rating_date < '" + date + "';";

    con.prepareStatement(sql).executeUpdate();
  }

  /**
   * Handler for the update command for our database.
   * NOTE: As of now, our database only allows updates on
   * company name due to mergers or acquisitions.
   */
  private static void updateHandler() throws SQLException {
    System.out.println("Please enter in the ticker name you want to update: ");
    String tickerName = scanner.next();
    System.out.println("Please enter in the new name you want to change to: ");
    String newName = scanner.next();

    String sql = "UPDATE company_info SET company_name = '" + newName + "' " +
            "WHERE ticker_name = '" + tickerName + "';";
    con.prepareStatement(sql).executeUpdate();
  }

  /**
   * Handler for the reading of objects of our database.
   */
  private static void readHandler() throws SQLException {
    String sql;
    String type;
    System.out.println("Please enter in BASIC to view an entire table, " +
            "TICKER for the avg rating of the company, " +
            "SECTOR for sector information on a sector, "
            + "or CUSTOM to input your own SQL script");
    type = scanner.next();
    switch (type) {
      case "BASIC":
        System.out.println("Please enter in a table to view: ");
        String table_name = scanner.next();
        sql = "SELECT * FROM " + table_name + ";";
        ResultSet rs = con.prepareStatement(sql).executeQuery();
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();

        StringBuilder header = new StringBuilder();
        for (int i = 1; i < columnCount + 1; i++) {
          if (i < columnCount) {
            header.append(metaData.getColumnLabel(i) + " | ");
          } else {
            header.append(metaData.getColumnLabel(i));
          }
        }
        System.out.println(header);

        while (rs.next()) {
          StringBuilder output = new StringBuilder();
          for (int i = 1; i < columnCount + 1; i++) {
            if (i < columnCount) {
              output.append(rs.getString(i) + " | ");
            } else {
              output.append(rs.getString(i));
            }
          }
          System.out.println(output);
        }
        break;
      case "TICKER":
        System.out.println("Please enter in the ticker you want to get info on: ");
        String ticker_name = scanner.next();
        sql = "SELECT average_rating FROM company_info WHERE ticker_name = '" + ticker_name + "';";

        ResultSet rsticker = con.prepareStatement(sql).executeQuery();

        while (rsticker.next()) {
          System.out.println(rsticker.getString(1));
        }
        break;
      case "SECTOR":
        System.out.println("Please enter in a sector name: ");
        String sector = scanner.next();
        sql = "SELECT * FROM sector_info WHERE sector_name ='" + sector + "';";
        ResultSet rssector = con.prepareStatement(sql).executeQuery();
        ResultSetMetaData metaDataSector = rssector.getMetaData();
        int columnCountSector = metaDataSector.getColumnCount();

        StringBuilder headerSector = new StringBuilder();
        for (int i = 1; i < columnCountSector + 1; i++) {
          if (i < columnCountSector) {
            headerSector.append(metaDataSector.getColumnLabel(i) + " | ");
          } else {
            headerSector.append(metaDataSector.getColumnLabel(i));
          }
        }
        System.out.println(headerSector);

        while (rssector.next()) {
          StringBuilder output = new StringBuilder();
          for (int i = 1; i < columnCountSector + 1; i++) {
            if (i < columnCountSector) {
              output.append(rssector.getString(i) + " | ");
            } else {
              output.append(rssector.getString(i));
            }
          }
          System.out.println(output);
        }
        break;
      case "CUSTOM":
        System.out.println("Please enter a custom SQL script");
        String custom = scanner.next();
        sql = custom;

        ResultSet rsCustom = con.prepareStatement(sql).executeQuery();
        ResultSetMetaData metaDataCustom = rsCustom.getMetaData();
        int columnCountCustom = metaDataCustom.getColumnCount();

        StringBuilder headerCustom = new StringBuilder();
        for (int i = 1; i < columnCountCustom + 1; i++) {
          if (i < columnCountCustom) {
            headerCustom.append(metaDataCustom.getColumnLabel(i) + " | ");
          } else {
            headerCustom.append(metaDataCustom.getColumnLabel(i));
          }
        }
        System.out.println(headerCustom);

        while (rsCustom.next()) {
          StringBuilder output = new StringBuilder();
          for (int i = 1; i < columnCountCustom + 1; i++) {
            if (i < columnCountCustom) {
              output.append(rsCustom.getString(i) + " | ");
            } else {
              output.append(rsCustom.getString(i));
            }
          }
          System.out.println(output);
        }
        break;
      default: readHandler();
    }
  }

  /**
   * Handler for the creation of objects into our database.
   * Note: not allowed to edit sector_info.
   */
  private static void createHandler() throws SQLException {
    String sql;
    String tableName;
    System.out.println("\nPlease enter in the table you want to create in: ");
    tableName = scanner.next();

    switch (tableName) {
      case "analyst_ratings":
        System.out.println("Please enter in the rating out of 5: ");
        int rating = Integer.parseInt(scanner.next());
        System.out.println("Please enter in the rating dating (format: YYYY-MM-DD): ");
        String YYYYMMDD = scanner.next();
        System.out.println("Please enter in the rating agency name: ");
        String agencyName = scanner.next();
        System.out.println("Please enter in the rating ticker name: ");
        String tickerName = scanner.next();

        sql = "INSERT INTO " + tableName +
                " (rating_out_of_5, rating_date, rating_agency, rating_ticker)" +
                " VALUES " + "(" + rating + ", '" + YYYYMMDD + "', '" + agencyName
                + "', '" + tickerName + "');";
        con.prepareStatement(sql).executeUpdate();
        break;
      case "company_info":
        System.out.println("Please enter in the ticker name: ");
        String tickerNameCom = scanner.next();
        System.out.println("Please enter in the company name ");
        String companyNameCom = scanner.next();
        System.out.println("Please enter in the company sector ");
        String sectorCom = scanner.next();

        sql = "INSERT INTO " + tableName +
                " (ticker_name, company_name, company_sector)" +
                " VALUES " + "('" + tickerNameCom + "', '" + companyNameCom + "', '" +
                sectorCom + "');";
        con.prepareStatement(sql).executeUpdate();
        break;
      case "price_data":
        System.out.println("Please enter in the date (YYYY-MM-DD): ");
        String YYYYMMDDPrice = scanner.next();
        System.out.println("Please enter in the open price ");
        double open = Integer.parseInt(scanner.next());
        System.out.println("Please enter in the high price ");
        double high = Integer.parseInt(scanner.next());
        System.out.println("Please enter in the low price: ");
        double low = Integer.parseInt(scanner.next());
        System.out.println("Please enter in the close price: ");
        double close = Integer.parseInt(scanner.next());
        System.out.println("Please enter in the ticker name: ");
        String tickerNamePrice = scanner.next();

        sql = "INSERT INTO " + tableName +
                " (data_date, data_open, data_high, data_low, data_close, data_ticker)" +
                " VALUES " + "('" + YYYYMMDDPrice + "', " + open + ", " + high +
                ", " + low + ", " + close + ", '" + tickerNamePrice + "');";
        con.prepareStatement(sql).executeUpdate();
        break;
      default: System.out.println("Table name does not exists. Enter in a valid table name: ");
        createHandler();
    }
  }

  /**
   * Prompts the user to enter in stock ticker and builds a basic financial report on the stock.
   */
  private static void buildReport() {
    String stockTicker;
    System.out.println("\nPlease enter in a stock ticker (lowercase): ");
    stockTicker = scanner.next();

    initStocksInDatabase();

    if (!DBstocks.contains(stockTicker)) {
      System.out.println("Stock not found in the database. Please enter a valid stock ticker: ");
      buildReport();
    }

    try {
      String sql = "{CALL build_basic_report('" + stockTicker + "')}";
      ResultSet rs = con.prepareStatement(sql).executeQuery();
      ResultSetMetaData metaData = rs.getMetaData();

      int columnCount = metaData.getColumnCount();

      System.out.println("\nOutput from trackCharacter function call on " + stockTicker + ":\n");

      StringBuilder header = new StringBuilder();
      for (int i = 1; i < columnCount + 1; i++) {
        if (i < columnCount) {
          header.append(metaData.getColumnLabel(i) + " | ");
        } else {
          header.append(metaData.getColumnLabel(i));
        }
      }
      System.out.println(header);

      while (rs.next()) {
        StringBuilder output = new StringBuilder();
        for (int i = 1; i < columnCount + 1; i++) {
          if (i < columnCount) {
            output.append(rs.getString(i) + " | ");
          } else {
            output.append(rs.getString(i));
          }
        }
        System.out.println(output);
      }
    } catch (SQLException e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  /**
   * Initializes the stocks inside of the database to allow error checking on the front-end side
   * of the application.
   */
  private static void initStocksInDatabase() {
    try {
      String sql = "SELECT ticker_name FROM company_info;";
      ResultSet rs = con.prepareStatement(sql).executeQuery();
      while (rs.next()) {
        DBstocks.add(rs.getString(1).toLowerCase());
      }
    } catch (SQLException e) {
      throw new IllegalArgumentException(e.getMessage());
    }
  }

  /**
   * Validates the username and password that the user inputs via system.in.
   */
  private static void validateCredentials() {
    String username;
    String password;

    System.out.println("Please enter in username: ");
    username = scanner.next();
    System.out.println("Please enter in password: ");
    password = scanner.next();
    System.out.println("\nConnecting to mySQL database...\n");

    try {
      con = DriverManager.getConnection(url, username, password);
      System.out.println("Successfully connected to mySQL");
      System.out.println("URL: " + url);
      System.out.println("Connection: " + con + "\n");
    } catch (SQLException e) {
      System.out.println("______________________________________\n");
      System.out.println("Invalid username and password. Please try again\n");
      validateCredentials();
    }
  }
}
