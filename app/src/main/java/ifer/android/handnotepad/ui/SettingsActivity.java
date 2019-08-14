package ifer.android.handnotepad.ui;

import android.content.DialogInterface;
import android.content.SharedPreferences;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.widget.CheckBox;
import android.widget.EditText;

import ifer.android.handnotepad.AppController;
import ifer.android.handnotepad.R;
import ifer.android.handnotepad.ui.MainActivity;
import ifer.android.handnotepad.util.Constants;
import static ifer.android.handnotepad.util.AndroidUtils.*;
import static ifer.android.handnotepad.util.GenericUtils.isEmptyOrNull;


public class SettingsActivity extends AppCompatActivity {

    EditText prefServer;
    CheckBox prefAutoRefresh;

    private SharedPreferences settings;

    private String initial_server;
    private Boolean initial_autorefresh;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_settings);

        prefServer = (EditText) findViewById(R.id.pref_server);
        prefAutoRefresh = (CheckBox) findViewById(R.id.prefAutoRefresh);

        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        loadSettings();
    }

    private void loadSettings(){
        settings = getSharedPreferences(Constants.SETTINGS_NAME, 0);

        prefServer.setText(settings.getString(Constants.PrefServerKey, null));
        prefAutoRefresh.setChecked((settings.getBoolean(Constants.PrefAutoRefreshKey,false)));

        initial_server = prefServer.getText().toString();
        initial_autorefresh = prefAutoRefresh.isChecked();
    }

    private void saveSettings (){
        SharedPreferences.Editor editor = settings.edit();
        editor.putString(Constants.PrefServerKey, prefServer.getText().toString());
        editor.putBoolean(Constants.PrefAutoRefreshKey,  prefAutoRefresh.isChecked());
        editor.apply();
    }

    private boolean validateSettings(){
        if(isEmptyOrNull(prefServer.getText().toString()))
            return (false);

        return (true);
    }

    private boolean settingsChanged (){
        if ((!prefServer.getText().toString().equals(initial_server)) || (!prefAutoRefresh.isChecked() == initial_autorefresh) )
            return true;

        return (false);
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        switch (item.getItemId()) {
            case R.id.action_settings_save:
                if (validateSettings() == false){
                    showToastMessage(this, getString(R.string.error_empty_setting));
                    return true;
                }
                AppController.connectionEstablished = false;
                saveSettings();
                MainActivity.setupConnection(this);
                finish();
                return true;

            case R.id.action_settings_cancel:
                cancelEdit ();
                return true;

            case android.R.id.home:    //make toolbar home button behave like cancel, when in edit mode
                cancelEdit();
                return true;

            default:
                return super.onOptionsItemSelected(item);
        }
    }


    private void cancelEdit (){

        if (settingsChanged()){   //New visit or data changed
            showPopup(this, Popup.WARNING, getString(R.string.warn_not_saved),  new CancelPosAction(), new CancelNegAction());
        }
        else {                                          //Data not changed
            finish();
        }
    }

    //Make android back button behave like cancel, when in edit mode
    @Override
    public void onBackPressed() {
        cancelEdit();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.settings, menu);
        return true;
    }

    class CancelPosAction implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
            finish();
        }
    }

    class CancelNegAction implements DialogInterface.OnClickListener {
        @Override
        public void onClick(DialogInterface dialog, int which) {
        }
    }

}
