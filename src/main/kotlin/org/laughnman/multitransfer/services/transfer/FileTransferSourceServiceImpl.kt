package org.laughnman.multitransfer.services.transfer

import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.flow.flowOn
import kotlinx.coroutines.rx3.rxObservable
import mu.KotlinLogging
import org.laughnman.multitransfer.dao.FileDao
import org.laughnman.multitransfer.models.transfer.FileSourceCommand
import org.laughnman.multitransfer.models.transfer.MetaInfo
import org.laughnman.multitransfer.models.transfer.TransferInfo
import org.laughnman.multitransfer.utilities.readAsSequence
import java.nio.ByteBuffer
import java.nio.file.Path
import kotlin.io.path.fileSize
import kotlin.io.path.name

private val logger = KotlinLogging.logger {}

class FileTransferSourceServiceImpl(private val command: FileSourceCommand, private val fileDao: FileDao) : TransferSourceService {

	private fun buildFlow(path: Path): Flow<TransferInfo> = flow {
		logger.debug { "Calling buildSequence path: $path" }

		fileDao.openForRead(path.toFile()).use { fin ->
			fin.readAsSequence(command.bufferSize.toBytes().toInt()).forEach { (readLength, buffer) ->
				emit(TransferInfo(buffer, readLength))
			}
		}
	}.flowOn(Dispatchers.IO)

	private fun buildObservable(path: Path): Observable<ByteBuffer> = rxObservable(Dispatchers.IO) {
		logger.debug { "Starting observable for path $path" }
		fileDao.openForRead(path.toFile()).use { fin ->
			fin.readAsSequence(command.bufferSize.toBytes().toInt()).forEach { (readLength, byteArr) ->
				val buffer = ByteBuffer.wrap(byteArr, 0, readLength)
				send(buffer)
			}
		}
	}


	override fun read() = flow {
		command.filePaths.map { path ->
			val metaInfo = MetaInfo(fileName = path.name, fileSize = path.fileSize())
			emit(Pair(metaInfo, buildFlow(path)))
		}
	}
	override suspend fun buildObservableSequence(): Sequence<Pair<MetaInfo, Observable<ByteBuffer>>> = command.filePaths.map { path ->
		val metaInfo = MetaInfo(fileName = path.name, fileSize = path.fileSize())
		Pair(metaInfo, buildObservable(path))
	}.asSequence()

}