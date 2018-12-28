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

    private static final int START_GENE = 3;
    private static final int START_READ = 2;
    private static final int END_READ = 1;
    private static final int END_GENE = 0;

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
                points.add(new Point(intervals[j], (j % 2 == 0 ? START_GENE : END_GENE), i));
            }
        }

        for (int i = 0; i < m; i ++) {
            int [] intervals = fastInegers(reader.readLine());

            for (int j =0; j < intervals.length; j++) {
                points.add(new Point(intervals[j], (j % 2 == 0 ? START_READ : END_READ), i));
            }
        }

        points.sort((o1, o2) -> {
            int res = Integer.compare(o1.coord, o2.coord);
            if (res == 0) {
                res = Integer.compare(o2.flags, o1.flags);
            }
            return res;
        });

        int [] numberReadsOverGen = new int[n+1];
        int [] genInRead = new int[m];
        int [] revertRead = new int[m];

        Arrays.fill(genInRead, -1);
        Arrays.fill(revertRead, 1);
        Set<Integer> gens = new HashSet<>();
        Set<Integer> reads = new HashSet<>();

        for (Point p : points) {
            switch (p.flags) {
                case START_GENE:
                    gens.add(p.num);
                    break;

                case START_READ:
                    reads.add(p.num);
                    break;

                case END_GENE:
                    gens.remove(p.num);
                    for (Iterator<Integer> it = reads.iterator(); it.hasNext(); ) {
                        int nRead = it.next();
                        if (genInRead[nRead] == -1) {
                            genInRead[nRead] = p.num;
                            numberReadsOverGen[p.num]++;
                        } else if (genInRead[nRead] != p.num) {
                            numberReadsOverGen[genInRead[nRead]] -= revertRead[nRead];
                            revertRead[nRead] = 0;
                            it.remove();
                        }
                    }

                    break;

                default:
                    reads.remove(p.num);
                    if (gens.size() == 1) {
                        int nGen = gens.iterator().next();
                        if (genInRead[p.num] == -1) {
                            genInRead[p.num] = nGen;
                            numberReadsOverGen[nGen]++;
                        } else if (genInRead[p.num] != nGen) {
                            numberReadsOverGen[genInRead[p.num]] -= revertRead[p.num];
                            revertRead[p.num] = 0;
                        }
                    } else {

                        if (gens.size() != 0) {
                            if (genInRead[p.num] != -1) {
                                numberReadsOverGen[genInRead[p.num]] -= revertRead[p.num];
                                revertRead[p.num] = 0;
                            } else {
                                genInRead[p.num] = n;
                            }
                        }

                    }
            }

        }

        for (int i = 0; i < n; i++) {
            System.out.println(numberReadsOverGen[i]);
        }
    }
}