package frontend;import services.*;import model.*;import javafx.application.Application;import javafx.beans.property.SimpleObjectProperty;import javafx.collections.FXCollections;import javafx.collections.ObservableList;import javafx.geometry.Insets;import javafx.geometry.Pos;import javafx.scene.Scene;import javafx.scene.Node;import javafx.scene.Cursor;import javafx.scene.control.*;import javafx.scene.layout.*;import javafx.stage.Stage;

import java.math.BigDecimal;import java.sql.SQLException;import java.util.List;import java.util.Optional;import java.util.LinkedHashMap;import java.util.Map;import java.util.function.Supplier;import java.nio.charset.StandardCharsets;import java.nio.file.Files;import java.nio.file.Path;

/**
 * Frontend.java (JavaFX edition)
 *
 * A single-file JavaFX desktop client that drives your existing "services" package
 * (UserService, ProductService, OrderService, ...) directly - no REST layer needed.
 *
 * HOW TO USE
 *
 * Drop this file into your src folder (default package, next to "services", "dao", "model").
 *
 * Make sure JavaFX is on your module/class path (IntelliJ: Project Structure > Libraries,
 *  plus VM options like --module-path /path/to/javafx-sdk/lib --add-modules javafx.controls
 *  if you're on a non-modular classpath project - IntelliJ's "Run/Debug Configuration" wizard
 *  for JavaFX projects sets this up for you).
 *
 * Make sure your DB connection (used inside the DAOs) is configured and reachable.
 *
 * Run Frontend.main().
 *
 * IMPORTANT - PLEASE READ
 * Your model classes (User, Address, Product, Order, ...) weren't included in what you shared -
 * only the service layer. I inferred every getter/setter name from how each service method
 * uses it (e.g. address.getAddressLine(), order.setBuyerId(...)). If a real model class uses a
 * different name, you'll get a compile error on that one line - just rename the call to match.
 *
 * There is no CartService / Cart model in what you gave me (only CartItemService, which
 * operates on an existing cartId). I treat "cartId == buyerId" as a simple stand-in so every
 * buyer has one running cart. Swap getOrCreateCartId(...) if you have a real carts table.
 *
 * Login does a lookup by email and a direct string compare against passwordHash, since no
 * hashing/verification method was shown in UserService. Replace with real hash checking
 * (e.g. BCrypt.checkpw) before using this for anything real.
 */
public class FarmersInFrontEnd extends Application {

    // ---- Services (all reused from your services package) -----------------
    private final UserService userService = new UserService();
    private final address addressService = new address();
    private final CategoryService categoryService = new CategoryService();
    private final SubcategoryService subcategoryService = new SubcategoryService();
    private final ProductService productService = new ProductService();
    private final SupplierProductService supplierProductService = new SupplierProductService();
    private final buyerprofile buyerProfileService = new buyerprofile();
    private final SupplierProfileService supplierProfileService = new SupplierProfileService();
    private final CartItemService cartItemService = new CartItemService();
    private final OrderService orderService = new OrderService();
    private final service.OrderItemService orderItemService = new service.OrderItemService();
    private final PaymentService paymentService = new PaymentService();
    private final DeliveryService deliveryService = new DeliveryService();
    private final ReviewService reviewService = new ReviewService();
    private final ComplaintService complaintService = new ComplaintService();
    private final ConversationService conversationService = new ConversationService();
    private final MessageService messageService = new MessageService();
    private final service.NotificationService notificationService = new service.NotificationService();
    private final admin_logs adminLogService = new admin_logs();
    private final MarketPriceService marketPriceService = new MarketPriceService();
    private final RefundService refundService = new RefundService();
    private final SeasonalAvailabilityService seasonalAvailabilityService = new SeasonalAvailabilityService();

    // ---- Session state ------------------------------------------------------
    private User currentUser;
    private BuyerProfile currentBuyerProfile;
    private SupplierProfile currentSupplierProfile;
    private Stage primaryStage;

    public static void main(String[] args) {
        launch(args);
    }

    @Override
    public void start(Stage stage) {
        this.primaryStage = stage;
        stage.setTitle("FarmersIn");
        stage.setMinWidth(1120);
        stage.setMinHeight(720);
        stage.setScene(buildLoginScene());
        stage.setMaximized(true);
        stage.show();
    }

// =========================================================================
//  LOGIN / REGISTER
// =========================================================================

    private Scene buildLoginScene() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-background");

        VBox brand = new VBox(18);
        brand.setAlignment(Pos.CENTER_LEFT);
        brand.setPadding(new Insets(70));
        brand.setPrefWidth(600);

        Label badge = new Label("FARM TO BUSINESS");
        badge.getStyleClass().add("brand-badge");
        Label brandTitle = new Label("Fresh produce.\nTrusted suppliers.\nOne simple market.");
        brandTitle.getStyleClass().add("brand-title");
        brandTitle.setWrapText(true);
        Label brandText = new Label("FarmersIn connects verified farmers and dairy suppliers with restaurants, shops and business buyers.");
        brandText.getStyleClass().add("brand-copy");
        brandText.setWrapText(true);
        brandText.setMaxWidth(470);

        HBox featureRow = new HBox(12,
                featureChip("✓ Verified sellers"),
                featureChip("✓ Fresh categories"),
                featureChip("✓ Secure orders"));
        featureRow.setAlignment(Pos.CENTER_LEFT);
        brand.getChildren().addAll(badge, brandTitle, brandText, featureRow);

        VBox card = new VBox(16);
        card.getStyleClass().add("glass-card");
        card.setPadding(new Insets(34));
        card.setMaxWidth(430);

        Label welcome = new Label("Welcome back");
        welcome.getStyleClass().add("form-title");
        Label subtitle = new Label("Sign in to continue to your FarmersIn workspace.");
        subtitle.getStyleClass().add("muted-text");
        subtitle.setWrapText(true);

        TextField emailField = styledTextField("Email address");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("modern-field");

        Button loginBtn = primaryButton("Log in");
        loginBtn.setMaxWidth(Double.MAX_VALUE);
        Button registerBtn = secondaryButton("Create buyer or supplier account");
        registerBtn.setMaxWidth(Double.MAX_VALUE);

        Label security = new Label("Admin accounts are created only in the database by the system administrator.");
        security.getStyleClass().add("hint-text");
        security.setWrapText(true);

        loginBtn.setOnAction(e -> {
            try {
                User user = userService.getUserByEmail(emailField.getText().trim());
                String entered = passwordField.getText();
                if (user == null || user.getPasswordHash() == null || !user.getPasswordHash().equals(entered)) {
                    throw new IllegalArgumentException("Incorrect email or password.");
                }
                onLoginSuccess(user);
            } catch (Exception ex) {
                showError(ex);
            }
        });
        passwordField.setOnAction(e -> loginBtn.fire());
        registerBtn.setOnAction(e -> primaryStage.setScene(buildRegisterScene()));

        card.getChildren().addAll(welcome, subtitle, new Separator(), emailField, passwordField, loginBtn, registerBtn, security);

        StackPane cardHolder = new StackPane(card);
        cardHolder.setPadding(new Insets(50));
        root.setLeft(brand);
        root.setCenter(cardHolder);

        Scene scene = new Scene(root, 1280, 800);
        applyTheme(scene);
        return scene;
    }

    private Scene buildRegisterScene() {
        BorderPane root = new BorderPane();
        root.getStyleClass().add("app-background");

        VBox card = new VBox(14);
        card.getStyleClass().add("glass-card");
        card.setPadding(new Insets(34));
        card.setMaxWidth(520);

        Label title = new Label("Create your FarmersIn account");
        title.getStyleClass().add("form-title");
        Label subtitle = new Label("Choose the role that matches your work. The admin will verify your profile.");
        subtitle.getStyleClass().add("muted-text");
        subtitle.setWrapText(true);

        TextField nameField = styledTextField("Full name");
        TextField emailField = styledTextField("Email address");
        PasswordField passwordField = new PasswordField();
        passwordField.setPromptText("Password");
        passwordField.getStyleClass().add("modern-field");
        TextField phoneField = styledTextField("Phone (optional)");
        ComboBox<String> roleBox = new ComboBox<>(FXCollections.observableArrayList("BUYER", "SUPPLIER"));
        roleBox.getSelectionModel().selectFirst();
        roleBox.setMaxWidth(Double.MAX_VALUE);
        roleBox.getStyleClass().add("modern-field");

        Button createBtn = primaryButton("Create account");
        Button backBtn = secondaryButton("Back to login");
        createBtn.setMaxWidth(Double.MAX_VALUE);
        backBtn.setMaxWidth(Double.MAX_VALUE);

        createBtn.setOnAction(e -> {
            try {
                if (nameField.getText().isBlank() || emailField.getText().isBlank() || passwordField.getText().isBlank()) {
                    throw new IllegalArgumentException("Name, email and password are required.");
                }
                User user = new User();
                user.setFullName(nameField.getText().trim());
                user.setEmail(emailField.getText().trim());
                user.setPasswordHash(passwordField.getText());
                user.setPhone(phoneField.getText().isBlank() ? null : phoneField.getText().trim());
                user.setRole(roleBox.getValue());
                userService.addUser(user);
                info("Account created. Your profile will remain pending until approved by an admin.");
                primaryStage.setScene(buildLoginScene());
            } catch (Exception ex) {
                showError(ex);
            }
        });
        backBtn.setOnAction(e -> primaryStage.setScene(buildLoginScene()));

        card.getChildren().addAll(title, subtitle, new Separator(), nameField, emailField, passwordField,
                phoneField, new Label("Account type"), roleBox, createBtn, backBtn);
        StackPane holder = new StackPane(card);
        holder.setPadding(new Insets(50));
        root.setCenter(holder);

        Scene scene = new Scene(root, 1280, 800);
        applyTheme(scene);
        return scene;
    }

    private void onLoginSuccess(User user) {
        this.currentUser = user;
        try {
            if ("BUYER".equalsIgnoreCase(user.getRole())) {
                try { currentBuyerProfile = buyerProfileService.getBuyerByUserId(user.getUserId()); }
                catch (Exception ignore) { currentBuyerProfile = null; }
            } else if ("SUPPLIER".equalsIgnoreCase(user.getRole())) {
                try { currentSupplierProfile = supplierProfileService.getSupplierByUserId(user.getUserId()); }
                catch (Exception ignore) { currentSupplierProfile = null; }
            }
        } catch (Exception ignore) { /* profile may not exist yet - that's fine */ }

        primaryStage.setScene(buildDashboardScene(user.getRole().toUpperCase()));
    }

