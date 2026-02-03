package de.idrinth.habitevaluator.android;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import de.idrinth.habitevaluator.android.databinding.FragmentAddEmotionPairBinding;
import de.idrinth.habitevaluator.shared.model.EmotionPair;
import de.idrinth.habitevaluator.shared.model.User;
import de.idrinth.habitevaluator.shared.repository.EmotionPairRepository;

public class AddEmotionPairFragment extends Fragment {

    private FragmentAddEmotionPairBinding binding;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentAddEmotionPairBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.saveEmotionPairButton.setOnClickListener(v -> addEmotionPair());
    }

    private void addEmotionPair() {
        String negativeLabel = binding.negativeLabelInput.getText().toString().trim();
        String positiveLabel = binding.positiveLabelInput.getText().toString().trim();

        if (negativeLabel.isEmpty() || positiveLabel.isEmpty()) {
            Toast.makeText(requireContext(), R.string.emotion_labels_required, Toast.LENGTH_SHORT).show();
            return;
        }

        User currentUser = MainActivity.getSharedCurrentUser();
        EmotionPair pair = new EmotionPair(negativeLabel, positiveLabel);
        pair.setUser(currentUser);

        EmotionPairRepository repository = MainActivity.getSharedEmotionPairRepository();

        if (repository != null) {
            repository.save(pair);
            MainActivity.getSharedEmotionPairs().add(pair);
            Toast.makeText(requireContext(), R.string.emotion_pair_added, Toast.LENGTH_SHORT).show();
            clearForm();
        }
    }

    private void clearForm() {
        binding.negativeLabelInput.setText("");
        binding.positiveLabelInput.setText("");
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
