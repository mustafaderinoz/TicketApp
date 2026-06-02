// core katmanındaki EventRepository bizden Event listesi bekliyor burada repository yazacağız
//interface EventRepository {
//    suspend fun getEvents(): Result<List<Event>>
//}
package com.mustafaderinoz.data.repository

import com.mustafaderinoz.core.domain.event.Event
import com.mustafaderinoz.core.domain.event.EventRepository
import com.mustafaderinoz.data.mapper.toDomain
import com.mustafaderinoz.data.remote.EventApi
import com.mustafaderinoz.data.util.runCatchingApi

// Şu an eventapi kullanıyoruz daha sonra bu api değişebilir.

class EventRepositoryImpl(
    private val eventApi: EventApi
) : EventRepository {
    override suspend fun getEvents(): Result<List<Event>> = runCatchingApi { eventApi.getEvents() }.map { list -> list.map { it.toDomain() }}
    override suspend fun getEvent(id: String): Result<Event> =
        runCatchingApi { eventApi.getEvent(id) }.map { it.toDomain() }
}

// apiden isteği çağır gelen elemanları domain nesnesine maple böylelikle geriye event listesi dönecek
// biz şimdi bir sürü bağımlılık ekledik datamodule kısmında gerekli eklemeleri yap