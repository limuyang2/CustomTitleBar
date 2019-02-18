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
        titleBar.titleGravity = Gravity.CENTER

        titleBar.removeCenterViewAndTitleView()
        titleBar.title = "TitleBar2"

        titleBar.addRightTextButton("呵呵")
        titleBar.addRightImageButton(R.drawable.icon_titlebar_back)
//        titleBar.addRightImageButton(R.drawable.icon_titlebar_back,0)
    }
}
