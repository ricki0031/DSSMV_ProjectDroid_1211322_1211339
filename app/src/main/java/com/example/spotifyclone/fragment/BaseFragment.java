package com.example.spotifyclone.fragment;

import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import com.example.spotifyclone.MainActivity;
import com.example.spotifyclone.model.MediaItem;
import com.example.spotifyclone.utils.Session;

/** Funcionalidades comuns a todos os ecrãs. */
public abstract class BaseFragment extends Fragment {

    /** Token sempre atualizado (renovado automaticamente pela MainActivity). */
    protected String token() {

        return Session.getAccessToken(requireContext());
    }

    /** true se o Fragment ainda está no ecrã (as respostas da API podem chegar depois de sair). */
    protected boolean alive() {

        return isAdded() && getView() != null;
    }

    @Nullable
    protected MainActivity main() {
        if (getActivity() instanceof MainActivity) {
            return (MainActivity) getActivity();
        } else {
            return null;
        }
    }

    protected void open(MediaItem item) {
        MainActivity activity = main();
        if (activity != null) {
            activity.openMedia(item);
        }
    }

    protected String deviceId() {
        MainActivity activity = main();
        if (activity != null) {
            return activity.globalDeviceId;
        } else {
            return null;
        }
    }
}
