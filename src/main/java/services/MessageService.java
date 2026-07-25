package services;

import dao.messages;
import model.Message;

import java.sql.SQLException;
import java.util.List;

public class MessageService {

    private final messages messageDAO;

    public MessageService() {
        this.messageDAO = new messages();
    }

    public MessageService(messages messageDAO) {
        if (messageDAO == null) {
            throw new IllegalArgumentException("Message DAO cannot be null.");
        }
        this.messageDAO = messageDAO;
    }

    public boolean addMessage(Message message) throws SQLException {
        validateMessage(message);
        message.setMessageText(message.getMessageText().trim());
        message.setRead(false);
        return messageDAO.addMessage(message);
    }

    public Message getMessageById(int messageId) throws SQLException {
        validateMessageId(messageId);

        Message message = messageDAO.getMessageById(messageId);

        if (message == null) {
            throw new IllegalArgumentException("Message not found.");
        }

        return message;
    }

    public List<Message> getMessagesByConversationId(int conversationId)
            throws SQLException {

        validateConversationId(conversationId);
        return messageDAO.getMessagesByConversationId(conversationId);
    }

    public boolean markMessagesAsRead(
            int conversationId,
            int currentUserId
    ) throws SQLException {

        validateConversationId(conversationId);
        validateUserId(currentUserId);

        return messageDAO.markMessagesAsRead(
                conversationId,
                currentUserId
        );
    }

    public int getUnreadMessageCount(
            int conversationId,
            int recipientUserId
    ) throws SQLException {

        validateConversationId(conversationId);
        validateUserId(recipientUserId);

        return messageDAO.getUnreadMessageCount(
                conversationId,
                recipientUserId
        );
    }

    public boolean deleteMessage(int messageId)
            throws SQLException {

        validateMessageId(messageId);

        Message message = messageDAO.getMessageById(messageId);

        if (message == null) {
            throw new IllegalArgumentException("Message not found.");
        }

        return messageDAO.deleteMessage(messageId);
    }

    private void validateMessage(Message message) {
        if (message == null) {
            throw new IllegalArgumentException(
                    "Message cannot be null."
            );
        }

        validateConversationId(message.getConversationId());
        validateUserId(message.getSenderUserId());

        if (isBlank(message.getMessageText())) {
            throw new IllegalArgumentException(
                    "Message text is required."
            );
        }
    }

    private void validateMessageId(int messageId) {
        if (messageId <= 0) {
            throw new IllegalArgumentException(
                    "Message ID must be greater than zero."
            );
        }
    }

    private void validateConversationId(int conversationId) {
        if (conversationId <= 0) {
            throw new IllegalArgumentException(
                    "Conversation ID must be greater than zero."
            );
        }
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