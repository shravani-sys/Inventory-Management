package project;

import javax.swing.*; 
import javax.swing.table.DefaultTableModel;
import java.awt.*; 
import java.sql.*;

public class MainApplication extends JFrame {

    private Connection conn;

    public MainApplication() {
        setTitle("Inventory Management System");
        setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        setSize(800, 600);
        setLayout(new BorderLayout());
    
        // Connect to the database
        try {
            Class.forName("com.mysql.cj.jdbc.Driver");
            conn = DriverManager.getConnection("jdbc:mysql://localhost:3306/inventory", "root", "root");
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "MySQL JDBC driver not found!");
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error connecting to database: " + e.getMessage());
        }
    
        // Inventory Panel
        JPanel inventoryPanel = createTablePanel("products", "Products");
        JButton addInventoryButton = new JButton("Add Product");
        JButton editInventoryButton = new JButton("Edit Product");
        JButton deleteInventoryButton = new JButton("Delete Product");
        addCrudOperations(inventoryPanel, addInventoryButton, editInventoryButton, deleteInventoryButton, "products");
    
        // Users Panel
        JPanel usersPanel = createTablePanel("users", "users");
        JButton addUserButton = new JButton("Add User");
        JButton editUserButton = new JButton("Edit User");
        JButton deleteUserButton = new JButton("Delete User");
        addCrudOperationsUsers(usersPanel, addUserButton, editUserButton, deleteUserButton, "users");
        
        // supplier Panel
        JPanel supplierPanel = createTablePanel("suppliers", "suppliers");
        JButton addsupplierButton = new JButton("Add suppliers");
        JButton editsupplierButton = new JButton("Edit suppliers");
        JButton deletesupplierButton = new JButton("Delete suppliers");
        addCrudOperationsSupplier(supplierPanel, addsupplierButton, editsupplierButton, deletesupplierButton, "suppliers");
    
        // purchaseinfo Panel
        JPanel purchasePanel = createTablePanel("purchaseinfo", "purchase info");
        JButton addpurchaseButton = new JButton("Add purchase");
        JButton editpurchaseButton = new JButton("Edit purchase");
        JButton deletepurchaseButton = new JButton("Delete purchase");
        addCrudOperationsPurchase(purchasePanel, addpurchaseButton, editpurchaseButton, deletepurchaseButton, "purchaseinfo");
    
        // Add panels to the main frame
        JPanel mainPanel = new JPanel(new GridLayout(2, 2));
        mainPanel.add(inventoryPanel);
        mainPanel.add(usersPanel);
        mainPanel.add(supplierPanel);
        mainPanel.add(purchasePanel);
        add(mainPanel, BorderLayout.CENTER);
    
