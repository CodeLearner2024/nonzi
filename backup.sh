#!/usr/bin/env bash
# =============================================================================
# backup.sh — Sauvegarde de la base H2 de l'application RH
#
# UTILISATION :
#   chmod +x backup.sh
#   ./backup.sh
#
# CRON (quotidien à 2h du matin, rétention 30 jours) :
#   0 2 * * * /opt/rh/backup.sh >> /var/log/rh-backup.log 2>&1
# =============================================================================

set -euo pipefail

# ── Configuration ─────────────────────────────────────────────────────────────
BACKUP_DIR="${BACKUP_DIR:-/data/backups}"
H2_PATH="${H2_PATH:-/data/rhdb}"          # Sans extension
DB_USERNAME="${DB_USERNAME:-sa}"
DB_PASSWORD="${DB_PASSWORD:-}"
RETENTION_DAYS="${RETENTION_DAYS:-30}"
APP_URL="${APP_URL:-http://localhost:8222}"
DATE=$(date +%Y%m%d_%H%M%S)
BACKUP_FILE="${BACKUP_DIR}/rh-backup-${DATE}.zip"

mkdir -p "${BACKUP_DIR}"

echo "[$(date)] Démarrage de la sauvegarde..."

# ── Vérifier si l'application est arrêtée (méthode 1 — préférée) ─────────────
# Si l'app est arrêtée, copier directement le fichier .mv.db
if ! curl -sf "${APP_URL}/actuator/health" > /dev/null 2>&1; then
    echo "[$(date)] Application arrêtée — copie directe du fichier H2"
    cp "${H2_PATH}.mv.db" "${BACKUP_DIR}/rh-backup-${DATE}.mv.db"
    echo "[$(date)] Sauvegarde créée : rh-backup-${DATE}.mv.db"
else
    # ── Méthode 2 : SCRIPT SQL via H2 (application en cours d'exécution) ──────
    # H2 exporte le schéma + données dans un ZIP SQL
    echo "[$(date)] Application en cours — export SQL via H2..."
    java -cp /data/h2.jar org.h2.tools.Script \
        -url "jdbc:h2:file:${H2_PATH};AUTO_SERVER=TRUE" \
        -user "${DB_USERNAME}" \
        -password "${DB_PASSWORD}" \
        -script "${BACKUP_FILE}" \
        -options "COMPRESSION ZIP"
    echo "[$(date)] Sauvegarde créée : ${BACKUP_FILE}"
fi

# ── Nettoyage des anciennes sauvegardes ───────────────────────────────────────
echo "[$(date)] Suppression des sauvegardes de plus de ${RETENTION_DAYS} jours..."
find "${BACKUP_DIR}" -name "rh-backup-*" -mtime "+${RETENTION_DAYS}" -delete

echo "[$(date)] Sauvegarde terminée avec succès."

# =============================================================================
# RESTAURATION (procédure manuelle)
# =============================================================================
# 1. Arrêter l'application :
#      docker compose stop app
#
# 2a. Restaurer depuis une copie .mv.db :
#      cp rh-backup-YYYYMMDD_HHMMSS.mv.db /data/rhdb.mv.db
#
# 2b. Restaurer depuis un export SQL ZIP :
#      java -cp h2.jar org.h2.tools.RunScript \
#        -url "jdbc:h2:file:/data/rhdb" \
#        -user sa -password "" \
#        -script rh-backup-YYYYMMDD.zip \
#        -options "COMPRESSION ZIP"
#
# 3. Redémarrer l'application :
#      docker compose start app
# =============================================================================
