package com.lion.boardproject.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.lion.boardproject.databinding.BottomSheetSettingBinding
import com.lion.boardproject.fragment.BoardReadFragment

class BottomSheetSettingViewModel(val boardReadFragment: BoardReadFragment, val bottomSheetDialog: BottomSheetDialog) : ViewModel() {

    // 클릭한 댓글의 index
    var clickPosition = MutableLiveData(0)

    // 클릭한 댓글의 내용
    // 닉네임
    // textViewBottomSheetNickName - Text
    val textViewBottomSheetNickNameText = MutableLiveData("")
    // 댓글 내용
    // textViewBottomSheetReply - Text
    val textViewBottomSheetReplyText = MutableLiveData("")

    // 버튼의 텍스트
    // buttonBottomSheetModify - Text
    val buttonBottomSheetModifyText = MutableLiveData("수정")
    // buttonBottomSheetDelete - Text
    val buttonBottomSheetDeleteText = MutableLiveData("삭제")

    // 수정 버튼을 누르면 호출되는 메소드
    // buttonBottomSheetModify - onClick
    fun buttonBottomSheetModifyOnClick() {
        bottomSheetDialog.dismiss()
        boardReadFragment.modifyReply(clickPosition.value!!)
    }
    
    // 삭제 버튼을 누르면 호출되는 메소드
    // buttonBottomSheetDelete - onClick
    fun buttonBottomSheetDeleteOnClick() {
        bottomSheetDialog.dismiss()
        boardReadFragment.deleteBoardReply()
    }

}