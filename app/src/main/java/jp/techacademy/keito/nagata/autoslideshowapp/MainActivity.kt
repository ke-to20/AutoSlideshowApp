package jp.techacademy.keito.nagata.autoslideshowapp

import android.Manifest
import androidx.appcompat.app.AppCompatActivity
import android.os.Bundle
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import android.provider.MediaStore
import android.content.ContentUris
import android.os.Handler
import android.view.View
import kotlinx.android.synthetic.main.activity_main.*
import java.util.*


class MainActivity : AppCompatActivity(), View.OnClickListener {

    //    変数
    //    画像枚数
    var numImages: Int = 0

    //    表示している画像が何枚目か
    var numDisplayImage: Int = 1

    //    再生 or 停止
//    true：再生中　　false：停止中
    var auto: Boolean = false

    private val PERMISSIONS_REQUEST_CODE = 100

    override fun onCreate(savedInstanceState: Bundle?) {

        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_main)

        // Android 6.0以降の場合
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            // パーミッションの許可状態を確認する
            if (checkSelfPermission(Manifest.permission.READ_EXTERNAL_STORAGE) == PackageManager.PERMISSION_GRANTED) {
                // 許可されている
                getContentsInfo()


            } else {
                // 許可されていないので許可ダイアログを表示する
                requestPermissions(
                    arrayOf(Manifest.permission.READ_EXTERNAL_STORAGE),
                    PERMISSIONS_REQUEST_CODE
                )
            }
            // Android 5系以下の場合
        } else {
            getContentsInfo()
        }
    }

    override fun onRequestPermissionsResult(
        requestCode: Int,
        permissions: Array<String>,
        grantResults: IntArray
    ) {
        when (requestCode) {
            PERMISSIONS_REQUEST_CODE ->
                if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    getContentsInfo()

                }
        }
    }

    //    ボタン作動させる
    private fun getContentsInfo() {
        Log.d("ANDROID1", "getContentsInfoが実行されたよ")

//        １枚目を表示
        displayImage(1)

//        画像枚数を数える
        countImage()

        go_button.setOnClickListener(this)
        back_button.setOnClickListener(this)
        play_stop_button.setOnClickListener(this)

    }

    override fun onClick(v: View?) {
        Log.d("ANDROID1", "onClickが実行されたよ")

        if (v != null) {
            when (v.id) {
                R.id.go_button -> nextPicture()
                R.id.back_button -> previousPicture()
                R.id.play_stop_button -> playStop()

            }
        }
    }

    //    画像枚数カウンタ
    private fun countImage() {
        // 画像の情報を取得する
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目（null = 全項目）
            null, // フィルタ条件（null = フィルタなし）
            null, // フィルタ用パラメータ
            null // ソート (nullソートなし）
        )

        if (cursor!!.moveToFirst()) {
            do {
                // indexからIDを取得し、そのIDから画像のURIを取得する
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                val imageUri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                Log.d("ANDROID1", "URI : " + imageUri.toString())
                numImages++

            } while (cursor.moveToNext())
        }
        cursor.close()
        Log.d("ANDROID1", "合計画像枚数 : " + numImages.toString() + "枚")


    }


//    画像表示関数

    private fun displayImage(counter: Int) {

        // 画像の情報を取得する
        val resolver = contentResolver
        val cursor = resolver.query(
            MediaStore.Images.Media.EXTERNAL_CONTENT_URI, // データの種類
            null, // 項目（null = 全項目）
            null, // フィルタ条件（null = フィルタなし）
            null, // フィルタ用パラメータ
            null // ソート (nullソートなし）
        )

        if (cursor != null) {
            Log.d("ANDROID1", cursor.javaClass.kotlin.toString())
        }


        if (cursor!!.moveToFirst()) {
            for (i in 0 until counter) {

                // indexからIDを取得し、そのIDから画像のURIを取得する
                val fieldIndex = cursor.getColumnIndex(MediaStore.Images.Media._ID)
                val id = cursor.getLong(fieldIndex)
                val imageUri =
                    ContentUris.withAppendedId(MediaStore.Images.Media.EXTERNAL_CONTENT_URI, id)

                Log.d("ANDROID1", "URI : " + imageUri.toString())
                imageView.setImageURI(imageUri)
//                Thread.sleep(3000)
                cursor.moveToNext()
            }
        }
        cursor.close()

    }


    //    「進む」ボタン
    private fun nextPicture() {
        Log.d("ANDROID1", "nextPictureが実行されたよ")

        //        何枚目の画像を表示させるかの計算
        if (numImages == numDisplayImage) {
            numDisplayImage = 1
        } else {
            numDisplayImage++
        }
        Log.d("ANDROID1", numDisplayImage.toString() + "枚目の画像を表示させるよ")

        displayImage(numDisplayImage)

    }

    //    「戻る」ボタン
    private fun previousPicture() {
        Log.d("ANDROID1", "previousPictureが実行されたよ")

        //        何枚目の画像を表示させるかの計算
        if (numDisplayImage == 1) {
            numDisplayImage = numImages
        } else {
            numDisplayImage--
        }
        Log.d("ANDROID1", numDisplayImage.toString() + "枚目の画像を表示させるよ")

        displayImage(numDisplayImage)

    }

    private var mTimer: Timer? = null

    // タイマー用の時間のための変数
    private var mTimerSec = 0.0
    private var mHandler = Handler()


    //    「再生・停止」ボタン
    private fun playStop() {

        Log.d("ANDROID1", "playStopが実行されたよ")

//        ボタンを非表示に
        go_button.visibility = View.INVISIBLE
        back_button.visibility = View.INVISIBLE


//        ボタンの表示確認　再生 or 停止
        if (play_stop_button.text.toString() == "再生") {

            Log.d("ANDROID1", "再生が押されたよ")

//            表示が 再生 のとき
//            停止に表示変更
            play_stop_button.text = "停止"

//            タイマー　自動送り　開始 2s停止]
            if (mTimer == null) {

                Log.d("ANDROID1", "タイマーが開始されたよ")

                mTimer = Timer()
                mTimer!!.schedule(object : TimerTask() {

                    override fun run() {
                        Log.d("ANDROID1", "runが実行されたよ")

                        mHandler.post {
                            nextPicture()
                        }

                    }
                }, 2000, 2000) // 最初に始動させるまで100ミリ秒、ループの間隔を100ミリ秒 に設定
            }


        } else {
            //            表示が 停止 のとき
//            再生に表示変更
            play_stop_button.text = "再生"
            Log.d("ANDROID1", "停止が実行されたよ")

            if (mTimer != null) {

                mTimer!!.cancel()
                mTimer = null
            }

//            ボタンを再表示
            go_button.visibility = View.VISIBLE
            back_button.visibility = View.VISIBLE

        }


    }


}