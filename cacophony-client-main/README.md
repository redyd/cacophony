## A. Introduction
* Ce client implémente le protocole Cacophony 1.0 pour dialoguer avec le Cacophony Node
* Il supporte la connexion TLS et embarque l'autorité attendue pour ce laboratoire
* Il permet l'utilisation d'arguments, sur la ligne de commande, remplissant automatiquement les champs `Host`, `Port`, `Login`, `Password` et la case à cocher `with SSL/TLS` (cf. figure 2 de l'énoncé):
```
python main.py node1.swilabus.xyz 5002 louis labo2026 tls
```

## B. Démarrage du client
Pour fonctionner, ce client Python nécessite l'installation de la librairie `bcrypt`. Pour ce faire, il faut utiliser la commande Python (à faire une seule fois):
```
pip install bcrypt
```

Une fois la librairie ajoutée, il est possible de démarrer le client avec la commande :
```
python main.py
```

### B.1 Mise en place d'un environnement virtuel
Si vous utilisez déjà Python pour d'autres projets, il est parfois préférable de confiner l'environnement de développement en installant les librairies directement dans cet espace. Pour ce faire, il faut utiliser `virtualenv`:
```
python -m venv .venv
.venv\Scripts\activate.bat
pip install bcrypt
python main.py
```
Il convient dans ce cas d'activer l'environnement à chaque nouveau terminal (ou invite de commandes) démarré.