import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Chunk {
    /// 1.4.3.6.2 Чанк имеет фиксированный размер (RCHS), можно хранить количество блоков (cbs), которые были записаны от начала,
    public static int RCHS = 20;
    public List<String> blocks = Collections.synchronizedList(new ArrayList<>());
    public AtomicInteger cbs = new AtomicInteger(0);
    /// TODO 1.4.3.6.7. Информация об увеличении счётчика распространяется с помощью широкого вещания на другие контроллеры.

    public Chunk(){
        for (int i=0; i<RCHS; i++){
            blocks.add("");
        }
    }
}
