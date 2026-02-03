package de.idrinth.habitevaluator.desktop.controller;

import de.idrinth.habitevaluator.shared.model.DiaryEntry;
import de.idrinth.habitevaluator.shared.model.EmotionEntry;
import de.idrinth.habitevaluator.shared.model.EventCorrelation;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.SleepEntry;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.DiaryEntryRepository;
import de.idrinth.habitevaluator.shared.repository.EmotionEntryRepository;
import de.idrinth.habitevaluator.shared.repository.SleepEntryRepository;
import de.idrinth.habitevaluator.shared.service.DiaryService;
import de.idrinth.habitevaluator.shared.service.EventCorrelationService;
import de.idrinth.habitevaluator.shared.service.HabitScoringService;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.chart.BarChart;
import javafx.scene.chart.CategoryAxis;
import javafx.scene.chart.NumberAxis;
import javafx.scene.chart.XYChart;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;

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

    @FXML
    private BarChart<String, Number> emotionEventsChart;
    @FXML
    private CategoryAxis emotionXAxis;
    @FXML
    private NumberAxis emotionYAxis;

    @FXML
    private TableView<EventCorrelation> correlationTable;
    @FXML
    private TableColumn<EventCorrelation, String> eventAColumn;
    @FXML
    private TableColumn<EventCorrelation, String> eventBColumn;
    @FXML
    private TableColumn<EventCorrelation, Number> correlationColumn;
    @FXML
    private TableColumn<EventCorrelation, Number> sharedDaysColumn;

    private List<Habit> habits = new ArrayList<>();
    private SleepEntryRepository sleepEntryRepository;
    private DiaryEntryRepository diaryEntryRepository;
    private EmotionEntryRepository emotionEntryRepository;
    private User currentUser;

    private final DiaryService diaryService = new DiaryService();
    private final HabitScoringService scoringService = new HabitScoringService();
    private final EventCorrelationService correlationService = new EventCorrelationService();

    public void setHabits(List<Habit> habits) {
        this.habits = habits != null ? habits : new ArrayList<>();
    }

    public void setSleepEntryRepository(SleepEntryRepository sleepEntryRepository) {
        this.sleepEntryRepository = sleepEntryRepository;
    }

    public void setDiaryEntryRepository(DiaryEntryRepository diaryEntryRepository) {
        this.diaryEntryRepository = diaryEntryRepository;
    }

    public void setEmotionEntryRepository(EmotionEntryRepository emotionEntryRepository) {
        this.emotionEntryRepository = emotionEntryRepository;
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
        populateEmotionEventsChart(labels, startDate, today);
        populateCorrelationTable();
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

    private void populateEmotionEventsChart(List<String> labels, LocalDate startDate, LocalDate today) {
        List<EmotionEntry> allEntries = new ArrayList<>();
        if (emotionEntryRepository != null && currentUser != null) {
            allEntries = emotionEntryRepository.findByUserId(currentUser.getId());
        }

        Map<LocalDate, Integer> countByDate = new TreeMap<>();
        for (LocalDate d = startDate; !d.isAfter(today); d = d.plusDays(1)) {
            countByDate.put(d, 0);
        }
        for (EmotionEntry entry : allEntries) {
            LocalDate entryDate = entry.getRecordedAt().toLocalDate();
            if (!entryDate.isBefore(startDate) && !entryDate.isAfter(today)) {
                countByDate.merge(entryDate, 1, Integer::sum);
            }
        }

        XYChart.Series<String, Number> series = new XYChart.Series<>();
        int i = 0;
        for (Integer count : countByDate.values()) {
            series.getData().add(new XYChart.Data<>(labels.get(i), count));
            i++;
        }
        emotionXAxis.setCategories(FXCollections.observableArrayList(labels));
        emotionEventsChart.getData().clear();
        emotionEventsChart.getData().add(series);
    }

    private void populateCorrelationTable() {
        List<DiaryEntry> diaryEntries = new ArrayList<>();
        List<SleepEntry> sleepEntries = new ArrayList<>();
        if (diaryEntryRepository != null && currentUser != null) {
            diaryEntries = diaryEntryRepository.findByUserId(currentUser.getId());
        }
        if (sleepEntryRepository != null && currentUser != null) {
            sleepEntries = sleepEntryRepository.findByUserId(currentUser.getId());
        }

        List<EventCorrelation> correlations = correlationService.calculateCorrelations(
                habits, diaryEntries, sleepEntries);

        eventAColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEventA()));
        eventBColumn.setCellValueFactory(c -> new SimpleStringProperty(c.getValue().getEventB()));
        correlationColumn.setCellValueFactory(c ->
                new SimpleDoubleProperty(Math.round(c.getValue().getCorrelation() * 1000.0) / 1000.0));
        sharedDaysColumn.setCellValueFactory(c ->
                new SimpleIntegerProperty(c.getValue().getSharedDays()));

        correlationTable.setItems(FXCollections.observableArrayList(correlations));
    }
}
