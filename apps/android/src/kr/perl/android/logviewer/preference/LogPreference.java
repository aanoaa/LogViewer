package kr.perl.android.logviewer.preference;

import java.util.Map;

import kr.perl.android.logviewer.R;
import android.content.SharedPreferences;
import android.content.SharedPreferences.OnSharedPreferenceChangeListener;
import android.os.Bundle;
import android.preference.EditTextPreference;
import android.preference.ListPreference;
import android.preference.Preference;
import android.preference.PreferenceActivity;

public class LogPreference extends PreferenceActivity implements OnSharedPreferenceChangeListener {
	@Override
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        addPreferencesFromResource(R.xml.preferences);
        init();
    }
	
	@Override
    protected void onResume() {
        super.onResume();
        getPreferenceScreen().getSharedPreferences().registerOnSharedPreferenceChangeListener(this);
    }
	
	@Override
    protected void onPause() {
        super.onPause();
        getPreferenceScreen().getSharedPreferences().unregisterOnSharedPreferenceChangeListener(this);
    }
	
	@Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
		Preference pref = findPreference(key);
		setSummary(pref);
    }
	
	private void setSummary(Preference pref) {
		if (pref instanceof ListPreference) {
            pref.setSummary(((ListPreference) pref).getEntry());
        } else if (pref instanceof EditTextPreference) {
        	pref.setSummary(((EditTextPreference) pref).getText());
        }
	}
	
	private void init() {
		Map<String, ?> map = getPreferenceScreen().getSharedPreferences().getAll();
		for (String key : map.keySet()) {
			setSummary(findPreference(key));
		}
	}
}