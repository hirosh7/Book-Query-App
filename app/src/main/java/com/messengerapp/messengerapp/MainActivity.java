package com.messengerapp.messengerapp;

import android.app.AlertDialog;
import android.app.ProgressDialog;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.v4.view.MenuItemCompat;
import android.support.v7.app.ActionBarActivity;
import android.support.v7.widget.ShareActionProvider;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.loopj.android.http.AsyncHttpClient;
import com.loopj.android.http.JsonHttpResponseHandler;

import org.apache.http.Header;
import org.json.JSONObject;

import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;


public class MainActivity extends ActionBarActivity implements View.OnClickListener,
        AdapterView.OnItemClickListener {

    TextView mainTextView;
    Button mainButton;
    EditText mainEditText;
    ListView mainListView;
    JSONAdaptor mJSONAdapter;
    ShareActionProvider mShareActionProvider;
    private static final String PREFS = "prefs";
    private static final String PREF_NAME = "name";
    private static final String QUERY_URL = "http://openlibrary.org/search.json?q=";
    SharedPreferences mSharedPreferences;
    ProgressDialog mDialog;

    @Override
    @SuppressWarnings("unchecked")
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // 1. Access the TextView defined in layout XML and then set its text
        mainTextView = (TextView) findViewById(R.id.main_textview);

        // 2. Access the Button defined in layout XML and listen for it here
        mainButton = (Button) findViewById(R.id.main_button);
        mainButton.setOnClickListener(this);

        // 3. Access the EditText defined in layout XML
        // New Feature code simulated in a comment
        mainEditText = (EditText) findViewById(R.id.main_edittext);

        // 4. Access the ListView
        mainListView = (ListView) findViewById(R.id.main_listview);

        // 5. Set this activity to react to list items being pressed
        mainListView.setOnItemClickListener(this);

        // 7. Greet the user, or ask for their name if new
        displayWelcome();

        // 10. Create a JSONAdapter for the ListView
        mJSONAdapter = new JSONAdaptor(this, getLayoutInflater());

        // Set the ListView to use the ArrayAdapter
        mainListView.setAdapter(mJSONAdapter);
    }

    public void displayWelcome() {
        // Access the device's key-value storage
        // MODE_PRIVATE specifies that data is only accessible by this app
        mSharedPreferences = getSharedPreferences(PREFS, MODE_PRIVATE);

        // Read the user's name,
        // or an empty string if nothing found (second param is the default)
        String name = mSharedPreferences.getString(PREF_NAME, "");

        if (name.length() > 0) {

            // If the name is valid, display a Toast welcoming them
            Toast.makeText(this, "Welcome back, " + name + "!", Toast.LENGTH_LONG).show();
        } else {

            // otherwise, show a dialog to ask for their name
            AlertDialog.Builder alert = new AlertDialog.Builder(this);
            alert.setTitle("Hello!");
            alert.setMessage("What is your name?");

            // Create EditText for entry
            final EditText input = new EditText(this);
            alert.setView(input);

            // Make an "OK" button to save the name
            alert.setPositiveButton("OK", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int whichButton) {

                    // Grab the EditText's input and capitalize first letter of name
                    String inputName = capitalizeFirstLetter(input.getText().toString());

                    // Put it into memory (don't forget to commit!)
                    SharedPreferences.Editor e = mSharedPreferences.edit();
                    e.putString(PREF_NAME, inputName);
                    e.apply();

                    // Welcome the new user
                    Toast.makeText(getApplicationContext(), "Welcome, " + inputName + "!", Toast.LENGTH_LONG).show();
                }
            });

            // Make a "Cancel" button
            // that simply dismisses the alert
            alert.setNegativeButton("Cancel", new DialogInterface.OnClickListener() {

                public void onClick(DialogInterface dialog, int whichButton) {}
            });

            alert.show();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu.
        // Adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);

        // Access the Share Item defined in menu XML
        MenuItem shareItem = menu.findItem(R.id.menu_item_share);

        // Access the object responsible for
        // putting together the sharing sub-menu
        if (shareItem != null) {
            mShareActionProvider = (ShareActionProvider) MenuItemCompat.getActionProvider(shareItem);
        }

        // Create an Intent to share your content
        setShareIntent();

        return true;
    }

    private void setShareIntent() {

        if (mShareActionProvider != null) {

            // create an Intent with the contents of the TextView
            Intent shareIntent = new Intent(Intent.ACTION_SEND);
            shareIntent.setType("text/plain");
            shareIntent.putExtra(Intent.EXTRA_SUBJECT, "Android Development");
            shareIntent.putExtra(Intent.EXTRA_TEXT, mainTextView.getText());

            // Make sure the provider knows
            // it should work with that Intent
            mShareActionProvider.setShareIntent(shareIntent);
        }
    }

    @Override
    @SuppressWarnings("unchecked")
    public void onClick(View v) {

        /// 9. Take what was typed into the EditText and use in search
        queryBooks(mainEditText.getText().toString());
    }

    @Override
    public void onItemClick(AdapterView<?> parent, View view, int position, long id) {

    }

    public String capitalizeFirstLetter(String original){
        if(original.length() == 0)
            return original;
        return original.substring(0, 1).toUpperCase() + original.substring(1);
    }

    private void queryBooks(String searchString) {

        // Prepare your search string to be put in a URL
        // It might have reserved characters or something
        String urlString = "";
        try {
            urlString = URLEncoder.encode(searchString, "UTF-8");
        } catch (UnsupportedEncodingException e) {

            // if this fails for some reason, let the user know why
            e.printStackTrace();
            Toast.makeText(this, "Error: " + e.getMessage(), Toast.LENGTH_LONG).show();
        }

        // Create a client to perform networking
        AsyncHttpClient client = new AsyncHttpClient();

        // Have the client get a JSONArray of data
        // and define how to respond
        client.get(QUERY_URL + urlString,
                new JsonHttpResponseHandler() {

                    @Override
                    public void onSuccess(int statusCode, Header[] headers, JSONObject jsonObject) {

                        // Display a "Toast" message
                        // to announce your success
                        Toast.makeText(getApplicationContext(), "Success!",
                                Toast.LENGTH_LONG).show();

                        // update the data in your custom method.
                        mJSONAdapter.updateData(jsonObject.optJSONArray("docs"));
                    }

                    @Override
                    public void onFailure(int statusCode, Header[] headers, Throwable throwable,
                                          JSONObject error) {

                        // Display a "Toast" message
                        // to announce the failure
                        Toast.makeText(getApplicationContext(), "Error: " + statusCode + " " +
                                throwable.getMessage(), Toast.LENGTH_LONG).show();

                        // Log error message
                        // to help solve any problems
                        Log.e("MessengerApp android", statusCode + " " + throwable.getMessage());
                    }
                });
    }
}
