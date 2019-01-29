FLEETMANAGER README KÄYTTÖOHJEET


1.TOIMINTAAN VAADITUT OHJELMAT/TIEDOSTOT: 
Jos ohjelma on kansiomuodossa, varmista ennen käyttöä, että fleetmanager-kansiossa on seuraavat tiedostot:
-java ohjelman bin ja src (connectionHandler,Car,CarDatabaseHandler,ServerStarter ja ServerUI) tiedostot
-CarDatatabase.db tietokantatiedosto
-kaksi jar tiedostoa, gson-2.7 ja sqlite-jdbc-3.23.1

Ohjelma vaatii toimintaan ja testaukseen myös:
-Testiohjelman advanced REST client
-mahdollisesti SQLite ohjelman


2. HUOMIOITAVAA:
Ohjelmassa on pari tunnettua ongelmaa: 
-Ohjelma ei pysty käsittelemään SQL injektiota testauksessa, älä tuhoa tietokannan sisältöä injektiotestauksilla!
-Puutteelliset (null) numeroarvot tietokantaan tallennetaan kokonaisluku arvona 0, koska javan int-tyyppi ei voi käsitellä null-arvoja
-Ohjelmalla ei ole pääsyä tietokantaan samaan aikaan, kun sitä tarkastellaan SQLite ohjelmalla. Autojen tietokannan sisällön tarkastelu
kannattaa tehdä joko ohjelman kautta tai silloin, kun ohjelma ei ole käynnissä.
-Jos ohjelman sulkee ilman, että painaa shut down server-näppäintä, server on päällä vielä tunnin (pystyy vielä käsittelemään pyyntöjä) ja
estää uudestaan käynnistetyn ohjelman toiminnan tunniksi

Toiminnnan muokattavuus:
Ohjelman ymmärtäminen sekä toiminnan ja tietokannan olioiden muokattavuus on tehty mahdollisimman helpoksi.

Toiminnan rajoitteet:
Auton rekisteri on SQL pääavain, auto-olioissa tulee aina olla jokin rekisterinumero (Tällä hetkellä mikä tahansa ei-tyhjä String olio käy rekisteriksi, asia on kuitenkin helposti muokattavissa).
Testaustarkoituksien takia API toimii vain tunnin ajan käynnistämisen jälkeen  (Muokattavuudessa tässä kyse on vain yhden numeroarvon muuttamisesta).
Kaikki auto-olion parametrit ovat joko String tai Integer olioita ja niissä ei ole mitään muita rajoitteita, kuin että licence-String ei saa olla tyhjä ja sen pitää olla uniikki kun sitä yritetään tallentaa tietokantaan,
siksi auto-oliot eivät ole välttämättä täysin realistia ja tallennettavien auto-olioiden arvoja "aitouksia" ei tarkisteta (tämä on kuitenkin helposti muutettavissa).


3. Käyttöohjeet:

Jos ohejlma on tiedostomuodossa, ensimmäiseksi tulee käynnistää java-koodilla tehty ServerStarter luokan Main-funktio, esimerkiksi eclipsen (java developer -ohjelma) avulla.
Pieni "Fleet Manager UI" ikkuna tulisi ilmestyä ruudulle. Painamalla "start server"-nappia server käynnistyy. Kun napin painalluksen jälkeen ohjelman konsoliin ilmestyy "server and connection handler online",
on ohjelma valmis API testeihin. Ohjelma on päällä noin tunnin ennen kuin se automaattisesti sammuu. Konsolista näkee ohjelman toiminnan ja sen käsittelemien pyyntöjen parametrit.

Jos ohjelma on java jar-muodossa, se käynnistyy käynnistämällä jar tiedoston. Pieni "Fleet Manager UI" ikkuna tulisi ilmestyä ruudulle.
Painamalla "start server"-nappia server käynnistyy ja viimeistään noin sekunnin jälkeen kaikki toiminnat pitäisivät olla valmiita testaukseen. 

Seuraavaksi voi käyttää testiohjelmaa API:n testaukseen.
Testiohjelmalla advanced REST client voidaan tehdä GET,POST,DELETE ja PUT http pyyntö -testejä.
Toiminta ei ole täydellisestä https protokollalla, suosittelen vain ainoastaan http protokollan käyttöä. Lisäksi esimerkiksi Postmanillä tehtyjen http pyyntöjen/kutsujen kanssa on ollut ongelmia.

Kun on valmis API testauksen kanssa, "shut down server" -nappi sammuttaa ohjelman.

API testauksen osoite on http://localhost:8083/cars

