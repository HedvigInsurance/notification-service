package com.hedvig.notificationService.service.request

import com.hedvig.notificationService.customerio.EventHandler
import com.hedvig.notificationService.customerio.dto.StartDateUpdatedEvent
import org.springframework.cglib.core.ReflectUtils
import org.springframework.stereotype.Service
import kotlin.jvm.internal.Reflection
import kotlin.reflect.KClass
import kotlin.reflect.full.createInstance

@Service
class EventRequestHandler(
    private val eventHandler: EventHandler,
//    private val handledRequestRepository: HandledRequestRepository
) {
    fun onEventRequest(
        requestId: String,
        event: Map<String, Any>
    ) {

        val name = event["name"] as String
        Reflections



//        if (handledRequestRepository.isRequestHandled(requestId)) {
//            return
//        }
//        val name = event["name"] as? String
//            ?: throw NoNameOnEventException(event)
//        val data = event["data"] as? Map<String, Any>
//            ?: throw UnableToParseDataFromEventException(event)
//        when (name) {
//            StartDateUpdatedEvent::class.simpleName -> {
//
//                val event = StartDateUpdatedEvent(
//                    data["contractId"] as? String
//                        ?: throw ,
//                )
//            }
//            else -> throw CantMapEventException(name, event)
//        }
//        handledRequestRepository.storeHandledRequest(requestId)
    }
}
/*
abstract class ParseEventException(message: String) : Throwable(message)

class NoNameOnEventException(event: Map<String, Any>) : ParseEventException("No name on event $event")
class UnableToParseDataFromEventException(event: Map<String, Any>) : ParseEventException("Unable to parse data form event: $event")
class CantMapEventException(name: String, event: Map<String, Any>) : ParseEventException("Unable to map event with name: $name [event: $event]")
class UnableToParseDataItemFromEventException(name: String, event: Map<String, Any>) : ParseEventException("Unable to parse data item form event: $event")*/