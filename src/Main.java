import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.concurrent.ConcurrentSkipListMap;
import java.util.concurrent.atomic.AtomicInteger;

public class Main {


    //PackageT - таблица сопоставления внешних и внутренних адресов
    public static final ConcurrentSkipListMap<LBA,ConcurrentSkipListMap<BlockHash,ILBA>> packageT = new ConcurrentSkipListMap<>();
    public static final  ConcurrentSkipListMap<ILBA,Boolean> garbageMap = new ConcurrentSkipListMap<>();

    Dedup dedup = new Dedup();

    //резервирует пространство на диске (ленте).
    public static ReservedChunk reservedChunk = new ReservedChunk();


    public static void main(String[] args) {
        Main m = new Main();
        try {
            LBA lba = new LBA(0);
            m.dedup(lba, "123 456 789 101 102 103 104 105 106 107 108 109 110");
           // m.read(lba);
        } catch (Exception e){
            e.printStackTrace();
        }
    }

    /*
    1.1 На вход поступает пакет (LBA, data).
    LBA - внешний адрес записи блока (6 байт),
    data - массив с пользовательскими данными
    */
    void dedup(LBA lba, String data) throws Exception {

/// Вынес из цикла------------------------------------------------------------------------------------------------------
        ConcurrentSkipListMap<BlockHash,ILBA> blocksBefore = packageT.get(lba);
        ///  1.3 Делается запрос в PackageT по LBA
        ///   PackageT возвращает либо массив из BN элементов вида (hash, iLBA), либо пустой массив
        if (blocksBefore == null) {
            // создаем
            packageT.put(lba, new ConcurrentSkipListMap<>() );
        } else {
            ///Если получен не пустой массив, тогда он расширяется ещё одним булевым флагом для каждого элемента
            blocksBefore.forEach((bh, ilba)->{
                garbageMap.put(ilba, Boolean.valueOf(false));
            });
        }
        ConcurrentSkipListMap<BlockHash,ILBA> blocks = packageT.get(lba);
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
                dto = ReservedChunk.addBlock(block);
                Debug.printILBAHashBlock(dto, hash, block);

                /// 1.4.3.7 После записи блока на диск вызывается функция добавления информации в DedupT с аргументами (hash, iLBA, 1).
                Dedup.create(hash, dto);

                chunks.add(block);
            } else {
                /// 1.4.4 Если на шаге 1.4.2 вернулось не пустое значение,
                /// то в этом случае блок считается дедуплицированным (уже есть на диске),
                /// и далее возможны два варианта.

                /// 1.4.4.1 Блок мог измениться или остаться прежним, это можно определить, сравнив iLBA из DedupT и iLBA из PackageT.

                if (packageT.get(lba).get(hash).equals(dto)) {
                    /// 1.4.4.2. Блок не изменился. Блок содержит те же самые данные, что были записаны ранее (случай, когда iLBA совпали).
                    ///1.4.4.3 Тогда мы просто поднимаем флаг в массиве (по индексу текущего блока в цикле), который мы расширили в пункте 1.3
                    garbageMap.put(dto, Boolean.valueOf(true));
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
        }

        Debug.printPackageT();
        Debug.printGarbageMap();
        /// 1.6. В случае, если в пункте 1.3 PackageT вернула не пустой массив,
        /// запускается цикл по всем элементам этого массива (расширенной версии с флагом).
        garbageMap.forEach((ilba, flag)->{
            /// 1.6.1. Если флаг элемента массива не выставлен, то это означает,
            /// что этот элемент больше не используется, поэтому необходимо уменьшить значение счётчика в DedupT
            Dedup.dec(ilba);
        });

    /// 1.7. На этом обработка запроса записи завершена
        Debug.printChunks();
    }

    /// 2.	Алгоритм обработки запроса на чтение
    String read(LBA lba){
        StringBuilder sb = new StringBuilder();
        ///2.2. Делается запрос в PackageT по LBA. PackageT возвращает либо массив из BN элементов вида (hash, iLBA), либо пустой массив
        ConcurrentSkipListMap<BlockHash,ILBA> blocks = packageT.get(lba);
        var it = blocks.values().iterator();
        while (it.hasNext()){
            ILBA ilba = it.next();
            Chunk ch = ReservedChunk.chunks.get(ilba.getChunk());
            String s = ch.blocks.get(ilba.getBlock());
            System.out.println(s);
            sb.append(s);
        }
        return sb.toString();
    }
}