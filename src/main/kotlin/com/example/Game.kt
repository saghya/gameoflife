package com.example

import javafx.application.Application
import javafx.scene.Group
import javafx.scene.Scene
import javafx.scene.canvas.Canvas
import javafx.scene.canvas.GraphicsContext
import javafx.scene.input.KeyCode
import javafx.scene.paint.Color
import javafx.scene.text.Font
import javafx.scene.text.FontWeight
import javafx.stage.Stage

class GameOfLife(val graphicsContext: GraphicsContext) {
    val cellSize: Int = 15
    private var height: Int = graphicsContext.canvas.height.toInt() / cellSize
    private var width: Int = graphicsContext.canvas.width.toInt() / cellSize
    var grid: Array<BooleanArray> = Array(height) { BooleanArray(width) }
    private var nextGrid: Array<BooleanArray> = Array(height) { BooleanArray(width) }

    var elapsedTime: Int = 0
    @Volatile
    var paused = false

    init {
        initializeGame(true)
    }

    fun initializeGame(random: Boolean) {
        elapsedTime = 0
        // Initialize the grid randomly with live and dead cells or only with dead cells
        for (row in 0 until height) {
            for (col in 0 until width) {
                grid[row][col] = if (random) { Math.random() < 0.2 } else { false }
            }
        }
    }

    fun playGame() {
        while(true) {
            if (!paused) {
                updateGrid()
                elapsedTime++
            }
            drawGrid()
            drawStrings()
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

    private fun drawGrid() {
        graphicsContext.clearRect(0.0, 0.0, graphicsContext.canvas.width, graphicsContext.canvas.height)
        graphicsContext.fill = Color.BLACK

        for (row in 0 until height) {
            for (col in 0 until width) {
                if (grid[row][col]) {
                    graphicsContext.fillRect(col.toDouble()*cellSize, row.toDouble()*cellSize, cellSize.toDouble(), cellSize.toDouble())
                }
            }
        }
    }

    private fun drawStrings() {
        graphicsContext.fill = Color.GREY
        graphicsContext.fillText("Elapsed time: $elapsedTime", 10.0, 20.0)
        graphicsContext.fillText("Population:   " + grid.flatMap { it.toList() }.count{it}, 10.0, 35.0)

        if (paused) {
            val pause = "Paused"
            graphicsContext.fillText(
                pause,
                graphicsContext.canvas.width / 2.0 - graphicsContext.font.size * pause.length / 2.0,
                graphicsContext.canvas.height / 2.0
            )
        }
    }
}

class Game : Application() {
    private val width = 900
    private val height = 512
    private lateinit var gameOfLife: GameOfLife

    override fun start(mainStage: Stage) {
        val canvas = Canvas(width.toDouble(), height.toDouble())
        val graphicsContext = canvas.graphicsContext2D
        val root = Group()
        val scene = Scene(root)

        graphicsContext.lineWidth = 3.0
        graphicsContext.font = Font.font("Consolas", FontWeight.BOLD, 20.0)

        handleKeyboard(scene)
        handleMouse(scene)

        root.children.add(canvas)
        mainStage.title = "Game of Life"
        mainStage.scene = scene
        mainStage.show()

        gameOfLife = GameOfLife(graphicsContext)
        Thread {
            gameOfLife.playGame()
        }.start()
    }

    private fun handleKeyboard(scene: Scene) {
        var spacePressed = false
        scene.setOnKeyPressed { event ->
            if (!spacePressed && event.code == KeyCode.SPACE) {
                spacePressed = true
                gameOfLife.paused = !gameOfLife.paused
            }

            if (gameOfLife.paused && event.code == KeyCode.C) { // clear grid
                gameOfLife.initializeGame(false)
            }

            if (gameOfLife.paused && event.code == KeyCode.R) { // new random grid
                gameOfLife.initializeGame(true)
            }
        }
        scene.setOnKeyReleased { event ->
            if (event.code == KeyCode.SPACE) {
                spacePressed = false
            }
        }
    }

    private fun handleMouse(scene: Scene)
    {
        scene.setOnMouseClicked { event ->
            if (!gameOfLife.paused)
                return@setOnMouseClicked

            val x = event.x / gameOfLife.cellSize
            val y = event.y / gameOfLife.cellSize
            if (x < width / gameOfLife.cellSize && y < height / gameOfLife.cellSize) {
                gameOfLife.grid[y.toInt()][x.toInt()] = !gameOfLife.grid[y.toInt()][x.toInt()]
            }
        }
    }
}
