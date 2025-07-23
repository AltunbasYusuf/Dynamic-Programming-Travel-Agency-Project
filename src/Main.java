import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.ArrayList;
import java.util.List;
import java.util.Arrays;
import java.util.Scanner;


public class Main {
    public static void main(String[] args) {
        double startTime = System.currentTimeMillis();
        Scanner scanner = new Scanner(System.in);
        System.out.print("Please enter the total number of landmarks (including Hotel): ");
        int totalLandmarkLimit = scanner.nextInt();
        totalLandmarkLimit--;
        Map<String, Double> visitor_load = new HashMap<>();
        Map<String, Double> personal_interest = new HashMap<>();
        Map<String, Double> landmark_map = new HashMap<>();
        Graph map=new Graph();
        //Visitor load okuma kodları
        try {
            BufferedReader br = new BufferedReader(new FileReader("data/visitor_load.txt"));
            String line;
            br.readLine();
            while ((line = br.readLine()) != null) {
                String[] parts = line.split("\t");
                String landmark_name = parts[0].trim();
                double load = Double.parseDouble(parts[1].trim());
                visitor_load.put(landmark_name, load);
            }
            br.close();
        } catch (IOException e) {
            System.out.println("Dosya okuma hatası: " + e.getMessage());
        }

        //Personal interest okuma kodları
        try{
            BufferedReader br = new BufferedReader(new FileReader("data/personal_interest.txt"));
            String line;
            br.readLine();
            while((line=br.readLine())!=null) {
                String[] parts = line.split("\t");
                String landmark_name = parts[0].trim();
                double personalInterest = Double.parseDouble(parts[1].trim());
                personal_interest.put(landmark_name,personalInterest);
            }
            br.close();
        }
        catch (IOException e) {
            System.out.println("Dosya okuma hatası: " + e.getMessage());
        }

        //Landmark map data okuma kodları
        try{
            BufferedReader br=new BufferedReader(new FileReader("data/landmark_map_data.txt"));
            String line;
            br.readLine();
            while((line=br.readLine())!=null){
                String parts[] = line.split("\t");
                String from=parts[0].trim();
                String to= parts[1].trim();
                double baseScore=Double.parseDouble(parts[2]);
                double travelTime=Double.parseDouble(parts[3]);
                landmark_map.put(from+to,baseScore); //landmarklar arası bütün yolların base scorelarını tutmak için key değerini from+to aldım
                map.addEdge(from,to,travelTime);
            }
        }
        catch (IOException e) {
            System.out.println("Dosya okuma hatası: " + e.getMessage());
        }
        System.out.println("Three input files are read.");
        System.out.println();
        System.out.println("The tour planning are now processing...");
        System.out.println();
        //Landmarkların isimlerini personal interest listinden çekiyoruz
        List<String> landmarkNames = new ArrayList<>(personal_interest.keySet());
        Map<String, Integer> indexMap = new HashMap<>();
        Map<Integer, String> reverseIndexMap = new HashMap<>();
        for (int i = 0; i < landmarkNames.size(); i++) {
            indexMap.put(landmarkNames.get(i), i);
            reverseIndexMap.put(i, landmarkNames.get(i));
        }

        int N = indexMap.size();
        double[][] dp = new double[1 << N][N];
        int[][] backtrack = new int[1 << N][N];
        for (double[] row : dp) Arrays.fill(row, -1);

        int start = indexMap.get("Hotel");

        double maxScore = dfs(1 << start, start, 1, totalLandmarkLimit, dp, backtrack, map, landmark_map, personal_interest, visitor_load, indexMap);


        List<String> route = reconstructPath(backtrack, start, dp, reverseIndexMap);
        System.out.println("The visited landmarks:");
        int pathCounter=1;
        for (String name : route) {
            System.out.println(pathCounter+". " + name);
            pathCounter++;
        }
        System.out.println();
        System.out.println("Total attractiveness score: " + maxScore);
        System.out.println();
        double totalTravelTime = calculateTotalTravelTime(route, map);
        System.out.println("Total travel time: " + totalTravelTime + " min.");
        System.out.println();

        //Program çalışma zamanı kodu
        double endTime = System.currentTimeMillis();
        double duration = endTime - startTime;
        double seconds = (duration / 1000) % 60;
        System.out.print("Program took "  + seconds + " seconds "+ "("+ seconds*1000+ " ms) to run.");

    }


//DFS ile best path bulma fonksiyonu
public static double dfs(int mask, int current, int visitedCount, int totalLimit,
                         double[][] dp, int[][] backtrack, Graph map,
                         Map<String, Double> landmark_map,
                         Map<String, Double> personal_interest,
                         Map<String, Double> visitor_load,
                         Map<String, Integer> indexMap) {

    // Memoization kodu
    //Eğer programımız bu mask ve current için daha önce hesaplama yaptıysa bir daha yapmamasını sağlıyor
    if (dp[mask][current] != -1) return dp[mask][current];

    String currentName = getNameByIndex(indexMap, current);
    double maxScore = 0;
    int bestNext = -1;

    // Eğer kullanıcı maksimum gezilecek yere ulaştıysa hotele geri dönme kodu
    if (visitedCount == totalLimit) {
        List<Edge> returnEdges = map.getNeighbors(currentName);
        for (int i = 0; i < returnEdges.size(); i++) {
            Edge edge = returnEdges.get(i);
            if (edge.to.equals("Hotel")) {
                String key = currentName + "Hotel";
                double returnScore = adjustedAttractivenessScore(
                        landmark_map.getOrDefault(key, 0.0),
                        personal_interest.getOrDefault("Hotel", 1.0),
                        visitor_load.getOrDefault("Hotel", 0.0),
                        edge.travelTime
                );
                return dp[mask][current] = returnScore;
            }
        }
        return dp[mask][current] = 0;
    }

    //Bulunduğumuz landmarkın komşularını liste alıp bitmask kullanarak eğer daha önce gidilmediyse sıradaki komşunun puanını hesaplama kodu
    List<Edge> neighbors = map.getNeighbors(currentName);
    for (int i = 0; i < neighbors.size(); i++) {
        Edge edge = neighbors.get(i);
        String next = edge.to;
        Integer nextIndexObj = indexMap.get(next);
        if (nextIndexObj == null) continue;
        int nextIndex = nextIndexObj;
        if ((mask & (1 << nextIndex)) == 0) {
            String key = currentName + next;
            double score = adjustedAttractivenessScore(
                    landmark_map.getOrDefault(key, 0.0),
                    personal_interest.getOrDefault(next, 0.0),
                    visitor_load.getOrDefault(next, 0.0),
                    edge.travelTime
            );
            double totalScore = score + dfs(mask | (1 << nextIndex), nextIndex,
                    visitedCount + 1, totalLimit,
                    dp, backtrack, map,
                    landmark_map, personal_interest, visitor_load, indexMap);
            //Komşular arasından en yüksek score a sahip olan komşunun skorunu totale ekleyip sonra o komşunun indexini tutuyoruz
            if (totalScore > maxScore) {
                maxScore = totalScore;
                bestNext = nextIndex;
            }
        }
    }
    if (bestNext != -1) {
        backtrack[mask][current] = bestNext;
    }
    return dp[mask][current] = maxScore;
}
    //From ve To kullanarak travel time hesaplama fonksiyonu
    public static double calculateTotalTravelTime(List<String> route, Graph map) {
        double totalTime = 0.0;
        for (int i = 0; i < route.size() - 1; i++) {
            String from = route.get(i);
            String to = route.get(i + 1);
            for (int j = 0; j < map.getNeighbors(from).size(); j++) {
                Edge edge = map.getNeighbors(from).get(j);
                if (edge.to.equals(to)) {
                    totalTime += edge.travelTime;
                    break;
                }
            }
        }
        return totalTime;
    }

