import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.stream.Stream;

public class SaveService {
    /**
     * Loads the diverse data.
     * 
     * @param dem demineur main instance
     */
    public static void Load(Demineur dem) {
        File inputFile = new File("save.sav");
        String saveStr = "";
        if (inputFile.exists()) {
            Path path = Paths.get("save.sav");
            StringBuilder sb = new StringBuilder();
            try (Stream<String> stream = Files.lines(path)) {
                stream.forEach(s -> sb.append(s));

            } catch (IOException e) {
                e.printStackTrace();
            }
            saveStr = sb.toString();
            String decodedstr;
            try {
                decodedstr = AES.decrypt(saveStr, AES.secretKey);
                String[] data = decodedstr.split(",");
                System.out.println(decodedstr);
                dem.playerName = data[0];
                System.out.println(Integer.parseInt(data[2]));
                dem.gameDifficulty = Commons.difficulty.valueOf(data[1]);
                if (dem.gameDifficulty == Commons.difficulty.CUSTOM)
                    Commons.customDiff = new int[] { Integer.parseInt(data[2]), Integer.parseInt(data[3]),
                            Integer.parseInt(data[4]) };
                        Client.serverIp = data[5];
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
    }

    /**
     * Saves the diverse data.
     * 
     * @param dem demineur main instance
     */
    public static void Save(Demineur dem) {
        Thread th = new Thread(() -> {
            File outputFile = new File("save.sav");
            FileWriter out;
            try {
                out = new FileWriter(outputFile);
                String saveStr = dem.playerName + ',' + dem.getChamp().getDifficulte() + ',' + dem.getChamp().getSizeX()
                        + ',' + dem.getChamp().getSizeY() + ',' + dem.getChamp().getNbMines() + ',' + Client.serverIp;
                System.out.println(saveStr);
                out.write(AES.encrypt(saveStr, AES.secretKey));
                out.close();
            } catch (IOException e) {
                e.printStackTrace();
            } catch (Exception e) {
                e.printStackTrace();
            }
        });
        th.start();
    }
}
