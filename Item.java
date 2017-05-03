/**
 * Created by anish on 4/28/17.
 */
public class Item {
    private String name;

    public Item(String name) {
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public boolean equals(Object o) {
        return ((Item) o).name.equals(name);
    }

    public int hashCode() {
        return name.hashCode();
    }

    public String toString()
    {
        return name;
    }
}
