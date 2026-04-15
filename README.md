# VISA Backoffice - Système de Gestion

## 📋 Vue d'ensemble
**VISA Backoffice** est une application backend construite avec **Spring Boot 4.0.5** pour gérer les opérations administratives du système VISA. C'est une API RESTful qui communique avec une base de données PostgreSQL pour persister les données.

## 🛠️ Stack Technologique
| Composant | Version | Rôle |
|-----------|---------|------|
| **Spring Boot** | 4.0.5 | Framework principal |
| **Java** | 21 | Langage de programmation |
| **Spring Data JPA** | 4.0.5 | ORM - Accès aux données |
| **PostgreSQL** | (Runtime) | Base de données relationnelle |
| **Lombok** | (Dernière) | Génération de code (getters, setters, constructeurs) |
| **Maven** | 3.6.3+ | Gestionnaire de dépendances et build |

## 📦 Prérequis

### Avant de commencer, assurez-vous d'avoir:

1. **Java 21** installé
   ```bash
   java -version
   # Doit afficher java version "21.x.x"
   ```

2. **Maven 3.6.3+** installé
   ```bash
   mvn -version
   # Doit afficher Maven 3.6.3+
   ```

3. **PostgreSQL 12+** installé et en cours d'exécution
   ```bash
   # Vérifier que PostgreSQL écoute sur le port 5432
   psql --version
   ```

4. **Une base de données PostgreSQL** créée
   ```sql
   CREATE DATABASE visa_db;
   CREATE USER postgres WITH PASSWORD 'postgres';
   GRANT ALL PRIVILEGES ON DATABASE visa_db TO postgres;
   ```

## 🏗️ Structure du Projet

```
backoffice/
│
├── src/
│   ├── main/
│   │   ├── java/com/visa/backoffice/
│   │   │   ├── BackofficeApplication.java          # Application principale (point d'entrée)
│   │   │   └── controller/
│   │   │       └── TestController.java              # Contrôleur REST (endpoints)
│   │   │
│   │   └── resources/
│   │       └── application.properties               # Configuration de l'application
│   │
│   └── test/
│       └── java/com/visa/backoffice/
│           └── BackofficeApplicationTests.java      # Tests unitaires
│
├── pom.xml                                          # Configuration Maven (dépendances)
├── mvnw & mvnw.cmd                                  # Maven Wrapper (Windows/Linux)
├── .gitignore                                       # Fichiers à ignorer par Git
└── README.md                                        # Ce fichier

```

## ⚙️ Configuration

### 1. **application.properties** (Configuration de l'application)

Location: `src/main/resources/application.properties`

```properties
# Nom de l'application
spring.application.name=backoffice


# Configuration JPA/Hibernate
spring.jpa.hibernate.ddl-auto=update                # Crée/met à jour les tables automatiquement
spring.jpa.show-sql=true                            # Affiche les requêtes SQL
spring.jpa.properties.hibernate.dialect=org.hibernate.dialect.PostgreSQLDialect

# Port du serveur
server.port=8080
```

### 2. **Dépendances Maven** (dans pom.xml)

- **spring-boot-starter-web**: Framework web REST
- **spring-boot-starter-data-jpa**: Accès aux données avec Hibernate
- **postgresql**: Driver JDBC PostgreSQL
- **lombok**: Génération de code et annotations utilitaires
- **spring-boot-starter-test**: Framework de test

## Comment Lancer le Projet

### Option 1: Avec Maven (Recommandé)

```bash
# 1. Naviguer vers le répertoire du projet
cd f:\ITU\S6\MrNaina\PROJET_VISA\backoffice

# 2. Vérifier que PostgreSQL est en cours d'exécution
# (La base de données visa_db doit exister)

# 3. Lancer l'application
mvn spring-boot:run

# L'application démarre sur http://localhost:8080
```

### Option 2: Avec Maven Wrapper (Ne nécessite pas Maven installé)

```bash
cd f:\ITU\S6\MrNaina\PROJET_VISA\backoffice

# Windows
mvnw.cmd spring-boot:run

# Linux/Mac
./mvnw spring-boot:run
```

### Option 3: Build et Exécution JAR

```bash
# 1. Builder un JAR exécutable
mvn clean package

# 2. Exécuter le JAR
java -jar target/backoffice-0.0.1-SNAPSHOT.jar

# L'application démarre sur http://localhost:8080
```


## Base de Données

### Configuration
- **Type**: PostgreSQL
- **Host**: localhost
- **Port**: 5432
- **Database**: visa_db
- **Utilisateur**: postgres
- **Mot de passe**: postgres

### Créer la Base de Données
```sql
-- Se connecter à PostgreSQL en tant qu'administrateur
-- Puis exécuter:
"CREATE DATABASE visa_db;"


## Vérifier que tout Fonctionne

1. **Vérifier que PostgreSQL est actif**
   ```bash
   psql -U postgres -d visa_db
   ```

2. **Lancer l'application**
   mvn spring-boot:run

3. **Tester**
   - `http://localhost:8080/` → "Backend Visa OK"

