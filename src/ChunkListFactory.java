import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChunkListFactory {
    // Допустим на нашу ленту помещается 2 чанка, нумерация с 0
    public static int SIZE = 20;

    public static List<Chunk> create() {
        List<Chunk>chunks = Collections.synchronizedList(new ArrayList<>());

        for (int i=0; i<SIZE; i++) {
            chunks.add(new Chunk());
        }

        return chunks;
    }
}
