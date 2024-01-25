package org.example;

import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;

public class LuckyDraw {

    public List<String> drawPrize(HashMap<String, String> employeeMap, int count) {
        SecureRandom random = new SecureRandom();
        List<String> selectedElements = new ArrayList<>();
        List<String> list = new ArrayList<>(employeeMap.keySet());
        Collections.sort(list);

        // 生成随机索引，将对应元素加入选中列表
        while (selectedElements.size() < count) {
            int randomIndex = random.nextInt(list.size());
            String randomElement = list.get(randomIndex);

            // 确保不重复选择
            if (!selectedElements.contains(randomElement)) {
                selectedElements.add(randomElement);
            }
        }

        return selectedElements;
    }
    public static void main(String[] args) {
        ExcelReader reader = new ExcelReader();
        HashMap<String, String> employeeMap = reader.read();
        LuckyDraw drawer = new LuckyDraw();
        List<String> winnersList = drawer.drawPrize(employeeMap, 6);
        for (String jobNum: winnersList) {
            System.out.println(String.format("%s, %s", jobNum, employeeMap.get(jobNum)));
        }
    }
}
