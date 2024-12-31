import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

/// 1.4.3.2 Этот объект имеет внутреннее состояние, которое резервирует пространство на диске (ленте).
public class ReservedChunk {
    /// 1.4.3.3 Резервирование выполняется за счёт хранения номера блока (отступа на ленте) (chi)
    public static AtomicInteger currentChunckIndex = new AtomicInteger(0);// номер чанка
    //TODO 1.4.3.4 при этом гарантируется, что никакой другой контроллер не хранит этот же самый номер

    public static List<Chunk> chunks = ChunkListFactory.create();

    static {
        chunks.add(new Chunk());
    }

    public static synchronized ILBA addBlock(String block) throws Exception {
        Chunk ch;

        /// 1.4.3.5.1 проверяет очередь сборщика мусора, которая хранит список адресов iLBA, доступных для перезаписи.
        if (GarbageCollector.gcQueue.isEmpty()) {
            /// 1.4.3.6 Если очередь оказалась пустой, то объект Reserved Chunk проверяет наличие места в чанке.
            int chunkIndex = currentChunckIndex.get();
            ch = chunks.get(chunkIndex);
            int blockIndex = ch.append(block);
            if (blockIndex == -1) {
                chunkIndex = currentChunckIndex.incrementAndGet();
                if (chunkIndex < Config.CHUNK_INDEX_MAX) {
                    ch = new Chunk();
                    blockIndex = ch.append(block);
                    chunks.add(ch);
                } else {
                    throw new Exception("Место кончилось");
                }
                
            }

            /// 1.4.3.6.6 В качестве результата возвращается iLBA вставленного блока
            return new ILBA(chunkIndex,blockIndex);

        } else {
            /// 1.4.3.5.3 Если очередь не пуста, то берётся и удаляется первый элемент (qiLBA) из этой очереди (q означает, что это iLBA из очереди).
            ILBA qiLBA = GarbageCollector.gcQueue.poll();
            /// 1.4.3.5.4 Отправляется запрос на запись на диск данных (qiLBA, data [i]). В качестве результата возвращается qiLBA.
            ch = chunks.get(qiLBA.getChunk());
            ch.blocks.add(qiLBA.block,block);
            return qiLBA;
        }
    }


}
