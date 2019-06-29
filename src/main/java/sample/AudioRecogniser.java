package sample;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class AudioRecogniser {
    private String file;
    private static String AUDIO_PATH = "/home/adrian/iatros_matriculas/audios/";

    public AudioRecogniser(String file) {
        this.file = file;

    }

    public String recogniseAudio() {

        // Convert 3gp to mp3
        Thread t1 = new Thread(new Speech());
        t1.start();
        try {
            System.out.println("Convert to mp3");

            Process convtomp3 = Runtime.getRuntime().exec("ffmpeg -y -i " + AUDIO_PATH + file + ".3gp -vn " + AUDIO_PATH + file + ".mp3");
            convtomp3.waitFor();
        } catch (Exception e) {
            System.exit(-1);
        }
        // Convert mp3 to raw
        try {
            System.out.println("Convert to raw");
            Process convtoraw = Runtime.getRuntime().exec("sox " + AUDIO_PATH + file + ".mp3 -t raw -r 16000 -b 16 -e signed-integer " + AUDIO_PATH  + "matricula.raw");
            convtoraw.waitFor();
        } catch (Exception e) {
            System.exit(-1);
        }

        // Convert raw to CC and process de audio


        String[] cmd = {"sh", "/home/adrian/iatros_matriculas/iatros_launche-modr.sh"};
        String result="";
        try {
            System.out.println("Processing audio");

            Process p = Runtime.getRuntime().exec(cmd);
            p.waitFor();
            BufferedReader errorReader = new BufferedReader(new InputStreamReader(p.getErrorStream()));

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    p.getInputStream()));
            String line = "";
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
                result+=line;
            }
            line = "";
            while ((line = errorReader.readLine()) != null) {
                System.out.println(line);


            }
        } catch (Exception e) {
            System.out.println(e.toString());
        }
        return result.trim();
    }
}