// =========================================================================
//  DASHBOARD SHELL (role switcher + tabs)
// =========================================================================

    private Scene buildDashboardScene(String initialViewRole) {
        BorderPane shell = new BorderPane();
        shell.getStyleClass().add("dashboard-background");

        VBox sidebar = new VBox(10);
        sidebar.getStyleClass().add("sidebar");
        sidebar.setPadding(new Insets(26, 18, 24, 18));
        sidebar.setPrefWidth(250);

        HBox logoRow = new HBox(10);
        logoRow.setAlignment(Pos.CENTER_LEFT);
        Label logoMark = new Label("F");
        logoMark.getStyleClass().add("sidebar-logo-mark");
        Label logo = new Label("FarmersIn");
        logo.getStyleClass().add("sidebar-logo");
        logoRow.getChildren().addAll(logoMark, logo);

        Label role = new Label(currentUser.getRole().toUpperCase() + " WORKSPACE");
        role.getStyleClass().add("sidebar-role");

        VBox navigation = new VBox(4);
        VBox.setVgrow(navigation, Priority.ALWAYS);

        StackPane content = new StackPane();
        content.setPadding(new Insets(22));

        Map<String, Supplier<Node>> views = buildViewsForRole(currentUser.getRole().toUpperCase());
        if (views.isEmpty()) {
            Label empty = new Label("No workspace is available for this account role.");
            empty.getStyleClass().add("muted-text");
            content.getChildren().setAll(empty);
        } else {
            final Button[] selected = new Button[1];
            views.forEach((name, supplier) -> {
                Button nav = new Button(navIcon(name) + "   " + name);
                nav.getStyleClass().add("nav-button");
                nav.setMaxWidth(Double.MAX_VALUE);
                nav.setAlignment(Pos.CENTER_LEFT);
                nav.setOnAction(e -> {
                    if (selected[0] != null) selected[0].getStyleClass().remove("nav-button-selected");
                    if (!nav.getStyleClass().contains("nav-button-selected")) nav.getStyleClass().add("nav-button-selected");
                    selected[0] = nav;
                    Node view = supplier.get();
                    view.getStyleClass().add("content-card");
                    content.getChildren().setAll(view);
                });
                navigation.getChildren().add(nav);
                if (selected[0] == null) {
                    selected[0] = nav;
                    nav.getStyleClass().add("nav-button-selected");
                    Node view = supplier.get();
                    view.getStyleClass().add("content-card");
                    content.getChildren().setAll(view);
                }
            });
        }

        Button logoutBtn = secondaryButton("⎋   Log out");
        logoutBtn.setMaxWidth(Double.MAX_VALUE);
        logoutBtn.setOnAction(e -> {
            currentUser = null;
            currentBuyerProfile = null;
            currentSupplierProfile = null;
            primaryStage.setScene(buildLoginScene());
        });

        Region sidebarSpacer = new Region();
        VBox.setVgrow(sidebarSpacer, Priority.ALWAYS);

        sidebar.getChildren().addAll(logoRow, role, new Separator(), navigation, sidebarSpacer, logoutBtn);

        HBox header = new HBox(16);
        header.getStyleClass().add("top-header");
        header.setPadding(new Insets(18, 26, 18, 26));
        header.setAlignment(Pos.CENTER_LEFT);

        Label avatar = new Label(initials(currentUser.getFullName()));
        avatar.getStyleClass().add("avatar-circle");

        VBox greetBox = new VBox(1);
        Label greeting = new Label("Welcome back, " + firstName(currentUser.getFullName()));
        greeting.getStyleClass().add("header-title");
        Label greetSub = new Label(currentUser.getEmail());
        greetSub.getStyleClass().add("header-subtitle");
        greetBox.getChildren().addAll(greeting, greetSub);

        Region spacer = new Region();
        HBox.setHgrow(spacer, Priority.ALWAYS);
        Label account = new Label(currentUser.getRole().toUpperCase());
        account.getStyleClass().add("account-pill");
        header.getChildren().addAll(avatar, greetBox, spacer, account);

        BorderPane main = new BorderPane();
        main.setTop(header);
        main.setCenter(content);
        shell.setLeft(sidebar);
        shell.setCenter(main);

        Scene scene = new Scene(shell, 1400, 860);
        applyTheme(scene);
        return scene;
    }

    private String navIcon(String viewName) {
        return switch (viewName) {
            case "Browse Products" -> "🛒";
            case "My Cart" -> "🧺";
            case "My Orders" -> "📦";
            case "Addresses" -> "📍";
            case "Messages" -> "💬";
            case "Notifications" -> "🔔";
            case "Complaints" -> "⚠";
            case "My Profile" -> "👤";
            case "My Listings" -> "🌿";
            case "Incoming Orders" -> "📥";
            case "Deliveries" -> "🚚";
            case "Reviews" -> "⭐";
            case "Supplier Approvals" -> "✅";
            case "Buyer Approvals" -> "✅";
            case "Categories" -> "🏷";
            case "Refunds" -> "💵";
            case "Market Prices" -> "📈";
            case "Admin Logs" -> "🗂";
            default -> "•";
        };
    }

    private String initials(String fullName) {
        if (fullName == null || fullName.isBlank()) return "?";
        String[] parts = fullName.trim().split("\\s+");
        String first = parts[0].substring(0, 1);
        String last = parts.length > 1 ? parts[parts.length - 1].substring(0, 1) : "";
        return (first + last).toUpperCase();
    }

    private String firstName(String fullName) {
        if (fullName == null || fullName.isBlank()) return "";
        return fullName.trim().split("\\s+")[0];
    }

    private Map<String, Supplier<Node>> buildViewsForRole(String role) {
        Map<String, Supplier<Node>> views = new LinkedHashMap<>();
        switch (role) {
            case "BUYER" -> {
                views.put("Browse Products", this::buildBrowseProductsPanel);
                views.put("My Cart", this::buildCartPanel);
                views.put("My Orders", this::buildBuyerOrdersPanel);
                views.put("Addresses", this::buildAddressesPanel);
                views.put("Messages", this::buildMessagesPanel);
                views.put("Notifications", this::buildNotificationsPanel);
                views.put("Complaints", () -> buildComplaintsPanel(true));
            }
            case "SUPPLIER" -> {
                views.put("My Profile", this::buildSupplierProfilePanel);
                views.put("My Listings", this::buildSupplierListingsPanel);
                views.put("Incoming Orders", this::buildSupplierOrdersPanel);
                views.put("Deliveries", this::buildDeliveriesPanel);
                views.put("Reviews", this::buildSupplierReviewsPanel);
                views.put("Messages", this::buildMessagesPanel);
            }
            case "ADMIN" -> {
                views.put("Supplier Approvals", this::buildVerifySuppliersPanel);
                views.put("Buyer Approvals", this::buildVerifyBuyersPanel);
                views.put("Categories", this::buildCategoriesPanel);
                views.put("Complaints", () -> buildComplaintsPanel(false));
                views.put("Refunds", this::buildRefundsPanel);
                views.put("Market Prices", this::buildMarketPricesPanel);
                views.put("Admin Logs", this::buildAdminLogsPanel);
            }
        }
        return views;
    }

// =========================================================================
//  BUYER: BROWSE PRODUCTS
// =========================================================================

    private BorderPane buildBrowseProductsPanel() {
        BorderPane panel = new BorderPane();
        VBox heading = sectionHeading("Browse Products", "Explore everything currently listed on the marketplace.");
        TableView<Row> table = buildTable("Product ID", "Name", "Category ID", "Default Unit", "Description");

        Runnable refresh = () -> {
            ObservableList<Row> rows = FXCollections.observableArrayList();
            try {
                for (Product p : productService.getAllActiveProducts()) {
                    rows.add(new Row(p.getProductId(), p.getProductName(), p.getCategoryId(),
                            p.getDefaultUnit(), p.getDescription()));
                }
            } catch (Exception ex) { showError(ex); }
            table.setItems(rows);
        };
        refresh.run();

        Button refreshBtn = toolbarButton("↻ Refresh");
        refreshBtn.setOnAction(e -> refresh.run());

        Button viewListingsBtn = toolbarButtonPrimary("View Supplier Listings");
        viewListingsBtn.setOnAction(e -> {
            Row row = table.getSelectionModel().getSelectedItem();
            if (row == null) { info("Select a product first."); return; }
            showSupplierListingsDialog((int) row.get(0));
        });

        HBox top = toolbar(refreshBtn, viewListingsBtn);

        VBox topBox = new VBox(10, heading, top);
        panel.setTop(topBox);
        panel.setCenter(table);
        return panel;
    }

    private void showSupplierListingsDialog(int productId) {
        Stage dialog = new Stage();
        dialog.setTitle("Supplier Listings");
        BorderPane root = new BorderPane();
        root.getStyleClass().add("dialog-background");

        TableView<Row> table = buildTable("Listing ID", "Supplier ID", "Price/Unit", "Available Qty", "Min Order Qty", "Unit", "Grade");
        ObservableList<Row> rows = FXCollections.observableArrayList();
        try {
            for (SupplierProduct sp : supplierProductService.getApprovedByProductId(productId)) {
                rows.add(new Row(sp.getSupplierProductId(), sp.getSupplierId(), sp.getPricePerUnit(),
                        sp.getAvailableQuantity(), sp.getMinimumOrderQuantity(), sp.getUnitType(), sp.getQualityGrade()));
            }
        } catch (Exception ex) { showError(ex); }
        table.setItems(rows);

        Button addToCartBtn = primaryButton("Add Selected to Cart");
        addToCartBtn.setOnAction(e -> {
            Row row = table.getSelectionModel().getSelectedItem();
            if (row == null) { info("Select a listing first."); return; }
            Optional<String> qtyOpt = textPrompt("Quantity", "Quantity to add:");
            if (qtyOpt.isEmpty() || qtyOpt.get().isBlank()) return;
            try {
                requireBuyer();
                CartItem item = new CartItem();
                item.setCartId(getOrCreateCartId(currentUser.getUserId()));
                item.setSupplierProductId((int) row.get(0));
                item.setQuantity(new BigDecimal(qtyOpt.get().trim()));
                cartItemService.addCartItem(item);
                info("Added to cart.");
            } catch (Exception ex) { showError(ex); }
        });

        HBox bottom = new HBox(addToCartBtn);
        bottom.setPadding(new Insets(14));
        bottom.setAlignment(Pos.CENTER_RIGHT);

        root.setCenter(table);
        root.setBottom(bottom);
        BorderPane.setMargin(table, new Insets(14));
        Scene scene = new Scene(root, 720, 420);
        applyTheme(scene);
        dialog.setScene(scene);
        dialog.show();
    }

    /**
     * Stand-in for a real Cart/CartService. Since no cart table/service was provided,
     * this uses the buyer's own userId as their cartId, so each buyer has exactly one
     * running cart. Swap this out if you have a real carts table.
     */
    private int getOrCreateCartId(int buyerUserId) {
        return buyerUserId;
    }

