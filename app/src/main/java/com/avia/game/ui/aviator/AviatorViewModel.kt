package com.avia.game.ui.aviator

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.cancel
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch

class AviatorViewModel : ViewModel() {
    var playerXY = 0f to 0f
    var gameState = true
    private var addingStar = true
    var starsCallback: (() -> Unit)? = null
    var enemySpeed = 5
    var enemyRespawnTime = 5500

    var isMagnet = false
    var isBoost = false
    var isTimer = false

    var starsAmount = 0

    private var gameScope = CoroutineScope(Dispatchers.Default)
    private val _enemies = MutableLiveData<List<Pair<Float, Float>>>(emptyList())
    val enemies: LiveData<List<Pair<Float, Float>>> = _enemies
    private val _stars = MutableLiveData<List<Pair<Float, Float>>>(emptyList())
    val stars: LiveData<List<Pair<Float, Float>>> = _stars

    private val _scores = MutableLiveData(0)
    val scores: LiveData<Int> = _scores

    private val _lives = MutableLiveData(3)
    val lives: LiveData<Int> = _lives

    fun setPlayerXY(x: Float, y: Float) {
        playerXY = x to y
    }

    fun stop() {
        gameScope.cancel()
    }

    private fun increaseEnemySpeed() {
        gameScope.launch {
            do {
                delay(5000)
                enemySpeed += 1
                enemyRespawnTime -= 200
            } while (enemySpeed < 20)
        }
    }

    fun start(
        minX: Int,
        maxX: Int,
        enemyWidth: Int,
        maxY: Int,
        enemyHeight: Int,
        playerWidth: Int,
        playerHeight: Int,
        starSize: Int
    ) {
        gameScope = CoroutineScope(Dispatchers.Default)
        generateEnemies(minX, maxX, enemyWidth, enemyHeight)
        letEnemiesMove(maxY, enemyHeight, enemyWidth, playerWidth, playerHeight)

        generateStars(minX, maxX, starSize)
        letStarsMove(maxY, starSize, playerWidth, playerHeight)

        startTimer()
        increaseEnemySpeed()
    }

    private fun startTimer() {
        gameScope.launch {
            while (true) {
                delay(1000)
                _scores.postValue(_scores.value!! + if (isBoost) 2 else 1)
            }
        }
    }

    private fun generateEnemies(minX: Int, maxX: Int, enemyWidth: Int, enemyHeight: Int) {
        gameScope.launch {
            while (true) {
                delay(enemyRespawnTime.toLong())
                if (!isTimer) {
                    val newEnemies = _enemies.value!!.toMutableList()
                    newEnemies.add((minX..maxX - enemyWidth).random().toFloat() to 0f - enemyHeight)
                    _enemies.postValue(newEnemies)
                }
            }
        }
    }

    private fun generateStars(minX: Int, maxX: Int, starSize: Int) {
        gameScope.launch {
            while (true) {
                delay(4000)
                val newStars = _stars.value!!.toMutableList()
                newStars.add((minX..maxX - starSize).random().toFloat() to 0f - starSize)
                _stars.postValue(newStars)
            }
        }
    }

    private fun letEnemiesMove(
        maxY: Int,
        enemyHeight: Int,
        enemyWidth: Int,
        playerWidth: Int,
        playerHeight: Int
    ) {
        gameScope.launch {
            while (true) {
                delay(16)
                val currentList = _enemies.value!!
                val newList = mutableListOf<Pair<Float, Float>>()
                currentList.forEach { enemy ->
                    if (enemy.second <= maxY) {
                        val enemyX = enemy.first.toInt()..enemy.first.toInt() + enemyWidth
                        val enemyY = enemy.second.toInt()..enemy.second.toInt() + enemyHeight
                        val playerX = playerXY.first.toInt()..playerXY.first.toInt() + playerWidth
                        val playerY =
                            playerXY.second.toInt()..playerXY.second.toInt() + playerHeight
                        if (playerX.any { it in enemyX } && playerY.any { it in enemyY }) {
                            if (_lives.value!! - 1 >= 0) {
                                _lives.postValue(_lives.value!! - 1)
                            }
                        } else {
                            newList.add(enemy.first to enemy.second + enemySpeed)
                        }
                    }
                }
                _enemies.postValue(newList)
            }
        }
    }

    private fun letStarsMove(
        maxY: Int,
        starSize: Int,
        playerWidth: Int,
        playerHeight: Int
    ) {
        gameScope.launch {
            while (true) {
                delay(16)
                val currentList = _stars.value!!
                val newList = mutableListOf<Pair<Float, Float>>()
                currentList.forEach { star ->
                    if (star.second <= maxY) {
                        val starX = star.first.toInt()..star.first.toInt() + starSize
                        val starY = star.second.toInt()..star.second.toInt() + starSize
                        val playerX = playerXY.first.toInt()..playerXY.first.toInt() + playerWidth
                        val playerY =
                            playerXY.second.toInt()..playerXY.second.toInt() + playerHeight
                        if (playerX.any { it in starX } && playerY.any { it in starY }) {
                            if (addingStar) {
                                addingStar = false
                                starsCallback?.invoke()
                                starsAmount += 1
                                delay(100)
                                addingStar = true
                            }
                        } else {
                            if (isMagnet) {
                                val magnetPosition =
                                    playerXY.first + starSize to playerXY.second + starSize
                                var xDirection = if (magnetPosition.first > star.first) 5 else -5
                                if (xDirection in magnetPosition.first.toInt() - 4..(magnetPosition.first.toInt() + 4)) {
                                    xDirection = 0
                                }
                                var yDirection = if (magnetPosition.second > star.second) 5 else -5
                                if (yDirection in magnetPosition.second.toInt() - 4..(magnetPosition.second.toInt() + 4)) {
                                    yDirection = 0
                                }
                                newList.add(star.first + xDirection.toFloat() to star.second + yDirection)
                            } else {
                                newList.add(star.first to star.second + 5)
                            }
                        }
                    }
                }
                _stars.postValue(newList)
            }
        }
    }

    fun timer() {
        viewModelScope.launch {
            isTimer = true
            delay(30000)
            isTimer = false
        }
    }

    fun magnet() {
        viewModelScope.launch {
            isMagnet = true
            delay(15000)
            isMagnet = false
        }
    }

    fun boost() {
        viewModelScope.launch {
            isBoost = true
            delay(30000)
            isBoost = false
        }
    }

    override fun onCleared() {
        super.onCleared()
        gameScope.cancel()
    }
}