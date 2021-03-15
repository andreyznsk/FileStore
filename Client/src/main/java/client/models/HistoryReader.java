package client.models;

import javafx.beans.binding.StringBinding;

import java.io.BufferedReader;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayDeque;
import java.util.Queue;
import java.util.Stack;

public class HistoryReader {
    public static void main(String[] args) {
        String fileName = "src/main/java/ru/JavaLevel2/Lesson7/client/chatHisotory/chathistory_Andreyz.txt";

        BufferedReader reader = null;
        try {
            reader = new BufferedReader(new FileReader(fileName));
        } catch (FileNotFoundException fileNotFoundException) {
            fileNotFoundException.printStackTrace();
        }
        ArrayDeque<String> stringStack = new ArrayDeque<String>();
        try{
            String strTemp;
            int i=0;
            while ((strTemp = reader.readLine()) != null) {
                if (i>10) stringStack.removeFirst();
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
        System.out.println(str2.toString());
    }
}