4. TESTAUSMENETELMÄT

4.1 GET-testit
näillä haetaan JSON lista tai yksittäinen JSON olio autokannasta

GET pyyntö osoitteeseen http://localhost:8083/cars varmistaa, että yhteys pystytään muodostamaan ja "server" on päällä

GET pyyntö osoitteeseen http://localhost:8083/cars/GetAllCars palauttaa JSON array-listan kaikista auto-olioista

GET pyyntö osoitteeseen http://localhost:8083/cars/?Licence=XXXX palauttaa auton, jonka rekisteri on XXXX

GET pyyntö osoitteeseen http://localhost:8083/cars/?YearMin=XXXX&YearMax=YYYY&Brand=ZZZZ&Model=AAAA palauttaa listan autoista, joiden minimi-vuosimalli on XXXX, maksimi-vuosimalli YYYY, merkki ZZZZ ja malli AAAA,
kaikki parametrit ovat valinnaisia, mutta jos yearmin ja yearmax arvoja ei spesifioida niin haku tapahtuu vuosimallien 1900 ja 2100 välilitä. Lisäksi, koska merkki ja malli oliot ovat String-olioita, täytyy niiden
olla täysin identtisiä (.equals metodi palauttaa true) olioiden String-arvojen kanssa. 

esimerkikisi:
GET http://localhost:8083/cars/?GetAllCars
GET http://localhost:8083/cars/?Licence=ADB-123
GET http://localhost:8083/cars/?YearMax=2012&Brand=Audi


4.2 POST-testit
Näillä lisätään JSON olio autosta autokantaan

POST pyyntö osoitteeseen http://localhost:8083/cars sopivalla JSON olio bodyllä (Autolla pitää olla arvo licence-kohdalla. Lisäksi tiedetyn virheen takia kaikki puuttuvat numeroarvot tallennetaan nollina.)
tallentaa auto JSON olion tietokantaan.

esimerkiksi:
POST http://localhost:8083/cars (BODY JSON:) { "brand":"Audi", "model":"A5", "licence":"GHG-642", "yearModel":2013, "inspectionDate":"23-12-2015", "engineSize":5, "enginePower":140}
POST http://localhost:8083/cars (BODY JSON:) { "brand":"Audi", "licence":"GHG-642", "yearModel":2013, "engineSize":5, "enginePower":140}


4.3 DELETE-testit
Näillä poistetaan auto tietokannasta

DELETE pyyntö osoitteeseen http://localhost:8083/cars sopivalla JSON olio bodyllä (Autolla pitää olla arvo licence-kohdalla.) poistaa auton tietokannasta, jonka licence arvo on sama tietokannan olion kanssa.

esimerkiksi:
DELETE http://localhost:8083/cars (BODY JSON:) { "brand":"Audi", "model":"A5", "licence":"GHG-642", "yearModel":2013, "inspectionDate":"23-12-2015", "engineSize":5, "enginePower":140}
DELETE http://localhost:8083/cars (BODY JSON:) { "brand":"Audi", "licence":"GHG-642", "yearModel":2013, "engineSize":5, "enginePower":140}

4.4 PUT-testit
Näillä muokataan autoa tietokannasta.
Toiminta perustuu delete ja post metodeihin.

POST pyyntö osoitteeseen http://localhost:8083/cars sopivalla JSON olio bodyllä (Autoilla pitää olla arvo licence-kohdalla. Vanha versio autosta ensin, uudempi myöhemmin)
poistaa auton tietokannasta, jonka licence arvo on sama tietokannan olion kanssa ja samalla lisää uuden auton sinne, jonka arvot on JSON olion spesifioimat.
Ainoa vaatimus auton muokkaamisessa on aiemman auton version rekisteri tulee olla sama kuin nykyisen.

esimerkiksi:
POST http://localhost:8083/cars (BODY JSON:) [{ "brand":"Audi", "licence":"GHG-642", "yearModel":2013, "inspectionDate":"01-12-2012", "engineSize":5, "enginePower":140},
{ "brand":"Audi", "model":"A5", "licence":"GHG-642", "yearModel":2013, "inspectionDate":"23-12-2015", "engineSize":5, "enginePower":140}] muokkaa audi oliota siten, että model kohta saa arvon ja tarkastuspäivämäärä muuttuu.




5. YHTEYSTIEDOT
Tarvittaessa lisätietoja ohjelman käynnistyksistä ja/tai toiminnasta saa ottamalla yhteyttä sähköpostiini mikael.remes@gmail.com 












