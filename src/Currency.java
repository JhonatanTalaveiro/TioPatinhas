import java.util.UUID;

public class Currency extends BaseEntity {
    private final String symbol;
    private final String name;
    private final boolean isFiat;

    public Currency(String symbol, String name, boolean isFiat){
        this.id = UUID.randomUUID();

        this.symbol = symbol;
        this.name = name;
        this.isFiat = isFiat;
    }

    public Currency(UUID id, String symbol, String name, boolean isFiat) {
        this.id = id;

        this.symbol = symbol;
        this.name = name;
        this.isFiat = isFiat;
    }

    public String getSymbol() {
        return symbol;
    }

    public String getName() {
        return name;
    }

    public boolean isFiat() {
        return isFiat;
    }

    @Override
    public String toString() {
        return "Currency{id=" + id + ", symbol='" + symbol + "', name='" + name + "', isFiat=" + isFiat + "}";
    }
}
