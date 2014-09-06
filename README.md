# Kahvipäiväkirja

*English summary:* Kahvipäiväkirja is a coffee tasting diary web app. You can
use it to make notes on the coffees you've tasted. I'm working on it for the
University of Helsinki course on database applications. Since the course is
held in Finnish, all the documentation is in Finnish, too.

## Yleisiä linkkejä

* Työn repositorio: https://github.com/miikka/kahvipaivakirja
* Työn dokumentaatio: [`docs/dokumentaatio.pdf`][docs]

[docs]: https://github.com/miikka/kahvipaivakirja/blob/master/docs/dokumentaatio.pdf?raw=true

## Dokumentaation muokkaaminen

Dokumentaatio on kirjoitettu LaTeXilla ja sijaitsee `docs`-hakemistossa. Kun
teet muutoksia dokumentaatioon, käännä siitä uusi PDF-versio komennolla `make
docs`.


## Paikallisen testipalvelimen ajaminen

    lein ring server-headless

Palvelimen pyörii osoitteessa http://localhost:3000/
