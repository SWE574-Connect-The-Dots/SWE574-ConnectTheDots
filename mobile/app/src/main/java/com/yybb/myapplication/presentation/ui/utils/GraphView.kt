package com.yybb.myapplication.presentation.ui.utils

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.ScaleGestureDetector
import android.view.View
import android.app.AlertDialog
import android.widget.Toast
import kotlin.math.atan2
import kotlin.math.cos
import kotlin.math.sqrt
import kotlin.math.pow
import kotlin.math.min
import kotlin.math.max
import kotlin.math.sin
import android.text.*

data class Node(
    var id: Int,
    var x: Float,
    var y: Float,
    val radius: Float = 120f,
    val title: String = "",
    val description: String = ""
)

data class Edge(
    val from: Node,
    val to: Node,
    val description: String
)

class GraphView(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {

    val nodes = mutableListOf<Node>()
    val edges = mutableListOf<Edge>()

    private var draggingNode: Node? = null
    private var lastTouchX = 0f
    private var lastTouchY = 0f


    private val edgePaint = Paint().apply {
        color = Color.BLACK
        strokeWidth = 5f
        isAntiAlias = true
    }

    private val edgeTextPaint = Paint().apply {
        color = Color.MAGENTA
        textSize = 41f
        isAntiAlias = true
    }

    // Pan & Zoom
    var scaleFactor = 1f
    private var minScaleFactor = 0.5f
    private val scaleDetector = ScaleGestureDetector(context, ScaleListener())
    var offsetX = 0f
    var offsetY = 0f
    private var isDragging = false

    init {
        val rnd = kotlin.random.Random.Default
        val canvasWidth = 4000f
        val canvasHeight = 4000f
        val radius = 120f
        val clusterRadius = 400f

        // Step 1: Create nodes
        for (i in 1..5) {
            nodes.add(
                Node(
                    id = i,
                    x = rnd.nextFloat() * canvasWidth,
                    y = rnd.nextFloat() * canvasHeight,
                    radius = radius,
                    title = "Node $i",
                    description = "This is node $i"
                )
            )
        }

        // Step 2: Generate edges (connections)
        val nodeCount = nodes.size
        val edgeSet = mutableSetOf<Pair<Int, Int>>()
        while (edgeSet.size < 3) {
            val fromIdx = rnd.nextInt(nodeCount)
            var toIdx = rnd.nextInt(nodeCount)
            if (fromIdx == toIdx) continue
            val pair = if (fromIdx < toIdx) Pair(fromIdx, toIdx) else Pair(toIdx, fromIdx)
            if (edgeSet.add(pair)) {
                edges.add(
                    Edge(
                        from = nodes[fromIdx],
                        to = nodes[toIdx],
                        description = "Edge from ${nodes[fromIdx].title} to ${nodes[toIdx].title}"
                    )
                )
            }
        }

        // Step 3: Reposition connected nodes closer together
        for (edge in edges) {
            val from = edge.from
            val to = edge.to

            val angle = rnd.nextFloat() * 2 * Math.PI
            val distance = clusterRadius * (0.5f + rnd.nextFloat() * 0.5f)
            to.x = from.x + (distance * kotlin.math.cos(angle)).toFloat()
            to.y = from.y + (distance * kotlin.math.sin(angle)).toFloat()
        }

        relaxLayout()
    }

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)

        canvas.save()
        canvas.translate(offsetX, offsetY)
        canvas.scale(scaleFactor, scaleFactor)

        // Draw edges
        for (edge in edges) {
            canvas.drawLine(edge.from.x, edge.from.y, edge.to.x, edge.to.y, edgePaint)
            val midX = (edge.from.x + edge.to.x) / 2
            val midY = (edge.from.y + edge.to.y) / 2
            canvas.drawText(edge.description, midX, midY, edgeTextPaint)
        }

        // Paints
        val borderPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            style = Paint.Style.STROKE
            strokeWidth = 4f
        }

        val textPaint = TextPaint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.BLACK
            textSize = 28f
            textAlign = Paint.Align.CENTER
        }

        val fillPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
            color = Color.WHITE
            style = Paint.Style.FILL
        }

        // Draw nodes
        for (node in nodes) {
            // Draw the circle
            canvas.drawCircle(node.x, node.y, node.radius, fillPaint)
            canvas.drawCircle(node.x, node.y, node.radius, borderPaint)

            // Constrain text width (slightly less than diameter)
            val maxTextWidth  = (node.radius * 1.5f).toInt()

            // Build StaticLayout for wrapped text
            val staticLayout = StaticLayout.Builder.obtain(
                node.title, 0, node.title.length, textPaint, maxTextWidth
            )
                .setAlignment(Layout.Alignment.ALIGN_CENTER)
                .setIncludePad(false)
                .build()

            val textHeight = staticLayout.height
            val textWidth = staticLayout.width.toFloat()

            // Center text block in circle
            val textX = node.x - textWidth / 6f
            val textY = node.y - textHeight / 2f

            canvas.save()
            canvas.translate(textX, textY)
            staticLayout.draw(canvas)
            canvas.restore()
        }

        canvas.restore()
    }



    override fun onTouchEvent(event: MotionEvent): Boolean {
        scaleDetector.onTouchEvent(event)

        val x = (event.x - offsetX) / scaleFactor
        val y = (event.y - offsetY) / scaleFactor

        when (event.action) {
            MotionEvent.ACTION_DOWN -> {
                draggingNode = nodes.find { dist(it.x, it.y, x, y) <= it.radius }
                lastTouchX = event.x
                lastTouchY = event.y
            }

            MotionEvent.ACTION_MOVE -> {
                if (draggingNode != null) {
                    draggingNode!!.x = x
                    draggingNode!!.y = y
                    isDragging = true
                    invalidate()
                } else {
                    offsetX += event.x - lastTouchX
                    offsetY += event.y - lastTouchY
                    invalidate()
                }
                lastTouchX = event.x
                lastTouchY = event.y
            }

            MotionEvent.ACTION_UP -> {
                if (!isDragging) {
                    val tappedNode = nodes.find { dist(it.x, it.y, x, y) <= it.radius }
                    if (tappedNode != null) {
                        showNodeDetails(tappedNode)
                    }
                }
                draggingNode = null
                isDragging = false
            }
        }
        return true
    }

    private fun dist(x1: Float, y1: Float, x2: Float, y2: Float) =
        sqrt((x1 - x2).pow(2) + (y1 - y2).pow(2))

    private fun showNodeDetails(node: Node) {
        AlertDialog.Builder(context)
            .setTitle(node.title)
            .setMessage(node.description)
            .setPositiveButton("OK", null)
            .show()
    }

    inner class ScaleListener : ScaleGestureDetector.SimpleOnScaleGestureListener() {
        override fun onScale(detector: ScaleGestureDetector): Boolean {
            val scalePrev = scaleFactor
            val scale = detector.scaleFactor
            scaleFactor *= scale
            scaleFactor = max(minScaleFactor, min(scaleFactor, 3f))

            val focusX = detector.focusX
            val focusY = detector.focusY

            offsetX += (offsetX - focusX) * (scaleFactor / scalePrev - 1)
            offsetY += (offsetY - focusY) * (scaleFactor / scalePrev - 1)

            invalidate()
            return true
        }
    }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) {
        super.onSizeChanged(w, h, oldw, oldh)

        val centerX = nodes.map { it.x }.average().toFloat()
        val centerY = nodes.map { it.y }.average().toFloat()

        offsetX = w / 2f - centerX * scaleFactor
        offsetY = h / 2f - centerY * scaleFactor

        val scaleX = w / 4000f
        val scaleY = h / 4000f
        minScaleFactor = min(scaleX, scaleY)
        scaleFactor = minScaleFactor

        offsetX = w / 2f - centerX * scaleFactor
        offsetY = h / 2f - centerY * scaleFactor
    }

    fun addEdge(fromNodeId: Int, toNodeId: Int, description: String) {
        val fromNode = nodes.find { it.id == fromNodeId }
        val toNode = nodes.find { it.id == toNodeId }
        if (fromNode != null && toNode != null) {
            edges.add(Edge(fromNode, toNode, description))
            invalidate()
        }
    }

    fun addNode(
        x: Float,
        y: Float,
        title: String = "New Node",
        description: String = "",
        connectToNodeId: Int? = null,
        connectionDescription: String? = null
    ) {
        val radius = 120f
        val minSpacing = 3f * radius
        var validX = x
        var validY = y

        // Repeat until position is clear of all nodes
        var attempts = 0
        val maxAttempts = 50

        while (attempts < maxAttempts) {
            var overlapFound = false

            for (node in nodes) {
                val distance = sqrt((node.x - validX).pow(2) + (node.y - validY).pow(2))
                if (distance < minSpacing) {
                    val angle = atan2(validY - node.y, validX - node.x)
                    validX = node.x + cos(angle) * minSpacing
                    validY = node.y + sin(angle) * minSpacing
                    overlapFound = true
                    break
                }
            }

            if (!overlapFound) break
            attempts++
        }

        // Create node
        val newId = (nodes.maxOfOrNull { it.id } ?: 0) + 1
        val trimmedDescription = if (description.length > 200) description.take(200) else description

        val newNode = Node(
            id = newId,
            x = validX,
            y = validY,
            radius = radius,
            title = title,
            description = trimmedDescription
        )

        nodes.add(newNode)

        // Optionally connect to an existing node
        connectToNodeId?.let { targetId ->
            val targetNode = nodes.find { it.id == targetId }
            if (targetNode != null) {
                addEdge(newNode.id, targetNode.id, connectionDescription ?: "Edge from ${newNode.title}")
            }
        }

        Toast.makeText(context, "Node added successfully", Toast.LENGTH_SHORT).show()
        invalidate()
    }

    fun removeEdge(fromNodeId: Int, toNodeId: Int) {
        edges.removeAll { it.from.id == fromNodeId && it.to.id == toNodeId }
        invalidate()
    }

    private fun relaxLayout(iterations: Int = 200) {
        repeat(iterations) {
            applyRepulsion()
            applyAttraction()
        }
    }

    private fun applyRepulsion() {
        val minDistance = 4f * nodes.first().radius
        for (i in nodes.indices) {
            for (j in i + 1 until nodes.size) {
                val n1 = nodes[i]
                val n2 = nodes[j]
                val dx = n2.x - n1.x
                val dy = n2.y - n1.y
                val distance = sqrt(dx * dx + dy * dy)
                if (distance < minDistance && distance > 0) {
                    val overlap = (minDistance - distance) / 2f
                    val normX = dx / distance
                    val normY = dy / distance
                    n1.x -= normX * overlap
                    n1.y -= normY * overlap
                    n2.x += normX * overlap
                    n2.y += normY * overlap
                }
            }
        }
    }

    private fun applyAttraction() {
        val idealEdgeLength = 800f
        for (edge in edges) {
            val from = edge.from
            val to = edge.to
            val dx = to.x - from.x
            val dy = to.y - from.y
            val distance = sqrt(dx * dx + dy * dy)
            if (distance > 0) {
                val diff = (distance - idealEdgeLength) / distance
                val adjustX = dx * diff * 0.05f
                val adjustY = dy * diff * 0.05f
                from.x += adjustX
                from.y += adjustY
                to.x -= adjustX
                to.y -= adjustY
            }
        }
    }
}
