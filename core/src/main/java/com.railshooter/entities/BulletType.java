package src.main.java.com.railshooter.entities;

/** Типы пуль игрока */
public enum BulletType {
    NORMAL,     // Обычная — 1 хп урона, останавливается
    PIERCING,   // Пробивающая — 1 урон, проходит сквозь врагов
    EXPLOSIVE   // Взрывная — 2 урона, область 50px
}
