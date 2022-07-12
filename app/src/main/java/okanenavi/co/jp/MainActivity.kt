package okanenavi.co.jp

import android.content.Intent
import android.os.Bundle
import android.util.Log
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.ktx.auth
import com.google.firebase.ktx.Firebase
import okanenavi.co.jp.ui.home.HomeActivity

class MainActivity : AppCompatActivity() {
    companion object {
        private val TAG = MainActivity::class.java.simpleName
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        val currentUser = Firebase.auth.currentUser
        if (currentUser != null) {
            Log.d(TAG, "currentUser: ${currentUser.uid}")
            gotoHome()
        } else {
            Firebase.auth.signInAnonymously().addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    Log.d(TAG, "signed: ${task.result.user?.uid}")
                    gotoHome()
                } else {
                    Log.w(TAG, "signInAnonymously:failure", task.exception)
                }
            }
        }
    }

    private fun gotoHome() {
        val intent = Intent(this, HomeActivity::class.java)
        intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TASK or Intent.FLAG_ACTIVITY_NEW_TASK
        startActivity(intent)
    }
}