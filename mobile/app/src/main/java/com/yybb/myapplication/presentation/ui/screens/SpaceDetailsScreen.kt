package com.yybb.myapplication.presentation.ui.screens

import android.app.AlertDialog
import android.content.Context
import android.view.View
import android.widget.*
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.Check
import androidx.compose.material.icons.filled.Close
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.FloatingActionButton
import androidx.compose.material3.Icon
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.unit.dp
import androidx.compose.ui.viewinterop.AndroidView
import androidx.hilt.navigation.compose.hiltViewModel
import com.yybb.myapplication.R
import com.yybb.myapplication.presentation.ui.viewmodel.SpaceDetailsViewModel
import com.yybb.myapplication.presentation.ui.utils.GraphView

@Composable
fun SpaceDetailsScreen(
    viewModel: SpaceDetailsViewModel = hiltViewModel(),
    onNavigateToNext: () -> Unit = {}
) {
    val context = LocalContext.current
    val graphView = remember { GraphView(context) }

    var expanded by remember { mutableStateOf(false) }

    Box(modifier = Modifier.fillMaxSize()) {

        // GraphView (custom Android view)
        AndroidView(
            factory = { graphView },
            modifier = Modifier.fillMaxSize()
        )

        // Expandable Floating Action Button
        Box(
            modifier = Modifier.fillMaxSize()
                .padding(end = 16.dp, bottom = 16.dp),
            contentAlignment = Alignment.BottomEnd
        ) {
            // Main FAB
            FloatingActionButton(
                onClick = { expanded = !expanded },
                containerColor = MaterialTheme.colorScheme.primary
            ) {
                Icon(
                    imageVector = if (expanded) Icons.Default.Close else Icons.Default.Add,
                    contentDescription = "Actions"
                )
            }

            // Expandable options
            Column(
                verticalArrangement = Arrangement.spacedBy(12.dp),
                horizontalAlignment = Alignment.Start,
                modifier = Modifier
                    .padding(start = 16.dp, bottom = 70.dp)
            ) {
                if (expanded) {
                    SpeedDialItem("Add Node") {
                        showAddNodeDialog(context, graphView)
                        expanded = false
                    }
                    SpeedDialItem("Add Edge") {
                        showAddEdgeDialog(context, graphView)
                        expanded = false
                    }
                }
            }
        }

        // Zoom Controls (+ and -)
        Column(
            modifier = Modifier
                .align(Alignment.BottomStart)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            FloatingActionButton(
                onClick = {
                    zoomGraphCentered(graphView, zoomIn = true)
                },
                modifier = Modifier.size(50.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_zoom_in),
                        contentDescription = "Zoom In",
                        modifier = Modifier.size(24.dp) // adjust icon size if needed
                    )
                }
            }

            Spacer(modifier = Modifier.height(12.dp))

            FloatingActionButton(
                onClick = {
                    zoomGraphCentered(graphView, zoomIn = false)
                },
                modifier = Modifier.size(50.dp)
            ) {
                Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                    Icon(
                        painter = painterResource(id = R.drawable.ic_zoom_out),
                        contentDescription = "Zoom Out",
                        modifier = Modifier.size(24.dp)
                    )
                }
            }
        }
    }
}

/* -----------------------------
   Mini FAB Component
------------------------------ */
@Composable
fun SpeedDialItem(label: String, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        shape = RoundedCornerShape(24.dp),
        colors = ButtonDefaults.buttonColors(
            containerColor = MaterialTheme.colorScheme.secondary, // <-- your custom color here
            contentColor = MaterialTheme.colorScheme.onSecondary // text/icon color
        ),
        contentPadding = PaddingValues(horizontal = 16.dp, vertical = 8.dp),
        modifier = Modifier.widthIn(min = 140.dp)
    ) {
        Icon(Icons.Default.Add, contentDescription = label)
        Spacer(modifier = Modifier.width(8.dp))
        Text(text = label)
    }
}

/* -----------------------------
   Centered Zoom Helper Function
------------------------------ */

private fun zoomGraphCentered(graphView: GraphView, zoomIn: Boolean) {
    val zoomFactor = if (zoomIn) 1.2f else 1 / 1.2f

    val focusX = graphView.width / 2f
    val focusY = graphView.height / 2f

    val worldX = (focusX - graphView.offsetX) / graphView.scaleFactor
    val worldY = (focusY - graphView.offsetY) / graphView.scaleFactor

    // Apply zoom without limits
    graphView.scaleFactor *= zoomFactor
    // Optional: if you want a soft minimum to avoid disappearing completely, e.g., 0.01f
    graphView.scaleFactor = graphView.scaleFactor.coerceAtLeast(0.01f)

    graphView.offsetX = focusX - worldX * graphView.scaleFactor
    graphView.offsetY = focusY - worldY * graphView.scaleFactor

    graphView.invalidate()
}

/* -----------------------------
   Dialog helper functions
------------------------------ */

