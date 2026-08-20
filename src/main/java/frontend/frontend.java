package frontend;

import javafx.application.Application;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyObjectWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.concurrent.Task;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.effect.DropShadow;
import javafx.scene.input.MouseButton;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.scene.text.Font;
import javafx.scene.text.FontPosture;
import javafx.scene.text.FontWeight;
import javafx.geometry.Rectangle2D;
import javafx.stage.Screen;
import javafx.stage.Stage;

import java.lang.reflect.*;
import java.math.BigDecimal;
import java.sql.Date;
import java.sql.Timestamp;
import java.time.LocalDate;
import java.util.*;
import java.util.function.Consumer;
import java.util.function.Supplier;

/**
 * FarmersIn - single-class JavaFX frontend.
 *
 * IMPORTANT:
 *  - This class intentionally does not import any model/dao/service classes.
 *  - It discovers your existing services/DAOs/models at runtime using reflection.
 *  - Therefore you can place only this file in your project without editing backend files.
 *
 * Visual identity: warm beige backgrounds, dark-soil brown for structure and
 * primary actions, light wheat-tan for accents, and near-black text throughout
 * for strong readability.
 *
 * Place at: src/main/java/frontend/FarmersInFrontend.java
 * Run: frontend.FarmersInFrontend
 */
public class frontend {

    private Stage stage;
    private BorderPane shell;
    private VBox sidebar;
    private StackPane content;
    private Label pageTitle;
    private Label pageSubtitle;
    private Label toast;
    private final Map<String, Button> navButtons = new LinkedHashMap<>();

    private String role = "ADMIN";
    private Integer currentUserId = null;
    private Integer currentProfileId = null;
    private String currentUserName = "";
    private String currentUserEmail = "";

    // ---------------------------------------------------------------------
    // PALETTE — beige / dark brown / light brown, near-black text
    // ---------------------------------------------------------------------
    private static final String BG        = "#ECE0C8"; // warm beige page background
    private static final String CARD      = "#F8F1E1"; // pale cream card surface
    private static final String SOIL_DARK = "#3B2A1E"; // deep dark brown (sidebar, primary)
    private static final String BARK      = "#5B4028"; // mid dark brown (hover / secondary emphasis)
    private static final String WHEAT     = "#C9A876"; // light brown / tan accent
    private static final String WHEAT_SOFT= "#E3CFA8"; // pale tan (sidebar text, chips)
    private static final String INK       = "#1C140D"; // near-black text
    private static final String MUTED     = "#6E5B45"; // muted warm brown for secondary text
    private static final String BORDER    = "#D9C6A3"; // soft tan border
    private static final String DANGER_BG = "#F2DED2"; // soft terracotta chip background
    private static final String DANGER_FG = "#8B3A2B"; // brick red text/icon
    private static final String WARNING   = "#8A5A22"; // amber brown

    // legacy aliases kept so every original call-site below still compiles unchanged
    private static final String GREEN  = SOIL_DARK;
    private static final String DARK   = INK;
    private static final String DANGER = DANGER_FG;

    /**
     * Clamps a requested [width, height] to the visible screen bounds
     * (leaving a small margin) so the window never opens partly off-screen
     * on smaller displays. Returns {width, height}.
     */
    private double[] fitToScreen(double preferredW, double preferredH) {
        Rectangle2D bounds = Screen.getPrimary().getVisualBounds();
        double maxW = bounds.getWidth() - 40;
        double maxH = bounds.getHeight() - 40;
        double w = Math.min(preferredW, maxW);
        double h = Math.min(preferredH, maxH);
        return new double[]{w, h};
    }

    private void startUi(Stage primaryStage) {
        this.stage = primaryStage;
        stage.setTitle("FarmersIn • Farm to Business Marketplace");
        stage.setMinWidth(920);
        stage.setMinHeight(640);
        showWelcome();
        stage.show();
    }

    // ---------------------------------------------------------------------
    // ENTRY / ROLE SELECTION
    // ---------------------------------------------------------------------

    private void showWelcome() {
        VBox root = new VBox(22);
        root.setAlignment(Pos.CENTER);
        root.setPadding(new Insets(42));
        root.setStyle("-fx-background-color:" + BG + ";");

        HBox brandRow = new HBox(10);
        brandRow.setAlignment(Pos.CENTER);
        Label leaf = new Label("\uD83C\uDF3E"); // sheaf of rice / wheat glyph
        leaf.setFont(Font.font(30));
        Label brand = new Label("FarmersIn");
        brand.setFont(Font.font("Georgia", FontWeight.EXTRA_BOLD, 40));
        brand.setTextFill(Color.web(SOIL_DARK));
        brandRow.getChildren().addAll(leaf, brand);

        Label slogan = new Label("◆   Fresh supply. Fair trade. Better business.   ◆");
        slogan.setFont(Font.font("System", FontPosture.ITALIC, 15));
        slogan.setTextFill(Color.web(MUTED));

        VBox card = new VBox(16);
        card.setMaxWidth(590);
        card.setPadding(new Insets(32, 30, 30, 30));
        card.setStyle(cardStyle());
        card.setEffect(softShadow());

        // decorative "stitched furrow" accent strip — signature touch
        HBox furrow = new HBox();
        furrow.setPrefHeight(6);
        furrow.setMaxWidth(Double.MAX_VALUE);
        for (int i = 0; i < 18; i++) {
            Region seg = new Region();
            seg.setPrefWidth(1000.0 / 18);
            seg.setPrefHeight(6);
            seg.setStyle("-fx-background-color:" + (i % 2 == 0 ? SOIL_DARK : WHEAT) + ";");
            furrow.getChildren().add(seg);
        }

        Label heading = new Label("Sign in to FarmersIn");
        heading.setFont(Font.font("Georgia", FontWeight.BOLD, 24));
        heading.setTextFill(Color.web(SOIL_DARK));

        Label help = new Label("Enter your account email and password, then choose the correct portal.");
        help.setWrapText(true);
        help.setTextFill(Color.web(MUTED));

        ToggleGroup roles = new ToggleGroup();
        HBox roleBox = new HBox(10);
        roleBox.setAlignment(Pos.CENTER_LEFT);
        for (String r : List.of("ADMIN", "BUYER", "SUPPLIER")) {
            ToggleButton b = new ToggleButton(pretty(r));
            b.setToggleGroup(roles);
            b.setUserData(r);
            b.setPrefWidth(150);
            styleRoleToggle(b);
            roleBox.getChildren().add(b);
            if (r.equals("ADMIN")) b.setSelected(true);
        }

        TextField emailField = new TextField();
        emailField.setPromptText("name@example.com");
        emailField.setPrefHeight(44);
        styleInput(emailField);

        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Enter password");
        passwordField.setPrefHeight(44);
        styleInput(passwordField);

        Label loginState = new Label();
        loginState.setTextFill(Color.web(MUTED));
        loginState.setWrapText(true);

        Button enter = primaryButton("Sign In");
        enter.setMaxWidth(Double.MAX_VALUE);
        enter.setPrefHeight(46);

        Runnable login = () -> {
            Toggle t = roles.getSelectedToggle();
            role = t == null ? "ADMIN" : String.valueOf(t.getUserData());

            String email = emailField.getText() == null ? "" : emailField.getText().trim().toLowerCase();
            String password = passwordField.getText() == null ? "" : passwordField.getText();

            if (email.isBlank()) {
                alert("Email required", "Please enter your account email address.");
                emailField.requestFocus();
                return;
            }
            if (password.isBlank()) {
                alert("Password required", "Please enter your password.");
                passwordField.requestFocus();
                return;
            }

            enter.setDisable(true);
            loginState.setText("Signing in…");

            runAsync(() -> invoke("UserService", "getUserByEmail", email), user -> {
                enter.setDisable(false);
                loginState.setText("");

                if (user == null) {
                    alert("Login failed", "No account was found for this email.");
                    return;
                }

                String actualRole = extractString(user, "getRole");
                if (actualRole == null || !role.equalsIgnoreCase(actualRole.trim())) {
                    alert("Wrong portal", "This account belongs to the " +
                            (actualRole == null ? "registered" : pretty(actualRole)) +
                            " portal. Please select the correct role.");
                    return;
                }

                String status = extractString(user, "getAccountStatus");
                if (status != null && (status.equalsIgnoreCase("BLOCKED") || status.equalsIgnoreCase("REJECTED"))) {
                    alert("Account unavailable", "This account is currently " + status.toLowerCase() + ".");
                    return;
                }

                String storedPassword = extractString(user, "getPasswordHash");
                if (!passwordMatches(password, storedPassword)) {
                    alert("Login failed", "Incorrect email or password.");
                    passwordField.clear();
                    passwordField.requestFocus();
                    return;
                }

                currentUserId = extractInt(user, "getUserId");
                currentUserName = Optional.ofNullable(extractString(user, "getFullName")).orElse("");
                currentUserEmail = Optional.ofNullable(extractString(user, "getEmail")).orElse(email);

                if (currentUserId == null) {
                    alert("Login failed", "The account was found, but its User ID could not be resolved.");
                    return;
                }

                resolveProfileThenOpen();
            }, ex -> {
                enter.setDisable(false);
                loginState.setText("");
                alert("Login failed", cleanErrorMessage(ex));
            });
        };

        enter.setOnAction(e -> login.run());
        passwordField.setOnAction(e -> login.run());

        Label note = new Label("Your User ID is detected automatically from the account email.");
        note.setTextFill(Color.web(MUTED));
        note.setStyle("-fx-font-size: 12px;");

        card.getChildren().addAll(
                furrow, heading, help, roleBox,
                labeled("Email", emailField),
                labeled("Password", passwordField),
                loginState, enter, note
        );
        root.getChildren().addAll(brandRow, slogan, card);
        double[] size = fitToScreen(1180, 760);
        stage.setScene(new Scene(root, size[0], size[1]));
        stage.centerOnScreen();
        Platform.runLater(emailField::requestFocus);
    }

