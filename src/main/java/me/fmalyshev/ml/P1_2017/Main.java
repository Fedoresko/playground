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

    private static final int START = 2;
    private static final int GENE = 1;

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

        points.sort((o1, o2) -> {
            int res = Integer.compare(o1.coord, o2.coord);
            if (res == 0) {
                res = Integer.compare(o2.flags, o1.flags);
            }
            return res;
        });

        int [] slOvGen = new int[n];
        int [] genOvSl = new int[m];

        Arrays.fill(genOvSl, -1);
        Set<Integer> gens = new HashSet<>();
        Set<Integer> slices = new HashSet<>();

        for (Point p : points) {
            switch (p.flags) {
                case START | GENE:
                    gens.add(p.num);
                    break;

                case START:
                    slices.add(p.num);
                    break;

                case GENE:
                    gens.remove(p.num);
                    for (Iterator<Integer> it = slices.iterator(); it.hasNext(); ) {
                        int nSlice = it.next();
                        if (genOvSl[nSlice] == -1) {
                            genOvSl[nSlice] = p.num;
                            slOvGen[p.num]++;
                        } else if (genOvSl[nSlice] != p.num) {
                            slOvGen[genOvSl[nSlice]]--;
                            it.remove();
                        }
                    }

                    break;

                default:
                    slices.remove(p.num);
                    if (gens.size() == 1) {
                        int nGen = gens.iterator().next();
                        if (genOvSl[p.num] == -1) {
                            genOvSl[p.num] = nGen;
                            slOvGen[nGen]++;
                        } else if (genOvSl[p.num] != nGen) {
                            gens.clear();
                            slOvGen[genOvSl[p.num]]--;
                        }
                    } else if (gens.size() != 0) {
                        if (genOvSl[p.num] != -1) {
                            slOvGen[genOvSl[p.num]]--;
                        }
                        gens.clear();
                    }
            }

        }

        for (int i = 0; i < n; i++) {
            System.out.println(slOvGen[i]);
        }
    }
}