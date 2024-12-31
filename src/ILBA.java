import java.util.Objects;

public class ILBA implements Comparable<ILBA>{
    private final int chunk;
    public final int block;

    public ILBA(int chunk, int block){
        this.chunk = chunk;
        this.block = block;
    }

    public int getChunk() {
        return chunk;
    }

    public int getBlock() {
        return block;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ILBA ilba = (ILBA) o;
        return chunk == ilba.chunk && block == ilba.block;
    }

    @Override
    public int compareTo(ILBA otherILBA) {
        int c = Integer.compare(getChunk(), otherILBA.getChunk());
        if (c==0) {
            return Integer.compare(getBlock(), otherILBA.getBlock());
        } else {
            return c;
        }
    }

    @Override
    public String toString(){
        return "ILBA [chunk="+chunk+" block="+block +"]";
    }
}
