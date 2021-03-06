# Kahvipäiväkirja

*English summary:* Kahvipäiväkirja is a coffee tasting diary web app. You can
use it to make notes on the coffees you've tasted. I made it for the University
of Helsinki course on database applications. Since the course is held in
Finnish, all the documentation is in Finnish, too.

## Yleisiä linkkejä

* Työn repositorio: https://github.com/miikka/kahvipaivakirja
* Työn dokumentaatio: [doc/dokumentaatio.pdf][docs]

Testi-instanssi users.cs.helsinki.fi-palvelimella ei ole enää toiminnassa,
sillä kurssi on ohi. Ohjeet sovelluksen ajamiseen paikallisesti löytyvät
alempana tästä tiedostosta.

[docs]: https://github.com/miikka/kahvipaivakirja/blob/master/doc/dokumentaatio.pdf?raw=true

## Käyttäjätunnukset ja salasanat

* Tavallinen käyttäjä: `Testaaja`/`kofeiini`
* Ylläpitäjä: `Yllapitaja`/`kofeiini`

## Työn aihe

Juon mielelläni hyvää kahvia ja haluaisin ymmärtää paremmin, mikä
oikeastaan tekee kahvista hyvää. Niinpä olen päättänyt alkaa pitää
päiväkirjaa maistamistani kahveista.

Sovelluksen tarkoituksena on antaa käyttäjän kirjata
maistelukokemuksia. Olennaista tietoa on, mitä kahvia
maisteltiin. Käyttäjä voi antaa kahville arvosanan ja kirjata
vapaamuotoisia huomioita. Lisäksi sovellus tarjoaa listan parhaista
kahveista ja kahvityypeistä.

Toimintoja:

* Maistelukokemuksien kirjaaminen ja muokkaaminen
* Maisteluhistorian katselu
* Top-listojen katselu
* Käyttäjän ja ylläpitäjän kirjautuminen
* Ylläpitotoiminnot

Ylläpitotoimintoihin kuuluu:

* Käyttäjien hallinointi
* Kahvilaatujen ja paahtimoiden tietojen muokkaaminen ja yhdistely

## Ohjeita sovelluskehitykseen

### Tietokanta-asetukset

Tietokanta-asetukset tallenetaan tiedostoon `~/.kahvipaivakirja.edn`
[clojure.java.jdbc:n käyttämässä muodossa][db-spec]. Esimerkiksi:

    {:classname "org.postgresql.Driver"
     :subprotocol "postgresql"
     :subname "//localhost:5432/miikka"
     :user "miikka"}

[db-spec]: http://clojure-doc.org/articles/ecosystem/java_jdbc/home.html#setting-up-a-data-source


### Paikallisen testipalvelimen ajaminen

Testipalvelinta kannattaa ajaa REPLissä:

	(use 'kahvipaivakirja.core 'ring.server.standalone)
	(def s (serve app))

	;; Kun haluat pysäyttää palvelimen:
	(.stop s)

Komentoriviltä palvelimen voi käynnistää komennolla.

    lein ring server-headless

Palvelin pyörii osoitteessa http://localhost:3000/


### Dokumentaation muokkaaminen

Dokumentaatio on kirjoitettu LaTeXilla ja sijaitsee `docs`-hakemistossa. Kun
teet muutoksia dokumentaatioon, käännä siitä uusi PDF-versio komennolla `make
docs`.


### Sovelluksen asentaminen users-palvelimelle

Sovellus asennetaan users-palvelimelle tekemällä siitä WAR-tiedosto ja
kopioimalla se `~/tomcat/webapps`-hakemistoon. Tämän voi tehdä komennolla:

    make deploy

Ensimmäistä kertaa sovellusta asennettaessa tietokanta on ensin
alustettava. Aja users-palvelimella komento `make reset-db`.
