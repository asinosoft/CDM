
import android.content.Context
import android.util.Log
import androidx.recyclerview.widget.RecyclerView
import android.view.MotionEvent
import android.view.GestureDetector
import android.view.View

class RecyclerTouchListerner(context: Context, private val mListener: OnItemMotionEventListener?) : RecyclerView.OnItemTouchListener {

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {

    }

    private val mGestureDetector: GestureDetector
    private val mGestureListener: GestureListener

    init {
        mGestureListener = GestureListener()

        mGestureDetector = GestureDetector(context, mGestureListener)
    }

    override fun onInterceptTouchEvent(view: RecyclerView, e: MotionEvent): Boolean {

        mGestureListener.setRecyclerView(view)

        mGestureDetector.onTouchEvent(e)

        return false
    }

    override fun onTouchEvent(view: RecyclerView, motionEvent: MotionEvent) {
        Log.i("Touch: ", "$view / ${motionEvent.actionMasked}")
    }

    private inner class GestureListener : GestureDetector.SimpleOnGestureListener() {
        private var mRecyclerView: RecyclerView? = null

        override fun onSingleTapUp(e: MotionEvent): Boolean {
            val view = findView(e)
            if (validate(view!!)) {
                val position = findPosition(view)
                mListener!!.onItemClick(view, position)
            }
            return true
        }


        override fun onLongPress(e: MotionEvent) {
            val view = findView(e)
            if (validate(view!!)) {
                val position = findPosition(view)
                mListener!!.onItemLongClick(view, position)
            }
        }

        private fun validate(view: View?): Boolean {
            return view != null && mListener != null
        }

        private fun findView(e: MotionEvent): View? {
            return mRecyclerView!!.findChildViewUnder(e.x, e.y)
        }

        private fun findPosition(view: View): Int {
            return mRecyclerView!!.getChildPosition(view)
        }

        fun setRecyclerView(mRecyclerView: RecyclerView) {
            this.mRecyclerView = mRecyclerView
        }
    }

    interface OnItemMotionEventListener {
        fun onItemClick(view: View, position: Int)

        fun onItemLongClick(view: View, position: Int)
    }

    class SimpleItemMotionEventListener : OnItemMotionEventListener {

        override fun onItemClick(view: View, position: Int) {

        }

        override fun onItemLongClick(view: View, position: Int) {

        }
    }
}