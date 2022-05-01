package org.laughnman.multitransfer.services.transfer

import io.ktor.client.features.*
import io.ktor.http.*
import io.reactivex.rxjava3.core.Observer
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.collect
import kotlinx.coroutines.runBlocking
import mu.KotlinLogging
import org.laughnman.multitransfer.dao.ArtifactoryDao
import org.laughnman.multitransfer.models.transfer.ArtifactoryDestinationCommand
import org.laughnman.multitransfer.models.transfer.MetaInfo
import org.laughnman.multitransfer.models.transfer.TransferInfo
import org.laughnman.multitransfer.utilities.FunctionalObserver
import java.nio.ByteBuffer

private val logger = KotlinLogging.logger {}

class ArtifactoryTransferDestinationServiceImpl(private val command: ArtifactoryDestinationCommand, private val artifactoryDao: ArtifactoryDao) : TransferDestinationService {

	override suspend fun write(metaInfo: MetaInfo, input: Flow<TransferInfo>) {
		logger.debug { "Calling write with metaInfo: $metaInfo" }

		// If the file path ends with a slash then it is assumed we are writing to a directory and the file name will need to be added.
		val filePath = if (command.filePath.endsWith("/")) "${command.filePath}${metaInfo.fileName}" else command.filePath

		val buffer = ArrayList<Byte>()

		logger.info { "Buffering ${metaInfo.fileName} for transfer to Artifactory." }
		// Copy each byte array into the buffer.
		// TODO: Find a more optimized way to do this.
		input.collect { transferInfo ->
			for (i in 0 until transferInfo.bytesRead) {
				buffer.add(transferInfo.buffer[i])
			}
		}

		logger.info { "Deploying file ${metaInfo.fileName} to $filePath" }
		val bufferArr = buffer.toByteArray()

		val fileInfo = try {
			artifactoryDao.deployArtifactWithChecksum(command.url, filePath, bufferArr, command.userName, command.exclusive.password, command.exclusive.token)
		}
		catch (e: ClientRequestException) {
			if (e.response.status == HttpStatusCode.NotFound) {
				logger.info { "Cached file not found for ${metaInfo.fileName}, uploading new version." }
				artifactoryDao.deployArtifact(command.url, filePath, bufferArr, command.userName, command.exclusive.password, command.exclusive.token)
			}
			else {
				throw e
			}
		}

		logger.info { "File ${metaInfo.fileName} successfully deployed to ${fileInfo.downloadUri}" }
	}

	private suspend fun test() {

	}

	override suspend fun buildSubscriber(metaInfo: MetaInfo): Observer<ByteBuffer> = FunctionalObserver<ByteBuffer>().also {
		logger.debug { "Calling write with metaInfo: $metaInfo" }
		// If the file path ends with a slash then it is assumed we are writing to a directory and the file name will need to be added.
		val filePath = if (command.filePath.endsWith("/")) "${command.filePath}${metaInfo.fileName}" else command.filePath

		val buffer = ByteBuffer.allocate(metaInfo.fileSize.toInt())
		it.next = { inBuff ->
			buffer.put(inBuff)
		}
		it.complete = {
			runBlocking {
				val fileInfo = try {
					artifactoryDao.deployArtifactWithChecksum(command.url, filePath, buffer.array(), command.userName,
						command.exclusive.password, command.exclusive.token)
				} catch (e: ClientRequestException) {
					if (e.response.status == HttpStatusCode.NotFound) {
						logger.info { "Cached file not found for ${metaInfo.fileName}, uploading new version." }
						artifactoryDao.deployArtifact(command.url, filePath, buffer.array(), command.userName, command.exclusive.password,
							command.exclusive.token)
					} else {
						throw e
					}
				}
				logger.info { "File ${metaInfo.fileName} successfully deployed to ${fileInfo.downloadUri}" }
			}
		}
	}
}