/*
 * Copyright (c) 2014.
 * Created by MrTJP.
 * All rights reserved.
 */
package mrtjp.core.handler

import codechicken.lib.packet.PacketCustom
import mrtjp.core.world.Messenger
import net.minecraftforge.common.MinecraftForge
import net.minecraftforge.fml.relauncher.{Side, SideOnly}

class MrTJPCoreProxy_server
{
    def preInit(): Unit ={}

    def init(): Unit =
    {
        PacketCustom.assignHandler(MrTJPCoreSPH.channel, MrTJPCoreSPH)
        //SimpleGenHandler.init()
    }

    def postInit(): Unit ={}
}

class MrTJPCoreProxy_client extends MrTJPCoreProxy_server
{
    @SideOnly(Side.CLIENT)
    override def preInit(): Unit =
    {
        super.preInit()
    }

    @SideOnly(Side.CLIENT)
    override def init(): Unit =
    {
        super.init()
        PacketCustom.assignHandler(MrTJPCoreCPH.channel, MrTJPCoreCPH)
    }

    @SideOnly(Side.CLIENT)
    override def postInit(): Unit =
    {
        MinecraftForge.EVENT_BUS.register(Messenger)
//        MinecraftForge.EVENT_BUS.register(RenderTicker)
    }
}

object MrTJPCoreProxy extends MrTJPCoreProxy_client