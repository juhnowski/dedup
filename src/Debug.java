import java.util.List;

public class Debug {
    private static boolean flag = true;

    public static void printILBAHashBlock(ILBA ilba,BlockHash hash, String block) {
        if (flag) {
            System.out.println(ilba+" \t" + hash + " \t '"+block+"'");
        }
    }

    public static void printPackageT() {
        if (flag) {
            System.out.println("\n-------------- PackageT --------------");
            
            var it = Controller.packageT.navigableKeySet().iterator();
            while (it.hasNext()) {
                LBA lba = it.next();
                System.out.println(lba);
                var hashList = Controller.packageT.get(lba);
                var it1=hashList.navigableKeySet().iterator();
                while (it1.hasNext()) {
                    BlockHash bh = it1.next();
                    ILBA ilba = hashList.get(bh);
                    System.out.println(" \t" + bh +" \t" + ilba);
                }
            }
        }
    }

    public static void printGarbageMap() {
        if (flag) {
            System.out.println("\n-------------- GarbageMap --------------");
            if ( Controller.garbageMap.isEmpty()) {
                System.out.println("Empty");
                return;
            }

            var it = Controller.garbageMap.navigableKeySet().iterator();
            while (it.hasNext()) {
                ILBA ilba = it.next();
                Boolean fl = Controller.garbageMap.get(ilba);
                System.out.println(ilba + "\t"+fl);
            }
        }
    }

    public static void printChunks() {
        if (flag) {
            System.out.println("\n-------------- ReservedChunk.chunks --------------");
           
            int chunksSize = ReservedChunk.chunks.size();
            if (chunksSize == 0) {
                System.out.println("Empty");
            }

            for (int i=0; i < chunksSize; i++) {
                System.out.println("Chunk="+i);
                Chunk ch = ReservedChunk.chunks.get(i);
                int blocksSize = ch.blocks.size();

                if (blocksSize==0) {
                    System.out.println("\t Empty");
                } else {
                    for (int j=0; j < blocksSize; j++){
                        String data = ch.blocks.get(j);
                        if (data.length()==0) {
                            System.out.println("\t Block=" + j + "\t Empty"); 
                        } else {
                            System.out.println("\t Block=" + j + "\t " + data); 
                        }
                        
                    }
                }
            }
        }
    }
}
