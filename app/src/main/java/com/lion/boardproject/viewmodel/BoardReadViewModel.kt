package com.lion.boardproject.viewmodel

import androidx.databinding.BindingAdapter
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.google.android.material.appbar.MaterialToolbar
import com.lion.boardproject.fragment.BoardReadFragment
import com.lion.boardproject.fragment.BoardWriteFragment

class BoardReadViewModel(val boardReadFragment: BoardReadFragment) : ViewModel() {
    // textFieldBoardReadTitle - text
    val textFieldBoardReadTitleText = MutableLiveData(" ")
    // textFieldBoardReadNickName - text
    val textFieldBoardReadNickName = MutableLiveData(" ")
    // textFieldBoardReadType - text
    val textFieldBoardReadTypeText = MutableLiveData(" ")
    // textFieldBoardReadText - text
    val textFieldBoardReadTextText = MutableLiveData("  ")
    // textViewBoardReadCommentCount - text
    val textViewBoardReadCommentCount = MutableLiveData("")

    // 수정 버튼을 누르고 난뒤 전송버튼을 눌렀을 때 분기하기 위한 변수
    // true : 댓글 수정, false : 댓글 새로 등록
    val isModifyReply = MutableLiveData(false)
    // 댓글 수정시 수정할 댓글의 index
    val clickPosition = MutableLiveData(0)

    // 댓글
    val editTextBoardReadComment = MutableLiveData("")

    companion object{
        // toolbarBoardRead - onNavigationClickBoardRead
        @JvmStatic
        @BindingAdapter("onNavigationClickBoardRead")
        fun onNavigationClickBoardRead(materialToolbar: MaterialToolbar, boardReadFragment: BoardReadFragment){
            materialToolbar.setNavigationOnClickListener {
                boardReadFragment.movePrevFragment()
            }
        }
    }

    // iconButtonBoardReadCommentSend - onClick
    fun iconButtonBoardReadCommentSendOnClick() {
        // 댓글 수정인지 확인
        if(isModifyReply.value == true) {
            boardReadFragment.isKeyboardOpen = false
            boardReadFragment.isDialogShown = false
            // 수정일 때 실행
            boardReadFragment.updateBoardReply()
        }
        else {
            // 댓글 등록일 때 실행
            boardReadFragment.enterBoardComment()
        }
    }
}