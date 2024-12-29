import java.util.Objects;
import java.util.Optional;

public class BlockHash implements Comparable<BlockHash>{
    public int blockHash;

    public BlockHash(String block) {
        this.blockHash = block.hashCode();
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        BlockHash blockHash1 = (BlockHash) o;
        return blockHash == blockHash1.blockHash;
    }

    @Override
    public int hashCode() {
        return Objects.hashCode(Optional.of(blockHash));
    }

    @Override
    public int compareTo(BlockHash otherBlockHash) {
        return Integer.compare(blockHash, otherBlockHash.blockHash);
    }
}
