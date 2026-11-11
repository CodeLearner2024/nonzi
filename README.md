# RH Application

Application de gestion des ressources humaines — Spring Boot 3 + Thymeleaf + Spring Security.

---

## Prérequis

| Outil | Version minimum |
|---|---|
| Java (JDK) | 17 |
| Maven | 3.9 |
| Docker + Docker Compose | 24 / 2.x |
| Nginx | 1.18 (optionnel, proxy HTTPS) |

---

## Variables d'environnement

Copier `.env.example` en `.env` et remplir chaque valeur :

```bash
cp .env.example .env
nano .env
```

| Variable | Description | Obligatoire en prod |
|---|---|---|
| `SPRING_PROFILES_ACTIVE` | `dev` ou `prod` | Oui |
| `PORT` | Port d'écoute (défaut : 8222) | Non |
| `H2_PATH` | Chemin fichier H2 sans extension (ex : `/data/rhdb`) | Oui |
| `DB_USERNAME` | Utilisateur H2 | Oui |
| `DB_PASSWORD` | Mot de passe H2 | Oui |
| `ADMIN_USERNAME` | Nom du compte admin initial | Non (défaut : `admin`) |
| `ADMIN_EMAIL` | Email du compte admin initial | Non |
| `ADMIN_PASSWORD` | Mot de passe admin initial | **Oui** |

> ⚠️ Ne jamais commiter `.env` dans Git. Il est dans `.gitignore`.

---

## Build

```bash
# Compiler et créer le JAR
mvn clean package -DskipTests

# Le JAR est dans : target/rh-app.jar
```

---

## Lancement

### Développement (H2 fichier local, profil dev)

```bash
# Avec Maven
export ADMIN_PASSWORD=monMotDePasseDev
mvn spring-boot:run

# Ou avec le JAR
export ADMIN_PASSWORD=monMotDePasseDev
java -jar target/rh-app.jar --spring.profiles.active=dev
```

L'application démarre sur **http://localhost:8222**  
Console H2 : http://localhost:8222/h2-console (JDBC URL : `jdbc:h2:file:./data/rhdb-dev`)

### Production (Docker)

```bash
# 1. Préparer les variables
cp .env.example .env
nano .env   # Renseigner les valeurs réelles

# 2. Construire et démarrer
docker compose up -d --build

# 3. Vérifier le démarrage
docker compose logs -f app
```

---

## Vérification

```bash
# Healthcheck
curl http://localhost:8222/actuator/health
# → {"status":"UP"}

# Page de connexion
open http://localhost:8222/login
# Connexion : admin / <votre ADMIN_PASSWORD>
```

---

## Sauvegarde

```bash
# Rendre le script exécutable
chmod +x backup.sh

# Lancer une sauvegarde manuelle
./backup.sh

# Installer le cron (quotidien à 2h, rétention 30 jours)
crontab -e
# Ajouter : 0 2 * * * /opt/rh/backup.sh >> /var/log/rh-backup.log 2>&1
```

Les sauvegardes sont stockées dans `/data/backups/`.

### Restauration

```bash
# 1. Arrêter l'application
docker compose stop app

# 2a. Restaurer depuis une copie .mv.db
docker compose cp rh-backup-20240101_020000.mv.db app:/data/rhdb.mv.db

# 2b. Restaurer depuis un export SQL ZIP
#     (copier h2.jar depuis le JAR de l'application)
java -cp h2.jar org.h2.tools.RunScript \
  -url "jdbc:h2:file:/data/rhdb" \
  -user sa -password "" \
  -script rh-backup-20240101.zip \
  -options "COMPRESSION ZIP"

# 3. Redémarrer
docker compose start app
```

---

## Mise à jour de l'application

```bash
# Recompiler et redéployer (le volume rh-data est conservé)
git pull
docker compose up -d --build

# Les données persistent : Flyway applique uniquement les nouvelles migrations
```

---

## Reverse proxy HTTPS (Nginx + Let's Encrypt)

```bash
# 1. Installer certbot
sudo apt install certbot python3-certbot-nginx

# 2. Obtenir un certificat
sudo certbot --nginx -d votre-domaine.com

# 3. Copier la configuration Nginx
sudo cp nginx/rh-app.conf /etc/nginx/sites-available/rh-app
sudo ln -s /etc/nginx/sites-available/rh-app /etc/nginx/sites-enabled/

# 4. Tester et recharger
sudo nginx -t && sudo systemctl reload nginx

# 5. Pare-feu : ouvrir 80 et 443 seulement
sudo ufw allow 80/tcp
sudo ufw allow 443/tcp
sudo ufw deny 8222/tcp   # Ne pas exposer le port app directement
```

---

## Limites de H2 en production

> H2 est une base de données embarquée, adaptée aux applications mono-instance
> de taille modeste. Avant de déployer, prenez connaissance de ces contraintes :

| Limite | Impact |
|---|---|
| **Une seule instance** | Impossible de scaler horizontalement (réplicas, load balancer) |
| **Pas de haute disponibilité** | Si le serveur tombe, la base est indisponible |
| **Sauvegardes indispensables** | Pas de réplication automatique — sauvegardes quotidiennes obligatoires |
| **Concurrence limitée** | Performances acceptables jusqu'à ~50 utilisateurs simultanés |
| **Pas de pgAdmin / DBeaver natif** | Outillage DBA moins riche que PostgreSQL |

**Migration vers PostgreSQL** : grâce à Flyway, la migration est préparée.
Il suffira de :
1. Adapter `V1__init.sql` à la syntaxe PostgreSQL (types `SERIAL`, `TEXT`…)
2. Remplacer la dépendance H2 par `postgresql` dans `pom.xml`
3. Mettre à jour `application-prod.yml` avec l'URL JDBC PostgreSQL
4. Exporter les données H2 et les importer dans PostgreSQL
