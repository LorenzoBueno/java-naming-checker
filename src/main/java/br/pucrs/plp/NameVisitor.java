package br.pucrs.plp;

import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.FieldDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.github.javaparser.ast.body.Parameter;
import com.github.javaparser.ast.body.VariableDeclarator;
import com.github.javaparser.ast.expr.SimpleName;
import com.github.javaparser.ast.expr.VariableDeclarationExpr;
import com.github.javaparser.ast.visitor.VoidVisitorAdapter;

import java.util.List;
import java.util.function.Function;
import java.util.function.Predicate;

public class NameVisitor extends VoidVisitorAdapter<List<Violation>> {

    @Override
    public void visit(ClassOrInterfaceDeclaration n, List<Violation> out) {
        check(n.getName(), "Classe", NamingUtils::isPascal, NamingUtils::toPascal, out);
        super.visit(n, out);
    }

    @Override
    public void visit(MethodDeclaration n, List<Violation> out) {
        check(n.getName(), "Método", NamingUtils::isCamel, NamingUtils::toCamel, out);
        super.visit(n, out);
    }

    @Override
    public void visit(Parameter n, List<Violation> out) {
        check(n.getName(), "Parâmetro", NamingUtils::isCamel, NamingUtils::toCamel, out);
        super.visit(n, out);
    }

    @Override
    public void visit(FieldDeclaration n, List<Violation> out) {
        boolean constante = n.isStatic() && n.isFinal();
        for (VariableDeclarator v : n.getVariables()) {
            if (constante) {
                check(v.getName(), "Constante", NamingUtils::isUpperSnake, NamingUtils::toUpperSnake, out);
            } else {
                check(v.getName(), "Campo", NamingUtils::isCamel, NamingUtils::toCamel, out);
            }
        }
        super.visit(n, out);
    }

    // variáveis locais (campos são tratados em visit(FieldDeclaration))
    @Override
    public void visit(VariableDeclarationExpr n, List<Violation> out) {
        for (VariableDeclarator v : n.getVariables()) {
            check(v.getName(), "Variável", NamingUtils::isCamel, NamingUtils::toCamel, out);
        }
        super.visit(n, out);
    }

    private void check(SimpleName nome, String tipo,
                       Predicate<String> valido, Function<String, String> sugerir,
                       List<Violation> out) {
        String id = nome.getIdentifier();
        if (!valido.test(id)) {
            int linha = nome.getBegin().map(p -> p.line).orElse(-1);
            out.add(new Violation(linha, tipo, id, sugerir.apply(id)));
        }
    }
}