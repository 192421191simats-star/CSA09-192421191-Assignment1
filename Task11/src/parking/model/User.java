package parking.model;

/** A person parking on campus. */
public abstract class User {
    private final String id;

    protected User(String id) {
        if (id == null || id.isBlank()) throw new IllegalArgumentException("User id required");
        this.id = id;
    }

    public String getId() { return id; }

    /** True if the user parks free of charge (valid monthly pass). */
    public abstract boolean hasFreeParking();

    public String category() { return getClass().getSimpleName().toUpperCase(); }

    @Override public String toString() {
        return category() + (hasFreeParking() ? "(pass)" : "");
    }
}
