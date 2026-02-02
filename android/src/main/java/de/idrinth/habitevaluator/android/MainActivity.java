package de.idrinth.habitevaluator.android;

import android.content.SharedPreferences;
import android.os.Bundle;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.viewpager2.widget.ViewPager2;

import com.google.gson.reflect.TypeToken;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import de.idrinth.habitevaluator.android.databinding.ActivityMainBinding;
import de.idrinth.habitevaluator.android.persistence.FileSystemHabitCategoryRepository;
import de.idrinth.habitevaluator.android.persistence.FileSystemHabitRepository;
import de.idrinth.habitevaluator.android.ui.ScreenPagerAdapter;
import de.idrinth.habitevaluator.shared.api.ApiClient;
import de.idrinth.habitevaluator.shared.api.RemoteHabitRepository;
import de.idrinth.habitevaluator.shared.api.RemoteUserRepository;
import de.idrinth.habitevaluator.shared.model.Habit;
import de.idrinth.habitevaluator.shared.model.HabitCategory;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.HabitCategoryRepository;
import de.idrinth.habitevaluator.shared.repository.HabitRepository;
import de.idrinth.habitevaluator.shared.service.DefaultDataInitializer;

public class MainActivity extends AppCompatActivity {

    private static final String PLACEHOLDER_USERNAME = "android_user";
    private static List<Habit> sharedHabits;
    private static HabitRepository sharedHabitRepository;
    private static boolean sharedUsingRemoteStorage;
    private static List<HabitCategory> sharedCategories = new ArrayList<>();
    private static HabitCategoryRepository sharedCategoryRepository;
    private static ApiClient sharedApiClient;
    private static User sharedCurrentUser;

    public static List<Habit> getSharedHabits() {
        return sharedHabits;
    }

    public static HabitRepository getSharedHabitRepository() {
        return sharedHabitRepository;
    }

    public static boolean isSharedUsingRemoteStorage() {
        return sharedUsingRemoteStorage;
    }

    public static List<HabitCategory> getSharedCategories() {
        return sharedCategories;
    }

    public static HabitCategoryRepository getSharedCategoryRepository() {
        return sharedCategoryRepository;
    }

    public static ApiClient getSharedApiClient() {
        return sharedApiClient;
    }

    public static User getSharedCurrentUser() {
        return sharedCurrentUser;
    }

    public static void saveAllHabits() {
        if (sharedHabitRepository == null || sharedHabits == null) {
            return;
        }
        if (sharedUsingRemoteStorage) {
            new Thread(() -> {
                for (Habit habit : sharedHabits) {
                    sharedHabitRepository.save(habit);
                }
            }).start();
        } else {
            for (Habit habit : sharedHabits) {
                sharedHabitRepository.save(habit);
            }
        }
    }

    private ActivityMainBinding binding;
    private List<Habit> habits;
    private User currentUser;
    private HabitRepository habitRepository;
    private HabitCategoryRepository categoryRepository;
    private ApiClient apiClient;
    private boolean usingRemoteStorage;
    private List<HabitCategory> categoryList = new ArrayList<>();

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        SharedPreferences themePrefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
        SettingsActivity.applyThemeMode(themePrefs.getString(SettingsActivity.KEY_THEME_MODE, SettingsActivity.THEME_SYSTEM));

        super.onCreate(savedInstanceState);
        binding = ActivityMainBinding.inflate(getLayoutInflater());
        setContentView(binding.getRoot());

        habits = new ArrayList<>();
        sharedHabits = habits;

