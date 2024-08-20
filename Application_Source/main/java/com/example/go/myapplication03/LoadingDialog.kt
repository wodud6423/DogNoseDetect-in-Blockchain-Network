import android.app.ProgressDialog
import android.content.Context

// 로딩화면을 띄우는 클래스
class LoadingDialog(private val context: Context) {
    private var progressDialog: ProgressDialog? = null

    fun show() {
        if (progressDialog == null) {
            progressDialog = ProgressDialog(context)
            progressDialog?.setMessage("Loading...")
            progressDialog?.setCancelable(false)
            progressDialog?.show()
        }
    }

    fun hide() {
        progressDialog?.dismiss()
        progressDialog = null
    }
}