    //Rotayı adım adım list olarak veren fonksiyon
    public static List<String> reconstructPath(int[][] backtrack, int startIndex, double[][] dp, Map<Integer, String> reverseIndexMap) {
        List<String> path = new ArrayList<>();
        int mask = 1 << startIndex;
        int current = startIndex;

        path.add(reverseIndexMap.get(current));

        while (true) {
            int next = backtrack[mask][current];
            if ((mask | (1 << next)) == mask || next == 0 && Integer.bitCount(mask) == reverseIndexMap.size()) {
                break;
            }
            path.add(reverseIndexMap.get(next));
            mask |= (1 << next);
            current = next;
        }

        path.add("Hotel"); // Son dönüş için pathe Hoteli ekliyoruz
        return path;
    }

    //Oluşturduğumuz index mapte istediğimiz indexin string karşılığını döndürüyor
    public static String getNameByIndex(Map<String, Integer> map, int idx) {
        List<Map.Entry<String, Integer>> nameIndexPairs = new ArrayList<>(map.entrySet());
        for (int i = 0; i < nameIndexPairs.size(); i++) {
            Map.Entry<String, Integer> nameIndex  = nameIndexPairs.get(i);
            if (nameIndex .getValue() == idx) return nameIndex .getKey();
        }
        return null;
    }

    //Score hesaplama fonksiyonu
    public static double adjustedAttractivenessScore(double base_score, double personal_interest, double visitor_load, double travel_time){
        double adjAttractiveScore=base_score*personal_interest*(Math.max(1-visitor_load*0.03*travel_time,0.1));
        return adjAttractiveScore;
    }

}

