package de.idrinth.habitevaluator.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import de.idrinth.habitevaluator.android.databinding.FragmentDiaryNavigationBinding;

public class DiaryNavigationFragment extends Fragment {

    static final int NAVIGATION_CARD_COUNT = 6;
    static final String[] NAVIGATION_TARGETS = {
            "positivityDiary", "sportLog", "foodLog", "medicationLog", "medicationList", "emergencyPlan"
    };

    private FragmentDiaryNavigationBinding binding;

    static boolean isValidNavigationTarget(String target) {
        if (target == null) {
            return false;
        }
        for (String t : NAVIGATION_TARGETS) {
            if (t.equals(target)) {
                return true;
            }
        }
        return false;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentDiaryNavigationBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.positivityDiaryCard.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToPositivityDiary();
            }
        });

        binding.sportLogCard.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToSportLog();
            }
        });

        binding.foodLogCard.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToFoodLog();
            }
        });

        binding.medicationLogCard.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToMedicationLog();
            }
        });

        binding.medicationListCard.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToMedicationList();
            }
        });

        binding.emergencyPlanCard.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToEmergencyPlan();
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
