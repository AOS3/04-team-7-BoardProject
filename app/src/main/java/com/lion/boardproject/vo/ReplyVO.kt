package com.lion.boardproject.vo

import com.lion.boardproject.model.ReplyModel
import com.lion.boardproject.util.ReplyState
import com.lion.boardproject.util.ReplyUpdateState

class ReplyVO {
    // 댓글 작성자 닉네임
    var replyNickName = ""
    // 댓글 내용
    var replyText = ""
    // 댓글이 달린 글 구분 값
    var replyBoardId = ""
    // 시간
    var replyTimeStamp = 0L
    // 상태
    var replyState = 0
    // 댓글 작성 년
    var replyYear = 0
    // 댓글 작성 월
    var replyMonth = 0
    // 댓글 작성 일
    var replyDay = 0
    // 댓글 작성 시
    var replyHour = 0
    // 댓글 작성 분
    var replyMinute = 0
    // 댓글 수정 여부
    var checkUpdate = ReplyUpdateState.REPLY_UPDATE_STATE_NORMAL.number

    fun toReplyModel(replyDocumentId:String) : ReplyModel{
        val replyModel = ReplyModel()
        replyModel.replyDocumentId = replyDocumentId
        replyModel.replyNickName = replyNickName
        replyModel.replyText = replyText
        replyModel.replyBoardId = replyBoardId
        replyModel.replyTimeStamp = replyTimeStamp
        when(replyState){
            ReplyState.REPLY_STATE_NORMAL.number -> {
                replyModel.replyState = ReplyState.REPLY_STATE_NORMAL
            }
            ReplyState.REPLY_STATE_DELETE.number -> {
                replyModel.replyState = ReplyState.REPLY_STATE_DELETE
            }
        }
        replyModel.replyYear = replyYear
        replyModel.replyMonth = replyMonth
        replyModel.replyDay = replyDay
        replyModel.replyHour = replyHour
        replyModel.replyMinute = replyMinute
        replyModel.checkUpdate = checkUpdate

        return replyModel
    }
}