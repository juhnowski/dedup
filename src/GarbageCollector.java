import java.util.concurrent.ConcurrentLinkedQueue;

public class GarbageCollector implements Runnable {
    /// 1.4.3.5.2 Очередь сборщика мусора хранит список адресов iLBA, доступных для перезаписи.
    public static ConcurrentLinkedQueue<ILBA> gcQueue = new ConcurrentLinkedQueue<>();

    @Override
    public void run() {
        Dedup.C.forEach((i,c)->{
            if (c.get()==0) {
                Dedup.C.remove(i);
                BlockHash hash = Dedup.H.get(i);
                Dedup.T.remove(hash);
                Dedup.H.remove(i);
            }
        });
    }
}
