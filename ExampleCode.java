package application;

import javafx.application.Application;
import javafx.collections.FXCollections;
import javafx.geometry.Insets;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.layout.GridPane;
import javafx.stage.Stage;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileReader;
import java.io.FileWriter;
import java.io.IOException;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Optional;

import javafx.scene.layout.VBox;
import java.util.Random;
import java.util.Set;
import java.util.stream.Collectors;
public class HelpSystemApp extends Application {

    private Map<String, User> userAccounts = new HashMap<>(); // Keeping track of users
    private Map<String, Article> articles = new HashMap<>();
    private Map<String, String> invitationCodes = new HashMap<>(); // Store invite codes
    private Map<String, SpecialAccessGroup> specialAccessGroups = new HashMap<>();
    private Map<String, GeneralGroup> generalGroups = new HashMap<>();
    private boolean isFirstUser = true; // Is first user
    private User currentUser;

    @Override
    public void start(Stage primaryStage) {
        primaryStage.setTitle("CSE 360 Help System"); // Set window title

        GridPane grid = new GridPane();  // Initial Login/Registration Scene setup
        grid.setPadding(new Insets(10, 10, 10, 10));
        grid.setVgap(8);
        grid.setHgap(10);

        // Username input
        Label userLabel = new Label("Username:");
        GridPane.setConstraints(userLabel, 0, 0);
        TextField userInput = new TextField(); // Input field for the username
        GridPane.setConstraints(userInput, 1, 0);

        // Password input
        Label passLabel = new Label("Password:");
        GridPane.setConstraints(passLabel, 0, 1); // Same thing for password
        PasswordField passInput = new PasswordField(); // Hides the password input
        GridPane.setConstraints(passInput, 1, 1);

        // Confirm password input
        Label confirmPassLabel = new Label("Confirm Password:");
        GridPane.setConstraints(confirmPassLabel, 0, 2); // Asking to confirm password
        PasswordField confirmPasswordInput = new PasswordField(); // Another confirmation
        GridPane.setConstraints(confirmPasswordInput, 1, 2);

        // Invitation Code input
        Label inviteLabel = new Label("Invitation Code:");
        GridPane.setConstraints(inviteLabel, 0, 3); // Users need an invite code
        TextField inviteInput = new TextField(); // Input field for the code
        GridPane.setConstraints(inviteInput, 1, 3); // Place it next to the label

        // Login Button
        Button loginButton = new Button("Login");
        GridPane.setConstraints(loginButton, 1, 4);
        
        loginButton.setOnAction(e -> {
            String username = userInput.getText();
            String password = passInput.getText();
            // Login logic
            User user = userAccounts.get(username);
            if (user != null && user.getPassword().equals(password)) {
                System.out.println("Login successful!");
                currentUser = user;
                if (currentUser.getRoles().contains("ADMIN")) {
                    showAdminDashboard(primaryStage); // Show Admin Dashboard
                } else {
                    // Check if account setup is completed
                    if (!currentUser.isAccountSetupCompleted()) {
                        showAccountSetup(primaryStage); // Show account setup only if not completed
                    } else {
                    	handleLogin(currentUser, primaryStage); // Assuming at least one role exists
                    }
                }
            } else {
                System.out.println("Invalid username or password.");
            }
        });

    // Register button setup
    Button registerButton = new Button("Register");
    GridPane.setConstraints(registerButton, 1, 5); // Place the register button
    registerButton.setOnAction(e -> {
        String username = userInput.getText();
        String password = passInput.getText();

        // If this is the first user, make them an admin
        if (isFirstUser) {
            if (password.equals(confirmPasswordInput.getText())) {
                User firstUser = new User(username, password, "ADMIN"); // Make the first user an admin
                userAccounts.put(username, firstUser); // Save the user
                System.out.println("First user registered successfully as Admin!");
                isFirstUser = false; // No more "first user" after this
            } else {
                System.out.println("Passwords do not match."); // Handle password mismatch
            }
        } else {
            // Regular registration process after the first user
            String invitationCode = inviteInput.getText(); // Get the entered invite code
            if (invitationCodes.containsKey(invitationCode)) { // Check validity
                if (password.equals(confirmPasswordInput.getText())) {
                    String role = invitationCodes.get(invitationCode); // Get the role tied to the code
                    User newUser = new User(username, password, role); // Create the user with that role
                    userAccounts.put(username, newUser); // Save
                    System.out.println("User registered successfully with role: " + role);
                } else {
                System.out.println("Passwords do not match."); // If passwords mismatch
                }
            } else {
            System.out.println("Invalid invitation code."); // Handle invalid invite code
            }
        }
    });


//
        // Add everything to the grid (labels, inputs, buttons)
grid.getChildren().addAll(userLabel, userInput, passLabel, passInput, confirmPassLabel, confirmPasswordInput, inviteLabel, inviteInput, loginButton, registerButton);

// Set up the scene and show it
Scene scene = new Scene(grid, 400, 250); // 400x250 window size
primaryStage.setScene(scene); // Put the scene on the stage
primaryStage.show(); // Finally, show the stage
}

// Scene for "Finish setting up your account"
private void showAccountSetup(Stage primaryStage) {
    GridPane setupGrid = new GridPane();
    setupGrid.setPadding(new Insets(10, 10, 10, 10));
    setupGrid.setVgap(8);
    setupGrid.setHgap(10);

    // Email input field
    Label emailLabel = new Label("Email:");
    GridPane.setConstraints(emailLabel, 0, 0);
    TextField emailInput = new TextField(); // Input field for the email
    GridPane.setConstraints(emailInput, 1, 0);

    // First name input
    Label firstNameLabel = new Label("First Name:");
    GridPane.setConstraints(firstNameLabel, 0, 1); // Position the label for first name
    TextField firstNameInput = new TextField(); // Input field for first name
    GridPane.setConstraints(firstNameInput, 1, 1);

    // Middle name input (optional)
    Label middleNameLabel = new Label("Middle Name:");
    GridPane.setConstraints(middleNameLabel, 0, 2);
    TextField middleNameInput = new TextField();
    GridPane.setConstraints(middleNameInput, 1, 2);

    // Last name input
    Label lastNameLabel = new Label("Last Name:");
    GridPane.setConstraints(lastNameLabel, 0, 3); // Same pattern for last name
    TextField lastNameInput = new TextField();
    GridPane.setConstraints(lastNameInput, 1, 3);

    // Preferred name input (optional)
    Label preferredNameLabel = new Label("Preferred First Name (optional):");
    GridPane.setConstraints(preferredNameLabel, 0, 4);
    TextField preferredNameInput = new TextField();
    GridPane.setConstraints(preferredNameInput, 1, 4);

    // Finish button to complete the setup
    Button finishButton = new Button("Finish Setup");
    GridPane.setConstraints(finishButton, 1, 5); // Place the button
    finishButton.setOnAction(e -> {
        // Update the current user info with the inputs
        currentUser.setEmail(emailInput.getText());
        currentUser.setFirstName(firstNameInput.getText());
        currentUser.setMiddleName(middleNameInput.getText());
        currentUser.setLastName(lastNameInput.getText());
        currentUser.setPreferredFirstName(preferredNameInput.getText());
        System.out.println("Account setup completed!");

        // Mark the setup as done and redirect to the role selection screen
        currentUser.setAccountSetupCompleted(true);
        showRoleSelection(primaryStage); // After setup, go to role selection
    });

    // Add everything to the grid
    setupGrid.getChildren().addAll(emailLabel, emailInput, firstNameLabel, firstNameInput, middleNameLabel, middleNameInput, lastNameLabel, lastNameInput, preferredNameLabel, preferredNameInput, finishButton);

    // Set up the scene and display it
    Scene setupScene = new Scene(setupGrid, 400, 300); // Bigger window here
    primaryStage.setScene(setupScene); // Show the setup scene
}


    // Scene for Role Selection
private void showRoleSelection(Stage primaryStage) {
    // If the user has only one role just redirect
    if (currentUser.getRoles().size() == 1) {
        String selectedRole = currentUser.getRoles().get(0); // Get the single role
        System.out.println("Redirecting to role-specific page for role: " + selectedRole);
        
        // Send them directly to their role-specific page
        showRoleSpecificPage(primaryStage, selectedRole);
    } else {
        // If they have multiple roles, let them choose which one to use
        GridPane roleGrid = new GridPane();
        roleGrid.setPadding(new Insets(10, 10, 10, 10)); // Add padding around the grid
        roleGrid.setVgap(8); // Space between rows
        roleGrid.setHgap(10); // Space between columns

        // Label to instruct the user to select a role
        Label roleLabel = new Label("Select Role for this session:");
        GridPane.setConstraints(roleLabel, 0, 0); // Position the label

        // Dropdown for selecting one of their available roles
        ComboBox<String> roleSelect = new ComboBox<>();
        roleSelect.getItems().addAll(currentUser.getRoles()); // Fill it with user's roles
        GridPane.setConstraints(roleSelect, 1, 0); // Position the dropdown

        // Button to proceed after role selection
        Button proceedButton = new Button("Proceed");
        GridPane.setConstraints(proceedButton, 1, 1); // Position the button
        proceedButton.setOnAction(e -> {
            String selectedRole = roleSelect.getValue(); // Get the selected role
            System.out.println("User selected role: " + selectedRole);

            // Redirect them to the appropriate page based on their choice
            showRoleSpecificPage(primaryStage, selectedRole);
        });

        // Add everything to the grid (label, dropdown, button)
        roleGrid.getChildren().addAll(roleLabel, roleSelect, proceedButton);

        // Set up the scene for role selection and display it
        Scene roleScene = new Scene(roleGrid, 400, 200); // Set scene size
        primaryStage.setScene(roleScene); // Display the role selection scene
    }
}
    
