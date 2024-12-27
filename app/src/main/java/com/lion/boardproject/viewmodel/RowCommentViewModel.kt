package com.lion.boardproject.viewmodel

import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.lion.boardproject.fragment.BoardReadFragment

class RowCommentViewModel(val boardReadFragment: BoardReadFragment) : ViewModel() {

    // 클릭한 댓글의 index
    var clickPosition = 0

    // textViewRowCommentID - Text
    val textViewRowCommentIDText = MutableLiveData("")
    // textViewRowCommentContent - Text
    val textViewRowCommentContentText = MutableLiveData("")
    // textViewRowCommentTime - Text
    val textViewRowCommentTimeText = MutableLiveData("")
    // 댓글 수정 여부
    val textViewRowCommentCheckUpdateText = MutableLiveData("")
    // 댓글 작성 시간
    val textViewRowCommentTime2Text = MutableLiveData("")

    // 댓글 수정 또는 삭제 여부를 설정하는 메서드
    fun imageButtonRowCommentSettingOnClick(){

        boardReadFragment.showBottomSheetSetting(clickPosition)
    }

}