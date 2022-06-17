package com.keelim.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingData
import androidx.room.withTransaction
import com.keelim.common.Dispatcher
import com.keelim.common.KeelimDispatchers.IO
import com.keelim.data.api.ApiRequestFactory
import com.keelim.data.db.AppDatabase
import com.keelim.data.db.entity.History
import com.keelim.data.db.entity.SimpleHistory
import com.keelim.data.db.paging.DBPagingSource
import com.keelim.data.model.notification.Notification
import com.keelim.data.model.notification.mapepr.toNotification
import javax.inject.Inject
import kotlinx.coroutines.CoroutineDispatcher
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.withContext

class IoRepositoryImpl @Inject constructor(
    @Dispatcher(IO) private val ioDispatcher: CoroutineDispatcher,
    private val apiRequestFactory: ApiRequestFactory,
    private val db: AppDatabase,
) : IoRepository {
    override suspend fun insertHistories(history: History) = withContext(ioDispatcher) {
        db.historyDao().insertHistories(history)
    }

    override suspend fun updateHistories(history: History) = withContext(ioDispatcher) {
        db.historyDao().updateHistories(history)
    }

    override suspend fun deleteHistories(history: History) = withContext(ioDispatcher) {
        db.historyDao().updateHistories(history)
    }

    override suspend fun getAllHistories(): List<History> = withContext(ioDispatcher) {
        return@withContext try {
            db.historyDao().getAllHistory()
        } catch (e: Exception){
            emptyList()
        }
    }

    override fun loadHistoriesById(uid: Int): Flow<History> = db
        .historyDao()
        .loadHistoriesById(uid)
        .distinctUntilChanged()

    override fun loadHistoriesBySubjects(subject: String): Flow<List<History>> = db
        .historyDao()
        .loadHistoriesBySubjects(subject)
        .distinctUntilChanged()


    override val all: Flow<List<History>> = db
        .historyDao()
        .getAll()
        .distinctUntilChanged()

    override fun getAllPaging(): Flow<PagingData<History>> {
        return Pager(
            config = PagingConfig(pageSize = 10),
            pagingSourceFactory = {
                DBPagingSource(db)
            }
        ).flow
            .distinctUntilChanged()
    }

    override suspend fun getNotification(): List<Notification>  = withContext(ioDispatcher){
        return@withContext try {
            val response = apiRequestFactory.retrofit.getNotification()
            if (response.isSuccessful && response.body() != null) {
                response.body()?.toNotification() ?: emptyList()
            } else {
                emptyList()
            }
        } catch (e: Exception) {
            throw Exception(e)
        }
    }

    override suspend fun insertSimpleHistories(history: SimpleHistory) = db.withTransaction {
        db.simpleHistoryDao().insertHistories(history)
    }

    override suspend fun getAllSimpleHistories(history: SimpleHistory) = db.withTransaction{
        db.simpleHistoryDao().getAllSimpleHistory()
    }
}
