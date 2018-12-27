package me.fmalyshev.ml.P1_2017;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

public class Main {

    private static String[] fastSplit(String string, char delimiter) {
        int off = 0;
        int next = 0;
        ArrayList<String> list = new ArrayList<>();
        while ((next = string.indexOf(delimiter, off)) != -1) {
            list.add(string.substring(off, next));
            off = next + 1;
        }
        // If no match was found, return this
        if (off == 0)
            return new String[]{string};

        // Add remaining segment
        list.add(string.substring(off, string.length()));

        // Construct result
        int resultSize = list.size();
        while (resultSize > 0 && list.get(resultSize - 1).length() == 0)
            resultSize--;
        String[] result = new String[resultSize];
        return list.subList(0, resultSize).toArray(result);
    }

    private static int [] fastInegers(String string) {
        String [] read = fastSplit(string, ' ');
        int [] res = new int[read.length];
        for (int i = 0; i < read.length; i++) {
            res[i] = Integer.valueOf(read[i]);
        }
        return res;
    }

    private static final int START = 1;
    private static final int GENE = 2;

    private static class Point {
        public Point(int coord, int flags, int num) {
            this.coord = coord;
            this.flags = flags;
            this.num = num;
        }
        int coord;
        int flags;
        int num;
    }

    public static void main(String[] args) throws Exception {
        BufferedReader reader = new BufferedReader(new InputStreamReader(System.in));


        int [] nm = fastInegers(reader.readLine());
        int n = nm[0]; int m = nm[1];

        ArrayList<Point> points = new ArrayList<>();

        for (int i = 0; i < n; i ++) {
            int [] intervals = fastInegers(reader.readLine());

            for (int j =0; j < intervals.length; j++) {
                points.add(new Point(intervals[j], (j % 2 == 0 ? START : 0) | GENE, i));
            }
        }

        for (int i = 0; i < m; i ++) {
            int [] intervals = fastInegers(reader.readLine());

            for (int j =0; j < intervals.length; j++) {
                points.add(new Point(intervals[j], (j % 2 == 0 ? START : 0), i));
            }
        }

        points.sort((o1, o2) ->
                Integer.compare(o1.coord, o2.coord));


    }

}