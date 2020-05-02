package ru.n0de.manager
import android.content.Context
import android.content.Intent
import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.provider.ContactsContract
import android.util.AttributeSet
import android.util.Log
import android.view.MotionEvent
import android.widget.RelativeLayout
import de.hdodenhof.circleimageview.CircleImageView
import org.jetbrains.anko.image
import java.io.InputStream
import java.io.Serializable
import kotlin.math.abs

data class Cir(val IdContact: String = "", val Number: String = "", val Email: String = "", val Name: String = "", val action: Actions? = Actions.Sms)
