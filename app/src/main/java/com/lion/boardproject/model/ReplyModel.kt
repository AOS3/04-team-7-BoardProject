package com.lion.boardproject.model

import com.lion.boardproject.util.ReplyState
import com.lion.boardproject.util.ReplyUpdateState
import com.lion.boardproject.vo.ReplyVO

class ReplyModel {
    // 댓글 문서 id
    var replyDocumentId = ""
    // 댓글 작성자 닉네임
    var replyNickName = ""
    // 댓글 내용
    var replyText = ""
    // 댓글이 달린 글 구분 값
    var replyBoardId = ""
    // 시간
    var replyTimeStamp = 0L
    // 상태
    var replyState = ReplyState.REPLY_STATE_NORMAL
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

    fun toReplyVO() : ReplyVO{
        val replyVO = ReplyVO()

        replyVO.replyNickName = replyNickName
        replyVO.replyText = replyText
        replyVO.replyBoardId = replyBoardId
        replyVO.replyTimeStamp = replyTimeStamp
        replyVO.replyState = replyState.number
        replyVO.replyYear = replyYear
        replyVO.replyMonth = replyMonth
        replyVO.replyDay = replyDay
        replyVO.replyHour = replyHour
        replyVO.replyMinute = replyMinute
        replyVO.checkUpdate = checkUpdate

        return replyVO
    }
}