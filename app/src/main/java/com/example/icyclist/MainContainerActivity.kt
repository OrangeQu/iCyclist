package com.example.icyclist

import android.content.Intent
import android.os.Bundle
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.fragment.app.Fragment
import com.example.icyclist.fragment.CommunityFragment
import com.example.icyclist.fragment.ProfileFragment
import com.example.icyclist.fragment.SportFragment
import com.example.icyclist.manager.UserManager
import com.google.android.material.bottomnavigation.BottomNavigationView

class MainContainerActivity : AppCompatActivity() {

    private lateinit var bottomNavigation: BottomNavigationView
    
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        
        // 检查登录状态
        if (!UserManager.isLoggedIn(this)) {
            startActivity(Intent(this, LoginActivity::class.java))
            finish()
            return
        }
        
        setContentView(R.layout.activity_main_container)
        
        bottomNavigation = findViewById(R.id.bottomNavigation)
        
        // 默认显示运动页面
        if (savedInstanceState == null) {
            loadFragment(SportFragment())
        }
        
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.menu_sport -> {
                    loadFragment(SportFragment())
                    true
                }
                R.id.menu_community -> {
                    loadFragment(CommunityFragment())
                    true
                }
                R.id.menu_profile -> {
                    loadFragment(ProfileFragment())
                    true
                }
                else -> false
            }
        }
    }
    
    private fun loadFragment(fragment: Fragment) {
        supportFragmentManager.beginTransaction()
            .setCustomAnimations(
                R.anim.fade_in,  // 进入动画
                R.anim.fade_out, // 退出动画
                R.anim.fade_in,  // 弹出进入动画
                R.anim.fade_out  // 弹出退出动画
            )
            .replace(R.id.fragmentContainer, fragment)
            .commit()
    }
    
    fun selectMenuItem(menuId: Int) {
        bottomNavigation.selectedItemId = menuId
    }
    
    override fun onBackPressed() {
        if (bottomNavigation.selectedItemId != R.id.menu_sport) {
            bottomNavigation.selectedItemId = R.id.menu_sport
        } else {
            AlertDialog.Builder(this)
                .setTitle("退出应用")
                .setMessage("确定要退出 iCyclist 吗?")
                .setPositiveButton("退出") { _, _ ->
                    super.onBackPressed()
                }
                .setNegativeButton("取消", null)
                .show()
        }
    }
}
