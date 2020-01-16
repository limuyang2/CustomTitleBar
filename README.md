[![](https://jitpack.io/v/limuyang2/CustomTitleBar.svg)](https://jitpack.io/#limuyang2/CustomTitleBar)
# CustomTitleBar
自定义标题栏，拥有丰富的属性，支持标题对其方式、副标题、图标按钮、文字按钮等等。

## 预览
![](https://github.com/limuyang2/CustomTitleBar/blob/master/screenshot/screenshot1.png)  

## 获取
先在 build.gradle 的 repositories 添加：  
```gradle
allprojects {
	repositories {
		...
		maven { url 'https://jitpack.io' }
	}
}
```

再在dependencies添加：  
```gradle
dependencies {
        //AndroidX
	implementation 'com.github.limuyang2:CustomTitleBar:1.1.0'
}
```

## 使用
### style中定义标题样式
参考demo - [styles.xml](https://github.com/limuyang2/CustomTitleBar/blob/master/app/src/main/res/values/styles.xml)  

定义标题栏样式，将其需要的属性从[attrs_titlebar.xml](https://github.com/limuyang2/CustomTitleBar/blob/master/customtitlebar/src/main/res/values/attrs_titlebar.xml)文件中复制过来，覆盖其默认值即可。
```xml
    <style name="CustomTitleBar.Normal">
        <item name="titlebar_height">56dp</item>
        <item name="titlebar_elevation">8dp</item><!--阴影，0dp即为无-->
        <item name="titlebar_bg_color">@color/colorPrimary</item>
        <item name="titlebar_title_text_size">17sp</item>
        <item name="titlebar_title_color">#ffffff</item>
        <item name="titlebar_subtitle_color">#ffffff</item>
        <item name="titlebar_title_gravity">left_center</item>
        <!--如果需要背景透明，则不能显示分割线分割线-->
        <item name="titlebar_show_divider">false</item>
        <item name="titlebar_divider_height">0.5dp</item>
        <!--<item name="titlebar_image_btn_width">37dp</item>-->
        <!--<item name="android:paddingLeft">10dp</item>-->
        <!--<item name="android:paddingRight">10dp</item>-->
        <!--<item name="titlebar_image_btn_height">37dp</item>-->
        <item name="titlebar_text_btn_text_size">16sp</item>
        ...<!--更多属性请参考 attrs_titlebar.xml 文件-->
    </style>
```

再在app主题中进行应用：  
```xml
    <style name="AppTheme" parent="Theme.AppCompat.Light.NoActionBar">
        
        ...

        <!--标题栏主题样式-->
        <item name="CustomTitleBarStyle">@style/CustomTitleBar.Normal</item>
    </style>
```

在布局文件中使用控件：  
```xml
<top.limuyang2.customtitlebar.CustomTitleBar
        android:id="@+id/titleBar"
        android:layout_width="match_parent"
        android:layout_height="wrap_content" />
```
>说明：`android:layout_height`请设置为`wrap_content`，**自行设置的任何具体数值都将无效**。  
>如果需要单独修改某一界面的标题栏高度，请使用`app:titlebar_height="40dp"`。其他属性同理  

设置标题栏内容：  
在`Activity`中设置对应的内容  
```kotlin
titleBar.title = "TitleBar"

titleBar.addLeftBackImageButton().setOnClickListener {  }
titleBar.subTitle = "sub Title"

titleBar.showTitleView = true

titleBar.titleRes = R.string.app_name

titleBar.addRightTextButton("coll")
titleBar.addRightImageButton(android.R.drawable.ic_menu_search)
···
```
