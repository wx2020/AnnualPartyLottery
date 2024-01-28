package org.example;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;

import java.io.*;
import java.nio.charset.StandardCharsets;
import java.util.Arrays;
import java.util.List;

public class ListToText {
    public static void main(String[] args) {
        List<String> dataList = Arrays.asList("One", "Two", "Three", "Four", "Five");
        String currentPath = System.getProperty("user.dir");
        String filePath = currentPath + File.separator + "output.txt";

        exportListToFile("test", dataList);

        System.out.println("List exported to: " + filePath);
    }

    public static void exportListToFile(String dataHeader, List<String> dataList) {
        String currentPath = System.getProperty("user.dir");
        String filePath = currentPath + File.separator + "output.txt";
        try (BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(new FileOutputStream(filePath, true), StandardCharsets.UTF_8))) {
            if (new File(filePath).length() > 0) {
                writer.newLine(); // 写入换行符
            }
            writer.write(dataHeader);
            writer.newLine(); // 换行
            for (String data : dataList) {
                writer.write(data);
                writer.newLine(); // 换行
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}