    private void resolveProfileThenOpen() {
        currentProfileId = null;
        if (currentUserId == null || role.equals("ADMIN")) {
            openShell();
            return;
        }
        String svc = role.equals("BUYER") ? "buyerprofile" : "supplier_profiles";
        String method = role.equals("BUYER") ? "getBuyerByUserId" : "getSupplierByUserId";
        runAsync(() -> invoke(svc, method, currentUserId), result -> {
            currentProfileId = extractInt(result, role.equals("BUYER") ? "getBuyerId" : "getSupplierId");
            openShell();
        }, ex -> {
            // Still open the workspace. Some projects may not yet have a profile for this user.
            openShell();
        });
    }

    // ---------------------------------------------------------------------
    // APPLICATION SHELL
    // ---------------------------------------------------------------------

    private void openShell() {
        shell = new BorderPane();
        shell.setStyle("-fx-background-color:" + BG + ";");
        shell.setLeft(buildSidebar());
        shell.setTop(buildTopbar());

        content = new StackPane();
        content.setPadding(new Insets(22, 26, 26, 26));
        shell.setCenter(content);

        toast = new Label();
        toast.setVisible(false);
        toast.setManaged(false);

        double[] size = fitToScreen(1400, 860);
        Scene scene = new Scene(shell, size[0], size[1]);
        stage.setScene(scene);
        stage.centerOnScreen();
        showDashboard();
    }

    private Node buildSidebar() {
        sidebar = new VBox(8);
        sidebar.setPrefWidth(245);
        sidebar.setPadding(new Insets(24, 15, 20, 15));
        sidebar.setStyle("-fx-background-color:" + SOIL_DARK + ";");

        HBox logoRow = new HBox(8);
        logoRow.setAlignment(Pos.CENTER_LEFT);
        Label leaf = new Label("\uD83C\uDF3E");
        leaf.setFont(Font.font(18));
        Label logo = new Label("FarmersIn");
        logo.setFont(Font.font("Georgia", FontWeight.EXTRA_BOLD, 24));
        logo.setTextFill(Color.web(WHEAT_SOFT));
        logoRow.getChildren().addAll(leaf, logo);

        Label portal = new Label(pretty(role) + " Portal");
        portal.setTextFill(Color.web(WHEAT));
        portal.setStyle("-fx-font-size: 12px; -fx-font-weight:bold;");
        VBox brand = new VBox(4, logoRow, portal);
        brand.setPadding(new Insets(0, 10, 20, 6));
        sidebar.getChildren().add(brand);

        navButtons.clear();
        addNav("Dashboard", this::showDashboard);
        if (role.equals("ADMIN")) {
            addNav("Approval Requests", this::showApprovals);
            addNav("Categories", this::showCategories);
            addNav("Products", this::showProducts);
            addNav("Orders", () -> showGenericList("All Orders", "Orders across the marketplace", "OrderService", "getOrdersByBuyerId", true));
            addNav("Payments", this::showPayments);
            addNav("Complaints", this::showComplaints);
            addNav("Refunds", this::showRefunds);
            addNav("Deliveries", this::showDeliveries);
            addNav("Admin Logs", this::showAdminLogs);
        } else if (role.equals("BUYER")) {
            addNav("Marketplace", this::showMarketplace);
            addNav("My Orders", this::showBuyerOrders);
            addNav("Payments", this::showBuyerPayments);
            addNav("Complaints", this::showMyComplaints);
            addNav("Notifications", this::showNotifications);
        } else {
            addNav("My Listings", this::showSupplierListings);
            addNav("My Orders", this::showSupplierOrders);
            addNav("Deliveries", this::showDeliveries);
            addNav("Market Prices", this::showMarketPrices);
            addNav("Notifications", this::showNotifications);
        }

        Region spacer = new Region();
        VBox.setVgrow(spacer, Priority.ALWAYS);
        Button switchBtn = new Button("↩ Switch Portal");
        switchBtn.setMaxWidth(Double.MAX_VALUE);
        switchBtn.setStyle("-fx-background-color: transparent; -fx-text-fill:" + WHEAT_SOFT + "; -fx-alignment:CENTER-LEFT; -fx-padding:12; -fx-cursor:hand; -fx-font-weight:bold;");
        switchBtn.setOnAction(e -> showWelcome());
        sidebar.getChildren().addAll(spacer, switchBtn);
        return sidebar;
    }

