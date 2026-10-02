package br.pucrs.plp;

public record Violation(int linha, String tipo, String nome, String sugestao) {}