    // Admin Dashboard to manage users
private void showAdminDashboard(Stage primaryStage) {
    GridPane adminGrid = new GridPane();
    adminGrid.setPadding(new Insets(10, 10, 10, 10)); // Standard padding
    adminGrid.setVgap(8); // Vertical spacing
    adminGrid.setHgap(10); // Horizontal spacing

    // Welcome label for the Admin Dashboard
    Label welcomeLabel = new Label("Admin Dashboard");
    GridPane.setConstraints(welcomeLabel, 0, 0); // Place at the top

    // Button to invite new users
    Button inviteUserButton = new Button("Invite New User");
    GridPane.setConstraints(inviteUserButton, 0, 1); // Position the invite button
    inviteUserButton.setOnAction(e -> showInviteUserDialog()); // Show invite user dialog when clicked

    // Button to reset user accounts
    Button resetUserButton = new Button("Reset User Account");
    GridPane.setConstraints(resetUserButton, 0, 2); // Position reset
    resetUserButton.setOnAction(e -> showResetUserDialog()); // Show reset user dialog

    // Button to delete user accounts
    Button deleteUserButton = new Button("Delete User Account");
    GridPane.setConstraints(deleteUserButton, 0, 3); // Position delete
    deleteUserButton.setOnAction(e -> showDeleteUserDialog()); // Show delete user dialog

    // Button to list all user accounts
    Button listUsersButton = new Button("List User Accounts");
    GridPane.setConstraints(listUsersButton, 0, 4); // Position list users 
    listUsersButton.setOnAction(e -> listUsers()); // List users when clicked

    // Button to manage users
    Button manageUsersButton = new Button("Manage Users");
    GridPane.setConstraints(manageUsersButton, 0, 5); // Position the manage users button
    manageUsersButton.setOnAction(e -> showManageUsersPage(primaryStage)); // Go to manage users page
    
    Button manageArticlesButton = new Button("Manage Articles");
    GridPane.setConstraints(manageArticlesButton, 0, 6); // Position the manage articles button
    manageArticlesButton.setOnAction(e -> showManageArticlesPage(primaryStage)); // Go to manage articles page
    
    Button manageGroupsButton = new Button("Manage Special Access Groups");
    GridPane.setConstraints(manageGroupsButton, 0, 7); // Position the manage groups button
    manageGroupsButton.setOnAction(e -> showSpecialAccessGroupDialog()); // Go to special access group page
    
    Button manageGenGroupsButton = new Button("Manage General Groups");
    GridPane.setConstraints(manageGenGroupsButton, 0, 8); // Position the general groups button
    manageGenGroupsButton.setOnAction(e -> showGroupDialog()); // Go to general access group page

    // Logout button to exit the admin session
    Button logoutButton = new Button("Logout");
    GridPane.setConstraints(logoutButton, 0, 9); // Position the logout button
    logoutButton.setOnAction(e -> {
        currentUser = null; // Clear the current user when logging out
        start(primaryStage); // Redirect back to login scene
    });

    // Add the logout button and other elements to the grid
    adminGrid.getChildren().add(logoutButton); // Add the logout button to the grid
    adminGrid.getChildren().addAll(welcomeLabel, inviteUserButton, resetUserButton, deleteUserButton, listUsersButton, manageUsersButton, manageArticlesButton, manageGroupsButton, manageGenGroupsButton);

    // Set up the scene with the grid and display it
    Scene adminScene = new Scene(adminGrid, 500, 400); // Create the admin scene
    primaryStage.setScene(adminScene); // Set the scene
    primaryStage.setTitle("Admin Home Page"); // Title for the admin page

    // Extra VBox layout for padding
    VBox layout = new VBox(10);
    layout.setPadding(new Insets(20, 20, 20, 20));

    // Logout logic that also closes the stage
    logoutButton.setOnAction(e -> {
        primaryStage.close(); // Close the current window
        start(primaryStage); // Redirect to login screen again
    });
}

    // Show dialog for inviting a new user
private void showInviteUserDialog() {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Generate Invitation Code"); // Sets the title
        GridPane inviteGrid = new GridPane();
        inviteGrid.setPadding(new Insets(10, 10, 10, 10)); // Padding
        inviteGrid.setVgap(8); // Vertical
        inviteGrid.setHgap(10); // Horizontal
        Label roleLabel = new Label("Role (ADMIN, STUDENT, INSTRUCTOR):"); // Add the role labels
        GridPane.setConstraints(roleLabel, 0, 0);
        TextField roleInput = new TextField();
        GridPane.setConstraints(roleInput, 1, 0);
        Label inviteCodeLabel = new Label("Generated Code:"); // Add generated code label
        GridPane.setConstraints(inviteCodeLabel, 0, 1);
        TextField inviteCodeOutput = new TextField();
        inviteCodeOutput.setEditable(false); // Prevents editing
        GridPane.setConstraints(inviteCodeOutput, 1, 1);
        Button generateButton = new Button("Generate Code"); // Add generate code button
        GridPane.setConstraints(generateButton, 1, 2);
        generateButton.setOnAction(e -> {
            String role = roleInput.getText().toUpperCase();
            if (!role.equals("ADMIN") && !role.equals("STUDENT") && !role.equals("INSTRUCTOR")) { // Check to ensure balid role
                System.out.println("Invalid role entered.");
                return;
            }
            // Generate a unique invitation code
            String invitationCode = generateResetToken();
            invitationCodes.put(invitationCode, role); // Store the code with the associated role
            inviteCodeOutput.setText(invitationCode); // Display the generated code
            System.out.println("Generated invitation code: " + invitationCode + " for role: " + role);
        });
        inviteGrid.getChildren().addAll(roleLabel, roleInput, inviteCodeLabel, inviteCodeOutput, generateButton);

        Scene dialogScene = new Scene(inviteGrid, 400, 200); // Set up invitation scene
        dialogStage.setScene(dialogScene);
        dialogStage.show();
    }
    
    // Show dialog for resetting a user account
private void showResetUserDialog() {
    Stage dialogStage = new Stage();
    dialogStage.setTitle("Reset User Account"); // Set the title for the dialog
    
    // Create a grid for the reset dialog
    GridPane resetGrid = new GridPane();
    resetGrid.setPadding(new Insets(10, 10, 10, 10)); // Padding
    resetGrid.setVgap(8);
    resetGrid.setHgap(10);
    
    // Label and input field for the username
    Label usernameLabel = new Label("Username:");
    GridPane.setConstraints(usernameLabel, 0, 0); // Position the label
    TextField usernameInput = new TextField(); // Input field for the username
    GridPane.setConstraints(usernameInput, 1, 0); // Position the input next to the label
    
    // Button to reset the user's password
    Button resetButton = new Button("Reset Password");
    GridPane.setConstraints(resetButton, 1, 1); // Position the reset button
    resetButton.setOnAction(e -> {
        String username = usernameInput.getText(); // Get the entered username
        User user = userAccounts.get(username); // Check if the user exists

        if (user != null) {
            // Generate a one-time password and set expiration for 10 minutes
            String oneTimePassword = generateOneTimePassword(); // Generate the OTP
            user.setOtp(oneTimePassword);
            LocalDateTime expiryTime = LocalDateTime.now().plusMinutes(10); // Set expiration time
            user.setPassword(oneTimePassword);
            user.setOneTimePassword(true); // Mark the user as having a valid OTP
            user.setOneTimePasswordExpiry(expiryTime); // Set the expiration time
            
            // Notify the admin of the OTP
            System.out.println("One-Time Password for " + username + ": " + oneTimePassword);
            System.out.println("This OTP expires at: " + expiryTime);
        } else {
            System.out.println("User not found.");
        }

        dialogStage.close(); // Close the dialog when done
    });
    
    // Add all the components to the grid
    resetGrid.getChildren().addAll(usernameLabel, usernameInput, resetButton);
    
    // Set up the scene for the reset dialog
    Scene dialogScene = new Scene(resetGrid, 300, 150); // Set size for the dialog
    dialogStage.setScene(dialogScene); // Display the scene
    dialogStage.show(); // Show the dialog
}

// Helper method to generate a one-time password
private String generateOneTimePassword() {
    // Generating a random 6-digit OTP
    return String.valueOf((int)(Math.random() * 900000) + 100000); // Generates a number between 100000 and 999999
}



