package model;

import java.math.BigDecimal;
import java.sql.Date;

public class MarketPrice {
    private int marketPriceId;
    private int productId;
    private String cityOrMarket;
    private BigDecimal minimumPrice;
    private BigDecimal maximumPrice;
    private BigDecimal averagePrice;
    private String unitType;
    private Date priceDate;
    private Integer enteredBy; // Integer wrapper to allow NULL values
    private String source;

    // Default Constructor
    public MarketPrice() {}

    // Constructor for adding a new market price entry
    public MarketPrice(int productId, String cityOrMarket, BigDecimal minimumPrice,
                       BigDecimal maximumPrice, BigDecimal averagePrice, String unitType,
                       Date priceDate, Integer enteredBy, String source) {
        this.productId = productId;
        this.cityOrMarket = cityOrMarket;
        this.minimumPrice = minimumPrice;
        this.maximumPrice = maximumPrice;
        this.averagePrice = averagePrice;
        this.unitType = unitType;
        this.priceDate = priceDate;
        this.enteredBy = enteredBy;
        this.source = source;
    }

    // Full Constructor
    public MarketPrice(int marketPriceId, int productId, String cityOrMarket,
                       BigDecimal minimumPrice, BigDecimal maximumPrice, BigDecimal averagePrice,
                       String unitType, Date priceDate, Integer enteredBy, String source) {
        this.marketPriceId = marketPriceId;
        this.productId = productId;
        this.cityOrMarket = cityOrMarket;
        this.minimumPrice = minimumPrice;
        this.maximumPrice = maximumPrice;
        this.averagePrice = averagePrice;
        this.unitType = unitType;
        this.priceDate = priceDate;
        this.enteredBy = enteredBy;
        this.source = source;
    }

    // Getters and Setters
    public int getMarketPriceId() { return marketPriceId; }
    public void setMarketPriceId(int marketPriceId) { this.marketPriceId = marketPriceId; }

    public int getProductId() { return productId; }
    public void setProductId(int productId) { this.productId = productId; }

    public String getCityOrMarket() { return cityOrMarket; }
    public void setCityOrMarket(String cityOrMarket) { this.cityOrMarket = cityOrMarket; }

    public BigDecimal getMinimumPrice() { return minimumPrice; }
    public void setMinimumPrice(BigDecimal minimumPrice) { this.minimumPrice = minimumPrice; }

    public BigDecimal getMaximumPrice() { return maximumPrice; }
    public void setMaximumPrice(BigDecimal maximumPrice) { this.maximumPrice = maximumPrice; }

    public BigDecimal getAveragePrice() { return averagePrice; }
    public void setAveragePrice(BigDecimal averagePrice) { this.averagePrice = averagePrice; }

    public String getUnitType() { return unitType; }
    public void setUnitType(String unitType) { this.unitType = unitType; }

    public Date getPriceDate() { return priceDate; }
    public void setPriceDate(Date priceDate) { this.priceDate = priceDate; }

    public Integer getEnteredBy() { return enteredBy; }
    public void setEnteredBy(Integer enteredBy) { this.enteredBy = enteredBy; }

    public String getSource() { return source; }
    public void setSource(String source) { this.source = source; }
}
