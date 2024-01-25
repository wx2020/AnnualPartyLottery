package org.example;
import org.apache.poi.ss.usermodel.*;
import org.apache.poi.xssf.usermodel.XSSFWorkbook;


import java.io.IOException;
import java.io.InputStream;
import java.util.HashMap;

public class ExcelReader {

    public static String specPath = "/excel/name_list_spec.xlsx";
    public static String normPath = "/excel/name_list_norm.xlsx";
    public HashMap<String, String> read(String path) {
        try {
            // 读取 Excel 文件
            InputStream inputStream = ExcelReader.class.getResourceAsStream(path);
            // 创建 Workbook 对象，支持 .xlsx 格式
            Workbook workbook = new XSSFWorkbook(inputStream);

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
            inputStream.close();
            workbook.close();
            return employeeMap;
        } catch (IOException e) {
            e.printStackTrace();
            return new HashMap<>();
        }
    }
    public static void main(String[] args) {
        ExcelReader excelReader = new ExcelReader();
        HashMap<String, String> specEmployeeMap = excelReader.read(specPath);
        System.out.println("spec employee Map = " + specEmployeeMap);
        HashMap<String, String> normEmployeeMap = excelReader.read(normPath);
        System.out.println("norm employee Map = " + normEmployeeMap);
    }
}