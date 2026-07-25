package services;

import dao.carts;
import model.Cart;

import java.sql.SQLException;

public class CartService {

    private final carts cartDAO;

    public CartService() {
        this.cartDAO = new carts();
    }

    public CartService(carts cartDAO) {
        if (cartDAO == null) {
            throw new IllegalArgumentException(
                    "Cart DAO cannot be null."
            );
        }

        this.cartDAO = cartDAO;
    }

    public boolean addCart(Cart cart)
            throws SQLException {

        validateCart(cart);

        Cart existingCart =
                cartDAO.getCartByBuyerId(cart.getBuyerId());

        if (existingCart != null) {
            throw new IllegalArgumentException(
                    "A cart already exists for this buyer."
            );
        }

        return cartDAO.addCart(cart);
    }

    public Cart getCartById(int cartId)
            throws SQLException {

        validateCartId(cartId);

        Cart cart = cartDAO.getCartById(cartId);

        if (cart == null) {
            throw new IllegalArgumentException(
                    "Cart not found."
            );
        }

        return cart;
    }

    public Cart getCartByBuyerId(int buyerId)
            throws SQLException {

        validateBuyerId(buyerId);

        Cart cart =
                cartDAO.getCartByBuyerId(buyerId);

        if (cart == null) {
            throw new IllegalArgumentException(
                    "Cart not found for this buyer."
            );
        }

        return cart;
    }

    public Cart getOrCreateCartByBuyerId(int buyerId)
            throws SQLException {

        validateBuyerId(buyerId);

        return cartDAO.getOrCreateCartByBuyerId(buyerId);
    }

    public boolean deleteCart(int cartId)
            throws SQLException {

        validateCartId(cartId);

        Cart existingCart =
                cartDAO.getCartById(cartId);

        if (existingCart == null) {
            throw new IllegalArgumentException(
                    "Cart not found."
            );
        }

        return cartDAO.deleteCart(cartId);
    }

    public boolean deleteCartByBuyerId(int buyerId)
            throws SQLException {

        validateBuyerId(buyerId);

        Cart existingCart =
                cartDAO.getCartByBuyerId(buyerId);

        if (existingCart == null) {
            throw new IllegalArgumentException(
                    "Cart not found for this buyer."
            );
        }

        return cartDAO.deleteCartByBuyerId(buyerId);
    }

    private void validateCart(Cart cart) {
        if (cart == null) {
            throw new IllegalArgumentException(
                    "Cart cannot be null."
            );
        }

        validateBuyerId(cart.getBuyerId());
    }

    private void validateCartId(int cartId) {
        if (cartId <= 0) {
            throw new IllegalArgumentException(
                    "Cart ID must be greater than zero."
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
}