    // Show dialog for deleting a user account
private void showDeleteUserDialog() {
    Stage dialogStage = new Stage();
    dialogStage.setTitle("Delete User Account");

    // Set up a grid layout for the delete dialog
    GridPane deleteGrid = new GridPane();
    deleteGrid.setPadding(new Insets(10, 10, 10, 10));
    deleteGrid.setVgap(8);
    deleteGrid.setHgap(10);

    // Label and input field for the username
    Label usernameLabel = new Label("Username:");
    GridPane.setConstraints(usernameLabel, 0, 0);
    TextField usernameInput = new TextField(); // Input for the username
    GridPane.setConstraints(usernameInput, 1, 0);

    // Button to delete the user
    Button deleteButton = new Button("Delete User");
    GridPane.setConstraints(deleteButton, 1, 1); // Place the delete button
    deleteButton.setOnAction(e -> {
        String username = usernameInput.getText(); // Get the username entered

        // Try to remove the user, print success or failure
        if (userAccounts.remove(username) != null) {
            System.out.println("User " + username + " deleted successfully."); // User found and deleted
        } else {
            System.out.println("User not found."); // No user with that username
        }

        dialogStage.close(); // Close dialog
    });

    // Add all pieces to the grid
    deleteGrid.getChildren().addAll(usernameLabel, usernameInput, deleteButton);

    // Set up and show the dialog scene
    Scene dialogScene = new Scene(deleteGrid, 300, 150); // Set size of the dialog
    dialogStage.setScene(dialogScene); // Display the scene
    dialogStage.show(); // Show the dialog
}
// Student home page method
private void showStudentHomePage(Stage primaryStage) { 
    Stage studentHomeStage = new Stage(); // Set the stage
    studentHomeStage.setTitle("Student Home Page");

    VBox layout = new VBox(10);
    layout.setPadding(new Insets(20));

    Label welcomeLabel = new Label("Welcome, Student!");

    // Buttons for new features
    Button quitButton = new Button("Quit");
    quitButton.setOnAction(e -> studentHomeStage.close()); // Quit application

    Button sendGenericMessageButton = new Button("Send Generic Message");
    sendGenericMessageButton.setOnAction(e -> sendGenericMessage());

    Button sendSpecificMessageButton = new Button("Send Specific Message");
    sendSpecificMessageButton.setOnAction(e -> sendSpecificMessage());

    Button searchArticlesButton = new Button("Search Articles");
    searchArticlesButton.setOnAction(e -> searchArticles());
    
    Button viewSpecialAccessArticlesButton = new Button("View Special Access Articles");
    viewSpecialAccessArticlesButton.setOnAction(e -> showSpecialAccessArticles(primaryStage));

	// Add new buttons to layout
    layout.getChildren().addAll(welcomeLabel, quitButton, sendGenericMessageButton, 
                                sendSpecificMessageButton, searchArticlesButton, viewSpecialAccessArticlesButton); 

    Scene scene = new Scene(layout, 400, 300);
    studentHomeStage.setScene(scene); // Display
    studentHomeStage.show();
}

private void showSpecialAccessArticles(Stage primaryStage) {
    User currentStudent = currentUser;

    // Create a list of special access groups where the student has permission to view article bodies
    List<SpecialAccessGroup> accessibleGroups = getAccessibleSpecialAccessGroupsForStudent(currentStudent);

    if (accessibleGroups.isEmpty()) { // Special Check
        showAlert("Access Denied", "You are not a part of any special access groups.");
        return;
    }

    // Create a list of article titles from these accessible groups
    ListView<String> articleListView = new ListView<>();
    for (SpecialAccessGroup group : accessibleGroups) {
        for (Article article : group.getArticles()) {
            articleListView.getItems().add(article.getTitle());
        }
    }

    // Display the list of articles from accessible groups
    Button viewBodyButton = new Button("View Article Body");
    viewBodyButton.setOnAction(e -> {
        String selectedTitle = articleListView.getSelectionModel().getSelectedItem();
        if (selectedTitle != null) {
            // Find the selected article
            Article selectedArticle = getArticleByTitle(selectedTitle);
            if (selectedArticle != null) {
                // Display the body of the selected article
                String decryptedBody = selectedArticle.decryptBody(); // Assuming decryptBody is a method in Article class
                showAlert("Article Body", decryptedBody);
            } else {
                showAlert("Error", "Article not found.");
            }
        } else {
            showAlert("No Selection", "Please select an article to view.");
        }
    });

    VBox layout = new VBox(10); // New Vbox
    layout.setPadding(new Insets(20, 20, 20, 20));

    layout.getChildren().addAll(new Label("Select an Article to View Body:"), articleListView, viewBodyButton); // Add buttons

    Scene scene = new Scene(layout, 400, 400);
    Stage stage = new Stage(); // Display
    stage.setTitle("Special Access Articles");
    stage.setScene(scene);
    stage.show();
}

private Article getArticleByTitle(String title) {
    // Iterate over the articles map (assuming articles is a Map<String, Article>)
    for (Article article : articles.values()) {
        if (article.getTitle().equals(title)) {
            return article; // Return the article if the title matches
        }
    }
    return null; // Return null if no article with the given title is found
}

private List<SpecialAccessGroup> getAccessibleSpecialAccessGroupsForStudent(User student) {
    // This method should return a list of SpecialAccessGroups the student has access to view
    List<SpecialAccessGroup> accessibleGroups = new ArrayList<>();
    for (SpecialAccessGroup group : specialAccessGroups.values()) {  // Assuming specialAccessGroups is a map of groups
        if (group.getStudents().contains(student)) {
            accessibleGroups.add(group);
        }
    }
    return accessibleGroups;
}
 // Show the user management page
private void showManageUsersPage(Stage primaryStage) {
    Stage manageUsersStage = new Stage();
    manageUsersStage.setTitle("Manage Users"); // Set title for the window

    VBox layout = new VBox(10); // Vertical layout with spacing
    layout.setPadding(new Insets(20, 20, 20, 20)); // Padding for breathing room

    Label manageUsersLabel = new Label("Manage Users"); // Label for the top of the page

    // Create a ListView to display the users
    ListView<String> userListView = new ListView<>();

    // Populate the ListView with user data
    for (String username : userAccounts.keySet()) {
        User user = userAccounts.get(username);
        String roles = String.join(", ", user.getRoles()); // Join roles with a comma
        userListView.getItems().add(username + " - Roles: " + roles); // Add to the ListView
    }

    // Buttons for managing users
    Button addUserButton = new Button("Add User");
    Button deleteUserButton = new Button("Delete Selected User");
    Button editUserRoleButton = new Button("Edit Selected User Role");

    // Action for adding a user
    addUserButton.setOnAction(e -> {
        showAddUserDialog(); // Open dialog to add a user
    });

    // Action for deleting a selected user
    deleteUserButton.setOnAction(e -> {
        String selectedUser = userListView.getSelectionModel().getSelectedItem(); // Get selected user
        if (selectedUser != null) {
            String username = selectedUser.split(" - ")[0]; // Extract the username
            userAccounts.remove(username); // Remove user
            userListView.getItems().remove(selectedUser); // Remove from ListView
            System.out.println("Deleted user: " + username);
        } else {
            System.out.println("No user selected for deletion."); // Handle no selection
        }
    });

    // Action for editing user roles
    editUserRoleButton.setOnAction(e -> {
        String selectedUser = userListView.getSelectionModel().getSelectedItem(); // Get selected user
        if (selectedUser != null) {
            String username = selectedUser.split(" - ")[0]; // Username
            showEditUserRoleDialog(username); // Open dialog to edit user roles
        } else {
            System.out.println("No user selected for role edit."); // Handle no selection
        }
    });
    // Add everything to the layout
    layout.getChildren().addAll(manageUsersLabel, userListView, addUserButton, deleteUserButton, editUserRoleButton);
    // Set up the scene and display it
    Scene scene = new Scene(layout, 400, 300); // Set size
    manageUsersStage.setScene(scene); // Set the scene on the stage
    manageUsersStage.show(); // Show stage
}
    // Method to add a user
    private void showAddUserDialog() {
        Stage addUserStage = new Stage(); // Set stage
        addUserStage.setTitle("Add New User");
        VBox layout = new VBox(10);
        
        layout.setPadding(new Insets(20, 20, 20, 20)); // Padding
        Label nameLabel = new Label("Enter User Name:");
        TextField nameInput = new TextField(); // Input for the combobox
        Label roleLabel = new Label("Select Role:");
        ComboBox<String> roleComboBox = new ComboBox<>();
        roleComboBox.getItems().addAll("Student", "Instructor", "Admin"); // Selection
        
        Button submitButton = new Button("Submit");
        submitButton.setOnAction(e -> {
            String userName = nameInput.getText();
            String role = roleComboBox.getValue();
            if (!userName.isEmpty() && role != null) {
                // Logic to add the user to the system
                System.out.println("Added user: " + userName + " with role: " + role);
                addUserStage.close();
            } else {
                System.out.println("User name or role not selected.");
            }
        });
        layout.getChildren().addAll(nameLabel, nameInput, roleLabel, roleComboBox, submitButton);
        Scene scene = new Scene(layout, 300, 200);
        addUserStage.setScene(scene);
        addUserStage.show();
    }
    // Method to handle editing a user role (simplified)
 private void showEditUserRoleDialog(String selectedUser) {
    Stage editUserRoleStage = new Stage();
    editUserRoleStage.setTitle("Edit User Roles");
    VBox layout = new VBox(10);
     layout.setPadding(new Insets(20, 20, 20, 20));
 
    Label userLabel = new Label("Editing roles for: " + selectedUser); // Display user being edited

    // Get the current user object
    User user = userAccounts.get(selectedUser.split(" - ")[0]); // Extract username from the string

    // Create checkboxes for role selection
    CheckBox studentCheckBox = new CheckBox("Student");
    CheckBox instructorCheckBox = new CheckBox("Instructor");
    CheckBox adminCheckBox = new CheckBox("Admin");

    // Mark checkboxes based on current user roles
    studentCheckBox.setSelected(user.getRoles().contains("Student"));
    instructorCheckBox.setSelected(user.getRoles().contains("Instructor"));
    adminCheckBox.setSelected(user.getRoles().contains("Admin"));

    // Button to submit the role changes
    Button submitButton = new Button("Submit");
    submitButton.setOnAction(e -> {
    // Collect selected roles based on checkboxes
    List<String> selectedRoles = new ArrayList<>();
        if (studentCheckBox.isSelected()) {
            selectedRoles.add("Student");
        }
        if (instructorCheckBox.isSelected()) {
            selectedRoles.add("Instructor");
        }
        if (adminCheckBox.isSelected()) {
            selectedRoles.add("Admin");
        }

    // Update the user's roles with the new selection
    user.setRoles(selectedRoles);
    System.out.println("Updated roles for " + selectedUser + " to: " + selectedRoles);
    editUserRoleStage.close(); // Close the role editing dialog
});

// Add elements to the layout
    layout.getChildren().addAll(userLabel, studentCheckBox, instructorCheckBox, adminCheckBox, submitButton);

// Set up and show the scene for editing roles
    Scene scene = new Scene(layout, 300, 200); // Set size for the role editing window
    editUserRoleStage.setScene(scene); // Set the scene
    editUserRoleStage.show(); // Show the stage
 }
// Instructor Home Page
 private void showInstructorHomePage(Stage primaryStage) {
	    Stage instructorHomeStage = new Stage();
	    instructorHomeStage.setTitle("Instructor Home Page");

	    VBox layout = new VBox(10);
	    layout.setPadding(new Insets(20, 20, 20, 20));

	    Label welcomeLabel = new Label("Welcome, Instructor!");

	    // Manage Articles Button
	    Button manageArticlesButton = new Button("Manage Articles");
	    manageArticlesButton.setOnAction(e -> showManageArticlesPage(primaryStage)); // Navigate to manage articles page

	    // Manage Special Access Groups Button
	    Button manageSpecialGroupsButton = new Button("Manage Special Access Groups");
	    manageSpecialGroupsButton.setOnAction(e -> showSpecialAccessGroupDialog()); // Navigate to manage special groups page

	    Button manageGenGroupsButton = new Button("Manage General Groups");
	    manageGenGroupsButton.setOnAction(e -> showGroupDialog()); // Go to general access group page
	 
	    Button searchArticlesButton = new Button("Search Articles");
	    searchArticlesButton.setOnAction(e -> searchArticles());

	    // Logout Button
	    Button logoutButton = new Button("Log Out");
	    logoutButton.setOnAction(e -> {
	        instructorHomeStage.close();
	        start(primaryStage); // Redirect back to login after logout
	    });

	    // Add buttons to the layout
	    layout.getChildren().addAll(welcomeLabel,
	                                manageArticlesButton,
	                                manageSpecialGroupsButton,
	                                manageGenGroupsButton,
	                                searchArticlesButton,
	                                logoutButton);

	    // Create and show the scene
	    Scene scene = new Scene(layout, 400, 300);
	    instructorHomeStage.setScene(scene);
	    instructorHomeStage.show();
	}


    private void handleLogin(User user, Stage primaryStage) {
    if (user.isOneTimePassword()) {
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Create New Password"); // Sets the title
        GridPane inviteGrid = new GridPane();
        inviteGrid.setPadding(new Insets(10, 10, 10, 10)); // Padding
        inviteGrid.setVgap(8); // Vertical
        inviteGrid.setHgap(10); // Horizontal
        Label roleLabel = new Label("New Password:");
        GridPane.setConstraints(roleLabel, 0, 0);
        TextField roleInput = new TextField();
        GridPane.setConstraints(roleInput, 1, 0);
        Button generateButton = new Button("Confirm New Password"); // Add generate code button
        GridPane.setConstraints(generateButton, 1, 2);
        generateButton.setOnAction(e -> {
            String role = roleInput.getText();
            user.setPassword(role);
            user.setOneTimePassword(false);
            primaryStage.close(); // Close the current window
            start(primaryStage);
        });
        // Add everything to the layout
        inviteGrid.getChildren().addAll(roleLabel, roleInput, generateButton);
        // Set up the scene and display it
        Scene scene = new Scene(inviteGrid, 400, 300);
        dialogStage.setScene(scene);
        dialogStage.show();
    }
    else
    if (user.getRoles().size() == 1) {
        // Single role: redirect to the appropriate home page
        redirectToRoleHomePage(user.getRoles().get(0), primaryStage);
    } else if (user.getRoles().size() > 1) {
        // Multiple roles: prompt the user to select a role
        showRoleSelectionDialog(user, primaryStage);
    }
}
    // Role Selection Dialog Method
    private void showRoleSelectionDialog(User user, Stage primaryStage) {
        Stage roleDialogStage = new Stage();
        roleDialogStage.setTitle("Select Role");

        // Create rolegrid
        GridPane roleGrid = new GridPane();
        roleGrid.setPadding(new Insets(10, 10, 10, 10));
        roleGrid.setVgap(8);
        roleGrid.setHgap(10);

        // Label for role selection
        Label roleLabel = new Label("Select Role:");
        GridPane.setConstraints(roleLabel, 0, 0);

        // Dropdown for role selection
    ComboBox<String> roleSelectionBox = new ComboBox<>();
    roleSelectionBox.getItems().addAll(user.getRoles()); // Add user roles to the dropdown
    GridPane.setConstraints(roleSelectionBox, 1, 0);

    // Submit button for confirming role selection
    Button submitButton = new Button("Submit");
    GridPane.setConstraints(submitButton, 1, 1);
    submitButton.setOnAction(e -> {
        String selectedRole = roleSelectionBox.getValue(); // Get the selected role
            if (selectedRole != null) {
                redirectToRoleHomePage(selectedRole, primaryStage); // Redirect based on role
                roleDialogStage.close(); // Close the dialog
            }
    });

    // Add elements to the grid
    roleGrid.getChildren().addAll(roleLabel, roleSelectionBox, submitButton);

    // Set up and show the role selection dialog
    Scene roleScene = new Scene(roleGrid, 300, 150);
    roleDialogStage.setScene(roleScene);
    roleDialogStage.show();
}

// Redirect to the appropriate home page based on the role
private void redirectToRoleHomePage(String role, Stage primaryStage) {
    String normalizedRole = role.toUpperCase(); // Normalize role string

    switch (normalizedRole) {
        case "STUDENT":
            showStudentHomePage(primaryStage); // Redirect to student home
            break;
        case "INSTRUCTOR":
            showInstructorHomePage(primaryStage); // Redirect to instructor home
            break;
        default:
                System.out.println("Unknown role: " + role);
    }
}