// =========================================================================
//  BUYER: CART + CHECKOUT
// =========================================================================

    private BorderPane buildCartPanel() {
        BorderPane panel = new BorderPane();
        VBox heading = sectionHeading("My Cart", "Review items before checkout.");
        TableView<Row> table = buildTable("Cart Item ID", "Supplier Product ID", "Quantity");

        Runnable refresh = () -> {
            ObservableList<Row> rows = FXCollections.observableArrayList();
            try {
                requireBuyer();
                for (CartItem c : cartItemService.getItemsByCartId(getOrCreateCartId(currentUser.getUserId()))) {
                    rows.add(new Row(c.getCartItemId(), c.getSupplierProductId(), c.getQuantity()));
                }
            } catch (Exception ex) { showError(ex); }
            table.setItems(rows);
        };
        refresh.run();

        Button refreshBtn = toolbarButton("↻ Refresh");
        refreshBtn.setOnAction(e -> refresh.run());

        Button updateQtyBtn = toolbarButton("✎ Update Quantity");
        updateQtyBtn.setOnAction(e -> {
            Row row = table.getSelectionModel().getSelectedItem();
            if (row == null) return;
            Optional<String> qtyOpt = textPrompt("Update Quantity", "New quantity:");
            if (qtyOpt.isEmpty() || qtyOpt.get().isBlank()) return;
            try {
                cartItemService.updateQuantity((int) row.get(0), new BigDecimal(qtyOpt.get().trim()));
                refresh.run();
            } catch (Exception ex) { showError(ex); }
        });

        Button removeBtn = toolbarButtonDanger("✕ Remove Item");
        removeBtn.setOnAction(e -> {
            Row row = table.getSelectionModel().getSelectedItem();
            if (row == null) return;
            try { cartItemService.deleteCartItem((int) row.get(0)); refresh.run(); }
            catch (Exception ex) { showError(ex); }
        });

        Button checkoutBtn = toolbarButtonPrimary("Checkout Cart →");
        checkoutBtn.setOnAction(e -> checkoutFlow(refresh));

        HBox top = toolbar(refreshBtn, updateQtyBtn, removeBtn, checkoutBtn);

        VBox topBox = new VBox(10, heading, top);
        panel.setTop(topBox);
        panel.setCenter(table);
        return panel;
    }

    /**
     * Simplified checkout: asks for a supplier ID and delivery address ID, then turns every
     * current cart item into an Order + OrderItems, and clears the cart.
     * (Orders in this schema belong to a single supplier, so in a full app you'd group cart
     * items by supplier automatically - here we ask directly to keep the flow short.)
     */
    private void checkoutFlow(Runnable refreshCart) {
        try {
            requireBuyer();
            List<CartItem> items = cartItemService.getItemsByCartId(getOrCreateCartId(currentUser.getUserId()));
            if (items.isEmpty()) { info("Your cart is empty."); return; }

            List<Address> addresses = addressService.getAddressesByUserId(currentUser.getUserId());
            if (addresses.isEmpty()) { info("Add a delivery address first (My Addresses tab)."); return; }

            Optional<String> supplierIdOpt = textPrompt("Checkout", "Supplier ID for this order:");
            if (supplierIdOpt.isEmpty() || supplierIdOpt.get().isBlank()) return;

            StringBuilder addrList = new StringBuilder();
            for (Address a : addresses) addrList.append(a.getAddressId()).append(": ").append(a.getAddressLine()).append("\n");
            Optional<String> addressIdOpt = textPrompt("Checkout", "Delivery Address ID:\n" + addrList);
            if (addressIdOpt.isEmpty() || addressIdOpt.get().isBlank()) return;

            BigDecimal productTotal = BigDecimal.ZERO;
            for (CartItem c : items) {
                SupplierProduct sp = supplierProductService.getSupplierProductById(c.getSupplierProductId());
                productTotal = productTotal.add(sp.getPricePerUnit().multiply(c.getQuantity()));
            }

            Order order = new Order();
            order.setBuyerId(currentUser.getUserId());
            order.setSupplierId(Integer.parseInt(supplierIdOpt.get().trim()));
            order.setDeliveryAddressId(Integer.parseInt(addressIdOpt.get().trim()));
            order.setProductTotal(productTotal);
            order.setDeliveryCharge(BigDecimal.ZERO);
            order.setDiscountAmount(BigDecimal.ZERO);
            order.setAdvancePercentage(BigDecimal.ZERO);
            orderService.addOrder(order);

            for (CartItem c : items) {
                SupplierProduct sp = supplierProductService.getSupplierProductById(c.getSupplierProductId());
                OrderItem oi = new OrderItem();
                oi.setOrderId(order.getOrderId());
                oi.setSupplierProductId(c.getSupplierProductId());
                oi.setQuantity(c.getQuantity());
                oi.setUnitPrice(sp.getPricePerUnit());
                orderItemService.addOrderItem(oi);
            }

            cartItemService.clearCart(getOrCreateCartId(currentUser.getUserId()));
            info("Order placed! Order ID: " + order.getOrderId());
            refreshCart.run();
        } catch (Exception ex) { showError(ex); }
    }

// =========================================================================
//  BUYER: MY ORDERS (+ pay + review + complaint)
// =========================================================================

    private BorderPane buildBuyerOrdersPanel() {
        BorderPane panel = new BorderPane();
        VBox heading = sectionHeading("My Orders", "Track, pay for, and review your placed orders.");
        TableView<Row> table = buildTable("Order ID", "Supplier ID", "Total", "Order Status", "Payment Status");

        Runnable refresh = () -> {
            ObservableList<Row> rows = FXCollections.observableArrayList();
            try {
                requireBuyer();
                for (Order o : orderService.getOrdersByBuyerId(currentUser.getUserId())) {
                    rows.add(new Row(o.getOrderId(), o.getSupplierId(), o.getTotalAmount(),
                            o.getOrderStatus(), o.getPaymentStatus()));
                }
            } catch (Exception ex) { showError(ex); }
            table.setItems(rows);
        };
        refresh.run();

        Button refreshBtn = toolbarButton("↻ Refresh");
        refreshBtn.setOnAction(e -> refresh.run());

        Button payBtn = toolbarButtonPrimary("💳 Make Payment");
        payBtn.setOnAction(e -> {
            Row row = table.getSelectionModel().getSelectedItem();
            if (row == null) return;
            Optional<String[]> valuesOpt = promptFields("Make Payment",
                    "Payment type (ADVANCE/REMAINING/REFUND)", "Payment method", "Amount", "Transaction reference (optional)");
            if (valuesOpt.isEmpty()) return;
            String[] values = valuesOpt.get();
            try {
                Payment payment = new Payment();
                payment.setOrderId((int) row.get(0));
                payment.setBuyerId(currentUser.getUserId());
                payment.setPaymentType(values[0]);
                payment.setPaymentMethod(values[1]);
                payment.setAmount(new BigDecimal(values[2].trim()));
                payment.setTransactionReference(values[3].isBlank() ? null : values[3]);
                paymentService.addPayment(payment);
                info("Payment submitted (status PENDING until verified).");
            } catch (Exception ex) { showError(ex); }
        });

        Button reviewBtn = toolbarButton("⭐ Leave Review");
        reviewBtn.setOnAction(e -> {
            Row row = table.getSelectionModel().getSelectedItem();
            if (row == null) return;
            Optional<String[]> valuesOpt = promptFields("Leave Review", "Rating (1-5)", "Comments (optional)");
            if (valuesOpt.isEmpty()) return;
            String[] values = valuesOpt.get();
            try {
                Review review = new Review();
                review.setOrderId((int) row.get(0));
                review.setBuyerId(currentUser.getUserId());
                review.setSupplierId((int) row.get(1));
                review.setRating(Integer.parseInt(values[0].trim()));
                review.setComments(values[1].isBlank() ? null : values[1]);
                reviewService.addReview(review);
                info("Review submitted.");
            } catch (Exception ex) { showError(ex); }
        });

        Button fileComplaintBtn = toolbarButtonDanger("⚠ File Complaint");
        fileComplaintBtn.setOnAction(e -> {
            Row row = table.getSelectionModel().getSelectedItem();
            if (row == null) return;
            fileComplaintDialog((int) row.get(0), (int) row.get(1));
        });

        HBox top = toolbar(refreshBtn, payBtn, reviewBtn, fileComplaintBtn);

        VBox topBox = new VBox(10, heading, top);
        panel.setTop(topBox);
        panel.setCenter(table);
        return panel;
    }

// =========================================================================
//  BUYER: ADDRESSES
// =========================================================================

    private BorderPane buildAddressesPanel() {
        BorderPane panel = new BorderPane();
        VBox heading = sectionHeading("Addresses", "Manage delivery and billing addresses.");
        TableView<Row> table = buildTable("Address ID", "Type", "Line", "City", "Area", "Default");

        Runnable refresh = () -> {
            ObservableList<Row> rows = FXCollections.observableArrayList();
            try {
                requireLoggedIn();
                for (Address a : addressService.getAddressesByUserId(currentUser.getUserId())) {
                    rows.add(new Row(a.getAddressId(), a.getAddressType(), a.getAddressLine(),
                            a.getCity(), a.getArea(), a.isDefault()));
                }
            } catch (Exception ex) { showError(ex); }
            table.setItems(rows);
        };
        refresh.run();

        Button refreshBtn = toolbarButton("↻ Refresh");
        refreshBtn.setOnAction(e -> refresh.run());

        Button addBtn = toolbarButtonPrimary("+ Add Address");
        addBtn.setOnAction(e -> {
            Optional<String[]> valuesOpt = promptFields("Add Address",
                    "Type (BUSINESS/DELIVERY/FARM/BILLING)", "Address line", "City", "Area", "Postal code (optional)");
            if (valuesOpt.isEmpty()) return;
            String[] values = valuesOpt.get();
            try {
                Address addr = new Address();
                addr.setUserId(currentUser.getUserId());
                addr.setAddressType(values[0]);
                addr.setAddressLine(values[1]);
                addr.setCity(values[2]);
                addr.setArea(values[3]);
                addr.setPostalCode(values[4].isBlank() ? null : values[4]);
                addressService.addAddress(addr);
                refresh.run();
            } catch (Exception ex) { showError(ex); }
        });

        Button makeDefaultBtn = toolbarButton("★ Set as Default");
        makeDefaultBtn.setOnAction(e -> {
            Row row = table.getSelectionModel().getSelectedItem();
            if (row == null) return;
            try { addressService.setDefaultAddress(currentUser.getUserId(), (int) row.get(0)); refresh.run(); }
            catch (Exception ex) { showError(ex); }
        });

        Button deleteBtn = toolbarButtonDanger("✕ Delete");
        deleteBtn.setOnAction(e -> {
            Row row = table.getSelectionModel().getSelectedItem();
            if (row == null) return;
            try { addressService.deleteAddress(currentUser.getUserId(), (int) row.get(0)); refresh.run(); }
            catch (Exception ex) { showError(ex); }
        });

        HBox top = toolbar(refreshBtn, addBtn, makeDefaultBtn, deleteBtn);

        VBox topBox = new VBox(10, heading, top);
        panel.setTop(topBox);
        panel.setCenter(table);
        return panel;
    }

