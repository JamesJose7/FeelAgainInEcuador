package com.jose.feelagaininecuador.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Point;
import android.graphics.drawable.Drawable;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.View;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import com.jose.feelagaininecuador.model.DocData;
import com.jose.feelagaininecuador.R;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.squareup.okhttp.Call;
import com.squareup.okhttp.Callback;
import com.squareup.okhttp.OkHttpClient;
import com.squareup.okhttp.Request;
import com.squareup.okhttp.Response;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import me.grantland.widget.AutofitHelper;

public class MainActivity extends AppCompatActivity {

    public static final String QUERY_MAIN = "Query main";
    public static boolean wasSearchedMain = false;

    protected TextView queueTime;
    protected EditText searchBar;
    protected ProgressBar mProgressBar;
    protected ImageView mSearchButton;
    protected ProgressBar mDataPB;

    protected FloatingActionButton fab;

    private List<DocData> mDocs;

    private String mQueryString;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Image Loader
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .build();
        ImageLoader.getInstance().init(config);

        queueTime = (TextView) findViewById(R.id.queue_time);
        searchBar = (EditText) findViewById(R.id.search_bar);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mSearchButton = (ImageView) findViewById(R.id.search_button);

        toggleRefresh();

        //Get last query
        Intent intent = getIntent();
        if (intent.hasExtra(BubbleDisplay.QUERY_BUBBLE) && wasSearchedMain) {
            String query = intent.getStringExtra(BubbleDisplay.QUERY_BUBBLE);
            String url = "http://j4loxa.com/serendipity/sr/browse?q=" + query + "&wt=json&rows=100";
            searchBar.setText(query);
            getQuery();
            getContents(url);
        }

        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getQuery();
                wasSearchedMain = true;

                // http://j4loxa.com/serendipity/sr/browse?q=quito&wt=json
                String url = "http://j4loxa.com/serendipity/sr/browse?q=" + mQueryString + "&wt=json&rows=100";

