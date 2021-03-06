package io.woong.filmpedia.ui.page.search

import android.content.Context
import android.content.Intent
import android.graphics.Rect
import android.os.Bundle
import android.view.KeyEvent
import android.view.MenuItem
import android.view.MotionEvent
import android.view.View
import android.view.inputmethod.EditorInfo
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.activity.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import io.woong.filmpedia.FilmpediaApp
import io.woong.filmpedia.R
import io.woong.filmpedia.data.search.SearchResult
import io.woong.filmpedia.databinding.ActivitySearchBinding
import io.woong.filmpedia.ui.base.BaseActivity
import io.woong.filmpedia.ui.page.movie.MovieActivity
import io.woong.filmpedia.util.AnimationUtil
import io.woong.filmpedia.util.GoToTopScrollListener
import io.woong.filmpedia.util.InfinityScrollListener
import io.woong.filmpedia.util.ListDecoration

class SearchActivity : BaseActivity<ActivitySearchBinding>(R.layout.activity_search),
    View.OnClickListener,
    TextView.OnEditorActionListener,
    SearchResultListAdapter.OnSearchResultClickListener {

    private val viewModel: SearchViewModel by viewModels()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        binding.apply {
            lifecycleOwner = this@SearchActivity
            vm = viewModel

            setSupportActionBar(toolbar)
            supportActionBar?.apply {
                setDisplayHomeAsUpEnabled(true)
                setHomeAsUpIndicator(R.drawable.icon_back)
                title = resources.getString(R.string.search_toolbar_title)
            }

            searchBar.setOnEditorActionListener(this@SearchActivity)

            resultList.apply {
                adapter = SearchResultListAdapter().apply {
                    setOnSearchResultClickListener(this@SearchActivity)
                }
                layoutManager = LinearLayoutManager(this@SearchActivity, LinearLayoutManager.VERTICAL, false)
                addItemDecoration(ListDecoration.VerticalDecoration(2))
                addOnScrollListener(
                    InfinityScrollListener {
                        val app = application as FilmpediaApp
                        viewModel.searchNext(app.tmdbApiKey, app.language, app.region, searchBar.text.toString())
                    }
                )
                addOnScrollListener(
                    GoToTopScrollListener(goToTopButton, 10, 2000) {
                        this.smoothScrollToPosition(0)
                    }
                )
            }

            offline.loadAgain.setOnClickListener(this@SearchActivity)

            AnimationUtil.blink(loading, 1000)
        }

        viewModel.updateGenres(apiKey, language)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return when (item.itemId) {
            android.R.id.home -> {
                finish()
                true
            }
            else -> super.onOptionsItemSelected(item)
        }
    }

    override fun onEditorAction(v: TextView?, actionId: Int, event: KeyEvent?): Boolean {
        return if (v?.id == binding.searchBar.id) {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                val app = application as FilmpediaApp
                viewModel.search(app.tmdbApiKey, app.language, app.region, v.text.toString())
                hideKeyboard()
                true
            } else {
                false
            }
        } else {
            false
        }
    }

    override fun onClick(v: View?) {
        if (v?.id == binding.offline.loadAgain.id) {
            viewModel.updateGenres(apiKey, language)
        }
    }

    override fun onSearchResultClick(result: SearchResult?) {
        if (result != null) {
            val intent = Intent(this, MovieActivity::class.java)
            intent.putExtra(MovieActivity.MOVIE_ID_EXTRA_ID, result.id)
            intent.putExtra(MovieActivity.MOVIE_TITLE_EXTRA_ID, result.title)
            startActivity(intent)
        }
    }

    override fun dispatchTouchEvent(event: MotionEvent?): Boolean {
        if (event != null) {
            val eventX = event.x.toInt()
            val eventY = event.y.toInt()
            val focusedView = currentFocus

            if (focusedView != null) {
                val focusedRect = Rect()
                focusedView.getGlobalVisibleRect(focusedRect)

                if (isNotInRect(focusedRect, eventX, eventY)) {
                    hideKeyboard()
                    focusedView.clearFocus()
                }
            }
        }

        return super.dispatchTouchEvent(event)
    }

    private fun isNotInRect(rect: Rect, x: Int, y: Int): Boolean = rect.contains(x, y).not()

    private fun hideKeyboard() {
        val inputManager = getSystemService(Context.INPUT_METHOD_SERVICE) as InputMethodManager
        inputManager.hideSoftInputFromWindow(binding.searchBar.windowToken, 0)
    }
}