// =========================================================================
//  MESSAGES (shared by buyer + supplier views)
// =========================================================================

    private BorderPane buildMessagesPanel() {
        BorderPane panel = new BorderPane();
        VBox heading = sectionHeading("Messages", "Conversations between you and your trading partners.");

        TableView<Row> convTable = buildTable("Conversation ID", "Buyer ID", "Supplier ID", "Order ID");
        TableView<Row> msgTable = buildTable("Message ID", "Sender", "Text", "Read");

        Runnable refreshConvs = () -> {
            ObservableList<Row> rows = FXCollections.observableArrayList();
            try {
                requireLoggedIn();
                List<Conversation> convs;
                if ("SUPPLIER".equalsIgnoreCase(currentUser.getRole()) && currentSupplierProfile != null) {
                    convs = conversationService.getConversationsBySupplierId(currentSupplierProfile.getSupplierId());
                } else if (currentBuyerProfile != null) {
                    convs = conversationService.getConversationsByBuyerId(currentBuyerProfile.getBuyerId());
                } else {
                    convs = List.of();
                }
                for (Conversation c : convs) {
                    rows.add(new Row(c.getConversationId(), c.getBuyerId(), c.getSupplierId(), c.getOrderId()));
                }
            } catch (Exception ex) { showError(ex); }
            convTable.setItems(rows);
        };
        refreshConvs.run();

        Runnable refreshMsgs = () -> {
            ObservableList<Row> rows = FXCollections.observableArrayList();
            Row selected = convTable.getSelectionModel().getSelectedItem();
            if (selected != null) {
                try {
                    for (Message m : messageService.getMessagesByConversationId((int) selected.get(0))) {
                        rows.add(new Row(m.getMessageId(), m.getSenderUserId(), m.getMessageText(), m.isRead()));
                    }
                } catch (Exception ex) { showError(ex); }
            }
            msgTable.setItems(rows);
        };
        convTable.getSelectionModel().selectedItemProperty().addListener((obs, oldV, newV) -> refreshMsgs.run());

        TextField newMsgField = styledTextField("Type a message...");
        Button sendBtn = toolbarButtonPrimary("Send ➤");
        sendBtn.setOnAction(e -> {
            Row selected = convTable.getSelectionModel().getSelectedItem();
            if (selected == null) { info("Select a conversation first."); return; }
            if (newMsgField.getText().isBlank()) return;
            try {
                Message m = new Message();
                m.setConversationId((int) selected.get(0));
                m.setSenderUserId(currentUser.getUserId());
                m.setMessageText(newMsgField.getText());
                messageService.addMessage(m);
                newMsgField.clear();
                refreshMsgs.run();
            } catch (Exception ex) { showError(ex); }
        });

        Button newConvBtn = toolbarButtonPrimary("+ Start / Open Conversation");
        newConvBtn.setOnAction(e -> {
            Optional<String[]> valuesOpt = promptFields("Start Conversation", "Buyer ID", "Supplier ID", "Order ID (optional)");
            if (valuesOpt.isEmpty()) return;
            String[] values = valuesOpt.get();
            try {
                Integer orderId = values[2].isBlank() ? null : Integer.parseInt(values[2].trim());
                conversationService.getOrCreateConversation(
                        Integer.parseInt(values[0].trim()), Integer.parseInt(values[1].trim()), orderId);
                refreshConvs.run();
            } catch (Exception ex) { showError(ex); }
        });

        HBox top = toolbar(newConvBtn);

        HBox bottom = new HBox(10, newMsgField, sendBtn);
        bottom.setPadding(new Insets(12, 0, 0, 0));
        bottom.setAlignment(Pos.CENTER_LEFT);
        HBox.setHgrow(newMsgField, Priority.ALWAYS);

        SplitPane split = new SplitPane(convTable, msgTable);
        split.getStyleClass().add("modern-split");
        split.setDividerPositions(0.45);

        VBox topBox = new VBox(10, heading, top);
        panel.setTop(topBox);
        panel.setCenter(split);
        panel.setBottom(bottom);
        return panel;
    }

// =========================================================================
//  BUYER: NOTIFICATIONS
// =========================================================================

    private BorderPane buildNotificationsPanel() {
        BorderPane panel = new BorderPane();
        VBox heading = sectionHeading("Notifications", "Stay up to date with account and order activity.");
        TableView<Row> table = buildTable("Notification ID", "Title", "Message", "Type", "Read");

        Runnable refresh = () -> {
            ObservableList<Row> rows = FXCollections.observableArrayList();
            try {
                requireLoggedIn();
                for (Notification n : notificationService.getNotificationsByUserId(currentUser.getUserId())) {
                    rows.add(new Row(n.getNotificationId(), n.getTitle(), n.getMessage(), n.getNotificationType(), n.isRead()));
                }
            } catch (Exception ex) { showError(ex); }
            table.setItems(rows);
        };
        refresh.run();

        Button refreshBtn = toolbarButton("↻ Refresh");
        refreshBtn.setOnAction(e -> refresh.run());

        Button markReadBtn = toolbarButton("✓ Mark Selected as Read");
        markReadBtn.setOnAction(e -> {
            Row row = table.getSelectionModel().getSelectedItem();
            if (row == null) return;
            try { notificationService.markAsRead((int) row.get(0)); refresh.run(); }
            catch (Exception ex) { showError(ex); }
        });

        Button markAllReadBtn = toolbarButtonPrimary("✓ Mark All as Read");
        markAllReadBtn.setOnAction(e -> {
            try { notificationService.markAllAsReadForUser(currentUser.getUserId()); refresh.run(); }
            catch (Exception ex) { showError(ex); }
        });

        HBox top = toolbar(refreshBtn, markReadBtn, markAllReadBtn);

        VBox topBox = new VBox(10, heading, top);
        panel.setTop(topBox);
        panel.setCenter(table);
        return panel;
    }

// =========================================================================
//  COMPLAINTS (buyer: file + view own, admin: resolve all)
// =========================================================================

    private BorderPane buildComplaintsPanel(boolean buyerView) {
        BorderPane panel = new BorderPane();
        VBox heading = sectionHeading("Complaints", buyerView
                ? "Complaints you've filed and their current status."
                : "Open complaints awaiting admin resolution.");
        TableView<Row> table = buildTable("Complaint ID", "Order ID", "Submitted By", "Against", "Type", "Status", "Description");

        Runnable refresh = () -> {
            ObservableList<Row> rows = FXCollections.observableArrayList();
            try {
                requireLoggedIn();
                List<Complaint> complaints = buyerView
                        ? complaintService.getComplaintsSubmittedBy(currentUser.getUserId())
                        : complaintService.getComplaintsByStatus("OPEN");
                for (Complaint c : complaints) {
                    rows.add(new Row(c.getComplaintId(), c.getOrderId(), c.getSubmittedBy(),
                            c.getAgainstUserId(), c.getComplaintType(), c.getComplaintStatus(), c.getDescription()));
                }
            } catch (Exception ex) { showError(ex); }
            table.setItems(rows);
        };
        refresh.run();

        Button refreshBtn = toolbarButton("↻ Refresh");
        refreshBtn.setOnAction(e -> refresh.run());

        HBox top = toolbar(refreshBtn);

        if (!buyerView) {
            Button resolveBtn = toolbarButtonPrimary("Resolve Selected");
            resolveBtn.setOnAction(e -> {
                Row row = table.getSelectionModel().getSelectedItem();
                if (row == null) return;
                Optional<String[]> valuesOpt = promptFields("Resolve Complaint", "New status (RESOLVED/REJECTED)", "Admin response");
                if (valuesOpt.isEmpty()) return;
                String[] values = valuesOpt.get();
                try {
                    complaintService.resolveComplaint((int) row.get(0), values[0], values[1]);
                    refresh.run();
                } catch (Exception ex) { showError(ex); }
            });
            top.getChildren().add(resolveBtn);
        }

        VBox topBox = new VBox(10, heading, top);
        panel.setTop(topBox);
        panel.setCenter(table);
        return panel;
    }

    private void fileComplaintDialog(int orderId, int againstUserId) {
        Optional<String[]> valuesOpt = promptFields("File Complaint", "Complaint type", "Description");
        if (valuesOpt.isEmpty()) return;
        String[] values = valuesOpt.get();
        try {
            Complaint complaint = new Complaint();
            complaint.setOrderId(orderId);
            complaint.setSubmittedBy(currentUser.getUserId());
            complaint.setAgainstUserId(againstUserId);
            complaint.setComplaintType(values[0]);
            complaint.setDescription(values[1]);
            complaintService.addComplaint(complaint);
            info("Complaint filed.");
        } catch (Exception ex) { showError(ex); }
    }

