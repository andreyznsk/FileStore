package client.models;

import java.io.*;
import java.util.*;

public class ChatHistoryBuilder {
    private BufferedReader reader;
    private BufferedWriter writer;

    public static String getFileName (String nickName){
        return "src/main/java/ru/JavaLevel2/Lesson7/client/chatHisotory/chathistory_" + nickName + ".txt";
    }

    public ChatHistoryBuilder(String nickName) {

        String fileName = getFileName(nickName);

        try {
            writer = new BufferedWriter(new FileWriter(fileName, true));
        } catch (FileNotFoundException e) {
            //e.printStackTrace();
            System.err.println("File not found + " + e.getMessage());
        } catch (IOException e) {
            e.printStackTrace();
        }
        try {
            reader = new BufferedReader(new FileReader(fileName));
        } catch (FileNotFoundException fileNotFoundException) {
            fileNotFoundException.printStackTrace();


        }
    }


    public void closeChatHistoryFile(){
        try {
            reader.close();
            writer.close();
        } catch (IOException e) {
            System.err.println(e.getMessage());
            e.printStackTrace();
        }
    }

    public String readChatHistory() {
        ArrayDeque<String> stringStack = new ArrayDeque<>();
        try{
            String strTemp;
            int i=0;
            while ((strTemp = reader.readLine()) != null) {
                if (i>99) stringStack.removeFirst();
                stringStack.add(strTemp+"\n");
                i++;
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
        StringBuilder str2 = new StringBuilder();
        while (stringStack.peekFirst()!=null) {
            //System.out.println(stringStack.pop());
            str2.append(stringStack.pollFirst());
        }

        return str2.toString();
    }



    public void writeChatHistory(String message){
        try {
            System.out.println(message);
            writer.write(message);
            writer.write(System.lineSeparator());
            writer.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

}
