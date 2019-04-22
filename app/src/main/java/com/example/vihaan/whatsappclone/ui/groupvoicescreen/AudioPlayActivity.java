package com.example.vihaan.whatsappclone.ui.groupvoicescreen;

import android.app.Activity;
import android.net.Uri;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;

import com.example.vihaan.whatsappclone.R;

import java.io.File;
import java.util.ArrayList;

public class AudioPlayActivity extends Activity {
    private AudioItemAdapter audioItemAdapter;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView rv = (RecyclerView) findViewById(R.id.rv);

        // arrange cells in vertical column
        rv.setLayoutManager(new LinearLayoutManager(this));

        // add 256 stub audio items
        ArrayList<AudioItem> audioItems = new ArrayList<>();
        for (int i = 0; i < 256; i++) {
            Uri uri = Uri.fromFile(new File("test"));
            //@todo: bursı sacmalik burayi mutlaka degistir. hybrid diye bir sey yok.
            audioItems.add(new AudioItem(154));
        }
        audioItemAdapter = new AudioItemAdapter(audioItems);
        rv.setAdapter(audioItemAdapter);
    }

    @Override
    protected void onPause() {
        super.onPause();
        audioItemAdapter.stopPlayer();
    }
}
