package sample;

import java.io.BufferedReader;
import java.io.InputStreamReader;

public class Speech implements Runnable {
    public void run(){
        try {
            String[] cmd = {"sh", "/home/adrian/iatros_matriculas/speech_background.sh"};
            Process iatros = Runtime.getRuntime().exec(cmd);

            BufferedReader errorReader = new BufferedReader(new InputStreamReader(iatros.getErrorStream()));

            BufferedReader reader = new BufferedReader(new InputStreamReader(
                    iatros.getInputStream()));
            String line = "";
            while ((line = reader.readLine()) != null) {
                System.out.println(line);
            }
            line = "";
            while ((line = errorReader.readLine()) != null) {
                System.out.println(line);


            }
        } catch (Exception e) {
            System.exit(-1);
        }
        System.out.println("Speech is running");
    }
}