// =========================================================================
//  SUPPLIER: PROFILE
// =========================================================================

    private BorderPane buildSupplierProfilePanel() {
        BorderPane panel = new BorderPane();
        VBox heading = sectionHeading("My Profile", "Your supplier verification and performance summary.");
        TextArea infoArea = new TextArea();
        infoArea.setEditable(false);
        infoArea.getStyleClass().add("info-panel");

        Runnable refresh = () -> {
            try {
                currentSupplierProfile = supplierProfileService.getSupplierByUserId(currentUser.getUserId());
                infoArea.setText("Supplier ID: " + currentSupplierProfile.getSupplierId()
                        + "\nType: " + currentSupplierProfile.getSupplierType()
                        + "\nFarm/Business name: " + currentSupplierProfile.getFarmOrBusinessName()
                        + "\nCNIC: " + currentSupplierProfile.getCnicNumber()
                        + "\nVerification status: " + currentSupplierProfile.getVerificationStatus()
                        + "\nAverage rating: " + currentSupplierProfile.getAverageRating()
                        + "\nCompleted orders: " + currentSupplierProfile.getTotalCompletedOrders());
            } catch (Exception ex) {
                infoArea.setText("No supplier profile found yet. Create one below.");
                currentSupplierProfile = null;
            }
        };
        refresh.run();

        Button createBtn = toolbarButtonPrimary("+ Create Supplier Profile");
        createBtn.setOnAction(e -> {
            Optional<String[]> valuesOpt = promptFields("Create Supplier Profile",
                    "Type (FARMER/DISTRIBUTOR/etc.)", "Farm/Business name", "CNIC number", "Registration number (optional)");
            if (valuesOpt.isEmpty()) return;
            String[] values = valuesOpt.get();
            try {
                SupplierProfile profile = new SupplierProfile();
                profile.setUserId(currentUser.getUserId());
                profile.setSupplierType(values[0]);
                profile.setFarmOrBusinessName(values[1]);
                profile.setCnicNumber(values[2]);
                profile.setRegistrationNumber(values[3].isBlank() ? null : values[3]);
                supplierProfileService.addSupplierProfile(profile);
                refresh.run();
            } catch (Exception ex) { showError(ex); }
        });

        Button refreshBtn = toolbarButton("↻ Refresh");
        refreshBtn.setOnAction(e -> refresh.run());

        HBox top = toolbar(refreshBtn, createBtn);

        VBox topBox = new VBox(10, heading, top);
        panel.setTop(topBox);
        panel.setCenter(infoArea);
        return panel;
    }

// =========================================================================
//  SUPPLIER: LISTINGS
// =========================================================================

    private BorderPane buildSupplierListingsPanel() {
        BorderPane panel = new BorderPane();
        VBox heading = sectionHeading("My Listings", "Manage the products you're currently offering.");
        TableView<Row> table = buildTable("Listing ID", "Product ID", "Price/Unit", "Available Qty", "Min Order", "Unit", "Status");

        Runnable refresh = () -> {
            ObservableList<Row> rows = FXCollections.observableArrayList();
            try {
                requireSupplier();
                for (SupplierProduct sp : supplierProductService.getListingsBySupplierId(currentSupplierProfile.getSupplierId())) {
                    rows.add(new Row(sp.getSupplierProductId(), sp.getProductId(), sp.getPricePerUnit(),
                            sp.getAvailableQuantity(), sp.getMinimumOrderQuantity(), sp.getUnitType(), sp.getListingStatus()));
                }
            } catch (Exception ex) { showError(ex); }
            table.setItems(rows);
        };
        refresh.run();

        Button refreshBtn = toolbarButton("↻ Refresh");
        refreshBtn.setOnAction(e -> refresh.run());

        Button addBtn = toolbarButtonPrimary("+ Add Listing");
        addBtn.setOnAction(e -> {
            Optional<String[]> valuesOpt = promptFields("Add Listing",
                    "Product ID", "Price per unit", "Available quantity", "Minimum order quantity", "Unit type", "Quality grade (optional)");
            if (valuesOpt.isEmpty()) return;
            String[] values = valuesOpt.get();
            try {
                requireSupplier();
                SupplierProduct sp = new SupplierProduct();
                sp.setSupplierId(currentSupplierProfile.getSupplierId());
                sp.setProductId(Integer.parseInt(values[0].trim()));
                sp.setPricePerUnit(new BigDecimal(values[1].trim()));
                sp.setAvailableQuantity(new BigDecimal(values[2].trim()));
                sp.setMinimumOrderQuantity(new BigDecimal(values[3].trim()));
                sp.setUnitType(values[4]);
                sp.setQualityGrade(values[5].isBlank() ? null : values[5]);
                supplierProductService.addSupplierProduct(sp);
                refresh.run();
            } catch (Exception ex) { showError(ex); }
        });

        Button updateStatusBtn = toolbarButton("✎ Update Status");
        updateStatusBtn.setOnAction(e -> {
            Row row = table.getSelectionModel().getSelectedItem();
            if (row == null) return;
            Optional<String> statusOpt = textPrompt("Update Status", "New status (PENDING/APPROVED/REJECTED/UNAVAILABLE):");
            if (statusOpt.isEmpty() || statusOpt.get().isBlank()) return;
            try { supplierProductService.updateListingStatus((int) row.get(0), statusOpt.get()); refresh.run(); }
            catch (Exception ex) { showError(ex); }
        });

        HBox top = toolbar(refreshBtn, addBtn, updateStatusBtn);

        VBox topBox = new VBox(10, heading, top);
        panel.setTop(topBox);
        panel.setCenter(table);
        return panel;
    }

// =========================================================================
//  SUPPLIER: INCOMING ORDERS
// =========================================================================

    private BorderPane buildSupplierOrdersPanel() {
        BorderPane panel = new BorderPane();
        VBox heading = sectionHeading("Incoming Orders", "Orders placed by buyers against your listings.");
        TableView<Row> table = buildTable("Order ID", "Buyer ID", "Total", "Order Status", "Payment Status");

        Runnable refresh = () -> {
            ObservableList<Row> rows = FXCollections.observableArrayList();
            try {
                requireSupplier();
                for (Order o : orderService.getOrdersBySupplierId(currentSupplierProfile.getSupplierId())) {
                    rows.add(new Row(o.getOrderId(), o.getBuyerId(), o.getTotalAmount(),
                            o.getOrderStatus(), o.getPaymentStatus()));
                }
            } catch (Exception ex) { showError(ex); }
            table.setItems(rows);
        };
        refresh.run();

        Button refreshBtn = toolbarButton("↻ Refresh");
        refreshBtn.setOnAction(e -> refresh.run());

        Button updateStatusBtn = toolbarButtonPrimary("✎ Update Order Status");
        updateStatusBtn.setOnAction(e -> {
            Row row = table.getSelectionModel().getSelectedItem();
            if (row == null) return;
            Optional<String> statusOpt = textPrompt("Update Status",
                    "New status (PENDING/ACCEPTED/PROCESSING/DISPATCHED/DELIVERED/CANCELLED/REJECTED):");
            if (statusOpt.isEmpty() || statusOpt.get().isBlank()) return;
            try { orderService.updateOrderStatus((int) row.get(0), statusOpt.get()); refresh.run(); }
            catch (Exception ex) { showError(ex); }
        });

        Button viewItemsBtn = toolbarButton("👁 View Order Items");
        viewItemsBtn.setOnAction(e -> {
            Row row = table.getSelectionModel().getSelectedItem();
            if (row == null) return;
            showOrderItemsDialog((int) row.get(0));
        });

        HBox top = toolbar(refreshBtn, updateStatusBtn, viewItemsBtn);

        VBox topBox = new VBox(10, heading, top);
        panel.setTop(topBox);
        panel.setCenter(table);
        return panel;
    }

    private void showOrderItemsDialog(int orderId) {
        Stage dialog = new Stage();
        dialog.setTitle("Order Items");
        TableView<Row> table = buildTable("Item ID", "Supplier Product ID", "Quantity", "Unit Price", "Subtotal");
        ObservableList<Row> rows = FXCollections.observableArrayList();
        try {
            for (OrderItem oi : orderItemService.getItemsByOrderId(orderId)) {
                rows.add(new Row(oi.getOrderItemId(), oi.getSupplierProductId(), oi.getQuantity(), oi.getUnitPrice(), oi.getSubtotal()));
            }
        } catch (Exception ex) { showError(ex); }
        table.setItems(rows);
        BorderPane root = new BorderPane(table);
        root.getStyleClass().add("dialog-background");
        BorderPane.setMargin(table, new Insets(14));
        Scene scene = new Scene(root, 640, 380);
        applyTheme(scene);
        dialog.setScene(scene);
        dialog.show();
    }

// =========================================================================
//  DELIVERIES
// =========================================================================

    private BorderPane buildDeliveriesPanel() {
        BorderPane panel = new BorderPane();
        VBox heading = sectionHeading("Deliveries", "Track and update logistics for an order.");

        TextField orderIdField = styledTextField("Order ID");
        orderIdField.setPrefWidth(120);
        Button loadBtn = toolbarButtonPrimary("Load Delivery");

        TextArea infoArea = new TextArea();
        infoArea.setEditable(false);
        infoArea.getStyleClass().add("info-panel");

        loadBtn.setOnAction(e -> {
            try {
                int orderId = Integer.parseInt(orderIdField.getText().trim());
                Delivery d;
                try {
                    d = deliveryService.getDeliveryByOrderId(orderId);
                } catch (Exception notFound) {
                    Optional<String> methodOpt = textPrompt("New Delivery", "No delivery yet. Delivery method?");
                    if (methodOpt.isEmpty() || methodOpt.get().isBlank()) return;
                    Delivery nd = new Delivery();
                    nd.setOrderId(orderId);
                    nd.setDeliveryMethod(methodOpt.get());
                    deliveryService.addDelivery(nd);
                    d = deliveryService.getDeliveryByOrderId(orderId);
                }
                infoArea.setText("Delivery ID: " + d.getDeliveryId()
                        + "\nMethod: " + d.getDeliveryMethod()
                        + "\nStatus: " + d.getDeliveryStatus()
                        + "\nDriver: " + d.getDriverName()
                        + "\nDriver phone: " + d.getDriverPhone()
                        + "\nVehicle: " + d.getVehicleNumber());
            } catch (Exception ex) { showError(ex); }
        });

        Button dispatchBtn = toolbarButton("🚚 Mark Dispatched");
        dispatchBtn.setOnAction(e -> {
            try {
                Delivery d = deliveryService.getDeliveryByOrderId(Integer.parseInt(orderIdField.getText().trim()));
                deliveryService.markAsDispatched(d.getDeliveryId());
                loadBtn.fire();
            } catch (Exception ex) { showError(ex); }
        });

        Button deliveredBtn = toolbarButton("✓ Mark Delivered");
        deliveredBtn.setOnAction(e -> {
            try {
                Delivery d = deliveryService.getDeliveryByOrderId(Integer.parseInt(orderIdField.getText().trim()));
                Optional<String[]> valuesOpt = promptFields("Mark Delivered", "Delivery proof (optional)", "Received by (optional)");
                if (valuesOpt.isEmpty()) return;
                String[] values = valuesOpt.get();
                deliveryService.markAsDelivered(d.getDeliveryId(), values[0], values[1]);
                loadBtn.fire();
            } catch (Exception ex) { showError(ex); }
        });

        Button logisticsBtn = toolbarButton("✎ Update Logistics Info");
        logisticsBtn.setOnAction(e -> {
            try {
                Delivery d = deliveryService.getDeliveryByOrderId(Integer.parseInt(orderIdField.getText().trim()));
                Optional<String[]> valuesOpt = promptFields("Update Logistics", "Driver name", "Driver phone", "Vehicle number");
                if (valuesOpt.isEmpty()) return;
                String[] values = valuesOpt.get();
                deliveryService.updateLogisticsInfo(d.getDeliveryId(), values[0], values[1], values[2]);
                loadBtn.fire();
            } catch (Exception ex) { showError(ex); }
        });

        HBox orderRow = new HBox(10, new Label("Order ID:"), orderIdField, loadBtn);
        orderRow.setAlignment(Pos.CENTER_LEFT);
        HBox actionsRow = toolbar(dispatchBtn, deliveredBtn, logisticsBtn);

        VBox topBox = new VBox(10, heading, orderRow, actionsRow);
        panel.setTop(topBox);
        panel.setCenter(infoArea);
        return panel;
    }

