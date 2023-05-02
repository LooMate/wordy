package com.miniproject.CounterWordsInFile;

import com.miniproject.CounterWordsInFile.enums.ProgramMode;

import java.nio.file.Path;
import java.util.Map;

public class Main {

    public static void main(String[] args) {
	// write your code here

        long s = System.currentTimeMillis();

        TextService ts = new TextService();
        Map<String, Long> stringLongMap = ts.splitTaskAndCountWords(Path.of("src/main/resources/static/file1.txt"), ProgramMode.DEFAULT);

        System.out.println(stringLongMap);
        System.out.println("Total time: " + (System.currentTimeMillis() - s));

    }
}
