/*
 * Copyright (c) 2014.
 * Created by MrTJP.
 * All rights reserved.
 */
package mrtjp.core.gui

import java.util.{List => JList}

import codechicken.lib.colour.EnumColour
import codechicken.lib.gui.GuiDraw
import codechicken.lib.util.FontUtils
import mrtjp.core.item.ItemKeyStack
import mrtjp.core.vec.{Point, Rect, Size}
import net.minecraft.client.Minecraft
import net.minecraft.client.gui.Gui
import net.minecraft.client.renderer.GlStateManager._
import net.minecraft.client.util.ITooltipFlag.TooltipFlags._
import net.minecraft.client.renderer.{OpenGlHelper, RenderHelper}
import net.minecraft.item.ItemStack

class NodeItemList(x:Int, y:Int, w:Int, h:Int) extends TNode
{
    position = Point(x, y)
    var size = Size(w, h)
    override def frame = Rect(position, size)

    private val squareSize = 20
    private val rows = size.height/squareSize
    private val columns = size.width/squareSize

    private var currentPage = 0
    private var pagesNeeded = 0

    private var waitingForList = true
    private var downloadFinished = false

    private var selection:ItemKeyStack = null
    private var hover:ItemKeyStack = null

    private var xLast = 0
    private var yLast = 0

    private var filter = ""

    def getSelected = selection

    private var displayList = Vector[ItemKeyStack]()

    def setDisplayList(list:Vector[ItemKeyStack]):this.type =
    {
        displayList = list
        waitingForList = false
        currentPage = 0
        this
    }

    def setNewFilter(filt:String):this.type =
    {
        filter = filt.toLowerCase
        xLast = -1
        yLast = -1
        currentPage = 0
        this
    }

    def pageUp(): Unit =
    {
        currentPage += 1
        if (currentPage > pagesNeeded) currentPage = pagesNeeded
    }

    def pageDown(): Unit =
    {
        currentPage -= 1
        if (currentPage < 0) currentPage = 0
    }

    def resetDownloadStats(): Unit =
    {
        waitingForList = true
        downloadFinished = false
    }

    def filterAllows(stack:ItemKeyStack):Boolean =
    {
        def stringMatch(name:String, filter:String):Boolean =
        {
            for (s <- filter.split(" ")) if (!name.contains(s)) return false
            true
        }

        if (stringMatch(stack.key.getName.toLowerCase, filter)) true
        else false
    }

    private def getSeachedCount =
    {
        var count = 0
        for (stack <- displayList) if (filterAllows(stack)) count += 1
        count
    }

    override def drawBack_Impl(mouse:Point, frame:Float): Unit =
    {
        drawGradientRect(x, y, x+size.width, y+size.height, 0xff808080, 0xff808080)
        pagesNeeded = (getSeachedCount-1)/(rows*columns)
        if (pagesNeeded < 0) pagesNeeded = 0
        if (currentPage > pagesNeeded) currentPage = pagesNeeded

        if (!downloadFinished) drawLoadingScreen()
        else drawAllItems(mouse.x, mouse.y)
    }

    override def drawFront_Impl(mouse:Point, rframe:Float): Unit =
    {
        if (hover != null) GuiDraw.drawMultiLineTip(
            mouse.x+12, mouse.y-12,
            hover.makeStack.getTooltip(mcInst.player,
                if(mcInst.gameSettings.advancedItemTooltips) ADVANCED else NORMAL))
        FontUtils.drawCenteredString(
            "Page: "+(currentPage+1)+"/"+(pagesNeeded+1), x+(size.width/2), y+frame.height+6, EnumColour.BLACK.rgb)
    }

    override def mouseClicked_Impl(p:Point, button:Int, consumed:Boolean) =
    {
        if (!consumed && frame.contains(p))
        {
            xLast = p.x
            yLast = p.y
            true
        }
        else false
    }

