package kr.perl.android.logviewer.provider;

import android.content.SearchRecentSuggestionsProvider;

public class SearchHistoryProvider extends SearchRecentSuggestionsProvider {
	public final static String AUTHORITY = "kr.perl.provider.LogViewer.SearchHistory";
    public final static int MODE = DATABASE_MODE_QUERIES;

    public SearchHistoryProvider() {
        setupSuggestions(AUTHORITY, MODE);
    }	
}
