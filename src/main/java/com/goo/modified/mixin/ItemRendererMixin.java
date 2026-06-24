package com.goo.modified.mixin;

import com.goo.goo_lib.common.registry.TextEffects;
import com.goo.goo_lib.utils.text.effect.base.ConfiguredEffect;
import com.goo.goo_lib.utils.text.effect.config.GradientConfig;
import com.goo.modified.common.ClientConfig;
import com.goo.modified.common.Modified;
import com.goo.modified.utils.Tier;
import com.goo.modified.utils.TierUtils;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.entity.ItemRenderer;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.util.FastColor;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import org.joml.Matrix4f;
import org.joml.Quaternionf;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(ItemRenderer.class)
public class ItemRendererMixin {

    @Inject(
            method = "render(Lnet/minecraft/world/item/ItemStack;Lnet/minecraft/world/item/ItemDisplayContext;ZLcom/mojang/blaze3d/vertex/PoseStack;Lnet/minecraft/client/renderer/MultiBufferSource;IILnet/minecraft/client/resources/model/BakedModel;)V",
            at = @At("HEAD")
    )
    private void renderCustomBehindRays(
            ItemStack itemStack,
            ItemDisplayContext displayContext,
            boolean leftHand,
            PoseStack poseStack,
            MultiBufferSource bufferSource,
            int combinedLight,
            int combinedOverlay,
            BakedModel p_model,
            CallbackInfo ci
    ) {
        if (ClientConfig.RENDER_SUNBURST_RAYS.isFalse()) return;
        if (!itemStack.isEmpty() && (displayContext == ItemDisplayContext.GUI || displayContext == ItemDisplayContext.GROUND)) {

            Tier tier = TierUtils.getTier(Modified.getSafeRegistryAccess(), itemStack);
            if (tier == null) return;
            if (!tier.renderRays()) return;

            poseStack.pushPose();
            // per context transforms
            if (displayContext == ItemDisplayContext.GUI) {
                poseStack.mulPose(Axis.YP.rotationDegrees(180));
            } else {
                poseStack.translate(0.0F, p_model.getTransforms().ground.translation.y, 0);

                Matrix4f pose = poseStack.last().pose();
                pose.m00(1);
                pose.m01(0);
                pose.m02(0);
                pose.m10(0);
                pose.m11(1);
                pose.m12(0);
                pose.m20(0);
                pose.m21(0);
                pose.m22(1);

                Quaternionf cam = new Quaternionf(
                        Minecraft.getInstance().gameRenderer.getMainCamera().rotation()
                );
                poseStack.mulPose(cam);
                poseStack.mulPose(Axis.YP.rotationDegrees(180));

                poseStack.scale(0.5F, 0.5F, 0.5F);
            }

            float smoothTicks = (float) (System.currentTimeMillis() % 360000L) / 50.0F;

            int colorA = 0;
            int colorB = 0;
            boolean foundConfig = false;

            for (ConfiguredEffect<?> textEffect : tier.textEffects()) {
                if (textEffect.getConfig() instanceof GradientConfig config) {
                    var configColors = config.colors();
                    if (!configColors.isEmpty()) {
                        colorA = configColors.getFirst() | 0xFF000000; // Force Alpha to 255 (Opaque)

                        if (configColors.size() > 1) {
                            colorB = configColors.getLast() & 0x00FFFFFF; // Force Alpha to 0 (Transparent)
                        } else {
                            colorB = configColors.getFirst() & 0x00FFFFFF; // Fallback to same color, but transparent
                        }
                        foundConfig = true;
                        break;
                    }
                }
            }

            if (!foundConfig) {
                colorA = LEGENDARY_COLORS[0];
                colorB = LEGENDARY_COLORS[LEGENDARY_COLORS.length - 1];
            }

            modified$renderSunburst(RenderType.dragonRays(), poseStack, bufferSource, smoothTicks, colorA, colorB);

            poseStack.popPose();

        }
    }

    @Unique
    private static final Integer[] LEGENDARY_COLORS = new Integer[]{FastColor.ARGB32.color(255, 200, 0), FastColor.ARGB32.color(0,150, 100, 0)};

    @Unique
    private static void modified$renderSunburst(RenderType renderType, PoseStack poseStack, MultiBufferSource bufferSource, float ticks, int colorA, int colorB) {
        VertexConsumer consumer = bufferSource.getBuffer(renderType);

        int rayCount = ClientConfig.SUNBURST_RAY_COUNT.getAsInt();
        float baseRotation = ticks * 0.8F;

        float rayLength = (float) (ClientConfig.SUNBURST_RAY_LENGTH.getAsDouble() + 0.08F * (float) Math.sin(ticks * 0.08F));
        float rayWidth = (float) ClientConfig.SUNBURST_RAY_WIDTH.getAsDouble();

        float angleStep = 360.0F / rayCount;


        for (int i = 0; i < rayCount; i++) {
            poseStack.pushPose();

            float currentAngle = (i * angleStep) + baseRotation;
            poseStack.mulPose(Axis.ZP.rotationDegrees(currentAngle));

            Matrix4f matrix = poseStack.last().pose();

            // Vertex 1: Center
            consumer.addVertex(matrix, 0, 0, 0)
                    .setColor(colorA);

            // Vertex 2: Left Tip
            consumer.addVertex(matrix, -rayWidth * 0.5F, rayLength, 0)
                    .setColor(colorB);

            // Vertex 3: Right Tip
            consumer.addVertex(matrix, rayWidth * 0.5F, rayLength, 0)
                    .setColor(colorB);

            poseStack.popPose();
        }
    }

}