    // List all user accounts
private void listUsers() {
    System.out.println("Current Users:");
    for (String username : userAccounts.keySet()) {
        System.out.println(" - " + username + " with roles: " + userAccounts.get(username).getRoles());
    }
}

// Generate a reset token
private String generateResetToken() {
    return Long.toHexString(Double.doubleToLongBits(Math.random())); // Random hex
}

// Show the role's page
private void showRoleSpecificPage(Stage primaryStage, String role) {
    GridPane roleGrid = new GridPane();
    roleGrid.setPadding(new Insets(10, 10, 10, 10));
    roleGrid.setVgap(8);
    roleGrid.setHgap(10);

    // Label to welcome the user based on their role
    Label rolePageLabel = new Label("Welcome to the " + role + " Page!");
    GridPane.setConstraints(rolePageLabel, 0, 0);

    // Logout button
    Button logoutButton = new Button("Logout");
    GridPane.setConstraints(logoutButton, 0, 1);
    logoutButton.setOnAction(e -> {
        currentUser = null; // Clear the current user
        start(primaryStage); // Redirect back to login page
    });

    // Add elements to the grid
    roleGrid.getChildren().addAll(rolePageLabel, logoutButton);

    // Set up and show the role's page
    Scene roleScene = new Scene(roleGrid, 400, 200);
    primaryStage.setScene(roleScene);
}

private void showEditArticleDialog(String selectedArticle) {
    Stage editArticleStage = new Stage();
    editArticleStage.setTitle("Edit Article");
    VBox layout = new VBox(10);
    layout.setPadding(new Insets(20, 20, 20, 20));

    // Display the article being edited
    Label articleLabel = new Label("Editing article: " + selectedArticle);

    // Get the current article object
    Article article = articles.get(selectedArticle);

    TextField levelField = new TextField(article.getLevel());
    TextField titleField = new TextField(article.getTitle()); // Title input
    TextField descriptionField = new TextField(article.getDescription()); // Description input
    TextField keywordsField = new TextField(String.join(", ", article.getKeywords())); // Keywords input
    
    TextField groupsField = new TextField(String.join(", ", article.getGroups())); // Groups input
    
    TextField referencesField = new TextField(String.join(", ", article.getReferences())); // References input

    // Button to submit the article changes
    Button submitButton = new Button("Submit");
    submitButton.setOnAction(e -> {
        // Manually update each field of the article
    	article.setId(article.getId());
    	article.setLevel(levelField.getText());
        article.setTitle(titleField.getText()); // Update title
        article.setDescription(descriptionField.getText()); // Update description
        article.setKeywords(new ArrayList<>(Arrays.asList(keywordsField.getText().split(",\\s*")))); // Update keywords
        
        // Update groups and references
        article.setGroups(new ArrayList<>(Arrays.asList(groupsField.getText().split(",\\s*")))); // Update groups
        article.setReferences(new ArrayList<>(Arrays.asList(referencesField.getText().split(",\\s*")))); // Update references
        
        articles.remove(selectedArticle);
        articles.put(titleField.getText(), article);
        // Print the updated article
        System.out.println("Updated article: " + article.getTitle());
        editArticleStage.close(); // Close the editing dialog
    });

    // Add elements to the layout
    layout.getChildren().addAll(articleLabel,
    							  new Label("Level:"), levelField,
                                  new Label("Title:"), titleField,
                                  new Label("Description:"), descriptionField,
                                  new Label("Keywords (comma-separated):"), keywordsField,
                                  new Label("Groups (comma-separated):"), groupsField,
                                  new Label("References (comma-separated):"), referencesField,
                                  submitButton);

    // Set up and show the scene for editing the article
    Scene scene = new Scene(layout, 400, 600);
    editArticleStage.setScene(scene);
    editArticleStage.show();
}

private void showAddArticleDialog() {
    Stage addArticleStage = new Stage(); // Set stage
    addArticleStage.setTitle("Add New Article");
    VBox layout = new VBox(10);
    
    layout.setPadding(new Insets(20, 20, 20, 20)); // Padding

    Label levelLabel = new Label("Enter Article Level:");
    TextField levelInput = new TextField();

    Label titleLabel = new Label("Enter Article Title:");
    TextField titleInput = new TextField(); // Input for article title

    Label descriptionLabel = new Label("Enter Article Description:");
    TextField descriptionInput = new TextField(); // Input for article description

    Label keywordsLabel = new Label("Enter Keywords (comma-separated):");
    TextField keywordsInput = new TextField(); // Input for keywords

    Label bodyLabel = new Label("Enter Article Body:");
    TextArea bodyInput = new TextArea(); // Input for article body

    Label groupsLabel = new Label("Enter Groups (comma-separated):");
    TextField groupsInput = new TextField(); // Input for groups

    Label referencesLabel = new Label("Enter References (comma-separated):");
    TextField referencesInput = new TextField(); // Input for references

    // Submit button
    Button submitButton = new Button("Submit");
    submitButton.setOnAction(e -> {
    	String level = levelInput.getText();
        String title = titleInput.getText();
        String description = descriptionInput.getText();
        List<String> keywords = Arrays.asList(keywordsInput.getText().split(",\\s*")); // Split keywords
        String body = bodyInput.getText();
        List<String> groups = Arrays.asList(groupsInput.getText().split(",\\s*")); // Split groups
        List<String> references = Arrays.asList(referencesInput.getText().split(",\\s*")); // Split references

        // Check for required fields
        if (!title.isEmpty() && !description.isEmpty()) {
            Random random = new Random();
            long id = random.nextLong();
            Article newArticle = new Article(id, level, title, description, keywords, body, references, groups); // Create new article
            articles.put(title, newArticle); 
            System.out.println("Added article: " + title);
            addArticleStage.close();
        } else {
            System.out.println("Title and description cannot be empty.");
        }
    });

    // Add elements to the layout
    layout.getChildren().addAll(levelLabel, levelInput, titleLabel, titleInput, 
                                  descriptionLabel, descriptionInput,
                                  keywordsLabel, keywordsInput,
                                  bodyLabel, bodyInput,
                                  groupsLabel, groupsInput,
                                  referencesLabel, referencesInput,
                                  submitButton);

    // Set up and show the scene for adding a new article
    Scene scene = new Scene(layout, 400, 500);
    addArticleStage.setScene(scene); // Display
    addArticleStage.show();
}



private void showManageArticlesPage(Stage primaryStage) {
    Stage manageArticlesStage = new Stage();
    manageArticlesStage.setTitle("Manage Articles"); // Set title for the window

    VBox layout = new VBox(10); // Vertical layout
    layout.setPadding(new Insets(20, 20, 20, 20)); // Padding

    Label manageArticlesLabel = new Label("Manage Articles"); // Label for the top of the page

    // Create a ListView to display the articles
    ListView<String> articleListView = new ListView<>();

    // Populate the ListView with article data
    for (String articleName : articles.keySet()) {
    	Article article = articles.get(articleName);
    	String title = article.getTitle();
        articleListView.getItems().add(""+title); // Add articles to the ListView
    }

    // Buttons for managing articles
    Button addArticleButton = new Button("Add Article");
    Button deleteArticleButton = new Button("Delete Selected Article");
    Button editArticleButton = new Button("Edit Selected Article");
    Button backupArticlesButton = new Button("Backup Articles");
    Button restoreArticlesButton = new Button("Restore Articles");
    Button listArticlesButton = new Button("List Articles (by group)");

    // Action for adding an article
    addArticleButton.setOnAction(e -> {
        showAddArticleDialog();
    });

    // Action for deleting a selected article
    deleteArticleButton.setOnAction(e -> {
        String selectedArticle = articleListView.getSelectionModel().getSelectedItem(); // Get selected article
        if (selectedArticle != null) {
        	String art = selectedArticle.split(" - ")[0];
        	articles.remove(art); // Delete article from help system
            articleListView.getItems().remove(selectedArticle); // Remove from ListView
            System.out.println("Deleted article: " + selectedArticle);
        } else {
            System.out.println("No article selected for deletion."); // Handle no selection
        }
    });

    // Action for editing an article
    editArticleButton.setOnAction(e -> {
        String selectedArticle = articleListView.getSelectionModel().getSelectedItem(); // Get selected article
        if (selectedArticle != null) {
            showEditArticleDialog(selectedArticle); // Open dialog to edit article
        } else {
            System.out.println("No article selected for editing."); // Handle no selection
        }
    });
    
    backupArticlesButton.setOnAction(e -> {
        showBackupDialog();
    });
    
    restoreArticlesButton.setOnAction(e -> {
        showRestoreDialog();
    });
    
    listArticlesButton.setOnAction(e -> {
    	showArticleListingDialog();
    });

    // Add everything to the layout
    layout.getChildren().addAll(manageArticlesLabel, articleListView, addArticleButton, deleteArticleButton, editArticleButton, backupArticlesButton, restoreArticlesButton, listArticlesButton);

    // Set up the scene and display it
    Scene scene = new Scene(layout, 500, 400);
    manageArticlesStage.setScene(scene);
    manageArticlesStage.show();
}


private void showBackupDialog() {
    // Input dialog for backup filename
    TextInputDialog filenameDialog = new TextInputDialog("backup.txt");
    filenameDialog.setTitle("Backup Articles");
    filenameDialog.setHeaderText("Enter the filename for the backup:");
    filenameDialog.setContentText("Filename:");
    
    Optional<String> filenameResult = filenameDialog.showAndWait();

    if (filenameResult.isPresent()) {
        String filename = filenameResult.get();

        // Dialog for selecting groups
        ListView<String> groupListView = new ListView<>();
        groupListView.getItems().addAll(getAllGroups()); // Assuming this method returns all available groups
        groupListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);
        
        // VBox to hold both dialogs
        VBox dialogPane = new VBox();
        dialogPane.getChildren().add(new Label("Select groups to include in backup:"));
        dialogPane.getChildren().add(groupListView);

        // Create a confirmation dialog
        Dialog<ButtonType> groupSelectionDialog = new Dialog<>();
        groupSelectionDialog.setTitle("Select Groups");
        groupSelectionDialog.setHeaderText("Choose groups for backup");
        groupSelectionDialog.getDialogPane().setContent(dialogPane);
        groupSelectionDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Show the dialog and wait for a response
        Optional<ButtonType> groupResult = groupSelectionDialog.showAndWait();
        
        if (groupResult.isPresent() && groupResult.get() == ButtonType.OK) {
            // Retrieve selected groups
            List<String> selectedGroups = groupListView.getSelectionModel().getSelectedItems();
            // Call the backup method with the filename and selected groups
            backupArticles(filename, selectedGroups);
        }
    }
}

