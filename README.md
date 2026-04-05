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

Sans argument, le node utilise les fichiers par défaut (dans /node/src/main/resources/: node-config.json,
/data/user.json & /data/tag.json)

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

## Données existantes

Les fichiers JSON sous `node/src/main/resources/data` ont été réduits à un exemple minimal, cohérent sur les 3 nœuds :

- `user.json` / `tag.json` pour `node1.swilabus.xyz`
- `user2.json` / `tag2.json` pour `node2.swilabus.xyz`
- `user3.json` / `tag3.json` pour `node3.swilabus.xyz`

Les mots de passes des users sont tous `labo2026`

L'état d'exemple fourni est le suivant :

- `bobby@node1.swilabus.xyz` existe sur le node 1.
- `louis@node2.swilabus.xyz` existe sur le node 2 et suit `bobby@node1.swilabus.xyz`.
- `louise@node3.swilabus.xyz` existe sur le node 3 et est abonnée au tag `#cacophony@node1.swilabus.xyz`.
- `bobby@node1.swilabus.xyz` est lui aussi abonné à `#cacophony@node1.swilabus.xyz`, ce qui permet à ses messages contenant ce tag d'être relayés.

## Exemple d'utilisation

Lancer d'abord le speaker :

```bash
./gradlew :speaker:run
```

Puis lancer les trois nœuds dans trois terminaux :

```bash
./gradlew :node:run
./gradlew :node:run --args="node-config2 user2 tag2"
./gradlew :node:run --args="node-config3 user3 tag3"
```

Avec les données d'exemple :

1. `bobby` se connecte au node 1.
2. `louis` se connecte au node 2.
3. `louise` se connecte au node 3.
4. `bobby` envoie `SEND Salut tout le monde #cacophony`

Résultat attendu :

- `louis` reçoit le message sur le node 2 grâce à son abonnement utilisateur à `bobby@node1.swilabus.xyz`.
- `louise` reçoit le message sur le node 3 grâce à son abonnement au tag `#cacophony@node1.swilabus.xyz`.

## Automatisation

Le dossier `runner/` contient un orchestrateur Python qui démarre automatiquement le speaker, les 3 nodes et des clients de test.

Prérequis pour les scénarios TLS :

- Python 3.10+
- `node1.swilabus.xyz`, `node2.swilabus.xyz` et `node3.swilabus.xyz` doivent résoudre localement, par exemple vers `127.0.0.1`.
- le module Python `bcrypt` doit être installé.
- Java / Gradle doivent être disponibles pour construire `speaker` et `node`.
- Les données `user*.json` et `tag*.json` sont générées par le runner selon le scénario ; elles ne sont pas relues telles quelles depuis le repo.

Commandes principales :

```bash
python3 runner/run.py
python3 runner/run.py --scenario runner/scenarios/tag_edge_cases.json
python3 runner/run.py --skip-build
python3 runner/run.py --keep-runtime
python3 runner/run.py --mode gui-demo --action-delay 1.5 --scenario runner/scenarios/gui_demo.json
```

Notes :

- Le démarrage n'est pas instantané : les nodes annoncent leur présence au speaker via une horloge interne de 30 secondes.
- Les logs d'exécution sont conservés sous `runner/.runtime/<timestamp>/logs/`.
- En mode `gui-demo`, les fenêtres clientes restent ouvertes après la séquence automatisée ; utiliser `Ctrl+C` dans le terminal pour arrêter toute la démo.
- `--action-delay <secondes>` ajoute une pause après chaque action non `sleep`, utile pour suivre visuellement le flux.
- Si le TLS ne peut pas être établi à cause d'une mauvaise résolution DNS/hosts ou d'un problème de certificat, une erreur explicite est affichée.
