package com.jose.feelagaininecuador.ui;

import android.content.Context;
import android.content.Intent;
import android.graphics.Bitmap;
import android.graphics.Color;
import android.graphics.Point;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.CardView;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.support.v7.widget.Toolbar;
import android.util.Log;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MotionEvent;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.GridLayout;
import android.widget.ImageView;
import android.widget.ListView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.jose.feelagaininecuador.R;
import com.jose.feelagaininecuador.adapters.HashTagAdapter;
import com.jose.feelagaininecuador.alert_dialogs.AlertDialogFragment;
import com.jose.feelagaininecuador.model.DocData;
import com.nostra13.universalimageloader.core.DisplayImageOptions;
import com.nostra13.universalimageloader.core.ImageLoader;
import com.nostra13.universalimageloader.core.ImageLoaderConfiguration;
import com.nostra13.universalimageloader.core.assist.FailReason;
import com.nostra13.universalimageloader.core.listener.SimpleImageLoadingListener;
import com.pkmmte.view.CircularImageView;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class BubbleDisplay extends AppCompatActivity {

    private static final long DOUBLE_PRESS_INTERVAL = 250; // in millis
    private long lastPressTime;

    private boolean mHasDoubleClicked = false;

    private static final ScheduledExecutorService worker =
            Executors.newSingleThreadScheduledExecutor();

    public static final String QUERY_BUBBLE = "Query Bubble";

    protected TextView queueTime;
    protected EditText searchBar;
    protected FloatingActionButton fab;
    protected ImageView mSearchButton;
    protected RelativeLayout mMainContainer;

    protected ProgressBar mProgressBar;
    protected ProgressBar mBubblePB;

    protected ListView mListView;
    protected RelativeLayout mBackgroundCard;
    protected TextView mViewFullImageButton;
    protected RelativeLayout mDisplayFullImageLayout;
    protected ImageView mFullScreenImage;
    protected Button mBackFromFullScreen;

    private List<DocData> mDocs;

    private String mFullImageUri;
    private String mQueryString;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_bubble_display);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        //Image Loader
        ImageLoaderConfiguration config = new ImageLoaderConfiguration.Builder(this)
                .build();
        ImageLoader.getInstance().init(config);

        //Main display
        queueTime = (TextView) findViewById(R.id.queue_time);
        searchBar = (EditText) findViewById(R.id.search_bar);
        mProgressBar = (ProgressBar) findViewById(R.id.progress_bar);
        mSearchButton = (ImageView) findViewById(R.id.search_button);
        mMainContainer = (RelativeLayout) findViewById(R.id.main_container);

        //More info card
        mBackgroundCard = (RelativeLayout) findViewById(R.id.hash_card);
        mBackgroundCard.setVisibility(View.INVISIBLE);
        mViewFullImageButton = (TextView) findViewById(R.id.view_full_image_button);

        //Display full image
        mDisplayFullImageLayout = (RelativeLayout) findViewById(R.id.displayFullImageLayout);
        mDisplayFullImageLayout.setVisibility(View.GONE);
        mFullScreenImage = (ImageView) findViewById(R.id.imageFullScreen);
        mBackFromFullScreen = (Button) findViewById(R.id.backArrow);
        mBackFromFullScreen.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mBackFromFullScreen.setBackgroundColor(Color.parseColor("#55ffffff"));
                    //Show main container
                    mMainContainer.setVisibility(View.VISIBLE);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    mBackFromFullScreen.setBackgroundColor(Color.parseColor("#00000000"));

                    mDisplayFullImageLayout.setVisibility(View.GONE);

                    //Show FAB
                    fab.setVisibility(View.VISIBLE);
                }
                return true;
            }
        });
        mViewFullImageButton.setOnTouchListener(new View.OnTouchListener() {
            @Override
            public boolean onTouch(View view, MotionEvent event) {
                if (event.getAction() == MotionEvent.ACTION_DOWN) {
                    mViewFullImageButton.setTextColor(Color.parseColor("#05c6ff"));
                    //Clear last image
                    mFullScreenImage.setImageResource(android.R.color.transparent);
                }
                if (event.getAction() == MotionEvent.ACTION_UP) {
                    mViewFullImageButton.setTextColor(Color.parseColor("#9905c6ff"));
                    mDisplayFullImageLayout.setVisibility(View.VISIBLE);
                    displayFullImage(mFullImageUri, mFullScreenImage);

                    //Hide main container
                    mMainContainer.setVisibility(View.INVISIBLE);

                    //hide FAB
                    fab.setVisibility(View.INVISIBLE);
                }
                return true;
            }
        });


        mListView = (ListView) findViewById(R.id.hashtag_recycler);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> adapterView, View view, int i, long l) {
                TextView hashButton = (TextView) view.findViewById(R.id.hashtag_text);
                String hashtag = hashButton.getText().toString();
                String[] hashtagA = hashtag.split("#");
                hashtag = hashtagA[1];

                getQuery();
                String newQuery = "http://j4loxa.com/serendipity/sr/browse?q=" + mQueryString + "&wt=json&rows=100&fq=hash_tags:" + hashtag;

                //Toast.makeText(BubbleDisplay.this, hashtag, Toast.LENGTH_LONG).show();
                getContents(newQuery);
                mBackgroundCard.setVisibility(View.INVISIBLE);
            }
        });

        toggleRefresh();

        Intent intent = getIntent();
        if (intent.hasExtra(MainActivity.QUERY_MAIN) && MainActivity.wasSearchedMain) {
            String query = intent.getStringExtra(MainActivity.QUERY_MAIN);
            String url = "http://j4loxa.com/serendipity/sr/browse?q=" + query + "&wt=json&rows=100";
            searchBar.setText(query);
            getQuery();
            getContents(url);
        }

        mSearchButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {

                getQuery();
                MainActivity.wasSearchedMain = true;

                // http://j4loxa.com/serendipity/sr/browse?q=quito&wt=json
                String url = "http://j4loxa.com/serendipity/sr/browse?q=" + mQueryString + "&wt=json&rows=100";

                getContents(url);
            }
        });

        fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent intent = new Intent(BubbleDisplay.this, MainActivity.class);
                intent.putExtra(QUERY_BUBBLE, mQueryString);
                startActivity(intent);
            }
        });
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
                    alertUserAboutError();
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
                    } catch (IOException | JSONException e) {
                        Log.e("ERROR", "Exception caught: ", e);
                    }
                }
            });
        } else {
            Toast.makeText(this, "Network is unavailable!", Toast.LENGTH_LONG).show();
            alertUserAboutError();
        }
    }

    private void alertUserAboutError() {
        AlertDialogFragment dialog = new AlertDialogFragment();
        dialog.show(getFragmentManager(), "error_dialog");
    }

    private void getQuery() {
        mQueryString = searchBar.getText().toString();
    }

    private void updateDisplay() {
        //Queue time message
        queueTime.setText(DocData.getQueueTime());

        int row = 0;
        int col = 0;

        int total = mDocs.size();
        int column = 3;
        int rows = total / column;

        Point size = new Point();
        getWindowManager().getDefaultDisplay().getSize(size);
        int screenWidth = size.x;
        int thirdScreenWidth = (int) (screenWidth * 0.33);

        GridLayout insertPoint = (GridLayout) findViewById(R.id.bubbleGrid);
        insertPoint.removeAllViews();

        insertPoint.setColumnCount(column);
        insertPoint.setRowCount(rows + 1);

        for (DocData doc : mDocs) {
            LayoutInflater vi = (LayoutInflater) getApplicationContext().getSystemService(Context.LAYOUT_INFLATER_SERVICE);
            View view = vi.inflate(R.layout.bubble_template, null);

            final CircularImageView bubbleImage = (CircularImageView) view.findViewById(R.id.bubble_image);
            mBubblePB = (ProgressBar) view.findViewById(R.id.bubble_pb);

            bubbleImage.getLayoutParams().height = (thirdScreenWidth - 12);
            bubbleImage.getLayoutParams().width = (thirdScreenWidth - 12);

            //final String hashTags = getDocHashTags(doc);
            final List<String> hashTagList = doc.getHashTags();
            final String imageUri = doc.getImageUri();

            bubbleImage.setOnTouchListener(new View.OnTouchListener() {

                @Override
                public boolean onTouch(View v, MotionEvent event) {
                    if (event.getAction() == MotionEvent.ACTION_DOWN) {

                        // Get current time in nano seconds.
                        long pressTime = System.currentTimeMillis();


                        // If double click...
                        if (pressTime - lastPressTime <= DOUBLE_PRESS_INTERVAL) {
                            //Set current image Uri
                            mFullImageUri = imageUri;

                            //Toast.makeText(getApplicationContext(), "Double Click Event", Toast.LENGTH_SHORT).show();
                            //Toast.makeText(BubbleDisplay.this, hashTags, Toast.LENGTH_LONG).show();
                            mHasDoubleClicked = true;

                            //Populate card view
                            HashTagAdapter hashTagAdapter = new HashTagAdapter(BubbleDisplay.this, hashTagList);
                            mListView.setAdapter(hashTagAdapter);

                            mBackgroundCard.setVisibility(View.VISIBLE);
                        }
                        lastPressTime = pressTime;

                        bubbleImage.setBorderColor(Color.parseColor("#4CAF50"));

                        Runnable colorTask = new Runnable() {
                            @Override
                            public void run() {
                                bubbleImage.setBorderColor(Color.WHITE);
                            }
                        };
                        worker.schedule(colorTask, 500, TimeUnit.MILLISECONDS);
                    }
                    if (event.getAction() == MotionEvent.ACTION_UP) {
                        bubbleImage.setBorderColor(Color.WHITE);
                    }
                    return true;
                }

            });

            displayImage(doc.getImageUri(), bubbleImage);

            //TEST
            GridLayout.LayoutParams params = new GridLayout.LayoutParams();
            params.height = GridLayout.LayoutParams.WRAP_CONTENT;
            params.width = thirdScreenWidth - 12;
            switch (col) {
                case 1:
                    params.leftMargin = 6;
                    break;
                case 2:
                    params.leftMargin = 6;
                    params.rightMargin = 6;
                    break;
                case 3:
                    params.rightMargin = 6;
                    break;
                default:
            }
            params.topMargin = 3;
            params.bottomMargin = 3;
            params.setGravity(Gravity.CENTER);
            params.columnSpec = GridLayout.spec(col);
            params.rowSpec = GridLayout.spec(row);
            view.setLayoutParams(params);

            insertPoint.addView(view);

            //rows and columns
            if (col == 2) {
                col = 0;
                row++;
            } else {
                col++;
            }
        }
    }


    private String getDocHashTags(DocData doc) {
        String hashTags = "";
        for (String hashTag : doc.getHashTags()) {
            hashTags += String.format("#%s ", hashTag);
        }

        return hashTags;
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
            data.setImageUri(element.getString("image"));

            JSONArray hashTags = element.getJSONArray("hash_tags");
            for (int j = 0; j < hashTags.length(); j++) {
                data.getHashTags().add(hashTags.getString(j));
            }

            if (data.getImageUri().length() > 1) {
                mDocs.add(data);
            }
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


    public void displayImage(String imageUri, ImageView imageView) {
        final ProgressBar imageProgressBar = mBubblePB;

        ImageLoader imageLoader = ImageLoader.getInstance();


        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .showImageOnFail(getResources().getDrawable(R.drawable.image_missing))
                .build();

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

    public void displayFullImage(String imageUri, ImageView imageView) {
        final ProgressBar imageProgressBar = (ProgressBar) findViewById(R.id.fullScreenImageProgressBar);

        ImageLoader imageLoader = ImageLoader.getInstance();


        DisplayImageOptions options = new DisplayImageOptions.Builder().cacheInMemory(true)
                .showImageOnFail(getResources().getDrawable(R.drawable.image_missing))
                .build();

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

    public void changeVisibility(View view) {
        mBackgroundCard.setVisibility(View.INVISIBLE);
    }
}
