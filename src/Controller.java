import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Controller {


    //PackageT - таблица сопоставления внешних и внутренних адресов
    public static final ConcurrentSkipListMap<LBA,ConcurrentSkipListMap<BlockHash,ILBA>> packageT = new ConcurrentSkipListMap<>();
    public static final  ConcurrentSkipListMap<ILBA,Boolean> garbageMap = new ConcurrentSkipListMap<>();

  

    //резервирует пространство на диске (ленте).
    public static ReservedChunk reservedChunk = new ReservedChunk();

    public static void main(String[] args) {

        try {
            LBA lba0 = new LBA(0);
            LBA lba1 = new LBA(1);
            LBA lba2 = new LBA(2);

            Dedup dedup0 = new Dedup(lba0, "123 456 789 101 102 103 104 105 106 107 108 109 110");
            Dedup dedup1 = new Dedup(lba1, "123 456 789 101 102 103 104 105 106 107 108 109 110");
            Dedup dedup2 = new Dedup(lba2, "123 456 789 101 102 103 104 105 106 107 108 109 110");

            Thread.Builder builderGC = Thread.ofVirtual().name("GC");
            GarbageCollector gc = new GarbageCollector();
            Thread tGC = builderGC.start(gc);
            System.out.println("Thread gc name: '" + tGC.getName() + "' started");

            Thread.Builder builderDedup = Thread.ofVirtual().name("Dedup");
            Thread t0 = builderDedup.start(dedup0);
            Thread t1 = builderDedup.start(dedup1);
            Thread t2 = builderDedup.start(dedup2);
            
            t0.join();
            t1.join();
            t2.join();

            System.out.println("--------------- read ---------------");

            Reader reader0 = new Reader(lba0);
            Reader reader1 = new Reader(lba1);
            Reader reader2 = new Reader(lba2);

            Thread.Builder readerDedup = Thread.ofVirtual().name("Reader");
            Thread r0 = readerDedup.start(reader0);
            Thread r1 = readerDedup.start(reader1);
            Thread r2 = readerDedup.start(reader2);

            r0.join();
            r1.join();
            r2.join();

        } catch (Exception e){
            e.printStackTrace();
        }
    }

}