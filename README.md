🏦 Construí uma API de transferências bancárias do zero — e usei o projeto pra estudar de verdade os problemas que sistemas financeiros reais precisam resolver.

Alguns dos desafios que enfrentei:

🔹 Como representar dinheiro sem erro de arredondamento? (BigDecimal, nunca double)
🔹 Como impedir que duas transferências simultâneas corrompam o saldo de uma conta? (lock pessimista)
🔹 Como evitar deadlock quando duas contas transferem uma pra outra ao mesmo tempo? (ordenação consistente de locks)
🔹 Como proteger endpoints financeiros sem guardar sessão no servidor? (JWT stateless)

Stack: Java 21 + Spring Boot 4.1 + Spring Security + JPA/Hibernate + JUnit/Mockito

O projeto tem autenticação completa, transações atômicas, tratamento global de exceções e testes automatizados cobrindo as regras de negócio críticas.

Código completo e documentado no GitHub — explico lá cada decisão técnica e por quê.

🔗 [https://github.com/moacirnascimento439/bank-transfer-api]

Estou em busca de oportunidades como desenvolvedor Java, com interesse especial no setor bancário. Se seu time está contratando ou se você tiver feedback sobre o projeto, ficarei muito feliz em conversar!

#Java #SpringBoot #Backend #DesenvolvedorJava #Fintech #API #JWT