        initializeStorage();
        setupViewPager();
        setupBottomNavigation();
    }

    private void setupViewPager() {
        ScreenPagerAdapter pagerAdapter = new ScreenPagerAdapter(this);
        binding.viewPager.setAdapter(pagerAdapter);
        binding.viewPager.setOffscreenPageLimit(ScreenPagerAdapter.PAGE_COUNT);
        binding.viewPager.setCurrentItem(ScreenPagerAdapter.PAGE_HOME, false);

        binding.viewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                switch (position) {
                    case ScreenPagerAdapter.PAGE_SETTINGS:
                        binding.bottomNavigation.setSelectedItemId(R.id.nav_settings);
                        break;
                    case ScreenPagerAdapter.PAGE_HOME:
                        binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
                        break;
                    case ScreenPagerAdapter.PAGE_ADD_HABIT:
                        binding.bottomNavigation.setSelectedItemId(R.id.nav_add_habit);
                        break;
                    case ScreenPagerAdapter.PAGE_EDIT_HABITS:
                        binding.bottomNavigation.setSelectedItemId(R.id.nav_edit_habits);
                        break;
                }
            }
        });
    }

    private void setupBottomNavigation() {
        binding.bottomNavigation.setSelectedItemId(R.id.nav_home);
        binding.bottomNavigation.setOnItemSelectedListener(item -> {
            int id = item.getItemId();
            if (id == R.id.nav_settings) {
                binding.viewPager.setCurrentItem(ScreenPagerAdapter.PAGE_SETTINGS, true);
                return true;
            } else if (id == R.id.nav_home) {
                binding.viewPager.setCurrentItem(ScreenPagerAdapter.PAGE_HOME, true);
                return true;
            } else if (id == R.id.nav_add_habit) {
                binding.viewPager.setCurrentItem(ScreenPagerAdapter.PAGE_ADD_HABIT, true);
                return true;
            } else if (id == R.id.nav_edit_habits) {
                binding.viewPager.setCurrentItem(ScreenPagerAdapter.PAGE_EDIT_HABITS, true);
                return true;
            }
            return false;
        });
    }

    public void onSettingsChanged() {
        initializeStorage();
    }

    private void initializeStorage() {
        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
        String mode = prefs.getString(SettingsActivity.KEY_STORAGE_MODE, SettingsActivity.MODE_LOCAL);

        if (SettingsActivity.MODE_REMOTE.equals(mode)) {
            initializeRemoteStorage(prefs);
        } else {
            initializeLocalStorage();
        }
        updateStorageModeLabel();
        loadCategories();
    }

    private void initializeLocalStorage() {
        usingRemoteStorage = false;
        java.io.File storageDir = new java.io.File(getFilesDir(), "habit-data");
        habitRepository = new FileSystemHabitRepository(storageDir);
        categoryRepository = new FileSystemHabitCategoryRepository(storageDir);
        apiClient = null;
        currentUser = getOrCreateLocalUser();
        sharedHabitRepository = habitRepository;
        sharedUsingRemoteStorage = false;
        sharedCategoryRepository = categoryRepository;
        sharedApiClient = null;
        sharedCurrentUser = currentUser;
    }

    private User getOrCreateLocalUser() {
        SharedPreferences prefs = getSharedPreferences(SettingsActivity.PREFS_NAME, MODE_PRIVATE);
        String userId = prefs.getString("local_user_id", null);
        User user = new User(PLACEHOLDER_USERNAME, "placeholder");
        if (userId != null) {
            user.setId(userId);
        } else {
            prefs.edit().putString("local_user_id", user.getId()).apply();
        }
        return user;
    }

    private void initializeRemoteStorage(SharedPreferences prefs) {
        String url = prefs.getString(SettingsActivity.KEY_API_URL, "");
        String username = prefs.getString(SettingsActivity.KEY_API_USERNAME, "");
        String password = prefs.getString(SettingsActivity.KEY_API_PASSWORD, "");

        new Thread(() -> {
            try {
                ApiClient client = new ApiClient(url);
                boolean loggedIn = client.login(username, password);
                if (loggedIn) {
                    apiClient = client;
                    habitRepository = new RemoteHabitRepository(client);
                    RemoteUserRepository userRepo = new RemoteUserRepository(apiClient);
                    currentUser = userRepo.findAll().stream().findFirst().orElse(null);
                    usingRemoteStorage = true;
                    sharedHabitRepository = habitRepository;
                    sharedUsingRemoteStorage = true;
                    sharedCategoryRepository = null;
                    sharedApiClient = apiClient;
                    sharedCurrentUser = currentUser;
                    runOnUiThread(() -> {
                        updateStorageModeLabel();
                        loadCategories();
                        loadHabits();
                    });
                    return;
                }
            } catch (IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(this, "Failed to connect to remote API, using local storage",
                                Toast.LENGTH_LONG).show());
            }
            runOnUiThread(() -> {
                initializeLocalStorage();
                updateStorageModeLabel();
                loadCategories();
                loadHabits();
            });
        }).start();
    }

    private void loadCategories() {
        categoryList.clear();
        if (usingRemoteStorage && apiClient != null) {
            new Thread(() -> {
                try {
                    List<HabitCategory> remoteCats = apiClient.get("/api/categories",
                            new TypeToken<List<HabitCategory>>() {}.getType());
                    runOnUiThread(() -> {
                        if (remoteCats != null) {
                            categoryList.addAll(remoteCats);
                        }
                        syncSharedCategories();
                        loadHabits();
                    });
                } catch (IOException e) {
                    runOnUiThread(() -> {
                        syncSharedCategories();
                        loadHabits();
                    });
                }
            }).start();
        } else if (categoryRepository != null && currentUser != null) {
            categoryList.addAll(categoryRepository.findByUserId(currentUser.getId()));
            syncSharedCategories();
            loadHabits();
        } else {
            syncSharedCategories();
            loadHabits();
        }
    }

    private void syncSharedCategories() {
        sharedCategories.clear();
        sharedCategories.addAll(categoryList);
    }

    private void loadHabits() {
        habits.clear();
        if (habitRepository != null && currentUser != null) {
            if (usingRemoteStorage) {
                new Thread(() -> {
                    List<Habit> remoteHabits = habitRepository.findByUserId(currentUser.getId());
                    runOnUiThread(() -> habits.addAll(remoteHabits));
                }).start();
            } else {
                habits.addAll(habitRepository.findByUserId(currentUser.getId()));
            }
        }
    }

    private void updateStorageModeLabel() {
        if (usingRemoteStorage) {
            binding.storageModeText.setText(R.string.storage_mode_remote);
        } else {
            binding.storageModeText.setText(R.string.storage_mode_local);
        }
    }

    public void loadDefaults() {
        new Thread(() -> {
            try {
                if (usingRemoteStorage && apiClient != null) {
                    apiClient.post("/api/init-defaults", Collections.emptyMap(),
                            new TypeToken<Map<String, Object>>() {}.getType());
                } else if (categoryRepository != null && habitRepository != null && currentUser != null) {
                    DefaultDataInitializer initializer = new DefaultDataInitializer(categoryRepository, habitRepository);
                    initializer.initializeDefaults(currentUser);
                }
                runOnUiThread(() -> {
                    Toast.makeText(this, R.string.defaults_loaded, Toast.LENGTH_SHORT).show();
                    loadCategories();
                });
            } catch (IOException e) {
                runOnUiThread(() ->
                        Toast.makeText(this, getString(R.string.load_defaults_failed, e.getMessage()),
                                Toast.LENGTH_LONG).show());
            }
        }).start();
    }
}
