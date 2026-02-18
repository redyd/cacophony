# Projet réseau 2026 - Cacophony

Membres :

- GEORGES Mattéo
- WATHELET Pauline
- BERRIAH Riham
- SOEUR Timëo

## Structure du projet
```
cacophony
├── grammar    (librairie partagée)
├── node       (Cacophony Node)
└── speaker    (Cacophony Speaker)
```

## Prérequis

- Java 21+
- Gradle (via le wrapper inclus `./gradlew`)

## Structure du projet

Le projet est organisé en trois sous-modules Gradle :

- **grammar** : librairie partagée contenant les règles de grammaires du réseau.
- **node** : application représentant un nœud du réseau.
- **speaker** : application chargée de la diffusion des messages.

## Exécuter un sous-projet

### Node
```bash
./gradlew node:run
```

### Speaker
```bash
./gradlew speaker:run
```

## Tests

Les tests unitaires sont configurés globalement avec JUnit 5. Pour les lancer :
```bash
./gradlew test          # tous les sous-projets
./gradlew node:test     # uniquement node
./gradlew speaker:test  # uniquement speaker
./gradlew grammar:test  # uniquement grammar
```

## Logging

Un système de log est mis en place avec SLF4J. Des exemples d'utilisation sont disponibles dans les classes `App` de chaque sous-projet.

## Gestion des dépendances

Les versions des dépendances sont centralisées dans `gradle/libs.versions.toml` à la racine du projet.