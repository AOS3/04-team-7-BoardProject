package com.lion.boardproject.fragment

import android.content.Context
import android.content.Context.INPUT_METHOD_SERVICE
import android.content.DialogInterface
import android.graphics.Rect
import android.os.Bundle
import android.os.SystemClock
import android.util.Log
import androidx.fragment.app.Fragment
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.view.inputmethod.InputMethodManager
import androidx.core.content.ContextCompat.getSystemService
import androidx.core.view.children
import androidx.core.view.isVisible
import androidx.databinding.DataBindingUtil
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.bottomsheet.BottomSheetDialog
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.divider.MaterialDividerItemDecoration
import com.lion.boardproject.BoardActivity
import com.lion.boardproject.R
import com.lion.boardproject.databinding.BottomSheetSettingBinding
import com.lion.boardproject.databinding.FragmentBoardReadBinding
import com.lion.boardproject.databinding.RowCommentBinding
import com.lion.boardproject.model.BoardModel
import com.lion.boardproject.model.ReplyModel
import com.lion.boardproject.service.BoardService
import com.lion.boardproject.service.ReplyService
import com.lion.boardproject.util.ReplyState
import com.lion.boardproject.viewmodel.BoardReadViewModel
import com.lion.boardproject.viewmodel.BottomSheetSettingViewModel
import com.lion.boardproject.viewmodel.RowCommentViewModel
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.async
import kotlinx.coroutines.launch
import java.util.Calendar
import kotlin.concurrent.thread

class BoardReadFragment(val boardMainFragment: BoardMainFragment) : Fragment() {

    lateinit var fragmentBoardReadBinding: FragmentBoardReadBinding
    lateinit var boardActivity: BoardActivity

    // 현재 글의 문서 id를 담을 변수
    lateinit var boardDocumentId:String

    // 글 데이터를 담을 변수
    lateinit var boardModel:BoardModel

    // 댓글 임시 데이터
    lateinit var replyList:MutableList<ReplyModel>

    // 키보드 상태 추적 변수
    var isKeyboardOpen = false
    var isDialogShown = true

    override fun onCreateView(inflater: LayoutInflater, container: ViewGroup?, savedInstanceState: Bundle?): View? {
        fragmentBoardReadBinding = DataBindingUtil.inflate(inflater, R.layout.fragment_board_read, container, false)
        fragmentBoardReadBinding.boardReadViewModel = BoardReadViewModel(this@BoardReadFragment)
        fragmentBoardReadBinding.lifecycleOwner = this@BoardReadFragment

        boardActivity = activity as BoardActivity

        // 키보드가 나타날 때 ScrollView 조정
        setupKeyboardListener()

        // 이미지 뷰를 안보이는 상태로 설정한다.
        fragmentBoardReadBinding.imageViewBoardRead.isVisible = false

        // 댓글 목록을 가져오는 메서드를 호출한다.
        refreshReplyRecyclerView()
        // arguments의 값을 변수에 담아주는 메서드를 호출한다.
        gettingArguments()
        // 툴바를 구성하는 메서드를 호출한다.
        settingToolbar()
        // 글 데이터를 가져와 보여주는 메서드를 호출한다.
        settingBoardData()

        // 댓글 RecyclerView를 구성하는 메서드를 호출한다.
        // 가져오는 속도가 늦어서 댓글 가져오는 메소드에서 호출
        // settingRecyclerViewComment()



        return fragmentBoardReadBinding.root
    }

    // 키보드 올리고 내렸을 때 그에 맞게 ScrollView 조정하는 메서드
    private fun setupKeyboardListener() {
        val rootView = fragmentBoardReadBinding.root
        val commentLayout = fragmentBoardReadBinding.linearLayoutBoardReadComment
        val scrollView = fragmentBoardReadBinding.linearLayoutList
        val nestedScrollView = fragmentBoardReadBinding.nestedScrollView

        rootView.viewTreeObserver.addOnGlobalLayoutListener {
            // 키보드가 올라온 영역의 정보를 가져옴
            val rect = Rect()
            // rootView의 현재 보이는 영역을 가져옴
            rootView.getWindowVisibleDisplayFrame(rect)

            val visibleHeight = rect.height()
            val totalHeight = rootView.height
            val keyboardHeight = totalHeight - visibleHeight

            if (keyboardHeight > totalHeight * 0.15) {
                // 댓글 입력 영역의 LayoutParams 조정
                val commentLayoutParams = commentLayout.layoutParams
                if (commentLayoutParams is ViewGroup.MarginLayoutParams) {
                    commentLayoutParams.bottomMargin = keyboardHeight
                    commentLayout.layoutParams = commentLayoutParams
                }

//                // ScrollView의 LayoutParams 조정
//                val scrollViewLayoutParams = scrollView.layoutParams
//                if (scrollViewLayoutParams is ViewGroup.MarginLayoutParams) {
//                    scrollViewLayoutParams.bottomMargin = keyboardHeight
//                    scrollView.layoutParams = scrollViewLayoutParams
//                }

                // 스크롤뷰를 맨 아래로 스크롤
                scrollView.post {
                    scrollView.scrollTo(0, nestedScrollView.height)
                }
            } else {
                // 키보드가 닫힌 경우 LayoutParams 복원
                val commentLayoutParams = commentLayout.layoutParams
                if (commentLayoutParams is ViewGroup.MarginLayoutParams) {
                    commentLayoutParams.bottomMargin = 0
                    commentLayout.layoutParams = commentLayoutParams
                }
                // NestedScrollView를 초기 위치로 스크롤
                scrollView.post {
                    scrollView.scrollTo(0, 0) // 스크롤뷰를 맨 위로 복구
                }
            }
        }
    }



