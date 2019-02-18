package top.limuyang2.titlebar

import android.os.Bundle
import android.support.v7.app.AppCompatActivity
import android.view.Gravity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        titleBar.title = "TitleBar"

        titleBar.addLeftBackImageButton()
        titleBar.subTitle = "sub Title"

        titleBar.showTitleView = true
        titleBar.titleGravity = Gravity.START

        titleBar.removeCenterViewAndTitleView()
        titleBar.titleRes = R.string.app_name

        titleBar.addRightTextButton("coll")
        titleBar.addRightImageButton(android.R.drawable.ic_menu_search)
    }
}
