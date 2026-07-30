package frontend;

import javafx.application.Application;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontWeight;
import javafx.stage.Stage;

import model.BuyerProfile;
import model.Category;
import model.Product;
import model.User;

import services.CategoryService;
import services.ProductService;
import services.UserService;
import services.buyerprofile;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class FarmersInFrontEnd extends Application {

    private Stage primaryStage;
    private BorderPane dashboardRoot;
    private VBox sidebar;
    private StackPane contentArea;
    private Label pageTitle;
    private String currentRole = "ADMIN";
    private User currentUser;

    /*
     * The frontend communicates only with service classes.
     * It never calls DAO classes directly.
     */
    private final UserService userService = new UserService();
    private final CategoryService categoryService = new CategoryService();
    private final ProductService productService = new ProductService();
    private final buyerprofile buyerProfileService = new buyerprofile();

    // Professional FarmersIn theme
    private static final String PRIMARY = "#1F7A3D";
    private static final String PRIMARY_DARK = "#14532D";
    private static final String PRIMARY_LIGHT = "#EAF6EE";
    private static final String BACKGROUND = "#F4F7F5";
    private static final String CARD = "#FFFFFF";
    private static final String TEXT = "#1F2937";
    private static final String MUTED = "#6B7280";
    private static final String BORDER = "#E5E7EB";
    private static final String WARNING = "#F59E0B";
    private static final String DANGER = "#DC2626";
    private static final String INFO = "#2563EB";

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;

        primaryStage.setTitle("FarmersIn");
        primaryStage.setMinWidth(1100);
        primaryStage.setMinHeight(700);
        primaryStage.setWidth(1450);
        primaryStage.setHeight(900);

        showLoginScreen();
        primaryStage.show();
        primaryStage.centerOnScreen();
    }

    // =========================================================
    // LOGIN SCREEN
    // =========================================================

    private void showLoginScreen() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BACKGROUND + ";");

        VBox leftPanel = new VBox(24);
        leftPanel.setAlignment(Pos.CENTER_LEFT);
        leftPanel.setPadding(new Insets(70));
        leftPanel.setPrefWidth(650);
        leftPanel.setStyle(
                "-fx-background-color: linear-gradient(to bottom right, " +
                        PRIMARY_DARK + ", " + PRIMARY + ");"
        );

        Label logo = new Label("🌿  FarmersIn");
        logo.setTextFill(Color.WHITE);
        logo.setFont(Font.font("Arial", FontWeight.BOLD, 34));

        Label heading = new Label("Fresh from farms,\ndirectly to your business.");
        heading.setTextFill(Color.WHITE);
        heading.setFont(Font.font("Arial", FontWeight.BOLD, 42));

        Label description = new Label(
                "A trusted digital marketplace connecting buyers,\n" +
                        "farmers and dairy suppliers across Pakistan."
        );
        description.setTextFill(Color.web("#DDEFE3"));
        description.setFont(Font.font("Arial", 18));

        VBox featureBox = new VBox(13);
        featureBox.getChildren().addAll(
                createLoginFeature("✓", "Verified suppliers"),
                createLoginFeature("✓", "Fresh agricultural products"),
                createLoginFeature("✓", "Secure orders and payments"),
                createLoginFeature("✓", "Direct communication")
        );

        leftPanel.getChildren().addAll(logo, heading, description, featureBox);

        VBox loginCard = new VBox(16);
        loginCard.setAlignment(Pos.CENTER_LEFT);
        loginCard.setPadding(new Insets(38));
        loginCard.setMaxWidth(420);
        loginCard.setStyle(cardStyle(18));
        loginCard.setEffect(createShadow());

        Label welcome = new Label("Welcome back");
        welcome.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        welcome.setTextFill(Color.web(TEXT));

        Label signInText = new Label("Sign in to continue to FarmersIn");
        signInText.setFont(Font.font("Arial", 14));
        signInText.setTextFill(Color.web(MUTED));

        Label emailLabel = fieldLabel("Email address");
        TextField emailField = createTextField("admin@gmail.com");

        Label passwordLabel = fieldLabel("Password");
        PasswordField passwordField = createPasswordField("admin123");

        Label roleLabel = fieldLabel("Account type");
        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("ADMIN", "BUYER", "SUPPLIER");
        roleBox.setValue("ADMIN");
        roleBox.setMaxWidth(Double.MAX_VALUE);
        roleBox.setPrefHeight(44);
        roleBox.setStyle(inputStyle());

        Label messageLabel = new Label();
        messageLabel.setTextFill(Color.web(DANGER));
        messageLabel.setWrapText(true);

        Button loginButton = createPrimaryButton("Sign In");
        loginButton.setMaxWidth(Double.MAX_VALUE);

        Button registerButton = createSecondaryButton("Create New Account");
        registerButton.setMaxWidth(Double.MAX_VALUE);

        loginButton.setOnAction(event -> {
            String email = emailField.getText().trim();
            String password = passwordField.getText();
            String selectedRole = roleBox.getValue();

            if (email.isEmpty() || password.isEmpty() || selectedRole == null) {
                messageLabel.setText("Please complete all fields.");
                return;
            }

            try {
                User user = userService.getUserByEmail(email);

                /*
                 * Your current UserService stores and reads passwordHash.
                 * This comparison assumes the registration screen is temporarily
                 * storing the entered password in that field.
                 * Replace this with BCrypt before final deployment.
                 */
                if (user.getPasswordHash() == null
                        || !user.getPasswordHash().equals(password)) {
                    messageLabel.setText("Invalid email or password.");
                    return;
                }

                if (!selectedRole.equalsIgnoreCase(user.getRole())) {
                    messageLabel.setText(
                            "This account is registered as " + user.getRole() + "."
                    );
                    return;
                }

                if ("BLOCKED".equalsIgnoreCase(user.getAccountStatus())
                        || "REJECTED".equalsIgnoreCase(user.getAccountStatus())) {
                    messageLabel.setText("This account is not allowed to sign in.");
                    return;
                }

                /*
                 * ADMIN and ACTIVE accounts can sign in.
                 * A newly registered BUYER/SUPPLIER remains PENDING until approved.
                 */
                if (!"ADMIN".equalsIgnoreCase(user.getRole())
                        && !"ACTIVE".equalsIgnoreCase(user.getAccountStatus())) {
                    messageLabel.setText(
                            "Your account is pending administrator approval."
                    );
                    return;
                }

                currentUser = user;
                currentRole = user.getRole().trim().toUpperCase();
                showDashboard();

            } catch (IllegalArgumentException e) {
                messageLabel.setText(e.getMessage());
            } catch (SQLException e) {
                messageLabel.setText("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        });

        registerButton.setOnAction(event -> showRegistrationScreen());

        loginCard.getChildren().addAll(
                welcome,
                signInText,
                createSpacer(5),
                emailLabel,
                emailField,
                passwordLabel,
                passwordField,
                roleLabel,
                roleBox,
                messageLabel,
                loginButton,
                registerButton
        );

        StackPane rightPanel = new StackPane(loginCard);
        rightPanel.setPadding(new Insets(60));

        root.setLeft(leftPanel);
        root.setCenter(rightPanel);

        primaryStage.setScene(new Scene(root));
    }

    private HBox createLoginFeature(String icon, String text) {
        Label iconLabel = new Label(icon);
        iconLabel.setTextFill(Color.web("#A7F3D0"));
        iconLabel.setFont(Font.font("Arial", FontWeight.BOLD, 18));

        Label textLabel = new Label(text);
        textLabel.setTextFill(Color.WHITE);
        textLabel.setFont(Font.font("Arial", 16));

        HBox row = new HBox(10, iconLabel, textLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    // =========================================================
    // REGISTRATION SCREEN
    // =========================================================

    private void showRegistrationScreen() {
        BorderPane root = new BorderPane();
        root.setStyle("-fx-background-color: " + BACKGROUND + ";");

        HBox top = new HBox();
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPadding(new Insets(20, 35, 20, 35));
        top.setStyle("-fx-background-color: white; -fx-border-color: transparent transparent " + BORDER + " transparent;");

        Label logo = new Label("🌿 FarmersIn");
        logo.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        logo.setTextFill(Color.web(PRIMARY_DARK));

        top.getChildren().add(logo);
        root.setTop(top);

        GridPane form = new GridPane();
        form.setHgap(18);
        form.setVgap(14);
        form.setPadding(new Insets(36));
        form.setMaxWidth(700);
        form.setStyle(cardStyle(18));
        form.setEffect(createShadow());

        Label title = new Label("Create your FarmersIn account");
        title.setFont(Font.font("Arial", FontWeight.BOLD, 28));
        title.setTextFill(Color.web(TEXT));

        Label subtitle = new Label("Register as a buyer or supplier");
        subtitle.setTextFill(Color.web(MUTED));

        TextField nameField = createTextField("Full name");
        TextField emailField = createTextField("Email address");
        PasswordField passwordField = createPasswordField("Password");
        TextField phoneField = createTextField("Phone number");

        ComboBox<String> roleBox = new ComboBox<>();
        roleBox.getItems().addAll("BUYER", "SUPPLIER");
        roleBox.setPromptText("Select account type");
        roleBox.setPrefHeight(44);
        roleBox.setMaxWidth(Double.MAX_VALUE);
        roleBox.setStyle(inputStyle());

        TextField cityField = createTextField("City");
        TextArea addressArea = new TextArea();
        addressArea.setPromptText("Complete address");
        addressArea.setPrefRowCount(3);
        addressArea.setStyle(inputStyle());

        Label message = new Label();
        message.setWrapText(true);

        Button register = createPrimaryButton("Create Account");
        Button back = createSecondaryButton("Back to Login");

        register.setMaxWidth(Double.MAX_VALUE);
        back.setMaxWidth(Double.MAX_VALUE);

        register.setOnAction(event -> {
            if (nameField.getText().trim().isEmpty()
                    || emailField.getText().trim().isEmpty()
                    || passwordField.getText().isEmpty()
                    || phoneField.getText().trim().isEmpty()
                    || roleBox.getValue() == null
                    || cityField.getText().trim().isEmpty()) {

                message.setTextFill(Color.web(DANGER));
                message.setText("Please complete all required fields.");
                return;
            }

            try {
                User user = new User();
                user.setFullName(nameField.getText().trim());
                user.setEmail(emailField.getText().trim());
                user.setPasswordHash(passwordField.getText());
                user.setPhone(phoneField.getText().trim());
                user.setRole(roleBox.getValue());

                boolean created = userService.addUser(user);

                if (created) {
                    message.setTextFill(Color.web(PRIMARY));
                    message.setText(
                            "Account created successfully. " +
                                    "The account is pending administrator approval."
                    );

                    nameField.clear();
                    emailField.clear();
                    passwordField.clear();
                    phoneField.clear();
                    roleBox.setValue(null);
                    cityField.clear();
                    addressArea.clear();
                } else {
                    message.setTextFill(Color.web(DANGER));
                    message.setText("Account could not be created.");
                }

            } catch (IllegalArgumentException e) {
                message.setTextFill(Color.web(DANGER));
                message.setText(e.getMessage());
            } catch (SQLException e) {
                message.setTextFill(Color.web(DANGER));
                message.setText("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        });

        back.setOnAction(event -> showLoginScreen());

        form.add(title, 0, 0, 2, 1);
        form.add(subtitle, 0, 1, 2, 1);
        form.add(fieldLabel("Full name"), 0, 2);
        form.add(fieldLabel("Email address"), 1, 2);
        form.add(nameField, 0, 3);
        form.add(emailField, 1, 3);
        form.add(fieldLabel("Password"), 0, 4);
        form.add(fieldLabel("Phone number"), 1, 4);
        form.add(passwordField, 0, 5);
        form.add(phoneField, 1, 5);
        form.add(fieldLabel("Account type"), 0, 6);
        form.add(fieldLabel("City"), 1, 6);
        form.add(roleBox, 0, 7);
        form.add(cityField, 1, 7);
        form.add(fieldLabel("Address"), 0, 8, 2, 1);
        form.add(addressArea, 0, 9, 2, 1);
        form.add(message, 0, 10, 2, 1);
        form.add(register, 0, 11);
        form.add(back, 1, 11);

        ColumnConstraints col1 = new ColumnConstraints();
        col1.setPercentWidth(50);
        ColumnConstraints col2 = new ColumnConstraints();
        col2.setPercentWidth(50);
        form.getColumnConstraints().addAll(col1, col2);

        StackPane center = new StackPane(form);
        center.setPadding(new Insets(45));
        root.setCenter(center);

        primaryStage.setScene(new Scene(root));
    }

    // =========================================================
    // DASHBOARD SHELL
    // =========================================================

    private void showDashboard() {
        dashboardRoot = new BorderPane();
        dashboardRoot.setStyle("-fx-background-color: " + BACKGROUND + ";");

        sidebar = createSidebar();
        VBox topBar = createTopBar();

        contentArea = new StackPane();
        contentArea.setPadding(new Insets(0));
        contentArea.setStyle("-fx-background-color: " + BACKGROUND + ";");

        dashboardRoot.setLeft(sidebar);
        dashboardRoot.setTop(topBar);
        dashboardRoot.setCenter(contentArea);

        showHomePage();

        Scene scene = new Scene(dashboardRoot);
        primaryStage.setScene(scene);
        primaryStage.setMaximized(true);
    }

    private VBox createSidebar() {
        VBox side = new VBox(8);
        side.setPrefWidth(245);
        side.setPadding(new Insets(24, 18, 20, 18));
        side.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: transparent " + BORDER + " transparent transparent;"
        );

        Label logo = new Label("🌿 FarmersIn");
        logo.setFont(Font.font("Arial", FontWeight.BOLD, 26));
        logo.setTextFill(Color.web(PRIMARY_DARK));
        logo.setPadding(new Insets(0, 8, 24, 8));

        side.getChildren().add(logo);

        addMenuButton(side, "⌂", "Home", this::showHomePage);
        addMenuButton(side, "▣", "Products", this::showProductsPage);
        addMenuButton(side, "▦", "Categories", this::showCategoriesPage);

        if (!"BUYER".equals(currentRole)) {
            addMenuButton(side, "♙", "Suppliers", this::showSuppliersPage);
        }

        addMenuButton(side, "▤", "Orders", this::showOrdersPage);
        addMenuButton(side, "☆", "Reviews", this::showReviewsPage);
        addMenuButton(side, "!", "Complaints", this::showComplaintsPage);

        if ("ADMIN".equals(currentRole)) {
            addMenuButton(side, "✓", "Buyer Requests",
                    this::showPendingBuyerRequestsPage);
            addMenuButton(side, "↶", "Refunds", this::showRefundsPage);
            addMenuButton(side, "▥", "Reports", this::showReportsPage);
            addMenuButton(side, "♙", "Users", this::showUsersPage);
            addMenuButton(side, "▧", "Admin Logs", this::showAdminLogsPage);
        }

        if ("SUPPLIER".equals(currentRole)) {
            addMenuButton(side, "+", "Add Product", this::showAddProductPage);
            addMenuButton(side, "▤", "Deliveries", this::showDeliveriesPage);
        }

        if ("BUYER".equals(currentRole)) {
            addMenuButton(side, "🛒", "My Cart", this::showCartPage);
            addMenuButton(side, "✉", "Messages", this::showMessagesPage);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        VBox promo = new VBox(10);
        promo.setPadding(new Insets(16));
        promo.setStyle(
                "-fx-background-color: " + PRIMARY_LIGHT + ";" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-color: #CFE8D7;" +
                        "-fx-border-radius: 14;"
        );

        Label promoTitle = new Label("Grow Your Business");
        promoTitle.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        promoTitle.setTextFill(Color.web(PRIMARY_DARK));

        Label promoText = new Label(
                "List products and reach buyers across the country."
        );
        promoText.setWrapText(true);
        promoText.setTextFill(Color.web(MUTED));

        Button promoButton = createPrimaryButton("Get Started");
        promoButton.setPrefHeight(34);

        if ("SUPPLIER".equals(currentRole)) {
            promoButton.setOnAction(event -> showAddProductPage());
        } else {
            promoButton.setOnAction(event -> showProductsPage());
        }

        promo.getChildren().addAll(promoTitle, promoText, promoButton);

        Button logout = createMenuButton("⏻", "Logout");
        logout.setOnAction(event -> {
            currentUser = null;
            currentRole = "ADMIN";
            primaryStage.setMaximized(false);
            showLoginScreen();
        });

        side.getChildren().addAll(spacer, promo, createSpacer(4), logout);
        return side;
    }

    private void addMenuButton(VBox side, String icon, String text, Runnable action) {
        Button button = createMenuButton(icon, text);
        button.setOnAction(event -> action.run());
        side.getChildren().add(button);
    }

    private Button createMenuButton(String icon, String text) {
        Button button = new Button(icon + "   " + text);
        button.setAlignment(Pos.CENTER_LEFT);
        button.setPrefWidth(Double.MAX_VALUE);
        button.setPrefHeight(43);
        button.setCursor(Cursor.HAND);
        button.setFont(Font.font("Arial", 14));
        button.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: " + TEXT + ";" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 0 14;"
        );

        button.setOnMouseEntered(event -> button.setStyle(
                "-fx-background-color: " + PRIMARY_LIGHT + ";" +
                        "-fx-text-fill: " + PRIMARY_DARK + ";" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 0 14;"
        ));

        button.setOnMouseExited(event -> button.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: " + TEXT + ";" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 0 14;"
        ));

        return button;
    }

    private VBox createTopBar() {
        HBox row = new HBox(16);
        row.setAlignment(Pos.CENTER_LEFT);
        row.setPadding(new Insets(15, 25, 15, 25));
        row.setStyle(
                "-fx-background-color: white;" +
                        "-fx-border-color: transparent transparent " + BORDER + " transparent;"
        );

        Button menuButton = createIconButton("☰");

        TextField search = new TextField();
        search.setPromptText("Search products, suppliers, categories...");
        search.setPrefHeight(42);
        search.setMaxWidth(620);
        search.setStyle(
                "-fx-background-color: #F8FAF9;" +
                        "-fx-background-radius: 20;" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-radius: 20;" +
                        "-fx-padding: 0 18;" +
                        "-fx-font-size: 14px;"
        );
        HBox.setHgrow(search, Priority.ALWAYS);

        search.setOnAction(event -> {
            String searchText = search.getText().trim();
            if (searchText.isEmpty()) {
                showProductsPage();
            } else {
                showProductSearchResults(searchText);
            }
        });

        Button notification = createIconButton("🔔");
        Button message = createIconButton("✉");

        VBox userInfo = new VBox(2);
        Label userName = new Label(
                "ADMIN".equals(currentRole) ? "Admin User"
                        : "BUYER".equals(currentRole) ? "Buyer Account"
                          : "Supplier Account"
        );
        userName.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        Label roleLabel = new Label(capitalize(currentRole));
        roleLabel.setTextFill(Color.web(MUTED));
        roleLabel.setFont(Font.font("Arial", 12));
        userInfo.getChildren().addAll(userName, roleLabel);

        Label avatar = new Label("👤");
        avatar.setAlignment(Pos.CENTER);
        avatar.setPrefSize(42, 42);
        avatar.setStyle(
                "-fx-background-color: " + PRIMARY_LIGHT + ";" +
                        "-fx-background-radius: 21;" +
                        "-fx-font-size: 19px;"
        );

        row.getChildren().addAll(
                menuButton,
                search,
                notification,
                message,
                avatar,
                userInfo
        );

        pageTitle = new Label("Dashboard");
        pageTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));
        pageTitle.setTextFill(Color.web(TEXT));

        HBox titleRow = new HBox(pageTitle);
        titleRow.setPadding(new Insets(14, 28, 12, 28));
        titleRow.setStyle("-fx-background-color: " + BACKGROUND + ";");

        VBox wrapper = new VBox(row, titleRow);
        return wrapper;
    }

    // =========================================================
    // HOME PAGE
    // =========================================================

    private void showHomePage() {
        setPageTitle("Dashboard");

        VBox content = new VBox(22);
        content.setPadding(new Insets(5, 28, 28, 28));

        HBox topSection = new HBox(20);
        topSection.getChildren().addAll(createHeroBanner(), createOverviewPanel());
        HBox.setHgrow(topSection.getChildren().get(0), Priority.ALWAYS);

        VBox categoriesSection = createCategoriesSection();
        VBox productsSection = createProductSection();

        content.getChildren().addAll(topSection, categoriesSection, productsSection);

        ScrollPane scroll = createScrollPane(content);
        setContent(scroll);
    }

    private Pane createHeroBanner() {
        VBox hero = new VBox(18);
        hero.setPadding(new Insets(38));
        hero.setPrefHeight(360);
        hero.setMinWidth(600);
        hero.setStyle(
                "-fx-background-color: linear-gradient(to right, " +
                        PRIMARY_DARK + ", " + PRIMARY + ");" +
                        "-fx-background-radius: 18;"
        );

        Label title = new Label("Fresh From Farms\nto Your Business");
        title.setTextFill(Color.WHITE);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 38));

        Label description = new Label(
                "Your trusted marketplace for high-quality\n" +
                        "agricultural products from verified suppliers."
        );
        description.setTextFill(Color.web("#E3F4E8"));
        description.setFont(Font.font("Arial", 17));

        Button explore = new Button("Explore Products  →");
        explore.setCursor(Cursor.HAND);
        explore.setPrefHeight(48);
        explore.setStyle(
                "-fx-background-color: #48A83E;" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 15px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 0 22;"
        );
        explore.setOnAction(event -> showProductsPage());

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);

        HBox qualities = new HBox(30);
        qualities.getChildren().addAll(
                createHeroQuality("✓", "Quality\nGuaranteed"),
                createHeroQuality("★", "Verified\nSuppliers"),
                createHeroQuality("▣", "Secure\nTransactions")
        );

        hero.getChildren().addAll(title, description, explore, spacer, qualities);
        HBox.setHgrow(hero, Priority.ALWAYS);
        return hero;
    }

    private VBox createHeroQuality(String icon, String text) {
        Label iconLabel = new Label(icon);
        iconLabel.setTextFill(Color.web("#79D45B"));
        iconLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        Label textLabel = new Label(text);
        textLabel.setTextFill(Color.WHITE);
        textLabel.setFont(Font.font("Arial", 13));

        VBox box = new VBox(5, iconLabel, textLabel);
        return box;
    }

    private VBox createOverviewPanel() {
        VBox panel = new VBox(12);
        panel.setPadding(new Insets(22));
        panel.setPrefWidth(340);
        panel.setStyle(cardStyle(16));
        panel.setEffect(createShadow());

        Label heading = new Label("Overview");
        heading.setFont(Font.font("Arial", FontWeight.BOLD, 19));

        int productCount = 0;
        int supplierCount = 0;

        try {
            productCount = productService.getAllActiveProducts().size();
            supplierCount = userService.getUsersByRole("SUPPLIER").size();
        } catch (SQLException | IllegalArgumentException e) {
            /*
             * Keep zero values when the database cannot be read.
             * The error is printed for debugging without crashing the dashboard.
             */
            e.printStackTrace();
        }

        panel.getChildren().addAll(
                heading,
                createSeparator(),
                createOverviewRow(
                        "🛍", "Total Products",
                        String.valueOf(productCount), "", PRIMARY
                ),
                createSeparator(),
                createOverviewRow(
                        "♙", "Total Suppliers",
                        String.valueOf(supplierCount), "", INFO
                ),
                createSeparator(),
                createOverviewRow("🛒", "Total Orders", "0", "", WARNING),
                createSeparator(),
                createOverviewRow("☆", "Average Rating", "0.0", "", "#7C3AED")
        );

        return panel;
    }

    private HBox createOverviewRow(String icon, String label, String value, String change, String color) {
        Label iconLabel = new Label(icon);
        iconLabel.setAlignment(Pos.CENTER);
        iconLabel.setPrefSize(54, 54);
        iconLabel.setFont(Font.font("Arial", 22));
        iconLabel.setStyle(
                "-fx-background-color: derive(" + color + ", 85%);" +
                        "-fx-background-radius: 12;"
        );

        VBox values = new VBox(4);
        Label labelText = new Label(label);
        labelText.setTextFill(Color.web(MUTED));
        labelText.setFont(Font.font("Arial", 12));

        Label valueText = new Label(value);
        valueText.setFont(Font.font("Arial", FontWeight.BOLD, 21));
        values.getChildren().addAll(labelText, valueText);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label changeLabel = new Label(change);
        changeLabel.setTextFill(Color.web(PRIMARY));
        changeLabel.setFont(Font.font("Arial", FontWeight.BOLD, 12));

        HBox row = new HBox(14, iconLabel, values, spacer, changeLabel);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private VBox createCategoriesSection() {
        VBox section = new VBox(12);
        section.getChildren().add(
                createSectionHeader("Categories", this::showCategoriesPage)
        );

        TilePane tiles = new TilePane();
        tiles.setHgap(12);
        tiles.setVgap(12);
        tiles.setPrefColumns(8);

        try {
            List<Category> categories = categoryService.getActiveCategories();

            if (categories.isEmpty()) {
                tiles.getChildren().add(
                        createInlineEmptyLabel("No categories available.")
                );
            } else {
                for (Category category : categories) {
                    tiles.getChildren().add(
                            createCategoryCard(
                                    categoryIcon(category.getCategoryName()),
                                    category.getCategoryName()
                            )
                    );
                }
            }
        } catch (SQLException e) {
            tiles.getChildren().add(
                    createInlineEmptyLabel(
                            "Unable to load categories: " + e.getMessage()
                    )
            );
        }

        section.getChildren().add(tiles);
        return section;
    }

    private VBox createProductSection() {
        VBox section = new VBox(12);
        section.getChildren().add(
                createSectionHeader("Recent Products", this::showProductsPage)
        );

        HBox productsBox = new HBox(14);

        try {
            List<Product> products = productService.getAllActiveProducts();

            if (products.isEmpty()) {
                productsBox.getChildren().add(
                        createInlineEmptyLabel("No products available.")
                );
            } else {
                int limit = Math.min(products.size(), 5);

                for (int i = 0; i < limit; i++) {
                    Product product = products.get(i);

                    productsBox.getChildren().add(
                            createProductCard(
                                    productIcon(product.getProductName()),
                                    product.getProductName(),
                                    "Approved product",
                                    formatUnit(product.getDefaultUnit()),
                                    "0.0"
                            )
                    );
                }
            }
        } catch (SQLException e) {
            productsBox.getChildren().add(
                    createInlineEmptyLabel(
                            "Unable to load products: " + e.getMessage()
                    )
            );
        }

        section.getChildren().add(productsBox);
        return section;
    }

    private HBox createSectionHeader(String title, Runnable action) {
        Label label = new Label(title);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 19));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button viewAll = new Button("View All");
        viewAll.setCursor(Cursor.HAND);
        viewAll.setStyle(
                "-fx-background-color: transparent;" +
                        "-fx-text-fill: " + PRIMARY + ";" +
                        "-fx-font-weight: bold;"
        );
        viewAll.setOnAction(event -> action.run());

        HBox row = new HBox(label, spacer, viewAll);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private VBox createCategoryCard(String icon, String title) {
        VBox card = new VBox(8);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(15));
        card.setPrefWidth(135);
        card.setPrefHeight(105);
        card.setCursor(Cursor.HAND);
        card.setStyle(cardStyle(12));
        card.setEffect(createSmallShadow());

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Arial", 27));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", 12));
        titleLabel.setWrapText(true);
        titleLabel.setAlignment(Pos.CENTER);

        card.getChildren().addAll(iconLabel, titleLabel);

        card.setOnMouseClicked(event -> showCategoryProductsPage(title));

        card.setOnMouseEntered(event -> card.setStyle(
                "-fx-background-color: " + PRIMARY_LIGHT + ";" +
                        "-fx-background-radius: 12;" +
                        "-fx-border-color: #BFD9C8;" +
                        "-fx-border-radius: 12;"
        ));

        card.setOnMouseExited(event -> card.setStyle(cardStyle(12)));

        return card;
    }

    private VBox createProductCard(String icon, String name, String supplier, String price, String rating) {
        VBox card = new VBox(8);
        card.setPadding(new Insets(13));
        card.setPrefWidth(210);
        card.setStyle(cardStyle(14));
        card.setEffect(createSmallShadow());

        StackPane imageArea = new StackPane();
        imageArea.setPrefHeight(105);
        imageArea.setStyle(
                "-fx-background-color: " + PRIMARY_LIGHT + ";" +
                        "-fx-background-radius: 10;"
        );

        Label productIcon = new Label(icon);
        productIcon.setFont(Font.font("Arial", 54));
        imageArea.getChildren().add(productIcon);

        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        Label supplierLabel = new Label(supplier);
        supplierLabel.setTextFill(Color.web(MUTED));
        supplierLabel.setFont(Font.font("Arial", 12));

        Label priceLabel = new Label(price);
        priceLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        Label stockLabel = new Label("In Stock");
        stockLabel.setTextFill(Color.web(PRIMARY));
        stockLabel.setFont(Font.font("Arial", 12));

        HBox footer = new HBox();
        footer.setAlignment(Pos.CENTER_LEFT);

        Label ratingLabel = new Label("★ " + rating);
        ratingLabel.setTextFill(Color.web(WARNING));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button cartButton = createIconButton("🛒");
        cartButton.setOnAction(event -> showInfo("Product added to cart."));

        footer.getChildren().addAll(ratingLabel, spacer, cartButton);

        card.getChildren().addAll(
                imageArea, nameLabel, supplierLabel, priceLabel, stockLabel, footer
        );

        return card;
    }

    // =========================================================
    // PRODUCTS PAGE
    // =========================================================

    private void showProductsPage() {
        setPageTitle("Products");

        VBox content = createPageContainer();

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        TextField search = createTextField("Search products...");
        search.setPrefWidth(350);

        ComboBox<String> categoryFilter = new ComboBox<>();
        categoryFilter.getItems().add("All Categories");
        categoryFilter.setValue("All Categories");
        categoryFilter.setPrefHeight(44);
        categoryFilter.setStyle(inputStyle());

        try {
            for (Category category : categoryService.getActiveCategories()) {
                categoryFilter.getItems().add(category.getCategoryName());
            }
        } catch (SQLException e) {
            e.printStackTrace();
        }

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Button add = createPrimaryButton("+ Add Product");
        add.setVisible(!"BUYER".equals(currentRole));
        add.setManaged(!"BUYER".equals(currentRole));
        add.setOnAction(event -> showAddProductPage());

        header.getChildren().addAll(search, categoryFilter, spacer, add);

        TilePane productTiles = new TilePane();
        productTiles.setHgap(16);
        productTiles.setVgap(16);
        productTiles.setPrefColumns(4);

        Runnable reloadProducts = () -> {
            productTiles.getChildren().clear();

            try {
                List<Product> products;

                if ("All Categories".equals(categoryFilter.getValue())) {
                    products = productService.getAllActiveProducts();
                } else {
                    Category selectedCategory =
                            categoryService.getCategoryByName(
                                    categoryFilter.getValue()
                            );

                    products = productService.getProductsByCategoryId(
                            selectedCategory.getCategoryId()
                    );
                }

                String searchText = search.getText().trim().toLowerCase();

                for (Product product : products) {
                    if (!searchText.isEmpty()
                            && !product.getProductName()
                            .toLowerCase()
                            .contains(searchText)) {
                        continue;
                    }

                    productTiles.getChildren().add(
                            createLargeProductCard(
                                    productIcon(product.getProductName()),
                                    product.getProductName(),
                                    categoryNameFor(product.getCategoryId()),
                                    "FarmersIn",
                                    formatUnit(product.getDefaultUnit())
                            )
                    );
                }

                if (productTiles.getChildren().isEmpty()) {
                    productTiles.getChildren().add(
                            createInlineEmptyLabel("No products found.")
                    );
                }

            } catch (SQLException | IllegalArgumentException e) {
                productTiles.getChildren().add(
                        createInlineEmptyLabel(
                                "Unable to load products: " + e.getMessage()
                        )
                );
            }
        };

        search.textProperty().addListener(
                (observable, oldValue, newValue) -> reloadProducts.run()
        );

        categoryFilter.valueProperty().addListener(
                (observable, oldValue, newValue) -> reloadProducts.run()
        );

        reloadProducts.run();

        content.getChildren().addAll(header, productTiles);
        setContent(createScrollPane(content));
    }

    private VBox createLargeProductCard(String icon, String name, String category,
                                        String supplier, String price) {
        VBox card = new VBox(10);
        card.setPadding(new Insets(15));
        card.setPrefWidth(260);
        card.setStyle(cardStyle(14));
        card.setEffect(createSmallShadow());

        StackPane image = new StackPane();
        image.setPrefHeight(145);
        image.setStyle(
                "-fx-background-color: " + PRIMARY_LIGHT + ";" +
                        "-fx-background-radius: 12;"
        );

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Arial", 68));
        image.getChildren().add(iconLabel);

        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        Label categoryLabel = new Label(category);
        categoryLabel.setStyle(
                "-fx-background-color: " + PRIMARY_LIGHT + ";" +
                        "-fx-text-fill: " + PRIMARY_DARK + ";" +
                        "-fx-background-radius: 10;" +
                        "-fx-padding: 4 9;"
        );

        Label supplierLabel = new Label(supplier);
        supplierLabel.setTextFill(Color.web(MUTED));

        Label priceLabel = new Label(price);
        priceLabel.setFont(Font.font("Arial", FontWeight.BOLD, 17));

        Button action = createPrimaryButton(
                "BUYER".equals(currentRole) ? "Add to Cart" : "View Details"
        );
        action.setMaxWidth(Double.MAX_VALUE);
        action.setOnAction(event -> showInfo(
                "BUYER".equals(currentRole)
                        ? name + " added to cart."
                        : "Opening details for " + name + "."
        ));

        card.getChildren().addAll(
                image, nameLabel, categoryLabel, supplierLabel, priceLabel, action
        );

        return card;
    }

    private void showCategoryProductsPage(String categoryName) {
        setPageTitle(categoryName);

        VBox content = createPageContainer();

        HBox header = new HBox(12);
        header.setAlignment(Pos.CENTER_LEFT);

        Button backButton = createSecondaryButton("← Back to Categories");
        backButton.setOnAction(event -> showCategoriesPage());

        Label title = new Label(categoryName);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 22));

        header.getChildren().addAll(backButton, title);

        TilePane productTiles = new TilePane();
        productTiles.setHgap(16);
        productTiles.setVgap(16);
        productTiles.setPrefColumns(4);

        try {
            Category category = categoryService.getCategoryByName(categoryName);

            List<Product> products =
                    productService.getProductsByCategoryId(
                            category.getCategoryId()
                    );

            for (Product product : products) {
                productTiles.getChildren().add(
                        createLargeProductCard(
                                productIcon(product.getProductName()),
                                product.getProductName(),
                                categoryName,
                                "FarmersIn",
                                formatUnit(product.getDefaultUnit())
                        )
                );
            }

            if (products.isEmpty()) {
                content.getChildren().addAll(
                        header,
                        createEmptyState(
                                "No products available",
                                "Products for " + categoryName +
                                        " will appear here after they are added."
                        )
                );
            } else {
                content.getChildren().addAll(header, productTiles);
            }

        } catch (SQLException | IllegalArgumentException e) {
            content.getChildren().addAll(
                    header,
                    createEmptyState(
                            "Unable to load products",
                            e.getMessage()
                    )
            );
        }

        setContent(createScrollPane(content));
    }

    // =========================================================
    // GENERIC DATA PAGES
    // =========================================================

    private void showCategoriesPage() {
        setPageTitle("Categories");

        VBox content = createPageContainer();

        HBox header = createTitleActionRow(
                "Manage Product Categories",
                "ADMIN".equals(currentRole) ? "+ Add Category" : null,
                this::showAddCategoryDialog
        );

        TilePane categoriesPane = new TilePane();
        categoriesPane.setHgap(18);
        categoriesPane.setVgap(18);
        categoriesPane.setPrefColumns(4);

        try {
            List<Category> categories = categoryService.getAllCategories();

            for (Category category : categories) {
                int productCount =
                        productService.getProductsByCategoryId(
                                category.getCategoryId()
                        ).size();

                categoriesPane.getChildren().add(
                        createCategoryManagementCard(
                                categoryIcon(category.getCategoryName()),
                                category.getCategoryName(),
                                productCount + " products"
                        )
                );
            }

            if (categories.isEmpty()) {
                categoriesPane.getChildren().add(
                        createInlineEmptyLabel("No categories available.")
                );
            }

        } catch (SQLException e) {
            categoriesPane.getChildren().add(
                    createInlineEmptyLabel(
                            "Unable to load categories: " + e.getMessage()
                    )
            );
        }

        content.getChildren().addAll(header, categoriesPane);
        setContent(createScrollPane(content));
    }

    private VBox createCategoryManagementCard(String icon, String name, String count) {
        VBox card = new VBox(10);
        card.setAlignment(Pos.CENTER);
        card.setPadding(new Insets(22));
        card.setPrefWidth(230);
        card.setPrefHeight(170);
        card.setCursor(Cursor.HAND);
        card.setStyle(cardStyle(14));
        card.setEffect(createSmallShadow());

        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Arial", 42));

        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        Label countLabel = new Label(count);
        countLabel.setTextFill(Color.web(MUTED));

        card.getChildren().addAll(iconLabel, nameLabel, countLabel);

        card.setOnMouseClicked(event -> showCategoryProductsPage(name));

        card.setOnMouseEntered(event -> card.setStyle(
                "-fx-background-color: " + PRIMARY_LIGHT + ";" +
                        "-fx-background-radius: 14;" +
                        "-fx-border-color: #BFD9C8;" +
                        "-fx-border-radius: 14;"
        ));

        card.setOnMouseExited(event -> card.setStyle(cardStyle(14)));

        return card;
    }

    private void showSuppliersPage() {
        setPageTitle("Suppliers");
        VBox content = createPageContainer();
        content.getChildren().add(
                createEmptyState(
                        "No suppliers available",
                        "Supplier profiles will appear here after registration and verification."
                )
        );
        setContent(createScrollPane(content));
    }

    private void showOrdersPage() {
        setPageTitle("Orders");
        VBox content = createPageContainer();
        content.getChildren().add(
                createEmptyState(
                        "No orders available",
                        "Orders will appear here after buyers place them."
                )
        );
        setContent(createScrollPane(content));
    }

    private void showUsersPage() {
        setPageTitle("Users");

        VBox content = createPageContainer();
        VBox list = new VBox(12);

        try {
            List<User> users = new ArrayList<>();
            users.addAll(userService.getUsersByRole("ADMIN"));
            users.addAll(userService.getUsersByRole("BUYER"));
            users.addAll(userService.getUsersByRole("SUPPLIER"));

            for (User user : users) {
                HBox card = new HBox(16);
                card.setAlignment(Pos.CENTER_LEFT);
                card.setPadding(new Insets(18));
                card.setStyle(cardStyle(14));

                Label avatar = new Label("👤");
                avatar.setAlignment(Pos.CENTER);
                avatar.setPrefSize(46, 46);
                avatar.setStyle(
                        "-fx-background-color: " + PRIMARY_LIGHT + ";" +
                                "-fx-background-radius: 23;"
                );

                VBox information = new VBox(4);

                Label name = new Label(user.getFullName());
                name.setFont(Font.font("Arial", FontWeight.BOLD, 15));

                Label email = new Label(user.getEmail());
                email.setTextFill(Color.web(MUTED));

                information.getChildren().addAll(name, email);

                Region spacer = new Region();
                HBox.setHgrow(spacer, Priority.ALWAYS);

                Label role = new Label(user.getRole());
                role.setStyle(statusStyle(INFO));

                Label status = new Label(user.getAccountStatus());
                status.setStyle(
                        statusStyle(
                                "ACTIVE".equalsIgnoreCase(
                                        user.getAccountStatus()
                                ) ? PRIMARY : WARNING
                        )
                );

                card.getChildren().addAll(
                        avatar, information, spacer, role, status
                );

                list.getChildren().add(card);
            }

            if (users.isEmpty()) {
                list.getChildren().add(
                        createEmptyState(
                                "No users available",
                                "Registered users will appear here."
                        )
                );
            }

        } catch (SQLException e) {
            list.getChildren().add(
                    createEmptyState(
                            "Unable to load users",
                            e.getMessage()
                    )
            );
        }

        content.getChildren().add(list);
        setContent(createScrollPane(content));
    }


    private void showPendingBuyerRequestsPage() {
        setPageTitle("Pending Buyer Requests");

        VBox content = createPageContainer();
        VBox requestsBox = new VBox(12);

        try {
            List<BuyerProfile> pendingBuyers =
                    buyerProfileService.getPendingVerifications();

            if (pendingBuyers.isEmpty()) {
                requestsBox.getChildren().add(
                        createEmptyState(
                                "No pending buyer requests",
                                "New buyer verification requests will appear here."
                        )
                );
            } else {
                for (BuyerProfile profile : pendingBuyers) {
                    HBox card = new HBox(18);
                    card.setAlignment(Pos.CENTER_LEFT);
                    card.setPadding(new Insets(18));
                    card.setStyle(cardStyle(14));
                    card.setEffect(createSmallShadow());

                    Label icon = new Label("👤");
                    icon.setAlignment(Pos.CENTER);
                    icon.setPrefSize(48, 48);
                    icon.setStyle(
                            "-fx-background-color: " + PRIMARY_LIGHT + ";" +
                                    "-fx-background-radius: 24;" +
                                    "-fx-font-size: 20px;"
                    );

                    VBox information = new VBox(5);

                    Label businessName =
                            new Label(profile.getBusinessName());
                    businessName.setFont(
                            Font.font("Arial", FontWeight.BOLD, 16)
                    );

                    Label businessType = new Label(
                            "Business type: " + profile.getBusinessType()
                    );
                    businessType.setTextFill(Color.web(MUTED));

                    Label userId = new Label(
                            "User ID: " + profile.getUserId()
                    );
                    userId.setTextFill(Color.web(MUTED));

                    Label status = new Label(
                            "Status: " + profile.getVerificationStatus()
                    );
                    status.setTextFill(Color.web(WARNING));

                    information.getChildren().addAll(
                            businessName, businessType, userId, status
                    );

                    Region spacer = new Region();
                    HBox.setHgrow(spacer, Priority.ALWAYS);

                    Button approveButton =
                            createPrimaryButton("Approve");
                    Button rejectButton =
                            createSecondaryButton("Reject");

                    approveButton.setOnAction(event -> {
                        try {
                            boolean approved =
                                    buyerProfileService
                                            .updateVerificationStatus(
                                                    profile.getBuyerId(),
                                                    "VERIFIED"
                                            );

                            if (approved) {
                                userService.updateAccountStatus(
                                        profile.getUserId(),
                                        "ACTIVE"
                                );
                                showInfo("Buyer approved successfully.");
                                showPendingBuyerRequestsPage();
                            }
                        } catch (IllegalArgumentException e) {
                            showError(e.getMessage());
                        } catch (SQLException e) {
                            showError("Database error: " + e.getMessage());
                            e.printStackTrace();
                        }
                    });

                    rejectButton.setOnAction(event -> {
                        try {
                            boolean rejected =
                                    buyerProfileService
                                            .updateVerificationStatus(
                                                    profile.getBuyerId(),
                                                    "REJECTED"
                                            );

                            if (rejected) {
                                userService.updateAccountStatus(
                                        profile.getUserId(),
                                        "REJECTED"
                                );
                                showInfo("Buyer request rejected.");
                                showPendingBuyerRequestsPage();
                            }
                        } catch (IllegalArgumentException e) {
                            showError(e.getMessage());
                        } catch (SQLException e) {
                            showError("Database error: " + e.getMessage());
                            e.printStackTrace();
                        }
                    });

                    card.getChildren().addAll(
                            icon, information, spacer,
                            approveButton, rejectButton
                    );
                    requestsBox.getChildren().add(card);
                }
            }

        } catch (SQLException e) {
            requestsBox.getChildren().add(
                    createEmptyState(
                            "Unable to load buyer requests",
                            e.getMessage()
                    )
            );
            e.printStackTrace();
        }

        content.getChildren().add(requestsBox);
        setContent(createScrollPane(content));
    }

    private void showReviewsPage() {
        setPageTitle("Reviews");
        VBox content = createPageContainer();
        content.getChildren().add(
                createEmptyState(
                        "No reviews yet",
                        "Customer reviews will appear here after completed orders."
                )
        );
        setContent(createScrollPane(content));
    }

    private void showComplaintsPage() {
        setPageTitle("Complaints");
        VBox content = createPageContainer();
        content.getChildren().add(
                createEmptyState(
                        "No complaints available",
                        "Submitted complaints will appear here."
                )
        );
        setContent(createScrollPane(content));
    }

    private void showRefundsPage() {
        setPageTitle("Refunds");
        VBox content = createPageContainer();
        content.getChildren().add(
                createEmptyState(
                        "No refund requests",
                        "Refund requests will appear here when submitted."
                )
        );
        setContent(createScrollPane(content));
    }

    private void showReportsPage() {
        setPageTitle("Reports");

        VBox content = createPageContainer();
        HBox metrics = new HBox(16,
                createMetricCard("Monthly Revenue", "Rs. 0", "↗", PRIMARY),
                createMetricCard("Orders This Month", "0", "▤", INFO),
                createMetricCard("New Suppliers", "0", "♙", WARNING),
                createMetricCard("Refund Rate", "0%", "↶", DANGER)
        );

        VBox chart = new VBox(18);
        chart.setPadding(new Insets(25));
        chart.setPrefHeight(420);
        chart.setStyle(cardStyle(16));

        Label chartTitle = new Label("Sales Overview");
        chartTitle.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        HBox bars = new HBox(25);
        bars.setAlignment(Pos.BOTTOM_CENTER);
        bars.setPadding(new Insets(35));

        String[] months = {"Jan", "Feb", "Mar", "Apr", "May", "Jun"};
        int[] heights = {5, 5, 5, 5, 5, 5};

        for (int i = 0; i < months.length; i++) {
            VBox barBox = new VBox(8);
            barBox.setAlignment(Pos.BOTTOM_CENTER);

            Region bar = new Region();
            bar.setPrefSize(45, heights[i]);
            bar.setStyle(
                    "-fx-background-color: " + PRIMARY + ";" +
                            "-fx-background-radius: 8 8 0 0;"
            );

            Label month = new Label(months[i]);
            month.setTextFill(Color.web(MUTED));

            barBox.getChildren().addAll(bar, month);
            bars.getChildren().add(barBox);
        }

        chart.getChildren().addAll(chartTitle, bars);
        content.getChildren().addAll(metrics, chart);
        setContent(createScrollPane(content));
    }

    private void showAdminLogsPage() {
        setPageTitle("Admin Logs");
        VBox content = createPageContainer();
        content.getChildren().add(
                createEmptyState(
                        "No admin logs available",
                        "Administrative actions will be recorded here."
                )
        );
        setContent(createScrollPane(content));
    }

    private void showAddProductPage() {
        setPageTitle("Add Product");

        VBox content = createPageContainer();

        GridPane form = new GridPane();
        form.setHgap(18);
        form.setVgap(14);
        form.setPadding(new Insets(28));
        form.setStyle(cardStyle(16));

        TextField name = createTextField("Product name");

        ComboBox<Category> categoryBox = new ComboBox<>();
        categoryBox.setPromptText("Select category");
        categoryBox.setPrefHeight(44);
        categoryBox.setMaxWidth(Double.MAX_VALUE);
        categoryBox.setStyle(inputStyle());

        categoryBox.setCellFactory(listView -> new ListCell<>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null
                        ? null
                        : item.getCategoryName());
            }
        });

        categoryBox.setButtonCell(new ListCell<>() {
            @Override
            protected void updateItem(Category item, boolean empty) {
                super.updateItem(item, empty);
                setText(empty || item == null
                        ? null
                        : item.getCategoryName());
            }
        });

        try {
            categoryBox.getItems().setAll(
                    categoryService.getActiveCategories()
            );
        } catch (SQLException e) {
            e.printStackTrace();
        }

        TextField unit = createTextField("Default unit, e.g. KG or LITRE");

        TextArea description = new TextArea();
        description.setPromptText("Product description");
        description.setPrefRowCount(4);
        description.setStyle(inputStyle());

        Label message = new Label();

        Button save = createPrimaryButton("Save Product");
        Button clear = createSecondaryButton("Clear Form");

        save.setOnAction(event -> {
            if (name.getText().trim().isEmpty()
                    || categoryBox.getValue() == null) {

                message.setTextFill(Color.web(DANGER));
                message.setText("Product name and category are required.");
                return;
            }

            try {
                Product product = new Product();
                product.setProductName(name.getText().trim());
                product.setCategoryId(
                        categoryBox.getValue().getCategoryId()
                );
                product.setDefaultUnit(unit.getText().trim());
                product.setDescription(description.getText().trim());
                product.setActive(true);

                boolean saved = productService.addProduct(product);

                if (saved) {
                    message.setTextFill(Color.web(PRIMARY));
                    message.setText("Product saved successfully.");

                    name.clear();
                    categoryBox.setValue(null);
                    unit.clear();
                    description.clear();
                } else {
                    message.setTextFill(Color.web(DANGER));
                    message.setText("Product could not be saved.");
                }

            } catch (IllegalArgumentException e) {
                message.setTextFill(Color.web(DANGER));
                message.setText(e.getMessage());
            } catch (SQLException e) {
                message.setTextFill(Color.web(DANGER));
                message.setText("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        });

        clear.setOnAction(event -> {
            name.clear();
            categoryBox.setValue(null);
            unit.clear();
            description.clear();
            message.setText("");
        });

        form.add(fieldLabel("Product name"), 0, 0);
        form.add(fieldLabel("Category"), 1, 0);
        form.add(name, 0, 1);
        form.add(categoryBox, 1, 1);
        form.add(fieldLabel("Default unit"), 0, 2, 2, 1);
        form.add(unit, 0, 3, 2, 1);
        form.add(fieldLabel("Description"), 0, 4, 2, 1);
        form.add(description, 0, 5, 2, 1);
        form.add(message, 0, 6, 2, 1);
        form.add(save, 0, 7);
        form.add(clear, 1, 7);

        ColumnConstraints first = new ColumnConstraints();
        first.setPercentWidth(50);

        ColumnConstraints second = new ColumnConstraints();
        second.setPercentWidth(50);

        form.getColumnConstraints().addAll(first, second);

        content.getChildren().add(form);
        setContent(createScrollPane(content));
    }

    private void showDeliveriesPage() {
        setPageTitle("Deliveries");
        VBox content = createPageContainer();
        content.getChildren().add(
                createEmptyState(
                        "No deliveries available",
                        "Delivery records will appear here after orders are dispatched."
                )
        );
        setContent(createScrollPane(content));
    }

    private void showCartPage() {
        setPageTitle("My Cart");
        VBox content = createPageContainer();
        content.getChildren().add(
                createEmptyState(
                        "Your cart is empty",
                        "Products added by the buyer will appear here."
                )
        );
        setContent(createScrollPane(content));
    }

    private HBox createCartRow(String icon, String name, String quantity, String amount) {
        Label iconLabel = new Label(icon);
        iconLabel.setFont(Font.font("Arial", 34));

        VBox info = new VBox(4);
        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 15));
        Label quantityLabel = new Label(quantity);
        quantityLabel.setTextFill(Color.web(MUTED));
        info.getChildren().addAll(nameLabel, quantityLabel);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        Label amountLabel = new Label(amount);
        amountLabel.setFont(Font.font("Arial", FontWeight.BOLD, 16));

        Button remove = createSecondaryButton("Remove");
        remove.setOnAction(event -> showInfo(name + " removed from cart."));

        HBox row = new HBox(14, iconLabel, info, spacer, amountLabel, remove);
        row.setAlignment(Pos.CENTER_LEFT);
        return row;
    }

    private void showMessagesPage() {
        setPageTitle("Messages");
        VBox content = createPageContainer();
        content.getChildren().add(
                createEmptyState(
                        "No conversations yet",
                        "Buyer and supplier messages will appear here."
                )
        );
        setContent(createScrollPane(content));
    }

    private VBox createConversationItem(String name, String preview) {
        VBox item = new VBox(4);
        item.setPadding(new Insets(12));
        item.setStyle(
                "-fx-background-color: #F8FAF9;" +
                        "-fx-background-radius: 10;"
        );

        Label nameLabel = new Label(name);
        nameLabel.setFont(Font.font("Arial", FontWeight.BOLD, 14));

        Label previewLabel = new Label(preview);
        previewLabel.setTextFill(Color.web(MUTED));

        item.getChildren().addAll(nameLabel, previewLabel);
        return item;
    }

    private HBox createMessageBubble(String text, boolean incoming) {
        Label bubble = new Label(text);
        bubble.setWrapText(true);
        bubble.setMaxWidth(420);
        bubble.setPadding(new Insets(10, 14, 10, 14));
        bubble.setStyle(
                "-fx-background-color: " + (incoming ? PRIMARY_LIGHT : PRIMARY) + ";" +
                        "-fx-text-fill: " + (incoming ? TEXT : "white") + ";" +
                        "-fx-background-radius: 14;"
        );

        HBox row = new HBox(bubble);
        row.setAlignment(incoming ? Pos.CENTER_LEFT : Pos.CENTER_RIGHT);
        return row;
    }

    private void showSimpleCardsPage(String pageName, String heading,
                                     List<SimpleCardData> data) {
        setPageTitle(pageName);

        VBox content = createPageContainer();

        Label title = new Label(heading);
        title.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        VBox cards = new VBox(12);

        for (SimpleCardData item : data) {
            HBox card = new HBox(18);
            card.setAlignment(Pos.CENTER_LEFT);
            card.setPadding(new Insets(18));
            card.setStyle(cardStyle(14));

            Label icon = new Label("▣");
            icon.setAlignment(Pos.CENTER);
            icon.setPrefSize(48, 48);
            icon.setStyle(
                    "-fx-background-color: " + PRIMARY_LIGHT + ";" +
                            "-fx-background-radius: 12;" +
                            "-fx-font-size: 20px;"
            );

            VBox information = new VBox(4);
            Label first = new Label(item.first());
            first.setFont(Font.font("Arial", FontWeight.BOLD, 15));

            Label second = new Label(item.second());
            second.setTextFill(Color.web(MUTED));

            Label description = new Label(item.description());
            description.setTextFill(Color.web(MUTED));

            information.getChildren().addAll(first, second, description);

            Region spacer = new Region();
            HBox.setHgrow(spacer, Priority.ALWAYS);

            Label status = new Label(item.status());
            status.setStyle(
                    "-fx-background-color: " + PRIMARY_LIGHT + ";" +
                            "-fx-text-fill: " + PRIMARY_DARK + ";" +
                            "-fx-background-radius: 10;" +
                            "-fx-padding: 6 12;" +
                            "-fx-font-weight: bold;"
            );

            card.getChildren().addAll(icon, information, spacer, status);
            cards.getChildren().add(card);
        }

        content.getChildren().addAll(title, cards);
        setContent(createScrollPane(content));
    }

    private VBox createEmptyState(String title, String description) {
        VBox box = new VBox(14);
        box.setAlignment(Pos.CENTER);
        box.setPadding(new Insets(70));
        box.setMinHeight(420);
        box.setStyle(cardStyle(16));

        Label icon = new Label("▢");
        icon.setFont(Font.font("Arial", 52));
        icon.setTextFill(Color.web("#9CA3AF"));

        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 22));
        titleLabel.setTextFill(Color.web(TEXT));

        Label descriptionLabel = new Label(description);
        descriptionLabel.setFont(Font.font("Arial", 14));
        descriptionLabel.setTextFill(Color.web(MUTED));
        descriptionLabel.setWrapText(true);
        descriptionLabel.setMaxWidth(520);
        descriptionLabel.setAlignment(Pos.CENTER);

        box.getChildren().addAll(icon, titleLabel, descriptionLabel);
        return box;
    }


    private void showProductSearchResults(String searchText) {
        setPageTitle("Search Results");

        VBox content = createPageContainer();

        HBox headingRow = new HBox(12);
        headingRow.setAlignment(Pos.CENTER_LEFT);

        Label heading = new Label("Results for: " + searchText);
        heading.setFont(
                Font.font("Arial", FontWeight.BOLD, 20)
        );

        Button showAll =
                createSecondaryButton("Show All Products");
        showAll.setOnAction(event -> showProductsPage());

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        headingRow.getChildren().addAll(
                heading, spacer, showAll
        );

        TilePane productTiles = new TilePane();
        productTiles.setHgap(16);
        productTiles.setVgap(16);
        productTiles.setPrefColumns(4);

        try {
            List<Product> products =
                    productService.getAllActiveProducts();

            String normalized =
                    searchText.trim().toLowerCase();

            for (Product product : products) {
                String productName =
                        product.getProductName() == null
                                ? ""
                                : product.getProductName()
                                  .toLowerCase();

                String categoryName =
                        categoryNameFor(product.getCategoryId());

                if (productName.contains(normalized)
                        || categoryName.toLowerCase()
                        .contains(normalized)) {

                    productTiles.getChildren().add(
                            createLargeProductCard(
                                    productIcon(
                                            product.getProductName()
                                    ),
                                    product.getProductName(),
                                    categoryName,
                                    "FarmersIn",
                                    formatUnit(
                                            product.getDefaultUnit()
                                    )
                            )
                    );
                }
            }

            if (productTiles.getChildren().isEmpty()) {
                content.getChildren().addAll(
                        headingRow,
                        createEmptyState(
                                "No matching products",
                                "No product or category matches: "
                                        + searchText
                        )
                );
            } else {
                content.getChildren().addAll(
                        headingRow, productTiles
                );
            }

        } catch (SQLException e) {
            content.getChildren().addAll(
                    headingRow,
                    createEmptyState(
                            "Search failed",
                            e.getMessage()
                    )
            );
            e.printStackTrace();
        }

        setContent(createScrollPane(content));
    }

    private void showAddCategoryDialog() {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle("Add Category");
        dialog.setHeaderText("Create a new product category");
        dialog.setContentText("Category name:");

        dialog.showAndWait().ifPresent(name -> {
            String cleanedName = name.trim();

            if (cleanedName.isEmpty()) {
                showError("Category name is required.");
                return;
            }

            try {
                Category category = new Category();
                category.setCategoryName(cleanedName);
                category.setActive(true);

                if (categoryService.addCategory(category)) {
                    showInfo("Category added successfully.");
                    showCategoriesPage();
                } else {
                    showError("Category could not be added.");
                }

            } catch (IllegalArgumentException e) {
                showError(e.getMessage());
            } catch (SQLException e) {
                showError("Database error: " + e.getMessage());
                e.printStackTrace();
            }
        });
    }

    private Label createInlineEmptyLabel(String message) {
        Label label = new Label(message);
        label.setTextFill(Color.web(MUTED));
        label.setFont(Font.font("Arial", 14));
        label.setPadding(new Insets(22));
        label.setWrapText(true);
        return label;
    }

    private String categoryNameFor(int categoryId) {
        try {
            return categoryService
                    .getCategoryById(categoryId)
                    .getCategoryName();
        } catch (SQLException | IllegalArgumentException e) {
            return "Category";
        }
    }

    private String categoryIcon(String categoryName) {
        if (categoryName == null) {
            return "▦";
        }

        String value = categoryName.toLowerCase();

        if (value.contains("vegetable")) return "🥬";
        if (value.contains("fruit")) return "🍎";
        if (value.contains("grain") || value.contains("cereal")) return "🌾";
        if (value.contains("pulse") || value.contains("bean")) return "🫘";
        if (value.contains("dairy") || value.contains("milk")) return "🥛";
        if (value.contains("spice")) return "🌶";
        if (value.contains("nut") || value.contains("seed")) return "🌰";

        return "▦";
    }

    private String productIcon(String productName) {
        if (productName == null) {
            return "🌱";
        }

        String value = productName.toLowerCase();

        if (value.contains("tomato")) return "🍅";
        if (value.contains("potato")) return "🥔";
        if (value.contains("cucumber")) return "🥒";
        if (value.contains("onion")) return "🧅";
        if (value.contains("rice")) return "🍚";
        if (value.contains("milk")) return "🥛";
        if (value.contains("apple")) return "🍎";
        if (value.contains("chili") || value.contains("chilli")) return "🌶";

        return "🌱";
    }

    private String formatUnit(String unit) {
        if (unit == null || unit.trim().isEmpty()) {
            return "Unit not specified";
        }

        return "Per " + unit.trim().toLowerCase();
    }

    private String statusStyle(String color) {
        return "-fx-background-color: derive(" + color + ", 85%);" +
                "-fx-text-fill: " + color + ";" +
                "-fx-background-radius: 10;" +
                "-fx-padding: 6 10;" +
                "-fx-font-weight: bold;";
    }

    private void showError(String message) {
        Alert alert = new Alert(Alert.AlertType.ERROR);
        alert.setTitle("FarmersIn");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // =========================================================
    // REUSABLE UI COMPONENTS
    // =========================================================

    private VBox createPageContainer() {
        VBox content = new VBox(20);
        content.setPadding(new Insets(5, 28, 28, 28));
        return content;
    }

    private ScrollPane createScrollPane(Region content) {
        ScrollPane scroll = new ScrollPane(content);
        scroll.setFitToWidth(true);
        scroll.setStyle(
                "-fx-background: " + BACKGROUND + ";" +
                        "-fx-background-color: " + BACKGROUND + ";" +
                        "-fx-border-color: transparent;"
        );
        return scroll;
    }

    private VBox createMetricCard(String title, String value, String icon, String color) {
        VBox card = new VBox(9);
        card.setPadding(new Insets(18));
        card.setPrefWidth(220);
        card.setStyle(cardStyle(14));
        card.setEffect(createSmallShadow());

        HBox top = new HBox();
        Label iconLabel = new Label(icon);
        iconLabel.setAlignment(Pos.CENTER);
        iconLabel.setPrefSize(42, 42);
        iconLabel.setStyle(
                "-fx-background-color: derive(" + color + ", 85%);" +
                        "-fx-background-radius: 10;" +
                        "-fx-font-size: 18px;"
        );
        top.getChildren().add(iconLabel);

        Label titleLabel = new Label(title);
        titleLabel.setTextFill(Color.web(MUTED));

        Label valueLabel = new Label(value);
        valueLabel.setFont(Font.font("Arial", FontWeight.BOLD, 24));

        card.getChildren().addAll(top, titleLabel, valueLabel);
        return card;
    }

    private HBox createTitleActionRow(String title, String actionText, Runnable action) {
        Label titleLabel = new Label(title);
        titleLabel.setFont(Font.font("Arial", FontWeight.BOLD, 20));

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        HBox row = new HBox(titleLabel, spacer);
        row.setAlignment(Pos.CENTER_LEFT);

        if (actionText != null) {
            Button actionButton = createPrimaryButton(actionText);
            actionButton.setOnAction(event -> action.run());
            row.getChildren().add(actionButton);
        }

        return row;
    }

    private VBox createCardContainer(Region node) {
        VBox box = new VBox(node);
        box.setPadding(new Insets(18));
        box.setStyle(cardStyle(16));
        return box;
    }

    private Button createPrimaryButton(String text) {
        Button button = new Button(text);
        button.setCursor(Cursor.HAND);
        button.setPrefHeight(42);
        button.setStyle(
                "-fx-background-color: " + PRIMARY + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 9;" +
                        "-fx-padding: 0 18;"
        );

        button.setOnMouseEntered(event -> button.setStyle(
                "-fx-background-color: " + PRIMARY_DARK + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 9;" +
                        "-fx-padding: 0 18;"
        ));

        button.setOnMouseExited(event -> button.setStyle(
                "-fx-background-color: " + PRIMARY + ";" +
                        "-fx-text-fill: white;" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-background-radius: 9;" +
                        "-fx-padding: 0 18;"
        ));

        return button;
    }

    private Button createSecondaryButton(String text) {
        Button button = new Button(text);
        button.setCursor(Cursor.HAND);
        button.setPrefHeight(42);
        button.setStyle(
                "-fx-background-color: white;" +
                        "-fx-text-fill: " + PRIMARY_DARK + ";" +
                        "-fx-font-size: 14px;" +
                        "-fx-font-weight: bold;" +
                        "-fx-border-color: #BFD9C8;" +
                        "-fx-border-radius: 9;" +
                        "-fx-background-radius: 9;" +
                        "-fx-padding: 0 18;"
        );
        return button;
    }

    private Button createIconButton(String icon) {
        Button button = new Button(icon);
        button.setCursor(Cursor.HAND);
        button.setPrefSize(42, 42);
        button.setStyle(
                "-fx-background-color: #F7F9F8;" +
                        "-fx-background-radius: 21;" +
                        "-fx-border-color: " + BORDER + ";" +
                        "-fx-border-radius: 21;" +
                        "-fx-font-size: 16px;"
        );
        return button;
    }

    private TextField createTextField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.setPrefHeight(44);
        field.setMaxWidth(Double.MAX_VALUE);
        field.setStyle(inputStyle());
        return field;
    }

    private PasswordField createPasswordField(String prompt) {
        PasswordField field = new PasswordField();
        field.setPromptText(prompt);
        field.setPrefHeight(44);
        field.setMaxWidth(Double.MAX_VALUE);
        field.setStyle(inputStyle());
        return field;
    }

    private Label fieldLabel(String text) {
        Label label = new Label(text);
        label.setFont(Font.font("Arial", FontWeight.BOLD, 13));
        label.setTextFill(Color.web(TEXT));
        return label;
    }

    private Separator createSeparator() {
        Separator separator = new Separator();
        separator.setStyle("-fx-background-color: " + BORDER + ";");
        return separator;
    }

    private Region createSpacer(double height) {
        Region spacer = new Region();
        spacer.setPrefHeight(height);
        return spacer;
    }

    private Region createHorizontalSpacer() {
        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        return spacer;
    }

    private String inputStyle() {
        return "-fx-background-color: #FAFBFA;" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: 9;" +
                "-fx-background-radius: 9;" +
                "-fx-padding: 0 12;" +
                "-fx-font-size: 14px;";
    }

    private String cardStyle(int radius) {
        return "-fx-background-color: " + CARD + ";" +
                "-fx-background-radius: " + radius + ";" +
                "-fx-border-color: " + BORDER + ";" +
                "-fx-border-radius: " + radius + ";";
    }

    private DropShadow createShadow() {
        DropShadow shadow = new DropShadow();
        shadow.setRadius(22);
        shadow.setOffsetY(6);
        shadow.setColor(Color.rgb(15, 23, 42, 0.12));
        return shadow;
    }

    private DropShadow createSmallShadow() {
        DropShadow shadow = new DropShadow();
        shadow.setRadius(9);
        shadow.setOffsetY(3);
        shadow.setColor(Color.rgb(15, 23, 42, 0.07));
        return shadow;
    }

    private void setPageTitle(String title) {
        if (pageTitle != null) {
            pageTitle.setText(title);
        }
    }

    private void setContent(javafx.scene.Node node) {
        contentArea.getChildren().setAll(node);
    }

    private String capitalize(String text) {
        if (text == null || text.isEmpty()) {
            return "";
        }
        return text.substring(0, 1).toUpperCase()
                + text.substring(1).toLowerCase();
    }

    private void showInfo(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION);
        alert.setTitle("FarmersIn");
        alert.setHeaderText(null);
        alert.setContentText(message);
        alert.showAndWait();
    }

    // =========================================================
    // SIMPLE DATA CLASSES FOR TABLES
    // =========================================================

    public static class SupplierRow {
        private final javafx.beans.property.SimpleStringProperty name;
        private final javafx.beans.property.SimpleStringProperty business;
        private final javafx.beans.property.SimpleStringProperty city;
        private final javafx.beans.property.SimpleStringProperty status;

        public SupplierRow(String name, String business, String city, String status) {
            this.name = new javafx.beans.property.SimpleStringProperty(name);
            this.business = new javafx.beans.property.SimpleStringProperty(business);
            this.city = new javafx.beans.property.SimpleStringProperty(city);
            this.status = new javafx.beans.property.SimpleStringProperty(status);
        }

        public javafx.beans.property.StringProperty nameProperty() {
            return name;
        }

        public javafx.beans.property.StringProperty businessProperty() {
            return business;
        }

        public javafx.beans.property.StringProperty cityProperty() {
            return city;
        }

        public javafx.beans.property.StringProperty statusProperty() {
            return status;
        }
    }

    public static class OrderRow {
        private final javafx.beans.property.SimpleStringProperty id;
        private final javafx.beans.property.SimpleStringProperty buyer;
        private final javafx.beans.property.SimpleStringProperty amount;
        private final javafx.beans.property.SimpleStringProperty date;
        private final javafx.beans.property.SimpleStringProperty status;

        public OrderRow(String id, String buyer, String amount,
                        String date, String status) {
            this.id = new javafx.beans.property.SimpleStringProperty(id);
            this.buyer = new javafx.beans.property.SimpleStringProperty(buyer);
            this.amount = new javafx.beans.property.SimpleStringProperty(amount);
            this.date = new javafx.beans.property.SimpleStringProperty(date);
            this.status = new javafx.beans.property.SimpleStringProperty(status);
        }

        public javafx.beans.property.StringProperty idProperty() {
            return id;
        }

        public javafx.beans.property.StringProperty buyerProperty() {
            return buyer;
        }

        public javafx.beans.property.StringProperty amountProperty() {
            return amount;
        }

        public javafx.beans.property.StringProperty dateProperty() {
            return date;
        }

        public javafx.beans.property.StringProperty statusProperty() {
            return status;
        }
    }

    public static class UserRow {
        private final javafx.beans.property.SimpleStringProperty name;
        private final javafx.beans.property.SimpleStringProperty email;
        private final javafx.beans.property.SimpleStringProperty role;
        private final javafx.beans.property.SimpleStringProperty status;

        public UserRow(String name, String email, String role, String status) {
            this.name = new javafx.beans.property.SimpleStringProperty(name);
            this.email = new javafx.beans.property.SimpleStringProperty(email);
            this.role = new javafx.beans.property.SimpleStringProperty(role);
            this.status = new javafx.beans.property.SimpleStringProperty(status);
        }

        public javafx.beans.property.StringProperty nameProperty() {
            return name;
        }

        public javafx.beans.property.StringProperty emailProperty() {
            return email;
        }

        public javafx.beans.property.StringProperty roleProperty() {
            return role;
        }

        public javafx.beans.property.StringProperty statusProperty() {
            return status;
        }
    }

    public static class LogRow {
        private final javafx.beans.property.SimpleStringProperty date;
        private final javafx.beans.property.SimpleStringProperty action;
        private final javafx.beans.property.SimpleStringProperty entity;
        private final javafx.beans.property.SimpleStringProperty details;

        public LogRow(String date, String action, String entity, String details) {
            this.date = new javafx.beans.property.SimpleStringProperty(date);
            this.action = new javafx.beans.property.SimpleStringProperty(action);
            this.entity = new javafx.beans.property.SimpleStringProperty(entity);
            this.details = new javafx.beans.property.SimpleStringProperty(details);
        }

        public javafx.beans.property.StringProperty dateProperty() {
            return date;
        }

        public javafx.beans.property.StringProperty actionProperty() {
            return action;
        }

        public javafx.beans.property.StringProperty entityProperty() {
            return entity;
        }

        public javafx.beans.property.StringProperty detailsProperty() {
            return details;
        }
    }

    private record SimpleCardData(
            String first,
            String second,
            String status,
            String description
    ) {
    }

    public static void main(String[] args) {
        launch(args);
    }
}