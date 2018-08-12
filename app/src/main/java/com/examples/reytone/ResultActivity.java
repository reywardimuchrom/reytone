package com.examples.reytone;

import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Environment;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.ListView;

import java.io.File;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ResultActivity extends AppCompatActivity {
    File file;
    ArrayList <String> audioList;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_result);

        ListView listViewAudio = (ListView) findViewById(R.id.listAudio
        );
        audioList = new ArrayList<String>();

        File directory = Environment.getExternalStorageDirectory();
        file  = new File( directory + "/Reytones" );
        if (!file.exists()) {
            file.mkdir();
            file  = new File( directory + "/Reytones" );
        }

        final File listAudioFile[] = file.listFiles();

        for( int i = 0; i < listAudioFile.length; i++)
        {
            audioList.add( listAudioFile[i].getName() );
        }
        ArrayAdapter<String> adapter = new StableArrayAdapter(this,
                android.R.layout.simple_list_item_1,
                audioList);
        listViewAudio.setAdapter(adapter);
        listViewAudio.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Intent intent = new Intent();
                intent.setAction(android.content.Intent.ACTION_VIEW);
                intent.setDataAndType(Uri.fromFile(listAudioFile[position]), "audio/*");
                startActivity(intent);
            }
        });
    }
    private class StableArrayAdapter extends ArrayAdapter<String>{

        HashMap<String, Integer> mIdMap=new HashMap<String, Integer>();

        public StableArrayAdapter(Context context, int textViewResourceId, List<String> objects) {
            super(context, textViewResourceId,objects);

            for(int i=0;i<objects.size();i++)
            {
                mIdMap.put(objects.get(i), i);
            }
        }

        @Override
        public long getItemId(int position) {
            String item=getItem(position);
            return mIdMap.get(item);
        }

        @Override
        public boolean hasStableIds() {
            return true;
        }

    }
}
