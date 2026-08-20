# Quincaillerie Mvogt — Client desktop (JavaFX)

Client lourd (desktop) de gestion de stock pour la **Quincaillerie Mvogt** (Yaoundé),
destiné aux postes vendeurs et au back-office connectés en réseau local au serveur
de l'application. Il consomme l'API REST du [backend Spring Boot](../backend) et
couvre les trois rôles de l'application : Vendeur, Gestionnaire de stock et
Administrateur.

> Ce client reste dans le dépôt comme référence/ancien client de production. Le
> développement actif se concentre désormais sur le [client web Angular](../frontend-angular),
> qui vise à terme à le remplacer — voir `CLAUDE.md` à la racine du projet pour le
> détail de cette décision.

## Description

L'application permet à un vendeur d'enregistrer une vente au comptoir (recherche
produit, panier, décrément de stock, impression automatique d'un reçu 80 mm), et à
un gestionnaire/administrateur de piloter le stock (produits, catégories,
fournisseurs, approvisionnements), les comptes utilisateurs et les paramètres du
magasin depuis un back-office à onglets. C'est une application **standalone**,
packagée en exécutable natif Windows (via `jpackage`) avec sa propre JRE embarquée,
pensée pour un déploiement sur un PC/mini-serveur local sans dépendance au cloud.

## Architecture

Application MVC classique JavaFX (FXML + contrôleurs), organisée par couche :

```
client/
├── MainApp.java              # Point d'entrée JavaFX, choix de l'écran initial
├── Launcher.java             # Point d'entrée réel utilisé par jpackage (voir Technologies)
├── config/AppConfig.java     # URL de base de l'API (client.properties, externalisable)
├── session/Session.java      # Singleton : session courante (login/rôle) + ApiClient partagé
├── service/
│   ├── ApiClient.java        # Client HTTP générique (GET/POST/PUT/DELETE + JWT + JSON)
│   └── ReceiptService.java   # Génération et impression du reçu PDF (ticket de caisse)
├── model/                    # Records DTO miroir des requêtes/réponses de l'API
├── controller/                # Un contrôleur par écran/onglet (@FXML)
└── view/*.fxml                # Un FXML par écran/onglet, + styles (app.css)
```

- **Navigation** : pas de framework de routing — chaque écran est chargé via
  `FXMLLoader` puis substitué dans la `Scene` de la `Stage` principale
  (`login.fxml` → `vente.fxml` **ou** `backoffice.fxml` selon le rôle). Ce même
  mécanisme permet la navigation croisée Point de vente ⇄ Back-office pour les
  rôles ADMIN/GESTIONNAIRE.
- **Back-office à onglets** : `backoffice.fxml` est un `BorderPane` avec un
  `TabPane` central ; chaque onglet est un `fx:include` vers son propre
  FXML/contrôleur (`*_tab.fxml` / `*TabController`), gardant chaque écran
  indépendant et testable. Les onglets Catégories, Utilisateurs, Rapports et
  Paramètres sont retirés dynamiquement du `TabPane` si le rôle n'est pas ADMIN.
- **Session unique et partagée** : `Session` est un singleton qui porte l'unique
  `ApiClient` (et donc le token JWT courant) utilisé par tous les contrôleurs —
  cohérent avec un poste vendeur mono-utilisateur.
- **Pas de couche ORM/base de données côté client** : toute la donnée transite par
  l'API REST du backend ; le client ne fait que sérialiser/désérialiser du JSON.

## Fonctionnalités

- **Connexion** : écran de login avec affichage/masquage du mot de passe, case
  « Se souvenir de moi » (persistance de session entre deux lancements) et lien
  « Mot de passe oublié ? » (message invitant à contacter un administrateur — pas
  de flux de réinitialisation automatisé).
- **Point de vente** (Vendeur, et Administrateur via navigation) : recherche produit,
  panier avec vérification du stock disponible, nom client optionnel, validation de
  la vente puis génération et **impression automatique** du reçu (ticket 80 mm).
- **Back-office** (Gestionnaire/Administrateur) :
  - Tableau de bord avec actualisation automatique (toutes les 30 s) des indicateurs
    de stock.
  - Produits & alertes de stock (bascule vers les produits sous seuil).
  - Approvisionnements (saisie multi-lignes fournisseur/quantité/prix d'achat).
  - Fournisseurs.
  - Catégories, Utilisateurs et Rapports (ventes globales, top produits) —
    réservés à l'Administrateur.
  - Paramètres du magasin (nom, domaine d'activité, ville, téléphone, email
    affichés sur le reçu) — réservés à l'Administrateur.
- **Gestion de compte** : changement de mot de passe en self-service (modale
  accessible depuis les trois rôles), désactivation de compte côté Administrateur.
- **Reprise de session** : au lancement, si une session a été mémorisée, elle est
  revalidée par un appel léger à l'API avant d'ouvrir directement le bon écran ;
  en cas d'échec (token expiré, serveur injoignable), retour silencieux à l'écran
  de connexion.

## Technologies

| Domaine              | Choix                                                              |
|-----------------------|--------------------------------------------------------------------|
| Langage / runtime      | Java 21 (compilé en `release 21`)                                  |
| UI                     | JavaFX 21.0.4 (classifier `win`) + FXML                            |
| Thème                  | AtlantaFX 2.1.0 (`PrimerLight`), remplace le rendu Modena par défaut |
| Client HTTP            | `java.net.http.HttpClient` (JDK, sans dépendance externe)           |
| Sérialisation JSON     | Jackson Databind 2.17.2 + `jackson-datatype-jsr310` (support `Instant`) |
| Génération PDF          | OpenPDF 2.2.2 (`com.lowagie.text`)                                  |
| Impression              | `java.awt.Desktop.print(File)` (imprimante par défaut Windows, sans dialogue) |
| Persistance session      | `java.util.prefs.Preferences` (« Se souvenir de moi »)              |
| Build                   | Maven, `javafx-maven-plugin` 0.0.8                                  |
| Packaging natif          | `jpackage` (via `exec-maven-plugin`) → app-image Windows autonome avec JRE embarquée |
| Tests                    | JUnit 5 + TestFX 4.0.18 (smoke tests structurels FXML)              |

## Sécurité

- **Authentification par JWT** : le token obtenu via `POST /api/auth/login` est
  conservé en mémoire dans `Session`/`ApiClient` et automatiquement attaché à
  chaque requête (`Authorization: Bearer <token>`) par `ApiClient.requestBuilder()`.
- **« Se souvenir de moi »** : si activé, le token (et le login/rôle) est en plus
  écrit **en clair** dans les préférences utilisateur Windows (`Preferences`,
  registre `HKEY_CURRENT_USER`) — même modèle de confiance que le `localStorage`
  non chiffré du client Angular. Décocher la case (ou se déconnecter) efface
  immédiatement cette copie persistée.
- **Autorisation côté client = confort, pas rempart** : les boutons/onglets
  réservés à un rôle (Catégories, Utilisateurs, Rapports, Paramètres, suppression
  de produit, accès au Point de vente pour GESTIONNAIRE…) sont masqués/désactivés
  côté UI, mais l'application **fait confiance au backend** pour l'application
  réelle des règles (`@PreAuthorize`/`hasRole` côté Spring Security) — un appel
  API direct sans le bon rôle est de toute façon rejeté en 403 par le serveur.
- **Aucune invalidation de token** : changer son mot de passe ne révoque pas les
  JWT déjà émis (pas de blacklist) — un token volé reste valide jusqu'à son
  expiration naturelle (`JWT_EXPIRATION_MS` côté backend).
- **Mot de passe** : saisi via `PasswordField` (masqué par défaut, bascule en
  clair possible manuellement), jamais journalisé ni renvoyé par l'API ; les
  erreurs d'authentification affichent un message générique (« Identifiant ou mot
  de passe incorrect ») sans exposer le détail de la réponse serveur.
- **Transport** : l'URL de l'API (`client.properties`, `api.baseUrl`) pointe par
  défaut sur `http://localhost:8082/api` — en HTTP simple, cohérent avec un
  déploiement réseau local (v1) plutôt qu'exposé sur Internet ; le HTTPS n'est pas
  géré par ce client s'il fallait exposer l'API plus largement.

## Backend

Ce client ne fait aucun accès direct à la base de données : il consomme
exclusivement l'API REST du [backend Spring Boot](../backend) (Spring Boot 4.1,
Spring Security, MySQL). L'URL de base est externalisable sans recompiler via
`src/main/resources/client.properties` (`api.baseUrl=http://<ip-serveur>:8082/api`),
ce qui permet de pointer un poste vendeur vers l'IP du serveur local du magasin.

