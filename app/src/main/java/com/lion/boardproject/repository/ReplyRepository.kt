package com.lion.boardproject.repository

import android.util.Log
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.lion.boardproject.model.ReplyModel
import com.lion.boardproject.util.ReplyState
import com.lion.boardproject.util.ReplyUpdateState
import com.lion.boardproject.vo.ReplyVO
import kotlinx.coroutines.tasks.await

class ReplyRepository {

    companion object {

        // 댓글 데이터를 추가하는 메서드
        suspend fun addReplyData(replyVO: ReplyVO) {

            // Firebase Firestore 인스턴스를 가져옵니다.
            val fireStore = FirebaseFirestore.getInstance()
            // 댓글 데이터를 추가할 컬렉션의 문서 ID를 가져옵니다.
            val collectionReference = fireStore.collection("BoardData")
            // 댓글 데이터를 추가합니다.
            collectionReference.document(replyVO.replyBoardId)
                // 댓글 데이터를 추가할 컬렉션의 문서 ID를 가져옵니다.
                .collection("CommentData")
                .add(replyVO)
                .addOnSuccessListener { documentReference ->    // 댓글 데이터 추가가 성공하면 로그를 출력합니다.
                    Log.d("Firestore", "댓글 추가 성공. 댓글 ID: ${documentReference.id}")
                }
                .addOnFailureListener { e ->    // 댓글 데이터 추가가 실패하면 로그를 출력합니다.
                    Log.e("Firestore", "댓글 추가 실패", e)
                }
                .await()
        }

        // 댓글 목록을 가져오는 메서드
        suspend fun gettingReplyList(boardDocumentId:String) : MutableList<Map<String, *>> {
            // Firebase Firestore 인스턴스를 가져옵니다.
            val fireStore = FirebaseFirestore.getInstance()
            // 댓글 데이터를 추가할 컬렉션의 문서 ID를 가져옵니다.
            val collectionReference = fireStore.collection("BoardData")

            // 댓글 데이터를 가져옵니다.
            val result = collectionReference.document(boardDocumentId)
                .collection("CommentData")
                .whereEqualTo("replyState",ReplyState.REPLY_STATE_NORMAL.number)
                .orderBy("replyTimeStamp", Query.Direction.ASCENDING) // replyTimeStamp 기준 오름차순 정렬
                .get()
                .await()

            // 댓글 데이터를 담을 리스트를 생성합니다.
            val replyList = mutableListOf<Map<String, *>>()

            result.forEach {
                val map = mapOf(
                    // 문서 ID를 추가합니다.
                    "documentId" to it.id,
                    // 댓글 데이터를 추가합니다.
                    "replyVO" to it.toObject(ReplyVO::class.java)
                )
                replyList.add(map)
            }
            // 댓글 데이터를 반환합니다.
            return replyList
        }

        // 댓글 데이터를 수정하는 메서드
        suspend fun updateReplyData(replyVO: ReplyVO, replyDocumentId:String){
            val fireStore = FirebaseFirestore.getInstance()
            val collectionReference = fireStore.collection("BoardData")
            val documentReference = collectionReference.document(replyVO.replyBoardId).collection("CommentData").document(replyDocumentId)

            val update = mapOf(
                "replyText" to replyVO.replyText,
                "checkUpdate" to ReplyUpdateState.REPLY_UPDATE_STATE_CHANGE.number
            )

            documentReference.update(update).await()
        }

        // 댓글 데이터를 삭제 상태로 변경하는 메서드
        suspend fun deleteReplyData(replyVO: ReplyVO, replyDocumentId:String){
            val fireStore = FirebaseFirestore.getInstance()
            val collectionReference = fireStore.collection("BoardData")
            val documentReference = collectionReference.document(replyVO.replyBoardId).collection("CommentData").document(replyDocumentId)

            val update = mapOf(
                "replyState" to ReplyState.REPLY_STATE_DELETE.number
            )

            documentReference.update(update).await()
        }
    }
}