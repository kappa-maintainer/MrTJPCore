/*
 * Copyright (c) 2015.
 * Created by MrTJP.
 * All rights reserved.
 */
package mrtjp.core.fx

import codechicken.lib.vec.Vector3
import mrtjp.core.fx.particles.CoreParticle

trait TTargetParticle extends CoreParticle
{
    var target = Vector3.zero
    var prevTarget = Vector3.zero

    def tx = target.x
    def ty = target.y
    def tz = target.z

    def tx_=(x:Double): Unit ={target.x = x}
    def ty_=(y:Double): Unit ={target.y = y}
    def tz_=(z:Double): Unit ={target.z = z}

    def ptx = prevTarget.x
    def pty = prevTarget.y
    def ptz = prevTarget.z

    def ptx_=(x:Double): Unit ={prevTarget.x = x}
    def pty_=(y:Double): Unit ={prevTarget.y = y}
    def ptz_=(z:Double): Unit ={prevTarget.z = z}

    def dtx = tx-ptx
    def dty = ty-pty
    def dtz = tz-ptz

    def setTarget(x:Double, y:Double, z:Double): Unit =
    {
        target.set(x, y, z)
    }

    abstract override def onUpdate(): Unit =
    {
        super.onUpdate()
        prevTarget.set(target)
    }
}

class TargetChangeToAction extends ParticleAction
{
    var target = Vector3.zero
    var duration = 0.0

    override def canOperate(p:CoreParticle) = p.isInstanceOf[TTargetParticle]

    override def operate(p:CoreParticle, time:Double): Unit =
    {
        val tp = p.asInstanceOf[TTargetParticle]

        if (time < duration)
        {
            val dpos = target.copy.subtract(tp.target)
            val speed = dpos.copy.multiply(1/(duration-time)).multiply(deltaTime(time))
            tp.target.add(speed)
        }
        else isFinished = true
    }

    override def compile(p:CoreParticle): Unit ={}

    override def copy = ParticleAction.changeTargetTo(target.x, target.y, target.z, duration)
}

class TargetChangeForAction extends ParticleAction
{
    var delta = Vector3.zero
    var duration = 0.0

    override def canOperate(p:CoreParticle) = p.isInstanceOf[TTargetParticle]

    override def operate(p:CoreParticle, time:Double): Unit =
    {
        val tp = p.asInstanceOf[TTargetParticle]
        if (time < duration) tp.target.add(delta.copy.multiply(deltaTime(time)))
        else isFinished = true
    }

    override def compile(p:CoreParticle): Unit ={}

    override def copy = ParticleAction.changeTargetFor(delta.x, delta.y, delta.z, duration)
}