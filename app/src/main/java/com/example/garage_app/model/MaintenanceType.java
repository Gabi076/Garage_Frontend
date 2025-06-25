package com.example.garage_app.model;

public enum MaintenanceType {
    ULEI,
    REVIZIE,
    ANVELOPE,
    PIESE,
    ALTELE;

    @Override
    public String toString() {
        switch (this) {
            case ULEI: return "Ulei";
            case REVIZIE: return "Revizie";
            case ANVELOPE: return "Anvelope";
            case PIESE: return "Piese";
            case ALTELE: return "Altele";
            default: return name();
        }
    }
}
