import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.atomic.AtomicInteger;

public class Chunk {
    /// 1.4.3.6.2 Чанк имеет фиксированный размер (RCHS), можно хранить количество блоков (cbs), которые были записаны от начала,

    public List<String> blocks = Collections.synchronizedList(new ArrayList<>());
    public AtomicInteger cbs = new AtomicInteger(-1);
    /// TODO 1.4.3.6.7. Информация об увеличении счётчика распространяется с помощью широкого вещания на другие контроллеры.

    /**
     * Добавляет блок данных в чанк
     * @param block сохраняемые в блок данные
     * @return позицию вставки блока
     */
    public synchronized int append(String block) {
        int blockIndex = cbs.incrementAndGet();

        /// 1.4.3.6.3 тогда проверка сводится к тому, что есть место для записи блока
        if (( blockIndex < Config.RCHS)) {
            blocks.add(block);
            return blockIndex;
        } else {
            return -1;
        }
    }

}
