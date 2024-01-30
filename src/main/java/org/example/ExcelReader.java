package org.example;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.HashMap;

public class ExcelReader {

    String userDir = System.getProperty("user.dir");

    public static String specFileName = "name_list_spec.xlsx";
    public static String normFileName = "name_list_norm.xlsx";
    public HashMap<String, String> read(String fileName) {
        try {
            String filePath = userDir + File.separator + fileName;

            // 读取 Excel 文件
            FileInputStream fileInputStream = new FileInputStream(filePath);

            // 创建 Workbook 对象，支持 .xlsx 格式
            Workbook workbook = new XSSFWorkbook(fileInputStream);

            // 获取第一个 Sheet
            Sheet sheet = workbook.getSheetAt(0);
            int rowCount = sheet.getPhysicalNumberOfRows();
            HashMap<String, String> employeeMap = new HashMap<>();
            for (int i = 1; i < rowCount; i++) {
                String jobNum = sheet.getRow(i).getCell(0).getStringCellValue();
                String name = sheet.getRow(i).getCell(1).getStringCellValue();
                employeeMap.put(jobNum, name);
            }
            // 关闭资源
            fileInputStream.close();
            workbook.close();
            return employeeMap;
        } catch (IOException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }
    public static void main(String[] args) {
        ExcelReader excelReader = new ExcelReader();
        HashMap<String, String> specEmployeeMap = excelReader.read(specFileName);
        System.out.println("spec employee Map = " + specEmployeeMap);
        HashMap<String, String> normEmployeeMap = excelReader.read(normFileName);
        System.out.println("norm employee Map = " + normEmployeeMap);
    }
}