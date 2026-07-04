import java.util.Objects;

class Transition {
    String state;
    char symbol;

    Transition(String state, char symbol) {
        this.state = state;
        this.symbol = symbol;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof Transition that)) return false;
        return symbol == that.symbol && state.equals(that.state);
    }

    @Override
    public int hashCode() {
        return Objects.hash(state, symbol);
    }
}