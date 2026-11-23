import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

public class CurrencyQuote extends BaseEntity {
    Currency currency;
    Currency baseCurrency;
    BigDecimal price;
    LocalDateTime timestamp;

    public CurrencyQuote(Currency currency, Currency baseCurrency, BigDecimal price){
        this.id = UUID.randomUUID();

        this.currency = currency;
        this.baseCurrency = baseCurrency;
        this.price = price;

        this.timestamp = LocalDateTime.now();
    }

    public CurrencyQuote(UUID id, Currency currency, Currency baseCurrency, BigDecimal price, LocalDateTime timestamp){
        this.id = id;
        this.currency = currency;
        this.baseCurrency = baseCurrency;
        this.price = price;
        this.timestamp = timestamp;
    }

    public Currency getCurrency() { return currency; }

    public void setCurrency(Currency currency) { this.currency = currency; }

    public Currency getBaseCurrency() { return baseCurrency; }

    public void setBaseCurrency(Currency currency) { this.baseCurrency = currency; }

    public BigDecimal getPrice() { return price; }

    public void setPrice(BigDecimal price) { this.price = price; }

    public LocalDateTime getTimestamp() { return timestamp; }

    public void setTimestamp(LocalDateTime timestamp) { this.timestamp = timestamp; }

    public String getFormattedPrice(){
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");

        String formattedDate = timestamp.format(formatter);

        return formattedDate + " : " + baseCurrency.getSymbol() + price;
    }

    @Override
    public String toString() {
        return "CurrencyQuote{id=" + id + ", currency=" + currency.getSymbol() +
                ", baseCurrency=" + baseCurrency.getSymbol() +
                ", price=" + price + ", timestamp=" + timestamp + "}";
    }
}