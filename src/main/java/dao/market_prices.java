package dao;

import database.DBConnection; // Updated import
import model.MarketPrice;

import java.sql.*;
import java.util.ArrayList;
import java.util.List;

public class market_prices {

    // 1. Add new Market Price Record
    public boolean addMarketPrice(MarketPrice mp) throws SQLException {
        String query = "INSERT INTO market_prices (product_id, city_or_market, minimum_price, maximum_price, " +
                "average_price, unit_type, price_date, entered_by, source) VALUES (?, ?, ?, ?, ?, ?, ?, ?, ?)";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query, Statement.RETURN_GENERATED_KEYS)) {

            stmt.setInt(1, mp.getProductId());
            stmt.setString(2, mp.getCityOrMarket());
            stmt.setBigDecimal(3, mp.getMinimumPrice());
            stmt.setBigDecimal(4, mp.getMaximumPrice());
            stmt.setBigDecimal(5, mp.getAveragePrice());
            stmt.setString(6, mp.getUnitType());
            stmt.setDate(7, mp.getPriceDate());

            if (mp.getEnteredBy() != null) {
                stmt.setInt(8, mp.getEnteredBy());
            } else {
                stmt.setNull(8, Types.INTEGER);
            }

            if (mp.getSource() != null) {
                stmt.setString(9, mp.getSource());
            } else {
                stmt.setNull(9, Types.VARCHAR);
            }

            int affectedRows = stmt.executeUpdate();
            if (affectedRows > 0) {
                try (ResultSet rs = stmt.getGeneratedKeys()) {
                    if (rs.next()) {
                        mp.setMarketPriceId(rs.getInt(1));
                    }
                }
                return true;
            }
        }
        return false;
    }

    // 2. Get Market Price by ID
    public MarketPrice getMarketPriceById(int marketPriceId) throws SQLException {
        String query = "SELECT * FROM market_prices WHERE market_price_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, marketPriceId);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMarketPrice(rs);
                }
            }
        }
        return null;
    }

    // 3. Get Latest Market Price for a Product in a Specific City/Market
    public MarketPrice getLatestPriceByProductAndCity(int productId, String cityOrMarket) throws SQLException {
        String query = "SELECT * FROM market_prices WHERE product_id = ? AND city_or_market = ? " +
                "ORDER BY price_date DESC LIMIT 1";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, productId);
            stmt.setString(2, cityOrMarket);
            try (ResultSet rs = stmt.executeQuery()) {
                if (rs.next()) {
                    return mapResultSetToMarketPrice(rs);
                }
            }
        }
        return null;
    }

    // 4. Get Price History for a Specific Product
    public List<MarketPrice> getPriceHistoryByProductId(int productId) throws SQLException {
        List<MarketPrice> prices = new ArrayList<>();
        String query = "SELECT * FROM market_prices WHERE product_id = ? ORDER BY price_date DESC";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, productId);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    prices.add(mapResultSetToMarketPrice(rs));
                }
            }
        }
        return prices;
    }

    // 5. Get Market Prices by Date
    public List<MarketPrice> getPricesByDate(Date priceDate) throws SQLException {
        List<MarketPrice> prices = new ArrayList<>();
        String query = "SELECT * FROM market_prices WHERE price_date = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setDate(1, priceDate);
            try (ResultSet rs = stmt.executeQuery()) {
                while (rs.next()) {
                    prices.add(mapResultSetToMarketPrice(rs));
                }
            }
        }
        return prices;
    }

    // 6. Update Market Price Entry
    public boolean updateMarketPrice(MarketPrice mp) throws SQLException {
        String query = "UPDATE market_prices SET product_id = ?, city_or_market = ?, minimum_price = ?, " +
                "maximum_price = ?, average_price = ?, unit_type = ?, price_date = ?, entered_by = ?, " +
                "source = ? WHERE market_price_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, mp.getProductId());
            stmt.setString(2, mp.getCityOrMarket());
            stmt.setBigDecimal(3, mp.getMinimumPrice());
            stmt.setBigDecimal(4, mp.getMaximumPrice());
            stmt.setBigDecimal(5, mp.getAveragePrice());
            stmt.setString(6, mp.getUnitType());
            stmt.setDate(7, mp.getPriceDate());

            if (mp.getEnteredBy() != null) {
                stmt.setInt(8, mp.getEnteredBy());
            } else {
                stmt.setNull(8, Types.INTEGER);
            }

            if (mp.getSource() != null) {
                stmt.setString(9, mp.getSource());
            } else {
                stmt.setNull(9, Types.VARCHAR);
            }

            stmt.setInt(10, mp.getMarketPriceId());

            return stmt.executeUpdate() > 0;
        }
    }

    // 7. Delete Market Price Entry
    public boolean deleteMarketPrice(int marketPriceId) throws SQLException {
        String query = "DELETE FROM market_prices WHERE market_price_id = ?";

        try (Connection conn = DBConnection.getConnection();
             PreparedStatement stmt = conn.prepareStatement(query)) {

            stmt.setInt(1, marketPriceId);
            return stmt.executeUpdate() > 0;
        }
    }

    // Helper method to map ResultSet row to MarketPrice Object
    private MarketPrice mapResultSetToMarketPrice(ResultSet rs) throws SQLException {
        MarketPrice mp = new MarketPrice();
        mp.setMarketPriceId(rs.getInt("market_price_id"));
        mp.setProductId(rs.getInt("product_id"));
        mp.setCityOrMarket(rs.getString("city_or_market"));
        mp.setMinimumPrice(rs.getBigDecimal("minimum_price"));
        mp.setMaximumPrice(rs.getBigDecimal("maximum_price"));
        mp.setAveragePrice(rs.getBigDecimal("average_price"));
        mp.setUnitType(rs.getString("unit_type"));
        mp.setPriceDate(rs.getDate("price_date"));

        int enteredBy = rs.getInt("entered_by");
        if (!rs.wasNull()) {
            mp.setEnteredBy(enteredBy);
        } else {
            mp.setEnteredBy(null);
        }

        mp.setSource(rs.getString("source"));
        return mp;
    }
}