// =========================================================================
//  SUPPLIER: REVIEWS
// =========================================================================

    private BorderPane buildSupplierReviewsPanel() {
        BorderPane panel = new BorderPane();
        VBox heading = sectionHeading("Reviews", "Feedback buyers have left on your completed orders.");
        TableView<Row> table = buildTable("Review ID", "Order ID", "Buyer ID", "Rating", "Comments");
        Label avgLabel = new Label("Average rating: -");
        avgLabel.getStyleClass().add("stat-pill");

        Runnable refresh = () -> {
            ObservableList<Row> rows = FXCollections.observableArrayList();
            try {
                requireSupplier();
                for (Review r : reviewService.getReviewsBySupplierId(currentSupplierProfile.getSupplierId())) {
                    rows.add(new Row(r.getReviewId(), r.getOrderId(), r.getBuyerId(), r.getRating(), r.getComments()));
                }
                double avg = reviewService.getAverageRatingForSupplier(currentSupplierProfile.getSupplierId());
                avgLabel.setText("★ Average rating: " + avg);
            } catch (Exception ex) { showError(ex); }
            table.setItems(rows);
        };
        refresh.run();

        Button refreshBtn = toolbarButton("↻ Refresh");
        refreshBtn.setOnAction(e -> refresh.run());

        HBox top = toolbar(refreshBtn, avgLabel);

        VBox topBox = new VBox(10, heading, top);
        panel.setTop(topBox);
        panel.setCenter(table);
        return panel;
    }

// =========================================================================
//  ADMIN: VERIFY SUPPLIERS / BUYERS
// =========================================================================

    private BorderPane buildVerifySuppliersPanel() {
        BorderPane panel = new BorderPane();
        VBox heading = sectionHeading("Supplier Approvals", "Verify pending supplier accounts.");
        TableView<Row> table = buildTable("Supplier ID", "User ID", "Farm/Business", "CNIC", "Status");

        Runnable refresh = () -> {
            ObservableList<Row> rows = FXCollections.observableArrayList();
            try {
                for (SupplierProfile sp : supplierProfileService.getPendingVerifications()) {
                    rows.add(new Row(sp.getSupplierId(), sp.getUserId(), sp.getFarmOrBusinessName(),
                            sp.getCnicNumber(), sp.getVerificationStatus()));
                }
            } catch (Exception ex) { showError(ex); }
            table.setItems(rows);
        };
        refresh.run();

        Button refreshBtn = toolbarButton("↻ Refresh");
        refreshBtn.setOnAction(e -> refresh.run());

        Button approveBtn = toolbarButtonPrimary("✓ Approve");
        approveBtn.setOnAction(e -> updateSupplierVerification(table, "VERIFIED", refresh));
        Button rejectBtn = toolbarButtonDanger("✕ Reject");
        rejectBtn.setOnAction(e -> updateSupplierVerification(table, "REJECTED", refresh));

        HBox top = toolbar(refreshBtn, approveBtn, rejectBtn);

        VBox topBox = new VBox(10, heading, top);
        panel.setTop(topBox);
        panel.setCenter(table);
        return panel;
    }

    private void updateSupplierVerification(TableView<Row> table, String status, Runnable refresh) {
        Row row = table.getSelectionModel().getSelectedItem();
        if (row == null) return;
        int supplierId = (int) row.get(0);
        try {
            supplierProfileService.updateVerificationStatus(supplierId, status);
            logAdminAction("VERIFY_SUPPLIER_" + status, "SUPPLIER_PROFILE", supplierId, null);
            refresh.run();
        } catch (Exception ex) { showError(ex); }
    }

    private BorderPane buildVerifyBuyersPanel() {
        BorderPane panel = new BorderPane();
        VBox heading = sectionHeading("Buyer Approvals", "Verify pending buyer accounts.");
        TableView<Row> table = buildTable("Buyer ID", "User ID", "Business Name", "Type", "Status");

        Runnable refresh = () -> {
            ObservableList<Row> rows = FXCollections.observableArrayList();
            try {
                for (BuyerProfile bp : buyerProfileService.getPendingVerifications()) {
                    rows.add(new Row(bp.getBuyerId(), bp.getUserId(), bp.getBusinessName(),
                            bp.getBusinessType(), bp.getVerificationStatus()));
                }
            } catch (Exception ex) { showError(ex); }
            table.setItems(rows);
        };
        refresh.run();

        Button refreshBtn = toolbarButton("↻ Refresh");
        refreshBtn.setOnAction(e -> refresh.run());

        Button approveBtn = toolbarButtonPrimary("✓ Approve");
        approveBtn.setOnAction(e -> updateBuyerVerification(table, "VERIFIED", refresh));
        Button rejectBtn = toolbarButtonDanger("✕ Reject");
        rejectBtn.setOnAction(e -> updateBuyerVerification(table, "REJECTED", refresh));

        HBox top = toolbar(refreshBtn, approveBtn, rejectBtn);

        VBox topBox = new VBox(10, heading, top);
        panel.setTop(topBox);
        panel.setCenter(table);
        return panel;
    }

    private void updateBuyerVerification(TableView<Row> table, String status, Runnable refresh) {
        Row row = table.getSelectionModel().getSelectedItem();
        if (row == null) return;
        int buyerId = (int) row.get(0);
        try {
            buyerProfileService.updateVerificationStatus(buyerId, status);
            logAdminAction("VERIFY_BUYER_" + status, "BUYER_PROFILE", buyerId, null);
            refresh.run();
        } catch (Exception ex) { showError(ex); }
    }

// =========================================================================
//  ADMIN: CATEGORIES
// =========================================================================

    private BorderPane buildCategoriesPanel() {
        BorderPane panel = new BorderPane();
        VBox heading = sectionHeading("Categories", "Manage marketplace product categories.");
        TableView<Row> table = buildTable("Category ID", "Name", "Active");

        Runnable refresh = () -> {
            ObservableList<Row> rows = FXCollections.observableArrayList();
            try {
                for (Category c : categoryService.getAllCategories()) {
                    rows.add(new Row(c.getCategoryId(), c.getCategoryName(), c.isActive()));
                }
            } catch (Exception ex) { showError(ex); }
            table.setItems(rows);
        };
        refresh.run();

        Button refreshBtn = toolbarButton("↻ Refresh");
        refreshBtn.setOnAction(e -> refresh.run());

        Button addBtn = toolbarButtonPrimary("+ Add Category");
        addBtn.setOnAction(e -> {
            Optional<String[]> valuesOpt = promptFields("Add Category", "Name", "Description (optional)");
            if (valuesOpt.isEmpty()) return;
            String[] values = valuesOpt.get();
            try {
                Category cat = new Category();
                cat.setCategoryName(values[0]);
                cat.setDescription(values[1].isBlank() ? null : values[1]);
                categoryService.addCategory(cat);
                logAdminAction("ADD_CATEGORY", "CATEGORY", null, values[0]);
                refresh.run();
            } catch (Exception ex) { showError(ex); }
        });

        Button toggleBtn = toolbarButton("⇄ Toggle Active");
        toggleBtn.setOnAction(e -> {
            Row row = table.getSelectionModel().getSelectedItem();
            if (row == null) return;
            boolean active = (boolean) row.get(2);
            try { categoryService.setCategoryActiveStatus((int) row.get(0), !active); refresh.run(); }
            catch (Exception ex) { showError(ex); }
        });

        HBox top = toolbar(refreshBtn, addBtn, toggleBtn);

        VBox topBox = new VBox(10, heading, top);
        panel.setTop(topBox);
        panel.setCenter(table);
        return panel;
    }

// =========================================================================
//  ADMIN: REFUNDS
// =========================================================================

    private BorderPane buildRefundsPanel() {
        BorderPane panel = new BorderPane();
        VBox heading = sectionHeading("Refunds", "Requested refunds awaiting a decision.");
        TableView<Row> table = buildTable("Refund ID", "Payment ID", "Order ID", "Amount", "Reason", "Status");

        Runnable refresh = () -> {
            ObservableList<Row> rows = FXCollections.observableArrayList();
            try {
                for (Refund r : refundService.getPendingRefunds()) {
                    rows.add(new Row(r.getRefundId(), r.getPaymentId(), r.getOrderId(),
                            r.getRefundAmount(), r.getRefundReason(), r.getRefundStatus()));
                }
            } catch (Exception ex) { showError(ex); }
            table.setItems(rows);
        };
        refresh.run();

        Button refreshBtn = toolbarButton("↻ Refresh");
        refreshBtn.setOnAction(e -> refresh.run());

        Button updateBtn = toolbarButtonPrimary("✎ Update Status");
        updateBtn.setOnAction(e -> {
            Row row = table.getSelectionModel().getSelectedItem();
            if (row == null) return;
            Optional<String> statusOpt = textPrompt("Update Refund", "New status (REQUESTED/APPROVED/REJECTED/COMPLETED):");
            if (statusOpt.isEmpty() || statusOpt.get().isBlank()) return;
            try {
                refundService.updateRefundStatus((int) row.get(0), statusOpt.get());
                logAdminAction("UPDATE_REFUND_" + statusOpt.get(), "REFUND", (int) row.get(0), null);
                refresh.run();
            } catch (Exception ex) { showError(ex); }
        });

        HBox top = toolbar(refreshBtn, updateBtn);

        VBox topBox = new VBox(10, heading, top);
        panel.setTop(topBox);
        panel.setCenter(table);
        return panel;
    }

