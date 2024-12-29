import java.util.concurrent.ConcurrentLinkedQueue;

public class GarbageCollector {
    /// 1.4.3.5.2 Очередь сборщика мусора хранит список адресов iLBA, доступных для перезаписи.
    public static ConcurrentLinkedQueue<ILBA> gcQueue = new ConcurrentLinkedQueue<>();
}
