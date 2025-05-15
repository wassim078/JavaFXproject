# 🌱 LiveCycle - Plateforme de Recyclage


## 📝 Description du Projet

LiveCycle est une application de recyclage développée avec JavaFX qui met en relation les vendeurs (particuliers) et les acheteurs (entreprises de recyclage). Ce projet a été développé dans le cadre d'un projet académique à [Esprit School of Engineering](https://esprit.tn/).

Le projet vise à promouvoir les Objectifs de Développement Durable (ODD) liés à la consommation responsable en:
- Réduisant les déchets par la réutilisation de produits recyclables
- Créant de nouveaux emplois dans l'économie circulaire
- Favorisant une culture de recyclage et de consommation durable

## 📋 Table des Matières

- [📥 Installation](#installation)
- [✨ Fonctionnalités](#fonctionnalités)
- [🏗️ Structure du Projet](#structure-du-projet)
- [🛠️ Technologies Utilisées](#technologies-utilisées)
- [🔌 APIs Intégrées](#apis-intégrées)
- [📦 Modules](#modules)
- [👥 Contribution](#contribution)
- [📄 License](#license)

## 📥 Installation

1. Clonez le repository:
   ```bash
   git clone https://github.com/votre-nom/livecycle.git
   cd livecycle
   ```

2. Assurez-vous d'avoir installé:
   * Java JDK 17 ou supérieur
   * Maven 3.8 ou supérieur
   * MySQL 8.0

3. Configurez la base de données:
   * Créez une base de données MySQL nommée `livecycle`
   * Configurez les identifiants dans `com.example.livecycle.utils.DatabaseConnection`

4. Compilez et exécutez le projet:
   ```bash
   mvn clean install
   mvn javafx:run
   ```

## ✨ Fonctionnalités

### 👤 Module Utilisateur
- Inscription et authentification des utilisateurs
- Authentification par reconnaissance faciale (OpenCV)
- Réinitialisation de mot de passe par email
- Gestion des profils utilisateurs

### 📢 Module Annonces et Catégories
- Publication d'annonces de produits recyclables
- Filtrage par catégories
- Recherche avancée
- Notifications par SMS (via Twilio) lors de la suppression d'annonce

### 💬 Module Forum
- Publication de posts sur le recyclage
- Partage d'informations et astuces
- Communication entre utilisateurs
- Système de commentaires et réactions

### 🚚 Module Commandes et Livraisons
- Gestion des commandes
- Suivi des livraisons
- Système de réclamations
- Paiement en ligne (via Stripe)
- Génération de factures PDF

### ♻️ Module Collecte
- Création de points de collecte
- Planification de collectes
- Visualisation des points de collecte sur une carte

### 👨‍💼 Module Administration
- Tableau de bord administrateur
- Gestion des utilisateurs
- Statistiques et rapports
- Modération du contenu

## 🏗️ Structure du Projet

L'application suit une architecture MVC (Modèle-Vue-Contrôleur):

```
livecycle/
├── src/
│   └── main/
│       ├── java/
│       │   └── com/
│       │       └── example/
│       │           └── livecycle/
│       │               ├── controllers/
│       │               │   ├── auth/
│       │               │   ├── backoffice/
│       │               │   └── frontoffice/
│       │               ├── entities/
│       │               ├── services/
│       │               └── utils/
│       └── resources/
│           └── com/
│               └── example/
│                   └── livecycle/
│                       ├── auth/
│                       ├── backoffice/
│                       ├── css/
│                       ├── frontoffice/
│                       ├── html/
│                       └── images/
└── pom.xml
```

## 🛠️ Technologies Utilisées

- **🖥️ JavaFX**: Framework GUI pour l'interface utilisateur
- **🔄 Maven**: Gestion des dépendances et build
- **💾 MySQL**: Base de données
- **🔒 JBCrypt**: Hachage des mots de passe
- **👁️ OpenCV**: Reconnaissance faciale
- **📄 iText & PDFBox**: Génération de documents PDF
- **🎨 Kordamp Ikonli**: Bibliothèque d'icônes

## 🔌 APIs Intégrées

- **📱 Twilio**: Envoi de SMS pour les notifications
- **💳 Stripe**: Traitement des paiements
- **📅 Google Calendar API**: Intégration de calendrier pour les collectes
- **📧 JavaMail**: Envoi d'emails pour la vérification et réinitialisation de mot de passe
- **🔍 OpenCV**: Authentification par reconnaissance faciale

## 📦 Modules

### 👤 Module Gestion Utilisateur (par Wassim Jmili)
Gestion des utilisateurs, authentification et autorisations.

### 📢 Module Annonces et Catégories (par Rawand Snoussi)
Système de publication et gestion des annonces de produits recyclables.

### 💬 Module Forum (par Ranim Ben Farhat)
Plateforme de discussion et partage d'informations sur le recyclage.

### 🚚 Module Commandes, Livraisons et Réclamations (par Ayoub Benhmida)
Gestion du processus de commande, livraison et service client.

### ♻️ Module Collecte (par Mouhib Souihi)
Organisation et gestion des points de collecte de déchets recyclables.

## 👥 Contribution

Ce projet a été développé par:

1. **👨‍💻 Wassim Jmili** - Module Gestion Utilisateur
2. **👨‍💻 Rawand Snoussi** - Module Annonces et Catégories
3. **👩‍💻 Ranim Ben Farhat** - Module Forum
4. **👨‍💻 Ayoub Benhmida** - Module Commandes, Livraisons et Réclamations
5. **👨‍💻 Mouhib Souihi** - Module Collecte

Pour contribuer à ce projet:
1. Fork le projet
2. Créez votre Feature Branch (`git checkout -b feature/AmazingFeature`)
3. Commit vos changements (`git commit -m 'Add some AmazingFeature'`)
4. Push vers la Branch (`git push origin feature/AmazingFeature`)
5. Ouvrez une Pull Request

## 📄 License

Ce projet est sous licence MIT - voir le fichier [LICENSE](LICENSE) pour plus de détails.

---

🏫 Projet développé à [Esprit School of Engineering](https://esprit.tn/) - 2024/2025[readme.md](https://github.com/user-attachments/files/20223966/readme.md)
 données:

Créez une base de données MySQL nommée livecycle
Configurez les identifiants dans com.example.livecycle.utils.DatabaseConnection


Compilez et exécutez le projet:
bashmvn clean install
mvn javafx:run


Fonctionnalités
Module Utilisateur

Inscription et authentification des utilisateurs
Authentification par reconnaissance faciale (OpenCV)
Réinitialisation de mot de passe par email
Gestion des profils utilisateurs

Module Annonces et Catégories

Publication d'annonces de produits recyclables
Filtrage par catégories
Recherche avancée
Notifications par SMS (via Twilio) lors de la suppression d'annonce

Module Forum

Publication de posts sur le recyclage
Partage d'informations et astuces
Communication entre utilisateurs
Système de commentaires et réactions

Module Commandes et Livraisons

Gestion des commandes
Suivi des livraisons
Système de réclamations
Paiement en ligne (via Stripe)
Génération de factures PDF

Module Collecte

Création de points de collecte
Planification de collectes
Visualisation des points de collecte sur une carte



Technologies Utilisées

JavaFX: Framework GUI pour l'interface utilisateur
Maven: Gestion des dépendances et build
MySQL: Base de données
JBCrypt: Hachage des mots de passe
OpenCV: Reconnaissance faciale
iText & PDFBox: Génération de documents PDF
Kordamp Ikonli: Bibliothèque d'icônes

APIs Intégrées

Twilio: Envoi de SMS pour les notifications
Stripe: Traitement des paiements
Google Calendar API: Intégration de calendrier pour les collectes
JavaMail: Envoi d'emails pour la vérification et réinitialisation de mot de passe
OpenCV: Authentification par reconnaissance faciale

Modules
Module Gestion Utilisateur (par Wassim Jmili)
Gestion des utilisateurs, authentification et autorisations.
Module Annonces et Catégories (par Rawand Snoussi)
Système de publication et gestion des annonces de produits recyclables.
Module Forum (par Ranim Ben Farhat)
Plateforme de discussion et partage d'informations sur le recyclage.
Module Commandes, Livraisons et Réclamations (par Ayoub Benhmida)
Gestion du processus de commande, livraison et service client.
Module Collecte (par Mouhib Souihi)
Organisation et gestion des points de collecte de déchets recyclables.
Contribution
Ce projet a été développé par:

Wassim Jmili - Module Gestion Utilisateur
Rawand Snoussi - Module Annonces et Catégories
Ranim Ben Farhat - Module Forum
Ayoub Benhmida - Module Commandes, Livraisons et Réclamations
Mouhib Souihi - Module Collecte

Pour contribuer à ce projet:

Fork le projet
Créez votre Feature Branch (git checkout -b feature/AmazingFeature)
Commit vos changements (git commit -m 'Add some AmazingFeature')
Push vers la Branch (git push origin feature/AmazingFeature)
Ouvrez une Pull Request

License
Ce projet est sous licence MIT - voir le fichier LICENSE pour plus de détails.

Projet développé à Esprit School of Engineering - 2024/2025
