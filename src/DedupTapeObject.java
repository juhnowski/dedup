import java.util.concurrent.atomic.AtomicInteger;

public class DedupTapeObject {
    public ILBA iLBA; // - внутренний адрес блока данных на бесконечной ленте (которая является абстракцией диска)
    public AtomicInteger c = new AtomicInteger(0); // - является счётчиком, который показывает, в скольких LBA этот блок дедупликации используется

}
