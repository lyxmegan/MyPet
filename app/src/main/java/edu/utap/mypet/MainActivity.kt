package edu.utap.mypet


import android.app.Activity
import android.app.AlarmManager
import android.app.PendingIntent
import android.content.Context
import android.content.Intent
import android.graphics.Typeface
import android.os.Bundle
import android.provider.MediaStore
import android.text.Layout
import android.util.Log
import android.view.Gravity
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.LinearLayout
import android.widget.TextView
import android.widget.Toast
import androidx.activity.viewModels
import androidx.appcompat.app.ActionBar
import androidx.appcompat.app.AppCompatActivity
import androidx.appcompat.widget.Toolbar
import androidx.lifecycle.Observer
import com.facebook.FacebookSdk
import com.firebase.ui.auth.AuthUI
import com.firebase.ui.auth.IdpResponse
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.firebase.auth.FirebaseAuth
import edu.utap.mypet.funFacts.FactsFragment
import edu.utap.mypet.petEdit.PetsFragment
import edu.utap.mypet.reminders.ReminderFragment
import edu.utap.mypet.todoList.TodoFragment
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity() {
    companion object {
        const val RC_SIGN_IN = 123
        const val cameraRC = 10
    }

    private val viewModel: MainViewModel by viewModels()
    fun hideKeyboard() {
        val imm = getSystemService(AppCompatActivity.INPUT_METHOD_SERVICE) as InputMethodManager
        imm.hideSoftInputFromWindow(window.decorView.rootView.windowToken, 0);
    }

    private fun initUserUI() {
        viewModel.observeFirebaseAuthLiveData().observe(this, Observer {
            if( it == null ) {
                Log.d(javaClass.simpleName, "No one is signed in")
            } else {
                Log.d(javaClass.simpleName, "${it.displayName} ${it.email} ${it.uid} signed in")
            }
        })
    }

    private fun setTitle(newTitle: String) {
       setSupportActionBar(toolbar)
        supportActionBar?.apply {
            actionBarTitle.text = newTitle
        }
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        FacebookSdk.setApplicationId(getString(R.string.facebook_app_id))
        FacebookSdk.sdkInitialize(applicationContext)
        setContentView(R.layout.activity_main)
        createSignInIntent()
        initUserUI()
        val toolbar: Toolbar = findViewById(R.id.toolbar)
        setSupportActionBar(toolbar)
        viewModel.observeTitle().observe(this, Observer { newTitle ->
            setTitle(newTitle)
        })

        viewModel.setPhotoIntent(::takePictureIntent)

        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.setOnNavigationItemSelectedListener { menuItem ->
            // This line changes the selected icon
            menuItem.isChecked = true
            when (menuItem.itemId) {
                R.id.navigation_pets -> {
                    supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.main_frame, PetsFragment.newInstance(), "petFrag")
                            .commitNow()


                }
                R.id.navigation_todo -> {
                    supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.main_frame, TodoFragment.newInstance())
                            .commitNow()

                }
                R.id.navigation_reminders -> {
                    supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.main_frame, ReminderFragment.newInstance())
                            .commitNow()


                }
                R.id.navigation_facts -> {
                    supportFragmentManager
                            .beginTransaction()
                            .replace(R.id.main_frame, FactsFragment.newInstance())
                            .commitNow()

                }
            }
            false
        }

        supportFragmentManager
                .beginTransaction()
                .replace(R.id.main_frame, PetsFragment.newInstance(), "petFrag")
                .commitNow()
    }

    private fun takePictureIntent() {
        Intent(MediaStore.ACTION_IMAGE_CAPTURE).also { takePictureIntent ->
            takePictureIntent.putExtra(MediaStore.EXTRA_OUTPUT, viewModel.getPhotoURI())
            startActivityForResult(takePictureIntent, cameraRC)
        }
        Log.d(javaClass.simpleName, "takePhotoIntent")
    }

    fun setBottomNavigationVisibility(visibility: Int) {
        // get the reference of the bottomNavigationView and set the visibility.
        val navView: BottomNavigationView = findViewById(R.id.nav_view)
        navView.visibility = visibility

    }

    /*fun cancelAlarm() {
        val alarmManager = getSystemService(Context.ALARM_SERVICE) as AlarmManager
        val intent = Intent(this, AlarmReceiver::class.java)
        val pendingIntent = PendingIntent.getBroadcast(this, 0, intent, 0)
        alarmManager.cancel(pendingIntent)
    }*/

    private fun createSignInIntent(){
        val providers = arrayListOf(
                AuthUI.IdpConfig.EmailBuilder().build(),
                AuthUI.IdpConfig.FacebookBuilder().build(),
                AuthUI.IdpConfig.GoogleBuilder().build()
        )
        startActivityForResult(
                AuthUI.getInstance()
                        .createSignInIntentBuilder()
                        .setAvailableProviders(providers)
                        .setIsSmartLockEnabled(false)
                        //.setTheme(R.style.LoginUIStyle)
                        .build(),
                RC_SIGN_IN
        )
    }

    private fun signOut() {
        // [START auth_sign_out]
        AuthUI.getInstance()
                .signOut(this)
                .addOnCompleteListener {
                    // ...
                }
        // [END auth_sign_out]
    }
    override fun onActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
        super.onActivityResult(requestCode, resultCode, data)
        //auth
        if (requestCode == RC_SIGN_IN) {
            val response = IdpResponse.fromResultIntent(data)

            if (resultCode == Activity.RESULT_OK) {
                // Successfully signed in
                val user = FirebaseAuth.getInstance().currentUser
                if (user != null) {
                    val userName = user.displayName
                    val id = user.uid
                    //Toast.makeText(applicationContext, "${user.displayName}, ${user.uid} sign in ", Toast.LENGTH_LONG).show()
                    //Log.d(javaClass.simpleName, "current user name$userName, uid: $id")
                }
            } else {
                if (response != null) {
                    response.getError()?.getErrorCode()
                }
                // ...
            }
        }

        if(requestCode == cameraRC) {
            if(resultCode == RESULT_OK) {
                viewModel.pictureSuccess()
            } else {
                viewModel.pictureFailure()
                setBottomNavigationVisibility(View.GONE)
            }
        }


    }
    override fun onCreateOptionsMenu(menu: Menu): Boolean {
        // Inflate the menu; this adds items to the action bar if it is present.
        menuInflater.inflate(R.menu.menu_main, menu)
        return true
    }
    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        // Handle action bar item clicks here.
        val id = item.itemId
        return if (id == R.id.logout) {
            signOut()
            val logoutIntent = Intent(this, MainActivity::class.java)
            logoutIntent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(logoutIntent)
            true
        } else super.onOptionsItemSelected(item)

    }


}





