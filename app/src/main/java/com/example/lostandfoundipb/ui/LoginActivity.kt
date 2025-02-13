package com.example.lostandfoundipb.ui

import android.os.Bundle
import android.text.TextUtils
import android.view.View
import android.view.WindowManager
import androidx.appcompat.app.AppCompatActivity
import androidx.lifecycle.ViewModelProviders
import com.example.lostandfoundipb.R
import com.example.lostandfoundipb.Utils.SessionManagement
import com.example.lostandfoundipb.Utils.emailValidator
import com.example.lostandfoundipb.Utils.passwordValidator
import com.example.lostandfoundipb.retrofit.ApiService
import com.example.lostandfoundipb.retrofit.models.User
import com.example.lostandfoundipb.ui.viewmodel.LoginViewModel
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.alert
import org.jetbrains.anko.startActivity
import org.jetbrains.anko.yesButton

class   LoginActivity : AppCompatActivity() {
    lateinit var email: String
    lateinit var username: String
    lateinit var password: String
    lateinit var session: SessionManagement

    lateinit var viewModel: LoginViewModel
    private val apiService by lazy {
        this.let { ApiService.create(this) }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_login)
        session = SessionManagement(applicationContext)
        if(session.checkLogin()){
            startActivity<MainActivity>()
            finish()
        }
        viewModel = ViewModelProviders.of(this).get(LoginViewModel::class.java)
        loginResult()
        onClick()
    }

    private fun onClick() {
        login_forgot_password.setOnClickListener { startActivity<ForgotPasswordActivity>() }
        login_register.setOnClickListener { startActivity<RegisterActivity>() }
        login_btn_login.setOnClickListener { checkLogin() }
    }

    private fun checkLogin() {
        login_username.error = null
        login_password.error = null

        var cancel = false
        var focusView: View? = null

        var isEmail = true


        email = login_username.text.toString()
        password = login_password.text.toString()

        if (TextUtils.isEmpty(email)) {
            login_username.error = getString(R.string.error_empty)
            focusView = login_username
            cancel = true
        }
        else if (!TextUtils.isEmpty(email) && !emailValidator(email)) {
            isEmail = false
        }

        if (TextUtils.isEmpty(password)) {
            login_password.error = getString(R.string.error_empty)
            focusView = login_password
            cancel = true
        }
        else if (!passwordValidator(password)) {
            login_password.error = getString(R.string.password_error)
            focusView = login_password
            cancel = true
        }

        if (cancel) {
            focusView?.requestFocus()
        } else {
            if (isEmail){
                attemptLogin(User.LogIn(null,email,password))
            }
            else {
                attemptLogin(User.LogIn(email,null,password))
            }
        }

    }

    private fun attemptLogin(body: User.LogIn) {
        showProgress(true)
        viewModel.login(apiService, body)
    }

    private fun loginResult(){
        viewModel.loginResult.observe({lifecycle},{result ->
            if(result.success){
                showProgress(false)
                session.createLogin(result.token!!, password)
                startActivity<MainActivity>()
                finish()
            }
            else{
                showProgress(false)
                result.let {
                    alert(it.message!!){
                        yesButton {  }
                    }.show()
                }
            }
        })
    }


    private fun showProgress(show: Boolean){
        login_progress.visibility = if(show) View.VISIBLE else View.GONE
        disableTouch(show)
    }

    fun disableTouch(status: Boolean){
        if(status){
            window.setFlags(
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE,
                    WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE
            )
        } else {
            window.clearFlags(WindowManager.LayoutParams.FLAG_NOT_TOUCHABLE)
        }
    }
}