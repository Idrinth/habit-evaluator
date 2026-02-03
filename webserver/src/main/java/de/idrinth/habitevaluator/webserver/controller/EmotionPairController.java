package de.idrinth.habitevaluator.webserver.controller;

import de.idrinth.habitevaluator.shared.model.EmotionEntry;
import de.idrinth.habitevaluator.shared.model.EmotionPair;
import de.idrinth.habitevaluator.shared.repository.EmotionEntryRepository;
import de.idrinth.habitevaluator.shared.repository.EmotionPairRepository;
import jakarta.servlet.http.HttpSession;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

@RestController
@RequestMapping("/api/emotions")
public class EmotionPairController {

    private static final DateTimeFormatter LABEL_FORMAT = DateTimeFormatter.ofPattern("MM/dd");

    private final EmotionPairRepository emotionPairRepository;
    private final EmotionEntryRepository emotionEntryRepository;

    public EmotionPairController(EmotionPairRepository emotionPairRepository,
                                 EmotionEntryRepository emotionEntryRepository) {
        this.emotionPairRepository = emotionPairRepository;
        this.emotionEntryRepository = emotionEntryRepository;
    }

    @GetMapping("/pairs")
    public ResponseEntity<List<Map<String, Object>>> getPairs(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }
        List<EmotionPair> pairs = emotionPairRepository.findByUserId(userId);
        List<Map<String, Object>> result = new ArrayList<>();
        for (EmotionPair pair : pairs) {
            Map<String, Object> map = new LinkedHashMap<>();
            map.put("id", pair.getId());
            map.put("negativeLabel", pair.getNegativeLabel());
            map.put("positiveLabel", pair.getPositiveLabel());
            result.add(map);
        }
        return ResponseEntity.ok(result);
    }

    @GetMapping("/graph")
    public ResponseEntity<Map<String, Object>> getGraphData(
            @RequestParam(defaultValue = "week") String period,
            HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        int days = "month".equals(period) ? 30 : 7;
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(days - 1);

        List<String> labels = new ArrayList<>();
        for (LocalDate d = startDate; !d.isAfter(today); d = d.plusDays(1)) {
            labels.add(d.format(LABEL_FORMAT));
        }

        List<EmotionPair> pairs = emotionPairRepository.findByUserId(userId);
        List<EmotionEntry> allEntries = emotionEntryRepository.findByUserId(userId);

        List<Map<String, Object>> pairSeries = new ArrayList<>();
        for (EmotionPair pair : pairs) {
            Map<LocalDate, List<Integer>> dayStrengths = new TreeMap<>();
            for (LocalDate d = startDate; !d.isAfter(today); d = d.plusDays(1)) {
                dayStrengths.put(d, new ArrayList<>());
            }

            for (EmotionEntry entry : allEntries) {
                if (entry.getEmotionPair() != null
                        && pair.getId().equals(entry.getEmotionPair().getId())) {
                    LocalDate entryDate = entry.getRecordedAt().toLocalDate();
                    if (!entryDate.isBefore(startDate) && !entryDate.isAfter(today)) {
                        dayStrengths.get(entryDate).add(entry.getStrength());
                    }
                }
            }

            List<Double> dailyAverages = new ArrayList<>();
            double totalSum = 0;
            int totalCount = 0;
            for (LocalDate d = startDate; !d.isAfter(today); d = d.plusDays(1)) {
                List<Integer> strengths = dayStrengths.get(d);
                if (strengths.isEmpty()) {
                    dailyAverages.add(0.0);
                } else {
                    double avg = strengths.stream().mapToInt(Integer::intValue).average().orElse(0);
                    dailyAverages.add(Math.round(avg * 100.0) / 100.0);
                    totalSum += strengths.stream().mapToInt(Integer::intValue).sum();
                    totalCount += strengths.size();
                }
            }

            double overallAverage = totalCount > 0
                    ? Math.round((totalSum / totalCount) * 100.0) / 100.0
                    : 0.0;

            Map<String, Object> series = new LinkedHashMap<>();
            series.put("pairId", pair.getId());
            series.put("negativeLabel", pair.getNegativeLabel());
            series.put("positiveLabel", pair.getPositiveLabel());
            series.put("dailyAverages", dailyAverages);
            series.put("overallAverage", overallAverage);
            series.put("totalEntries", totalCount);
            pairSeries.add(series);
        }

        Map<String, Object> result = new LinkedHashMap<>();
        result.put("labels", labels);
        result.put("period", period);
        result.put("pairs", pairSeries);
        return ResponseEntity.ok(result);
    }
}
