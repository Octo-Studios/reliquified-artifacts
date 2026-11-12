package it.hurts.shatterbyte.reliquified_artifacts.network.packets;

import artifacts.registry.ModItems;
import it.hurts.shatterbyte.reliquified_artifacts.ReliquifiedArtifacts;
import it.hurts.shatterbyte.reliquified_artifacts.items.charm.HeliumFlamingoItem;
import it.hurts.sskirillss.relics.utils.EntityUtils;
import lombok.AllArgsConstructor;
import lombok.Data;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;
import top.theillusivec4.curios.api.CuriosApi;
import top.theillusivec4.curios.api.SlotResult;
import top.theillusivec4.curios.api.type.capability.ICuriosItemHandler;

import java.util.Optional;

@Data
@AllArgsConstructor
public class FlamingoSwimPacket implements CustomPacketPayload {
    private final boolean toggled;

    public static final CustomPacketPayload.Type<FlamingoSwimPacket> TYPE = new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath(ReliquifiedArtifacts.MODID, "swim"));

    public static final StreamCodec<RegistryFriendlyByteBuf, FlamingoSwimPacket> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.BOOL, FlamingoSwimPacket::isToggled,
            FlamingoSwimPacket::new);

    public void handle(IPayloadContext ctx) {
        ctx.enqueueWork(() -> {
            Player player = ctx.player();
            ItemStack stack = EntityUtils.findEquippedCurio(player, ModItems.HELIUM_FLAMINGO.value());

            if (!(stack.getItem() instanceof HeliumFlamingoItem relic))
                return;

            Optional<ICuriosItemHandler> inventoryOptional = CuriosApi.getCuriosInventory(player);
            if (inventoryOptional.isEmpty()) return;
            Optional<SlotResult> slotOptional = inventoryOptional.get().findFirstCurio(itemStack -> itemStack == stack);
            if (slotOptional.isEmpty()) return;

            if (!relic.trySetHovering(slotOptional.get().slotContext(), stack, toggled))
                return;

            if (toggled && !player.isInLiquid()) {
                player.setDeltaMovement(player.getDeltaMovement().add(player.getLookAngle().scale(0.6F)));
                player.hasImpulse = true;
                player.fallDistance = 0F;
            }
        });
    }

    @Override
    public CustomPacketPayload.@NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
