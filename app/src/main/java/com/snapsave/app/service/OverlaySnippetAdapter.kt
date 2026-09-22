package com.snapsave.app.service

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Color
import android.graphics.Typeface
import android.graphics.drawable.GradientDrawable
import android.text.TextUtils
import android.view.Gravity
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import android.widget.FrameLayout
import android.widget.ImageView
import android.widget.LinearLayout
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.snapsave.app.R
import com.snapsave.app.core.formatSize
import com.snapsave.app.data.SnippetEntity
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext

internal class OverlaySnippetAdapter(
    private val scope: CoroutineScope,
    private val onSelected: (View, SnippetEntity) -> Unit,
    private val onLongClick: ((View, SnippetEntity) -> Unit)? = null
) : RecyclerView.Adapter<OverlaySnippetAdapter.Holder>() {

    var isGrid: Boolean = true
        set(value) {
            if (field != value) {
                field = value
                notifyDataSetChanged()
            }
        }

    companion object {
        const val VIEW_TYPE_LIST = 0
        const val VIEW_TYPE_GRID = 1
    }

    private var snippets = emptyList<SnippetEntity>()
    private var submitGeneration = 0L

    init {
        setHasStableIds(true)
    }

    class Holder(
        val frame: FrameLayout,
        val cardBg: GradientDrawable,
        val titleView: TextView,
        val langBadge: TextView,
        val metaView: TextView,
        val previewView: TextView,
        val copyIcon: ImageView?
    ) : RecyclerView.ViewHolder(frame) {
        var snippet: SnippetEntity? = null
    }

    override fun getItemCount(): Int = snippets.size

    override fun getItemId(position: Int): Long =
        snippets.getOrNull(position)?.id ?: RecyclerView.NO_ID

    override fun getItemViewType(position: Int): Int =
        if (isGrid) VIEW_TYPE_GRID else VIEW_TYPE_LIST

    fun submit(items: List<SnippetEntity>, force: Boolean = false) {
        val generation = ++submitGeneration
        if (!force && items == snippets) return
        if (snippets.isEmpty() && items.isNotEmpty()) {
            snippets = items
            notifyDataSetChanged()
            return
        }
        val oldItems = snippets
        scope.launch {
            val diff = withContext(Dispatchers.Default) {
                DiffUtil.calculateDiff(object : DiffUtil.Callback() {
                    override fun getOldListSize(): Int = oldItems.size
                    override fun getNewListSize(): Int = items.size
                    override fun areItemsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                        oldItems[oldItemPosition].id == items[newItemPosition].id

                    override fun areContentsTheSame(oldItemPosition: Int, newItemPosition: Int): Boolean =
                        oldItems[oldItemPosition] == items[newItemPosition]
                })
            }
            if (generation != submitGeneration) return@launch
            snippets = items
            diff.dispatchUpdatesTo(this@OverlaySnippetAdapter)
        }
    }

    private fun withAlpha(color: Int, alpha: Int): Int = Color.argb(
        alpha.coerceIn(0, 255),
        Color.red(color),
        Color.green(color),
        Color.blue(color)
    )

    @SuppressLint("ClickableViewAccessibility")
    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): Holder {
        val context = parent.context
        val density = context.resources.displayMetrics.density
        val palette = SnippetOverlayPaletteResolver.resolve(context)
        val shadowStrength = SnippetOverlayPreferences.snippetShadowStrength(context)

        val cardBg = GradientDrawable().apply {
            shape = GradientDrawable.RECTANGLE
            cornerRadius = 14 * density
            setColor(withAlpha(palette.surfaceColor, if (palette.isDark) 240 else 250))
            setStroke(
                (1 * density).toInt(),
                withAlpha(palette.outlineColor, if (palette.isDark) 60 else 80)
            )
        }

        if (viewType == VIEW_TYPE_GRID) {
            // Compact Grid Card (2-column layout)
            val frame = FrameLayout(context).apply {
                isClickable = true
                isFocusable = true
                background = cardBg
                elevation = (5f * shadowStrength * density).coerceAtLeast(0f)
                val pad = (9 * density).toInt()
                setPadding(pad, pad, pad, pad)
                val lp = RecyclerView.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    val marginH = (3 * density).toInt()
                    val marginV = (3 * density).toInt()
                    setMargins(marginH, marginV, marginH, marginV)
                }
                layoutParams = lp
            }

            val container = LinearLayout(context).apply {
                orientation = LinearLayout.VERTICAL
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            // Top Row: Lang badge + Line count
            val topRow = LinearLayout(context).apply {
                orientation = LinearLayout.HORIZONTAL
                gravity = Gravity.CENTER_VERTICAL
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, (4 * density).toInt())
                }
            }

            val langBadge = TextView(context).apply {
                textSize = 9.5f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(palette.selectedChipContentColor)
                val badgeBg = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 6 * density
                    setColor(palette.primaryColor)
                }
                background = badgeBg
                val pH = (6 * density).toInt()
                val pV = (2 * density).toInt()
                setPadding(pH, pV, pH, pV)
            }

            val metaView = TextView(context).apply {
                textSize = 9.5f
                setTextColor(palette.mutedTextColor)
                gravity = Gravity.END
                layoutParams = LinearLayout.LayoutParams(
                    0,
                    ViewGroup.LayoutParams.WRAP_CONTENT,
                    1f
                )
            }

            topRow.addView(langBadge)
            topRow.addView(metaView)
            container.addView(topRow)

            // Title View (Bold, 1 line truncate)
            val titleView = TextView(context).apply {
                textSize = 12f
                typeface = Typeface.DEFAULT_BOLD
                setTextColor(palette.textColor)
                maxLines = 1
                ellipsize = TextUtils.TruncateAt.END
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                ).apply {
                    setMargins(0, 0, 0, (4 * density).toInt())
                }
            }
            container.addView(titleView)

            // Monospace Preview Box (compact 2 lines)
            val previewContainer = FrameLayout(context).apply {
                val bg = GradientDrawable().apply {
                    shape = GradientDrawable.RECTANGLE
                    cornerRadius = 8 * density
                    setColor(withAlpha(palette.surfaceVariantColor, if (palette.isDark) 140 else 200))
                }
                background = bg
                val p = (5 * density).toInt()
                setPadding(p, p, p, p)
                layoutParams = LinearLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }

            val previewView = TextView(context).apply {
                typeface = Typeface.MONOSPACE
                textSize = 10f
                setTextColor(if (palette.isDark) Color.parseColor("#E0E3EC") else Color.parseColor("#2E333D"))
                maxLines = 2
                ellipsize = TextUtils.TruncateAt.END
                layoutParams = FrameLayout.LayoutParams(
                    ViewGroup.LayoutParams.MATCH_PARENT,
                    ViewGroup.LayoutParams.WRAP_CONTENT
                )
            }
            previewContainer.addView(previewView)
            container.addView(previewContainer)

            frame.addView(container)

            val holder = Holder(frame, cardBg, titleView, langBadge, metaView, previewView, null)

            frame.setOnClickListener {
                holder.snippet?.let { onSelected(frame, it) }
            }

            frame.setOnLongClickListener {
                holder.snippet?.let { snippet ->
                    onLongClick?.invoke(frame, snippet)
                }
                true
            }

            frame.setOnTouchListener { view, event ->
                when (event.actionMasked) {
                    MotionEvent.ACTION_DOWN -> view.animate().scaleX(0.96f).scaleY(0.96f).setDuration(80).start()
                    MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> view.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
                }
                false
            }

            return holder
        }

        // Full-width List Card
        val frame = FrameLayout(context).apply {
            isClickable = true
            isFocusable = true
            background = cardBg
            elevation = (6f * shadowStrength * density).coerceAtLeast(0f)
            val pad = (10 * density).toInt()
            setPadding(pad, pad, pad, pad)
            val lp = RecyclerView.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                val marginH = (4 * density).toInt()
                val marginV = (4 * density).toInt()
                setMargins(marginH, marginV, marginH, marginV)
            }
            layoutParams = lp
        }

        val container = LinearLayout(context).apply {
            orientation = LinearLayout.VERTICAL
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }

        // Top Row: Lang badge + Title + Copy icon
        val topRow = LinearLayout(context).apply {
            orientation = LinearLayout.HORIZONTAL
            gravity = Gravity.CENTER_VERTICAL
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, (6 * density).toInt())
            }
        }

        val langBadge = TextView(context).apply {
            textSize = 10f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(palette.selectedChipContentColor)
            val badgeBg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 8 * density
                setColor(palette.primaryColor)
            }
            background = badgeBg
            val pH = (7 * density).toInt()
            val pV = (2 * density).toInt()
            setPadding(pH, pV, pH, pV)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, (8 * density).toInt(), 0)
            }
        }

        val titleView = TextView(context).apply {
            textSize = 13.5f
            typeface = Typeface.DEFAULT_BOLD
            setTextColor(palette.textColor)
            maxLines = 1
            ellipsize = TextUtils.TruncateAt.END
            layoutParams = LinearLayout.LayoutParams(
                0,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                1f
            )
        }

        val copyIcon = ImageView(context).apply {
            setImageDrawable(ContextCompat.getDrawable(context, R.drawable.ic_overlay_copy))
            setColorFilter(palette.primaryColor)
            val iconSize = (16 * density).toInt()
            layoutParams = LinearLayout.LayoutParams(iconSize, iconSize).apply {
                setMargins((6 * density).toInt(), 0, 0, 0)
            }
        }

        topRow.addView(langBadge)
        topRow.addView(titleView)
        topRow.addView(copyIcon)
        container.addView(topRow)

        // Monospace Preview Box (bo tròn 10dp)
        val previewContainer = FrameLayout(context).apply {
            val bg = GradientDrawable().apply {
                shape = GradientDrawable.RECTANGLE
                cornerRadius = 10 * density
                setColor(withAlpha(palette.surfaceVariantColor, if (palette.isDark) 140 else 200))
            }
            background = bg
            val p = (7 * density).toInt()
            setPadding(p, p, p, p)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            ).apply {
                setMargins(0, 0, 0, (4 * density).toInt())
            }
        }

        val previewView = TextView(context).apply {
            typeface = Typeface.MONOSPACE
            textSize = 11f
            setTextColor(if (palette.isDark) Color.parseColor("#E0E3EC") else Color.parseColor("#2E333D"))
            maxLines = 2
            ellipsize = TextUtils.TruncateAt.END
            layoutParams = FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        previewContainer.addView(previewView)
        container.addView(previewContainer)

        // Meta info (lines count · size)
        val metaView = TextView(context).apply {
            textSize = 10.5f
            setTextColor(palette.mutedTextColor)
            layoutParams = LinearLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
            )
        }
        container.addView(metaView)

        frame.addView(container)

        val holder = Holder(frame, cardBg, titleView, langBadge, metaView, previewView, copyIcon)

        frame.setOnClickListener {
            holder.snippet?.let { onSelected(frame, it) }
        }

        frame.setOnLongClickListener {
            holder.snippet?.let { snippet ->
                onLongClick?.invoke(frame, snippet)
            }
            true
        }

        frame.setOnTouchListener { view, event ->
            when (event.actionMasked) {
                MotionEvent.ACTION_DOWN -> view.animate().scaleX(0.97f).scaleY(0.97f).setDuration(80).start()
                MotionEvent.ACTION_UP, MotionEvent.ACTION_CANCEL -> view.animate().scaleX(1f).scaleY(1f).setDuration(120).start()
            }
            false
        }

        return holder
    }

    override fun onBindViewHolder(holder: Holder, position: Int) {
        val snippet = snippets.getOrNull(position)
        if (snippet == null) {
            holder.snippet = null
            return
        }
        holder.snippet = snippet
        holder.frame.findViewWithTag<View>("copied_feedback_badge")?.let(holder.frame::removeView)
        holder.frame.animate().cancel()
        holder.frame.scaleX = 1f
        holder.frame.scaleY = 1f

        holder.titleView.text = snippet.title
        holder.langBadge.text = snippet.extension.uppercase().ifBlank { snippet.language.uppercase() }
        if (isGrid) {
            holder.metaView.text = "${snippet.lineCount}L"
        } else {
            holder.metaView.text = "${snippet.lineCount} dòng · ${formatSize(snippet.sizeBytes)}"
        }
        holder.previewView.text = snippet.preview.trim().ifBlank { "(Snippet trống)" }
    }
}