// =========================================================================
//  ADMIN: MARKET PRICES
// =========================================================================

    private BorderPane buildMarketPricesPanel() {
        BorderPane panel = new BorderPane();
        VBox heading = sectionHeading("Market Prices", "Look up and record daily market price data.");
        TableView<Row> table = buildTable("ID", "Product ID", "City/Market", "Min", "Max", "Avg", "Unit");
        TextField productIdField = styledTextField("Product ID");
        productIdField.setPrefWidth(120);

        Button loadBtn = toolbarButtonPrimary("Load History");
        loadBtn.setOnAction(e -> {
            ObservableList<Row> rows = FXCollections.observableArrayList();
            try {
                int productId = Integer.parseInt(productIdField.getText().trim());
                for (MarketPrice mp : marketPriceService.getPriceHistoryByProductId(productId)) {
                    rows.add(new Row(mp.getMarketPriceId(), mp.getProductId(), mp.getCityOrMarket(),
                            mp.getMinimumPrice(), mp.getMaximumPrice(), mp.getAveragePrice(), mp.getUnitType()));
                }
            } catch (Exception ex) { showError(ex); }
            table.setItems(rows);
        });

        Button addBtn = toolbarButton("+ Add Market Price");
        addBtn.setOnAction(e -> {
            Optional<String[]> valuesOpt = promptFields("Add Market Price",
                    "Product ID", "City/Market", "Minimum price", "Maximum price", "Average price", "Unit type",
                    "Price date (YYYY-MM-DD)");
            if (valuesOpt.isEmpty()) return;
            String[] values = valuesOpt.get();
            try {
                MarketPrice mp = new MarketPrice();
                mp.setProductId(Integer.parseInt(values[0].trim()));
                mp.setCityOrMarket(values[1]);
                mp.setMinimumPrice(new BigDecimal(values[2].trim()));
                mp.setMaximumPrice(new BigDecimal(values[3].trim()));
                mp.setAveragePrice(new BigDecimal(values[4].trim()));
                mp.setUnitType(values[5]);
                mp.setPriceDate(java.sql.Date.valueOf(values[6].trim()));
                mp.setEnteredBy(currentUser.getUserId());
                marketPriceService.addMarketPrice(mp);
                info("Market price added.");
            } catch (Exception ex) { showError(ex); }
        });

        HBox row = new HBox(10, new Label("Product ID:"), productIdField, loadBtn, addBtn);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("toolbar-row");

        VBox topBox = new VBox(10, heading, row);
        panel.setTop(topBox);
        panel.setCenter(table);
        return panel;
    }

// =========================================================================
//  ADMIN: LOGS
// =========================================================================

    private BorderPane buildAdminLogsPanel() {
        BorderPane panel = new BorderPane();
        VBox heading = sectionHeading("Admin Logs", "Audit trail of administrative actions.");
        TableView<Row> table = buildTable("Log ID", "Admin User ID", "Action", "Entity Type", "Entity ID", "Details");

        Runnable refresh = () -> {
            ObservableList<Row> rows = FXCollections.observableArrayList();
            try {
                for (AdminLog log : adminLogService.getAllLogs()) {
                    rows.add(new Row(log.getLogId(), log.getAdminUserId(), log.getAction(),
                            log.getEntityType(), log.getEntityId(), log.getDetails()));
                }
            } catch (Exception ex) { showError(ex); }
            table.setItems(rows);
        };
        refresh.run();

        Button refreshBtn = toolbarButton("↻ Refresh");
        refreshBtn.setOnAction(e -> refresh.run());

        HBox top = toolbar(refreshBtn);

        VBox topBox = new VBox(10, heading, top);
        panel.setTop(topBox);
        panel.setCenter(table);
        return panel;
    }

    /** Best-effort audit trail: silently no-ops if logging fails, so it never blocks the main action. */
    private void logAdminAction(String action, String entityType, Integer entityId, String details) {
        try {
            AdminLog log = new AdminLog();
            log.setAdminUserId(currentUser.getUserId());
            log.setAction(action);
            log.setEntityType(entityType);
            log.setEntityId(entityId);
            log.setDetails(details);
            adminLogService.addLog(log);
        } catch (Exception ignore) { /* logging failures should not block admin actions */ }
    }

