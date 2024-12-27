package com.lion.boardproject.service

import android.util.Log
import com.lion.boardproject.model.ReplyModel
import com.lion.boardproject.repository.ReplyRepository
import com.lion.boardproject.repository.UserRepository
import com.lion.boardproject.vo.ReplyVO

class ReplyService {

    companion object {
        // 댓글을 추가하는 메서드
        suspend fun addReplyData(replyModel: ReplyModel) {

            // ReplyVO로 변환한다.
            val replyVO = replyModel.toReplyVO()

            // Firebase Firestore에 댓글 데이터를 추가한다.
            ReplyRepository.addReplyData(replyVO)
        }

        // 댓글 목록을 가져오는 메서드
        suspend fun gettingReplyList(boardDocumentId:String) : MutableList<ReplyModel> {
            val replyList = mutableListOf<ReplyModel>()
            val resultList = ReplyRepository.gettingReplyList(boardDocumentId)

            resultList.forEach {
                val replyVO = it["replyVO"] as ReplyVO
                val documentId = it["documentId"] as String
                val replyModel = replyVO.toReplyModel(documentId)
                replyList.add(replyModel)
            }

            return replyList
        }

        // 댓글 데이터를 수정하는 메서드
        suspend fun updateReplyData(replyModel: ReplyModel){
            val boardVO = replyModel.toReplyVO()
            val replyDocumentId = replyModel.replyDocumentId
            ReplyRepository.updateReplyData(boardVO, replyDocumentId)
        }

        suspend fun deleteReplyData(replyModel: ReplyModel){
            val boardVO = replyModel.toReplyVO()
            val replyDocumentId = replyModel.replyDocumentId
            ReplyRepository.deleteReplyData(boardVO, replyDocumentId)
        }
    }
}