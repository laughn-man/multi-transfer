package org.laughnman.multitransfer.services.transfer

import io.reactivex.rxjava3.core.Observable
import kotlinx.coroutines.flow.flow
import kotlinx.coroutines.delay
import kotlinx.coroutines.rx3.rxObservable
import org.laughnman.multitransfer.dao.ArtifactoryDao
import org.laughnman.multitransfer.models.transfer.ArtifactorySourceCommand
import org.laughnman.multitransfer.models.transfer.MetaInfo
import org.laughnman.multitransfer.models.transfer.TransferInfo
import org.laughnman.multitransfer.utilities.findFileName
import java.nio.ByteBuffer

private const val SUSPEND_TIME = 100L

class ArtifactoryTransferSourceServiceImpl(private val command: ArtifactorySourceCommand,	private val artifactoryDao: ArtifactoryDao) : TransferSourceService {

	private suspend fun buildMetaInfo(filePath: String): MetaInfo {
		val fileInfo = artifactoryDao.getFileInfo(command.url, filePath, command.userName, command.exclusive.password, command.exclusive.token)
		return MetaInfo(fileName = filePath.findFileName(), fileSize = fileInfo.size)
	}

	private suspend fun buildFlow(filePath: String) = flow {
		artifactoryDao.downloadArtifact(command.url, filePath, command.userName, command.exclusive.password, command.exclusive.token) { channel ->

			val buffer = ByteArray(command.bufferSize.toBytes().toInt())

			while (!channel.isClosedForRead) {
				val readLength = channel.readAvailable(buffer, 0, buffer.size)

				if (readLength == 0) {
					delay(SUSPEND_TIME)
				}
				else if (readLength > 0) {
					emit(TransferInfo(buffer, readLength))
				}
			}
		}
	}

	override fun read() = flow {
		command.filePaths.map { path ->
			// If the path is a folder then get the folder children and emit for each file. Else just emit the file as normal.
			if (path.endsWith("/")) {
				// Get all the files in the folder.
				val folderInfo = artifactoryDao.getFolderInfo(command.url, path, command.userName, command.exclusive.password, command.exclusive.token)
				folderInfo.children
					.filter { !it.folder }
					.forEach { child ->
						val filePath = "${folderInfo.repo}/${folderInfo.path}${child.uri}"
						val metaInfo = buildMetaInfo(filePath)
						val flow = buildFlow(filePath)
						emit(Pair(metaInfo, flow))
					}
			}
			else {
				val metaInfo = buildMetaInfo(path)
				val flow = buildFlow(path)
				emit(Pair(metaInfo, flow))
			}
		}
	}

	private fun buildObservable(filePath: String): Observable<ByteBuffer> = rxObservable {
		artifactoryDao.downloadArtifact(command.url, filePath, command.userName, command.exclusive.password, command.exclusive.token) { channel ->
			val bufferSize = command.bufferSize.toBytes().toInt()

			while (!channel.isClosedForRead) {
				val buffer = ByteArray(bufferSize)
				val readLength = channel.readAvailable(buffer, 0, buffer.size)

				if (readLength == 0) {
					delay(SUSPEND_TIME)
				}
				else if (readLength > 0) {
					send(ByteBuffer.wrap(buffer, 0, readLength))
				}
			}
		}
	}

	override suspend fun buildObservableSequence(): Sequence<Pair<MetaInfo, Observable<ByteBuffer>>> = command.filePaths.flatMap { path ->
		// If the path is a folder then get the folder children and emit for each file. Else just emit the file as normal.
		if (path.endsWith("/")) {
			// Get all the files in the folder.
			val folderInfo = artifactoryDao.getFolderInfo(command.url, path, command.userName, command.exclusive.password, command.exclusive.token)
			folderInfo.children
				.filter { !it.folder }
				.map { child ->
					val filePath = "${folderInfo.repo}/${folderInfo.path}${child.uri}"
					val metaInfo = buildMetaInfo(filePath)
					val observable = buildObservable(filePath)
					Pair(metaInfo, observable)
				}
		}
		else {
			val metaInfo = buildMetaInfo(path)
			val observable = buildObservable(path)
			listOf(Pair(metaInfo, observable))
		}
	}.asSequence()
}