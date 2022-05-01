package org.laughnman.multitransfer.services.transfer

import io.reactivex.rxjava3.core.Observer
import kotlinx.coroutines.flow.Flow
import org.laughnman.multitransfer.models.transfer.MetaInfo
import org.laughnman.multitransfer.models.transfer.TransferInfo
import org.reactivestreams.Subscriber
import java.nio.ByteBuffer


interface TransferDestinationService {

	suspend fun write(metaInfo: MetaInfo, input: Flow<TransferInfo>)

	suspend fun buildSubscriber(metaInfo: MetaInfo): Observer<ByteBuffer>

}