Principaux endpoints consommés :

| Domaine            | Endpoints                                                          |
|---------------------|----------------------------------------------------------------------|
| Authentification      | `POST /api/auth/login`                                              |
| Compte                | `PUT /api/compte/mot-de-passe`                                       |
| Produits               | `GET/POST/PUT/DELETE /api/produits`, `GET /api/produits/alertes`     |
| Catégories             | `GET/POST/PUT/DELETE /api/categories`                                |
| Fournisseurs           | `GET/POST/PUT/DELETE /api/fournisseurs`                              |
| Ventes                 | `GET/POST /api/ventes`                                               |
| Approvisionnements     | `GET/POST /api/approvisionnements`, `GET /api/approvisionnements/{id}`, `GET /api/approvisionnements/fournisseur/{id}` |
| Utilisateurs            | `GET/POST/PUT/DELETE /api/utilisateurs`                              |
| Paramètres du magasin  | `GET /api/parametres`, `PUT /api/parametres` (ADMIN)                 |

Toutes les requêtes (hors login) exigent le header `Authorization: Bearer <JWT>` ;
le backend applique le contrôle d'accès par rôle (`hasRole`/`hasAnyRole`) et
retourne des codes HTTP normalisés (`404` entité introuvable, `409` conflit métier
— ex. stock insuffisant, login/email en double —, `400` validation).

## Lancer le projet

```powershell
$env:JAVA_HOME = 'C:\Program Files\Java\jdk-25.0.2'
$env:Path = "$env:JAVA_HOME\bin;$env:Path"

mvn javafx:run        # lance l'application en mode développement
mvn -o test            # exécute les tests JUnit/TestFX
mvn -o clean package   # génère l'app-image jpackage dans target/dist/
```

Le backend doit être démarré au préalable (par défaut sur `http://localhost:8082`,
avec une base MySQL/MariaDB accessible — voir le README du backend).
