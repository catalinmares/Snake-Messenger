# Snake-Messenger
The Android version of the Snake Messenger application

**1. INTRODUCERE**

**1.1. Scopul documentului**

Acest document își propune să descrie într-un mod transparent și ușor de urmărit structura generală a unei aplicații de chat, un sistem (produs) software modelat după specificațiile comerciale (cerințele părților interesate).
Acesta are rolul de a expune cerințele funcționale ale aplicației care va fi dezvoltată, incluzând prezentarea unui set de cazuri de utilizare care descriu interacțiunea dintre utilizatori și software.


**1.2. Domeniul/contextul de utilizare al sistemului**

Software-ul prezentat face parte din categoria de aplicații de comunicare ce facilitează interacțiunea între persoane din mediul online.



**2. DESCRIERE GENERALĂ**

**2.1. Scurtă descriere a sistemului**

Snake Messenger este o aplicație de chat proiectată cu scopul de a ușura interacțiunea dintre utilizatorii din mediul online. Aceasta oferă o comunicație în timp real între clienți, oferind suport multimedia pentru mesaje. De asemenea, aplicația va fi disponibilă atât pe dispozitive mobile care rulează Android, cât și sub forma unei aplicații Web, ambele platforme dispunând de aceleași funcționalități.


**2.2. Motivație**

Într-un secol al tehnologizării, tot mai multe procese tind să fie digitalizate sau parțial automatizate prin intermediul ingineriei software.
Sistemul propus anterior este conceput pentru a răspunde nevoilor utilizatorilor care doresc sa folosească Internetul ca mijloc de comunicare.


**2.3. Produse similare**

Piața de desfacere pentru această categorie de produse software este în plină dezvoltare, bucurându-se de o cerere mare, oferind posibilitatea de inovație. Printre cele mai utilizate aplicații de tip messaging, enumerăm: Slack, Facebook Messenger, WhatsApp.
Slack este un sistem ce se mulează foarte bine pe latura de business, oferind un spațiu de lucru prin integrarea cu servicii de dezvoltare software, spre exemplu automatizarea deployment-urilor.
WhatsApp și Facebook Messenger au un public țintă mai larg, axându-se pe
interconectarea persoanele din locații geografice diferite, fără costurile aferente convorbirilor internaționale.


**2.4. Riscurile proiectului**

La nivel de competiție, Snake Messenger se confruntă cu titanii comunicației de tip Instant Messaging, precum Facebook Messenger și WhatsApp Messenger. De asemenea, o altă competiție o reprezintă alte aplicații ale căror funcționalități includ schimbul de mesaje, precum Snapchat sau Slack.
	Deși aplicația reprezintă o intrare nouă în domeniul software din partea dezvoltatorilor, aceasta este totodată o provocare, deoarece se dorește utilizarea acesteia atât într-o formă generică - web, cât și una particulară - Android, cel mai răspândit sistem de operare pentru dispozitive mobile.
	Privind riscurile de planificare, există posibilitatea de a subestima timpul alocat pentru implementarea componentelor pe care se bazează sistemul. Aceste
estimări eronate apar din cauza mai multor factori, un exemplu fiind întâmpinarea problemelor tehnice. Termenul de predare a aplicației poate fi întârziat.
	Din punct de vedere al problemelor tehnologice, echipa de dezvoltare trebuie să se familiarizeze cu instrumente software noi, fapt ce poate deveni un impas.
Nu există o certitudine că aplicația va oferi o performanță optimă.
	La nivelul factorilor externi, există un impediment destul de mare: preferințele și tendințele actuale în materie de funcționalități, având un impact asupra satisfacției utilizatorului.



**3. SISTEMUL PROPUS**

**3.1. Categorii de utilizatori ai sistemului**

Sistemul are țintă un public larg, adresându-se utilizatorilor indiferent de zona geografică și categoria socială. 
	Aplicația funcționează ca un canal bidirecțional de comunicare, permițând schimbul de mesaje între clienți în timp real. Pentru a utiliza sistemul, un utilizator nou va avea posibilitatea de a-și crea un cont, primind un mail de confirmare al acestuia. Apoi, se va putea autentifica folosind datele introduse anterior (adresă de e-mail și parolă). De îndată ce autentificarea s-a realizat cu succes, utilizatorul va putea căuta alte persoane care folosesc aplicația și să le trimită o cerere de prietenie. În cazul în care cererea de prietenie este acceptată, cei doi utilizatori se vor vedea reciproc în ferestrele de chat asociate fiecăruia. Mai departe, fiecare utilizator are posibilitatea de a accesa din fereastra de navigare un anumit prieten cu care dorește să comunice, sistemul deschizând o nouă fereastră de dialog între cei doi utilizatori. În cadrul acestei ferestre, fiecare dintre cei doi utilizatori are posibilitatea de a scrie mesaje, acestea fiind preluate de aplicație și trimise atât destinatarului, cât și expeditorului, pe baza identificatorului ferestrei de dialog deschise.
Pentru a putea beneficia de aplicație, un viitor utilizator trebuie sa dețină un telefon mobil, un laptop sau o tabletă, ce dispun de o conexiune activă la Internet. De asemenea, aplicația nu necesită costuri suplimentare pentru utilizare. În ceea ce privește frecvența de utilizare, aplicația își propune să devină un mijloc de comunicare între utilizatori, ceea ce implică utilizarea zilnică a acesteia. Este de așteptat un flux al accesărilor sistemului în număr proporțional cu contactele fiecărui utilizator.


