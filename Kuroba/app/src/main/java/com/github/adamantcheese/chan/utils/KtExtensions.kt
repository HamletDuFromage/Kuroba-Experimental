package com.github.adamantcheese.chan.utils

import android.content.Context
import android.content.ContextWrapper
import android.view.View
import androidx.lifecycle.Lifecycle
import com.airbnb.epoxy.*
import com.github.adamantcheese.chan.StartActivity
import com.github.adamantcheese.chan.controller.Controller
import io.reactivex.disposables.CompositeDisposable
import io.reactivex.disposables.Disposable
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlin.coroutines.resume

operator fun CompositeDisposable.plusAssign(disposable: Disposable) {
  this.add(disposable)
}


fun EpoxyRecyclerView.withModelsAsync(buildModels: EpoxyController.() -> Unit) {
  val controller = object : AsyncEpoxyController(true) {
    override fun buildModels() {
      buildModels(this)
    }
  }

  setController(controller)
  controller.requestModelBuild()
}

fun EpoxyController.addOneshotModelBuildListener(callback: () -> Unit) {
  addModelBuildListener(object : OnModelBuildFinishedListener {
    override fun onModelBuildFinished(result: DiffResult) {
      callback()

      removeModelBuildListener(this)
    }
  })
}

fun Context.getLifecycleFromContext(): Lifecycle? {
  return when (this) {
    is StartActivity -> this.lifecycle
    is ContextWrapper -> (this.baseContext as? StartActivity)?.lifecycle
    else -> null
  }
}

suspend fun View.awaitUntilLaidOut(continueRendering: Boolean = true) {
  suspendCancellableCoroutine<Unit> { cancellableContinuation ->
    AndroidUtils.waitForLayout(this) {
      cancellableContinuation.resume(Unit)
      return@waitForLayout continueRendering
    }
  }
}

fun Controller.findControllerOrNull(predicate: (Controller) -> Boolean): Controller? {
  if (predicate(this)) {
    return this
  }

  for (childController in childControllers) {
    val result = childController.findControllerOrNull(predicate)
    if (result != null) {
      return result
    }
  }

  return null
}