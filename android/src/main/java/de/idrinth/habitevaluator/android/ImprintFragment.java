package de.idrinth.habitevaluator.android;

import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import de.idrinth.habitevaluator.android.databinding.FragmentImprintBinding;

public class ImprintFragment extends Fragment {

    static final String EMAIL_ADDRESS = "self@idrinth.de";
    static final String MAILTO_URI = "mailto:" + EMAIL_ADDRESS;
    static final String VERSION_PREFIX = "Version ";

    private FragmentImprintBinding binding;

    static String formatVersionLabel(String versionName) {
        if (versionName == null || versionName.isEmpty()) {
            return VERSION_PREFIX + "unknown";
        }
        return VERSION_PREFIX + versionName;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                             @Nullable Bundle savedInstanceState) {
        binding = FragmentImprintBinding.inflate(inflater, container, false);
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        binding.versionLabel.setText(formatVersionLabel(BuildConfig.VERSION_NAME));
        binding.emailLink.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_SENDTO);
            intent.setData(Uri.parse(MAILTO_URI));
            if (intent.resolveActivity(requireActivity().getPackageManager()) != null) {
                startActivity(intent);
            }
        });
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }
}
