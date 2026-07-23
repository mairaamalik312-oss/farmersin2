package dao;

import database.DBConnection;
import model.Review;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class reviews {

    // 1. Add a New Review (One per order_id due to UNIQUE constraint)
    public boolean addReview(Review review) throws SQLException {
        String query = "INSERT INTO reviews (order_id, buyer_id, supplier_id, rating, comments) " +
                "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, review.getOrderId());
            stmt.setInt(2, review.getBuyerId());
            stmt.setInt(3, review.getSupplierId());
            stmt.setInt(4, review.getRating());

            if (review.getComments() != null) {
                stmt.setString(5, review.getComments());
            } else {
                stmt.setNull(5, Types.VARCHAR);
            }

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        review.setReviewId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    // 2. Get Review by ID
    public Review getReviewById(int reviewId) throws SQLException {
        String query = "SELECT * FROM reviews WHERE review_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, reviewId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToReview(rs);
                }
            }
        }
        return null;
    }

    // 3. Get Review by Order ID (Enforces UNIQUE relationship)
    public Review getReviewByOrderId(int orderId) throws SQLException {
        String query = "SELECT * FROM reviews WHERE order_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, orderId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToReview(rs);
                }
            }
        }
        return null;
    }

    // 4. Get All Reviews for a Specific Supplier
    public List<Review> getReviewsBySupplierId(int supplierId) throws SQLException {
        List<Review> list = new ArrayList<>();
        String query = "SELECT * FROM reviews WHERE supplier_id = ? ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, supplierId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToReview(rs));
                }
            }
        }
        return list;
    }

    // 5. Get All Reviews Written by a Specific Buyer
    public List<Review> getReviewsByBuyerId(int buyerId) throws SQLException {
        List<Review> list = new ArrayList<>();
        String query = "SELECT * FROM reviews WHERE buyer_id = ? ORDER BY created_at DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, buyerId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    list.add(mapResultSetToReview(rs));
                }
            }
        }
        return list;
    }

    // 6. Calculate Average Rating for a Supplier
    public double getAverageRatingForSupplier(int supplierId) throws SQLException {
        String query = "SELECT AVG(rating) FROM reviews WHERE supplier_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, supplierId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return rs.getDouble(1);
                }
            }
        }
        return 0.0;
    }

    // 7. Update Review (Rating and Comments)
    public boolean updateReview(Review review) throws SQLException {
        String query = "UPDATE reviews SET rating = ?, comments = ? WHERE review_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, review.getRating());

            if (review.getComments() != null) {
                stmt.setString(2, review.getComments());
            } else {
                stmt.setNull(2, Types.VARCHAR);
            }

            stmt.setInt(3, review.getReviewId());

            return stmt.executeUpdate() > 0;
        }
    }

    // 8. Delete a Review
    public boolean deleteReview(int reviewId) throws SQLException {
        String query = "DELETE FROM reviews WHERE review_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, reviewId);
            return stmt.executeUpdate() > 0;
        }
    }

    // Helper method to map ResultSet row to Review Object
    private Review mapResultSetToReview(ResultSet rs) throws SQLException {
        Review review = new Review();
        review.setReviewId(rs.getInt("review_id"));
        review.setOrderId(rs.getInt("order_id"));
        review.setBuyerId(rs.getInt("buyer_id"));
        review.setSupplierId(rs.getInt("supplier_id"));
        review.setRating(rs.getInt("rating"));
        review.setComments(rs.getString("comments"));
        review.setCreatedAt(rs.getTimestamp("created_at"));
        return review;
    }
}