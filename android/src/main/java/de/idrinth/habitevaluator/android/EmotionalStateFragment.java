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
import java.util.List;

import de.idrinth.habitevaluator.android.databinding.FragmentEmotionalStateBinding;
import de.idrinth.habitevaluator.android.ui.EmotionPairAdapter;
import de.idrinth.habitevaluator.shared.model.EmotionPair;
import de.idrinth.habitevaluator.shared.repository.EmotionPairRepository;

public class EmotionalStateFragment extends Fragment {

    private FragmentEmotionalStateBinding binding;
    private EmotionPairAdapter adapter;
    private List<EmotionPair> emotionPairs;

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

        emotionPairs = new ArrayList<>();
        adapter = new EmotionPairAdapter(emotionPairs, this::confirmDeleteEmotionPair);
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
        emotionPairs.clear();
        emotionPairs.addAll(MainActivity.getSharedEmotionPairs());
        adapter.notifyDataSetChanged();
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

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
