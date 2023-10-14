package com.example.secureonlinesharing;

import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import android.widget.ImageButton;
import android.widget.MediaController;
import android.widget.VideoView;

import androidx.annotation.NonNull;
import androidx.fragment.app.Fragment;
import androidx.navigation.fragment.NavHostFragment;

import com.example.secureonlinesharing.databinding.FragmentThirdBinding;

public class ThirdFragment extends Fragment {

    private FragmentThirdBinding binding;

    @Override
    public View onCreateView(
            LayoutInflater inflater, ViewGroup container,
            Bundle savedInstanceState

    )

    {

        binding = FragmentThirdBinding.inflate(inflater, container, false);
        VideoView videoView = (VideoView) binding.videoView;  //casting to VideoView is not Strictly required above API level 26
        videoView.setZOrderOnTop(true);
        videoView.setVisibility(View.VISIBLE);
  //      videoView.setVideoURI(Uri.parse("android.resource://" + getActivity().getPackageName() + "/" + R.raw.monkey)); //set the path of the video that we need to use in our VideoView

        MediaController mediaController = new MediaController(getActivity());
        mediaController.setAnchorView(videoView);
        mediaController.setMediaPlayer(videoView);
        videoView.setMediaController(mediaController);
        //videoView.setVideoPath("android.resource://" + getActivity().getPackageName() + "/" + R.raw.video_clip); //set the path of the video that we need to use in our VideoView
        videoView.start();  //start() method of the VideoView class will start the video to play
        return binding.getRoot();

    }

    public void onViewCreated(@NonNull View view, Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

       // ((MainActivity) getActivity()).backButton.setVisibility(View.VISIBLE);
        ImageButton backButton = getActivity().findViewById(R.id.backButton);
        if (backButton!= null)
        {
            backButton.setVisibility(View.VISIBLE);

            backButton.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View view)
                {
                    NavHostFragment.findNavController(ThirdFragment.this)
                            .navigate(R.id.action_thirdFragment_to_SecondFragment);
                }
            });
        }

      /*  binding.buttonSecond.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                NavHostFragment.findNavController(SecondFragment.this)
                        .navigate(R.id.action_SecondFragment_to_FirstFragment);
            }
        });*/
    }

    @Override
    public void onDestroyView() {
        super.onDestroyView();
        binding = null;
    }

}