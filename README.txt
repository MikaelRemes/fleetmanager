FLEETMANAGER README K�YTT�OHJEET


1.TOIMINTAAN VAADITUT OHJELMAT/TIEDOSTOT: 
Jos ohjelma on kansiomuodossa, varmista ennen k�ytt��, ett� fleetmanager-kansiossa on seuraavat tiedostot:
-java ohjelman bin ja src (connectionHandler,Car,CarDatabaseHandler,ServerStarter ja ServerUI) tiedostot
-CarDatatabase.db tietokantatiedosto
-kaksi jar tiedostoa, gson-2.7 ja sqlite-jdbc-3.23.1

Ohjelma vaatii toimintaan ja testaukseen my�s:
-Testiohjelman advanced REST client
-mahdollisesti SQLite ohjelman


2. HUOMIOITAVAA:
Ohjelmassa on pari tunnettua ongelmaa: 
-Ohjelma ei pysty k�sittelem��n SQL injektiota testauksessa, �l� tuhoa tietokannan sis�lt�� injektiotestauksilla!
-Puutteelliset (null) numeroarvot tietokantaan tallennetaan kokonaisluku arvona 0, koska javan int-tyyppi ei voi k�sitell� null-arvoja
-Ohjelmalla ei ole p��sy� tietokantaan samaan aikaan, kun sit� tarkastellaan SQLite ohjelmalla. Autojen tietokannan sis�ll�n tarkastelu
kannattaa tehd� joko ohjelman kautta tai silloin, kun ohjelma ei ole k�ynniss�.
-Jos ohjelman sulkee ilman, ett� painaa shut down server-n�pp�int�, server on p��ll� viel� tunnin (pystyy viel� k�sittelem��n pyynt�j�) ja
est�� uudestaan k�ynnistetyn ohjelman toiminnan tunniksi

Toiminnnan muokattavuus:
Ohjelman ymm�rt�minen sek� toiminnan ja tietokannan olioiden muokattavuus on tehty mahdollisimman helpoksi.

Toiminnan rajoitteet:
Auton rekisteri on SQL p��avain, auto-olioissa tulee aina olla jokin rekisterinumero (T�ll� hetkell� mik� tahansa ei-tyhj� String olio k�y rekisteriksi, asia on kuitenkin helposti muokattavissa).
Testaustarkoituksien takia API toimii vain tunnin ajan k�ynnist�misen j�lkeen  (Muokattavuudessa t�ss� kyse on vain yhden numeroarvon muuttamisesta).
Kaikki auto-olion parametrit ovat joko String tai Integer olioita ja niiss� ei ole mit��n muita rajoitteita, kuin ett� licence-String ei saa olla tyhj� ja sen pit�� olla uniikki kun sit� yritet��n tallentaa tietokantaan,
siksi auto-oliot eiv�t ole v�ltt�m�tt� t�ysin realistia ja tallennettavien auto-olioiden arvoja "aitouksia" ei tarkisteta (t�m� on kuitenkin helposti muutettavissa).


3. K�ytt�ohjeet:

Jos ohejlma on tiedostomuodossa, ensimm�iseksi tulee k�ynnist�� java-koodilla tehty ServerStarter luokan Main-funktio, esimerkiksi eclipsen (java developer -ohjelma) avulla.
Pieni "Fleet Manager UI" ikkuna tulisi ilmesty� ruudulle. Painamalla "start server"-nappia server k�ynnistyy. Kun napin painalluksen j�lkeen ohjelman konsoliin ilmestyy "server and connection handler online",
on ohjelma valmis API testeihin. Ohjelma on p��ll� noin tunnin ennen kuin se automaattisesti sammuu. Konsolista n�kee ohjelman toiminnan ja sen k�sittelemien pyynt�jen parametrit.

Jos ohjelma on java jar-muodossa, se k�ynnistyy k�ynnist�m�ll� jar tiedoston. Pieni "Fleet Manager UI" ikkuna tulisi ilmesty� ruudulle.
Painamalla "start server"-nappia server k�ynnistyy ja viimeist��n noin sekunnin j�lkeen kaikki toiminnat pit�isiv�t olla valmiita testaukseen. 

Seuraavaksi voi k�ytt�� testiohjelmaa API:n testaukseen.
Testiohjelmalla advanced REST client voidaan tehd� GET,POST,DELETE ja PUT http pyynt� -testej�.
Toiminta ei ole t�ydellisest� https protokollalla, suosittelen vain ainoastaan http protokollan k�ytt��. Lis�ksi esimerkiksi Postmanill� tehtyjen http pyynt�jen/kutsujen kanssa on ollut ongelmia.

Kun on valmis API testauksen kanssa, "shut down server" -nappi sammuttaa ohjelman.

