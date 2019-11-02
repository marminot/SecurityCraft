package net.geforcemods.securitycraft.network.packets;

import io.netty.buffer.ByteBuf;
import net.geforcemods.securitycraft.SecurityCraft;
import net.geforcemods.securitycraft.tileentity.TileEntityInventoryScanner;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.geforcemods.securitycraft.util.Utils;
import net.minecraft.util.BlockPos;
import net.minecraftforge.fml.common.network.ByteBufUtils;
import net.minecraftforge.fml.common.network.simpleimpl.IMessage;
import net.minecraftforge.fml.common.network.simpleimpl.IMessageHandler;
import net.minecraftforge.fml.common.network.simpleimpl.MessageContext;

public class PacketSetISType implements IMessage{

	private int x, y, z;
	private String type;

	public PacketSetISType(){

	}

	public PacketSetISType(int x, int y, int z, String type){
		this.x = x;
		this.y = y;
		this.z = z;
		this.type = type;
	}

	@Override
	public void fromBytes(ByteBuf buf) {
		x = buf.readInt();
		y = buf.readInt();
		z = buf.readInt();
		type = ByteBufUtils.readUTF8String(buf);

	}

	@Override
	public void toBytes(ByteBuf buf) {
		buf.writeInt(x);
		buf.writeInt(y);
		buf.writeInt(z);
		ByteBufUtils.writeUTF8String(buf, type);

	}

	public static class Handler extends PacketHelper implements IMessageHandler<PacketSetISType, IMessage> {

		@Override
		public IMessage onMessage(PacketSetISType message, MessageContext context) {
			BlockPos pos = BlockUtils.toPos(message.x, message.y, message.z);

			((TileEntityInventoryScanner) getWorld(context.getServerHandler().playerEntity).getTileEntity(pos)).setType(message.type);

			SecurityCraft.log("Setting type to " + message.type);
			getWorld(context.getServerHandler().playerEntity).scheduleUpdate(pos, BlockUtils.getBlock(getWorld(context.getServerHandler().playerEntity), pos), 1);

			Utils.setISinTEAppropriately(getWorld(context.getServerHandler().playerEntity), pos, ((TileEntityInventoryScanner) getWorld(context.getServerHandler().playerEntity).getTileEntity(pos)).getContents(), ((TileEntityInventoryScanner) getWorld(context.getServerHandler().playerEntity).getTileEntity(pos)).getType());

			return null;
		}

	}

}