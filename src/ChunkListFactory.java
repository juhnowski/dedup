import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

public class ChunkListFactory {

    public static List<Chunk> create() {
        List<Chunk>chunks = Collections.synchronizedList(new ArrayList<>());

    //    chunks.add(new Chunk());
        
        // for (int i=0; i < Config.SIZE; i++) {
        //     chunks.add(new Chunk());
        // }

        return chunks;
    }
}
