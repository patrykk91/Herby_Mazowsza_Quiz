package pl.curlycode.herbymazowszaquiz;

import android.content.SharedPreferences;
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
