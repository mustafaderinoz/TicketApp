package com.mustafaderinoz.core.domain.event

//Somut olarak data katmanında yapıyoruz. Event.kt ve EventRepository yapmak zorunda oldukları belirtiliyor
// data çünkü firebase istek atar yarın bir gün başka yerlerede atabilir.
interface EventRepository {
    suspend fun getEvents(): Result<List<Event>>
    suspend fun getEvent(id: String): Result<Event>
}