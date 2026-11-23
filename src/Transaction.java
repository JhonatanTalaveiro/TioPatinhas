import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.UUID;

public class Transaction extends BaseEntity {
    Wallet wallet;
    Currency currency;
    BigDecimal quantity;
    BigDecimal priceAtTime;
    TransactionType type;
    LocalDateTime executedAt;

    public Transaction(Wallet wallet, Currency currency, BigDecimal quantity, BigDecimal priceAtTime, TransactionType type){
        this.id = UUID.randomUUID();

        this.wallet = wallet;
        this.currency = currency;
        this.quantity = quantity;
        this.priceAtTime = priceAtTime;
        this.type = type;

        this.executedAt = LocalDateTime.now();
    }

    public Transaction(UUID id, Wallet wallet, Currency currency, BigDecimal quantity, BigDecimal priceAtTime, TransactionType type, LocalDateTime executedAt){
        this.id = id;
        this.executedAt = executedAt;

        this.wallet = wallet;
        this.currency = currency;
        this.quantity = quantity;
        this.priceAtTime = priceAtTime;
        this.type = type;
    }

    public BigDecimal getTotalValue(){
        return (quantity.multiply(priceAtTime));
    }

    // Getters e setters usados pelos menus (MenuTransacao) e pelo Formatador.
    // Os atributos continuam com visibilidade de pacote por compatibilidade com
    // o código das fases anteriores, que os acessa diretamente.

    public Wallet getWallet() { return wallet; }

    public void setWallet(Wallet wallet) { this.wallet = wallet; }

    public Currency getCurrency() { return currency; }

    public void setCurrency(Currency currency) { this.currency = currency; }

    public BigDecimal getQuantity() { return quantity; }

    public void setQuantity(BigDecimal quantity) { this.quantity = quantity; }

    public BigDecimal getPriceAtTime() { return priceAtTime; }

    public void setPriceAtTime(BigDecimal priceAtTime) { this.priceAtTime = priceAtTime; }

    public TransactionType getType() { return type; }

    public void setType(TransactionType type) { this.type = type; }

    public LocalDateTime getExecutedAt() { return executedAt; }

    public void setExecutedAt(LocalDateTime executedAt) { this.executedAt = executedAt; }

    @Override
    public String toString() {
        return "Transaction{id=" + id +
                ", walletId=" + wallet.getId() +
                ", currency=" + currency.getSymbol() +
                ", quantity=" + quantity +
                ", priceAtTime=" + priceAtTime +
                ", type=" + type +
                ", executedAt=" + executedAt + "}";
    }
}
