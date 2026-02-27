/*
 * Copyright (c) 2015.
 * Created by MrTJP.
 * All rights reserved.
 */
package mrtjp.core.fx

import codechicken.lib.vec.Vector3
import mrtjp.core.fx.particles.CoreParticle

trait TScalableParticle extends CoreParticle
:
    var scale: Vector3 = Vector3.one.copy

    def scaleX: Double = scale.x
    def scaleY: Double = scale.y
    def scaleZ: Double = scale.z

    def scaleX_=(x:Double): Unit ={scale.x = x}
    def scaleY_=(y:Double): Unit ={scale.y = y}
    def scaleZ_=(z:Double): Unit ={scale.z = z}

class ScaleToAction extends ParticleAction
:
    var target: Vector3 = Vector3.zero
    var duration = 0.0

    override def canOperate(p: CoreParticle): Boolean = p.isInstanceOf[TScalableParticle]

    override def operate(p:CoreParticle, time:Double): Unit =
        val s = p.asInstanceOf[TScalableParticle]

        if time < duration then
            val dscale = target.copy.subtract(s.scale)
            val speed = dscale.copy.multiply(1/(duration-time)).multiply(deltaTime(time))
            s.scale.add(speed)

            //Check for resoulution errors - if any of the values have surpassed taret, then we are close enough
            val dscale2 = target.copy.subtract(s.scale)
            if dscale2.x.signum == 0 || dscale2.x.signum != dscale.x.signum ||
                    dscale2.y.signum == 0 || dscale2.y.signum != dscale.y.signum ||
                    dscale2.z.signum == 0 || dscale2.z.signum != dscale.z.signum then
                isFinished = true
        else isFinished = true

        if isFinished then
            s.scale.set(target)

    override def compile(p:CoreParticle): Unit ={}

    override def copy: ParticleAction = ParticleAction.scaleTo(target.x, target.y, target.z, duration)

class ScaleForAction extends ParticleAction
{
    var delta = Vector3.zero
    var duration = 0.0

    override def canOperate(p:CoreParticle) = p.isInstanceOf[TScalableParticle]

    override def operate(p:CoreParticle, time:Double): Unit =
        val s = p.asInstanceOf[TScalableParticle]
        if time < duration then s.scale.add(delta.copy.multiply(deltaTime(time)))
        else isFinished = true

    override def compile(p:CoreParticle): Unit ={}

    override def copy = ParticleAction.scaleFor(delta.x, delta.y, delta.z, duration)
}