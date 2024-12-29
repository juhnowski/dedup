import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Dedup {
    ///DedupT - таблица дедупликации
    // Связывает iLBA c хэш
    public static final ConcurrentSkipListMap<ILBA, BlockHash> H = new ConcurrentSkipListMap<>();
    // Связывает хэш с iLBA
    public static final ConcurrentSkipListMap<BlockHash,ILBA> T = new ConcurrentSkipListMap<>();
    // Связывает iLBA со счетчиком использования
    public static final ConcurrentSkipListMap<ILBA, AtomicInteger> C = new ConcurrentSkipListMap<>();

    public static synchronized void remove(ILBA ilba){
        BlockHash h = H.get(ilba);
        T.remove(h);
        C.remove(ilba);
        H.remove(ilba);
    }

    public static synchronized void dec(ILBA ilba){
        if (C.get(ilba).decrementAndGet() < 1 ) {
            remove(ilba);
        }
    }

    public static synchronized void inc(ILBA ilba) {
        C.get(ilba).incrementAndGet();
    }

    public static synchronized void create(BlockHash hash, ILBA ilba) {
        T.put(hash, ilba);
        H.put(ilba, hash);
        C.put(ilba, new AtomicInteger(1));
    }

    public static synchronized ILBA getILBAByHash(BlockHash hash) {
        return T.get(hash);
    }
}
