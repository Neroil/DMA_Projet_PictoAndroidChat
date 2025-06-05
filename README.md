# DMA_Projet_PictoAndroidChat
> Guillaume Dunant, Edwin Haeffner, Arthur Junod

Le but de ce projet est de faire une application de chat local utilisant Google Nearby.
## Projet
### Page d'accueil

La page d'accueil sera composée :
- Message d'accueil
- Champs de text pour le pseudo à utiliser pour la salle de discussion (si celui-ci est laissé vide un nom d'utilisateur aléatoire nous sera donné).
- Bouton Join pour rejoindre une salle de discussion aux alentour.
- Bouton Host pour hébérger une salle de discussion.

### Page salle de discussion

La partie haute de l'écran permet de voir les messages des autres utilisateurs de l'application. 

La partie basse est divisée en un text input et un rectangle de dessins.

L'utilisateur peut utiliser soit un trait noir pour dessiner soit utiliser la gomme en appuyant sur le bouton actionnable du milieu.

Il peut également effacer tout son dessin avec le bouton clear ou l'envoyer aux autres utilisateurs avec le bouton send.


### Librairies dessin

Nous avon finalement utiliser la librairie [DrawBox](https://github.com/akshay2211/DrawBox) pour le canva.
