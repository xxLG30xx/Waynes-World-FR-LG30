package fr.lg30.waynesworld;
public enum Cheat {
 TEMPS_INFINI("Temps infini",new int[][]{{0x01BC,9},{0x01BD,9},{0x01BE,9}}), INVINCIBILITE("Invincibilité",new int[][]{{0x00D5,5}}), SCHWING_ILLIMITE("Schwing illimité",new int[][]{{0x011F,1}}), TIR_RAPIDE("Tir rapide / PCB illimité",new int[][]{{0x00D1,3}});
 public final String label; public final int[][] writes; Cheat(String l,int[][] w){label=l;writes=w;} public String toString(){return label;}
}