private fun showAddEdgeDialog(context: Context, graphView: GraphView) {
    val nodeTitles = graphView.nodes.map { "${it.id}: ${it.title}" }.toTypedArray()
    var selectedFrom = graphView.nodes.firstOrNull()?.id ?: 0
    var selectedTo = graphView.nodes.firstOrNull()?.id ?: 0

    val input = EditText(context).apply { hint = "Edge Description" }

    val layout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
    }

    val fromSpinner = Spinner(context).apply {
        adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, nodeTitles)
        setSelection(0)
        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedFrom = graphView.nodes[position].id
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    val toSpinner = Spinner(context).apply {
        adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, nodeTitles)
        setSelection(0)
        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedTo = graphView.nodes[position].id
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    layout.addView(fromSpinner)
    layout.addView(toSpinner)
    layout.addView(input)

    AlertDialog.Builder(context)
        .setTitle("Add Edge")
        .setView(layout)
        .setPositiveButton("Add") { _, _ ->
            graphView.addEdge(selectedFrom, selectedTo, input.text.toString())
        }
        .setNegativeButton("Cancel", null)
        .show()
}

private fun showAddNodeDialog(context: Context, graphView: GraphView) {
    // Inputs
    val nodeNameInput = EditText(context).apply {
        hint = "Node Name"
    }

    val nodeDescriptionInput = EditText(context).apply {
        hint = "Node Description"
        maxLines = 4
        filters = arrayOf(android.text.InputFilter.LengthFilter(200))
    }

    val connectionDescriptionInput = EditText(context).apply {
        hint = "Connection Description"
        maxLines = 2
        filters = arrayOf(android.text.InputFilter.LengthFilter(100))
    }

    // Edge dropdown (list of existing nodes)
    val nodeTitles = graphView.nodes.map { "${it.id}: ${it.title}" }.toMutableList()
    nodeTitles.add(0, "Select node to connect") // default value

    val edgeSpinner = Spinner(context).apply {
        adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, nodeTitles)
        setSelection(0) // default to "Select node to connect"
    }

    // Layout with spacing
    val layout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        setPadding(32, 16, 32, 16)

        // Helper function to add views with spacing
        fun addWithSpace(view: View, spaceDp: Int = 12) {
            addView(view)
            val space = Space(context)
            val heightPx = (spaceDp * context.resources.displayMetrics.density).toInt()
            space.layoutParams = LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                heightPx
            )
            addView(space)
        }

        addWithSpace(nodeNameInput, 16)
        addWithSpace(nodeDescriptionInput, 16)

        if (nodeTitles.size > 1) { // there are existing nodes
            addWithSpace(TextView(context).apply {
                text = "Connect to existing node:"
            }, 8)
            addWithSpace(edgeSpinner, 16)
            addWithSpace(connectionDescriptionInput, 16)
        }
    }

    // Build dialog
    val dialog = AlertDialog.Builder(context)
        .setTitle("Add Node")
        .setView(layout)
        .setPositiveButton("Add", null)
        .setNegativeButton("Cancel", null)
        .show()


    dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener {
        val nodeName = nodeNameInput.text.toString().trim()
        val nodeDescription = nodeDescriptionInput.text.toString().trim()
        val connectionDescription = connectionDescriptionInput.text.toString().trim()

        val selectedNodeId = if (edgeSpinner.selectedItemPosition > 0) {
            val selectedText = edgeSpinner.selectedItem as String
            selectedText.substringBefore(":").toIntOrNull()
        } else null

        // Validation
        if (nodeName.isEmpty()) {
            nodeNameInput.error = "Node name cannot be empty"
            return@setOnClickListener
        }

        if (selectedNodeId != null && connectionDescription.isEmpty()) {
            connectionDescriptionInput.error = "Please fill connection description to add an edge"
            return@setOnClickListener
        }

        // Add node
        val centerX = graphView.width / 2f / graphView.scaleFactor - graphView.offsetX / graphView.scaleFactor
        val centerY = graphView.height / 2f / graphView.scaleFactor - graphView.offsetY / graphView.scaleFactor

        graphView.addNode(
            centerX,
            centerY,
            nodeName,
            nodeDescription,
            selectedNodeId,
            if (connectionDescription.isEmpty()) null else connectionDescription
        )

        dialog.dismiss()
    }
}

private fun showRemoveEdgeDialog(context: Context, graphView: GraphView) {
    if (graphView.edges.isEmpty()) {
        AlertDialog.Builder(context)
            .setTitle("Remove Edge")
            .setMessage("No edges available to remove.")
            .setPositiveButton("OK", null)
            .show()
        return
    }

    val edgeTitles = graphView.edges.map { edge ->
        "${edge.from.title} -> ${edge.to.title} : ${edge.description}"
    }.toTypedArray()

    var selectedIndex = 0
    val edgeSpinner = Spinner(context).apply {
        adapter = ArrayAdapter(context, android.R.layout.simple_spinner_dropdown_item, edgeTitles)
        setSelection(0)
        onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>, view: View?, position: Int, id: Long) {
                selectedIndex = position
            }

            override fun onNothingSelected(parent: AdapterView<*>) {}
        }
    }

    val layout = LinearLayout(context).apply {
        orientation = LinearLayout.VERTICAL
        addView(edgeSpinner)
    }

    AlertDialog.Builder(context)
        .setTitle("Remove Edge")
        .setView(layout)
        .setPositiveButton("Remove") { _, _ ->
            val edge = graphView.edges[selectedIndex]
            graphView.edges.remove(edge)
            graphView.invalidate()
        }
        .setNegativeButton("Cancel", null)
        .show()
}
