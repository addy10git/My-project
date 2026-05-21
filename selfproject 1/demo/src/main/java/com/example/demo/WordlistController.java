package com.example.demo;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.servlet.mvc.method.annotation.StreamingResponseBody;

import java.io.*;
import java.util.*;

@Controller
public class WordlistController {

    @GetMapping("/")
    public String index() {
        return "index";
    }

    @PostMapping("/generate")
    public ResponseEntity<StreamingResponseBody> generate(
            @RequestParam String words,
            @RequestParam String numbers,
            @RequestParam String symbols,
            @RequestParam(defaultValue = "5000000") long target,
            @RequestParam String fileName) {

        // Prepare the filename
        String finalFileName = fileName.isEmpty() ? "wordlist.txt" : fileName + ".txt";

        StreamingResponseBody responseBody = outputStream -> {
            BufferedWriter writer = new BufferedWriter(new OutputStreamWriter(outputStream));

            // Logic from your original code
            List<String> rawWords = Arrays.asList(words.split(",\\s*"));
            List<String> numList = parseNumbers(numbers);
            List<String> symList = Arrays.asList(symbols.split(",\\s*"));

            List<String> processedWords = new ArrayList<>();
            for (String w : rawWords) {
                processedWords.add(w.toLowerCase());
                processedWords.add(w.toUpperCase());
                if (w.length() > 0) {
                    processedWords.add(w.substring(0, 1).toUpperCase() + w.substring(1).toLowerCase());
                }
            }

            long count = 0;
            while (count < target) {
                for (String w : processedWords) {
                    for (String n : numList) {
                        for (String s : symList) {
                            if (count >= target) break;
                            writer.write(w + n + s + "\n");
                            count++;
                            if (count >= target) break;
                            writer.write(w + s + n + "\n");
                            count++;
                        }
                        if (count >= target) break;
                    }
                    if (count >= target) break;
                }
                if (processedWords.isEmpty()) break;
            }
            writer.flush();
            writer.close();
        };

        return ResponseEntity.ok()
                .header(HttpHeaders.CONTENT_DISPOSITION, "attachment; filename=" + finalFileName)
                .contentType(MediaType.TEXT_PLAIN)
                .body(responseBody);
    }

    private List<String> parseNumbers(String input) {
        List<String> list = new ArrayList<>();
        String[] parts = input.split(",\\s*");
        for (String part : parts) {
            if (part.contains("-")) {
                try {
                    String[] range = part.split("-");
                    int start = Integer.parseInt(range[0].trim());
                    int end = Integer.parseInt(range[1].trim());
                    for (int i = start; i <= end; i++) list.add(String.valueOf(i));
                } catch (Exception e) {}
            } else {
                list.add(part.trim());
            }
        }
        return list;
    }
}