    // 이전 화면으로 돌아가는 메서드
    fun movePrevFragment(){
        boardMainFragment.removeFragment(BoardSubFragmentName.BOARD_WRITE_FRAGMENT)
        boardMainFragment.removeFragment(BoardSubFragmentName.BOARD_READ_FRAGMENT)
    }

    // 툴바를 구성하는 메서드
    fun settingToolbar(){
        fragmentBoardReadBinding.apply {
            // 메뉴를 보이지 않게 설정한다.
            toolbarBoardRead.menu.children.forEach {
                it.isVisible = false
            }

            toolbarBoardRead.setOnMenuItemClickListener {
                when(it.itemId){
                    R.id.menuItemBoardReadModify -> {
                        // 글의 문서 번호를 전달한다.
                        val dataBundle = Bundle()
                        dataBundle.putString("boardDocumentId", boardDocumentId)
                        boardMainFragment.replaceFragment(BoardSubFragmentName.BOARD_MODIFY_FRAGMENT, true, true, dataBundle)
                    }
                    R.id.menuItemBoardReadDelete -> {
                        val builder = MaterialAlertDialogBuilder(boardActivity)
                        builder.setTitle("글 삭제")
                        builder.setMessage("삭제시 복구할 수 없습니다")
                        builder.setNegativeButton("취소", null)
                        builder.setPositiveButton("삭제"){ dialogInterface: DialogInterface, i: Int ->
                            proBoardDelete()
                        }
                        builder.show()
                    }
                }
                true
            }
        }
    }

    // arguments의 값을 변수에 담아준다.
    fun gettingArguments(){
        boardDocumentId = arguments?.getString("boardDocumentId")!!
    }

    // 글 데이터를 가져와 보여주는 메서드
    fun settingBoardData(){
        // 서버에서 데이터를 가져온다.
        CoroutineScope(Dispatchers.Main).launch {
            val work1 = async(Dispatchers.IO){
                BoardService.selectBoardDataOneById(boardDocumentId)
            }
            boardModel = work1.await()

            fragmentBoardReadBinding.apply {
                boardReadViewModel?.textFieldBoardReadTitleText?.value = boardModel.boardTitle
                boardReadViewModel?.textFieldBoardReadTextText?.value = boardModel.boardText
                boardReadViewModel?.textFieldBoardReadTypeText?.value = boardModel.boardTypeValue.str
                boardReadViewModel?.textFieldBoardReadNickName?.value = boardModel.boardWriterNickName

                // 작성자와 로그인한 사람이 같으면 메뉴를 보기에 한다.
                if(boardModel.boardWriteId == boardActivity.loginUserDocumentId){
                    toolbarBoardRead.menu.children.forEach {
                        it.isVisible = true
                    }
                }
            }

            // 첨부 이미지가 있다면
            if(boardModel.boardFileName != "none"){
                val work1 = async(Dispatchers.IO) {
                    // 이미지에 접근할 수 있는 uri를 가져온다.
                    BoardService.gettingImage(boardModel.boardFileName)
                }

                val imageUri = work1.await()
                boardActivity.showServiceImage(imageUri, fragmentBoardReadBinding.imageViewBoardRead)
                fragmentBoardReadBinding.imageViewBoardRead.isVisible = true
            }
        }
    }

