package mjtb49.hashreversals;

import kaptainwutax.mathutils.util.Mth;
import kaptainwutax.mcutils.rand.ChunkRand;
import kaptainwutax.mcutils.rand.seed.RegionSeed;
import kaptainwutax.mcutils.rand.seed.WorldSeed;
import kaptainwutax.mcutils.util.pos.CPos;
import kaptainwutax.mcutils.version.MCVersion;
import org.junit.jupiter.api.Test;

import java.util.Random;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;

public class ChunkRandomReverserTest {
	private final long TESTING_SEED = 923452555189237913L;

	@Test
	public void lattice2d() {
		long regionSeed = 186080818078439L;
		int salt = 14357620;
		int bound = 100;
		for (int regX = -bound; regX < bound; regX++) {
			for (int regZ = -bound; regZ < bound; regZ++) {
				long foundStructureSeed = (regionSeed - regX * RegionSeed.A - regZ * RegionSeed.B & Mth.MASK_48) - salt;
				CPos cPos = ChunkRandomReverser.reverseRegionSeed(regionSeed, foundStructureSeed, salt, MCVersion.latest());
				assertEquals(cPos.getX(), regX);
				assertEquals(cPos.getZ(), regZ);
			}
		}
	}

	private static long moveStructure(long regionSeed, int regX, int regZ) {
		return regionSeed - regX * RegionSeed.A - regZ * RegionSeed.B & Mth.MASK_48;
	}

	@Test
	public void reverseTerrainSeed() {
		Random r = new Random(TESTING_SEED);
		ChunkRand cr = new ChunkRand();

		for (int i = 0; i < 1000; i++) {
			int x = r.nextInt(2 * 1875000) - 1875000;
			int z = r.nextInt(2 * 1875000) - 1875000;
			long tseed = cr.setTerrainSeed(x, z, MCVersion.v1_16);
			assertEquals(ChunkRandomReverser.reverseTerrainSeed(tseed).getX(), x);
			assertEquals(ChunkRandomReverser.reverseTerrainSeed(tseed).getZ(), z);
		}
	}

	@Test
	public void reversePopulationSeedPost13() {
		ChunkRand cr = new ChunkRand();
		Random r = new Random(TESTING_SEED + 1);

		for (int i = 0; i < 100; i++) {
			long seed = r.nextLong() & ((1L << 48) - 1);
			int x = r.nextInt(2 * 1875000) - 1875000;
			int z = r.nextInt(2 * 1875000) - 1875000;
			long cseed = cr.setPopulationSeed(seed, x, z, MCVersion.v1_16);
			assertTrue(ChunkRandomReverser.reversePopulationSeed(cseed, x, z, MCVersion.v1_16).contains(seed));
		}
	}

	@Test
	public void reversePopulationSeedPre13() {
		ChunkRand cr = new ChunkRand();
		Random r = new Random(TESTING_SEED + 1);

		for (int i = 0; i < 100; i++) {
			long seed = r.nextLong() & ((1L << 48) - 1);
			int x = r.nextInt(2 * 100) - 100;
			int z = r.nextInt(2 * 100) - 100;
			long cseed = cr.setPopulationSeed(seed, x, z, MCVersion.v1_12);
			assertTrue(ChunkRandomReverser.reversePopulationSeed(cseed, x, z, MCVersion.v1_12).contains(seed));
		}
	}

	@Test
	public void reverseCarverSeed() {
		ChunkRand cr = new ChunkRand();
		Random r = new Random(TESTING_SEED + 10);

		for (int i = 0; i < 100; i++) {
			long seed = r.nextLong() & ((1L << 48) - 1);
			int x = r.nextInt(2 * 1875000) - 1875000;
			int z = r.nextInt(2 * 1875000) - 1875000;
			long cseed = cr.setCarverSeed(seed, x, z, MCVersion.v1_16);
			assertTrue(ChunkRandomReverser.reverseCarverSeed(cseed, x, z, MCVersion.v1_16).contains(seed));
		}
	}

	@Test
	public void getWorldseedFromTwoChunkseeds() {
		ChunkRand cr = new ChunkRand();

		Random r = new Random();
		for (int i = 0; i < 100; i++) {
			long seed = r.nextLong() & ((1L << 48) - 1);
			int x1 = r.nextInt(2 * 1875000) - 1875000;
			int z1 = r.nextInt(2 * 1875000) - 1875000;
			int x2 = r.nextInt(2 * 1875000) - 1875000;
			int z2 = r.nextInt(2 * 1875000) - 1875000;
			long cs1 = cr.setPopulationSeed(seed, x1 * 16, z1 * 16, MCVersion.v1_16);
			long cs2 = cr.setPopulationSeed(seed, x2 * 16, z2 * 16, MCVersion.v1_16);
			boolean foundSeed = false;
			for (MultiChunkHelper.Result result : ChunkRandomReverser.getWorldseedFromTwoChunkseeds(cs1, cs2, x2 - x1, z2 - z1, MCVersion.v1_16)) {
				foundSeed |= (result.getBitsOfSeed() == seed) && (x1 * 16 == result.getX()) && (z1 * 16 == result.getZ());
			}
			assertTrue(foundSeed);
		}
	}
}