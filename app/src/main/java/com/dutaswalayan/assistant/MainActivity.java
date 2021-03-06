package com.dutaswalayan.assistant;

import android.accounts.Account;
import android.app.LoaderManager;
import android.content.ContentResolver;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.content.SyncStatusObserver;
import android.os.Bundle;
import android.support.design.widget.FloatingActionButton;
import android.support.design.widget.Snackbar;
import android.support.v4.widget.SimpleCursorAdapter;
import android.support.v7.widget.SearchView;
import android.view.View;
import android.support.design.widget.NavigationView;
import android.support.v4.view.GravityCompat;
import android.support.v4.widget.DrawerLayout;
import android.support.v7.app.ActionBarDrawerToggle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.Toolbar;
import android.view.Menu;
import android.view.MenuItem;
import android.database.Cursor;
import android.widget.AdapterView;
import android.widget.FilterQueryProvider;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import com.dutaswalayan.assistant.contract.FeedContract;
import com.dutaswalayan.assistant.sync.GenericAccountService;
import com.dutaswalayan.assistant.sync.SyncUtils;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = "MainActivity";

    private ListView mListView;
    private SearchView searchView;
    private MenuItem refreshItem;
    /**
     * Cursor adapter for controlling ListView results.
     */
    private SimpleCursorAdapter mAdapter;

    /**
     * Handle to a SyncObserver. The ProgressBar element is visible until the SyncObserver reports
     * that the sync is complete.
     *
     * <p>This allows us to delete our SyncObserver once the application is no longer in the
     * foreground.
     */
    private Object mSyncObserverHandle;

    /**
     * Projection for querying the content provider.
     */
    private static final String[] PROJECTION = new String[]{
            FeedContract.Product._ID,
            FeedContract.Product.COLUMN_PRODUCT_ID,
            FeedContract.Product.COLUMN_DESCRIPTION,
            FeedContract.Product.COLUMN_UNIT,
            FeedContract.Product.COLUMN_PRICE,
            FeedContract.Product.COLUMN_BARCODE,
            FeedContract.Product.COLUMN_UPDATED};

    // Column indexes. The index of a column in the Cursor is the same as its relative position in
    // the projection.
    /** Column index for _ID */
    public static final int COLUMN_ID = 0;
    /** Column index for PID */
    public static final int COLUMN_PRODUCT_ID = 1;
    /** Column index for description */
    public static final int COLUMN_DESCRIPTION = 2;
    /** Column index for unit */
    public static final int COLUMN_UNIT = 3;
    /** Column index for price */
    public static final int COLUMN_PRICE = 4;
    /** Column index for barcode */
    public static final int COLUMN_BARCODE = 5;
    /** Column index for updated */
    public static final int COLUMN_UPDATED = 6;

    /**
     * List of Cursor columns to read from when preparing an adapter to populate the ListView.
     */
    private static final String[] FROM_COLUMNS = new String[]{
            FeedContract.Product.COLUMN_DESCRIPTION,
            FeedContract.Product.COLUMN_PRICE
    };

    /**
     * List of Views which will be populated by Cursor data.
     */
    private static final int[] TO_FIELDS = new int[]{
            android.R.id.text1,
            android.R.id.text2};

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        SyncUtils.CreateSyncAccount(this);

        setContentView(R.layout.activity_main);
        Toolbar toolbar = (Toolbar) findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        FloatingActionButton fab = (FloatingActionButton) findViewById(R.id.fab);
        fab.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                Intent i = new Intent(MainActivity.this, TransactionActivity.class);
                startActivity(i);
            }
        });

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        ActionBarDrawerToggle toggle = new ActionBarDrawerToggle(
                this, drawer, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawer.addDrawerListener(toggle);
        toggle.syncState();

        NavigationView navigationView = (NavigationView) findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        navigationView.getMenu().getItem(0).setChecked(true);

        mListView = (ListView) findViewById(R.id.listView);

        mAdapter = new SimpleCursorAdapter(
                this,       // Current context
                android.R.layout.simple_list_item_activated_2,  // Layout for individual rows
                null,                // Cursor
                FROM_COLUMNS,        // Cursor columns to use
                TO_FIELDS,           // Layout fields to use
                0                    // No flags
        );

        mAdapter.setViewBinder(new SimpleCursorAdapter.ViewBinder() {
            @Override
            public boolean setViewValue(View view, Cursor cursor, int i) {
                if (i == COLUMN_PRICE) {
                    ((TextView) view).setText(String.format("%,d",cursor.getLong(i)));
                    return true;
                } else {
                    return false;
                }
            }
        });

        mListView.setAdapter(mAdapter);
        mListView.setTextFilterEnabled(true);
        mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
            @Override
            public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor c = (Cursor) mAdapter.getItem(position);

                String pid = c.getString(COLUMN_PRODUCT_ID);
                String description = c.getString(COLUMN_DESCRIPTION);
                String barcode = c.getString(COLUMN_BARCODE);
                String unit = c.getString(COLUMN_UNIT);
                long price = c.getLong(COLUMN_PRICE);
                String updated = c.getString(COLUMN_UPDATED);

                Intent i = new Intent(MainActivity.this, DetailProduct.class);
                i.putExtra("Products.PID",pid);
                i.putExtra("Products.DESCRIPTION",description);
                i.putExtra("Products.BARCODE",barcode);
                i.putExtra("Products.UNIT",unit);
                i.putExtra("Products.PRICE",price);
                i.putExtra("Products.UPDATED",updated);
                startActivity(i);
            }
        });

        mAdapter.setFilterQueryProvider(new FilterQueryProvider() {
            @Override
            public Cursor runQuery(CharSequence constraint) {
                final ContentResolver contentResolver = getContentResolver();
                Cursor c = contentResolver.query(FeedContract.Product.CONTENT_URI,
                        PROJECTION,
                        FeedContract.Product.COLUMN_DESCRIPTION + " LIKE ?",
                        new String[]{"%" + constraint.toString() + "%"},
                        FeedContract.Product.COLUMN_DESCRIPTION + " asc"); // Sort
                return c;
            }
        });
        getLoaderManager().initLoader(0, null, this);
    }


    @Override
    public void onResume() {
        super.onResume();
        mSyncStatusObserver.onStatusChanged(0);

        // Watch for sync state changes
        final int mask = ContentResolver.SYNC_OBSERVER_TYPE_PENDING |
                ContentResolver.SYNC_OBSERVER_TYPE_ACTIVE;
        mSyncObserverHandle = ContentResolver.addStatusChangeListener(mask, mSyncStatusObserver);
    }

    @Override
    public void onPause() {
        super.onPause();
        if (mSyncObserverHandle != null) {
            ContentResolver.removeStatusChangeListener(mSyncObserverHandle);
            mSyncObserverHandle = null;
        }
    }

    @Override
    public void onBackPressed() {
        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        if (drawer.isDrawerOpen(GravityCompat.START)) {
            drawer.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
        }
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.menu_main, menu);
        refreshItem = menu.findItem(R.id.menu_refresh);
        searchView = (SearchView) menu.findItem(R.id.search).getActionView();
        searchView.setOnQueryTextListener(new SearchView.OnQueryTextListener() {
            @Override
            public boolean onQueryTextSubmit(String query) {
                return false;
            }

            @Override
            public boolean onQueryTextChange(String newText) {
                mAdapter.getFilter().filter(newText);
                return true;
            }
        });

        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();

        if (id == R.id.menu_refresh) {
            SyncUtils.TriggerRefresh();
        }

        return super.onOptionsItemSelected(item);
    }

    @SuppressWarnings("StatementWithEmptyBody")
    @Override
    public boolean onNavigationItemSelected(MenuItem item) {
        // Handle navigation view item clicks here.
        int id = item.getItemId();

        if (id == R.id.nav_products) {

        } else if (id == R.id.nav_transaction) {
            Intent i = new Intent(this, TransactionActivity.class);
            startActivity(i);
        } else if (id == R.id.nav_send) {
            //send email...
            Intent i = new Intent(Intent.ACTION_SEND);
            i.setType("message/rfc822");
            i.putExtra(Intent.EXTRA_EMAIL  , new String[]{"kritik.saran@dutaswalayan.com"});
            i.putExtra(Intent.EXTRA_SUBJECT, "Kritik dan saran");
            i.putExtra(Intent.EXTRA_TEXT   , "SARAN SAYA\n");
            try {
                startActivity(Intent.createChooser(i, "Send mail..."));
            } catch (android.content.ActivityNotFoundException ex) {
                Toast.makeText(MainActivity.this, "There are no email clients installed.", Toast.LENGTH_SHORT).show();
            }
        }

        DrawerLayout drawer = (DrawerLayout) findViewById(R.id.drawer_layout);
        drawer.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, Bundle args) {
        // We only have one loader, so we can ignore the value of i.
        // (It'll be '0', as set in onCreate().)
        return new CursorLoader(this,  // Context
                FeedContract.Product.CONTENT_URI, // URI
                PROJECTION,                // Projection
                null,                           // Selection
                null,                           // Selection args
                FeedContract.Product.COLUMN_DESCRIPTION + " asc"); // Sort
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mAdapter.swapCursor(data);
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mAdapter.swapCursor(null);
    }


    /**
     * Set the state of the Refresh button. If a sync is active, turn on the ProgressBar widget.
     * Otherwise, turn it off.
     *
     * @param refreshing True if an active sync is occuring, false otherwise
     */
    public void setRefreshActionButtonState(boolean refreshing) {
        if (refreshItem != null) {
            if (refreshing) {
                refreshItem.setActionView(R.layout.actionbar_indeterminate_progress);
            } else {
                refreshItem.setActionView(null);
            }
        }
    }

    /**
     * Crfate a new anonymous SyncStatusObserver. It's attached to the app's ContentResolver in
     * onResume(), and removed in onPause(). If status changes, it sets the state of the Refresh
     * button. If a sync is active or pending, the Refresh button is replaced by an indeterminate
     * ProgressBar; otherwise, the button itself is displayed.
     */
    private SyncStatusObserver mSyncStatusObserver = new SyncStatusObserver() {
        /** Callback invoked with the sync adapter status changes. */
        @Override
        public void onStatusChanged(int which) {
            MainActivity.this.runOnUiThread(new Runnable() {
                /**
                 * The SyncAdapter runs on a background thread. To update the UI, onStatusChanged()
                 * runs on the UI thread.
                 */
                @Override
                public void run() {
                    // Create a handle to the account that was created by
                    // SyncService.CreateSyncAccount(). This will be used to query the system to
                    // see how the sync status has changed.
                    Account account = GenericAccountService.GetAccount(SyncUtils.ACCOUNT_TYPE);
                    if (account == null) {
                        // GetAccount() returned an invalid value. This shouldn't happen, but
                        // we'll set the status to "not refreshing".
                        setRefreshActionButtonState(false);
                        return;
                    }

                    // Test the ContentResolver to see if the sync adapter is active or pending.
                    // Set the state of the refresh button accordingly.
                    boolean syncActive = ContentResolver.isSyncActive(
                            account, FeedContract.CONTENT_AUTHORITY);
                    boolean syncPending = ContentResolver.isSyncPending(
                            account, FeedContract.CONTENT_AUTHORITY);
                    setRefreshActionButtonState(syncActive || syncPending);
                }
            });
        }
    };

}
