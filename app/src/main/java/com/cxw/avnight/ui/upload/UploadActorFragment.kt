package com.cxw.avnight.ui.upload


import android.os.Bundle
import android.text.Editable
import android.util.Log
import android.view.View
import android.widget.EditText
import android.widget.LinearLayout.HORIZONTAL
import android.widget.Toast
import androidx.lifecycle.Observer
import androidx.recyclerview.widget.GridLayoutManager
import androidx.recyclerview.widget.LinearLayoutManager
import com.cxw.avnight.R
import com.cxw.avnight.adapter.UploadActorImgAdapter
import com.cxw.avnight.base.BaseVMFragment
import com.cxw.avnight.dialog.AlertDialog
import kotlinx.android.synthetic.main.upload_fragment.*
import com.yanzhenjie.album.Album
import com.cxw.avnight.util.AppConfigs
import com.cxw.avnight.util.BaseTools
import com.cxw.avnight.util.BaseTools.toRequestBody
import com.cxw.avnight.util.SPUtil
import com.cxw.avnight.viewmodel.UploadViewModel
import com.google.gson.Gson
import kotlinx.android.synthetic.main.activity_actor_introduce.*
import kotlinx.android.synthetic.main.activity_login.*
import org.jetbrains.anko.toast
import kotlin.collections.HashMap

class UploadActorFragment : BaseVMFragment<UploadViewModel>() {
    override fun providerVMClass(): Class<UploadViewModel> = UploadViewModel::class.java
    private val imgPathList = arrayListOf<String>()
    private val mActorInfo = HashMap<String, Any>()
    override fun getLayoutResId(): Int = R.layout.upload_fragment
    private lateinit var cameraDialog: AlertDialog
    private val uploadActorImgAdapter by lazy { UploadActorImgAdapter() }
    override fun initData() {
        submit_tv.setOnClickListener {
            initEt()
            mViewModel.getUpload(
                    BaseTools.filesToMultipartBodyParts(context!!, imgPathList),
                    toRequestBody(Gson().toJson(mActorInfo))
            )
        }
    }

    override fun onError(e: Throwable) {
        super.onError(e)
        BaseTools.initLottieAnim(lav, View.GONE, true)
        context!!.toast(e.message.toString())
    }

    override fun requestLoading(isLoading: Boolean) {
        super.requestLoading(isLoading)
        BaseTools.initLottieAnim(lav, View.VISIBLE, true)
    }

    override fun requestSuccess(requestSuccess: Boolean) {
        super.requestSuccess(requestSuccess)
        BaseTools.initLottieAnim(lav, View.GONE, false)
    }

    private fun initEt() {
        if (!name_et.text.toString().isNotBlank()) {
            context!!.toast(getString(R.string.name_not_empty))
            return
        }
        if (!gender_et.text.toString().isNotBlank()) {
            context!!.toast(getString(R.string.gender_not_empty))
            return
        }
        if (!city_et.text.toString().isNotBlank()) {
            context!!.toast(getString(R.string.city_not_empty))
            return
        }
        if (!class_content_et.text.toString().isNotBlank()) {
            context!!.toast(getString(R.string.class_content_not_blank))
        }
        if (!BaseTools.checkPhoneNumber(phone_et.text.toString())) {
            context!!.toast(getString(R.string.phone_number_wrong_format))
        }
        if (imgPathList.size == 0) {
            context!!.toast(getString(R.string.upload_img))
            return
        }

        if (!qq_et.text.toString().isNotBlank() && !wx_et.text.toString().isNotBlank() && !phone_et.text.toString().isNotBlank() && !potato_et.text.toString().isNotBlank()) {
            context!!.toast(getString(R.string.contact_information_not_blank))
        }
        mActorInfo["actor_name"] = name_et.text.toString().trim()
        mActorInfo["actor_age"] = age_et.text.toString().toInt()
        mActorInfo["actor_gender"] = gender_et.text.toString().trim()
        mActorInfo["actor_phone"] = phone_et.text.toString().trim()
        mActorInfo["actor_wx"] = wx_et.text.toString().trim()
        mActorInfo["actor_qq"] = qq_et.text.toString().trim()
        mActorInfo["actor_workaddress"] = address_et.text.toString().trim()
        mActorInfo["actor_introduce"] = class_content_et.text.toString().trim()
        mActorInfo["actor_evaluate"] = evaluation_et.text.toString().trim()
        mActorInfo["actor_city"] = city_et.text.toString().trim()
        mActorInfo["actor_bust"] = bust_et.text.toString().trim()
        mActorInfo["actor_height"] = height_et.text.toString().toInt()
        mActorInfo["actor_weight"] = weight_et.text.toString().toInt()
        mActorInfo["actor_isinvalid"] = 0
        mActorInfo["actor_isverification"] = 0
        mActorInfo["actor_upload"]=SPUtil.getInt("userId")
        mActorInfo["actor_sort"] = 800
    }

    override fun onNetReload(v: View) {

    }


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        mBaseLoadService.showSuccess()  //这里方案怎么解决
        super.onViewCreated(view, savedInstanceState)
    }

    override fun initView() {
        initRv()
        initCamera()
    }

    private fun initRv() {
        with(actor_info_img_rv) {
            setHasFixedSize(true)
            layoutManager = LinearLayoutManager(context, HORIZONTAL, false)
            adapter = uploadActorImgAdapter
        }
        initAdapter()
    }

    private fun initAdapter() {
        with(uploadActorImgAdapter) {
            setOnItemChildClickListener { _, _, position ->
                imgPathList.removeAt(position)
                remove(position)
            }
        }
    }


    private fun initCamera() {
        camera.setOnClickListener {
            if (uploadActorImgAdapter.itemCount >= AppConfigs.SELECT_COUNT) {
                context!!.toast(getString(R.string.up_to_six_pieces))
                return@setOnClickListener
            }
            cameraDialog = AlertDialog.Builder(context!!)
                    .setContentView(R.layout.dialog_change_layout)
                    .setOnClickListener(R.id.dialog_choose_one, View.OnClickListener {
                        Album.camera(this)
                                .image()
                                .onResult {
                                    uploadActorImgAdapter.addData(it)
                                    imgPathList.add(it)
                                }
                                .start()
                        cameraDialog.dismiss()
                    })
                    .setOnClickListener(R.id.dialog_choose_two, View.OnClickListener {
                        Album.image(this)
                                .multipleChoice()
                                .camera(false)
                                .columnCount(AppConfigs.COLUMN_COUNT)
                                .selectCount(AppConfigs.SELECT_COUNT)
                                .onResult { mAlbumFilePath ->
                                    mAlbumFilePath.forEach {
                                        uploadActorImgAdapter.addData(it.path)
                                        imgPathList.add(it.path)
                                    }
                                    if (uploadActorImgAdapter.itemCount < AppConfigs.SELECT_COUNT) return@onResult
                                    for (pos in uploadActorImgAdapter.itemCount downTo 7) {
                                        uploadActorImgAdapter.remove(pos)
                                    }
                                }
                                .onCancel {
                                }
                                .start()
                        cameraDialog.dismiss()

                    })
                    .setOnClickListener(R.id.dialog_cancel, View.OnClickListener {
                        cameraDialog.dismiss()
                    })
                    .fullWidth()
                    .formBottom(true)
                    .show()
        }
    }
}


