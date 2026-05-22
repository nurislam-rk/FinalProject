package com.railshooter.biome;

/** Биомы тоннеля — меняются каждые 5 волн */
public enum Biome {
    MINE,           // Волны 1-4: коричнево-серая шахта с факелами
    UNDERGROUND_RIVER, // Волны 5-9: голубой влажный тоннель, капли воды
    CRYSTAL_CAVE,   // Волны 10-14: фиолетовые кристаллы, светящиеся стены
    LAVA_CAVERN;    // Волны 15+: оранжевый, лавовые блики

    public static Biome fromWave(int wave) {
        if (wave < 5)  return MINE;
        if (wave < 10) return UNDERGROUND_RIVER;
        if (wave < 15) return CRYSTAL_CAVE;
        return LAVA_CAVERN;
    }

    // Цвет левой/правой каменной стены (r,g,b)
    public float[] wallColor() {
        switch (this) {
            case MINE:              return new float[]{0.18f, 0.14f, 0.10f};
            case UNDERGROUND_RIVER: return new float[]{0.10f, 0.16f, 0.25f};
            case CRYSTAL_CAVE:      return new float[]{0.18f, 0.10f, 0.28f};
            case LAVA_CAVERN:       return new float[]{0.28f, 0.12f, 0.06f};
            default:                return new float[]{0.18f, 0.14f, 0.10f};
        }
    }

    // Цвет пола тоннеля
    public float[] floorColor() {
        switch (this) {
            case MINE:              return new float[]{0.22f, 0.18f, 0.13f};
            case UNDERGROUND_RIVER: return new float[]{0.14f, 0.22f, 0.30f};
            case CRYSTAL_CAVE:      return new float[]{0.22f, 0.14f, 0.32f};
            case LAVA_CAVERN:       return new float[]{0.30f, 0.16f, 0.08f};
            default:                return new float[]{0.22f, 0.18f, 0.13f};
        }
    }

    // Цвет акцента (факелы / кристаллы / лава)
    public float[] accentColor() {
        switch (this) {
            case MINE:              return new float[]{1.0f, 0.55f, 0.10f};
            case UNDERGROUND_RIVER: return new float[]{0.30f, 0.70f, 1.00f};
            case CRYSTAL_CAVE:      return new float[]{0.80f, 0.30f, 1.00f};
            case LAVA_CAVERN:       return new float[]{1.0f, 0.35f, 0.05f};
            default:                return new float[]{1.0f, 0.55f, 0.10f};
        }
    }

    public String displayName() {
        switch (this) {
            case MINE:              return "ШАХТА";
            case UNDERGROUND_RIVER: return "ПОДЗЕМНАЯ РЕКА";
            case CRYSTAL_CAVE:      return "ПЕЩЕРА КРИСТАЛЛОВ";
            case LAVA_CAVERN:       return "ЛАВОВЫЕ НЕДРА";
            default:                return "ШАХТА";
        }
    }
}
