package tk.kvakva.takemypictures

import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.util.Log
import android.view.*
import android.widget.ImageView
import android.widget.ScrollView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.core.view.*
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
import com.squareup.picasso.Callback
import com.squareup.picasso.OkHttp3Downloader
import com.squareup.picasso.Picasso
import okhttp3.OkHttpClient
//import okhttp3.logging.HttpLoggingInterceptor
import androidx.preference.PreferenceFragmentCompat
import androidx.preference.PreferenceManager

private const val TAG = "MainActivity"
private const val FR_ID = "FR_ID"

class MainActivity : AppCompatActivity(), SwipeRefreshLayout.OnRefreshListener {

    private lateinit var mDetector: GestureDetectorCompat
    private lateinit var mPrFrag: PreferenceFragmentCompat

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        mDetector = GestureDetectorCompat(this, MyGesLsnr())
        findViewById<ConstraintLayout>(R.id.constrLay)

        if (supportFragmentManager.findFragmentByTag(FR_ID) == null) {
            mPrFrag = MySettingsFragment()
            Log.d(
                TAG,
                "onCreate: create mPrFrag supportFragmentManager.findFragmentByTag(FR_ID)=${
                    supportFragmentManager.findFragmentByTag(FR_ID)
                }"
            )
        } else {
            Log.d(
                TAG,
                "onCreate: supportFragmentManager.findFragmentByTag(FR_ID)=${
                    supportFragmentManager.findFragmentByTag(FR_ID)
                }"
            )
            mPrFrag = supportFragmentManager.findFragmentByTag(FR_ID) as PreferenceFragmentCompat
            findViewById<ScrollView>(R.id.scrlv).visibility = View.GONE
        }
        findViewById<SwipeRefreshLayout>(R.id.swprfrshlt).setOnRefreshListener(this)
    }


    /**
     * Called when a touch screen event was not handled by any of the views
     * under it.  This is most useful to process touch events that happen
     * outside of your window bounds, where there is no view to receive it.
     *
     * @param event The touch screen event being processed.
     *
     * @return Return true if you have consumed the event, false if you haven't.
     * The default implementation always returns false.
     */
    override fun onTouchEvent(event: MotionEvent?): Boolean {
        mDetector.onTouchEvent(event)
        return super.onTouchEvent(event)
    }

    /**
     * Initialize the contents of the Activity's standard options menu.  You
     * should place your menu items in to <var>menu</var>.
     *
     *
     * This is only called once, the first time the options menu is
     * displayed.  To update the menu every time it is displayed, see
     * [.onPrepareOptionsMenu].
     *
     *
     * The default implementation populates the menu with standard system
     * menu items.  These are placed in the [Menu.CATEGORY_SYSTEM] group so that
     * they will be correctly ordered with application-defined menu items.
     * Deriving classes should always call through to the base implementation.
     *
     *
     * You can safely hold on to <var>menu</var> (and any items created
     * from it), making modifications to it as desired, until the next
     * time onCreateOptionsMenu() is called.
     *
     *
     * When you add items to the menu, you can implement the Activity's
     * [.onOptionsItemSelected] method to handle them there.
     *
     * @param menu The options menu in which you place your items.
     *
     * @return You must return true for the menu to be displayed;
     * if you return false it will not be shown.
     *
     * @see .onPrepareOptionsMenu
     *
     * @see .onOptionsItemSelected
     */
    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        val inflater: MenuInflater = menuInflater
        inflater.inflate(R.menu.opmenu, menu)
        return true
    }

    /**
     * This hook is called whenever an item in your options menu is selected.
     * The default implementation simply returns false to have the normal
     * processing happen (calling the item's Runnable or sending a message to
     * its Handler as appropriate).  You can use this method for any items
     * for which you would like to do processing without those other
     * facilities.
     *
     *
     * Derived classes should call through to the base class for it to
     * perform the default menu handling.
     *
     * @param item The menu item that was selected.
     *
     * @return boolean Return false to allow normal menu processing to
     * proceed, true to consume it here.
     *
     * @see .onCreateOptionsMenu
     */
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        Log.d(TAG, "onOptionsItemSelected:  ${findViewById<ScrollView>(R.id.scrlv).visibility}")
        // Handle item selection
        return when (item.itemId) {
            R.id.openprefragment -> {
                if (findViewById<ScrollView>(R.id.scrlv).visibility == View.VISIBLE) {
                    Log.d(TAG, "onOptionsItemSelected: (R.id.scrlv).visibility==View.VISIBLE")
/*                supportFragmentManager
                    .beginTransaction()
                    .add(R.id.constrLay, MySettingsFragment())
                    .addToBackStack(null)
                    .commit()*/
                    supportFragmentManager
                        .beginTransaction()
                        .add(R.id.constrLay, mPrFrag, FR_ID)
                        .commit()
                    findViewById<ScrollView>(R.id.scrlv).visibility = View.GONE
                    true
                } else {
                    Log.d(TAG, "onOptionsItemSelected: (R.id.scrlv).visibility==View.NOT VISIBLE")
                    supportFragmentManager
                        .beginTransaction()
                        .remove(mPrFrag)
                        .commit()
                    findViewById<ScrollView>(R.id.scrlv).visibility = View.VISIBLE
                    true
                }
            }
 /*           R.id.ipcamurl -> {
                supportFragmentManager
                    .beginTransaction()
                    .hide(mPrFrag)
                    .commit()
                findViewById<ScrollView>(R.id.scrlv).visibility = View.VISIBLE
                true
            }*/
            else -> super.onOptionsItemSelected(item)
        }

    }

    override fun onRefresh() {
        val dlinkiv = findViewById<ImageView>(R.id.ipcimg)
        val wecamiv = findViewById<ImageView>(R.id.wvimg)

        val p = Picasso.Builder(this)
            //.loggingEnabled(true)
            //.indicatorsEnabled(true)
            .downloader(
                OkHttp3Downloader(
                    OkHttpClient.Builder()
                        //.addInterceptor(HttpLoggingInterceptor().setLevel(HttpLoggingInterceptor.Level.HEADERS))
                        .build()
                )
            )
            .build()


        val preferenceManager= PreferenceManager.getDefaultSharedPreferences(this)
        val wurl=preferenceManager.getString(resources.getString(R.string.wcamurl),"https://localhost/w.jpg")
        val iurl=preferenceManager.getString(resources.getString(R.string.ipcamurl),"https://localhost/i.jpg")

        p.load(wurl).into(wecamiv, SwipeToRefreshCallBack())
        p.load(iurl).into(dlinkiv)
    }

    inner class SwipeToRefreshCallBack : Callback {
        override fun onSuccess() {
            findViewById<SwipeRefreshLayout>(R.id.swprfrshlt).isRefreshing = false
        }

        override fun onError(e: Exception?) {
            findViewById<SwipeRefreshLayout>(R.id.swprfrshlt).isRefreshing = false
        }
    }

    inner class MyGesLsnr : GestureDetector.SimpleOnGestureListener() {

        override fun onDoubleTap(e: MotionEvent?): Boolean {

            if (supportActionBar?.isShowing == true) {
                supportActionBar?.hide()
                ViewCompat.getWindowInsetsController(this@MainActivity.findViewById(R.id.constrLay))
                    ?.hide(
                        WindowInsetsCompat.Type.statusBars() or
                                WindowInsetsCompat.Type.systemBars() or
                                WindowInsetsCompat.Type.navigationBars()
                    )
                ViewCompat.getWindowInsetsController(this@MainActivity.findViewById(R.id.constrLay))
                    ?.systemBarsBehavior =
                    WindowInsetsControllerCompat.BEHAVIOR_SHOW_TRANSIENT_BARS_BY_SWIPE
            } else {
                supportActionBar?.show()
                ViewCompat.getWindowInsetsController(this@MainActivity.findViewById(R.id.constrLay))
                    ?.show(
                        WindowInsetsCompat.Type.statusBars() or
                                WindowInsetsCompat.Type.systemBars() or
                                WindowInsetsCompat.Type.navigationBars()
                    )
            }

            Log.v("oiuoiuoi", "lkjlkjlkjlkjlkjlkjlk d tap")
            return super.onDoubleTap(e)
        }
    }

    /**
     * Called to process touch screen events.  You can override this to
     * intercept all touch screen events before they are dispatched to the
     * window.  Be sure to call this implementation for touch screen events
     * that should be handled normally.
     *
     * @param ev The touch screen event.
     *
     * @return boolean Return true if this event was consumed.
     */
    override fun dispatchTouchEvent(ev: MotionEvent?): Boolean {
        super.dispatchTouchEvent(ev)
        return mDetector.onTouchEvent(ev)


    }
}


class MySettingsFragment : PreferenceFragmentCompat() {
    override fun onCreatePreferences(savedInstanceState: Bundle?, rootKey: String?) {
        setPreferencesFromResource(R.xml.appprefs, rootKey)
    }
}
