/*
 * Copyright (c) 2015.
 * Created by MrTJP.
 * All rights reserved.
 */
package mrtjp.core.fx

import mrtjp.core.fx.particles.CoreParticle

import scala.collection.immutable
import scala.collection.mutable.Seq as MSeq

class SequenceAction extends ParticleAction
{
    var actions = MSeq[ParticleAction]()

    override def tickLife(): Unit =
    {
        super.tickLife()
        actions.find(!_.isFinished) match
        {
            case Some(action) => action.tickLife()
            case None =>
        }
    }

    override def runOn(p:CoreParticle, frame:Float): Unit =
    {
        super.runOn(p, frame)

        actions.find(!_.isFinished) match
        {
            case Some(action) => action.runOn(p, frame)
            case None =>
                isFinished = true
                return
        }

        if (actions.forall(_.isFinished))
            isFinished = true
    }

    override def operate(p:CoreParticle, time:Double): Unit ={}

    override def compile(p:CoreParticle): Unit =
    {
        super.compile(p)
        actions.foreach(_.compile(p))
    }

    override def reset(): Unit =
    {
        super.reset()
        actions.foreach(_.reset())
    }

    override def copy = ParticleAction.sequence(immutable.Seq.from(actions.map(_.copy)):_*)
}