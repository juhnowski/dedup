public class Reader implements Runnable{

    private LBA lba;

    public Reader(LBA lba) {
        this.lba = lba;
    }

    @Override
    public void run() {
        StringBuilder sb = new StringBuilder();
        var it = lba.list.iterator();
        while (it.hasNext()) {
            ILBA ilba = it.next();
            Chunk ch = ReservedChunk.chunks.get(ilba.getChunk());
            String s = ch.blocks.get(ilba.getBlock());
            sb.append(s);
        }
        System.out.println(sb.toString());

        // ///2.2. Делается запрос в PackageT по LBA. PackageT возвращает либо массив из BN элементов вида (hash, iLBA), либо пустой массив
        // ConcurrentSkipListMap<BlockHash,ILBA> blocks = packageT.get(lba);
        // var it = blocks.navigableKeySet().iterator();
        // while (it.hasNext()){
        //     BlockHash hash = it.next();
        //     ILBA ilba = blocks.get(hash);
        //     Chunk ch = ReservedChunk.chunks.get(ilba.getChunk());
        //     String s = ch.blocks.get(ilba.getBlock());
        //     System.out.println(s);
        //     sb.append(s);
        // }
        // return sb.toString();
    }
    
}