private void showSpecialBackupDialog() {
    // Input dialog for backup filename
    TextInputDialog filenameDialog = new TextInputDialog("special_access_backup.txt");
    filenameDialog.setTitle("Backup Special Access Groups");
    filenameDialog.setHeaderText("Enter the filename for the backup:");
    filenameDialog.setContentText("Filename:");
    
    Optional<String> filenameResult = filenameDialog.showAndWait();

    if (filenameResult.isPresent()) {
        String filename = filenameResult.get();

        // Dialog for selecting special access groups
        ListView<String> groupListView = new ListView<>();
        groupListView.getItems().addAll(getAllSpecialAccessGroups());
        groupListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

        // VBox to hold both dialogs
        VBox dialogPane = new VBox();
        dialogPane.getChildren().add(new Label("Select Special Access Groups to include in backup:"));
        dialogPane.getChildren().add(groupListView);

        // Create a confirmation dialog
        Dialog<ButtonType> groupSelectionDialog = new Dialog<>();
        groupSelectionDialog.setTitle("Select Special Access Groups");
        groupSelectionDialog.setHeaderText("Choose special access groups for backup");
        groupSelectionDialog.getDialogPane().setContent(dialogPane);
        groupSelectionDialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        // Show the dialog and wait for a response
        Optional<ButtonType> groupResult = groupSelectionDialog.showAndWait();

        if (groupResult.isPresent() && groupResult.get() == ButtonType.OK) {
            // Retrieve selected groups
            List<String> selectedGroups = groupListView.getSelectionModel().getSelectedItems();
            // Call the backup method with the filename and selected groups
            backupSpecialAccessGroups(filename, selectedGroups);
        }
    }
}

// Method for backing up special access groups
private void backupSpecialAccessGroups(String filename, List<String> selectedGroups) {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
        for (String groupName : selectedGroups) {
            SpecialAccessGroup group = specialAccessGroups.get(groupName); // Get the selected group by name
            if (group == null) continue; // Skip if the group does not exist

            for (Article article : group.getArticles()) { // Write to file
                writer.write("ID: " + article.getId());
                writer.newLine();
                writer.write("Level: " + article.getLevel());
                writer.newLine();
                writer.write("Title: " + article.getTitle());
                writer.newLine();
                writer.write("Description: " + article.getDescription());
                writer.newLine();
                writer.write("Keywords: " + String.join(";", article.getKeywords())); 
                writer.newLine();
                writer.write("Body: " + article.getBody());
                writer.newLine();
                writer.write("References: " + String.join(";", article.getReferences()));
                writer.newLine();
                writer.write("Groups: " + String.join(";", article.getGroups()));
                writer.newLine();
                writer.write("-----"); // Separator for each article
                writer.newLine();
            }

            writer.write("----- End of Group -----");
            writer.newLine();
        }

        System.out.println("Backup of special access groups completed successfully to " + filename);
    } catch (IOException e) { // Catch exception
        System.err.println("Error during backup: " + e.getMessage());
    }
}

// Method for backing up articles
private void backupArticles(String filename, List<String> selectedGroups) {
    try (BufferedWriter writer = new BufferedWriter(new FileWriter(filename))) {
        for (Map.Entry<String, Article> entry : articles.entrySet()) {
            Article article = entry.getValue();
            
            // Check if the article belongs to any of the selected groups
            if (selectedGroups.isEmpty() || article.getGroups().stream().anyMatch(selectedGroups::contains)) {
                // Write article details to the backup file
                writer.write("ID: " + article.getId());
                writer.newLine();
                writer.write("Level: " + article.getLevel());
                writer.newLine();
                writer.write("Title: " + article.getTitle());
                writer.newLine();
                writer.write("Description: " + article.getDescription());
                writer.newLine();
                writer.write("Keywords: " + String.join(";", article.getKeywords())); 
                writer.newLine();
                writer.write("Body: " + article.getBody());
                writer.newLine();
                writer.write("References: " + String.join(";", article.getReferences()));
                writer.newLine();
                writer.write("Groups: " + String.join(";", article.getGroups()));
                writer.newLine();
                writer.write("-----"); // Separator for each article
                writer.newLine();
            }
        }
        System.out.println("Backup completed successfully to " + filename);
    } catch (IOException e) { // Throw catch
        System.err.println("Error during backup: " + e.getMessage());
    }
}


private List<String> getAllGroups() {
    Set<String> groupSet = new HashSet<>(); // Using a Set to avoid duplicates

    // Iterate through each article and add its groups to the set
    for (Article article : articles.values()) { // Articles is a Map<Long, Article>
        List<String> groups = article.getGroups(); // Get the list of groups for the article
        if (groups != null) {
            groupSet.addAll(groups); // Add all groups to the set
        }
    }

    // Return the unique groups as a list
    return groupSet.stream().collect(Collectors.toList());
}

private List<String> getAllSpecialAccessGroups() {
    Set<String> specialGroupSet = new HashSet<>(); // Use a Set to avoid duplicates

    // Iterate through each special access group and add its name to the set
    for (SpecialAccessGroup group : specialAccessGroups.values()) { // specialAccessGroups is a Map<String, SpecialAccessGroup>
        specialGroupSet.add(group.getGroupName()); // Add the group name to the set
    }

    // Return the unique group names as a list
    return specialGroupSet.stream().collect(Collectors.toList());
}


private void showRestoreDialog() {
    // Input dialog for restore filename
    TextInputDialog dialog = new TextInputDialog("backup.txt");
    dialog.setTitle("Restore Articles");
    dialog.setHeaderText("Enter the filename to restore from:");
    dialog.setContentText("Filename:");
    Optional<String> result = dialog.showAndWait();

    if (result.isPresent()) { // If true, restore
        String filename = result.get();
        restoreArticles(filename);
    }
}

// Method for showing special restored dialog
private void showSpecialRestoreDialog(SpecialAccessGroup group) {
    // Input dialog for restore filename
    TextInputDialog dialog = new TextInputDialog("special_access_backup.txt");
    dialog.setTitle("Restore Articles");
    dialog.setHeaderText("Enter the filename to restore from:");
    dialog.setContentText("Filename:");
    Optional<String> result = dialog.showAndWait();

    if (result.isPresent()) {
        String filename = result.get();
        restoreSpecialArticles(filename, group);
    }
}

// Method for restoring articles
private void restoreArticles(String filename) {
    File backupFile = new File(filename);
    
    if (backupFile.exists()) {
        // Ask user if they want to merge or replace
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Restore Articles");
        alert.setHeaderText("Do you want to merge or replace existing articles?");
        alert.setContentText("Choose your option:");

        ButtonType mergeButton = new ButtonType("Merge"); // Buttons
        ButtonType replaceButton = new ButtonType("Replace");
        ButtonType cancelButton = new ButtonType("Cancel");

        alert.getButtonTypes().setAll(mergeButton, replaceButton, cancelButton); // Set buttons

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == cancelButton) {
            return; // User chose to cancel
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(backupFile))) {
            String line; // Reader for the file
            String id = null, level = null, title = null, description = null, body = null;
            List<String> keywords = new ArrayList<>();
            List<String> references = new ArrayList<>();
            List<String> groups = new ArrayList<>();
            
            if (result.get() == replaceButton) { 
            	articles.clear();
            }
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("ID: ")) {
                    id = line.substring(4).trim();
                } else if (line.startsWith("Level: ")) {
                    level = line.substring(7).trim();
                } else if (line.startsWith("Title: ")) {
                    title = line.substring(7).trim();
                } else if (line.startsWith("Description: ")) {
                    description = line.substring(13).trim();
                } else if (line.startsWith("Keywords: ")) {
                    keywords = Arrays.asList(line.substring(10).split(";"));
                } else if (line.startsWith("Body: ")) {
                    body = line.substring(6).trim();
                } else if (line.startsWith("References: ")) {
                    references = Arrays.asList(line.substring(12).split(";"));
                } else if (line.startsWith("Groups: ")) {
                    groups = Arrays.asList(line.substring(8).split(";"));
                } else if (line.equals("-----")) {
                    // Create a new Article object from the input data
                    long articleId = Long.parseLong(id);
                    Article article = new Article(articleId, level, title, description, keywords, body, references, groups);
                    
                    if (result.get() == mergeButton) {
                        // Merge logic: Check if any article with the same long ID already exists in the map
                        boolean idExists = articles.values().stream()
                            .anyMatch(existingArticle -> existingArticle.getId() == article.getId());
                        
                        if (!idExists) {
                            articles.put(title, article); // Add only if no matching long ID exists
                        }
                    } else if (result.get() == replaceButton) {
                        articles.put(title, article);
                    }

                    // Reset variables for next article
                    id = level = title = description = body = null;
                    keywords = new ArrayList<>();
                    references = new ArrayList<>();
                    groups = new ArrayList<>();
                }
            }
            showAlert("Restore Successful", "Articles restored successfully from " + backupFile.getName());
        } catch (IOException e) { // Exception catches
            showAlert("Error", "An error occurred while reading the backup file.");
        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid ID format in the backup file.");
        }
    } else {
        showAlert("File Not Found", "The specified backup file does not exist.");
    }
}

