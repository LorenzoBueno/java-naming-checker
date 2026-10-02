# java-naming-checker

A tool that reads a `.java` file, builds its **AST** with **JavaParser**, and checks whether identifiers follow Java naming conventions. Besides reporting violations, it can **fix the names automatically** (the `--fix` extension).

Practical Assignment 2 (TP2) for the **Programming Languages** course, PUCRS / School of Technology (Escola Politécnica), 2026/II.

## Team

- [Name 1]
- [Name 2]
- [Name 3]

## Problem

Naming conventions make code more readable and consistent, but checking them by hand is tedious and error-prone. This tool analyzes source code through its **syntactic structure** (not as plain text) and reports every name that breaks the rules, with the line number and a suggested fix.

## Conventions checked

| Element | Convention | Valid example |
|---|---|---|
| Class | PascalCase | `ContaBancaria` |
| Method | camelCase | `calcularTotal` |
| Parameter | camelCase | `valorBase` |
| Field | camelCase | `saldo` |
| Local variable | camelCase | `novoSaldo` |
| Constant (`static final`) | UPPER_SNAKE_CASE | `MAX_ITENS` |

## How it works

```
.java file → JavaParser → AST → Visitor (NameVisitor) → report
                                      └─ (--fix) transforms the AST → fixed code
```

1. `StaticJavaParser.parse` reads the file and produces the AST (`CompilationUnit`).
2. `NameVisitor`, a subclass of `VoidVisitorAdapter`, walks the tree.
3. At each relevant node it validates the name using `NamingUtils` and records a `Violation` (line, kind, name, suggestion).
4. Violations are sorted by line and printed in the report.

### AST nodes used

| AST node | What the visitor checks |
|---|---|
| `ClassOrInterfaceDeclaration` | name is PascalCase |
| `MethodDeclaration` | name is camelCase |
| `Parameter` | name is camelCase |
| `FieldDeclaration` | `static final` → UPPER_SNAKE_CASE; otherwise camelCase |
| `VariableDeclarationExpr` + `VariableDeclarator` | local variables are camelCase |

Example AST for a snippet with violations:

```
ClassOrInterfaceDeclaration  "minha_classe"    ← should be MinhaClasse
 ├─ FieldDeclaration (static final) "maxItens" ← should be MAX_ITENS
 └─ MethodDeclaration  "Calcular_Total"        ← should be calcularTotal
     └─ Parameter "ValorBase"                  ← should be valorBase
```

## Technologies and references

- Java 17
- [JavaParser](https://javaparser.org/) (`com.github.javaparser:javaparser-core:3.26.2`): Java parser and AST (third-party library).
- Maven + `exec-maven-plugin`
- [Additional references from the team]

## Prerequisites

- JDK 17 or later
- Maven 3.6 or later

## Installation and usage

```bash
git clone https://github.com/[username]/java-naming-checker.git
cd java-naming-checker

# check only
mvn -q compile exec:java -Dexec.args="exemplos/Exemplo2.java"

# check and generate the fixed code (extension)
mvn -q compile exec:java -Dexec.args="exemplos/Exemplo3.java --fix"
```

## Examples

The input files are in the `exemplos/` folder.

### Example 1: Code with no violations

**Input** (`exemplos/Exemplo1.java`):

```java
public class ContaBancaria {
    private static final int LIMITE_SAQUE = 1000;
    private double saldo;

    public void depositar(double valorDeposito) {
        double novoSaldo = saldo + valorDeposito;
        saldo = novoSaldo;
    }
}
```

**Output:**

```
Nenhuma violação encontrada.
```

**Nodes used:** `ClassOrInterfaceDeclaration`, `FieldDeclaration`, `MethodDeclaration`, `Parameter`, `VariableDeclarationExpr`.
**Processing:** the visitor walked every node and all names passed validation. This example shows the tool does not produce false positives.

### Example 2: Class and methods that break the conventions

**Input** (`exemplos/Exemplo2.java`):

```java
public class conta_bancaria {
    private double saldo;

    public void Depositar_Valor(double valor) {
        saldo += valor;
    }

    public double ObterSaldo() {
        return saldo;
    }
}
```

**Output:**

```
[ERRO] Linha 1   | Classe    | conta_bancaria   -> sugestão: ContaBancaria
[ERRO] Linha 4   | Método    | Depositar_Valor  -> sugestão: depositarValor
[ERRO] Linha 8   | Método    | ObterSaldo       -> sugestão: obterSaldo
```

**Nodes used:** `ClassOrInterfaceDeclaration`, `MethodDeclaration`.
**Processing:** at the class node the visitor validated PascalCase; at each method node it validated camelCase. For every invalid name, `NamingUtils` split it into words (on `_` and on case boundaries) and rebuilt it in the correct style.

### Example 3: Constant, field, parameters, and variable that break the conventions

**Input** (`exemplos/Exemplo3.java`):

```java
public class Relatorio {
    static final int maxItens = 10;
    private String NomeAutor;

    public int somar(int ValorA, int valor_b) {
        int Resultado = ValorA + valor_b;
        return Resultado;
    }
}
```

**Output:**

```
[ERRO] Linha 2   | Constante | maxItens         -> sugestão: MAX_ITENS
[ERRO] Linha 3   | Campo     | NomeAutor        -> sugestão: nomeAutor
[ERRO] Linha 5   | Parâmetro | ValorA           -> sugestão: valorA
[ERRO] Linha 5   | Parâmetro | valor_b          -> sugestão: valorB
[ERRO] Linha 6   | Variável  | Resultado        -> sugestão: resultado
```

**Nodes used:** `FieldDeclaration` (using `isStatic()` and `isFinal()` to tell constants from regular fields), `Parameter`, `VariableDeclarationExpr`, `VariableDeclarator`.
**Processing:** the visitor chose the convention based on the node type and the field modifiers, then applied the matching validation.

## Extension: automatic fixing (`--fix`)

With the `--fix` flag, besides the report, the tool **transforms the AST**: it builds a map of `invalid name → suggested name`, walks all `SimpleName` nodes, and replaces the identifier (`setIdentifier`). It then prints the modified AST with `cu.toString()` and writes the result to `<file>.corrigido.txt`.

```bash
mvn -q compile exec:java -Dexec.args="exemplos/Exemplo3.java --fix"
```

Because the rename happens on the AST, declarations and their usages are renamed together (for example, `ValorA` in the parameter declaration and in the expression `ValorA + valor_b`).

## Known limitations

- `--fix` does not distinguish scopes: if two different elements share the same invalid name, both are renamed together.
- Name collisions may occur if the suggested name already exists in the same scope.
- The fixed output is written with a `.txt` extension so it is not confused with the original file.
- Report messages are in Portuguese.

## Repository structure

```
java-naming-checker/
├── pom.xml
├── exemplos/
│   ├── Exemplo1.java
│   ├── Exemplo2.java
│   └── Exemplo3.java
└── src/main/java/br/pucrs/plp/
    ├── Main.java
    ├── NameVisitor.java
    ├── NamingUtils.java
    └── Violation.java
```

## Presentation video

[Video link]

## Credits

Academic project. JavaParser is developed by its maintainers and used under its original license (Apache 2.0 / LGPL).
