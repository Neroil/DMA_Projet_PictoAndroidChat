# DMA_Projet_PictoAndroidChat

Le but de ce projet est de faire une application de chat local utilisant Google Nearby.
## Architecture du projet

### Structure MVVM pour les discussions

Idée pour avoir un ordre des messages constants entre utilisateurs -> implémentation d'un mutex de lamport (Optionel si on a du temps à la fin, néanmoins le mentionner si cet ordre est un problème.)

## Layouts

### Page initiale

Message d'accueil 
Champs de text pour le PSEUDO
Bouton Join
Bouton Host

### Page Join

Scan les channels disponnible en utilisant les fonctionnalités de Google Nearby et les affiche dans une liste de rectangles. Chaque élément indique le nom et le nombre de personnes connectées

### Page Host

Indique le nom du channel à créer et la taille de celui-ci (nombre de personnes max)

### Page chat

La partie haute de l'écran permet de voir les messages des autres utilisateurs de l'application. 

La partie basse est divisée en un text input et un rectangle de dessins.

L'utilisateur peut changer la taille du trait, épais ou fin. 


## Librairies dessin

Toutes les librairies ont les fonctionnalité qui nous intéresse à voir ensuite pour l'export laquelle est la plus simple.

[Drawing-Canvas](https://github.com/Miihir79/DrawingCanvas-Library) exporte les dessins en *arrayList* de *CustomPath*.

[DrawBox](https://android-arsenal.com/details/1/8292) exporte les dessins en *BitMap*.