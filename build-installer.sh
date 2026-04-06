#!/usr/bin/env bash

# ============================================
# NoteTaker - Cross‑Platform Installer Builder
# ============================================

set -e

# ------------------------------
# Configuration
# ------------------------------
APP_NAME="NoteTaker"
APP_VERSION="1.0.0"
VENDOR="VotreNom"
DESCRIPTION="Application de prise de notes Markdown"
MAIN_CLASS="com.exemple.notetaker.App"

# Paths (adjust if needed)
ICON_PNG="src/main/resources/com/exemple/notetaker/icon.png"
ICON_ICO="src/main/resources/com/exemple/notetaker/icon.ico"
JMODS_PATH_WIN="C:/javafx-jmods-21"
JMODS_PATH_MAC="$HOME/javafx-jmods-21"
JMODS_PATH_LINUX="$HOME/javafx-jmods-21"

# Detect OS
OS="$(uname -s)"
case "$OS" in
    Linux*)     PLATFORM="linux" ;;
    Darwin*)    PLATFORM="mac" ;;
    MINGW*|CYGWIN*|MSYS*) PLATFORM="win" ;;
    *)          error "Unsupported OS: $OS" ;;
esac

# Colors
RED='\033[0;31m'
GREEN='\033[0;32m'
CYAN='\033[0;36m'
NC='\033[0m'

step() { echo -e "\n${CYAN}🔹 $1${NC}"; }
success() { echo -e "${GREEN}✅ $1${NC}"; }
error() { echo -e "${RED}❌ $1${NC}"; exit 1; }

# ------------------------------
# Prerequisites (platform‑specific)
# ------------------------------
step "Checking prerequisites..."

command -v java >/dev/null || error "Java missing"
command -v mvn >/dev/null || error "Maven missing"
command -v jpackage >/dev/null || error "jpackage missing"

if [ "$PLATFORM" = "win" ]; then
    command -v candle.exe >/dev/null || error "WiX v3.11 missing (candle.exe not found)"
    [ -d "$JMODS_PATH_WIN" ] || error "JavaFX JMODS not found at $JMODS_PATH_WIN"
else
    if [ "$PLATFORM" = "mac" ]; then
        [ -d "$JMODS_PATH_MAC" ] || error "JavaFX JMODS not found at $JMODS_PATH_MAC"
    else
        [ -d "$JMODS_PATH_LINUX" ] || error "JavaFX JMODS not found at $JMODS_PATH_LINUX"
    fi
fi

success "All prerequisites OK"

# ------------------------------
# Build fat JAR
# ------------------------------
step "Building fat JAR..."
rm -rf target 2>/dev/null || true
mvn clean package || mvn package || error "Maven build failed"
[ -f "target/notetaker-${APP_VERSION}.jar" ] || error "JAR not created"
success "JAR ready"

# ------------------------------
# Clean previous artifacts
# ------------------------------
rm -rf "$APP_NAME" 2>/dev/null || true

# ------------------------------
# Platform‑specific installer creation
# ------------------------------
step "Creating installer for $PLATFORM..."

# Common modules required by JavaFX + Ikonli (java.logging is critical)
COMMON_MODULES="java.logging,javafx.controls,javafx.fxml,javafx.web,javafx.base,javafx.graphics"

case "$PLATFORM" in
    win)
        OUTPUT="${APP_NAME}-${APP_VERSION}.msi"
        rm -f "$OUTPUT"
        jpackage --type msi \
            --input target \
            --name "$APP_NAME" \
            --app-version "$APP_VERSION" \
            --main-jar "notetaker-${APP_VERSION}.jar" \
            --main-class "$MAIN_CLASS" \
            --module-path "$JMODS_PATH_WIN" \
            --add-modules "$COMMON_MODULES" \
            --icon "$ICON_ICO" \
            --vendor "$VENDOR" \
            --description "$DESCRIPTION" \
            --win-menu --win-shortcut \
            --verbose
        ;;
    mac)
        OUTPUT="${APP_NAME}-${APP_VERSION}.dmg"
        rm -f "$OUTPUT"
        jpackage --type dmg \
            --input target \
            --name "$APP_NAME" \
            --app-version "$APP_VERSION" \
            --main-jar "notetaker-${APP_VERSION}.jar" \
            --main-class "$MAIN_CLASS" \
            --module-path "$JMODS_PATH_MAC" \
            --add-modules "$COMMON_MODULES" \
            --vendor "$VENDOR" \
            --description "$DESCRIPTION" \
            --verbose
        ;;
    linux)
        OUTPUT="${APP_NAME}-${APP_VERSION}.deb"
        rm -f "$OUTPUT"
        jpackage --type deb \
            --input target \
            --name "$APP_NAME" \
            --app-version "$APP_VERSION" \
            --main-jar "notetaker-${APP_VERSION}.jar" \
            --main-class "$MAIN_CLASS" \
            --module-path "$JMODS_PATH_LINUX" \
            --add-modules "$COMMON_MODULES" \
            --icon "$ICON_PNG" \
            --vendor "$VENDOR" \
            --description "$DESCRIPTION" \
            --verbose
        ;;
esac

if [ -f "$OUTPUT" ]; then
    success "Installer created: $OUTPUT"
else
    error "Installer creation failed"
fi

step "Build complete!"
echo "👉 Ready to distribute: $OUTPUT"