/*
 * Copyright (c) 2015.
 * Created by MrTJP.
 * All rights reserved.
 */
package mrtjp.core.fx

import mrtjp.core.fx.particles.CoreParticle

import scala.collection.{immutable, mutable}
import scala.collection.mutable.Seq as MSeq

class GroupAction extends ParticleAction
{
    var actions = MSeq[ParticleAction]()

    override def tickLife(): Unit =
    {
        super.tickLife()
        actions.foreach(_.tickLife())
    }

    override def runOn(p:CoreParticle, frame:Float): Unit =
    {
        super.runOn(p, frame)

        actions.foreach { a =>
            if (!a.isFinished)
                a.runOn(p, frame)
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

    override def copy = ParticleAction.group(immutable.Seq.from(actions.map(_.copy)):_*)
}