                getContents(url);
            }
        });

        fab = (FloatingActionButton) findViewById(R.id.fab);


        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                /*Snackbar.make(view, "Replace with your own action", Snackbar.LENGTH_LONG)
                        .setAction("Action", null).show();*/
                Intent intent = new Intent(MainActivity.this, BubbleDisplay.class);
                intent.putExtra(QUERY_MAIN, mQueryString);
                startActivity(intent);
                overridePendingTransition(R.anim.abc_fade_out, R.anim.abc_fade_in);
            }
        });
    }

    private void getQuery() {
        mQueryString = searchBar.getText().toString();
    }

    public void getContents(String jsonUrl) {


        if (isNetworkAvailable()) {
            //Let the user know data is being loaded
            toggleRefresh();

            OkHttpClient client = new OkHttpClient();
            Request request = new Request.Builder()
                    .url(jsonUrl)
                    .build();

            Call call = client.newCall(request);
            call.enqueue(new Callback() {
                @Override
                public void onFailure(Request request, IOException e) {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });
                    //Alert user about error
                    //alertUserAboutError();
                }

                @Override
                public void onResponse(Response response) throws IOException {
                    runOnUiThread(new Runnable() {
                        @Override
                        public void run() {
                            toggleRefresh();
                        }
                    });

                    try {
                        final String jsonData = response.body().string();
                        //Diplay collected data on logcat
                        Log.v("JSON", jsonData);
                        if (response.isSuccessful()) {
                            parseData(jsonData);
                            runOnUiThread(new Runnable() {
                                @Override
                                public void run() {
                                    updateDisplay();
                                }
                            });
                        } else {
                            //alertUserAboutError();
                        }
                    }
                    catch (IOException | JSONException e) {
                        Log.e("ERROR", "Exception caught: ", e);
                    }
                }
            });
        } else {
            Toast.makeText(this, "Network is unavailable!", Toast.LENGTH_LONG).show();
            //alertUserAboutError();
        }
    }

    private void updateDisplay() {
        //Queue time message
        queueTime.setText(DocData.getQueueTime());

        int counter = 0;
        int row = 0;
        int col = 0;

        int total = mDocs.size();
        int column = 2;
        int rows = total / column;

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        int screenWidth = size.x;
        int halfScreenWidth = (int) (screenWidth * 0.5 - 8);

        //ViewGroup insertPoint = (ViewGroup) findViewById(R.id.items_grid);
        GridLayout insertPoint = (GridLayout) findViewById(R.id.items_grid);
        insertPoint.removeAllViews();

        insertPoint.setColumnCount(column);
        insertPoint.setRowCount(rows + 1);

        for (DocData doc : mDocs) {
            LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = vi.inflate(R.layout.data_template, null);

            mDataPB = (ProgressBar) view.findViewById(R.id.data_pb);
            TextView titleView = (TextView) view.findViewById(R.id.title);
            ImageView imageView = (ImageView) view.findViewById(R.id.image_view);
            TextView descriptionView = (TextView) view.findViewById(R.id.description);

            AutofitHelper.create(titleView);
            AutofitHelper.create(descriptionView);

            titleView.setText(doc.getTitle());
            displayImage(doc.getImageUri(), imageView);
            descriptionView.setText(doc.getDescription());


            //TEST
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.width = halfScreenWidth;
            params.rightMargin = 2;
            params.leftMargin = 2;
            params.topMargin = 5;
            params.setGravity(Gravity.CENTER);
            params.columnSpec = GridLayout.spec(col);
            params.rowSpec = GridLayout.spec(row);
            view.setLayoutParams(params);

            insertPoint.addView(view);

            //insertPoint.addView(view, counter, new ViewGroup.LayoutParams(ViewGroup.LayoutParams.MATCH_PARENT, ViewGroup.LayoutParams.MATCH_PARENT));
            //insertPoint.addView(view, counter, new GridLayout.LayoutParams(GridLayout.spec(row), GridLayout.spec(col)));

            counter++;

            //rows and columns
            if (col == 1) {
                col = 0;
                row++;
            } else {
                col++;
            }


        }
    }

    private void parseData(String jsonData) throws JSONException {
        mDocs = new ArrayList<>();

        JSONObject jsonObject = new JSONObject(jsonData);

        JSONObject responseObj = jsonObject.getJSONObject("response");
        JSONArray docs = responseObj.getJSONArray("docs");

        for (int i = 0; i < docs.length(); i++) {
            DocData data = new DocData();

            JSONObject element = docs.getJSONObject(i);

            data.setTitle(element.getString("title"));
            data.setDescription(element.getString("description"));
            if (element.getString("image").length() > 1)
                data.setImageUri(element.getString("image"));
            else
                data.setImageUri("https://scontent-iad3-1.xx.fbcdn.net/hphotos-xtf1/v/t1.0-9/11025217_637827469656013_4230661887406901190_n.jpg?oh=5554db13eb5110e8e013b02e0da5c2d5&oe=571FC491");
            mDocs.add(data);
        }

        //Queue time message
        String queueTime;

        JSONObject responseHeader = jsonObject.getJSONObject("responseHeader");

        queueTime = String.format("Found %d results in %d ms.",
                responseObj.getInt("numFound"),
                responseHeader.getInt("QTime"));

        DocData.setQueueTime(queueTime);
    }

    public void toggleRefresh() {
        if (mProgressBar.getVisibility() == View.VISIBLE) {
            mProgressBar.setVisibility(View.INVISIBLE);
        } else {
            mProgressBar.setVisibility(View.VISIBLE);
        }
    }

    public boolean isNetworkAvailable() {
        ConnectivityManager manager = (ConnectivityManager)
                getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo networkInfo = manager.getActiveNetworkInfo();
        boolean isAvailable = false;
        if (networkInfo != null && networkInfo.isConnected()) {
            isAvailable = true;
        }

        return isAvailable;
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        //noinspection SimplifiableIfStatement
        if (id == R.id.action_settings) {
            return true;
        }

        return super.onOptionsItemSelected(item);
    }

    public void displayImage(String imageUri, ImageView imageView) {
        final ProgressBar imageProgressBar = mDataPB;

        ImageLoader imageLoader = ImageLoader.getInstance();


        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .showImageOnFail(getResources().getDrawable(R.drawable.image_missing))
                .build();


        //download and display image from url
        //imageLoader.displayImage(imageUri, imageView, options);

        imageLoader.displayImage(imageUri, imageView, options, new SimpleImageLoadingListener() {
            @Override
            public void onLoadingStarted(String imageUri, View view) {
                imageProgressBar.setVisibility(View.VISIBLE);
            }

            @Override
            public void onLoadingFailed(String imageUri, View view, FailReason failReason) {
                imageProgressBar.setVisibility(View.GONE);

            }

            @Override
            public void onLoadingComplete(String imageUri, View view, Bitmap loadedImage) {
                imageProgressBar.setVisibility(View.GONE);
            }
        });



    }
}
