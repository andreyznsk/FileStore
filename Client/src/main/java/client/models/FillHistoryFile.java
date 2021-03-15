package client.models;

import java.io.BufferedWriter;
import java.io.FileNotFoundException;
import java.io.FileWriter;
import java.io.IOException;

public class FillHistoryFile {
    public static void main(String[] args) throws IOException {
        String fileName = "src/main/java/ru/JavaLevel2/Lesson7/client/chatHisotory/chathistory_Andreyz.txt";
        BufferedWriter writer = null;

        try {
            writer = new BufferedWriter(new FileWriter(fileName, true));
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
            System.err.println("File not found + " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
        writer.write("Я: Это 100 строчка с конца\n");
        for (int i = 0; i < 99; i++) {
            try {
                writer.write("Я: " + i +"\n");
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        writer.close();
    }

}
