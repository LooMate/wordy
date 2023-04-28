package com.miniproject.CounterWordsInFile;

import com.miniproject.CounterWordsInFile.enums.ProgramMode;

import java.nio.file.Path;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
	// write your code here

        long s = System.currentTimeMillis();

        TextService ts = new TextService();//"src/static/file.txt"
        Map<String, Long> stringLongMap = ts.splitTaskAndCountWords(Path.of("C:\\Users\\KaTrin\\OneDrive\\Рабочий стол\\test.pdf"), ProgramMode.DEFAULT);

        System.out.println(stringLongMap);
        System.out.println("Total time: " + (System.currentTimeMillis() - s));

    }
}