// =========================================================================
//  SHARED HELPERS
// =========================================================================

    /** Lightweight generic row wrapper so one TableView builder works for every table in the app. */
    private static class Row {
        private final Object[] values;
        Row(Object... values) { this.values = values; }
        Object get(int i) { return values[i]; }
    }

    private TableView<Row> buildTable(String... columnNames) {
        TableView<Row> table = new TableView<>();
        table.getStyleClass().add("modern-table");
        table.setColumnResizePolicy(TableView.CONSTRAINED_RESIZE_POLICY_FLEX_LAST_COLUMN);
        Label placeholder = new Label("No records found");
        placeholder.getStyleClass().add("muted-text");
        table.setPlaceholder(placeholder);
        for (int i = 0; i < columnNames.length; i++) {
            final int idx = i;
            TableColumn<Row, Object> col = new TableColumn<>(columnNames[i]);
            col.setCellValueFactory(data -> new SimpleObjectProperty<>(data.getValue().get(idx)));
            table.getColumns().add(col);
        }
        return table;
    }

    /** Section title + short description shown above a workspace panel's toolbar. */
    private VBox sectionHeading(String title, String description) {
        Label heading = new Label(title);
        heading.getStyleClass().add("section-title");
        Label desc = new Label(description);
        desc.getStyleClass().add("section-subtitle");
        return new VBox(2, heading, desc);
    }

    /** Consistent toolbar row wrapper for action buttons above a table. */
    private HBox toolbar(Node... nodes) {
        HBox box = new HBox(8, nodes);
        box.getStyleClass().add("toolbar-row");
        box.setAlignment(Pos.CENTER_LEFT);
        return box;
    }

    private Button toolbarButton(String text) {
        Button b = new Button(text);
        b.getStyleClass().add("toolbar-button");
        b.setCursor(Cursor.HAND);
        return b;
    }

    private Button toolbarButtonPrimary(String text) {
        Button b = new Button(text);
        b.getStyleClass().addAll("toolbar-button", "toolbar-button-primary");
        b.setCursor(Cursor.HAND);
        return b;
    }

    private Button toolbarButtonDanger(String text) {
        Button b = new Button(text);
        b.getStyleClass().addAll("toolbar-button", "toolbar-button-danger");
        b.setCursor(Cursor.HAND);
        return b;
    }

    private Label featureChip(String text) {
        Label chip = new Label(text);
        chip.getStyleClass().add("feature-chip");
        return chip;
    }

    private TextField styledTextField(String prompt) {
        TextField field = new TextField();
        field.setPromptText(prompt);
        field.getStyleClass().add("modern-field");
        return field;
    }

    private Button primaryButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("primary-button");
        button.setCursor(Cursor.HAND);
        return button;
    }

    private Button secondaryButton(String text) {
        Button button = new Button(text);
        button.getStyleClass().add("secondary-button");
        button.setCursor(Cursor.HAND);
        return button;
    }

    private void applyTheme(Scene scene) {
        try {
            String css = """
            .root {
                -fx-font-family: "Segoe UI", "Inter", "Helvetica Neue", Arial, sans-serif;
                -fx-font-size: 13px;
                -fx-accent: #2f7d4a;
                -fx-focus-color: rgba(47,125,74,0.35);
                -fx-faint-focus-color: transparent;
            }

            /* ---------- Backgrounds ---------- */
            .app-background {
                -fx-background-color: linear-gradient(to bottom right, #eef8f1 0%, #fbf6ea 48%, #eaf3ee 100%);
            }
            .dashboard-background { -fx-background-color: #f2f6f3; }
            .dialog-background { -fx-background-color: #f6f9f6; }

            /* ---------- Cards ---------- */
            .glass-card, .content-card {
                -fx-background-color: rgba(255,255,255,0.97);
                -fx-background-radius: 20;
                -fx-border-color: rgba(255,255,255,0.9);
                -fx-border-width: 1;
                -fx-border-radius: 20;
                -fx-effect: dropshadow(gaussian, rgba(24,60,39,0.16), 34, 0.18, 0, 12);
            }
            .content-card { -fx-padding: 22; }

            /* ---------- Badges / chips / pills ---------- */
            .brand-badge {
                -fx-background-color: linear-gradient(to right, #2f7d4a, #57a86f);
                -fx-text-fill: white;
                -fx-background-radius: 999;
                -fx-padding: 7 16;
                -fx-font-size: 11px;
                -fx-font-weight: 800;
                -fx-letter-spacing: 1.5px;
            }
            .feature-chip {
                -fx-background-color: rgba(47,125,74,0.10);
                -fx-text-fill: #24603b;
                -fx-background-radius: 999;
                -fx-border-color: rgba(47,125,74,0.22);
                -fx-border-radius: 999;
                -fx-border-width: 1;
                -fx-padding: 6 13;
                -fx-font-size: 12px;
                -fx-font-weight: 700;
            }
            .account-pill {
                -fx-background-color: linear-gradient(to right, #2f7d4a, #45996a);
                -fx-text-fill: white;
                -fx-background-radius: 999;
                -fx-padding: 7 16;
                -fx-font-weight: 800;
                -fx-font-size: 11px;
                -fx-letter-spacing: 0.6px;
            }
            .stat-pill {
                -fx-background-color: #fff6e0;
                -fx-text-fill: #8a6d1c;
                -fx-background-radius: 999;
                -fx-border-color: #f0e0a8;
                -fx-border-radius: 999;
                -fx-border-width: 1;
                -fx-padding: 7 14;
                -fx-font-weight: 700;
            }

            /* ---------- Typography ---------- */
            .brand-title {
                -fx-font-size: 42px;
                -fx-font-weight: 800;
                -fx-text-fill: #16351f;
                -fx-line-spacing: -3;
            }
            .brand-copy { -fx-font-size: 15.5px; -fx-text-fill: #5a7263; -fx-line-spacing: 4; }
            .form-title { -fx-font-size: 25px; -fx-font-weight: 800; -fx-text-fill: #16351f; }
            .muted-text { -fx-font-size: 13px; -fx-text-fill: #6f8378; }
            .hint-text { -fx-font-size: 11px; -fx-text-fill: #8a9a90; }
            .section-title { -fx-font-size: 19px; -fx-font-weight: 800; -fx-text-fill: #1c3f28; }
            .section-subtitle { -fx-font-size: 12.5px; -fx-text-fill: #6f8378; }

            /* ---------- Inputs ---------- */
            .modern-field, .text-field, .password-field, .combo-box, .text-area {
                -fx-background-color: #f7faf7;
                -fx-border-color: #dbe6de;
                -fx-border-radius: 12;
                -fx-background-radius: 12;
                -fx-padding: 11 13;
                -fx-prompt-text-fill: #97a69c;
            }
            .modern-field:focused, .text-field:focused, .password-field:focused, .combo-box:focused, .text-area:focused {
                -fx-border-color: #4f9a68;
                -fx-background-color: white;
                -fx-effect: dropshadow(gaussian, rgba(79,154,104,0.25), 8, 0.3, 0, 0);
            }
            .combo-box .list-cell { -fx-padding: 4 8; }
            .text-area .content { -fx-background-color: transparent; -fx-background-radius: 12; }
            .info-panel .content { -fx-background-color: #fbfdfb; }
            .info-panel { -fx-font-size: 13.5px; -fx-font-family: "Consolas", "Menlo", monospace; }

            /* ---------- Buttons ---------- */
            .button { -fx-cursor: hand; -fx-background-radius: 11; -fx-padding: 9 16; -fx-font-weight: 700; }
            .primary-button {
                -fx-background-color: linear-gradient(to right, #2f7d4a, #4aa068);
                -fx-text-fill: white;
                -fx-padding: 12 20;
                -fx-effect: dropshadow(gaussian, rgba(47,125,74,0.35), 14, 0.2, 0, 5);
            }
            .primary-button:hover { -fx-background-color: linear-gradient(to right, #286b40, #3f8c5c); }
            .primary-button:pressed { -fx-background-color: #235c37; }
            .secondary-button {
                -fx-background-color: #eef4ef;
                -fx-text-fill: #2f6d43;
                -fx-border-color: #d5e3d8;
                -fx-border-width: 1;
                -fx-border-radius: 11;
            }
            .secondary-button:hover { -fx-background-color: #e3eee5; }
            .secondary-button:pressed { -fx-background-color: #d7e6da; }

            .toolbar-row {
                -fx-background-color: #ffffff;
                -fx-background-radius: 14;
                -fx-border-color: #e7eee8;
                -fx-border-radius: 14;
                -fx-border-width: 1;
                -fx-padding: 10 12;
            }
            .toolbar-button {
                -fx-background-color: #f4f7f4;
                -fx-text-fill: #33513f;
                -fx-border-color: #dfe8e1;
                -fx-border-width: 1;
                -fx-border-radius: 10;
                -fx-background-radius: 10;
                -fx-padding: 8 14;
                -fx-font-size: 12.5px;
                -fx-font-weight: 700;
            }
            .toolbar-button:hover { -fx-background-color: #e9f0ea; -fx-border-color: #c9dacd; }
            .toolbar-button:pressed { -fx-background-color: #dde8de; }
            .toolbar-button-primary {
                -fx-background-color: linear-gradient(to right, #2f7d4a, #4aa068);
                -fx-text-fill: white;
                -fx-border-color: transparent;
            }
            .toolbar-button-primary:hover { -fx-background-color: linear-gradient(to right, #286b40, #3f8c5c); }
            .toolbar-button-danger {
                -fx-background-color: #fdf1ef;
                -fx-text-fill: #b3402a;
                -fx-border-color: #f4d7d0;
                -fx-border-width: 1;
            }
            .toolbar-button-danger:hover { -fx-background-color: #fbe3de; }

            /* ---------- Sidebar ---------- */
            .sidebar {
                -fx-background-color: linear-gradient(to bottom, #163521, #1f4d31, #245a39);
                -fx-effect: dropshadow(gaussian, rgba(20,50,32,0.22), 20, 0.12, 5, 0);
            }
            .sidebar-logo-mark {
                -fx-background-color: linear-gradient(to bottom right, #ffffff, #d9ecdf);
                -fx-text-fill: #1f4d31;
                -fx-font-weight: 900;
                -fx-font-size: 15px;
                -fx-background-radius: 9;
                -fx-min-width: 30; -fx-min-height: 30;
                -fx-max-width: 30; -fx-max-height: 30;
                -fx-alignment: center;
            }
            .sidebar-logo { -fx-font-size: 22px; -fx-font-weight: 800; -fx-text-fill: white; }
            .sidebar-role {
                -fx-font-size: 10.5px;
                -fx-font-weight: 800;
                -fx-text-fill: #a9d0b6;
                -fx-letter-spacing: 1.2px;
                -fx-padding: 0 0 4 2;
            }
            .nav-button {
                -fx-background-color: transparent;
                -fx-text-fill: #d7e9dc;
                -fx-padding: 11 13;
                -fx-font-size: 13px;
                -fx-font-weight: 600;
                -fx-background-radius: 10;
                -fx-alignment: center-left;
            }
            .nav-button:hover { -fx-background-color: rgba(255,255,255,0.08); -fx-text-fill: white; }
            .nav-button-selected {
                -fx-background-color: rgba(255,255,255,0.18);
                -fx-text-fill: white;
                -fx-effect: dropshadow(gaussian, rgba(0,0,0,0.15), 6, 0.2, 0, 2);
            }

            /* ---------- Header ---------- */
            .top-header {
                -fx-background-color: rgba(255,255,255,0.92);
                -fx-border-color: transparent transparent #e2e9e4 transparent;
                -fx-border-width: 0 0 1 0;
            }
            .avatar-circle {
                -fx-background-color: linear-gradient(to bottom right, #2f7d4a, #57a86f);
                -fx-text-fill: white;
                -fx-font-weight: 800;
                -fx-font-size: 13px;
                -fx-min-width: 38; -fx-min-height: 38;
                -fx-max-width: 38; -fx-max-height: 38;
                -fx-background-radius: 999;
                -fx-alignment: center;
            }
            .header-title { -fx-font-size: 17px; -fx-font-weight: 800; -fx-text-fill: #1c3f28; }
            .header-subtitle { -fx-font-size: 11.5px; -fx-text-fill: #7c8d82; }

            /* ---------- Tables ---------- */
            .table-view, .modern-table {
                -fx-background-color: transparent;
                -fx-border-color: #e3eae5;
                -fx-border-radius: 14;
                -fx-background-radius: 14;
                -fx-padding: 0;
            }
            .table-view .column-header-background {
                -fx-background-color: #f2f7f3;
                -fx-background-radius: 14 14 0 0;
            }
            .table-view .column-header, .table-view .filler {
                -fx-background-color: transparent;
                -fx-padding: 11 10;
                -fx-border-color: transparent transparent #e6ede8 transparent;
            }
            .table-view .column-header .label {
                -fx-text-fill: #33513f;
                -fx-font-weight: 800;
                -fx-font-size: 11.5px;
            }
            .table-row-cell {
                -fx-background-color: white;
                -fx-border-color: transparent transparent #eef2ef transparent;
                -fx-padding: 2 0;
            }
            .table-row-cell:odd { -fx-background-color: #fafcfa; }
            .table-row-cell:hover { -fx-background-color: #f1f8f3; }
            .table-row-cell:selected {
                -fx-background-color: #dcefe1;
                -fx-text-background-color: #16351f;
            }
            .table-view .cell { -fx-padding: 9 10; -fx-text-fill: #2c3d33; }
            .table-view:focused { -fx-background-color: transparent; }

            /* ---------- Split pane ---------- */
            .modern-split { -fx-background-color: transparent; }
            .modern-split .split-pane-divider { -fx-background-color: transparent; -fx-padding: 0 4; }

            /* ---------- Misc ---------- */
            .separator .line { -fx-border-color: #e4ebe6; }
            .scroll-bar:vertical, .scroll-bar:horizontal { -fx-background-color: transparent; }
            .scroll-bar .thumb { -fx-background-color: #c7d6cb; -fx-background-radius: 8; }
            .scroll-bar .thumb:hover { -fx-background-color: #a9bfb0; }
            """;
            Path cssFile = Files.createTempFile("farmersin-theme-", ".css");
            Files.writeString(cssFile, css, StandardCharsets.UTF_8);
            cssFile.toFile().deleteOnExit();
            scene.getStylesheets().add(cssFile.toUri().toString());
        } catch (Exception ignored) {
            scene.getRoot().setStyle("-fx-font-family: 'Segoe UI'; -fx-background-color: #f3f6f2;");
        }
    }

    private void requireLoggedIn() {
        if (currentUser == null) throw new IllegalStateException("You must be logged in.");
    }

    private void requireBuyer() throws SQLException {
        requireLoggedIn();
        if (currentBuyerProfile == null) {
            try {
                currentBuyerProfile = buyerProfileService.getBuyerByUserId(currentUser.getUserId());
            } catch (Exception ex) {
                throw new IllegalStateException("No buyer profile yet for this account. "
                        + "Create one via BuyerProfileService.addBuyerProfile before shopping.");
            }
        }
    }

    private void requireSupplier() throws SQLException {
        requireLoggedIn();
        if (currentSupplierProfile == null) {
            currentSupplierProfile = supplierProfileService.getSupplierByUserId(currentUser.getUserId());
        }
    }

    /** Simple single-line input dialog. Returns empty if the user cancels. */
    private Optional<String> textPrompt(String title, String message) {
        TextInputDialog dialog = new TextInputDialog();
        dialog.setTitle(title);
        dialog.setHeaderText(null);
        dialog.setContentText(message);
        return dialog.showAndWait();
    }

    /** Multi-field input dialog. Returns empty if the user cancels. */
    private Optional<String[]> promptFields(String title, String... labels) {
        Dialog<String[]> dialog = new Dialog<>();
        dialog.setTitle(title);
        dialog.getDialogPane().getButtonTypes().addAll(ButtonType.OK, ButtonType.CANCEL);

        GridPane grid = new GridPane();
        grid.setHgap(10);
        grid.setVgap(10);
        grid.setPadding(new Insets(14));
        TextField[] fields = new TextField[labels.length];
        for (int i = 0; i < labels.length; i++) {
            Label lbl = new Label(labels[i]);
            lbl.getStyleClass().add("muted-text");
            grid.add(lbl, 0, i);
            fields[i] = styledTextField("");
            fields[i].setPrefWidth(230);
            grid.add(fields[i], 1, i);
        }
        dialog.getDialogPane().setContent(grid);

        dialog.setResultConverter(buttonType -> {
            if (buttonType == ButtonType.OK) {
                String[] values = new String[labels.length];
                for (int i = 0; i < labels.length; i++) values[i] = fields[i].getText().trim();
                return values;
            }
            return null;
        });

        return Optional.ofNullable(dialog.showAndWait().orElse(null));
    }

    private void info(String message) {
        Alert alert = new Alert(Alert.AlertType.INFORMATION, message, ButtonType.OK);
        alert.setHeaderText(null);
        alert.showAndWait();
    }

    private void showError(Exception ex) {
        Alert alert = new Alert(Alert.AlertType.ERROR, ex.getMessage() == null ? ex.toString() : ex.getMessage(), ButtonType.OK);
        alert.setHeaderText("Error");
        alert.showAndWait();
    }

}