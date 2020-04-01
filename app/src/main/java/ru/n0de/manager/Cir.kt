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

class Cir(IdContanct: String, Number: String, Email: String, Name: String): Serializable {
    var idContact: String = ""
    var number: String = ""
    var email: String = ""
    var name = ""

    init {
        idContact = IdContanct
        number = Number
        email = Email
        name = Name
    }
}
