package org.laughnman.multitransfer.utilities

import io.reactivex.rxjava3.observers.DefaultObserver
import mu.KotlinLogging

private val logger = KotlinLogging.logger {}

class FunctionalObserver<T>: DefaultObserver<T>() {

	var start = {
		logger.debug { "start called." }
	}

	var complete = {
		logger.debug { "complete called." }
	}

	var next = { _:T ->
		logger.debug { "next called." }
	}

	var error = { e:Throwable ->
		logger.error(e) { "error called." }
	}

	var finish = {
		logger.debug { "finish called." }
	}

	override fun onStart() {
		super.onStart()
		start()
	}

	override fun onNext(t: T) {
		next(t)
	}

	override fun onError(e: Throwable) {
		error(e)
		finish()
	}

	override fun onComplete() {
		complete()
		finish()
	}
}