API testauksen osoite on http://localhost:8083/cars

4. TESTAUSMENETELM�T

4.1 GET-testit
n�ill� haetaan JSON lista tai yksitt�inen JSON olio autokannasta

GET pyynt� osoitteeseen http://localhost:8083/cars varmistaa, ett� yhteys pystyt��n muodostamaan ja "server" on p��ll�

GET pyynt� osoitteeseen http://localhost:8083/cars/GetAllCars palauttaa JSON array-listan kaikista auto-olioista

GET pyynt� osoitteeseen http://localhost:8083/cars/?Licence=XXXX palauttaa auton, jonka rekisteri on XXXX

GET pyynt� osoitteeseen http://localhost:8083/cars/?YearMin=XXXX&YearMax=YYYY&Brand=ZZZZ&Model=AAAA palauttaa listan autoista, joiden minimi-vuosimalli on XXXX, maksimi-vuosimalli YYYY, merkki ZZZZ ja malli AAAA,
kaikki parametrit ovat valinnaisia, mutta jos yearmin ja yearmax arvoja ei spesifioida niin haku tapahtuu vuosimallien 1900 ja 2100 v�lilit�. Lis�ksi, koska merkki ja malli oliot ovat String-olioita, t�ytyy niiden
olla t�ysin identtisi� (.equals metodi palauttaa true) olioiden String-arvojen kanssa. 

esimerkikisi:
GET http://localhost:8083/cars/?GetAllCars
GET http://localhost:8083/cars/?Licence=ADB-123
GET http://localhost:8083/cars/?YearMax=2012&Brand=Audi


4.2 POST-testit
N�ill� lis�t��n JSON olio autosta autokantaan

POST pyynt� osoitteeseen http://localhost:8083/cars sopivalla JSON olio bodyll� (Autolla pit�� olla arvo licence-kohdalla. Lis�ksi tiedetyn virheen takia kaikki puuttuvat numeroarvot tallennetaan nollina.)
tallentaa auto JSON olion tietokantaan.

esimerkiksi:
POST http://localhost:8083/cars (BODY JSON:) { "brand":"Audi", "model":"A5", "licence":"GHG-642", "yearModel":2013, "inspectionDate":"23-12-2015", "engineSize":5, "enginePower":140}
POST http://localhost:8083/cars (BODY JSON:) { "brand":"Audi", "licence":"GHG-642", "yearModel":2013, "engineSize":5, "enginePower":140}


4.3 DELETE-testit
N�ill� poistetaan auto tietokannasta

DELETE pyynt� osoitteeseen http://localhost:8083/cars sopivalla JSON olio bodyll� (Autolla pit�� olla arvo licence-kohdalla.) poistaa auton tietokannasta, jonka licence arvo on sama tietokannan olion kanssa.

esimerkiksi:
DELETE http://localhost:8083/cars (BODY JSON:) { "brand":"Audi", "model":"A5", "licence":"GHG-642", "yearModel":2013, "inspectionDate":"23-12-2015", "engineSize":5, "enginePower":140}
DELETE http://localhost:8083/cars (BODY JSON:) { "brand":"Audi", "licence":"GHG-642", "yearModel":2013, "engineSize":5, "enginePower":140}

4.4 PUT-testit
N�ill� muokataan autoa tietokannasta.
Toiminta perustuu delete ja post metodeihin.

POST pyynt� osoitteeseen http://localhost:8083/cars sopivalla JSON olio bodyll� (Autoilla pit�� olla arvo licence-kohdalla. Vanha versio autosta ensin, uudempi my�hemmin)
poistaa auton tietokannasta, jonka licence arvo on sama tietokannan olion kanssa ja samalla lis�� uuden auton sinne, jonka arvot on JSON olion spesifioimat.
Ainoa vaatimus auton muokkaamisessa on aiemman auton version rekisteri tulee olla sama kuin nykyisen.

esimerkiksi:
POST http://localhost:8083/cars (BODY JSON:) [{ "brand":"Audi", "licence":"GHG-642", "yearModel":2013, "inspectionDate":"01-12-2012", "engineSize":5, "enginePower":140},
{ "brand":"Audi", "model":"A5", "licence":"GHG-642", "yearModel":2013, "inspectionDate":"23-12-2015", "engineSize":5, "enginePower":140}] muokkaa audi oliota siten, ett� model kohta saa arvon ja tarkastusp�iv�m��r� muuttuu.




5. YHTEYSTIEDOT
Tarvittaessa lis�tietoja ohjelman k�ynnistyksist� ja/tai toiminnasta saa ottamalla yhteytt� s�hk�postiini mikael.remes@gmail.com 












