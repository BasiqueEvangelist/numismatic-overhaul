package com.glisco.numismaticoverhaul.block;

import com.glisco.numismaticoverhaul.currency.CurrencyConverter;
import com.glisco.numismaticoverhaul.item.MoneyBagItem;
import com.glisco.numismaticoverhaul.villagers.data.NumismaticTradeOfferExtensions;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.NbtCompound;
import net.minecraft.nbt.NbtElement;
import net.minecraft.nbt.NbtList;
import net.minecraft.util.collection.DefaultedList;
import net.minecraft.village.TradeOffer;

import java.util.List;

public class ShopOffer {

    private final ItemStack sell;
    private final long price;

    public ShopOffer(ItemStack sell, long price) {

        if (sell.isEmpty()) throw new IllegalArgumentException("Sell Stack must not be empty");
        if (price == 0) throw new IllegalArgumentException("Price must not be null");

        this.sell = sell;
        this.price = price;
    }

    @SuppressWarnings("ConstantConditions")
    public TradeOffer toTradeOffer(ShopBlockEntity shop) {

        ItemStack buy = CurrencyConverter.getRequiredCurrencyTypes(price) == 1 ? CurrencyConverter.getAsItemStackList(price).get(0) : MoneyBagItem.create(price);

        int maxUses = count(shop.getItems(), sell) / sell.getCount();

        final var tradeOffer = new TradeOffer(buy, sell, maxUses, 0, 0);
        ((NumismaticTradeOfferExtensions) tradeOffer).numismatic$setReputation(-69420);
        return tradeOffer;
    }

    public long getPrice() {
        return price;
    }

    public ItemStack getSellStack() {
        return sell.copy();
    }

    public static NbtCompound writeAll(NbtCompound tag, List<ShopOffer> offers) {

        NbtList offerList = new NbtList();

        for (ShopOffer offer : offers) {
            offerList.add(offer.toNbt());
        }

        tag.put("Offers", offerList);

        return tag;
    }

    public static void readAll(NbtCompound tag, List<ShopOffer> offers) {
        offers.clear();

        NbtList offerList = tag.getList("Offers", NbtElement.COMPOUND_TYPE);

        for (NbtElement offerTag : offerList) {
            offers.add(fromNbt((NbtCompound) offerTag));
        }
    }

    public NbtCompound toNbt() {
        var nbt = new NbtCompound();
        nbt.putLong("Price", this.price);

        var itemNbt = new NbtCompound();
        this.sell.writeNbt(itemNbt);

        nbt.put("Item", itemNbt);
        return nbt;
    }

    public static ShopOffer fromNbt(NbtCompound nbt) {
        var item = ItemStack.fromNbt(nbt.getCompound("Item"));
        return new ShopOffer(item, nbt.getLong("Price"));
    }

    public static int count(DefaultedList<ItemStack> stacks, ItemStack testStack) {
        int count = 0;
        for (var stack : stacks) {
            if (!ItemStack.canCombine(stack, testStack)) continue;
            count += stack.getCount();
        }
        return count;
    }

    public static int remove(DefaultedList<ItemStack> stacks, ItemStack removeStack) {
        int toRemove = removeStack.getCount();
        for (var stack : stacks) {
            if (!ItemStack.canCombine(stack, removeStack)) continue;

            int removed = stack.getCount();
            stack.decrement(toRemove);

            toRemove -= removed;
            if (toRemove < 1) break;
        }
        return removeStack.getCount() - toRemove;
    }

    @Override
    public String toString() {
        return this.sell + "@" + this.price + "coins";
    }
}
