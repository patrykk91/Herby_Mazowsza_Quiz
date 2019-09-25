package pl.curlycode.herbymazowszaquiz;

import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.ActivityInfo;
import android.content.res.Configuration;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;

import java.util.Set;

public class MainActivity extends AppCompatActivity {

    public static final String CHOICES  = "pref_number_of_choices";
    public static final String REGIONS = "pref_RegionsToInclude";

    private boolean phoneDevice = true;

    private boolean preferencesChange = true;



    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        PreferenceManager.setDefaultValues(this, R.xml.preferences,false);
        PreferenceManager.getDefaultSharedPreferences(this).registerOnSharedPreferenceChangeListener(preferenceChangeListener);

        int screenSize = getResources().getConfiguration().screenLayout & Configuration.SCREENLAYOUT_SIZE_XLARGE;

        if (screenSize == Configuration.SCREENLAYOUT_SIZE_XLARGE || screenSize == Configuration.SCREENLAYOUT_SIZE_LARGE) {
            phoneDevice = false;
        }

        if (phoneDevice) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
            MainActivityFragment quizFragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.quizFragment);
            quizFragment.updateGuessRows(PreferenceManager.getDefaultSharedPreferences(this));
            quizFragment.updateRegions(PreferenceManager.getDefaultSharedPreferences(this));
            quizFragment.resetQuiz();
            preferencesChange = false;
        }
    }



    @Override
    protected void onStart() {
        super.onStart();

        if (preferencesChange) {

        }
    }


    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        int orientation = getResources().getConfiguration().orientation;

        if (orientation == Configuration.ORIENTATION_PORTRAIT) {
            // Inflate the menu; this adds items to the action bar if it is present.
            getMenuInflater().inflate(R.menu.menu_main, menu);
            return true;
        } else {
            return false;
        }
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {

        Intent preferencesIntent = new Intent(this, SettingsActivity.class);
        startActivity(preferencesIntent);

        return super.onOptionsItemSelected(item);
    }

        private SharedPreferences.OnSharedPreferenceChangeListener preferenceChangeListener = new SharedPreferences.OnSharedPreferenceChangeListener() {
            @Override
            public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {

                preferencesChange = true;

                MainActivityFragment quizFragment = (MainActivityFragment) getSupportFragmentManager().findFragmentById(R.id.quizFragment);

                if (key.equals(CHOICES)) {
                    quizFragment.updateGuessRows(sharedPreferences);

                    quizFragment.resetQuiz();

                } else if (key.equals(REGIONS)) {

                    Set<String> regions = sharedPreferences.getStringSet(REGIONS, null);

                    if (regions != null && regions.size() > 0) {
                        quizFragment.updateRegions(sharedPreferences);
                        quizFragment.resetQuiz();
                    } else {
                        SharedPreferences.Editor editor = sharedPreferences.edit();
                        regions.add(getString(R.string.default_region));
                        editor.putStringSet(REGIONS,null);
                        editor.apply();

                        Toast.makeText(MainActivity.this, R.string.default_region_message, Toast.LENGTH_SHORT).show();
                    }
                    Toast.makeText(MainActivity.this,R.string.restarting_quiz, Toast.LENGTH_SHORT).show();
                }
            }
        };
}
