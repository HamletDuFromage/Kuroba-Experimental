package com.github.k1rakishou.chan.features.media_viewer.media_view

import android.content.Context
import android.util.AttributeSet
import androidx.annotation.CallSuper
import com.github.k1rakishou.chan.core.base.KurobaCoroutineScope
import com.github.k1rakishou.chan.features.media_viewer.MediaViewerToolbar
import com.github.k1rakishou.chan.features.media_viewer.ViewableMedia
import com.github.k1rakishou.chan.features.media_viewer.helper.ExoPlayerWrapper
import com.github.k1rakishou.chan.ui.theme.widget.TouchBlockingFrameLayoutNoBackground
import com.github.k1rakishou.chan.ui.view.floating_menu.FloatingListMenuItem
import com.github.k1rakishou.chan.ui.widget.CancellableToast
import com.github.k1rakishou.core_logger.Logger
import com.google.android.exoplayer2.upstream.DataSource

abstract class MediaView<T : ViewableMedia, S : MediaViewState> constructor(
  context: Context,
  attributeSet: AttributeSet?,
  private val cacheDataSourceFactory: DataSource.Factory,
  protected val mediaViewContract: MediaViewContract,
  val mediaViewState: S
) : TouchBlockingFrameLayoutNoBackground(context, attributeSet, 0), MediaViewerToolbar.MediaViewerToolbarCallbacks {
  abstract val viewableMedia: T
  abstract val pagerPosition: Int
  abstract val totalPageItemsCount: Int
  abstract val hasContent: Boolean
  abstract val mediaOptions: List<FloatingListMenuItem>

  private var _mediaViewToolbar: MediaViewerToolbar? = null
  protected val mediaViewToolbar: MediaViewerToolbar?
    get() = _mediaViewToolbar

  private var _bound = false
  private var _shown = false
  private var _preloadingCalled = false
  private var _mediaFullyLoaded = false

  protected val cancellableToast by lazy { CancellableToast() }
  protected val scope = KurobaCoroutineScope()

  // May be used by all media views (including VideoMediaView) to play music in sound posts.
  protected val secondaryVideoPlayer = ExoPlayerWrapper(
    context = context,
    cacheDataSourceFactory = cacheDataSourceFactory,
    mediaViewContract = mediaViewContract,
    onAudioDetected = {}
  )

  val bound: Boolean
    get() = _bound
  val shown: Boolean
    get() = _shown

  fun initToolbar(toolbar: MediaViewerToolbar) {
    this._mediaViewToolbar = toolbar
    toolbar.onCreate(this)
  }

  fun startPreloading() {
    if (_preloadingCalled  || hasContent) {
      return
    }

    _preloadingCalled = true
    preload()

    Logger.d(TAG, "startPreloading(${pagerPosition}/${totalPageItemsCount}, ${viewableMedia.mediaLocation})")
  }

  fun onBind() {
    _bound = true
    bind()

    Logger.d(TAG, "onBind(${pagerPosition}/${totalPageItemsCount}, ${viewableMedia.mediaLocation})")
  }

  fun onShow() {
    _shown = true
    show()

    Logger.d(TAG, "onShow(${pagerPosition}/${totalPageItemsCount}, ${viewableMedia.mediaLocation})")
  }

  fun onHide() {
    _shown = false
    hide()

    Logger.d(TAG, "onHide(${pagerPosition}/${totalPageItemsCount}, ${viewableMedia.mediaLocation})")
  }

  fun onUnbind() {
    _shown = false
    _bound = false
    _preloadingCalled = false
    _mediaViewToolbar?.onDestroy()

    cancellableToast.cancel()
    scope.cancelChildren()
    unbind()
    secondaryVideoPlayer.release()

    Logger.d(TAG, "onUnbind(${pagerPosition}/${totalPageItemsCount}, ${viewableMedia.mediaLocation})")
  }

  abstract fun preload()
  abstract fun bind()
  abstract fun show()
  abstract fun hide()
  abstract fun unbind()

  @CallSuper
  open fun onSystemUiVisibilityChanged(systemUIHidden: Boolean) {
    if (systemUIHidden) {
      mediaViewToolbar?.hideToolbar()
    } else {
      mediaViewToolbar?.showToolbar()
    }
  }

  @CallSuper
  open fun hideControls() {
    mediaViewToolbar?.hideToolbar()
  }

  @CallSuper
  open fun showControls() {
    mediaViewToolbar?.showToolbar()
  }

  fun onMediaFullyLoaded() {
    if (_mediaFullyLoaded) {
      return
    }

    _mediaFullyLoaded = true

    mediaViewToolbar?.onMediaFullyLoaded()
  }

  @CallSuper
  override fun onCloseButtonClick() {
    mediaViewContract.closeMediaViewer()
  }

  override suspend fun onReloadButtonClick() {

  }

  override suspend fun onDownloadButtonClick(isLongClick: Boolean): Boolean {
    return mediaViewContract.onDownloadButtonClick(viewableMedia, isLongClick)
  }

  override fun onOptionsButtonClick() {
    mediaViewContract.onOptionsButtonClick(viewableMedia)
  }

  override fun toString(): String {
    return "MediaView(pagerPosition=$pagerPosition, totalPageItemsCount=$totalPageItemsCount, " +
      "_bound=$_bound, _shown=$_shown, _preloadingCalled=$_preloadingCalled, mediaLocation=${viewableMedia.mediaLocation})"
  }

  companion object {
    private const val TAG = "MediaView"
  }
}