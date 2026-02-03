package de.idrinth.habitevaluator.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.SeekBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.time.LocalDateTime;
import java.util.List;

import de.idrinth.habitevaluator.android.databinding.FragmentRecordEmotionEntryBinding;
import de.idrinth.habitevaluator.shared.model.EmotionEntry;
import de.idrinth.habitevaluator.shared.model.EmotionPair;
import de.idrinth.habitevaluator.shared.repository.EmotionEntryRepository;

public class RecordEmotionEntryFragment extends Fragment {

    private FragmentRecordEmotionEntryBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentRecordEmotionEntryBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        binding.strengthSeekBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                int strength = progress - 10;
                binding.strengthValueLabel.setText(String.valueOf(strength));
            }

            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
            }

            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
            }
        });

        binding.recordEmotionButton.setOnClickListener(v -> recordEmotion());
    }

    @Override
    public void onResume() {
        super.onResume();
        setupForSelectedPair();
    }

    private void setupForSelectedPair() {
        String pairId = MainActivity.getRecordEmotionPairId();
        if (pairId == null) {
            return;
        }
        List<EmotionPair> pairs = MainActivity.getSharedEmotionPairs();
        EmotionPair selectedPair = null;
        for (EmotionPair pair : pairs) {
            if (pair.getId().equals(pairId)) {
                selectedPair = pair;
                break;
            }
        }
        if (selectedPair == null) {
            return;
        }
        binding.emotionPairLabel.setText(selectedPair.toString());
        binding.negativeEndLabel.setText(selectedPair.getNegativeLabel());
        binding.positiveEndLabel.setText(selectedPair.getPositiveLabel());
        binding.strengthSeekBar.setProgress(10);
        binding.strengthValueLabel.setText("0");
        binding.notesInput.setText("");
    }

    private void recordEmotion() {
        String pairId = MainActivity.getRecordEmotionPairId();
        if (pairId == null) {
            Toast.makeText(requireContext(), R.string.no_emotion_pair_selected, Toast.LENGTH_SHORT).show();
            return;
        }

        EmotionEntryRepository repository = MainActivity.getSharedEmotionEntryRepository();
        if (repository == null) {
            return;
        }

        List<EmotionPair> pairs = MainActivity.getSharedEmotionPairs();
        EmotionPair selectedPair = null;
        for (EmotionPair pair : pairs) {
            if (pair.getId().equals(pairId)) {
                selectedPair = pair;
                break;
            }
        }
        if (selectedPair == null) {
            Toast.makeText(requireContext(), R.string.no_emotion_pair_selected, Toast.LENGTH_SHORT).show();
            return;
        }

        int strength = binding.strengthSeekBar.getProgress() - 10;
        String notes = binding.notesInput.getText().toString().trim();
        if (notes.isEmpty()) {
            notes = null;
        }

        EmotionEntry entry = new EmotionEntry(selectedPair, strength, LocalDateTime.now(), notes);
        entry.setUser(MainActivity.getSharedCurrentUser());
        repository.save(entry);

        Toast.makeText(requireContext(), R.string.emotion_entry_recorded, Toast.LENGTH_SHORT).show();

        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToEmotionalState();
        }
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
