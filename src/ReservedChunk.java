import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListSet;
import java.util.concurrent.atomic.AtomicInteger;

/// 1.4.3.2 Этот объект имеет внутреннее состояние, которое резервирует пространство на диске (ленте).
public class ReservedChunk {
    /// 1.4.3.3 Резервирование выполняется за счёт хранения номера блока (отступа на ленте) (chi)
    public static AtomicInteger chi = new AtomicInteger(0);// номера блока (отступа на ленте)
    //TODO 1.4.3.4 при этом гарантируется, что никакой другой контроллер не хранит этот же самый номер


    public static List<Chunk> chunks = ChunkListFactory.create();

    public static synchronized ILBA addBlock(String block) throws Exception {
        Chunk ch;
        int blockIndex=0;
        /// 1.4.3.5.1 проверяет очередь сборщика мусора, которая хранит список адресов iLBA, доступных для перезаписи.
        if (GarbageCollector.gcQueue.isEmpty()) {
            /// 1.4.3.6 Если очередь оказалась пустой, то объект Reserved Chunk проверяет наличие места в чанке.
            int current = chi.get();
            ch = chunks.get(current);
            /// 1.4.3.6.3 тогда проверка сводится к тому, что есть место для записи блока
            if (Chunk.RCHS-ch.blocks.size() < 0) {
                /// 1.4.3.6.4 Если место закончилось, то выполняется резервирование нового чанка (при этом cbs становится 0).
                current = chi.incrementAndGet();
                if (current>ChunkListFactory.SIZE) throw new Exception("Место кончилось");
                chunks.add(new Chunk());
            } else {
                /// 1.4.3.6.5 Если место есть, то блок записывается в конец, значение cbs увеличивается на 1.
                blockIndex = ch.cbs.incrementAndGet();
                ch.blocks.add(blockIndex,block);
            }
            /// 1.4.3.6.6 В качестве результата возвращается iLBA вставленного блока
            return new ILBA(current,blockIndex);
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
