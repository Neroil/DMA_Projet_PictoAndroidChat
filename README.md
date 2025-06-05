# DMA_Projet_PictoAndroidChat
> Guillaume Dunant, Edwin Haeffner, Arthur Junod

Le but de ce projet est de créer une application de chat local utilisant Google Nearby.
## Projet
### Page d'accueil

La page d'accueil est composée de :
- Message d'accueil
- Champ de texte pour le pseudo à utiliser dans la salle de discussion (si celui-ci est laissé vide, un nom d'utilisateur aléatoire sera généré).
- Bouton Join pour rejoindre une salle de discussion aux alentours.
- Bouton Host pour héberger une salle de discussion.

### Page salle de discussion

La partie haute de l'écran permet de voir les messages des autres utilisateurs de l'application. 

La partie basse est divisée en un champ de saisie texte pour les messages et un rectangle de dessin.

L'utilisateur peut utiliser soit un trait noir pour dessiner, soit utiliser la gomme en appuyant sur le bouton actionnable du milieu.

Il peut également effacer tout son dessin avec le bouton clear ou l'envoyer aux autres utilisateurs avec le bouton send.


### Librairies de dessin

Nous avons finalement utilisé la librairie [DrawBox](https://github.com/akshay2211/DrawBox) pour le canvas.