    private Node buildTopbar() {
        HBox top = new HBox(16);
        top.setAlignment(Pos.CENTER_LEFT);
        top.setPadding(new Insets(17, 26, 17, 26));
        top.setStyle("-fx-background-color:" + CARD + "; -fx-border-color:transparent transparent " + BORDER + " transparent;");

        VBox titles = new VBox(2);
        pageTitle = new Label("Dashboard");
        pageTitle.setFont(Font.font("Georgia", FontWeight.BOLD, 22));
        pageTitle.setTextFill(Color.web(INK));
        pageSubtitle = new Label("Marketplace overview");
        pageSubtitle.setTextFill(Color.web(MUTED));
        titles.getChildren().addAll(pageTitle, pageSubtitle);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);

        String displayIdentity = currentUserName == null || currentUserName.isBlank()
                ? role
                : currentUserName + "  •  " + pretty(role);
        Label identity = new Label(displayIdentity);
        identity.setStyle("-fx-background-color:" + WHEAT_SOFT + "; -fx-text-fill:" + INK + "; -fx-padding:8 14; -fx-background-radius:20; -fx-font-weight:bold; -fx-border-color:" + WHEAT + "; -fx-border-radius:20;");
        top.getChildren().addAll(titles, spacer, identity);
        return top;
    }

    private void addNav(String text, Runnable action) {
        Button b = new Button(text);
        b.setMaxWidth(Double.MAX_VALUE);
        b.setPrefHeight(43);
        b.setAlignment(Pos.CENTER_LEFT);
        b.setStyle(navStyle(false));
        b.setOnAction(e -> {
            navButtons.values().forEach(x -> x.setStyle(navStyle(false)));
            b.setStyle(navStyle(true));
            action.run();
        });
        navButtons.put(text, b);
        sidebar.getChildren().add(b);
    }

    // ---------------------------------------------------------------------
    // DASHBOARDS
    // ---------------------------------------------------------------------

    private void showDashboard() {
        setHeader("Dashboard", role.equals("ADMIN") ? "Control marketplace activity from one place" : "Your FarmersIn workspace");
        VBox box = new VBox(18);

        FlowPane stats = new FlowPane(14, 14);
        stats.getChildren().addAll(
                statCard("Portal", pretty(role), "Active workspace"),
                statCard("User", currentUserId == null ? "—" : "#" + currentUserId, "Signed-in record"),
                statCard("Profile", currentProfileId == null ? "—" : "#" + currentProfileId, role + " profile"),
                statCard("Backend", "Connected", "Backend layer enabled")
        );

        VBox welcome = panel("Welcome to FarmersIn", roleMessage());
        box.getChildren().addAll(stats, welcome);

        if (role.equals("ADMIN")) {
            HBox quick = new HBox(14,
                    actionCard("Pending Buyers", "Review buyer verification requests", this::showApprovals),
                    actionCard("Pending Suppliers", "Approve farmers and suppliers", this::showApprovals),
                    actionCard("Marketplace", "Manage categories and products", this::showProducts));
            HBox.setHgrow(quick.getChildren().get(0), Priority.ALWAYS);
            HBox.setHgrow(quick.getChildren().get(1), Priority.ALWAYS);
            HBox.setHgrow(quick.getChildren().get(2), Priority.ALWAYS);
            box.getChildren().add(quick);
        }
        show(box);
    }

    private String roleMessage() {
        if (role.equals("ADMIN")) return "Approve buyers and suppliers, organize the product catalogue, supervise payments, complaints, refunds and deliveries.";
        if (role.equals("BUYER")) return "Browse approved produce, review your orders and payments, and keep track of notifications.";
        return "Manage your product listings, incoming orders, deliveries and market pricing from a clean supplier workspace.";
    }

    // ---------------------------------------------------------------------
    // ADMIN APPROVALS
    // ---------------------------------------------------------------------

    private void showApprovals() {
        setHeader("Approval Requests", "Approve or reject new buyers and suppliers");
        TabPane tabs = new TabPane();
        tabs.setTabClosingPolicy(TabPane.TabClosingPolicy.UNAVAILABLE);
        tabs.getTabs().add(new Tab("Buyer Requests", approvalPane(true)));
        tabs.getTabs().add(new Tab("Supplier Requests", approvalPane(false)));
        show(tabs);
    }

    private Node approvalPane(boolean buyer) {
        VBox box = new VBox(12);
        box.setPadding(new Insets(14, 0, 0, 0));
        TableView<Object> table = dataTable();
        Label state = new Label("Loading requests…");
        state.setTextFill(Color.web(MUTED));

        Button refresh = secondaryButton("Refresh");
        Button approve = primaryButton("Approve Selected");
        Button reject = dangerButton("Reject Selected");
        HBox actions = new HBox(10, refresh, approve, reject);
        actions.setAlignment(Pos.CENTER_LEFT);

        Runnable load = () -> loadInto(table, state,
                buyer ? "buyerprofile" : "supplier_profiles",
                "getPendingVerifications");
        refresh.setOnAction(e -> load.run());
        approve.setOnAction(e -> updateApproval(table, buyer, "APPROVED", load));
        reject.setOnAction(e -> updateApproval(table, buyer, "REJECTED", load));
        box.getChildren().addAll(actions, state, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        Platform.runLater(load);
        return box;
    }

    private void updateApproval(TableView<Object> table, boolean buyer, String status, Runnable reload) {
        Object row = table.getSelectionModel().getSelectedItem();
        if (row == null) { alert("Select a request", "Choose a buyer or supplier request first."); return; }
        String getter = buyer ? "getBuyerId" : "getSupplierId";
        Integer id = extractInt(row, getter);
        if (id == null) { alert("Cannot identify request", "The selected record does not expose " + getter + "()."); return; }
        String service = buyer ? "buyerprofile" : "supplier_profiles";
        runAsync(() -> invoke(service, "updateVerificationStatus", id, status), x -> {
            toast(status.equals("APPROVED") ? "Request approved" : "Request rejected", false);
            reload.run();
        }, this::showError);
    }

    // ---------------------------------------------------------------------
    // CATEGORIES / PRODUCTS / MARKETPLACE
    // ---------------------------------------------------------------------

    private void showCategories() {
        setHeader("Categories", "Organize what buyers can discover");
        CRUDView view = genericCrudView("CategoryService", "getAllCategories", "Category", true,
                new ActionDef("Add Category", "addCategory", true),
                new ActionDef("Edit Selected", "updateCategory", true));
        Button active = secondaryButton("Toggle Active");
        active.setOnAction(e -> {
            Object row = view.table.getSelectionModel().getSelectedItem();
            if (row == null) { alert("Select a category", "Choose a category first."); return; }
            Integer id = firstInt(row, "getCategoryId", "getId");
            Boolean current = firstBoolean(row, "isActive", "getIsActive", "getActive");
            if (id == null) return;
            runAsync(() -> invoke("CategoryService", "setCategoryActiveStatus", id, current == null || !current), x -> view.reload.run(), this::showError);
        });
        view.actions.getChildren().add(active);
        show(view.root);
    }

    private void showProducts() {
        setHeader("Products", "Maintain the catalogue available to the marketplace");
        VBox root = new VBox(12);
        HBox actions = new HBox(10);
        TableView<Object> table = dataTable();
        Label state = new Label("Loading products…");
        Runnable reload = () -> loadInto(table, state, "ProductService", "getAllActiveProducts");
        Button refresh = secondaryButton("Refresh"); refresh.setOnAction(e -> reload.run());
        Button add = primaryButton("Add Product"); add.setOnAction(e -> modelEditor("Product", null, obj -> invokeAndReload("ProductService", "addProduct", obj, reload)));
        Button edit = secondaryButton("Edit Selected"); edit.setOnAction(e -> {
            Object row = table.getSelectionModel().getSelectedItem();
            if (row == null) { alert("Select a product", "Choose a product to edit."); return; }
            modelEditor("Product", row, obj -> invokeAndReload("ProductService", "updateProduct", obj, reload));
        });
        actions.getChildren().addAll(refresh, add, edit);
        root.getChildren().addAll(actions, state, table);
        VBox.setVgrow(table, Priority.ALWAYS);
        show(root); reload.run();
    }

    private void showMarketplace() {
        setHeader("Marketplace", "Browse active products and approved supplier listings");
        VBox root = new VBox(12);
        TableView<Object> products = dataTable();
        TableView<Object> listings = dataTable();
        Label productState = new Label();
        Label listingState = new Label("Select a product to view available supplier offers.");
        Button refresh = secondaryButton("Refresh Products");
        Runnable loadProducts = () -> loadInto(products, productState, "ProductService", "getAllActiveProducts");
        refresh.setOnAction(e -> loadProducts.run());
        products.getSelectionModel().selectedItemProperty().addListener((o, old, row) -> {
            if (row == null) return;
            Integer id = firstInt(row, "getProductId", "getId");
            if (id != null) loadInto(listings, listingState, "supplier_products", "getApprovedByProductId", id);
        });
        SplitPane split = new SplitPane(wrappedTable("Products", products, productState), wrappedTable("Approved Supplier Offers", listings, listingState));
        split.setDividerPositions(.44);
        VBox.setVgrow(split, Priority.ALWAYS);
        root.getChildren().addAll(refresh, split);
        show(root); loadProducts.run();
    }

    // ---------------------------------------------------------------------
    // BUYER / SUPPLIER DATA
    // ---------------------------------------------------------------------

    private void showBuyerOrders() {
        setHeader("My Orders", "Track your purchasing activity");
        if (currentProfileId == null) { showMissingProfile("Buyer"); return; }
        simpleServiceTable("OrderService", "getOrdersByBuyerId", currentProfileId);
    }

    private void showSupplierOrders() {
        setHeader("My Orders", "Orders placed with your farm or business");
        if (currentProfileId == null) { showMissingProfile("Supplier"); return; }
        simpleServiceTable("OrderService", "getOrdersBySupplierId", currentProfileId);
    }

    private void showSupplierListings() {
        setHeader("My Listings", "Manage produce offered by your supplier account");
        if (currentProfileId == null) { showMissingProfile("Supplier"); return; }
        VBox root = new VBox(12);
        TableView<Object> table = dataTable();
        Label state = new Label();
        Runnable reload = () -> loadInto(table, state, "supplier_products", "getListingsBySupplierId", currentProfileId);
        HBox actions = new HBox(10);
        Button refresh = secondaryButton("Refresh"); refresh.setOnAction(e -> reload.run());
        Button add = primaryButton("New Listing"); add.setOnAction(e -> modelEditor("SupplierProduct", null, obj -> {
            setIfPossible(obj, "setSupplierId", currentProfileId);
            invokeAndReload("supplier_products", "addSupplierProduct", obj, reload);
        }));
        Button edit = secondaryButton("Edit Selected"); edit.setOnAction(e -> {
            Object row = table.getSelectionModel().getSelectedItem();
            if (row == null) { alert("Select a listing", "Choose a supplier listing first."); return; }
            modelEditor("SupplierProduct", row, obj -> invokeAndReload("supplier_products", "updateSupplierProduct", obj, reload));
        });
        actions.getChildren().addAll(refresh, add, edit);
        root.getChildren().addAll(actions, state, table); VBox.setVgrow(table, Priority.ALWAYS);
        show(root); reload.run();
    }

    private void showBuyerPayments() {
        setHeader("Payments", "Payments linked to your buyer account");
        if (currentProfileId == null) { showMissingProfile("Buyer"); return; }
        simpleServiceTable("PaymentService", "getPaymentsByBuyerId", currentProfileId);
    }

    private void showMyComplaints() {
        setHeader("Complaints", "Requests and issues submitted from your account");
        if (currentUserId == null) { showMissingProfile("User"); return; }
        simpleServiceTable("ComplaintService", "getComplaintsSubmittedBy", currentUserId);
    }

    private void showNotifications() {
        setHeader("Notifications", "Latest account updates");
        if (currentUserId == null) { showMissingProfile("User"); return; }
        VBox root = new VBox(12);
        TableView<Object> table = dataTable(); Label state = new Label();
        Runnable reload = () -> loadInto(table, state, "NotificationService", "getNotificationsByUserId", currentUserId);
        Button refresh = secondaryButton("Refresh"); refresh.setOnAction(e -> reload.run());
        Button allRead = primaryButton("Mark All Read"); allRead.setOnAction(e -> runAsync(() -> invoke("NotificationService", "markAllAsReadForUser", currentUserId), x -> reload.run(), this::showError));
        root.getChildren().addAll(new HBox(10, refresh, allRead), state, table); VBox.setVgrow(table, Priority.ALWAYS);
        show(root); reload.run();
    }

    // ---------------------------------------------------------------------
    // ADMIN OPERATIONS
    // ---------------------------------------------------------------------

    private void showPayments() {
        setHeader("Payments", "Search and verify marketplace payments");
        searchByIdPanel("PaymentService", "getPaymentById", "Payment ID", (table, row) -> {
            Button verify = primaryButton("Verify Selected");
            verify.setOnAction(e -> {
                Object item = table.getSelectionModel().getSelectedItem();
                if (item == null) return;
                Integer id = firstInt(item, "getPaymentId", "getId");
                Integer adminId = currentUserId == null ? 1 : currentUserId;
                if (id != null) runAsync(() -> invoke("PaymentService", "verifyPayment", id, adminId, "VERIFIED"), x -> toast("Payment verified", false), this::showError);
            });
            return verify;
        });
    }

    private void showComplaints() {
        setHeader("Complaints", "Review unresolved marketplace complaints");
        VBox root = new VBox(12);
        ComboBox<String> status = new ComboBox<>(FXCollections.observableArrayList("OPEN", "PENDING", "RESOLVED", "CLOSED"));
        status.setValue("OPEN");
        TableView<Object> table = dataTable(); Label state = new Label();
        Runnable reload = () -> loadInto(table, state, "ComplaintService", "getComplaintsByStatus", status.getValue());
        status.setOnAction(e -> reload.run());
        Button resolve = primaryButton("Resolve Selected");
        resolve.setOnAction(e -> {
            Object row = table.getSelectionModel().getSelectedItem();
            if (row == null) return;
            Integer id = firstInt(row, "getComplaintId", "getId");
            TextInputDialog d = new TextInputDialog(); d.setHeaderText("Admin response"); d.setContentText("Resolution note:");
            d.showAndWait().ifPresent(note -> runAsync(() -> invoke("ComplaintService", "resolveComplaint", id, "RESOLVED", note), x -> reload.run(), this::showError));
        });
        root.getChildren().addAll(new HBox(10, new Label("Status:"), status, resolve), state, table); VBox.setVgrow(table, Priority.ALWAYS);
        show(root); reload.run();
    }

    private void showRefunds() {
        setHeader("Refunds", "Review pending refund requests");
        VBox root = new VBox(12); TableView<Object> table = dataTable(); Label state = new Label();
        Runnable reload = () -> loadInto(table, state, "RefundService", "getPendingRefunds");
        Button approve = primaryButton("Approve Selected");
        approve.setOnAction(e -> updateSimpleStatus(table, "RefundService", "updateRefundStatus", "getRefundId", "APPROVED", reload));
        Button reject = dangerButton("Reject Selected");
        reject.setOnAction(e -> updateSimpleStatus(table, "RefundService", "updateRefundStatus", "getRefundId", "REJECTED", reload));
        root.getChildren().addAll(new HBox(10, approve, reject), state, table); VBox.setVgrow(table, Priority.ALWAYS);
        show(root); reload.run();
    }

    private void showDeliveries() {
        setHeader("Deliveries", "Track dispatch and completion status");
        VBox root = new VBox(12);
        ComboBox<String> status = new ComboBox<>(FXCollections.observableArrayList("PENDING", "DISPATCHED", "IN_TRANSIT", "DELIVERED", "CANCELLED"));
        status.setValue("PENDING");
        TableView<Object> table = dataTable(); Label state = new Label();
        Runnable reload = () -> loadInto(table, state, "DeliveryService", "getDeliveriesByStatus", status.getValue());
        status.setOnAction(e -> reload.run());
        Button dispatch = primaryButton("Mark Dispatched");
        dispatch.setOnAction(e -> updateSingleId(table, "DeliveryService", "markAsDispatched", "getDeliveryId", reload));
        root.getChildren().addAll(new HBox(10, new Label("Status:"), status, dispatch), state, table); VBox.setVgrow(table, Priority.ALWAYS);
        show(root); reload.run();
    }

    private void showAdminLogs() {
        setHeader("Admin Logs", "Audit trail of administrative activity");
        simpleServiceTable("admin_logs", "getAllLogs");
    }

    private void showMarketPrices() {
        setHeader("Market Prices", "Lookup recent market price information");
        searchByIdPanel("MarketPriceService", "getMarketPriceById", "Market Price ID", null);
    }

    // ---------------------------------------------------------------------
    // GENERIC UI / REFLECTION ENGINE
    // ---------------------------------------------------------------------

    private void simpleServiceTable(String service, String method, Object... args) {
        VBox root = new VBox(12);
        TableView<Object> table = dataTable(); Label state = new Label();
        Button refresh = secondaryButton("Refresh");
        Runnable reload = () -> loadInto(table, state, service, method, args);
        refresh.setOnAction(e -> reload.run());
        root.getChildren().addAll(refresh, state, table); VBox.setVgrow(table, Priority.ALWAYS);
        show(root); reload.run();
    }

    private void showGenericList(String title, String subtitle, String service, String method, boolean needsId) {
        setHeader(title, subtitle);
        if (needsId) {
            searchByIdPanel(service, "getOrderById", "Order ID", null);
        } else simpleServiceTable(service, method);
    }

    private void searchByIdPanel(String service, String method, String prompt, RowActionFactory factory) {
        VBox root = new VBox(12);
        HBox bar = new HBox(10); TextField id = new TextField(); id.setPromptText(prompt); id.setPrefWidth(220); styleInput(id);
        Button search = primaryButton("Search");
        TableView<Object> table = dataTable(); Label state = new Label("Enter an ID to search.");
        search.setOnAction(e -> {
            Integer value = safeInt(id.getText());
            if (value == null) { alert("Invalid ID", "Enter a valid numeric ID."); return; }
            loadInto(table, state, service, method, value);
        });
        bar.getChildren().addAll(id, search);
        if (factory != null) {
            Node n = factory.create(table, null);
            if (n != null) bar.getChildren().add(n);
        }
        root.getChildren().addAll(bar, state, table); VBox.setVgrow(table, Priority.ALWAYS); show(root);
    }

    private CRUDView genericCrudView(String service, String listMethod, String model, boolean includeRefresh, ActionDef... defs) {
        VBox root = new VBox(12); HBox actions = new HBox(10); TableView<Object> table = dataTable(); Label state = new Label();
        Runnable reload = () -> loadInto(table, state, service, listMethod);
        if (includeRefresh) { Button refresh = secondaryButton("Refresh"); refresh.setOnAction(e -> reload.run()); actions.getChildren().add(refresh); }
        for (ActionDef d : defs) {
            Button b = d.primary ? primaryButton(d.label) : secondaryButton(d.label);
            b.setOnAction(e -> {
                Object selected = d.method.startsWith("update") ? table.getSelectionModel().getSelectedItem() : null;
                if (d.method.startsWith("update") && selected == null) { alert("Select a record", "Choose a row first."); return; }
                modelEditor(model, selected, obj -> invokeAndReload(service, d.method, obj, reload));
            });
            actions.getChildren().add(b);
        }
        root.getChildren().addAll(actions, state, table); VBox.setVgrow(table, Priority.ALWAYS); reload.run();
        return new CRUDView(root, actions, table, reload);
    }

    private void loadInto(TableView<Object> table, Label state, String service, String method, Object... args) {
        state.setText("Loading…");
        runAsync(() -> invoke(service, method, args), result -> {
            List<Object> rows = normalizeRows(result);
            populateTable(table, rows);
            state.setText(rows.isEmpty() ? "No records found." : rows.size() + " record" + (rows.size() == 1 ? "" : "s") + " found");
        }, ex -> {
            state.setText("Could not load records.");
            showError(ex);
        });
    }

    private List<Object> normalizeRows(Object result) {
        if (result == null) return new ArrayList<>();
        if (result instanceof Collection<?>) return new ArrayList<>((Collection<?>) result);
        if (result.getClass().isArray()) {
            int n = Array.getLength(result); List<Object> list = new ArrayList<>();
            for (int i=0;i<n;i++) list.add(Array.get(result,i));
            return list;
        }
        return new ArrayList<>(List.of(result));
    }

    private void populateTable(TableView<Object> table, List<Object> rows) {
        table.getColumns().clear();
        ObservableList<Object> data = FXCollections.observableArrayList(rows);
        table.setItems(data);
        if (rows.isEmpty()) return;
        Object sample = rows.get(0);
        List<Method> getters = readableGetters(sample.getClass());
        for (Method g : getters) {
            TableColumn<Object,Object> c = new TableColumn<>(pretty(propertyName(g)));
            c.setCellValueFactory(cd -> new ReadOnlyObjectWrapper<>(safeGetter(cd.getValue(), g)));
            c.setPrefWidth(Math.min(220, Math.max(105, c.getText().length()*9 + 30)));
            table.getColumns().add(c);
        }
    }

    private List<Method> readableGetters(Class<?> c) {
        List<Method> out = new ArrayList<>();
        for (Method m : c.getMethods()) {
            if (m.getParameterCount()!=0 || m.getDeclaringClass()==Object.class) continue;
            String n=m.getName();
            if ((n.startsWith("get") && n.length()>3) || (n.startsWith("is") && n.length()>2)) out.add(m);
        }
        out.sort(Comparator.comparingInt(m -> getterPriority(m.getName())));
        return out.size() > 10 ? out.subList(0,10) : out;
    }

    private int getterPriority(String n) {
        String s=n.toLowerCase();
        if(s.endsWith("id")) return 0; if(s.contains("name")) return 1; if(s.contains("status")) return 2; if(s.contains("email")) return 3; return 10;
    }

    private Object safeGetter(Object target, Method m) {
        try { Object v=m.invoke(target); return formatValue(v); } catch(Exception e){ return "—"; }
    }

    private Object formatValue(Object v) {
        if(v==null) return "—";
        String s=String.valueOf(v);
        return s.length()>70 ? s.substring(0,67)+"…" : s;
    }

    private String propertyName(Method g) {
        String n=g.getName(); if(n.startsWith("get")) n=n.substring(3); else if(n.startsWith("is")) n=n.substring(2); return n;
    }

    private TableView<Object> dataTable() {
        TableView<Object> t = new TableView<>();
        t.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        Label placeholder = new Label("No data to display");
        placeholder.setTextFill(Color.web(MUTED));
        placeholder.setStyle("-fx-font-style:italic;");
        t.setPlaceholder(placeholder);
        t.setStyle("-fx-background-color:" + CARD + "; -fx-border-color:" + BORDER + "; -fx-border-radius:10; -fx-background-radius:10; -fx-table-header-border-color: transparent;");
        t.setRowFactory(tv -> {
            TableRow<Object> row = new TableRow<>();
            row.setOnMouseClicked(e -> { if(e.getButton()==MouseButton.PRIMARY && e.getClickCount()==2 && !row.isEmpty()) showObjectDetails(row.getItem()); });
            return row;
        });
        return t;
    }

    private void showObjectDetails(Object obj) {
        Dialog<Void> d = new Dialog<>(); d.setTitle("Record Details"); d.getDialogPane().getButtonTypes().add(ButtonType.CLOSE);
        GridPane grid = new GridPane(); grid.setHgap(14); grid.setVgap(10); grid.setPadding(new Insets(15));
        int r=0;
        for(Method m: readableGettersAll(obj.getClass())) {
            Label k=new Label(pretty(propertyName(m))); k.setStyle("-fx-font-weight:bold; -fx-text-fill:" + INK + ";");
            Label v=new Label(String.valueOf(safeGetter(obj,m))); v.setWrapText(true); v.setMaxWidth(450); v.setTextFill(Color.web(INK));
            grid.add(k,0,r); grid.add(v,1,r++);
        }
        ScrollPane sp=new ScrollPane(grid); sp.setFitToWidth(true); sp.setPrefViewportHeight(500); sp.setPrefViewportWidth(650);
        d.getDialogPane().setContent(sp); d.getDialogPane().setStyle("-fx-background-color:" + CARD + ";");
        d.showAndWait();
    }

    private List<Method> readableGettersAll(Class<?> c) {
        List<Method> out=new ArrayList<>();
        for(Method m:c.getMethods()) if(m.getParameterCount()==0 && m.getDeclaringClass()!=Object.class && ((m.getName().startsWith("get")&&m.getName().length()>3)||(m.getName().startsWith("is")&&m.getName().length()>2))) out.add(m);
        out.sort(Comparator.comparing(Method::getName)); return out;
    }

    private void modelEditor(String simpleModelName, Object existing, Consumer<Object> onSave) {
        try {
            Class<?> cls = Class.forName("model." + simpleModelName);
            Object obj = existing != null ? existing : cls.getDeclaredConstructor().newInstance();
            Dialog<ButtonType> dialog = new Dialog<>(); dialog.setTitle((existing==null?"Add ":"Edit ")+pretty(simpleModelName));
            ButtonType save = new ButtonType("Save", ButtonBar.ButtonData.OK_DONE); dialog.getDialogPane().getButtonTypes().addAll(save, ButtonType.CANCEL);
            dialog.getDialogPane().setStyle("-fx-background-color:" + CARD + ";");
            GridPane grid = new GridPane(); grid.setHgap(12); grid.setVgap(10); grid.setPadding(new Insets(16));
            Map<Method,Control> fields = new LinkedHashMap<>(); int row=0;
            for(Method setter: editableSetters(cls)) {
                String prop=setter.getName().substring(3);
                Method getter=findGetter(cls,prop);
                Object current=getter==null?null:safeInvokeGetter(obj,getter);
                Class<?> type=setter.getParameterTypes()[0];
                Control field=createEditor(type,current);
                Label lbl = new Label(pretty(prop)); lbl.setTextFill(Color.web(INK));
                grid.add(lbl,0,row); grid.add(field,1,row++); fields.put(setter,field);
            }
            ScrollPane sp=new ScrollPane(grid); sp.setFitToWidth(true); sp.setPrefViewportWidth(560); sp.setPrefViewportHeight(560); dialog.getDialogPane().setContent(sp);
            dialog.showAndWait().filter(b->b==save).ifPresent(b->{
                try {
                    for(Map.Entry<Method,Control> en:fields.entrySet()) {
                        Object value=readEditor(en.getValue(),en.getKey().getParameterTypes()[0]);
                        if(value!=SKIP) en.getKey().invoke(obj,value);
                    }
                    onSave.accept(obj);
                } catch(Exception ex){ showError(ex); }
            });
        } catch(Exception ex) { showError(ex); }
    }

    private static final Object SKIP = new Object();

    private List<Method> editableSetters(Class<?> cls) {
        List<Method> ms=new ArrayList<>();
        for(Method m:cls.getMethods()) {
            if(!m.getName().startsWith("set")||m.getParameterCount()!=1) continue;
            String n=m.getName().toLowerCase();
            if(n.contains("createdat")||n.contains("updatedat")) continue;
            ms.add(m);
        }
        ms.sort(Comparator.comparing(Method::getName)); return ms;
    }

    private Method findGetter(Class<?> cls,String prop) {
        for(String n:List.of("get"+prop,"is"+prop)) try{return cls.getMethod(n);}catch(Exception ignored){}
        return null;
    }

    private Object safeInvokeGetter(Object obj,Method getter){ try{return getter.invoke(obj);}catch(Exception e){return null;} }

    private Control createEditor(Class<?> type,Object value) {
        if(type==boolean.class||type==Boolean.class){ CheckBox cb=new CheckBox(); cb.setSelected(Boolean.TRUE.equals(value)); return cb; }
        if(type.isEnum()){ ComboBox<String> cb=new ComboBox<>(); for(Object e:type.getEnumConstants()) cb.getItems().add(String.valueOf(e)); if(value!=null) cb.setValue(String.valueOf(value)); return cb; }
        TextField f=new TextField(value==null?"":String.valueOf(value)); styleInput(f); return f;
    }

    private Object readEditor(Control c,Class<?> type) {
        if(c instanceof CheckBox) return ((CheckBox)c).isSelected();
        String s=c instanceof TextInputControl?((TextInputControl)c).getText().trim():c instanceof ComboBox?String.valueOf(((ComboBox<?>)c).getValue()):"";
        if(s.isEmpty() && !type.isPrimitive()) return null;
        if(type==String.class) return s;
        if(type==int.class||type==Integer.class) return Integer.parseInt(s);
        if(type==long.class||type==Long.class) return Long.parseLong(s);
        if(type==double.class||type==Double.class) return Double.parseDouble(s);
        if(type==BigDecimal.class) return new BigDecimal(s);
        if(type==Date.class) return Date.valueOf(s);
        if(type==Timestamp.class) return Timestamp.valueOf(s);
        if(type==LocalDate.class) return LocalDate.parse(s);
        if(type.isEnum()) return Enum.valueOf((Class)type,s);
        return SKIP;
    }

    private void invokeAndReload(String service,String method,Object obj,Runnable reload){
        runAsync(() -> invoke(service,method,obj), x->{ toast("Saved successfully",false); reload.run(); }, this::showError);
    }

    private Object invoke(String serviceSimpleName, String methodName, Object... args) {
        try {
            Class<?> serviceClass = findServiceClass(serviceSimpleName);
            Object service = serviceClass.getDeclaredConstructor().newInstance();
            Method method = findCompatibleMethod(serviceClass, methodName, args);
            if(method==null) throw new NoSuchMethodException(serviceClass.getName()+"."+methodName+" with "+args.length+" argument(s)");
            return method.invoke(service,args);
        } catch(InvocationTargetException e){
            Throwable cause=e.getCause()==null?e:e.getCause();
            throw new RuntimeException(cause.getMessage()==null?cause.toString():cause.getMessage(),cause);
        } catch(Exception e){ throw new RuntimeException(e.getMessage()==null?e.toString():e.getMessage(),e); }
    }

    private Class<?> findServiceClass(String simple) throws ClassNotFoundException {
        List<String> names = new ArrayList<>();
        if(simple.contains(".")) names.add(simple);
        names.add("services." + simple);
        names.add("service." + simple);
        names.add("dao." + simple); // updated DAO classes such as supplier_profiles / supplier_products
        for (String n : names) {
            try {
                return Class.forName(n);
            } catch (ClassNotFoundException ignored) {
            }
        }
        throw new ClassNotFoundException(
                "Backend class not found: " + simple +
                        " (checked services.*, service.* and dao.*)"
        );
    }

    private Method findCompatibleMethod(Class<?> cls,String name,Object[] args){
        outer: for(Method m:cls.getMethods()){
            if(!m.getName().equals(name)||m.getParameterCount()!=args.length) continue;
            Class<?>[] pts=m.getParameterTypes();
            for(int i=0;i<pts.length;i++) if(args[i]!=null&&!wrap(pts[i]).isAssignableFrom(wrap(args[i].getClass()))) continue outer;
            return m;
        }
        return null;
    }

    private Class<?> wrap(Class<?> c){
        if(!c.isPrimitive()) return c;
        if(c==int.class)return Integer.class; if(c==long.class)return Long.class; if(c==double.class)return Double.class; if(c==boolean.class)return Boolean.class; if(c==float.class)return Float.class; if(c==short.class)return Short.class; if(c==byte.class)return Byte.class; if(c==char.class)return Character.class; return c;
    }

    private void runAsync(Supplier<Object> work, Consumer<Object> success, Consumer<Throwable> fail) {
        Task<Object> task = new Task<>() { @Override protected Object call() { return work.get(); } };
        task.setOnSucceeded(e -> success.accept(task.getValue()));
        task.setOnFailed(e -> fail.accept(task.getException()));
        Thread th = new Thread(task, "farmersin-ui-db"); th.setDaemon(true); th.start();
    }

    // ---------------------------------------------------------------------
    // HELPERS
    // ---------------------------------------------------------------------

    private void updateSimpleStatus(TableView<Object> table,String service,String method,String idGetter,String status,Runnable reload){
        Object row=table.getSelectionModel().getSelectedItem(); if(row==null){alert("Select a record","Choose a row first.");return;}
        Integer id=firstInt(row,idGetter,"getId"); if(id==null)return;
        runAsync(()->invoke(service,method,id,status),x->{toast("Status updated",false);reload.run();},this::showError);
    }

    private void updateSingleId(TableView<Object> table,String service,String method,String idGetter,Runnable reload){
        Object row=table.getSelectionModel().getSelectedItem(); if(row==null){alert("Select a record","Choose a row first.");return;}
        Integer id=firstInt(row,idGetter,"getId"); if(id==null)return;
        runAsync(()->invoke(service,method,id),x->{toast("Updated successfully",false);reload.run();},this::showError);
    }

    private Integer extractInt(Object obj,String getter){
        if(obj==null)return null; try{Object v=obj.getClass().getMethod(getter).invoke(obj);return v instanceof Number?((Number)v).intValue():safeInt(String.valueOf(v));}catch(Exception e){return null;}
    }
    private Integer firstInt(Object obj,String...getters){for(String g:getters){Integer i=extractInt(obj,g);if(i!=null)return i;}return null;}
    private Boolean firstBoolean(Object obj,String... getters){ for(String g:getters)try{Object v=obj.getClass().getMethod(g).invoke(obj);if(v instanceof Boolean)return(Boolean)v;}catch(Exception ignored){}return null; }
    private void setIfPossible(Object obj,String setter,Object value){try{Method m=findCompatibleMethod(obj.getClass(),setter,new Object[]{value});if(m!=null)m.invoke(obj,value);}catch(Exception ignored){}}
    private Integer safeInt(String s){try{return Integer.parseInt(s.trim());}catch(Exception e){return null;}}

    private String extractString(Object obj, String getter) {
        if (obj == null) return null;
        try {
            Object value = obj.getClass().getMethod(getter).invoke(obj);
            return value == null ? null : String.valueOf(value);
        } catch (Exception e) {
            return null;
        }
    }

    private boolean passwordMatches(String entered, String stored) {
        if (entered == null || stored == null) return false;

        // Supports projects that currently store the password directly.
        if (entered.equals(stored)) return true;

        // Also supports a common SHA-256 hex password hash without requiring
        // any additional dependency or backend-file change.
        try {
            java.security.MessageDigest md = java.security.MessageDigest.getInstance("SHA-256");
            byte[] digest = md.digest(entered.getBytes(java.nio.charset.StandardCharsets.UTF_8));
            StringBuilder hex = new StringBuilder();
            for (byte b : digest) hex.append(String.format("%02x", b));
            return hex.toString().equalsIgnoreCase(stored.trim());
        } catch (Exception ignored) {
            return false;
        }
    }

    private String cleanErrorMessage(Throwable ex) {
        if (ex == null) return "Unable to sign in.";
        String message = ex.getMessage();
        if (message == null || message.isBlank()) return "Unable to sign in.";
        if (message.toLowerCase().contains("user not found")) return "Incorrect email or password.";
        return message;
    }

    private Node wrappedTable(String title,TableView<Object> table,Label state){VBox v=new VBox(8);Label h=new Label(title);h.setStyle("-fx-font-weight:bold;-fx-font-size:15px;-fx-text-fill:"+INK+";");v.getChildren().addAll(h,state,table);VBox.setVgrow(table,Priority.ALWAYS);v.setPadding(new Insets(10));return v;}

    private Node statCard(String title,String value,String note){
        VBox v=new VBox(5);v.setPrefWidth(235);v.setPadding(new Insets(18));v.setStyle(cardStyle());v.setEffect(softShadow());
        Region accent = new Region(); accent.setPrefHeight(4); accent.setMaxWidth(Double.MAX_VALUE);
        accent.setStyle("-fx-background-color:" + WHEAT + "; -fx-background-radius:3;");
        Label t=new Label(title.toUpperCase());t.setTextFill(Color.web(MUTED));t.setStyle("-fx-font-size:11px;-fx-font-weight:bold;");
        Label val=new Label(value);val.setFont(Font.font("Georgia",FontWeight.BOLD,24));val.setTextFill(Color.web(INK));
        Label n=new Label(note);n.setTextFill(Color.web(MUTED));n.setStyle("-fx-font-size:12px;");
        v.getChildren().addAll(accent,t,val,n);return v;
    }
    private VBox panel(String title,String text){VBox v=new VBox(8);v.setPadding(new Insets(22));v.setStyle(cardStyle());v.setEffect(softShadow());Label h=new Label(title);h.setFont(Font.font("Georgia",FontWeight.BOLD,18));h.setTextFill(Color.web(INK));Label b=new Label(text);b.setWrapText(true);b.setTextFill(Color.web(MUTED));v.getChildren().addAll(h,b);return v;}
    private Node actionCard(String title,String text,Runnable action){VBox v=panel(title,text);v.setMinWidth(250);v.setOnMouseClicked(e->action.run());v.setStyle(cardStyle()+"-fx-cursor:hand;-fx-border-color:"+WHEAT+";-fx-border-width:1.4;");return v;}
    private Node labeled(String title,Node field){Label l=new Label(title);l.setTextFill(Color.web(INK));l.setStyle("-fx-font-weight:bold;");VBox v=new VBox(6,l,field);return v;}

    private void show(Node node){content.getChildren().setAll(node);}
    private void setHeader(String title,String subtitle){pageTitle.setText(title);pageSubtitle.setText(subtitle);}
    private void showMissingProfile(String kind){show(panel(kind+" profile not resolved","Enter the portal again with a valid User ID that has an existing "+kind.toLowerCase()+" profile."));}

    private Button primaryButton(String text){Button b=new Button(text);b.setStyle("-fx-background-color:"+SOIL_DARK+";-fx-text-fill:"+WHEAT_SOFT+";-fx-font-weight:bold;-fx-padding:10 16;-fx-background-radius:8;-fx-cursor:hand;");
        b.setOnMouseEntered(e->b.setStyle("-fx-background-color:"+BARK+";-fx-text-fill:"+WHEAT_SOFT+";-fx-font-weight:bold;-fx-padding:10 16;-fx-background-radius:8;-fx-cursor:hand;"));
        b.setOnMouseExited(e->b.setStyle("-fx-background-color:"+SOIL_DARK+";-fx-text-fill:"+WHEAT_SOFT+";-fx-font-weight:bold;-fx-padding:10 16;-fx-background-radius:8;-fx-cursor:hand;"));
        return b;}
    private Button secondaryButton(String text){Button b=new Button(text);b.setStyle("-fx-background-color:"+CARD+";-fx-text-fill:"+INK+";-fx-font-weight:bold;-fx-padding:9 15;-fx-background-radius:8;-fx-border-radius:8;-fx-border-color:"+BORDER+";-fx-cursor:hand;");
        b.setOnMouseEntered(e->b.setStyle("-fx-background-color:"+WHEAT_SOFT+";-fx-text-fill:"+INK+";-fx-font-weight:bold;-fx-padding:9 15;-fx-background-radius:8;-fx-border-radius:8;-fx-border-color:"+WHEAT+";-fx-cursor:hand;"));
        b.setOnMouseExited(e->b.setStyle("-fx-background-color:"+CARD+";-fx-text-fill:"+INK+";-fx-font-weight:bold;-fx-padding:9 15;-fx-background-radius:8;-fx-border-radius:8;-fx-border-color:"+BORDER+";-fx-cursor:hand;"));
        return b;}
    private Button dangerButton(String text){Button b=new Button(text);b.setStyle("-fx-background-color:"+DANGER_BG+";-fx-text-fill:"+DANGER_FG+";-fx-font-weight:bold;-fx-padding:9 15;-fx-background-radius:8;-fx-cursor:hand;");return b;}
    private void styleInput(TextInputControl f){f.setStyle("-fx-background-color:"+CARD+";-fx-text-fill:"+INK+";-fx-border-color:"+BORDER+";-fx-border-radius:8;-fx-background-radius:8;-fx-padding:9 11;");}
    private void styleRoleToggle(ToggleButton b){
        String off="-fx-background-color:"+WHEAT_SOFT+";-fx-text-fill:"+INK+";-fx-background-radius:8;-fx-padding:10 14;-fx-cursor:hand;-fx-font-weight:bold;";
        String on="-fx-background-color:"+SOIL_DARK+";-fx-text-fill:"+WHEAT_SOFT+";-fx-font-weight:bold;-fx-background-radius:8;-fx-padding:10 14;-fx-cursor:hand;";
        b.setStyle(off);
        b.selectedProperty().addListener((o,a,s)->b.setStyle(s?on:off));
    }
    private String navStyle(boolean active){return active?"-fx-background-color:"+WHEAT+";-fx-text-fill:"+INK+";-fx-font-weight:bold;-fx-background-radius:8;-fx-padding:10 13;-fx-cursor:hand;":"-fx-background-color:transparent;-fx-text-fill:"+WHEAT_SOFT+";-fx-background-radius:8;-fx-padding:10 13;-fx-cursor:hand;-fx-font-weight:bold;";}
    private String cardStyle(){return "-fx-background-color:"+CARD+";-fx-background-radius:14;-fx-border-radius:14;-fx-border-color:"+BORDER+";";}
    private DropShadow softShadow(){DropShadow ds=new DropShadow();ds.setColor(Color.web(SOIL_DARK,0.16));ds.setRadius(18);ds.setOffsetY(6);return ds;}

    private String pretty(String raw){
        if(raw==null)return ""; String s=raw.replace('_',' '); s=s.replaceAll("([a-z0-9])([A-Z])","$1 $2");
        StringBuilder out=new StringBuilder(); for(String p:s.trim().split("\\s+")){if(p.isEmpty())continue;if(out.length()>0)out.append(' ');out.append(Character.toUpperCase(p.charAt(0))).append(p.substring(1).toLowerCase());}return out.toString();
    }

    private void alert(String title,String message){Alert a=new Alert(Alert.AlertType.INFORMATION);a.setTitle("FarmersIn");a.setHeaderText(title);a.setContentText(message);a.getDialogPane().setStyle("-fx-background-color:"+CARD+";");a.showAndWait();}
    private void showError(Throwable ex){String msg=ex==null?"Unknown error":ex.getMessage();if(msg==null)msg=ex.toString();Alert a=new Alert(Alert.AlertType.ERROR);a.setTitle("FarmersIn");a.setHeaderText("Unable to complete this action");a.setContentText(msg);a.getDialogPane().setStyle("-fx-background-color:"+CARD+";");a.showAndWait();}
    private void toast(String message,boolean error){alert(error?"Action failed":"Success",message);}

    private static class ActionDef {String label,method;boolean primary;ActionDef(String l,String m,boolean p){label=l;method=m;primary=p;}}
    private static class CRUDView {VBox root;HBox actions;TableView<Object> table;Runnable reload;CRUDView(VBox r,HBox a,TableView<Object> t,Runnable x){root=r;actions=a;table=t;reload=x;}}
    private interface RowActionFactory {Node create(TableView<Object> table,Object row);}

    public static void main(String[] args) {
        Application.launch(FxLauncher.class, args);
    }

    /**
     * Internal JavaFX launcher. Keeping Application as a nested class prevents
     * the plain Java launcher from treating FarmersInFrontend itself as a
     * JavaFX application before main() gets a chance to run.
     */
    public static class FxLauncher extends Application {
        @Override
        public void start(Stage primaryStage) {
            new frontend().startUi(primaryStage);
        }
    }
}