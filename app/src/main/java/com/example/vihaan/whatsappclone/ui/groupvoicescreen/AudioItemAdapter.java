package com.example.vihaan.whatsappclone.ui.groupvoicescreen;

import android.media.MediaPlayer;
import android.os.Handler;
import android.os.Message;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.SeekBar;
import android.widget.TextView;

import com.example.vihaan.whatsappclone.R;
import com.example.vihaan.whatsappclone.ui.groupvoicescreen.AudioItem;

import java.util.List;
import java.util.Locale;

public class AudioItemAdapter extends RecyclerView.Adapter<AudioItemAdapter.AudioItemsViewHolder> implements Handler.Callback {

    private static final int MSG_UPDATE_SEEK_BAR = 1845;

    private MediaPlayer mediaPlayer;

    private Handler uiUpdateHandler;

    private List<AudioItem> audioItems;
    private int playingPosition;
    private AudioItemsViewHolder playingHolder;

    AudioItemAdapter(List<AudioItem> audioItems) {
        this.audioItems = audioItems;
        this.playingPosition = -1;
        uiUpdateHandler = new Handler(this);
    }

    @Override
    public AudioItemsViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        return new AudioItemsViewHolder(LayoutInflater.from(parent.getContext()).inflate(R.layout.fragment_voice_play_group, parent, false));
    }

    @Override
    public void onBindViewHolder(AudioItemsViewHolder holder, int position) {
        if (position == playingPosition) {
            playingHolder = holder;
            // this view holder corresponds to the currently playing audio cell
            // update its view to show playing progress
            updatePlayingView();
        } else {
            // and this one corresponds to non playing
            updateNonPlayingView(holder);
        }
        holder.tvIndex.setText(String.format(Locale.US, "%d", position));
    }

    @Override
    public int getItemCount() {
        return audioItems.size();
    }

    @Override
    public void onViewRecycled(AudioItemsViewHolder holder) {
        super.onViewRecycled(holder);
        if (playingPosition == holder.getAdapterPosition()) {
            // view holder displaying playing audio cell is being recycled
            // change its state to non-playing
            updateNonPlayingView(playingHolder);
            playingHolder = null;
        }
    }

    /**
     * Changes the view to non playing state
     * - icon is changed to play arrow
     * - seek bar disabled
     * - remove seek bar updater, if needed
     *
     * @param holder ViewHolder whose state is to be chagned to non playing
     */
    private void updateNonPlayingView(AudioItemsViewHolder holder) {
        if (holder == playingHolder) {
            uiUpdateHandler.removeMessages(MSG_UPDATE_SEEK_BAR);
        }
        holder.sbProgress.setEnabled(false);
        holder.sbProgress.setProgress(0);
        holder.ivPlayPause.setImageResource(R.drawable.ic_play_arrow);

    }

    /**
     * Changes the view to playing state
     * - icon is changed to pause
     * - seek bar enabled
     * - start seek bar updater, if needed
     */
    private void updatePlayingView() {
        playingHolder.sbProgress.setMax(mediaPlayer.getDuration());
        playingHolder.sbProgress.setProgress(mediaPlayer.getCurrentPosition());
        playingHolder.sbProgress.setEnabled(true);
        if (mediaPlayer.isPlaying()) {
            uiUpdateHandler.sendEmptyMessageDelayed(MSG_UPDATE_SEEK_BAR, 100);
            playingHolder.ivPlayPause.setImageResource(R.drawable.ic_pause);
        } else {
            uiUpdateHandler.removeMessages(MSG_UPDATE_SEEK_BAR);
            playingHolder.ivPlayPause.setImageResource(R.drawable.ic_play_arrow);
        }
    }

    void stopPlayer() {
        if (null != mediaPlayer) {
            releaseMediaPlayer();
        }
    }

    @Override
    public boolean handleMessage(Message msg) {
        switch (msg.what) {
            case MSG_UPDATE_SEEK_BAR: {
                playingHolder.sbProgress.setProgress(mediaPlayer.getCurrentPosition());
                uiUpdateHandler.sendEmptyMessageDelayed(MSG_UPDATE_SEEK_BAR, 100);
                return true;
            }
        }
        return false;
    }

    // Interaction listeners e.g. click, seekBarChange etc are handled in the view holder itself. This eliminates
    // need for anonymous allocations.
    class AudioItemsViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener, SeekBar.OnSeekBarChangeListener {
        SeekBar sbProgress;
        ImageView ivPlayPause;
        TextView tvIndex;

        AudioItemsViewHolder(View itemView) {
            super(itemView);
            ivPlayPause = (ImageView) itemView.findViewById(R.id.ivPlayPause);
            ivPlayPause.setOnClickListener(this);
            sbProgress = (SeekBar) itemView.findViewById(R.id.sbProgress);
            sbProgress.setOnSeekBarChangeListener(this);
            tvIndex = (TextView) itemView.findViewById(R.id.tvIndex);
        }

        @Override
        public void onClick(View v) {
            if (getAdapterPosition() == playingPosition) {
                // toggle between play/pause of audio
                if (mediaPlayer.isPlaying()) {
                    mediaPlayer.pause();
                } else {
                    mediaPlayer.start();
                }
            } else {
                // start another audio playback
                playingPosition = getAdapterPosition();
                if (mediaPlayer != null) {
                    if (null != playingHolder) {
                        updateNonPlayingView(playingHolder);
                    }
                    mediaPlayer.release();
                }
                playingHolder = this;
                startMediaPlayer(v,audioItems.get(playingPosition).audioResId);
            }
            updatePlayingView();
        }

        @Override
        public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
            if (fromUser) {
                mediaPlayer.seekTo(progress);
            }
        }

        @Override
        public void onStartTrackingTouch(SeekBar seekBar) {
        }

        @Override
        public void onStopTrackingTouch(SeekBar seekBar) {
        }
    }

    private void startMediaPlayer(View parent,int audioResId) {

        mediaPlayer = MediaPlayer.create(parent.getContext(), audioResId);
        mediaPlayer.setOnCompletionListener(new MediaPlayer.OnCompletionListener() {
            @Override
            public void onCompletion(MediaPlayer mp) {
                releaseMediaPlayer();
            }
        });
        mediaPlayer.start();
    }

    private void releaseMediaPlayer() {
        if (null != playingHolder) {
            updateNonPlayingView(playingHolder);
        }
        mediaPlayer.release();
        mediaPlayer = null;
        playingPosition = -1;
    }

}