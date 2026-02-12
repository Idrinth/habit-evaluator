package de.idrinth.habitevaluator.android;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import de.idrinth.habitevaluator.android.databinding.FragmentEmotionalStateBinding;
import de.idrinth.habitevaluator.android.ui.EmotionDataAdapter;
import de.idrinth.habitevaluator.shared.model.EmotionEntry;
import de.idrinth.habitevaluator.shared.model.EmotionPair;
import de.idrinth.habitevaluator.shared.repository.EmotionEntryRepository;
import de.idrinth.habitevaluator.shared.repository.EmotionPairRepository;

public class EmotionalStateFragment extends Fragment {

    static Map<String, List<EmotionEntry>> groupEntriesByPairId(
            List<EmotionPair> pairs, List<EmotionEntry> allEntries) {
        Map<String, List<EmotionEntry>> entriesByPair = new HashMap<>();
        for (EmotionPair pair : pairs) {
            List<EmotionEntry> pairEntries = new ArrayList<>();
            for (EmotionEntry entry : allEntries) {
                if (entry.getEmotionPair() != null && entry.getEmotionPair().getId().equals(pair.getId())) {
                    pairEntries.add(entry);
                }
            }
            pairEntries.sort(Comparator.comparing(EmotionEntry::getRecordedAt).reversed());
            entriesByPair.put(pair.getId(), pairEntries);
        }
        return entriesByPair;
    }

    private FragmentEmotionalStateBinding binding;
    private EmotionDataAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentEmotionalStateBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        adapter = new EmotionDataAdapter(
                this::onEmotionPairClicked,
                this::confirmDeleteEmotionPair,
                this::confirmDeleteEmotionEntry);
        binding.emotionPairsRecyclerView.setLayoutManager(new LinearLayoutManager(requireContext()));
        binding.emotionPairsRecyclerView.setAdapter(adapter);

        binding.addEmotionPairButton.setOnClickListener(v -> {
            if (getActivity() instanceof MainActivity) {
                ((MainActivity) getActivity()).navigateToAddEmotionPair();
            }
        });
    }

    @Override
    public void onResume() {
        super.onResume();
        refreshList();
    }

    private void refreshList() {
        List<EmotionPair> pairs = MainActivity.getSharedEmotionPairs();
        EmotionEntryRepository entryRepository = MainActivity.getSharedEmotionEntryRepository();
        List<EmotionEntry> allEntries = new ArrayList<>();
        if (entryRepository != null && MainActivity.getSharedLocalUser() != null) {
            allEntries.addAll(entryRepository.findByUserId(MainActivity.getSharedLocalUser().getId()));
        }

        Map<String, List<EmotionEntry>> entriesByPair = groupEntriesByPairId(pairs, allEntries);
        adapter.setData(pairs, entriesByPair);
    }

    private void onEmotionPairClicked(EmotionPair pair) {
        if (getActivity() instanceof MainActivity) {
            ((MainActivity) getActivity()).navigateToRecordEmotionEntry(pair.getId());
        }
    }

    private void confirmDeleteEmotionPair(EmotionPair pair) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_emotion_pair)
                .setMessage(getString(R.string.delete_emotion_pair_confirmation, pair.toString()))
                .setPositiveButton(android.R.string.ok, (dialog, which) -> deleteEmotionPair(pair))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void deleteEmotionPair(EmotionPair pair) {
        EmotionPairRepository repository = MainActivity.getSharedEmotionPairRepository();
        if (repository != null) {
            repository.deleteById(pair.getId());
        }
        MainActivity.getSharedEmotionPairs().remove(pair);
        refreshList();
        Toast.makeText(requireContext(), R.string.emotion_pair_deleted, Toast.LENGTH_SHORT).show();
    }

    private void confirmDeleteEmotionEntry(EmotionEntry entry) {
        new AlertDialog.Builder(requireContext())
                .setTitle(R.string.delete_emotion_entry)
                .setMessage(R.string.delete_emotion_entry_confirmation)
                .setPositiveButton(android.R.string.ok, (dialog, which) -> deleteEmotionEntry(entry))
                .setNegativeButton(android.R.string.cancel, null)
                .show();
    }

    private void deleteEmotionEntry(EmotionEntry entry) {
        EmotionEntryRepository repository = MainActivity.getSharedEmotionEntryRepository();
        if (repository != null) {
            repository.deleteById(entry.getId());
        }
        refreshList();
        Toast.makeText(requireContext(), R.string.emotion_entry_deleted, Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
