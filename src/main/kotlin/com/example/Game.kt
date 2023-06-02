package com.example

import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.paint.Color
import javafx.stage.Stage

class GameOfLife(private val canvasWidth: Int, private val canvasHeight: Int, private  val pixelSize: Int) {
    private var height: Int = canvasHeight / pixelSize
    private var width: Int = canvasWidth / pixelSize
    private var grid: Array<BooleanArray> = Array(height) { BooleanArray(width) }
    private var nextGrid: Array<BooleanArray> = Array(height) { BooleanArray(width) }

    init {
        initializeGrid()
    }

    private fun initializeGrid() {
        // Initialize the grid randomly with live and dead cells
        for (row in 0 until height) {
            for (col in 0 until width) {
                grid[row][col] = Math.random() < 0.1
            }
        }
    }

    fun playGame(graphicsContext: GraphicsContext) {
        while (true) {
            updateGrid()
            drawGrid(graphicsContext)
            Thread.sleep(200) // Delay between iterations
        }
    }

    private fun updateGrid() {
        for (row in 0 until height) {
            for (col in 0 until width) {
                val aliveNeighbors = countAliveNeighbors(row, col)
                nextGrid[row][col] = if (grid[row][col]) {
                    // Cell is alive
                    aliveNeighbors in 2..3
                } else {
                    // Cell is dead
                    aliveNeighbors == 3
                }
            }
        }

        // Swap the current grid with the next grid
        val temp = grid
        grid = nextGrid
        nextGrid = temp
    }

    private fun countAliveNeighbors(row: Int, col: Int): Int {
        var count = 0

        for (i in -1..1) {
            for (j in -1..1) {
                val neighborRow = (row + i + height) % height
                val neighborCol = (col + j + width) % width

                if (grid[neighborRow][neighborCol] && !(i == 0 && j == 0)) {
                    count++
                }
            }
        }

        return count
    }

    private fun drawGrid(graphicsContext: GraphicsContext) {
        graphicsContext.clearRect(0.0, 0.0, graphicsContext.canvas.width, graphicsContext.canvas.height)

        for (row in 0 until height) {
            for (col in 0 until width) {
                if (grid[row][col]) {
                    graphicsContext.fill = Color.BLACK
                    graphicsContext.fillRect(col.toDouble()*pixelSize, row.toDouble()*pixelSize, pixelSize.toDouble(), pixelSize.toDouble())
                }
            }
        }
    }
}

class Game : Application() {
    private val width = 512
    private val height = 512
    private val size = 5
    private val gameOfLife: GameOfLife = GameOfLife(width, height, size)

    override fun start(mainStage: Stage) {
        val canvas = Canvas(width.toDouble(), height.toDouble())
        val graphicsContext = canvas.graphicsContext2D

        //val root = BorderPane(canvas)
        val root = Group()
        val scene = Scene(root)

        root.children.add(canvas)
        mainStage.title = "Game of Life"
        mainStage.scene = scene
        mainStage.show()

        Thread {
            gameOfLife.playGame(graphicsContext)
        }.start()
    }
}

