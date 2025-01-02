import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Dedup implements Runnable{
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

    private LBA lba;
    private String data;

    /*
    1.1 На вход поступает пакет (LBA, data).
    LBA - внешний адрес записи блока (6 байт),
    data - массив с пользовательскими данными
    */
    public Dedup(LBA lba, String data){
        this.lba = lba;
        this.data = data;
    }


    @Override
    public void run() {

/// Вынес из цикла------------------------------------------------------------------------------------------------------
        ConcurrentSkipListMap<BlockHash,ILBA> blocksBefore = Controller.packageT.get(lba);
        ///  1.3 Делается запрос в PackageT по LBA
        ///   PackageT возвращает либо массив из BN элементов вида (hash, iLBA), либо пустой массив
        if (blocksBefore == null) {
            // создаем
            Controller.packageT.put(lba, new ConcurrentSkipListMap<>() );
        } else {
            ///Если получен не пустой массив, тогда он расширяется ещё одним булевым флагом для каждого элемента
            blocksBefore.forEach((bh, ilba)->{
                Controller.garbageMap.put(ilba, Boolean.valueOf(false));
            });
        }
        ConcurrentSkipListMap<BlockHash,ILBA> blocks = Controller.packageT.get(lba);
///---------------------------------------------------------------------------------------------------------------------


        List<String> chunks = new ArrayList<>();
        for (int i = 0; i < data.length(); i += Config.DBS) {
            //1.2 Массив data разбивается на равные блоки
            String block = data.substring(i, Math.min(data.length(), i + Config.DBS));

            ///1.4.	Для каждого подблока (размера DBS) исходных данных выполняются следующие шаги. Всего итераций BN

            /// 1.4.1. Вычисляется хэш этого блока (hash)
            BlockHash hash = new BlockHash(block);

            /// 1.4.2.1	 Делается запрос в DedupT по значению hash, где DedupT - таблица дедупликации
            ILBA dto = Dedup.getILBAByHash(hash);
            /// 1.4.2.2 DedupT возвращает либо информацию о существующем блоке (iLBA, c) с таким же hash, либо ничего не возвращает.
            /// Значение c является счётчиком, который показывает, в скольких LBA этот блок дедупликации используется
            if (dto == null) {
                /// 1.4.3. Если на шаге 1.4.2 вернулось пустое значение, то в этом случае блок считается новым и
                /// производится попытка добавления этого блока

                /// 1.4.3.1 Вызывается функция добавления блока у объекта Reserved Chunk.
                try {
                    dto = ReservedChunk.addBlock(block);
                } catch (Exception e) {
                    e.printStackTrace();
                    return;
                }
                
                Debug.printILBAHashBlock(dto, hash, block);

                /// 1.4.3.7 После записи блока на диск вызывается функция добавления информации в DedupT с аргументами (hash, iLBA, 1).
                Dedup.create(hash, dto);

                chunks.add(block);
            } else {
                /// 1.4.4 Если на шаге 1.4.2 вернулось не пустое значение,
                /// то в этом случае блок считается дедуплицированным (уже есть на диске),
                /// и далее возможны два варианта.

                /// 1.4.4.1 Блок мог измениться или остаться прежним, это можно определить, сравнив iLBA из DedupT и iLBA из PackageT.
                var tmp = Controller.packageT.get(lba).get(hash); 
                if ( (tmp!=null) && (tmp.equals(dto))) {
                    /// 1.4.4.2. Блок не изменился. Блок содержит те же самые данные, что были записаны ранее (случай, когда iLBA совпали).
                    ///1.4.4.3 Тогда мы просто поднимаем флаг в массиве (по индексу текущего блока в цикле), который мы расширили в пункте 1.3
                    Controller.garbageMap.put(dto, Boolean.valueOf(true));
                } else {
                    /// 4. Блок обновился. Блок содержит новые данные, но уже дедуплицированные (случай, когда iLBA не совпали).
                    /// Это по определению является новым использования блока дедупликации по конкретному LBA,
                    /// поэтому мы должны будем увеличить значение счётчика (c) на 1 для блока с таким hash-значением.
                    /// Вызывается функция увеличения счётчика по hash-значению у DedupT.
                    Dedup.inc(dto);
                }
                blocks.put(hash, dto);
            }

            /// 1.5. Блок записывается в PackageT
            blocks.put(hash,dto);
            lba.list.add(dto);
        }

        Debug.printPackageT();
        Debug.printGarbageMap();
        /// 1.6. В случае, если в пункте 1.3 PackageT вернула не пустой массив,
        /// запускается цикл по всем элементам этого массива (расширенной версии с флагом).
        Controller.garbageMap.forEach((ilba, flag)->{
            /// 1.6.1. Если флаг элемента массива не выставлен, то это означает,
            /// что этот элемент больше не используется, поэтому необходимо уменьшить значение счётчика в DedupT
            Dedup.dec(ilba);
        });

    /// 1.7. На этом обработка запроса записи завершена
        Debug.printChunks();

     //   throw new UnsupportedOperationException("Unimplemented method 'run'");
    }
}
