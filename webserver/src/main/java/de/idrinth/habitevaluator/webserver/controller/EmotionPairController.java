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

    private static final DateTimeFormatter LABEL_FORMAT = DateTimeFormatter.ofPattern("yyyy-MM-dd");

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
    public ResponseEntity<Map<String, Object>> getGraphData(HttpSession session) {
        String userId = (String) session.getAttribute("userId");
        if (userId == null) {
            return ResponseEntity.status(401).build();
        }

        List<EmotionPair> pairs = emotionPairRepository.findByUserId(userId);
        List<EmotionEntry> allEntries = emotionEntryRepository.findByUserId(userId);

        // Find the date range from all entries
        LocalDate today = LocalDate.now();
        LocalDate earliest = today;
        for (EmotionEntry entry : allEntries) {
            LocalDate entryDate = entry.getRecordedAt().toLocalDate();
            if (entryDate.isBefore(earliest)) {
                earliest = entryDate;
            }
        }

        List<String> labels = new ArrayList<>();
        for (LocalDate d = earliest; !d.isAfter(today); d = d.plusDays(1)) {
            labels.add(d.format(LABEL_FORMAT));
        }

        List<Map<String, Object>> pairSeries = new ArrayList<>();
        for (EmotionPair pair : pairs) {
            Map<LocalDate, List<Integer>> dayStrengths = new TreeMap<>();
            for (LocalDate d = earliest; !d.isAfter(today); d = d.plusDays(1)) {
                dayStrengths.put(d, new ArrayList<>());
            }

            for (EmotionEntry entry : allEntries) {
                if (entry.getEmotionPair() != null
                        && pair.getId().equals(entry.getEmotionPair().getId())) {
                    LocalDate entryDate = entry.getRecordedAt().toLocalDate();
                    if (!entryDate.isBefore(earliest) && !entryDate.isAfter(today)) {
                        dayStrengths.get(entryDate).add(entry.getStrength());
                    }
                }
            }

            List<Double> dailyAverages = new ArrayList<>();
            double totalSum = 0;
            int totalCount = 0;
            for (LocalDate d = earliest; !d.isAfter(today); d = d.plusDays(1)) {
                List<Integer> strengths = dayStrengths.get(d);
                if (strengths.isEmpty()) {
                    dailyAverages.add(null);
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
        result.put("pairs", pairSeries);
        return ResponseEntity.ok(result);
    }
}
