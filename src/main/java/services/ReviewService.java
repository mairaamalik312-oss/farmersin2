package services;

import dao.reviews;
import model.Review;

import java.sql.SQLException;
import java.util.List;

public class ReviewService {

    private final reviews reviewDAO;

    public ReviewService() {
        this.reviewDAO = new reviews();
    }

    public ReviewService(reviews reviewDAO) {
        if (reviewDAO == null) {
            throw new IllegalArgumentException("Review DAO cannot be null.");
        }
        this.reviewDAO = reviewDAO;
    }

    public boolean addReview(Review review)
            throws SQLException {

        validateReview(review);
        cleanReview(review);

        if (reviewDAO.getReviewByOrderId(review.getOrderId()) != null) {
            throw new IllegalArgumentException(
                    "A review already exists for this order."
            );
        }

        return reviewDAO.addReview(review);
    }

    public Review getReviewById(int reviewId)
            throws SQLException {

        validateReviewId(reviewId);

        Review review = reviewDAO.getReviewById(reviewId);

        if (review == null) {
            throw new IllegalArgumentException("Review not found.");
        }

        return review;
    }

    public Review getReviewByOrderId(int orderId)
            throws SQLException {

        validateOrderId(orderId);
        return reviewDAO.getReviewByOrderId(orderId);
    }

    public List<Review> getReviewsBySupplierId(int supplierId)
            throws SQLException {

        validateSupplierId(supplierId);
        return reviewDAO.getReviewsBySupplierId(supplierId);
    }

    public List<Review> getReviewsByBuyerId(int buyerId)
            throws SQLException {

        validateBuyerId(buyerId);
        return reviewDAO.getReviewsByBuyerId(buyerId);
    }

    public double getAverageRatingForSupplier(int supplierId)
            throws SQLException {

        validateSupplierId(supplierId);
        return reviewDAO.getAverageRatingForSupplier(supplierId);
    }

    public boolean updateReview(Review review)
            throws SQLException {

        if (review == null) {
            throw new IllegalArgumentException("Review cannot be null.");
        }

        validateReviewId(review.getReviewId());
        validateRating(review.getRating());
        cleanReview(review);

        if (reviewDAO.getReviewById(review.getReviewId()) == null) {
            throw new IllegalArgumentException("Review not found.");
        }

        return reviewDAO.updateReview(review);
    }

    public boolean deleteReview(int reviewId)
            throws SQLException {

        validateReviewId(reviewId);

        if (reviewDAO.getReviewById(reviewId) == null) {
            throw new IllegalArgumentException("Review not found.");
        }

        return reviewDAO.deleteReview(reviewId);
    }

    private void validateReview(Review review) {
        if (review == null) {
            throw new IllegalArgumentException("Review cannot be null.");
        }

        validateOrderId(review.getOrderId());
        validateBuyerId(review.getBuyerId());
        validateSupplierId(review.getSupplierId());
        validateRating(review.getRating());
    }

    private void cleanReview(Review review) {
        if (review.getComments() != null) {
            String comments = review.getComments().trim();
            review.setComments(
                    comments.isEmpty() ? null : comments
            );
        }
    }

    private void validateRating(int rating) {
        if (rating < 1 || rating > 5) {
            throw new IllegalArgumentException(
                    "Rating must be between 1 and 5."
            );
        }
    }

    private void validateReviewId(int reviewId) {
        if (reviewId <= 0) {
            throw new IllegalArgumentException(
                    "Review ID must be greater than zero."
            );
        }
    }

    private void validateOrderId(int orderId) {
        if (orderId <= 0) {
            throw new IllegalArgumentException(
                    "Order ID must be greater than zero."
            );
        }
    }

    private void validateBuyerId(int buyerId) {
        if (buyerId <= 0) {
            throw new IllegalArgumentException(
                    "Buyer ID must be greater than zero."
            );
        }
    }

    private void validateSupplierId(int supplierId) {
        if (supplierId <= 0) {
            throw new IllegalArgumentException(
                    "Supplier ID must be greater than zero."
            );
        }
    }
}