package services;

import dao.conversations;
import model.Conversation;

import java.sql.SQLException;
import java.util.List;

public class ConversationService {

    private final conversations conversationDAO;

    public ConversationService() {
        this.conversationDAO = new conversations();
    }

    public ConversationService(conversations conversationDAO) {
        if (conversationDAO == null) {
            throw new IllegalArgumentException(
                    "Conversation DAO cannot be null."
            );
        }

        this.conversationDAO = conversationDAO;
    }

    public boolean addConversation(Conversation conversation)
            throws SQLException {

        validateConversation(conversation);

        Conversation existingConversation =
                conversationDAO.getConversationByBuyerAndSupplier(
                        conversation.getBuyerId(),
                        conversation.getSupplierId()
                );

        if (existingConversation != null) {
            throw new IllegalArgumentException(
                    "A conversation already exists between this buyer and supplier."
            );
        }

        return conversationDAO.addConversation(conversation);
    }

    public Conversation getConversationById(int conversationId)
            throws SQLException {

        validateConversationId(conversationId);

        Conversation conversation =
                conversationDAO.getConversationById(
                        conversationId
                );

        if (conversation == null) {
            throw new IllegalArgumentException(
                    "Conversation not found."
            );
        }

        return conversation;
    }

    public Conversation getConversationByBuyerAndSupplier(
            int buyerId,
            int supplierId
    ) throws SQLException {

        validateBuyerId(buyerId);
        validateSupplierId(supplierId);

        return conversationDAO
                .getConversationByBuyerAndSupplier(
                        buyerId,
                        supplierId
                );
    }

    public Conversation getOrCreateConversation(
            int buyerId,
            int supplierId,
            Integer orderId
    ) throws SQLException {

        validateBuyerId(buyerId);
        validateSupplierId(supplierId);

        if (orderId != null) {
            validateOrderId(orderId);
        }

        return conversationDAO.getOrCreateConversation(
                buyerId,
                supplierId,
                orderId
        );
    }

    public List<Conversation> getConversationsByBuyerId(
            int buyerId
    ) throws SQLException {

        validateBuyerId(buyerId);

        return conversationDAO
                .getConversationsByBuyerId(buyerId);
    }

    public List<Conversation> getConversationsBySupplierId(
            int supplierId
    ) throws SQLException {

        validateSupplierId(supplierId);

        return conversationDAO
                .getConversationsBySupplierId(supplierId);
    }

    public Conversation getConversationByOrderId(int orderId)
            throws SQLException {

        validateOrderId(orderId);

        return conversationDAO.getConversationByOrderId(
                orderId
        );
    }

    public boolean deleteConversation(int conversationId)
            throws SQLException {

        validateConversationId(conversationId);

        Conversation conversation =
                conversationDAO.getConversationById(
                        conversationId
                );

        if (conversation == null) {
            throw new IllegalArgumentException(
                    "Conversation not found."
            );
        }

        return conversationDAO.deleteConversation(
                conversationId
        );
    }

    private void validateConversation(
            Conversation conversation
    ) {
        if (conversation == null) {
            throw new IllegalArgumentException(
                    "Conversation cannot be null."
            );
        }

        validateBuyerId(conversation.getBuyerId());
        validateSupplierId(conversation.getSupplierId());

        if (conversation.getOrderId() != null) {
            validateOrderId(conversation.getOrderId());
        }
    }

    private void validateConversationId(int conversationId) {
        if (conversationId <= 0) {
            throw new IllegalArgumentException(
                    "Conversation ID must be greater than zero."
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

    private void validateOrderId(int orderId) {
        if (orderId <= 0) {
            throw new IllegalArgumentException(
                    "Order ID must be greater than zero."
            );
        }
    }
}