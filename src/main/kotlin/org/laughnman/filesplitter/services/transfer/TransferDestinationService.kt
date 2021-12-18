package org.laughnman.filesplitter.services.transfer

import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.channels.ReceiveChannel
import org.laughnman.filesplitter.models.transfer.MetaInfo
import org.laughnman.filesplitter.models.transfer.TransferInfo

interface TransferDestinationService {

	fun write(metaInfo: MetaInfo, input: ReceiveChannel<TransferInfo>)

}