private void restoreSpecialArticles(String filename, SpecialAccessGroup group) {
    File backupFile = new File(filename);
    
    if (backupFile.exists()) {
        // Ask user if they want to merge or replace
        Alert alert = new Alert(Alert.AlertType.CONFIRMATION);
        alert.setTitle("Restore Articles");
        alert.setHeaderText("Do you want to merge or replace existing articles?");
        alert.setContentText("Choose your option:");

        ButtonType mergeButton = new ButtonType("Merge");
        ButtonType replaceButton = new ButtonType("Replace");
        ButtonType cancelButton = new ButtonType("Cancel");

        alert.getButtonTypes().setAll(mergeButton, replaceButton, cancelButton); // Set buttons

        Optional<ButtonType> result = alert.showAndWait();
        if (result.isPresent() && result.get() == cancelButton) {
            return; // User chose to cancel
        }

        try (BufferedReader reader = new BufferedReader(new FileReader(backupFile))) {
            String line; // Reader for the file
            String id = null, level = null, title = null, description = null, body = null;
            List<String> keywords = new ArrayList<>();
            List<String> references = new ArrayList<>();
            List<String> groups = new ArrayList<>();
            
            if (result.get() == replaceButton) {
            	group.clearArticles();
            }
            while ((line = reader.readLine()) != null) {
                if (line.startsWith("ID: ")) {
                    id = line.substring(4).trim();
                } else if (line.startsWith("Level: ")) {
                    level = line.substring(7).trim();
                } else if (line.startsWith("Title: ")) {
                    title = line.substring(7).trim();
                } else if (line.startsWith("Description: ")) {
                    description = line.substring(13).trim();
                } else if (line.startsWith("Keywords: ")) {
                    keywords = Arrays.asList(line.substring(10).split(";"));
                } else if (line.startsWith("Body: ")) {
                    body = line.substring(6).trim();
                } else if (line.startsWith("References: ")) {
                    references = Arrays.asList(line.substring(12).split(";"));
                } else if (line.startsWith("Groups: ")) {
                    groups = Arrays.asList(line.substring(8).split(";"));
                } else if (line.equals("-----")) {
                    // Create a new Article object from parsed data
                    long articleId = Long.parseLong(id);
                    Article article = new Article(articleId, level, title, description, keywords, body, references, groups);
                    
                    if (result.get() == mergeButton) {
                        boolean idExists = group.getArticles().stream()
                            .anyMatch(existingArticle -> existingArticle.getId() == article.getId());

                        if (!idExists) {
                            group.addArticle(article); // Add to the special access group if no matching ID exists
                            System.out.println("Article added to special access group: " + group.getGroupName());
                        } else {
                            System.out.println("Article with the same ID already exists in the special access group. Skipping...");
                        }
                    } else if (result.get() == replaceButton) {
                        // Replace logic: Simply add or update the article in the special access group
                        group.addArticle(article); // This will replace the article or add it if it doesn't exist
                        System.out.println("Article replaced in special access group: " + group.getGroupName());
                    }

                    // Reset variables for next article
                    id = level = title = description = body = null;
                    keywords = new ArrayList<>();
                    references = new ArrayList<>();
                    groups = new ArrayList<>();
                }
            }
            showAlert("Restore Successful", "Articles restored successfully from " + backupFile.getName());
        } catch (IOException e) { // Throw catch
            showAlert("Error", "An error occurred while reading the backup file.");
        } catch (NumberFormatException e) {
            showAlert("Error", "Invalid ID format in the backup file.");
        }
    } else {
        showAlert("File Not Found", "The specified backup file does not exist.");
    }
}

    // Method to display an alert message
private void showAlert(String title, String message) {
    Alert alert = new Alert(Alert.AlertType.INFORMATION);
    alert.setTitle(title);
    alert.setHeaderText(null);
    alert.setContentText(message);
    alert.showAndWait();
}

// Method for listing all articles
private void showArticleListingDialog() {
    // Create a new stage for listing articles
    Stage listArticlesStage = new Stage();
    listArticlesStage.setTitle("List Articles");

    // Create a VBox layout
    VBox layout = new VBox(10);

    // Create a ListView to display groups
    ListView<String> groupsListView = new ListView<>();
    groupsListView.getItems().addAll(getAllGroups()); // Method to get all groups

    // Allow multiple selections
    groupsListView.getSelectionModel().setSelectionMode(SelectionMode.MULTIPLE);

    // Create a button to list articles
    Button listArticlesButton = new Button("List Articles");
    listArticlesButton.setOnAction(event -> {
        List<String> selectedGroups = groupsListView.getSelectionModel().getSelectedItems();
        listArticles(selectedGroups);
    });

    // Add components to the layout
    layout.getChildren().addAll(groupsListView, listArticlesButton);

    // Set up the scene for listing articles
    Scene scene = new Scene(layout, 300, 400);
    listArticlesStage.setScene(scene);
    listArticlesStage.show();
}

// Method for listing specific articles
private void listArticles(List<String> selectedGroups) {
    // Create a new stage to show articles
    Stage articlesStage = new Stage();
    articlesStage.setTitle("Articles List");

    // Create a ListView to display the articles
    ListView<String> articlesListView = new ListView<>();

    // Filter articles based on selected groups
    if (selectedGroups.isEmpty()) {
        // If no groups are selected, show all articles
        articlesListView.getItems().addAll(articles.values().stream()
                .map(Article::getTitle) // Assuming Article class has a getTitle() method
                .collect(Collectors.toList()));
    } else {
        // Show only articles that belong to the selected groups
        for (Article article : articles.values()) {
            for (String group : selectedGroups) {
                if (article.getGroups().contains(group)) {
                    articlesListView.getItems().add(article.getTitle()); // Add the title to the list
                    break; // Break to avoid adding the same article multiple times
                }
            }
        }
    }

    // Set up the scene for articles display
    Scene scene = new Scene(articlesListView, 400, 300);
    articlesStage.setScene(scene);
    articlesStage.show();
}

// Method for group dialog
private void showGroupDialog() {
    Stage dialogStage = new Stage();
    dialogStage.setTitle("General Groups");

    VBox layout = new VBox(10);
    layout.setPadding(new Insets(20));

    // Group selection
    ComboBox<String> groupComboBox = new ComboBox<>();
    groupComboBox.getItems().addAll(generalGroups.keySet());
    groupComboBox.setPromptText("Select Group");

    // Action buttons
    Button addGroupButton = new Button("Create New Group");
    Button manageGroupButton = new Button("Manage Selected Group");

    addGroupButton.setOnAction(e -> createGroupDialog());
    manageGroupButton.setOnAction(e -> { // Combobox for selecting the group
        String selectedGroup = groupComboBox.getValue();
        if (selectedGroup != null) {
            manageGroupDialog(selectedGroup, currentUser);
        }
    });

    layout.getChildren().addAll(new Label("General Groups"), groupComboBox, addGroupButton, manageGroupButton); // Add buttons

    Scene scene = new Scene(layout, 300, 200);
    dialogStage.setScene(scene); // Display
    dialogStage.show();
}

// Method for showing special access groups
private void showSpecialAccessGroupDialog() {
    Stage dialogStage = new Stage();
    dialogStage.setTitle("Special Access Groups");

    VBox layout = new VBox(10);
    layout.setPadding(new Insets(20));

    // Group selection
    ComboBox<String> groupComboBox = new ComboBox<>();
    groupComboBox.getItems().addAll(specialAccessGroups.keySet());
    groupComboBox.setPromptText("Select Group");

    // Action buttons
    Button addGroupButton = new Button("Create New Group");
    Button manageGroupButton = new Button("Manage Selected Group");

    addGroupButton.setOnAction(e -> createSpecialAccessGroupDialog());
    manageGroupButton.setOnAction(e -> {
        String selectedGroup = groupComboBox.getValue();
        if (selectedGroup != null) {
            manageSpecialAccessGroupDialog(selectedGroup);
        }
    });
	// Add elements
    layout.getChildren().addAll(new Label("Special Access Groups"), groupComboBox, addGroupButton, manageGroupButton); 

    Scene scene = new Scene(layout, 300, 200);
    dialogStage.setScene(scene); // Display
    dialogStage.show();
}

// Method for creating group dialog
private void createGroupDialog() {
    Stage createGroupStage = new Stage();
    createGroupStage.setTitle("Create General Group");

    VBox layout = new VBox(10);
    layout.setPadding(new Insets(20));

    TextField groupNameField = new TextField(); // Input field
    groupNameField.setPromptText("Enter Group Name");

    Button createButton = new Button("Create"); // Logic create button
    createButton.setOnAction(e -> {
        String groupName = groupNameField.getText();
        if (!groupName.isEmpty() && !generalGroups.containsKey(groupName)) {
            generalGroups.put(groupName, new GeneralGroup(groupName));
            System.out.println("General Group Created: " + groupName);
            createGroupStage.close();
        } else {
            System.out.println("Invalid or Duplicate Group Name.");
        }
    });

    layout.getChildren().addAll(new Label("Group Name:"), groupNameField, createButton); // Add field and button

    Scene scene = new Scene(layout, 300, 150);
    createGroupStage.setScene(scene); // Display
    createGroupStage.show();
}

// Method for creating special access gropups
private void createSpecialAccessGroupDialog() {
    Stage createGroupStage = new Stage();
    createGroupStage.setTitle("Create Special Access Group");

    VBox layout = new VBox(10);
    layout.setPadding(new Insets(20));

    TextField groupNameField = new TextField(); // Input field
    groupNameField.setPromptText("Enter Group Name");

    Button createButton = new Button("Create"); // Standard button
    createButton.setOnAction(e -> {
        String groupName = groupNameField.getText();
        if (!groupName.isEmpty() && !specialAccessGroups.containsKey(groupName)) {
            specialAccessGroups.put(groupName, new SpecialAccessGroup(groupName));
            System.out.println("Special Access Group Created: " + groupName);
            createGroupStage.close();
        } else {
            System.out.println("Invalid or Duplicate Group Name.");
        }
    });

    layout.getChildren().addAll(new Label("Group Name:"), groupNameField, createButton); // Add elements

    Scene scene = new Scene(layout, 300, 150);
    createGroupStage.setScene(scene); // Display
    createGroupStage.show();
}

