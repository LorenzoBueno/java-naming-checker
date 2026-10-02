package br.pucrs.plp;

import com.github.javaparser.ParseProblemException;
import com.github.javaparser.ParserConfiguration;
import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.expr.SimpleName;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Main {
    public static void main(String[] args) throws IOException {
        if (args.length < 1) {
            System.err.println("Uso: Main <arquivo.java> [--fix]");
            System.exit(1);
        }
        Path caminho = Path.of(args[0]);
        boolean fix = args.length > 1 && args[1].equals("--fix");

        StaticJavaParser.getParserConfiguration()
                .setLanguageLevel(ParserConfiguration.LanguageLevel.JAVA_17);

        CompilationUnit cu;
        try {
            cu = StaticJavaParser.parse(caminho);   // texto -> AST
        } catch (ParseProblemException e) {
            System.err.println("Erro de sintaxe: " + e.getProblems());
            System.exit(2);
            return;
        }

        List<Violation> violacoes = new ArrayList<>();
        cu.accept(new NameVisitor(), violacoes);    // percorre a AST
        violacoes.sort(Comparator.comparingInt(Violation::linha));

        if (violacoes.isEmpty()) {
            System.out.println("Nenhuma violação encontrada.");
            return;
        }
        for (Violation v : violacoes) {
            System.out.printf("[ERRO] Linha %-3d | %-9s | %-16s -> sugestão: %s%n",
                    v.linha(), v.tipo(), v.nome(), v.sugestao());
        }

        // EXTENSÃO: correção automática (transformação da AST)
        if (fix) {
            Map<String, String> renomeios = new HashMap<>();
            for (Violation v : violacoes) renomeios.put(v.nome(), v.sugestao());

            cu.findAll(SimpleName.class).forEach(sn -> {
                String novo = renomeios.get(sn.getIdentifier());
                if (novo != null) sn.setIdentifier(novo);
            });

            Path saida = Path.of(args[0] + ".corrigido.txt");
            Files.writeString(saida, cu.toString());
            System.out.println("\nCódigo corrigido salvo em: " + saida);
        }
    }
}