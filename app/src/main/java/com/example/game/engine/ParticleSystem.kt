package com.example.game.engine

import androidx.compose.ui.graphics.Color
import kotlin.random.Random

data class Particle(
    var x: Float,
    var y: Float,
    var vx: Float,
    var vy: Float,
    var life: Float, // 1.0 down to 0.0
    val maxLife: Float,
    val color: Color,
    val size: Float,
    val isSparkle: Boolean = false,
    val isSmoke: Boolean = false
)

data class FloatingText(
    var x: Float,
    var y: Float,
    val text: String,
    val color: Color,
    var life: Float = 1.0f,
    val maxLife: Float = 1.0f
)

class ParticleSystem {
    val particles = mutableListOf<Particle>()
    val floatingTexts = mutableListOf<FloatingText>()

    fun emitDirtChunks(worldX: Float, worldY: Float, baseColor: Color, count: Int = 8) {
        val rand = Random.Default
        for (i in 0 until count) {
            val angle = rand.nextFloat() * 2f * Math.PI.toFloat()
            val speed = rand.nextFloat() * 140f + 50f
            particles.add(
                Particle(
                    x = worldX + rand.nextFloat() * 20f - 10f,
                    y = worldY + rand.nextFloat() * 20f - 10f,
                    vx = kotlin.math.cos(angle) * speed,
                    vy = kotlin.math.sin(angle) * speed - 60f,
                    life = 1.0f,
                    maxLife = rand.nextFloat() * 0.4f + 0.3f,
                    color = baseColor.copy(alpha = 0.9f),
                    size = rand.nextFloat() * 6f + 3f
                )
            )
        }
    }

    fun emitGemSparkles(worldX: Float, worldY: Float, gemColor: Color, count: Int = 14) {
        val rand = Random.Default
        for (i in 0 until count) {
            val angle = rand.nextFloat() * 2f * Math.PI.toFloat()
            val speed = rand.nextFloat() * 110f + 30f
            particles.add(
                Particle(
                    x = worldX + rand.nextFloat() * 16f - 8f,
                    y = worldY + rand.nextFloat() * 16f - 8f,
                    vx = kotlin.math.cos(angle) * speed,
                    vy = kotlin.math.sin(angle) * speed - 50f,
                    life = 1.0f,
                    maxLife = rand.nextFloat() * 0.6f + 0.4f,
                    color = gemColor,
                    size = rand.nextFloat() * 6f + 3f,
                    isSparkle = true
                )
            )
        }
    }

    fun emitCoinBurst(worldX: Float, worldY: Float, count: Int = 12) {
        val rand = Random.Default
        for (i in 0 until count) {
            val angle = rand.nextFloat() * 2f * Math.PI.toFloat()
            val speed = rand.nextFloat() * 150f + 70f
            particles.add(
                Particle(
                    x = worldX,
                    y = worldY,
                    vx = kotlin.math.cos(angle) * speed,
                    vy = kotlin.math.sin(angle) * speed - 120f,
                    life = 1.0f,
                    maxLife = rand.nextFloat() * 0.7f + 0.4f,
                    color = Color(0xFFFFD54F),
                    size = rand.nextFloat() * 7f + 4f,
                    isSparkle = true
                )
            )
        }
    }

    fun emitChimneySmoke(worldX: Float, worldY: Float) {
        val rand = Random.Default
        particles.add(
            Particle(
                x = worldX + rand.nextFloat() * 6f - 3f,
                y = worldY,
                vx = rand.nextFloat() * 14f + 4f, // drift slightly right with breeze
                vy = -rand.nextFloat() * 25f - 15f, // float up
                life = 1.0f,
                maxLife = 1.8f,
                color = Color(0xFFCFD8DC).copy(alpha = 0.5f),
                size = rand.nextFloat() * 5f + 6f,
                isSmoke = true
            )
        )
    }

    fun addFloatingText(x: Float, y: Float, text: String, color: Color) {
        floatingTexts.add(FloatingText(x, y, text, color))
    }

    fun update(dt: Float) {
        val pIter = particles.iterator()
        while (pIter.hasNext()) {
            val p = pIter.next()
            p.life -= dt / p.maxLife
            if (p.life <= 0f) {
                pIter.remove()
            } else {
                p.x += p.vx * dt
                p.y += p.vy * dt
                if (p.isSmoke) {
                    p.vy -= 8f * dt // buoyant rise
                    p.vx += 2f * dt
                } else {
                    p.vy += 320f * dt // Gravity for solid chunks
                }
            }
        }

        val tIter = floatingTexts.iterator()
        while (tIter.hasNext()) {
            val ft = tIter.next()
            ft.life -= dt / ft.maxLife
            if (ft.life <= 0f) {
                tIter.remove()
            } else {
                ft.y -= 45f * dt // float upward
            }
        }
    }
}