**3.2. Cerințe de sistem**

Aplicația nu necesita cerințe de sistem mai mari decât orice alt device smart existent în viața de zi cu zi a omului cotidian. Mai specific, pe dispozitivele mobile, aplicația necesită minim 128 MB RAM și minim 100 MB de memorie internă pentru instalarea aplicației și a actualizărilor. În ceea ce privește aplicația Web, nu există cerințe suplimentare față de resursele folosite de browser-ul din care se rulează.


**3.3. Cerințe funcționale**

**Înregistrare user**

Utilizatorul trebuie sa se înregistreze pentru a folosi aplicația cu o adresa de mail validă. Versiunea de mobile presupune instalarea aplicației pe telefon; acesta primește un prompt în care introduce datele, fiind o modalitate de a identifica unic un utilizator in sistem. Pe langa adresa de mail, trebuie introdusă și o parola, ce asigura siguranța contului. Pentru utilizarea în browser nu este necesară instalarea niciunui plug-in, iar formularul de înscriere urmărește aceiași pași din platforma mobile.

**Autentificare user**

În momentul în care un utilizator dispune de un cont valid urmând etapa de înregistrare, poate să se autentifice folosind datele de autentificare. La introducerea unor date eronate, se va sugera opțiunea parolă uitată.

**Căutare prieten**

Un utilizator introduce numele unei alt utilizator în bara de cautare cu scopul
de a începe o conversație cu acesta. Urmează previzualizarea profilului și apare opțiunea de adaugare prieten.

**Adăugare prieten**

Orice utilizator își poate extinde rețeaua de socializare prin trimiterea unor cereri de prietenie, care necesită aprobarea celor solicitați.

**Aprobare prieten**

Conectarea dintre doi useri se realizează abia atunci când cererea unuia este acceptată de celălalt. Aceasta apare în secțiunea de notificări.
Trimitere mesaj
Expedierea de mesaje se efectuează dintr-o fereastra specifică fiecărui
prieten. Aceasta actiune este posibilă doar după adăugarea de prieteni.

**Recepționare mesaj**

Recepționarea de mesaje se efectuează dintr-o fereastra specifică fiecărui
prieten. Utilizatorul poate primi mesaje doar de la prieteni și primește notificări când apar unele noi.

**Schimbare parolă**

Această opțiune poate fi inițiată voluntar, atunci când se dorește schimbarea parolei din motive personale, care nu trebuie justificate, sau în momentul în care parola a fost uitată. Procesul constă în trimiterea unui mail cu un link de redirectare într-o fereastră nouă, unde se completează formularul de resetare a parolei.

**Parolă uitată**

Aplicația noastră va trata acest caz drept resetarea parolei.

 **Actualizare status**
 
Fiecare utilizator va avea o descriere pe care o va putea actualiza oricând dorește. Aceasta va apărea în secțiunea de vizualizare a profilului.

 **Vizualizare profil**
 
Fiecare utilizator va avea o pagină de profil proprie în care va putea completa informații personale și care va putea fi vizualizată de prietenii acestuia.

**Conversație pe grup**

Există opțiunea de a crea grupuri de prieteni. Dacă un utilizator aparține unui 
grup, acesta face difuzarea mesajelor, membrii grupului fiind notificati de mesajele noi în același timp.

**Ștergere cont**

Utilizatorii ce doresc să își înceteze activitatea în cadrul aplicației își pot șterge contul prin accesarea unui link primit pe mail și introducerea parolei asociate contului.



**3.4. Cerințe nefuncționale**

**Cerințe de utilizabilitate**

Sistemul pune la dispoziția utilizatorului o interfață simplă, ușor de înțeles și de urmărit. Aplicația își propune să fie cât mai simplu de utilizat și accesibilă tuturor categoriilor de utilizatori.

**Cerințe de performanță**

Sistemul ar trebui să fie receptiv la interacțiunea utilizatorilor cu aplicația, permițând o vizualizare în timp real a mesajelor schimbate între utilizatori. De asemenea, se garantează că aplicația va păstra intacte mesajele transmise între utilizatori și se va ocupa de transmiterea corectă și completă a mesajelor. În ceea ce privește disponibilitatea, sistemul trebuie să fie receptiv la accesul simultan a mai multor utilizatori și să ofere acestora toate drepturile pe care le pot exercita în cadrul aplicației. Sistemul utilizează un server care gestionează mesajele transmise între utilizatori. Acesta trebuie să permită accesul simultan a mai multor utilizatori și să gestioneze cât mai rapid fluxul de mesaje primite de la utilizatori.

**Cerințe de suportabilitate**

Aplicația oferă adaptabilitate, ceea ce implică abilitatea de a modifica sistemul pentru a adăuga facilități noi care să ușureze procesul de comunicare dintre utilizator. De asemenea, este mentenabilă, oferind posibilitatea de a schimba sistemul pentru a opera cu tehnologii noi sau de a repara defecte. 

**Cerințe de implementare**

În ceea ce privește aplicația pentru mobile, tehnologiile folosite vor fi Java și framework-ul de Android, în timp ce aplicația Web va fi scrisă folosind JavaScript, utilizând framework-uri precum React și Node.js.
