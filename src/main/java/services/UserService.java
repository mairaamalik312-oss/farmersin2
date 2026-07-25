package services;

import dao.users;
import model.User;

import java.sql.SQLException;
import java.util.List;
import java.util.regex.Pattern;

public class UserService {

    private final users userDAO;

    private static final Pattern EMAIL_PATTERN =
            Pattern.compile("^[A-Za-z0-9+_.-]+@[A-Za-z0-9.-]+$");

    public UserService() {
        this.userDAO = new users();
    }

    public UserService(users userDAO) {
        if (userDAO == null) {
            throw new IllegalArgumentException(
                    "User DAO cannot be null."
            );
        }
        this.userDAO = userDAO;
    }

    public boolean addUser(User user)
            throws SQLException {

        validateUser(user);
        cleanUser(user);

        if (userDAO.getUserByEmail(user.getEmail()) != null) {
            throw new IllegalArgumentException(
                    "A user with this email already exists."
            );
        }

        user.setAccountStatus("PENDING");
        user.setEmailVerified(false);

        return userDAO.addUser(user);
    }

    public User getUserById(int userId)
            throws SQLException {

        validateUserId(userId);

        User user = userDAO.getUserById(userId);

        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }

        return user;
    }

    public User getUserByEmail(String email)
            throws SQLException {

        validateEmail(email);

        User user = userDAO.getUserByEmail(
                email.trim().toLowerCase()
        );

        if (user == null) {
            throw new IllegalArgumentException("User not found.");
        }

        return user;
    }

    public boolean updateUserProfile(User user)
            throws SQLException {

        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }

        validateUserId(user.getUserId());

        if (isBlank(user.getFullName())) {
            throw new IllegalArgumentException(
                    "Full name is required."
            );
        }

        user.setFullName(user.getFullName().trim());

        if (user.getPhone() != null) {
            String phone = user.getPhone().trim();
            user.setPhone(phone.isEmpty() ? null : phone);
        }

        if (userDAO.getUserById(user.getUserId()) == null) {
            throw new IllegalArgumentException("User not found.");
        }

        return userDAO.updateUserProfile(user);
    }

    public boolean updatePassword(
            int userId,
            String newPasswordHash
    ) throws SQLException {

        validateUserId(userId);

        if (isBlank(newPasswordHash)) {
            throw new IllegalArgumentException(
                    "Password hash is required."
            );
        }

        if (userDAO.getUserById(userId) == null) {
            throw new IllegalArgumentException("User not found.");
        }

        return userDAO.updatePassword(
                userId,
                newPasswordHash.trim()
        );
    }

    public boolean updateAccountStatus(
            int userId,
            String status
    ) throws SQLException {

        validateUserId(userId);

        String formattedStatus =
                validateAndFormatAccountStatus(status);

        if (userDAO.getUserById(userId) == null) {
            throw new IllegalArgumentException("User not found.");
        }

        return userDAO.updateAccountStatus(
                userId,
                formattedStatus
        );
    }

    public boolean setEmailVerified(
            int userId,
            boolean isVerified
    ) throws SQLException {

        validateUserId(userId);

        if (userDAO.getUserById(userId) == null) {
            throw new IllegalArgumentException("User not found.");
        }

        return userDAO.setEmailVerified(
                userId,
                isVerified
        );
    }

    public List<User> getUsersByRole(String role)
            throws SQLException {

        String formattedRole =
                validateAndFormatRole(role);

        return userDAO.getUsersByRole(formattedRole);
    }

    private void validateUser(User user) {
        if (user == null) {
            throw new IllegalArgumentException("User cannot be null.");
        }

        if (isBlank(user.getFullName())) {
            throw new IllegalArgumentException(
                    "Full name is required."
            );
        }

        validateEmail(user.getEmail());

        if (isBlank(user.getPasswordHash())) {
            throw new IllegalArgumentException(
                    "Password hash is required."
            );
        }

        validateAndFormatRole(user.getRole());
    }

    private void cleanUser(User user) {
        user.setFullName(user.getFullName().trim());

        user.setEmail(
                user.getEmail().trim().toLowerCase()
        );

        user.setPasswordHash(
                user.getPasswordHash().trim()
        );

        user.setRole(
                validateAndFormatRole(user.getRole())
        );

        if (user.getPhone() != null) {
            String phone = user.getPhone().trim();
            user.setPhone(phone.isEmpty() ? null : phone);
        }
    }

    private void validateEmail(String email) {
        if (isBlank(email)) {
            throw new IllegalArgumentException(
                    "Email is required."
            );
        }

        if (!EMAIL_PATTERN.matcher(
                email.trim()
        ).matches()) {
            throw new IllegalArgumentException(
                    "Invalid email format."
            );
        }
    }

    private String validateAndFormatRole(String role) {
        if (isBlank(role)) {
            throw new IllegalArgumentException(
                    "User role is required."
            );
        }

        String formatted = role.trim().toUpperCase();

        if (!formatted.equals("ADMIN")
                && !formatted.equals("BUYER")
                && !formatted.equals("SUPPLIER")) {
            throw new IllegalArgumentException(
                    "Invalid user role."
            );
        }

        return formatted;
    }

    private String validateAndFormatAccountStatus(
            String status
    ) {
        if (isBlank(status)) {
            throw new IllegalArgumentException(
                    "Account status is required."
            );
        }

        String formatted = status.trim().toUpperCase();

        if (!formatted.equals("PENDING")
                && !formatted.equals("ACTIVE")
                && !formatted.equals("BLOCKED")
                && !formatted.equals("REJECTED")) {
            throw new IllegalArgumentException(
                    "Invalid account status."
            );
        }

        return formatted;
    }

    private void validateUserId(int userId) {
        if (userId <= 0) {
            throw new IllegalArgumentException(
                    "User ID must be greater than zero."
            );
        }
    }

    private boolean isBlank(String value) {
        return value == null || value.trim().isEmpty();
    }
}