// Method for managing groups
private void manageGroupDialog(String groupName, User currentUser) {
    GeneralGroup group = generalGroups.get(groupName);
    if (group == null) return;

    // Ensure the current user is either an admin or instructor for this group
    boolean isAdmin = currentUser.getRoles().contains("ADMIN");
    boolean isInstructor = currentUser.getRoles().contains("INSTRUCTOR");

    if (currentUser.isStudent()) {
        showAlert("Access Denied", "You do not have permission to manage group membership.");
        return;
    }

    Stage manageGroupStage = new Stage();
    manageGroupStage.setTitle("Manage Group Membership: " + groupName);

    VBox layout = new VBox(10);
    layout.setPadding(new Insets(20));

    Label groupLabel = new Label("Managing Group: " + groupName);

    // List views to show current members in each role
    ListView<String> adminListView = new ListView<>();
    adminListView.getItems().addAll(FXCollections.observableArrayList(
        group.getAdmins().stream()
            .map(User::getUsername)  // Get username from each User in the group
            .toArray(String[]::new)
    ));

    ListView<String> instructorListView = new ListView<>();
    instructorListView.getItems().addAll(FXCollections.observableArrayList(
        group.getInstructors().stream()
            .map(User::getUsername)
            .toArray(String[]::new)
    ));

    ListView<String> studentListView = new ListView<>();
    studentListView.getItems().addAll(FXCollections.observableArrayList(
        group.getStudents().stream()
            .map(User::getUsername)
            .toArray(String[]::new)
    ));

    Button addAdminButton = new Button("Add Admin");
    Button removeAdminButton = new Button("Remove Admin");

    Button addInstructorButton = new Button("Add Instructor");
    Button removeInstructorButton = new Button("Remove Instructor");

    Button addStudentButton = new Button("Add Student");
    Button removeStudentButton = new Button("Remove Student");

    // For Admins: Can add/remove Admins, Instructors, and Students
    if(isAdmin)
    {
        addAdminButton.setOnAction(e -> addUserToGroup(group, "ADMIN"));
        removeAdminButton.setOnAction(e -> removeUserFromGroup(group, "ADMIN", adminListView));

        addInstructorButton.setOnAction(e -> addUserToGroup(group, "INSTRUCTOR"));
        removeInstructorButton.setOnAction(e -> removeUserFromGroup(group, "INSTRUCTOR", instructorListView));

        addStudentButton.setOnAction(e -> addUserToGroup(group, "STUDENT"));
        removeStudentButton.setOnAction(e -> removeUserFromGroup(group, "STUDENT", studentListView));
    }

    else if (isInstructor)
    {
        addStudentButton.setOnAction(e -> addUserToGroup(group, "STUDENT"));
        removeStudentButton.setOnAction(e -> removeUserFromGroup(group, "STUDENT", studentListView));
    }

    // Layout and scene
    layout.getChildren().addAll(groupLabel, new Label("Admins"), adminListView, addAdminButton, removeAdminButton,
            new Label("Instructors"), instructorListView, addInstructorButton, removeInstructorButton,
            new Label("Students"), studentListView, addStudentButton, removeStudentButton);

    Scene scene = new Scene(layout, 600, 600);
    manageGroupStage.setScene(scene);
    manageGroupStage.show();
}

// Method to add a user to the group based on the role (ADMIN, INSTRUCTOR, STUDENT)
private void addUserToGroup(GeneralGroup group, String role) {
	System.out.print("reached");
    Stage dialogStage = new Stage();
    dialogStage.setTitle("Add user to group");

    // Set up a grid layout for the add dialog
    GridPane addGrid = new GridPane();
    addGrid.setPadding(new Insets(10, 10, 10, 10));
    addGrid.setVgap(8);
    addGrid.setHgap(10);

    // Label and input field for the username
    Label usernameLabel = new Label("Username:");
    GridPane.setConstraints(usernameLabel, 0, 0);
    TextField usernameInput = new TextField(); // Input for the username
    GridPane.setConstraints(usernameInput, 1, 0);

    // Button to add the user
    Button addButton = new Button("Add User");
    GridPane.setConstraints(addButton, 1, 1);
    addButton.setOnAction(e -> {
        String username = usernameInput.getText(); 
        User user = userAccounts.get(username);
        if (user != null) {
            switch (role) {
                case "ADMIN":
                    group.addAdmin(user);
                    break;
                case "INSTRUCTOR":
                    group.addInstructors(user);
                    break;
                case "STUDENT":
                    group.addStudent(user);
                    break;
            }
            System.out.println(username + " added to " + group.getGroupName() + " as " + role);
        } else {
            System.out.println("User not found: " + username);
        }
        dialogStage.close(); // Close dialog
    });

    // Add all pieces to the grid
    addGrid.getChildren().addAll(usernameLabel, usernameInput, addButton);

    // Set up and show the dialog scene
    Scene dialogScene = new Scene(addGrid, 300, 150); // Set size of the dialog
    dialogStage.setScene(dialogScene); // Display the scene
    dialogStage.show(); // Show the dialog
}

// Method to remove a user from the group based on their role and update ListView
private void removeUserFromGroup(GeneralGroup group, String role, ListView<String> listView) {
    String selectedUser = listView.getSelectionModel().getSelectedItem();
    if (selectedUser != null) {
        User user = group.getUsersByRole(role).stream()
            .filter(u -> u.getUsername().equals(selectedUser))
            .findFirst()
            .orElse(null);

        if (user != null) {
            switch (role) {
                case "ADMIN":
                    group.getAdmins().remove(user);
                    break;
                case "INSTRUCTOR":
                    group.getInstructors().remove(user);
                    break;
                case "STUDENT":
                    group.getStudents().remove(user);
                    break;
            }
            listView.getItems().remove(selectedUser);
            System.out.println(selectedUser + " removed from " + group.getGroupName() + " as " + role);
        }
    }
}

// Method for managing special access groups
private void manageSpecialAccessGroupDialog(String groupName) {
    SpecialAccessGroup group = specialAccessGroups.get(groupName);
    if (group == null) return;

    Stage manageGroupStage = new Stage();
    manageGroupStage.setTitle("Manage Special Access Group: " + groupName);

    VBox layout = new VBox(10);
    layout.setPadding(new Insets(20));

    Label groupLabel = new Label("Managing Group: " + groupName);

    ListView<String> specialGroupArticleListView = new ListView<>();
    for (Article article : group.getArticles()) {
        specialGroupArticleListView.getItems().add(article.getTitle()); // Add article titles to the ListView
    }
    
    Button viewBodyButton = new Button("View Article Body");
    viewBodyButton.setOnAction(e -> {
        String selectedTitle = specialGroupArticleListView.getSelectionModel().getSelectedItem(); // Get selected article title
        if (selectedTitle != null) {
            Article selectedArticle = group.getArticles().stream()
                .filter(article -> article.getTitle().equals(selectedTitle))
                .findFirst()
                .orElse(null);

            if (selectedArticle != null) {
                User user = currentUser;
                if (group.getInstructorsView().contains(user)) {
                    String decryptedBody = selectedArticle.decryptBody();
                    showAlert("Article Body", decryptedBody);
                } else {
                    showAlert("Access Denied", "Only those with viewing rights in this group can view article bodies.");
                }
            } else {
                showAlert("Error", "Selected article not found.");
            }
        } else {
            showAlert("No Selection", "Please select an article to view.");
        }
    });
    Button deleteArticleButton = new Button("Delete Article");
    deleteArticleButton.setOnAction(e -> {
        String selectedTitle = specialGroupArticleListView.getSelectionModel().getSelectedItem(); // Get selected article title
        if (selectedTitle != null) {
            Article selectedArticle = group.getArticles().stream()
                .filter(article -> article.getTitle().equals(selectedTitle))
                .findFirst()
                .orElse(null);

            if (selectedArticle != null) {
                    group.removeArticle(selectedArticle); // Remove the article from the group
                    specialGroupArticleListView.getItems().remove(selectedTitle); // Update the ListView
                    System.out.println("Article \"" + selectedArticle.getTitle() + "\" removed from group: " + group.getGroupName());
                    showAlert("Article Removed", "Article \"" + selectedArticle.getTitle() + "\" has been deleted from the group.");
        }
        }
    });

    ListView<String> articleListView = new ListView<>();
    for (String articleName : articles.keySet()) {
        Article article = articles.get(articleName);
        String title = article.getTitle();
        articleListView.getItems().add(title);
    }
    // ListView for Instructors with Viewing Rights
    ListView<String> instructorsWithViewingRightsListView = new ListView<>();
    instructorsWithViewingRightsListView.getItems().addAll(FXCollections.observableArrayList(
        group.getInstructorsView().stream()
             .map(User::getUsername)
             .toArray(String[]::new)
    ));

    // ListView for Students with Viewing Rights
    ListView<String> studentsWithViewingRightsListView = new ListView<>();
    studentsWithViewingRightsListView.getItems().addAll(FXCollections.observableArrayList(
        group.getStudents().stream()
             .map(User::getUsername)
             .toArray(String[]::new)
    ));

    // ListView for Instructors with Admin Rights
    ListView<String> instructorsWithAdminRightsListView = new ListView<>();
    instructorsWithAdminRightsListView.getItems().addAll(FXCollections.observableArrayList(
        group.getInstructorsAd().stream()
             .map(User::getUsername)
             .toArray(String[]::new)
    ));

// Buttons for new viewing features
    Button addAdminWithAdminRightsButton = new Button("Add Admin (Admin Rights)");
    addAdminWithAdminRightsButton.setOnAction(e -> addInstructorWithAdminRights(group, instructorsWithViewingRightsListView));
    
    Button addInstructorWithViewingRightsButton = new Button("Add Instructor/Admin (Viewing Rights)");
    addInstructorWithViewingRightsButton.setOnAction(e -> addInstructorWithViewingRights(group, instructorsWithViewingRightsListView));

    Button addStudentWithViewingRightsButton = new Button("Add Student (Viewing Rights)");
    addStudentWithViewingRightsButton.setOnAction(e -> addStudentWithViewingRights(group, studentsWithViewingRightsListView));

    Button addInstructorWithAdminRightsButton = new Button("Add Instructor (Admin Rights)");
    addInstructorWithAdminRightsButton.setOnAction(e -> addInstructorWithAdminRights(group, instructorsWithAdminRightsListView));

    Button removeInstructorViewButton = new Button("Remove Instructor (Viewing)");
    removeInstructorViewButton.setOnAction(e -> {
        String selectedInstructor = instructorsWithViewingRightsListView.getSelectionModel().getSelectedItem();
        if (selectedInstructor != null) {
            User instructor = group.getInstructorsView().stream()
                    .filter(u -> u.getUsername().equals(selectedInstructor))
                    .findFirst()
                    .orElse(null);
            if (instructor != null) {
                group.getInstructorsView().remove(instructor);
                instructorsWithViewingRightsListView.getItems().remove(selectedInstructor);
                System.out.println("Instructor removed from viewing rights: " + selectedInstructor);
            }
        }
    });
    
    // Remove instructor/admin button
    Button removeInstructorAdButton = new Button("Remove Instructor/Admin (Admin)");
    removeInstructorAdButton.setOnAction(e -> {
        String selectedInstructor = instructorsWithAdminRightsListView.getSelectionModel().getSelectedItem();
        if (selectedInstructor != null) {
            User instructor = group.getInstructorsAd().stream()
                    .filter(u -> u.getUsername().equals(selectedInstructor))
                    .findFirst()
                    .orElse(null);
            if (instructor != null) {
                group.getInstructorsAd().remove(instructor);
                instructorsWithAdminRightsListView.getItems().remove(selectedInstructor);
                System.out.println("Instructor removed from admin rights: " + selectedInstructor);
            }
        }
    });

// Remove student button
    Button removeStudentButton = new Button("Remove Student");
    removeStudentButton.setOnAction(e -> {
        String selectedStudent = studentsWithViewingRightsListView.getSelectionModel().getSelectedItem();
        if (selectedStudent != null) {
            User student = group.getStudents().stream()
                    .filter(u -> u.getUsername().equals(selectedStudent))
                    .findFirst()
                    .orElse(null);
            if (student != null) {
                group.getStudents().remove(student);
                studentsWithViewingRightsListView.getItems().remove(selectedStudent);
                System.out.println("Student removed from viewing rights: " + selectedStudent);
            }
        }
    });

// Add article Button
    Button addArticleButton = new Button("Add Article to Group");
    addArticleButton.setOnAction(e -> {
        String selectedArticleTitle = articleListView.getSelectionModel().getSelectedItem();
        if (selectedArticleTitle != null) {
            Article selectedArticle = articles.get(selectedArticleTitle);
            if (selectedArticle != null) {
                addArticleToSpecialAccessGroup(selectedArticle, group);

                // Remove article from the regular articles list
                articles.remove(selectedArticleTitle); // Delete from regular articles

                System.out.println("Article \"" + selectedArticle.getTitle() + "\" added to special access group: " + groupName);
                // Refresh the ListView for the articles in special access group
                specialGroupArticleListView.getItems().add(selectedArticle.getTitle());
            }
        } else {
            System.out.println("No article selected to add.");
        }
    });
    
// Add student button
    Button addStudentButton = new Button("Add Student");
    addStudentButton.setOnAction(e -> addUserToSpecialGroup(group, "STUDENT"));

    Button backupArticlesButton = new Button("Backup Articles");
    Button restoreArticlesButton = new Button("Restore Articles");
    
    backupArticlesButton.setOnAction(e -> {
        showSpecialBackupDialog();
    });
    
    restoreArticlesButton.setOnAction(e -> {
        showSpecialRestoreDialog(group);
    });

// Add all elements
    layout.getChildren().addAll(groupLabel,
            new Label("Articles"), articleListView, addArticleButton,
            new Label("Articles in Special Access Group"), specialGroupArticleListView, viewBodyButton, deleteArticleButton,
		    new Label("Instructors/Admins (Viewing Rights)"), instructorsWithViewingRightsListView, addInstructorWithViewingRightsButton, removeInstructorViewButton,
		    new Label("Students (Viewing Rights)"), studentsWithViewingRightsListView, addStudentWithViewingRightsButton, removeStudentButton,
		    new Label("Instructors/Admins (Admin Rights)"), instructorsWithAdminRightsListView, addInstructorWithAdminRightsButton, addAdminWithAdminRightsButton, removeInstructorAdButton, backupArticlesButton, restoreArticlesButton);
		    
    Scene scene = new Scene(layout, 1000, 1000);
    manageGroupStage.setScene(scene); // Display
    manageGroupStage.show();
}

