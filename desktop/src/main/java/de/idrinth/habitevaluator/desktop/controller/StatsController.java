package de.idrinth.habitevaluator.desktop.controller;

import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.SleepEntry;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.DiaryEntryRepository;
import de.idrinth.habitevaluator.shared.repository.SleepEntryRepository;
import de.idrinth.habitevaluator.shared.service.DiaryService;
import de.idrinth.habitevaluator.shared.service.HabitScoringService;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.TreeMap;

public class StatsController {

    private static final int DAYS = 30;
    private static final DateTimeFormatter LABEL_FORMAT = DateTimeFormatter.ofPattern("MM/dd");

    @FXML
    private BarChart<String, Number> habitPointsChart;
    @FXML
    private CategoryAxis habitXAxis;
    @FXML
    private NumberAxis habitYAxis;

    @FXML
    private BarChart<String, Number> diaryPointsChart;
    @FXML
    private CategoryAxis diaryXAxis;
    @FXML
    private NumberAxis diaryYAxis;

    @FXML
    private BarChart<String, Number> sleepDurationChart;
    @FXML
    private CategoryAxis sleepDurationXAxis;
    @FXML
    private NumberAxis sleepDurationYAxis;

    @FXML
    private BarChart<String, Number> sleepEntriesChart;
    @FXML
    private CategoryAxis sleepEntriesXAxis;
    @FXML
    private NumberAxis sleepEntriesYAxis;

    private List<Habit> habits = new ArrayList<>();
    private SleepEntryRepository sleepEntryRepository;
    private DiaryEntryRepository diaryEntryRepository;
    private User currentUser;

    private final DiaryService diaryService = new DiaryService();
    private final HabitScoringService scoringService = new HabitScoringService();

    public void setHabits(List<Habit> habits) {
        this.habits = habits != null ? habits : new ArrayList<>();
    }

    public void setSleepEntryRepository(SleepEntryRepository sleepEntryRepository) {
        this.sleepEntryRepository = sleepEntryRepository;
    }

    public void setDiaryEntryRepository(DiaryEntryRepository diaryEntryRepository) {
        this.diaryEntryRepository = diaryEntryRepository;
    }

    public void setCurrentUser(User currentUser) {
        this.currentUser = currentUser;
    }

    public void loadData() {
        LocalDate today = LocalDate.now();
        LocalDate startDate = today.minusDays(DAYS - 1);

        List<String> labels = new ArrayList<>();
        for (LocalDate d = startDate; !d.isAfter(today); d = d.plusDays(1)) {
            labels.add(d.format(LABEL_FORMAT));
        }

        populateHabitChart(labels, startDate, today);
        populateDiaryChart(labels, startDate, today);
        populateSleepDurationChart(labels, startDate, today);
        populateSleepEntriesChart(labels, startDate, today);
    }

    private void populateHabitChart(List<String> labels, LocalDate startDate, LocalDate today) {
        XYChart.Series<String, Number> series = new XYChart.Series<>();
        int i = 0;
        for (LocalDate d = startDate; !d.isAfter(today); d = d.plusDays(1)) {
            int dayTotal = 0;
            for (Habit habit : habits) {
                dayTotal += scoringService.calculateHabitScore(habit, d, d).getScore();
            }
            series.getData().add(new XYChart.Data<>(labels.get(i), dayTotal));
            i++;
        }
        habitXAxis.setCategories(FXCollections.observableArrayList(labels));
        habitPointsChart.getData().clear();
        habitPointsChart.getData().add(series);
    }

    private void populateDiaryChart(List<String> labels, LocalDate startDate, LocalDate today) {
        List<DiaryEntry> allEntries = new ArrayList<>();
        if (diaryEntryRepository != null && currentUser != null) {
            allEntries = diaryEntryRepository.findByUserId(currentUser.getId());
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        int i = 0;
        for (LocalDate d = startDate; !d.isAfter(today); d = d.plusDays(1)) {
            series.getData().add(new XYChart.Data<>(labels.get(i), diaryService.getDayPoints(allEntries, d)));
            i++;
        }
        diaryXAxis.setCategories(FXCollections.observableArrayList(labels));
        diaryPointsChart.getData().clear();
        diaryPointsChart.getData().add(series);
    }

    private void populateSleepDurationChart(List<String> labels, LocalDate startDate, LocalDate today) {
        List<SleepEntry> allEntries = new ArrayList<>();
        if (sleepEntryRepository != null && currentUser != null) {
            allEntries = sleepEntryRepository.findByUserId(currentUser.getId());
        }

        Map<LocalDate, Double> durationByDate = new TreeMap<>();
        for (LocalDate d = startDate; !d.isAfter(today); d = d.plusDays(1)) {
            durationByDate.put(d, 0.0);
        }
        for (SleepEntry entry : allEntries) {
            if (!entry.getDate().isBefore(startDate) && !entry.getDate().isAfter(today)) {
                durationByDate.merge(entry.getDate(), entry.getHours(), Double::sum);
            }
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        int i = 0;
        for (Double duration : durationByDate.values()) {
            series.getData().add(new XYChart.Data<>(labels.get(i), duration));
            i++;
        }
        sleepDurationXAxis.setCategories(FXCollections.observableArrayList(labels));
        sleepDurationChart.getData().clear();
        sleepDurationChart.getData().add(series);
    }

    private void populateSleepEntriesChart(List<String> labels, LocalDate startDate, LocalDate today) {
        List<SleepEntry> allEntries = new ArrayList<>();
        if (sleepEntryRepository != null && currentUser != null) {
            allEntries = sleepEntryRepository.findByUserId(currentUser.getId());
        }

        Map<LocalDate, Integer> countByDate = new TreeMap<>();
        for (LocalDate d = startDate; !d.isAfter(today); d = d.plusDays(1)) {
            countByDate.put(d, 0);
        }
        for (SleepEntry entry : allEntries) {
            if (!entry.getDate().isBefore(startDate) && !entry.getDate().isAfter(today)) {
                countByDate.merge(entry.getDate(), 1, Integer::sum);
            }
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        int i = 0;
        for (Integer count : countByDate.values()) {
            series.getData().add(new XYChart.Data<>(labels.get(i), count));
            i++;
        }
        sleepEntriesXAxis.setCategories(FXCollections.observableArrayList(labels));
        sleepEntriesChart.getData().clear();
        sleepEntriesChart.getData().add(series);
    }
}
