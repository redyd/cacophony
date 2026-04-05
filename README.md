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
./gradlew :node:run --args="<config> <users> <tags>"
```

Sans argument, le node utilise les fichiers par défaut :

```bash
./gradlew :node:run
```

Les trois arguments correspondent à :

- `<config>` : fichier de configuration du node
- `<users>` : fichier JSON des utilisateurs
- `<tags>` : fichier JSON des tags

Exemple avec des chemins explicites :

```bash
./gradlew :node:run --args="node/src/main/resources/node-config.json node/src/main/resources/data/user.json node/src/main/resources/data/tag.json"
```

Exemple avec des noms courts résolus automatiquement :

```bash
./gradlew :node:run --args="node-config2 user2 tag2"
```

Exemple avec des arguments nommés, dans n'importe quel ordre :

```bash
./gradlew :node:run --args="users=user2 tags=tag2 config=node-config2"
```

Règles de résolution actuelles pour le node :

- soit aucun argument n'est fourni, et le node utilise `node-config.json`, `data/user.json` et `data/tag.json`.
- soit les trois arguments `config`, `users` et `tags` sont fournis ensemble.
- les arguments peuvent être positionnels, dans l'ordre `<config> <users> <tags>`, ou nommés avec `config=...`, `users=...`, `tags=...` dans n'importe quel ordre.
- `config` : accepte un chemin existant, un nom de fichier présent dans `node/src/main/resources`, ou ce même nom sans suffixe `.json`.
- `users` et `tags` : acceptent un chemin existant, un nom de fichier présent dans `node/src/main/resources/data`, ou ce même nom sans suffixe `.json`.
- si un seul argument ou un sous-ensemble des trois est fourni, le lancement est rejeté.
- il ne faut pas mélanger arguments positionnels et arguments nommés.

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

Un système de log est mis en place avec SLF4J. Retrouvez les détails des logs dans le dossier resources/logs de chaque
sous projets.

## Gestion des dépendances

Les versions des dépendances sont centralisées dans `gradle/libs.versions.toml` à la racine du projet.
