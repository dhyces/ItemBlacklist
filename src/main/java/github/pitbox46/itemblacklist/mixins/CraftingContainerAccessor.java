package github.pitbox46.itemblacklist.mixins;

import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.CraftingContainer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(CraftingContainer.class)
public interface CraftingContainerAccessor {

    @Accessor
    AbstractContainerMenu getMenu();
}
