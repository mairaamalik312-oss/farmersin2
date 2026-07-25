package services;

import dao.market_prices;
import model.MarketPrice;

import java.math.BigDecimal;
import java.sql.Date;
import java.sql.SQLException;
import java.util.List;

public class MarketPriceService {

    private final market_prices marketPriceDAO;

    public MarketPriceService() {
        this.marketPriceDAO = new market_prices();
    }

    public MarketPriceService(market_prices marketPriceDAO) {
        if (marketPriceDAO == null) {
            throw new IllegalArgumentException(
                    "Market price DAO cannot be null."
            );
        }

        this.marketPriceDAO = marketPriceDAO;
    }

    public boolean addMarketPrice(MarketPrice marketPrice)
            throws SQLException {

        validateMarketPrice(marketPrice);
        cleanMarketPrice(marketPrice);

        return marketPriceDAO.addMarketPrice(
                marketPrice
        );
    }

    public MarketPrice getMarketPriceById(
            int marketPriceId
    ) throws SQLException {

        validateMarketPriceId(marketPriceId);

        MarketPrice marketPrice =
                marketPriceDAO.getMarketPriceById(
                        marketPriceId
                );

        if (marketPrice == null) {
            throw new IllegalArgumentException(
                    "Market price record not found."
            );
        }

        return marketPrice;
    }

    public MarketPrice getLatestPriceByProductAndCity(
            int productId,
            String cityOrMarket
    ) throws SQLException {

        validateProductId(productId);

        if (isBlank(cityOrMarket)) {
            throw new IllegalArgumentException(
                    "City or market is required."
            );
        }

        return marketPriceDAO
                .getLatestPriceByProductAndCity(
                        productId,
                        cityOrMarket.trim()
                );
    }

    public List<MarketPrice> getPriceHistoryByProductId(
            int productId
    ) throws SQLException {

        validateProductId(productId);

        return marketPriceDAO
                .getPriceHistoryByProductId(productId);
    }

    public List<MarketPrice> getPricesByDate(Date priceDate)
            throws SQLException {

        if (priceDate == null) {
            throw new IllegalArgumentException(
                    "Price date is required."
            );
        }

        return marketPriceDAO.getPricesByDate(
                priceDate
        );
    }

    public boolean updateMarketPrice(
            MarketPrice marketPrice
    ) throws SQLException {

        if (marketPrice == null) {
            throw new IllegalArgumentException(
                    "Market price cannot be null."
            );
        }

        validateMarketPriceId(
                marketPrice.getMarketPriceId()
        );

        validateMarketPrice(marketPrice);
        cleanMarketPrice(marketPrice);

        MarketPrice existingMarketPrice =
                marketPriceDAO.getMarketPriceById(
                        marketPrice.getMarketPriceId()
                );

        if (existingMarketPrice == null) {
            throw new IllegalArgumentException(
                    "Market price record not found."
            );
        }

        return marketPriceDAO.updateMarketPrice(
                marketPrice
        );
    }

    public boolean deleteMarketPrice(int marketPriceId)
            throws SQLException {

        validateMarketPriceId(marketPriceId);

        MarketPrice marketPrice =
                marketPriceDAO.getMarketPriceById(
                        marketPriceId
                );

        if (marketPrice == null) {
            throw new IllegalArgumentException(
                    "Market price record not found."
            );
        }

        return marketPriceDAO.deleteMarketPrice(
                marketPriceId
        );
    }

    private void validateMarketPrice(
            MarketPrice marketPrice
    ) {
        if (marketPrice == null) {
            throw new IllegalArgumentException(
                    "Market price cannot be null."
            );
        }

        validateProductId(marketPrice.getProductId());

        if (isBlank(marketPrice.getCityOrMarket())) {
            throw new IllegalArgumentException(
                    "City or market is required."
            );
        }

        validatePrice(
                marketPrice.getMinimumPrice(),
                "Minimum price"
        );

        validatePrice(
                marketPrice.getMaximumPrice(),
                "Maximum price"
        );

        validatePrice(
                marketPrice.getAveragePrice(),
                "Average price"
        );

        if (marketPrice.getMinimumPrice()
                .compareTo(
                        marketPrice.getMaximumPrice()
                ) > 0) {

            throw new IllegalArgumentException(
                    "Minimum price cannot exceed maximum price."
            );
        }

        if (marketPrice.getAveragePrice()
                .compareTo(
                        marketPrice.getMinimumPrice()
                ) < 0
                || marketPrice.getAveragePrice()
                .compareTo(
                        marketPrice.getMaximumPrice()
                ) > 0) {

            throw new IllegalArgumentException(
                    "Average price must be between minimum and maximum price."
            );
        }

        if (isBlank(marketPrice.getUnitType())) {
            throw new IllegalArgumentException(
                    "Unit type is required."
            );
        }

        if (marketPrice.getPriceDate() == null) {
            throw new IllegalArgumentException(
                    "Price date is required."
            );
        }

        if (marketPrice.getEnteredBy() != null
                && marketPrice.getEnteredBy() <= 0) {

            throw new IllegalArgumentException(
                    "Entered-by user ID must be greater than zero."
            );
        }
    }

    private void cleanMarketPrice(
            MarketPrice marketPrice
    ) {
        marketPrice.setCityOrMarket(
                marketPrice.getCityOrMarket().trim()
        );

        marketPrice.setUnitType(
                marketPrice.getUnitType()
                        .trim()
                        .toUpperCase()
        );

        marketPrice.setSource(
                cleanOptionalValue(
                        marketPrice.getSource()
                )
        );
    }

    private void validatePrice(
            BigDecimal price,
            String fieldName
    ) {
        if (price == null) {
            throw new IllegalArgumentException(
                    fieldName + " is required."
            );
        }

        if (price.compareTo(BigDecimal.ZERO) < 0) {
            throw new IllegalArgumentException(
                    fieldName + " cannot be negative."
            );
        }
    }

    private void validateMarketPriceId(
            int marketPriceId
    ) {
        if (marketPriceId <= 0) {
            throw new IllegalArgumentException(
                    "Market price ID must be greater than zero."
            );
        }
    }

    private void validateProductId(int productId) {
        if (productId <= 0) {
            throw new IllegalArgumentException(
                    "Product ID must be greater than zero."
            );
        }
    }

    private String cleanOptionalValue(String value) {
        if (value == null) {
            return null;
        }

        String cleanedValue = value.trim();

        return cleanedValue.isEmpty()
                ? null
                : cleanedValue;
    }

    private boolean isBlank(String value) {
        return value == null
                || value.trim().isEmpty();
    }
}