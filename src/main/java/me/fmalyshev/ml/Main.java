package me.fmalyshev.ml;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.util.*;

public class Main {

    static class Data {
        Integer[] to;
        int nins;
    }
    static  Map<Integer, List<Data>> datamap = new HashMap<>(10000);
    static  Set<Integer> pres = new HashSet<>(10000);

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
            return new String[] { string };

        // Add remaining segment
        list.add(string.substring(off, string.length()));

        // Construct result
        int resultSize = list.size();
        while (resultSize > 0 && list.get(resultSize - 1).length() == 0)
            resultSize--;
        String[] result = new String[resultSize];
        return list.subList(0, resultSize).toArray(result);
    }

    public static void main(String[] args) throws Exception {
        // write your code here
        BufferedReader br = new BufferedReader(new InputStreamReader(System.in));

        Deque<Integer> cdeq = new LinkedList<>();
        String s = br.readLine();
        for ( String sn : fastSplit(s, ' ') ) {
            Integer n = Integer.valueOf(sn);
            pres.add(n);
            cdeq.push(n);
        }

        s = br.readLine();
        while (s != null) {
            int delim = s.indexOf("->");

            Data data = new Data();


            int [] from = Arrays.asList( fastSplit(s.substring(0, delim), '+')  ).stream().mapToInt(Integer::valueOf).toArray();
            data.to = Arrays.asList( fastSplit(s.substring(delim+2, s.length()), '+') ).stream().map(Integer::valueOf).toArray(Integer[]::new);
            for (int k : from) {
                if (!datamap.containsKey(k))
                    datamap.put(k, new ArrayList<>());
                List<Data> l = datamap.get(k);
                l.add(data);
            }
            data.nins = from.length;

            s = br.readLine();
        }

        while (!cdeq.isEmpty()) {
            Integer k = cdeq.pollFirst();
            if (datamap.containsKey(k))
                for (Iterator<Data> it = datamap.get(k).iterator(); it.hasNext(); ) {
                    Data data = it.next();
                    if (data.nins == 1)
                        for (Integer to : data.to) {
                            if (!pres.contains(to)) {
                                pres.add(to);
                                cdeq.push(to);
                            }
                        }

                    data.nins--;
                }
        }

        System.out.println(String.join(" ", pres.stream().map(String::valueOf).toArray(String []::new)) );
    }
}