        setVisible(true);
    }
    
    private JPanel createTablePanel(String tableName, String panelTitle) {
        JPanel panel = new JPanel(new BorderLayout());
        panel.setBorder(BorderFactory.createTitledBorder(panelTitle));
        JTable table = new JTable(buildTableModel(tableName));
        JScrollPane scrollPane = new JScrollPane(table);
        panel.add(scrollPane, BorderLayout.CENTER);
        return panel;
    }
    
    private DefaultTableModel buildTableModel(String tableName) {
        try {
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName);
    
            // Reset ResultSet cursor
            rs.beforeFirst();
    
            // Return DefaultTableModel
            return new DefaultTableModel(buildRowData(rs), buildColumnNames(rs));
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error retrieving data from " + tableName + " table: " + e.getMessage());
            return new DefaultTableModel();
        }
    }
    
    
    
    private Object[][] buildRowData(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        int rowCount = getRowCount(rs);
        Object[][] data = new Object[rowCount][columnCount];
        int rowIndex = 0;
        while (rs.next()) {
            for (int i = 0; i < columnCount; i++) {
                data[rowIndex][i] = rs.getObject(i + 1);
            }
            rowIndex++;
        }
        return data;
    }
    
    private String[] buildColumnNames(ResultSet rs) throws SQLException {
        ResultSetMetaData metaData = rs.getMetaData();
        int columnCount = metaData.getColumnCount();
        String[] columnNames = new String[columnCount];
        for (int i = 0; i < columnCount; i++) {
            columnNames[i] = metaData.getColumnName(i + 1);
        }
        return columnNames;
    }
    
    private int getRowCount(ResultSet rs) throws SQLException {
        int rowCount = 0;
        while (rs.next()) {
            rowCount++;
        }
        rs.beforeFirst(); // Reset ResultSet cursor
        return rowCount;
    }
    
    private void addCrudOperations(JPanel panel, JButton addButton, JButton editButton, JButton deleteButton, String tableName) {
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
    
        addButton.addActionListener(e -> openAddProductFrame(panel, tableName));
        editButton.addActionListener(e -> openEditProductFrame(panel, tableName));
        deleteButton.addActionListener(e -> deleteProduct(panel, tableName));
    }
    
    private void openAddProductFrame(JPanel panel, String tableName) {
        JFrame addProductFrame = new JFrame("Add Product");
        addProductFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addProductFrame.setSize(400, 300);
        addProductFrame.setLayout(new GridLayout(6, 2));
    
        JTextField productCodeField = new JTextField();
        JTextField productNameField = new JTextField();
        JTextField costPriceField = new JTextField();
        JTextField sellingPriceField = new JTextField();
        JTextField brandField = new JTextField();
    
        addProductFrame.add(new JLabel("Product Code:"));
        addProductFrame.add(productCodeField);
        addProductFrame.add(new JLabel("Product Name:"));
        addProductFrame.add(productNameField);
        addProductFrame.add(new JLabel("Cost Price:"));
        addProductFrame.add(costPriceField);
        addProductFrame.add(new JLabel("Selling Price:"));
        addProductFrame.add(sellingPriceField);
        addProductFrame.add(new JLabel("Brand:"));
        addProductFrame.add(brandField);
    
        JButton addProductButton = new JButton("Add Product");
        addProductFrame.add(addProductButton);
    
        addProductButton.addActionListener(addEvent -> {
            String productCode = productCodeField.getText();
            String productName = productNameField.getText();
            double costPrice = Double.parseDouble(costPriceField.getText());
            double sellingPrice = Double.parseDouble(sellingPriceField.getText());
            String brand = brandField.getText();
    
            try {
                String query = "INSERT INTO " + tableName + " (productcode, productname, costprice, sellprice, brand) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, productCode);
                pstmt.setString(2, productName);
                pstmt.setDouble(3, costPrice);
                pstmt.setDouble(4, sellingPrice);
                pstmt.setString(5, brand);
                pstmt.executeUpdate();
                pstmt.close();
    
                refreshTable(panel, tableName);
                addProductFrame.dispose();
                JOptionPane.showMessageDialog(this, "Product added successfully");
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error adding product: " + ex.getMessage());
            }
        });
    
        addProductFrame.setVisible(true);
    }
    
    private void openEditProductFrame(JPanel panel, String tableName) {
        JFrame editProductFrame = new JFrame("Edit Product");
        editProductFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        editProductFrame.setSize(400, 300);
        editProductFrame.setLayout(new GridLayout(7, 2));
    
        JTable table = (JTable) ((JScrollPane) panel.getComponent(0)).getViewport().getView();
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to edit.");
            return;
        }
    
        String productCode = table.getValueAt(selectedRow, 1).toString();
        String productName = table.getValueAt(selectedRow, 2).toString();
        String costPrice = table.getValueAt(selectedRow, 3).toString();
        String sellingPrice = table.getValueAt(selectedRow, 4).toString();
        String brand = table.getValueAt(selectedRow, 5).toString();
        String pid = table.getValueAt(selectedRow, 0).toString();
    
        JTextField productCodeField = new JTextField(productCode);
        JTextField productNameField = new JTextField(productName);
        JTextField costPriceField = new JTextField(costPrice);
        JTextField sellingPriceField = new JTextField(sellingPrice);
        JTextField brandField = new JTextField(brand);
    
        editProductFrame.add(new JLabel("Product Code:"));
        editProductFrame.add(productCodeField);
        editProductFrame.add(new JLabel("Product Name:"));
        editProductFrame.add(productNameField);
        editProductFrame.add(new JLabel("Cost Price:"));
        editProductFrame.add(costPriceField);
        editProductFrame.add(new JLabel("Selling Price:"));
        editProductFrame.add(sellingPriceField);
        editProductFrame.add(new JLabel("Brand:"));
        editProductFrame.add(brandField);
    
        JButton updateProductButton = new JButton("Update Product");
        editProductFrame.add(updateProductButton);
    
        updateProductButton.addActionListener(updateEvent -> {
            String updatedProductCode = productCodeField.getText();
            String updatedProductName = productNameField.getText();
            double updatedCostPrice = Double.parseDouble(costPriceField.getText());
            double updatedSellingPrice = Double.parseDouble(sellingPriceField.getText());
            String updatedBrand = brandField.getText();
    
            try {
                String query = "UPDATE " + tableName + " SET productname = ?, costprice = ?, sellprice = ?, brand = ? , productCode = ?WHERE pid = ?";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setString(1, updatedProductName);
        pstmt.setDouble(2, updatedCostPrice);
        pstmt.setDouble(3, updatedSellingPrice);
        pstmt.setString(4, updatedBrand);
        pstmt.setString(5, updatedProductCode);
        pstmt.setString(6, pid);
        int updatedRows = pstmt.executeUpdate();
        if (updatedRows > 0) {
            JOptionPane.showMessageDialog(this, "Product updated successfully");
            refreshTable(panel, tableName);
        } else {
            JOptionPane.showMessageDialog(this, "No product found with the provided code");
        }
                editProductFrame.dispose();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error updating product: " + ex.getMessage());
            }
        });
    
        editProductFrame.setVisible(true);
    }
    
    private void deleteProduct(JPanel panel, String tableName) {
        JTable table = (JTable) ((JScrollPane) panel.getComponent(0)).getViewport().getView();
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a product to delete.");
            return;
        }
    
        String productCode = table.getValueAt(selectedRow, 0).toString();
    
        try {
            String query = "DELETE FROM " + tableName + " WHERE pid = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, productCode);
            int deletedRows = pstmt.executeUpdate();
            if (deletedRows > 0) {
                JOptionPane.showMessageDialog(this, "Product deleted successfully");
                refreshTable(panel, tableName);
            } else {
                JOptionPane.showMessageDialog(this, "No product found with the provided code");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting product: " + ex.getMessage());
        }
    }
    
    
    private void addCrudOperationsUsers(JPanel panel, JButton addButton, JButton editButton, JButton deleteButton, String tableName) {
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
            
        addButton.addActionListener(e -> openAddUserFrame(panel, tableName));
        editButton.addActionListener(e -> openEditUserFrame(panel, tableName));
        deleteButton.addActionListener(e -> deleteUser(panel, tableName));
    }
    
    
    private void openAddUserFrame(JPanel panel, String tableName) {
        JFrame addUserFrame = new JFrame("Add User");
        addUserFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addUserFrame.setSize(400, 300);
        addUserFrame.setLayout(new GridLayout(7, 2));
    
        JTextField nameField = new JTextField();
        JTextField locationField = new JTextField();
        JTextField phoneField = new JTextField();
        JTextField usernameField = new JTextField();
        JPasswordField passwordField = new JPasswordField();
        JTextField userTypeField = new JTextField();
    
        addUserFrame.add(new JLabel("Name:"));
        addUserFrame.add(nameField);
        addUserFrame.add(new JLabel("Location:"));
        addUserFrame.add(locationField);
        addUserFrame.add(new JLabel("Phone:"));
        addUserFrame.add(phoneField);
        addUserFrame.add(new JLabel("Username:"));
        addUserFrame.add(usernameField);
        addUserFrame.add(new JLabel("Password:"));
        addUserFrame.add(passwordField);
        addUserFrame.add(new JLabel("User Type:"));
        addUserFrame.add(userTypeField);
    
        JButton addUserButton = new JButton("Add User");
        addUserFrame.add(addUserButton);
    
        addUserButton.addActionListener(addEvent -> {
            String name = nameField.getText();
            String location = locationField.getText();
            String phone = phoneField.getText();
            String username = usernameField.getText();
            String password = new String(passwordField.getPassword());
            String userType = userTypeField.getText();
    
            try {
                String query = "INSERT INTO " + tableName + " (name, location, phone, username, password, usertype) VALUES (?, ?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, name);
                pstmt.setString(2, location);
                pstmt.setString(3, phone);
                pstmt.setString(4, username);
                pstmt.setString(5, password);
                pstmt.setString(6, userType);
                pstmt.executeUpdate();
                pstmt.close();
    
                refreshTable(panel, tableName);
                addUserFrame.dispose();
                JOptionPane.showMessageDialog(this, "User added successfully");
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error adding user: " + ex.getMessage());
            }
        });
    
        addUserFrame.setVisible(true);
    }
    
    private void openEditUserFrame(JPanel panel, String tableName) {
        JFrame editUserFrame = new JFrame("Edit User");
        editUserFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        editUserFrame.setSize(400, 300);
        editUserFrame.setLayout(new GridLayout(8, 2));
    
        JTable table = (JTable) ((JScrollPane) panel.getComponent(0)).getViewport().getView();
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to edit.");
            return;
        }
    
        String name = table.getValueAt(selectedRow, 1).toString();
        String location = table.getValueAt(selectedRow, 2).toString();
        String phone = table.getValueAt(selectedRow, 3).toString();
        String username = table.getValueAt(selectedRow, 4).toString();
        String password = table.getValueAt(selectedRow, 5).toString();
        String userType = table.getValueAt(selectedRow, 6).toString();
        String id = table.getValueAt(selectedRow, 0).toString();
    
        JTextField nameField = new JTextField(name);
        JTextField locationField = new JTextField(location);
        JTextField phoneField = new JTextField(phone);
        JTextField usernameField = new JTextField(username);
        JPasswordField passwordField = new JPasswordField(password);
        JTextField userTypeField = new JTextField(userType);
    
        editUserFrame.add(new JLabel("Name:"));
        editUserFrame.add(nameField);
        editUserFrame.add(new JLabel("Location:"));
        editUserFrame.add(locationField);
        editUserFrame.add(new JLabel("Phone:"));
        editUserFrame.add(phoneField);
        editUserFrame.add(new JLabel("Username:"));
        editUserFrame.add(usernameField);
        editUserFrame.add(new JLabel("Password:"));
        editUserFrame.add(passwordField);
        editUserFrame.add(new JLabel("User Type:"));
        editUserFrame.add(userTypeField);
    
        JButton updateUserButton = new JButton("Update User");
        editUserFrame.add(updateUserButton);
    
        updateUserButton.addActionListener(updateEvent -> {
            String updatedName = nameField.getText();
            String updatedLocation = locationField.getText();
            String updatedPhone = phoneField.getText();
            String updatedUsername = usernameField.getText();
            String updatedPassword = new String(passwordField.getPassword());
            String updatedUserType = userTypeField.getText();
    
            try {
                String query = "UPDATE " + tableName + " SET name = ?, location = ?, phone = ?, username = ?, password = ?, usertype = ? WHERE id = ?";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setString(1, updatedName);
        pstmt.setString(2, updatedLocation);
        pstmt.setString(3, updatedPhone);
        pstmt.setString(4, updatedUsername);
        pstmt.setString(5, updatedPassword);
        pstmt.setString(6, updatedUserType);
        pstmt.setString(7, id);
        int updatedRows = pstmt.executeUpdate();
        if (updatedRows > 0) {
            JOptionPane.showMessageDialog(this, "User updated successfully");
            refreshTable(panel, tableName);
        } else {
            JOptionPane.showMessageDialog(this, "No user found with the provided ID");
        }
                editUserFrame.dispose();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error updating user: " + ex.getMessage());
            }
        });
    
        editUserFrame.setVisible(true);
    }
    
    private void deleteUser(JPanel panel, String tableName) {
        JTable table = (JTable) ((JScrollPane) panel.getComponent(0)).getViewport().getView();
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a user to delete.");
            return;
        }
    
        String id = table.getValueAt(selectedRow, 0).toString();
    
        try {
            String query = "DELETE FROM " + tableName + " WHERE id = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, id);
            int deletedRows = pstmt.executeUpdate();
            if (deletedRows > 0) {
                JOptionPane.showMessageDialog(this, "User deleted successfully");
                refreshTable(panel, tableName);
            } else {
                JOptionPane.showMessageDialog(this, "No user found with the provided ID");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting user: " + ex.getMessage());
        }
    }
    
    
    private void addCrudOperationsSupplier(JPanel panel, JButton addButton, JButton editButton, JButton deleteButton, String tableName) {
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
    
        addButton.addActionListener(e -> openAddSupplierFrame(panel, tableName));
        editButton.addActionListener(e -> openEditSupplierFrame(panel, tableName));
        deleteButton.addActionListener(e -> deleteSupplier(panel, tableName));
    }
    
    private void openAddSupplierFrame(JPanel panel, String tableName) {
        JFrame addSupplierFrame = new JFrame("Add Supplier");
        addSupplierFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addSupplierFrame.setSize(400, 300);
        addSupplierFrame.setLayout(new GridLayout(5, 2));
    
        JTextField supplierCodeField = new JTextField();
        JTextField fullNameField = new JTextField();
        JTextField locationField = new JTextField();
        JTextField mobileField = new JTextField();
    
        addSupplierFrame.add(new JLabel("Supplier Code:"));
        addSupplierFrame.add(supplierCodeField);
        addSupplierFrame.add(new JLabel("Full Name:"));
        addSupplierFrame.add(fullNameField);
        addSupplierFrame.add(new JLabel("Location:"));
        addSupplierFrame.add(locationField);
        addSupplierFrame.add(new JLabel("Mobile:"));
        addSupplierFrame.add(mobileField);
    
        JButton addSupplierButton = new JButton("Add Supplier");
        addSupplierFrame.add(addSupplierButton);
    
        addSupplierButton.addActionListener(addEvent -> {
            String supplierCode = supplierCodeField.getText();
            String fullName = fullNameField.getText();
            String location = locationField.getText();
            String mobile = mobileField.getText();
    
            try {
                String query = "INSERT INTO " + tableName + " (suppliercode, fullname, location, mobile) VALUES (?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, supplierCode);
                pstmt.setString(2, fullName);
                pstmt.setString(3, location);
                pstmt.setString(4, mobile);
                pstmt.executeUpdate();
                pstmt.close();
    
                refreshTable(panel, tableName);
                addSupplierFrame.dispose();
                JOptionPane.showMessageDialog(this, "Supplier added successfully");
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error adding supplier: " + ex.getMessage());
            }
        });
    
        addSupplierFrame.setVisible(true);
    }
    
    private void openEditSupplierFrame(JPanel panel, String tableName) {
        JFrame editSupplierFrame = new JFrame("Edit Supplier");
        editSupplierFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        editSupplierFrame.setSize(400, 300);
        editSupplierFrame.setLayout(new GridLayout(6, 2));
    
        JTable table = (JTable) ((JScrollPane) panel.getComponent(0)).getViewport().getView();
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a supplier to edit.");
            return;
        }
    
        String supplierCode = table.getValueAt(selectedRow, 1).toString();
        String fullName = table.getValueAt(selectedRow, 2).toString();
        String location = table.getValueAt(selectedRow, 3).toString();
        String mobile = table.getValueAt(selectedRow, 4).toString();
        String sid = table.getValueAt(selectedRow, 0).toString();
    
        JTextField supplierCodeField = new JTextField(supplierCode);
        JTextField fullNameField = new JTextField(fullName);
        JTextField locationField = new JTextField(location);
        JTextField mobileField = new JTextField(mobile);
    
        editSupplierFrame.add(new JLabel("Supplier Code:"));
        editSupplierFrame.add(supplierCodeField);
        editSupplierFrame.add(new JLabel("Full Name:"));
        editSupplierFrame.add(fullNameField);
        editSupplierFrame.add(new JLabel("Location:"));
        editSupplierFrame.add(locationField);
        editSupplierFrame.add(new JLabel("Mobile:"));
        editSupplierFrame.add(mobileField);
    
        JButton updateSupplierButton = new JButton("Update Supplier");
        editSupplierFrame.add(updateSupplierButton);
    
        updateSupplierButton.addActionListener(updateEvent -> {
            String updatedSupplierCode = supplierCodeField.getText();
            String updatedFullName = fullNameField.getText();
            String updatedLocation = locationField.getText();
            String updatedMobile = mobileField.getText();
    
            try {
                String query = "UPDATE " + tableName + " SET fullname = ?, location = ?, mobile = ?, suppliercode = ? WHERE sid = ?";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setString(1, updatedFullName);
        pstmt.setString(2, updatedLocation);
        pstmt.setString(3, updatedMobile);
        pstmt.setString(4, updatedSupplierCode);
        pstmt.setString(5, sid);
        int updatedRows = pstmt.executeUpdate();
        if (updatedRows > 0) {
            JOptionPane.showMessageDialog(this, "Supplier updated successfully");
            refreshTable(panel, tableName);
        } else {
            JOptionPane.showMessageDialog(this, "No supplier found with the provided code");
        }
                editSupplierFrame.dispose();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error updating supplier: " + ex.getMessage());
            }
        });
    
        editSupplierFrame.setVisible(true);
    }
    
    private void deleteSupplier(JPanel panel, String tableName) {
        JTable table = (JTable) ((JScrollPane) panel.getComponent(0)).getViewport().getView();
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a supplier to delete.");
            return;
        }
    
        String supplierCode = table.getValueAt(selectedRow, 0).toString();
    
        try {
            String query = "DELETE FROM " + tableName + " WHERE sid = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, supplierCode);
            int deletedRows = pstmt.executeUpdate();
            if (deletedRows > 0) {
                JOptionPane.showMessageDialog(this, "Supplier deleted successfully");
                refreshTable(panel, tableName);
            } else {
                JOptionPane.showMessageDialog(this, "No supplier found with the provided code");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting supplier: " + ex.getMessage());
        }
    }
    

    
    private void addCrudOperationsPurchase(JPanel panel, JButton addButton, JButton editButton, JButton deleteButton, String tableName) {
        JPanel buttonPanel = new JPanel(new FlowLayout());
        buttonPanel.add(addButton);
        buttonPanel.add(editButton);
        buttonPanel.add(deleteButton);
        panel.add(buttonPanel, BorderLayout.SOUTH);
    
        addButton.addActionListener(e -> openAddPurchaseFrame(panel, tableName));
        editButton.addActionListener(e -> openEditPurchaseFrame(panel, tableName));
        deleteButton.addActionListener(e -> deletePurchase(panel, tableName));
    }
    
    private void openAddPurchaseFrame(JPanel panel, String tableName) {
        JFrame addPurchaseFrame = new JFrame("Add Purchase");
        addPurchaseFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        addPurchaseFrame.setSize(400, 300);
        addPurchaseFrame.setLayout(new GridLayout(6, 2));
    
        JTextField supplierCodeField = new JTextField();
        JTextField productCodeField = new JTextField();
        JTextField dateField = new JTextField();
        JTextField quantityField = new JTextField();
        JTextField totalCostField = new JTextField();
    
        addPurchaseFrame.add(new JLabel("Supplier Code:"));
        addPurchaseFrame.add(supplierCodeField);
        addPurchaseFrame.add(new JLabel("Product Code:"));
        addPurchaseFrame.add(productCodeField);
        addPurchaseFrame.add(new JLabel("Date:"));
        addPurchaseFrame.add(dateField);
        addPurchaseFrame.add(new JLabel("Quantity:"));
        addPurchaseFrame.add(quantityField);
        addPurchaseFrame.add(new JLabel("Total Cost:"));
        addPurchaseFrame.add(totalCostField);
    
        JButton addPurchaseButton = new JButton("Add Purchase");
        addPurchaseFrame.add(addPurchaseButton);
    
        addPurchaseButton.addActionListener(addEvent -> {
            String supplierCode = supplierCodeField.getText();
            String productCode = productCodeField.getText();
            String date = dateField.getText();
            int quantity = Integer.parseInt(quantityField.getText());
            double totalCost = Double.parseDouble(totalCostField.getText());
    
            try {
                String query = "INSERT INTO " + tableName + " (suppliercode, productcode, date, quantity, totalcost) VALUES (?, ?, ?, ?, ?)";
                PreparedStatement pstmt = conn.prepareStatement(query);
                pstmt.setString(1, supplierCode);
                pstmt.setString(2, productCode);
                pstmt.setString(3, date);
                pstmt.setInt(4, quantity);
                pstmt.setDouble(5, totalCost);
                pstmt.executeUpdate();
                pstmt.close();
    
                refreshTable(panel, tableName);
                addPurchaseFrame.dispose();
                JOptionPane.showMessageDialog(this, "Purchase added successfully");
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error adding purchase: " + ex.getMessage());
            }
        });
    
        addPurchaseFrame.setVisible(true);
    }
    
    private void openEditPurchaseFrame(JPanel panel, String tableName) {
        JFrame editPurchaseFrame = new JFrame("Edit Purchase");
        editPurchaseFrame.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
        editPurchaseFrame.setSize(400, 300);
        editPurchaseFrame.setLayout(new GridLayout(7, 2));
    
        JTable table = (JTable) ((JScrollPane) panel.getComponent(0)).getViewport().getView();
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a purchase to edit.");
            return;
        }
    
        String supplierCode = table.getValueAt(selectedRow, 1).toString();
        String productCode = table.getValueAt(selectedRow, 2).toString();
        String date = table.getValueAt(selectedRow, 3).toString();
        String quantity = table.getValueAt(selectedRow, 4).toString();
        String totalCost = table.getValueAt(selectedRow, 5).toString();
        String purchaseID = table.getValueAt(selectedRow, 0).toString();
    
        JTextField supplierCodeField = new JTextField(supplierCode);
        JTextField productCodeField = new JTextField(productCode);
        JTextField dateField = new JTextField(date);
        JTextField quantityField = new JTextField(quantity);
        JTextField totalCostField = new JTextField(totalCost);
    
        editPurchaseFrame.add(new JLabel("Supplier Code:"));
        editPurchaseFrame.add(supplierCodeField);
        editPurchaseFrame.add(new JLabel("Product Code:"));
        editPurchaseFrame.add(productCodeField);
        editPurchaseFrame.add(new JLabel("Date:"));
        editPurchaseFrame.add(dateField);
        editPurchaseFrame.add(new JLabel("Quantity:"));
        editPurchaseFrame.add(quantityField);
        editPurchaseFrame.add(new JLabel("Total Cost:"));
        editPurchaseFrame.add(totalCostField);
    
        JButton updatePurchaseButton = new JButton("Update Purchase");
        editPurchaseFrame.add(updatePurchaseButton);
    
        updatePurchaseButton.addActionListener(updateEvent -> {
            String updatedSupplierCode = supplierCodeField.getText();
            String updatedProductCode = productCodeField.getText();
            String updatedDate = dateField.getText();
            int updatedQuantity = Integer.parseInt(quantityField.getText());
            double updatedTotalCost = Double.parseDouble(totalCostField.getText());
    
            try {
                updatePurchase(panel, tableName, updatedSupplierCode, updatedProductCode, updatedDate, updatedQuantity, updatedTotalCost, purchaseID);
                editPurchaseFrame.dispose();
            } catch (SQLException ex) {
                ex.printStackTrace();
                JOptionPane.showMessageDialog(this, "Error updating purchase: " + ex.getMessage());
            }
        });
    
        editPurchaseFrame.setVisible(true);
    }
    
    private void deletePurchase(JPanel panel, String tableName) {
        JTable table = (JTable) ((JScrollPane) panel.getComponent(0)).getViewport().getView();
        int selectedRow = table.getSelectedRow();
        if (selectedRow == -1) {
            JOptionPane.showMessageDialog(this, "Please select a purchase to delete.");
            return;
        }
    
        String purchaseID = table.getValueAt(selectedRow, 0).toString();
    
        try {
            String query = "DELETE FROM " + tableName + " WHERE purchaseID = ?";
            PreparedStatement pstmt = conn.prepareStatement(query);
            pstmt.setString(1, purchaseID);
            int deletedRows = pstmt.executeUpdate();
            if (deletedRows > 0) {
                JOptionPane.showMessageDialog(this, "Purchase deleted successfully");
                refreshTable(panel, tableName);
            } else {
                JOptionPane.showMessageDialog(this, "No purchase found with the provided ID");
            }
        } catch (SQLException ex) {
            ex.printStackTrace();
            JOptionPane.showMessageDialog(this, "Error deleting purchase: " + ex.getMessage());
        }
    }
    
    private void updatePurchase(JPanel panel, String tableName, String supplierCode, String productCode, String date, int quantity, double totalCost, String purchaseID) throws SQLException {
        String query = "UPDATE " + tableName + " SET suppliercode = ?, productcode = ?, date = ?, quantity = ?, totalcost = ? WHERE purchaseID = ?";
        PreparedStatement pstmt = conn.prepareStatement(query);
        pstmt.setString(1, supplierCode);
        pstmt.setString(2, productCode);
        pstmt.setString(3, date);
        pstmt.setInt(4, quantity);
        pstmt.setDouble(5, totalCost);
        pstmt.setString(6, purchaseID);
        int updatedRows = pstmt.executeUpdate();
        if (updatedRows > 0) {
            JOptionPane.showMessageDialog(this, "Purchase updated successfully");
            refreshTable(panel, tableName);
        } else {
            JOptionPane.showMessageDialog(this, "No purchase found with the provided ID");
        }
    }
    
    
    private void refreshTable(JPanel panel, String tableName) {
        JTable table = (JTable) ((JScrollPane) panel.getComponent(0)).getViewport().getView();
        DefaultTableModel model = (DefaultTableModel) table.getModel();
        model.setRowCount(0); // Clear existing data
        model.setColumnCount(0); // Clear columns
    
        try {
            Statement stmt = conn.createStatement(ResultSet.TYPE_SCROLL_INSENSITIVE, ResultSet.CONCUR_READ_ONLY);
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName);
            model.setColumnIdentifiers(buildColumnNames(rs));
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error retrieving data from " + tableName + " table: " + e.getMessage());
        }
    
        try {
            Statement stmt = conn.createStatement();
            ResultSet rs = stmt.executeQuery("SELECT * FROM " + tableName);
            while (rs.next()) {
                Object[] rowData = new Object[model.getColumnCount()];
                for (int i = 0; i < model.getColumnCount(); i++) {
                    rowData[i] = rs.getObject(i + 1);
                }
                model.addRow(rowData);
            }
            stmt.close();
        } catch (SQLException e) {
            e.printStackTrace();
            JOptionPane.showMessageDialog(null, "Error retrieving data from " + tableName + " table: " + e.getMessage());
        }
    }
    
    public static void main(String[] args) {
        SwingUtilities.invokeLater(MainApplication::new);
    }
}