    // 글 삭제 처리 메서드
    fun proBoardDelete(){
        CoroutineScope(Dispatchers.Main).launch {
            // 만약 첨부 이미지가 있다면 삭제한다.
            if(boardModel.boardFileName != "none"){
                val work1 = async(Dispatchers.IO){
                    BoardService.removeImageFile(boardModel.boardFileName)
                }
                work1.join()
            }
            // 글 정보를 삭제한다.
            val work2 = async(Dispatchers.IO){
                BoardService.deleteBoardData(boardDocumentId)
            }
            work2.join()
            // 글 목록 화면으로 이동한다.
            boardMainFragment.removeFragment(BoardSubFragmentName.BOARD_READ_FRAGMENT)
        }
    }

    /////////////////////////////////////// 댓글과 관련된 코드 ///////////////////////////////////////

    // 입력한 댓글을 저장하는 메소드
    fun enterBoardComment() {
        if(fragmentBoardReadBinding.boardReadViewModel?.editTextBoardReadComment?.value.toString() == ""){
            return
        }
        // Log.d("test200", fragmentBoardReadBinding.boardReadViewModel?.editTextBoardReadComment?.value.toString())
        // Log.d("test200", boardActivity.loginUserNickName)
        // Log.d("test200", boardModel.boardDocumentId)
        var replyUserId = boardActivity.loginUserDocumentId
        var replyNickName = boardActivity.loginUserNickName
        var replyText = fragmentBoardReadBinding.boardReadViewModel?.editTextBoardReadComment?.value.toString()
        var replyBoardId = boardModel.boardDocumentId
        var replyTimeStamp = System.nanoTime()
        var replyState = ReplyState.REPLY_STATE_NORMAL

        var calendar = Calendar.getInstance()
        // 년, 월, 일, 시, 분, 초 가져오기
        var replyYear = calendar.get(Calendar.YEAR)
        var replyMonth = calendar.get(Calendar.MONTH) + 1 // 0부터 시작하므로 +1 필요
        var replyDay = calendar.get(Calendar.DAY_OF_MONTH)
        var replyHour = calendar.get(Calendar.HOUR_OF_DAY) // 24시간 기준
        var replyMinute = calendar.get(Calendar.MINUTE)
        // var replySecond = calendar.get(Calendar.SECOND)

        // 댓글을 추가한다.
        CoroutineScope(Dispatchers.Main).launch {
            val work1 = async(Dispatchers.IO){
                val replyModel = ReplyModel()
                replyModel.replyNickName = replyNickName
                replyModel.replyText = replyText
                replyModel.replyBoardId = replyBoardId
                replyModel.replyTimeStamp = replyTimeStamp
                replyModel.replyYear = replyYear
                replyModel.replyMonth = replyMonth
                replyModel.replyDay = replyDay
                replyModel.replyHour = replyHour
                replyModel.replyMinute = replyMinute
                replyModel.replyState = replyState
                ReplyService.addReplyData(replyModel)
            }
            work1.join()

            fragmentBoardReadBinding.boardReadViewModel?.editTextBoardReadComment?.value = ""
            refreshReplyRecyclerView()
        }
    }

    /////////////////////////////////// 바텀 시트 관련 코드 ///////////////////////////////////////////

    fun showBottomSheetSetting(clickPosition:Int) {
        val bottomSheetSettingBinding = DataBindingUtil.inflate<BottomSheetSettingBinding>(layoutInflater, R.layout.bottom_sheet_setting, null, false)

        bottomSheetSettingBinding.lifecycleOwner = this@BoardReadFragment

        val bottomSheetSettingDialog = BottomSheetDialog(bottomSheetSettingBinding.root.context)
        bottomSheetSettingDialog.setContentView(bottomSheetSettingBinding.root)
        bottomSheetSettingBinding.bottomSheetSettingViewModel = BottomSheetSettingViewModel(this@BoardReadFragment, bottomSheetSettingDialog)

        bottomSheetSettingBinding.bottomSheetSettingViewModel?.textViewBottomSheetNickNameText?.value = "${replyList[clickPosition].replyNickName} :"
        bottomSheetSettingBinding.bottomSheetSettingViewModel?.textViewBottomSheetReplyText?.value = replyList[clickPosition].replyText
        bottomSheetSettingBinding.bottomSheetSettingViewModel?.clickPosition?.value = clickPosition

        bottomSheetSettingDialog.show()
    }

    // 수정 버튼을 눌렀을 경우 실행되는 메소드
    fun modifyReply(clickPosition: Int) {
        isDialogShown = true
        Log.d("test200", "isDialogShown = ${isDialogShown}")
        // 댓글 내용을 EditText에 설정
        fragmentBoardReadBinding.boardReadViewModel?.editTextBoardReadComment?.value = replyList[clickPosition].replyText
        // 댓글의 index를 설정
        fragmentBoardReadBinding.boardReadViewModel?.clickPosition?.value = clickPosition
        // 수정 버튼을 눌렀다는 것을 알림
        fragmentBoardReadBinding.boardReadViewModel?.isModifyReply?.value = true

        // 댓글 입력 view를 포커싱하면서 키보드를 올리는 메서드를 호출
        showSoftInput()

        // 키보드 상태를 감지하고 키보드가 닫힐 때 다이얼로그를 띄우는 메서드 호출
        setupKeyboardListener2()
    }

