package com.seedfinding.mcreversal;

import com.seedfinding.mcmath.component.vector.QVector;
import com.seedfinding.mcmath.util.Mth;
import com.seedfinding.mccore.rand.ChunkRand;
import com.seedfinding.mccore.util.pos.CPos;
import com.seedfinding.mccore.version.MCVersion;
import com.seedfinding.mccore.version.UnsupportedVersion;

import java.util.ArrayList;
import java.util.List;

public final class ChunkRandomReverser {

    public static final Lattice2D REGION_LATTICE = new Lattice2D(341873128712L, 132897987541L, 1L << 48);
    public static final int NUM_CHUNKS_ON_AXIS = 1875000;

    public static CPos reverseTerrainSeed(long terrainSeed) {
        return reverseTerrainSeed(terrainSeed, -NUM_CHUNKS_ON_AXIS, -NUM_CHUNKS_ON_AXIS, NUM_CHUNKS_ON_AXIS, NUM_CHUNKS_ON_AXIS);
    }

    public static CPos reverseTerrainSeed(long terrainSeed, int minX, int minZ, int maxX, int maxZ) {
        List<QVector> results = REGION_LATTICE.findSolutionsInBox(terrainSeed, minX, minZ, maxX, maxZ);
        if(results.isEmpty())return null;
        else if(results.size() == 1)return new CPos(results.get(0).get(0).intValue(), results.get(0).get(1).intValue());
        throw new IndexOutOfBoundsException("Bounds too large to identify a unique seed. If this is actually a problem for some horrifying future version of minecraft open a github issue but as of right now this should never run so I am legally allowed to write a long and funny error message instead of something more helpful.");
    }

    public static CPos reverseRegionSeed(long regionSeed, long worldSeed, int salt, MCVersion version) {
        return reverseTerrainSeed(regionSeed - (worldSeed & Mth.MASK_48) - salt);
    }

    public static CPos reverseRegionSeed(long regionSeed, int minX, int minZ, int maxX, int maxZ, long worldSeed, int salt, MCVersion version) {
        return reverseTerrainSeed(regionSeed - (worldSeed & Mth.MASK_48) - salt, minX, minZ, maxX, maxZ);
    }

    public static long reverseDecoratorSeed(long decoratorSeed, int index, int step, MCVersion version) {
        if(version.isOlderThan(MCVersion.v1_13)) {
            throw new UnsupportedVersion(version, "decorator seed");
        }

        return (decoratorSeed - index - 10000L * step) & Mth.MASK_48;
    }

    /**
     * Reverses the population seed hash (x*nextLong() + z*nextLong() ^ seed)
     * @param populationSeed the population seed
     * @param x the x chunk coordinate to find the seed at
     * @param z the z chunk coordinate to find the seed at
	 * @param version the version
     * @return list of worldseeds with the given population seed at the desired location
     */
    public static List<Long> reversePopulationSeed(long populationSeed, int x, int z, MCVersion version) {
        //TODO: Kill this eventually.
        if(version.isOlderThan(MCVersion.v1_13)) {
            return PopulationReverser.getSeedFromChunkseedPre13(populationSeed & Mth.MASK_48, x, z);
        }

        return PopulationReverser.reverse(populationSeed & Mth.MASK_48, x, z, new ChunkRand(), version);
    }

    /**
     * Reverses seeds from the x*nextLong()^z*nextLong()^seed hash used by mineshafts/caves/strongholds
     * @param carverSeed the output of the hash
     * @param x the x coordinate of the chunk
     * @param z the z coordinate of the chunk
	 * @param version the version
     * @return a list of worldseeds with the given carver seed at the desired location
     */
    public static List<Long> reverseCarverSeed(long carverSeed, int x, int z, MCVersion version) {
        return CarverReverser.reverse(carverSeed & Mth.MASK_48, x, z, new ChunkRand(), version);
    }

    /**
     * A method to locate worldseeds with two population chunkseeds separated by a given vector
     * @param chunkseed1 the first chunkseed
     * @param chunkseed2 the second chunkseed
     * @param chunkDx the x of the second chunkseed minus the x of the first, chunk coordinates
     * @param chunkDz the z of the second chunkseed minus the z of the first, chunk coordinates
	 * @param version the version
     * @return a list of all worldseeds and coords at which the chunkseeds can be found on those worldseeds.
     */
    public static ArrayList<MultiChunkHelper.Result> getWorldseedFromTwoChunkseeds(long chunkseed1, long chunkseed2, int chunkDx, int chunkDz, MCVersion version) {
        MultiChunkHelper helper = new MultiChunkHelper();
        return helper.getWorldseedFromTwoChunkseeds(chunkseed1, chunkseed2, 16*chunkDx, 16*chunkDz, version);
    }

    //TODO - Slime chunk Reversal
    //TODO - All trivial reversals.

}