// Method for adding user to group
private void addUserToSpecialGroup(SpecialAccessGroup group, String role) {
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("Add " + role);
    dialog.setHeaderText("Enter Username to Add as " + role);

    Optional<String> result = dialog.showAndWait();
    result.ifPresent(username -> {
        User user = userAccounts.get(username);
        if (user != null) {
            switch (role) {
            case "ADMIN":
                group.addAdmin(user); // Add the user to the admins list in the group
                break;
            case "INSTRUCTOR TO VIEW":
                group.addInstructorsView(user); // Add the user to the instructors list in the group
                break;
            case "INSTRUCTOR TO ADMIN":
            	group.addInstructorsAd(user);
            	break;
            case "STUDENT":
                group.addStudent(user); // Add the user to the students list in the group
                break;
            }
            System.out.println(username + " added to " + group.getGroupName() + " as " + role);
        } else {
            System.out.println("User not found: " + username);
        }
    });
}

// Method for adding article to a special access group 
public void addArticleToSpecialAccessGroup(Article article, SpecialAccessGroup group) {
    group.addArticle(article); // Add article
    article.encryptBody();

    System.out.println("Article added to special access group: " + group.getGroupName());
}
// Method for adding an instructor that can view specific groups
private void addInstructorWithViewingRights(SpecialAccessGroup group, ListView<String> listView) {
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("Add Instructor (Viewing Rights)");
    dialog.setHeaderText("Enter Username of Instructor");

    Optional<String> result = dialog.showAndWait();
    result.ifPresent(username -> {
        User user = userAccounts.get(username);
        if (user != null) {
            if (!group.getInstructorsView().contains(user)) {
                group.addInstructorsView(user);
                listView.getItems().add(username);
                System.out.println(username + " added as an instructor with viewing rights."); // Success
            }
        } else {
            System.out.println("User not found: " + username);
        }
    });
}

// Method for adding a student that can view specific groups
private void addStudentWithViewingRights(SpecialAccessGroup group, ListView<String> listView) {
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("Add Student (Viewing Rights)");
    dialog.setHeaderText("Enter Username of Student");

    Optional<String> result = dialog.showAndWait();
    result.ifPresent(username -> {
        User user = userAccounts.get(username);
        if (user != null) {
            if (!group.getStudents().contains(user)) {
                group.addStudent(user);
                listView.getItems().add(username);
                System.out.println(username + " added as a student with viewing rights."); // Success
            }
        } else {
            System.out.println("User not found: " + username);
        }
    });
}

// Method for adding instructor with admin privileges
private void addInstructorWithAdminRights(SpecialAccessGroup group, ListView<String> listView) {
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("Add Instructor/Admin (Admin Rights)");
    dialog.setHeaderText("Enter Username of Instructor/Admin");

    Optional<String> result = dialog.showAndWait();
    result.ifPresent(username -> {
        User user = userAccounts.get(username);
        if (user != null) {
            if (!group.getInstructorsAd().contains(user)) {
                group.addInstructorsAd(user);
                listView.getItems().add(username);
                System.out.println(username + " added as an instructor/admin with admin rights."); // Success
            }
        } else {
            System.out.println("User not found: " + username);
        }
    });
}

// Method for searching thru articles
private void searchArticles() {
    Stage searchStage = new Stage();
    searchStage.setTitle("Search Articles");

    VBox layout = new VBox(10);
    layout.setPadding(new Insets(20));

    Label instructionLabel = new Label("Search Articles:");

    // Dropdown for levels
    ComboBox<String> levelComboBox = new ComboBox<>();
    levelComboBox.getItems().addAll("All", "Beginner", "Intermediate", "Advanced", "Expert");
    levelComboBox.setValue("All"); // Default level is "All"

    // Dropdown for groups
    ComboBox<String> groupComboBox = new ComboBox<>();
    groupComboBox.getItems().addAll("All"); // Add default option
    groupComboBox.getItems().addAll(getAllGroups()); // Add available groups
    groupComboBox.setValue("All"); // Default group is "All"

    // Input field for search term
    TextField searchField = new TextField();
    searchField.setPromptText("Enter search term (title, author, or abstract)");

    Button searchButton = new Button("Search");
    searchButton.setOnAction(e -> performSearch(levelComboBox.getValue(), 
                                                groupComboBox.getValue(), 
                                                searchField.getText()));

    layout.getChildren().addAll(instructionLabel, new Label("Select Level:"), levelComboBox,
                                new Label("Select Group:"), groupComboBox,
                                new Label("Search Term:"), searchField, searchButton);

    Scene scene = new Scene(layout, 400, 400);
    searchStage.setScene(scene);
    searchStage.show();
}

// Method for performing a search via level group or term
private void performSearch(String level, String group, String term) {
    List<Article> filteredArticles = new ArrayList<>(articles.values());

    // Filter by level
    if (!level.equals("All")) {
        filteredArticles.removeIf(article -> !article.getLevel().equalsIgnoreCase(level));
    }

    // Filter by group
    if (!group.equals("All")) {
        filteredArticles.removeIf(article -> !article.getGroups().contains(group));
    }

    // Filter by search term
    if (!term.isEmpty()) {
        filteredArticles.removeIf(article -> !article.getTitle().toLowerCase().contains(term.toLowerCase())
                && !article.getDescription().toLowerCase().contains(term.toLowerCase())
                && article.getKeywords().stream().noneMatch(keyword -> keyword.toLowerCase().contains(term.toLowerCase())));
    }

    // Display results
    displaySearchResults(filteredArticles, group);
}

// Method for displaying results from search
private void displaySearchResults(List<Article> articles, String activeGroup) {
    Stage resultsStage = new Stage();
    resultsStage.setTitle("Search Results");

    VBox layout = new VBox(10);
    layout.setPadding(new Insets(20));

    Label groupLabel = new Label("Active Group: " + activeGroup);
    Label countLabel = new Label("Articles Found: " + articles.size());

    ListView<String> resultsListView = new ListView<>(); // List all that was found
    int index = 1;
    for (Article article : articles) {
        resultsListView.getItems().add(index + ". " + article.getTitle() + " - " + article.getDescription());
        index++;
    }

    Button viewArticleButton = new Button("View Article"); // Viewing the article button
    viewArticleButton.setOnAction(e -> {
        String selectedArticle = resultsListView.getSelectionModel().getSelectedItem();
        if (selectedArticle != null) {
            int selectedIndex = resultsListView.getSelectionModel().getSelectedIndex();
            viewArticleInDetail(articles.get(selectedIndex));
        } else {
            showAlert("No Selection", "Please select an article to view.");
        }
    });

    layout.getChildren().addAll(groupLabel, countLabel, resultsListView, viewArticleButton);

    Scene scene = new Scene(layout, 400, 500);
    resultsStage.setScene(scene); // Display
    resultsStage.show();
}

// Method for viewing more details on an article
private void viewArticleInDetail(Article article) {
    Stage articleStage = new Stage();
    articleStage.setTitle("Article Details: " + article.getTitle());

    VBox layout = new VBox(10);
    layout.setPadding(new Insets(20));

    Label titleLabel = new Label("Title: " + article.getTitle());
    Label authorLabel = new Label("Author: " + article.getDescription());
    Label keywordsLabel = new Label("Keywords: " + String.join(", ", article.getKeywords()));
    Label bodyLabel = new Label("Body: " + article.getBody());

    layout.getChildren().addAll(titleLabel, authorLabel, keywordsLabel, bodyLabel);

    Scene scene = new Scene(layout, 400, 300);
    articleStage.setScene(scene); // Display
    articleStage.show();
}

// Method for generic message
private void sendGenericMessage() {
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("Generic Message");
    dialog.setHeaderText("Send a Generic Message");
    dialog.setContentText("Enter your message:");

    Optional<String> result = dialog.showAndWait();
    result.ifPresent(message -> {
        System.out.println("Generic message sent: " + message);
        showAlert("Message Sent", "Your message has been sent to the help system.");
    });
}

// Method for specific method
private void sendSpecificMessage() {
    TextInputDialog dialog = new TextInputDialog();
    dialog.setTitle("Specific Message");
    dialog.setHeaderText("Send a Specific Message");
    dialog.setContentText("Describe what you need but cannot find:");

    Optional<String> result = dialog.showAndWait();
    result.ifPresent(message -> {
        // Save message for further analysis to identify missing help articles
        System.out.println("Specific message sent: " + message);
        showAlert("Message Sent", "Your request has been sent to the help system.");
    });
}



public static void main(String[] args) {
    launch(args); // Start
}
}
