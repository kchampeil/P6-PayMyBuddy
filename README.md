# Pay My Buddy


## Description

Application permettant de transférer facilement de l'argent entre amis/relations.
Il est également possible de transférer de l'argent depuis ou vers son compte bancaire

## Eléments techniques

* Build automation : `Maven`
* Langage : `Java` *version 1.8*
* Framework `Spring Boot` *version 2.5.0* avec notamment les starters suivants :
    * Spring Web
    * Thymeleaf
    * Spring Security
    * Lombok
    * Spring Data JPA
    * MySQL driver
    * Hibernate validator
    * Spring Boot Test
* `Bootstrap`
* Base de données `MySQL` *version 8.0*
* Couverture de code par `JaCoCo`
* Tests unitaires avec `Surefire`

## Installation


#### Environnement de production
* Créer une base MySQL en utilisant le fichier `src/main/resources/static/sql/schema-prod.sql`
* Utiliser le script `src/main/resources/static/sql/data.sql` pour initialiser des données en base
  
_Rq : il sera alors possible de se connecter à l'application avec un des utilisateurs insérés. Par exemple :_
_email : balthazar.picsou@pmb.com / mdp : BP2021!_

#### Environnement de tests
* Créer une base MySQL en utilisant le fichier  `src/test/resources/static/sql/schema-test.sql`

## Documentation


#### Diagramme de classe
![Diagramme De Classe](https://github.com/kchampeil/P6-PayMyBuddy/blob/develop/doc/DiagrammeDeClasse.png)

#### Modèle physique de données
![Modèle physique de données](https://github.com/kchampeil/P6-PayMyBuddy/blob/develop/doc/ModelePhysique.png)