    // 댓글 입력 view를 포커싱하면서 키보드를 올리는 메서드
    fun showSoftInput() {
        CoroutineScope(Dispatchers.Main).launch {
            // 약간의 지연 시간
            kotlinx.coroutines.delay(100) // 100ms 지연

            fragmentBoardReadBinding.editTextBoardReadComment.apply {
                requestFocus() // 포커스 요청

                // 키보드를 올리는 작업
                val imm = boardActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT) // 키보드 표시
            }
        }
    }

    // 키보드 상태를 감지하고 키보드가 닫힐 때 다이얼로그를 띄우는 메서드
    private fun setupKeyboardListener2() {
        val rootView = fragmentBoardReadBinding.root
        val rect = Rect()

        CoroutineScope(Dispatchers.Main).launch {
            kotlinx.coroutines.delay(1000) // 100ms 지연

            rootView.viewTreeObserver.addOnGlobalLayoutListener {
                rootView.getWindowVisibleDisplayFrame(rect)

                val visibleHeight = rect.height()
                val totalHeight = rootView.height
                val keyboardHeight = totalHeight - visibleHeight

                if (keyboardHeight > totalHeight * 0.15) {
                    // 키보드가 열려 있음
                    isKeyboardOpen = true
                } else if (isKeyboardOpen) {
                    // 키보드가 닫혔을 때 다이얼로그를 띄움
                    isKeyboardOpen = false

                    showConfirmationDialog()
                }
            }
        }
    }

    // 다이얼로그를 표시하는 메서드
    private fun showConfirmationDialog() {
        CoroutineScope(Dispatchers.Main).launch {

            // 약간의 지연 시간
            // 마지막 입력 후 지연을 주지 않으면 다시 다이얼로그가 뜨는 현상이 발생
            // 약간의 지연 시간을 준다.
            kotlinx.coroutines.delay(100) // 100ms 지연

            if (isDialogShown == false) {
                return@launch
            }
            MaterialAlertDialogBuilder(boardActivity)
                .setTitle("댓글 수정")
                .setMessage("댓글 수정을 취소하시겠습니까?")
                .setPositiveButton("확인") { dialog, _ ->
                    // EditText의 내용을 비운다
                    fragmentBoardReadBinding.boardReadViewModel?.editTextBoardReadComment?.value = ""
                    // 일반 댓글 입력 상태로 돌아간다
                    fragmentBoardReadBinding.boardReadViewModel?.isModifyReply?.value = false
                    isDialogShown = false
                    isKeyboardOpen = false
                    dialog.dismiss()
                }
                .setNegativeButton("취소") { dialog, _ ->
                    // 댓글 입력 view를 포커싱하면서 키보드를 올리는 작업
                    // 키보드 열림 상태로 설정
                    fragmentBoardReadBinding.editTextBoardReadComment.apply {
                        requestFocus() // 포커스 요청

                        // 키보드를 올리는 작업
                        val imm = boardActivity.getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
                        imm.showSoftInput(this, InputMethodManager.SHOW_IMPLICIT) // 키보드 표시
                    }

                    // 다이얼로그를 닫는다
                    dialog.dismiss()
                }
                .show()
        }

    }

    // 댓글을 수정하는 메서드
    fun updateBoardReply() {
        isDialogShown = false
        CoroutineScope(Dispatchers.Main).launch {
            val work1 = async(Dispatchers.IO){

                // 수정될 댓글의 인덱스를 저장한다.
                val clickPosition = fragmentBoardReadBinding.boardReadViewModel?.clickPosition?.value
                // replyModel을 생성하고 수정될 댓글의 내용을 저장한다.
                val replyModel = replyList[clickPosition!!]
                // 수정될 댓글의 내용을 저장한다.
                replyModel.replyText = fragmentBoardReadBinding.boardReadViewModel?.editTextBoardReadComment?.value.toString()
                // 댓글 데이터를 수정한다.
                ReplyService.updateReplyData(replyModel)
            }
            work1.join()

            // 수정 버튼 기능을 모드 수행한 후 댓글 입력 모드로 수정
            fragmentBoardReadBinding.boardReadViewModel?.isModifyReply?.value = false
            // EditText의 내용을 비운다
            fragmentBoardReadBinding.boardReadViewModel?.editTextBoardReadComment?.value = ""

            // 댓글 RecyclerView를 갱신한다.
            refreshReplyRecyclerView()
        }
    }

    // 댓글을 삭제 상태로 변경하는 메소드 메서드
    fun deleteBoardReply() {
        Log.d("test200", "들어옴")
        CoroutineScope(Dispatchers.Main).launch {
            val work1 = async(Dispatchers.IO){
                val clickPosition = fragmentBoardReadBinding.boardReadViewModel?.clickPosition?.value
                val replyModel = replyList[clickPosition!!]
                replyModel.replyState = ReplyState.REPLY_STATE_DELETE
                ReplyService.deleteReplyData (replyModel)
            }
            work1.join()

            // 댓글 RecyclerView를 갱신한다.
            refreshReplyRecyclerView()
        }
    }

    ////////////////////////////////////////////////////////////////////////////////////////////////
    

    // 댓글 목록을 가져오는 메서드
    fun refreshReplyRecyclerView() {
        // 글의 문서 id를 가져온다.
        boardDocumentId = arguments?.getString("boardDocumentId")!!
        CoroutineScope(Dispatchers.Main).launch {
            val work1 = async(Dispatchers.IO){
                // 문서 id를 통해서 댓글 목록을 가져온다.
                ReplyService.gettingReplyList(boardDocumentId)
            }
            replyList = work1.await()

            // 댓글 RecyclerView를 구성하는 메서드를 호출한다.
            settingRecyclerViewComment()
        }

        // 댓글 RecyclerView를 갱신한다.
        fragmentBoardReadBinding.recyclerViewBoardReadComment.adapter?.notifyDataSetChanged()
    }

    // 댓글 RecyclerView를 구성하는 메서드
    fun settingRecyclerViewComment() {
        fragmentBoardReadBinding.apply {
            boardReadViewModel?.textViewBoardReadCommentCount?.value = "댓글(${replyList.size})"
            recyclerViewBoardReadComment.adapter = recyclerViewCommentAdapter()
            recyclerViewBoardReadComment.layoutManager = LinearLayoutManager(boardActivity)
            val deco = MaterialDividerItemDecoration(boardActivity, MaterialDividerItemDecoration.VERTICAL)
            recyclerViewBoardReadComment.addItemDecoration(deco)
        }
    }

    // 댓글 RecyclerView를 구성하는 어댑터
    inner class recyclerViewCommentAdapter : RecyclerView.Adapter<recyclerViewCommentAdapter.RowCommentViewHolder>() {

        inner class RowCommentViewHolder(val rowCommentBinding: RowCommentBinding) : RecyclerView.ViewHolder(rowCommentBinding.root)

        override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): RowCommentViewHolder {
            val rowCommentBinding = DataBindingUtil.inflate<RowCommentBinding>(layoutInflater, R.layout.row_comment, parent, false)
            rowCommentBinding.rowCommentViewModel = RowCommentViewModel(this@BoardReadFragment)
            rowCommentBinding.lifecycleOwner = this@BoardReadFragment

            rowCommentBinding.imageButtonRowCommentSetting.isVisible = false

            val rowCommentViewHolder = RowCommentViewHolder(rowCommentBinding)

            return rowCommentViewHolder
        }

        override fun getItemCount(): Int {
            return replyList.size
        }

        override fun onBindViewHolder(holder: RowCommentViewHolder, position: Int) {
            if (boardActivity.loginUserNickName == replyList[position].replyNickName) {
                holder.rowCommentBinding.imageButtonRowCommentSetting.isVisible = true
            }
            if (replyList[position].checkUpdate == 2) {
                holder.rowCommentBinding.rowCommentViewModel?.textViewRowCommentCheckUpdateText?.value = "(수정됨)"
            }
            holder.rowCommentBinding.rowCommentViewModel?.textViewRowCommentIDText?.value = replyList[position].replyNickName
            holder.rowCommentBinding.rowCommentViewModel?.textViewRowCommentContentText?.value = replyList[position].replyText
            holder.rowCommentBinding.rowCommentViewModel?.textViewRowCommentTimeText?.value = "${replyList[position].replyYear}년 ${replyList[position].replyMonth}월 ${replyList[position].replyDay}일"
            holder.rowCommentBinding.rowCommentViewModel?.clickPosition = position
            holder.rowCommentBinding.rowCommentViewModel?.textViewRowCommentTime2Text?.value = String.format(
                "%02d : %02d", replyList[position].replyHour, replyList[position].replyMinute
            )
        }

    }

}