package net.geforcemods.securitycraft.tileentity;

import net.geforcemods.securitycraft.api.Option;
import net.geforcemods.securitycraft.blocks.BlockInventoryScanner;
import net.geforcemods.securitycraft.blocks.BlockInventoryScannerField;
import net.geforcemods.securitycraft.misc.EnumCustomModules;
import net.geforcemods.securitycraft.util.BlockUtils;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.inventory.IInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NBTTagCompound;
import net.minecraft.nbt.NBTTagList;
import net.minecraft.util.NonNullList;

public class TileEntityInventoryScanner extends TileEntityDisguisable implements IInventory{

	private NonNullList<ItemStack> inventoryContents = NonNullList.<ItemStack>withSize(37, ItemStack.EMPTY);
	private String type = "check";
	private boolean isProvidingPower;
	private int cooldown;

	@Override
	public void update(){
		if(cooldown > 0)
			cooldown--;
		else if(isProvidingPower){
			isProvidingPower = false;
			BlockUtils.updateAndNotify(getWorld(), pos, getWorld().getBlockState(pos).getBlock(), 1, true);
		}
	}

	@Override
	public void readFromNBT(NBTTagCompound tag){
		super.readFromNBT(tag);

		NBTTagList list = tag.getTagList("Items", 10);
		inventoryContents = NonNullList.<ItemStack>withSize(getSizeInventory(), ItemStack.EMPTY);

		for (int i = 0; i < list.tagCount(); ++i)
		{
			NBTTagCompound stackTag = list.getCompoundTagAt(i);
			int slot = stackTag.getByte("Slot") & 255;

			if (slot >= 0 && slot < inventoryContents.size())
				inventoryContents.set(slot, new ItemStack(stackTag));
		}


		if(tag.hasKey("cooldown"))
			cooldown = tag.getInteger("cooldown");

		if(tag.hasKey("type"))
			type = tag.getString("type");

	}

	@Override
	public NBTTagCompound writeToNBT(NBTTagCompound tag){
		super.writeToNBT(tag);

		NBTTagList list = new NBTTagList();

		for (int i = 0; i < inventoryContents.size(); ++i)
			if (!inventoryContents.get(i).isEmpty())
			{
				NBTTagCompound stackTag = new NBTTagCompound();
				stackTag.setByte("Slot", (byte)i);
				inventoryContents.get(i).writeToNBT(stackTag);
				list.appendTag(stackTag);
			}

		tag.setTag("Items", list);
		tag.setInteger("cooldown", cooldown);
		tag.setString("type", type);
		return tag;
	}

	@Override
	public int getSizeInventory() {
		return 37;
	}

	@Override
	public ItemStack decrStackSize(int index, int count)
	{
		if (!inventoryContents.get(index).isEmpty())
		{
			ItemStack stack;

			if (inventoryContents.get(index).getCount() <= count)
			{
				stack = inventoryContents.get(index);
				inventoryContents.set(index, ItemStack.EMPTY);
				markDirty();
				return stack;
			}
			else
			{
				stack = inventoryContents.get(index).splitStack(count);

				if (inventoryContents.get(index).getCount() == 0)
					inventoryContents.set(index, ItemStack.EMPTY);

				markDirty();
				return stack;
			}
		}
		else
			return ItemStack.EMPTY;
	}

	@Override
	public ItemStack getStackInSlot(int index) {
		return inventoryContents.get(index);
	}

	/**
	 * Copy of getStackInSlot which doesn't get overrided by CustomizableSCTE.
	 */

	public ItemStack getStackInSlotCopy(int index) {
		return inventoryContents.get(index);
	}

	@Override
	public void setInventorySlotContents(int index, ItemStack stack) {
		inventoryContents.set(index, stack);

		if (!stack.isEmpty() && stack.getCount() > getInventoryStackLimit())
			stack.setCount(getInventoryStackLimit());

		markDirty();
	}

	/**
	 * Adds the given stack to the inventory. Will void any excess.
	 * @param stack The stack to add
	 */
	public void addItemToStorage(ItemStack stack)
	{
		ItemStack remainder = stack;

		for(int i = 10; i < getContents().size(); i++)
		{
			remainder = insertItem(i, remainder);

			if(remainder.isEmpty())
				break;
		}
	}

	public ItemStack insertItem(int slot, ItemStack stackToInsert)
	{
		if(stackToInsert.isEmpty() || slot < 0 || slot >= getContents().size())
			return stackToInsert;

		ItemStack slotStack = getStackInSlot(slot);
		int limit = stackToInsert.getItem().getItemStackLimit(stackToInsert);

		if(slotStack.isEmpty())
		{
			setInventorySlotContents(slot, stackToInsert);
			return ItemStack.EMPTY;
		}
		else if(BlockInventoryScannerField.areItemStacksEqual(slotStack, stackToInsert) && slotStack.getCount() < limit)
		{
			if(limit - slotStack.getCount() >= stackToInsert.getCount())
			{
				slotStack.setCount(slotStack.getCount() + stackToInsert.getCount());
				return ItemStack.EMPTY;
			}
			else
			{
				ItemStack toInsert = stackToInsert.copy();
				ItemStack toReturn = toInsert.splitStack((slotStack.getCount() + stackToInsert.getCount()) - limit); //this is the remaining stack that could not be inserted

				slotStack.setCount(slotStack.getCount() + toInsert.getCount());
				return toReturn;
			}
		}

		return stackToInsert;
	}

	@Override
	public boolean hasCustomName() {
		return true;
	}

	@Override
	public int getInventoryStackLimit() {
		return 64;
	}

	@Override
	public boolean isUsableByPlayer(EntityPlayer player) {
		return true;
	}

	@Override
	public void openInventory(EntityPlayer player) {}

	@Override
	public void closeInventory(EntityPlayer player) {}

	@Override
	public boolean isItemValidForSlot(int var1, ItemStack var2) {
		return true;
	}

	public String getType(){
		return type;
	}

	public void setType(String type){
		this.type = type;
	}

	public boolean shouldProvidePower() {
		return (type.equals("redstone") && isProvidingPower) ? true : false;
	}

	public void setShouldProvidePower(boolean isProvidingPower) {
		this.isProvidingPower = isProvidingPower;
	}

	public void setCooldown(int cooldown) {
		this.cooldown = cooldown;
	}

	public NonNullList<ItemStack> getContents(){
		return inventoryContents;
	}

	public void setContents(NonNullList<ItemStack> contents){
		inventoryContents = contents;
	}

	@Override
	public void onModuleInserted(ItemStack stack, EnumCustomModules module)
	{
		super.onModuleInserted(stack, module);

		TileEntityInventoryScanner connectedScanner = BlockInventoryScanner.getConnectedInventoryScanner(world, pos);

		if(connectedScanner != null && !connectedScanner.hasModule(module))
			connectedScanner.insertModule(stack);
	}

	@Override
	public void onModuleRemoved(ItemStack stack, EnumCustomModules module)
	{
		super.onModuleRemoved(stack, module);

		TileEntityInventoryScanner connectedScanner = BlockInventoryScanner.getConnectedInventoryScanner(world, pos);

		if(connectedScanner != null && connectedScanner.hasModule(module))
			connectedScanner.removeModule(module);
	}

	@Override
	public EnumCustomModules[] acceptedModules() {
		return new EnumCustomModules[]{EnumCustomModules.WHITELIST, EnumCustomModules.SMART, EnumCustomModules.STORAGE, EnumCustomModules.DISGUISE};
	}

	@Override
	public Option<?>[] customOptions() {
		return null;
	}

}
