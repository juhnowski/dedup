import java.util.Objects;
import java.util.Optional;

public class LBA implements Comparable<LBA>{
    private final int lba;

    public LBA(int lba) {
        this.lba = lba;
    }

    public int getLba() {
        return lba;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        LBA lba1 = (LBA) o;
        return lba == lba1.lba;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(Optional.of(lba));
    }

    @Override
    public int compareTo(LBA otherLBA) {
        return Integer.compare(getLba(), otherLBA.getLba());
    }

    @Override
    public String toString() {
        return "LBA="+lba;
    }
}
