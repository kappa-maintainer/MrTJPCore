/*
 * Copyright (c) 2015.
 * Created by MrTJP.
 * All rights reserved.
 */
package mrtjp.core.gui

import mrtjp.core.vec.{Point, Rect, Size}
import net.minecraft.client.gui.ScaledResolution
import org.lwjgl.opengl.GL11

class ClipNode extends TNode
{
    var size = Size.zeroSize
    override def frame = Rect(position, size)

    override protected[gui] def drawBack(mouse:Point, rframe:Float): Unit =
    {
        if (!hidden)
        {
            val dp = mouse-position
            for (n <- familyByZ)
            {
                if (n == this) drawBack_Impl(mouse, rframe)
                else
                {
                    onChildPredraw()
                    translateTo()
                    n.drawBack(dp, rframe)
                    translateFrom()
                    onChildPostdraw()
                }
            }
        }
    }

    override protected[gui] def drawFront(mouse:Point, rframe:Float): Unit =
    {
        if (!hidden)
        {
            val dp = mouse-position
            for (n <- familyByZ)
            {
                if (n == this) drawFront_Impl(mouse, rframe)
                else
                {
                    onChildPredraw()
                    translateTo()
                    n.drawFront(dp, rframe)
                    translateFrom()
                    onChildPostdraw()
                }
            }
        }
    }

    private def onChildPredraw(): Unit =
    {
        val scaleRes = new ScaledResolution(mcInst)
        val scale = scaleRes.getScaleFactor

        val absPos = parent.convertPointToScreen(position)
        val sFrame = new Rect(absPos.x*scale, mcInst.displayHeight-(absPos.y*scale)-size.height*scale,
            size.width*scale, size.height*scale)

        GL11.glEnable(GL11.GL_SCISSOR_TEST)
        GL11.glScissor(sFrame.x, sFrame.y, sFrame.width, sFrame.height)
    }

    private def onChildPostdraw(): Unit =
    {
        GL11.glDisable(GL11.GL_SCISSOR_TEST)
    }

    override def traceHit(absPoint:Point) = !super.traceHit(absPoint)//only let hits within frame pass through

    override def mouseScrolled_Impl(p:Point, dir:Int, consumed:Boolean) = !frame.contains(p)
    override def mouseClicked_Impl(p:Point, button:Int, consumed:Boolean) = !frame.contains(p)
}

object ClipNode
{
    def tempDisableScissoring(): Unit =
    {
        GL11.glPushAttrib(GL11.GL_SCISSOR_BIT)
        GL11.glDisable(GL11.GL_SCISSOR_TEST)
    }

    def tempEnableScissoring(): Unit =
    {
        GL11.glPopAttrib()
    }
}
