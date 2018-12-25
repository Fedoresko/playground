package me.fmalyshev.ml;

import edu.stanford.nlp.parser.common.ParserQuery;
import edu.stanford.nlp.parser.lexparser.LexicalizedParser;

import java.io.*;
import java.util.Scanner;
import java.util.concurrent.*;

public class ParseQouraText {

    public static void main(String [] args) throws Exception {
        ExecutorService executor = Executors.newFixedThreadPool(7);
        Scanner scanner = new Scanner(new File("D:\\Program Files\\Python\\Python37\\datasets\\quora\\train.csv"));
        final LexicalizedParser lp = LexicalizedParser.loadModel("D:\\Program Files\\Python\\Python37\\stanford-parser-full-2018-10-17\\englishPCFG.ser.gz");
        File out = new File("D:\\Program Files\\Python\\Python37\\datasets\\quora\\processed.csv");
        out.createNewFile();
        final PrintWriter pw = new PrintWriter(new FileOutputStream(out));
        while (scanner.hasNext()) {
            final String line = scanner.nextLine();
            executor.execute(new Runnable() {
                @Override
                public void run() {
                    ParserQuery pq = lp.parserQuery();
                    String[] splitted = line.split(",(?=([^\"]*\"[^\"]*\")*[^\"]*$)");
                    StringBuilder sb = new StringBuilder();
                    sb.append(splitted[0] + ",\"");
                    String sentence = splitted[1];
                    if (sentence.charAt(0) == '"') {
                        sentence = sentence.substring(1, sentence.length()-1);
                        sentence = sentence.replaceAll("\"\"", "\"");
                    }
                    sentence = sentence.replaceAll("\".+\"", " word ");
                    pq.parse(lp.tokenize(sentence));
                    sb.append(pq.getBestParse().toString());
                    sb.append("\",");
                    sb.append(splitted[2]);
                    pw.println(sb.toString());
                }
            });
        }
        executor.shutdown();
        executor.awaitTermination(10, TimeUnit.MINUTES);
    }
}
