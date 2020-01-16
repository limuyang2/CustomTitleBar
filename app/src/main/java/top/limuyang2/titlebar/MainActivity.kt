package top.limuyang2.titlebar

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import kotlinx.android.synthetic.main.activity_main.*

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

//        val testView = TextView(this).apply {
//            text = "wwwwwwww"
//            setBackgroundColor(Color.DKGRAY)
//            layoutParams = RelativeLayout.LayoutParams(RelativeLayout.LayoutParams.MATCH_PARENT,RelativeLayout.LayoutParams.WRAP_CONTENT)
//        }
//        titleBar.setCenterView(testView)

        titleBar.title = "TitleBar"
//
//        titleBar.subTitle = "sub Title"
//
//        titleBar.showTitleView = true
//        titleBar.titleGravity = Gravity.START
//
//        titleBar.removeCenterViewAndTitleView()
//        titleBar.titleRes = R.string.app_name

        titleBar.addLeftBackImageButton()
        titleBar.addRightTextButton("coll")
        titleBar.addRightImageButton(android.R.drawable.ic_menu_search)
    }
}
