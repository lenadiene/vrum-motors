package br.com.vrum.util;

import jakarta.persistence.EntityManager;
import jakarta.persistence.EntityManagerFactory;
import jakarta.persistence.Persistence;
import java.util.HashMap;
import java.util.Map;

public class JPAUtil {

    private static final String PERSISTENCE_UNIT = "vrumMotorsPU";
    private static EntityManagerFactory factory;

    static {
        try {
            Map<String, String> props = new HashMap<>();
            props.put("jakarta.persistence.jdbc.driver", "com.mysql.cj.jdbc.Driver");
            props.put("jakarta.persistence.jdbc.url",
                    "jdbc:mysql://localhost:3306/vrum_motors?useSSL=false&serverTimezone=America/Sao_Paulo&allowPublicKeyRetrieval=true");
            props.put("jakarta.persistence.jdbc.user", "root");
            props.put("jakarta.persistence.jdbc.password", "root");
            factory = Persistence.createEntityManagerFactory(PERSISTENCE_UNIT, props);
        } catch (Exception e) {
            throw new ExceptionInInitializerError("Erro ao criar EntityManagerFactory: " + e.getMessage());
        }
    }

    public static EntityManager getEntityManager() {
        return factory.createEntityManager();
    }

    public static void closeFactory() {
        if (factory != null && factory.isOpen()) {
            factory.close();
        }
    }
}
