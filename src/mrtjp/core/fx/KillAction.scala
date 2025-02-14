/*
 * Copyright (c) 2015.
 * Created by MrTJP.
 * All rights reserved.
 */
package mrtjp.core.fx

import mrtjp.core.fx.particles.CoreParticle

class KillAction extends ParticleAction
{
    override def operate(p:CoreParticle, time:Double): Unit =
    {
        p.setExpired()
        isFinished = true
    }

    override def compile(p:CoreParticle): Unit ={}

    override def copy = ParticleAction.kill()
}