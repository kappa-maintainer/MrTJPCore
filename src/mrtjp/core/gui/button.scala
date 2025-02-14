/*
 * Copyright (c) 2014.
 * Created by MrTJP.
 * All rights reserved.
 */
package mrtjp.core.gui

import codechicken.lib.gui.GuiDraw
import codechicken.lib.texture.TextureUtils
import mrtjp.core.vec.{Point, Rect, Size}
import net.minecraft.client.audio.PositionedSoundRecord
import net.minecraft.client.renderer.GlStateManager
import net.minecraft.init.SoundEvents

import scala.jdk.CollectionConverters.*
import scala.collection.mutable.ListBuffer

/**
 * Base button class with position and width/height. Doesnt render anything, nor does it perform
 * action when clicked.
 */
class ButtonNode extends TNode
{
    var size = Size.zeroSize
    override def frame = Rect(position, size)

    var clickDelegate = {() => ()}
    var tooltipBuilder = {(_:ListBuffer[String]) => ()}
    var drawFunction = {() => ()}

    var mouseoverLock =  false

    override def mouseClicked_Impl(p:Point, button:Int, consumed:Boolean) =
    {
        if (!consumed && rayTest(p))
        {
            soundHandler.playSound(PositionedSoundRecord.getMasterRecord(SoundEvents.UI_BUTTON_CLICK, 1))
            onButtonClicked()
            true
        }
        else false
    }

    def onButtonClicked(): Unit =
    {
        clickDelegate()
    }

    override def drawBack_Impl(mouse:Point, rframe:Float): Unit =
    {
        GlStateManager.color(1, 1, 1, 1)
        val mouseover = mouseoverLock || (frame.contains(mouse) && rayTest(mouse))
        drawButtonBackground(mouseover)
        drawButton(mouseover)
    }

    override def drawFront_Impl(mouse:Point, rframe:Float): Unit =
    {
        if (rayTest(mouse))
        {
            val list = new ListBuffer[String]
            tooltipBuilder(list)

            //draw tooltip with absolute coords to allow it to force-fit on screen
            translateToScreen()
            val Point(mx, my) = parent.convertPointToScreen(mouse)
            GuiDraw.drawMultiLineTip(mx+12, my-12, list.asJava)
            translateFromScreen()
        }
    }

    def drawButtonBackground(mouseover:Boolean): Unit ={}
    def drawButton(mouseover:Boolean): Unit ={}
}

/**
 * Trait for buttons that renders their background as a default MC button.
 */
trait TButtonMC extends ButtonNode
{
    abstract override def drawButtonBackground(mouseover:Boolean): Unit =
    {
        super.drawButtonBackground(mouseover)

        TextureUtils.changeTexture(GuiLib.guiTex)

        GlStateManager.color(1, 1, 1, 1)
        val state = if (mouseover) 2 else 1

        drawTexturedModalRect(position.x, position.y, 0, 46+state*20, size.width/2, size.height/2)
        drawTexturedModalRect(position.x+size.width/2, position.y, 200-size.width/2, 46+state*20, size.width/2, size.height/2)
        drawTexturedModalRect(position.x, position.y+size.height/2, 0, 46+state*20+20-size.height/2, size.width/2, size.height/2)
        drawTexturedModalRect(position.x+size.width/2, position.y+size.height/2, 200-size.width/2, 46+state*20+20-size.height/2, size.width/2, size.height/2)
    }
}

/**
 * Trait for buttons that renders their foreground as text.
 */
trait TButtonText extends ButtonNode
{
    var text = ""
    def setText(t:String):this.type = {text = t; this}

    abstract override def drawButton(mouseover:Boolean): Unit =
    {
        super.drawButton(mouseover)
        GuiDraw.drawStringC(text, position.x+size.width/2, position.y+(size.height-8)/2, if (mouseover) 0xFFFFFFA0 else 0xFFE0E0E0)
        GlStateManager.color(1, 1, 1, 1)
    }
}

/**
 * Button that is used for selection.
 */
class DotSelectNode extends ButtonNode
{
    size = Size(8, 8)

    override def drawButtonBackground(mouseover:Boolean): Unit =
    {
        super.drawButtonBackground(mouseover)
        TextureUtils.changeTexture(GuiLib.guiExtras)
        GlStateManager.color(1, 1, 1, 1)
        drawTexturedModalRect(position.x, position.y, if (mouseover) 11 else 1, 1, 8, 8)
    }
}

object DotSelectNode
{
    def centered(x:Int, y:Int) =
    {
        val b = new DotSelectNode
        b.position = Point(x, y)-4
        b
    }
}

/**
 * Check box button that has either an on or off state.
 */
class CheckBoxNode extends ButtonNode with TButtonMC
{
    size = Size(14, 14)

    var state = false

    override def drawButton(mouseover:Boolean): Unit =
    {
        super.drawButton(mouseover)
        TextureUtils.changeTexture(GuiLib.guiExtras)
        val u = if (state) 17 else 1
        drawTexturedModalRect(position.x, position.y, u, 134, 14, 14)
    }

    override def onButtonClicked(): Unit =
    {
        state = !state
        super.onButtonClicked()
    }
}

object CheckBoxNode
{
    def centered(x:Int, y:Int) =
    {
        val b = new CheckBoxNode
        b.position = Point(x, y)-7
        b
    }
}

/**
  * Default implementation of a button in mc with normal render and text overlay
  */
class MCButtonNode extends ButtonNode with TButtonMC with TButtonText

/**
  * Implementation of a button with manual icon rendering via override.
  */
class IconButtonNode extends ButtonNode with TButtonMC