    private def drawLoadingScreen(): Unit =
    {
        val barSizeX = size.width/2
        val time = System.currentTimeMillis/(if (waitingForList) 40 else 8)
        val percent = (time%barSizeX).asInstanceOf[Int]

        if (!waitingForList && percent > barSizeX-8) downloadFinished = true
        val xStart = x+size.width/2-barSizeX/2
        val yStart = y+frame.height/3

        FontUtils.drawCenteredString("downloading data", (x+size.width)/2, (y+frame.height)/3+squareSize, 0xff165571)
        val xSize = percent
        val ySize = 9

        Gui.drawRect(xStart, yStart, xStart+xSize, yStart+ySize, 0xff165571)
    }

    private def drawAllItems(mx:Int, my:Int): Unit =
    {
        hover = null
        selection = null
        val xOffset = x-(squareSize-2)
        val yOffset = y+2
        var renderPointerX = 1
        var renderPointerY = 0
        var itemNumber = 0

        glItemPre()

        val b, c = new scala.util.control.Breaks
        b.breakable
          {
            for (keystack <- displayList) c.breakable
              {
                if (!filterAllows(keystack)) c.break()
                itemNumber += 1
                if (itemNumber <= rows*columns*currentPage) c.break()
                if (itemNumber > (rows*columns)*(currentPage+1)) b.break()

                val localX = xOffset+renderPointerX*squareSize
                val localY = yOffset+renderPointerY*squareSize
                if (mx > localX && mx < localX+squareSize && my > localY && my < localY+squareSize) hover = keystack
                if (xLast > localX && xLast < localX+squareSize && yLast > localY && yLast < localY+squareSize) selection = keystack

                if (selection != null && (selection == keystack))
                {
                    Gui.drawRect(localX-2, localY-2, localX+squareSize-2, localY+squareSize-2, 0xff000000)
                    Gui.drawRect(localX-1, localY-1, localX+squareSize-3, localY+squareSize-3, 0xffd2d2d2)
                    Gui.drawRect(localX, localY, localX+squareSize-4, localY+squareSize-4, 0xff595959)
                }

                inscribeItemStack(localX, localY, keystack.makeStack)
                renderPointerX += 1

                if (renderPointerX > columns)
                {
                    renderPointerX = 1
                    renderPointerY += 1
                }
                if (renderPointerY > rows) b.break()
            }
        }
        glItemPost()
    }

    private def glItemPre(): Unit =
    {
        pushMatrix()
        color(1.0F, 1.0F, 1.0F, 1.0F)
        RenderHelper.enableGUIStandardItemLighting()
        enableRescaleNormal()
        OpenGlHelper.setLightmapTextureCoords(OpenGlHelper.lightmapTexUnit, 240/1.0F, 240/1.0F)
        disableDepth()
        disableLighting()
    }

    private def glItemPost(): Unit =
    {
        enableDepth()
        popMatrix()
    }

    protected var renderItem = Minecraft.getMinecraft.getRenderItem
    private def inscribeItemStack(xPos:Int, yPos:Int, stack:ItemStack): Unit =
    {
        val font = stack.getItem.getFontRenderer(stack) match {
            case null => getFontRenderer
            case r => r
        }

        renderItem.zLevel = 100.0F
        enableDepth()
        enableLighting()
        renderItem.renderItemAndEffectIntoGUI(stack, xPos, yPos)
        renderItem.renderItemOverlayIntoGUI(font, stack, xPos, yPos, "")
        disableLighting()
        disableDepth()
        renderItem.zLevel = 0.0F

        var s:String = null
        if (stack.getCount == 1) s = ""
        else if (stack.getCount < 1000) s = stack.getCount+""
        else if (stack.getCount < 100000) s = stack.getCount/1000+"K"
        else if (stack.getCount < 1000000) s = "0M"+stack.getCount/100000
        else s = stack.getCount/1000000+"M"
        font.drawStringWithShadow(s, xPos+19-2-font.getStringWidth(s), yPos+6+3, 16777215)
    }
}
