package org.laughnman.multitransfer.services.transfer

import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.flow.Flow
import org.laughnman.multitransfer.models.transfer.MetaInfo
import org.laughnman.multitransfer.models.transfer.TransferInfo
import java.nio.ByteBuffer


interface TransferSourceService {

	fun read(): Flow<Pair<MetaInfo, Flow<TransferInfo>>>

	suspend fun buildObservableSequence(): Sequence<Pair<MetaInfo, Observable<ByteBuffer>>>

}