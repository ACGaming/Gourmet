package tehnut.gourmet.block;

import com.google.common.collect.ImmutableMap;
import net.minecraft.block.BlockBush;
import net.minecraft.block.BlockGrass;
import net.minecraft.block.SoundType;
import net.minecraft.block.properties.IProperty;
import net.minecraft.block.properties.PropertyBool;
import net.minecraft.block.properties.PropertyInteger;
import net.minecraft.block.state.BlockStateContainer;
import net.minecraft.block.state.IBlockState;
import net.minecraft.entity.player.EntityPlayer;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.util.EnumFacing;
import net.minecraft.util.EnumHand;
import net.minecraft.util.math.AxisAlignedBB;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.RayTraceResult;
import net.minecraft.world.IBlockAccess;
import net.minecraft.world.World;
import net.minecraftforge.items.ItemHandlerHelper;
import tehnut.gourmet.Gourmet;
import tehnut.gourmet.core.RegistrarGourmet;
import tehnut.gourmet.core.data.Harvest;
import tehnut.gourmet.core.util.IHarvestContainer;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import java.util.Random;

public class BlockBerryBush extends BlockBush implements IHarvestContainer {

    public static final IProperty<Integer> AGE = PropertyInteger.create("age", 0, 3);
    public static final IProperty<Boolean> TASTY = PropertyBool.create("tasty");
    private static final ImmutableMap<Integer, AxisAlignedBB> AABBS = ImmutableMap.of(
            0, new AxisAlignedBB(0.3125D, 0, 0.3125D, 0.6875D, 0.375D, 0.6875D),
            1, new AxisAlignedBB(0.25D, 0, 0.25D, 0.75D, 0.5D, 0.75D),
            2, new AxisAlignedBB(0.125D, 0, 0.125D, 0.875D, 0.8125D, 0.875D),
            3, FULL_BLOCK_AABB
    );

    private final Harvest harvest;
    private Item berryItem;

    public BlockBerryBush(Harvest harvest) {
        this.harvest = harvest;

        setCreativeTab(Gourmet.TAB_GOURMET);
        setUnlocalizedName(Gourmet.MODID + ".bush_" + harvest.getSimpleName());
        setDefaultState(getBlockState().getBaseState().withProperty(AGE, 0).withProperty(TASTY, false));
        setSoundType(SoundType.PLANT);
    }

    @Override
    public boolean onBlockActivated(World world, BlockPos pos, IBlockState state, EntityPlayer player, EnumHand hand, EnumFacing facing, float hitX, float hitY, float hitZ) {
        if (state.getValue(TASTY) && !world.isRemote) {
            if (berryItem == null)
                berryItem = RegistrarGourmet.getEdibles().get(harvest);

            ItemHandlerHelper.giveItemToPlayer(player, new ItemStack(berryItem, Math.max(1, world.rand.nextInt(4))));
            world.setBlockState(pos, state.withProperty(TASTY, false));
            return true;
        }

        return state.getValue(TASTY) || super.onBlockActivated(world, pos, state, player, hand, facing, hitX, hitY, hitZ);
    }

    @Override
    public ItemStack getPickBlock(IBlockState state, RayTraceResult target, World world, BlockPos pos, EntityPlayer player) {
        return new ItemStack(this);
    }

    @Override
    public void updateTick(World world, BlockPos pos, IBlockState state, Random rand) {
        super.updateTick(world, pos, state, rand);
        if (state.getValue(TASTY) && state.getValue(AGE) < 3)
            world.setBlockState(pos, state.withProperty(TASTY, false));

        if (state.getValue(AGE) < 3) {
            if(rand.nextFloat() >= 0.85)
                world.setBlockState(pos, state.cycleProperty(AGE));
        } else if (!state.getValue(TASTY) && rand.nextFloat() >= 0.95F) {
            world.setBlockState(pos, state.withProperty(TASTY, true));
        }
    }

    @Override
    public AxisAlignedBB getBoundingBox(IBlockState state, IBlockAccess source, BlockPos pos) {
        return AABBS.get(state.getValue(AGE));
    }

    @Override
    public boolean canBlockStay(World world, BlockPos pos, IBlockState state) {
        IBlockState down = world.getBlockState(pos.down());
        return down.getBlock() instanceof BlockGrass || (down.getBlock() instanceof BlockBerryBush && down.getValue(AGE) == 3);
    }

    @Override
    public boolean canPlaceBlockAt(World world, BlockPos pos) {
        IBlockState state = world.getBlockState(pos.down());
        return state.getBlock() instanceof BlockGrass || (state.getBlock() instanceof BlockBerryBush && state.getValue(AGE) == 3);
    }

    @Nullable
    @Override
    public AxisAlignedBB getCollisionBoundingBox(IBlockState blockState, IBlockAccess worldIn, BlockPos pos) {
        return blockState.getBoundingBox(worldIn, pos);
    }

    @Override
    public IBlockState getStateFromMeta(int meta) {
        if (meta >= 4)
            return getDefaultState().withProperty(AGE, 3).withProperty(TASTY, true);

        return getDefaultState().withProperty(AGE, meta);
    }

    @Override
    public int getMetaFromState(IBlockState state) {
        return state.getValue(TASTY) ? 4 : state.getValue(AGE);
    }

    @Override
    protected BlockStateContainer createBlockState() {
        return new BlockStateContainer.Builder(this).add(AGE, TASTY).build();
    }

    @Nonnull
    @Override
    public Harvest getHarvest() {
        return harvest;
    }
}
