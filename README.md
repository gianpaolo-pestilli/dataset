# CODICE PER LA GENERAZIONE DEL DATASET E ALTRI ARTEFATTI RELATIVI AL CORSO

Progetto per il corso di **Ingegneria del Software 2 (ISW2)** — costruzione di un dataset per la predizione della bugginess delle classi Java del progetto Apache **Syncope**, a partire da dati estratti da GitHub, Jira e SonarCloud, seguita da esperimenti di Machine Learning con Weka.

## Struttura della repository

```
dataset/
├── README.md
├── ISW2-Gianpaolo_Pestilli_0386492.pdf   # Relazione del progetto
└── dataset_creation/                     # Applicazione Java (Maven)
    ├── pom.xml
    ├── Instructions.txt                  # Guida alla configurazione
    ├── config.properties                 # File di configurazione runtime
    ├── src/main/java/...                 # Codice sorgente
    └── *.csv / *.arff / *.txt / *.pdf    # Dataset e report generati
```

## Cosa fa il progetto

L'applicazione, scritta in Java (Maven, JDK 25), automatizza l'intera pipeline di:

1. **Estrazione dati** dal repository Git del progetto (via [JGit](https://www.eclipse.org/jgit/)), dai ticket Jira e dalle metriche di SonarCloud.
2. **Costruzione del dataset**: per ogni release del progetto, calcola per ciascuna classe Java metriche di processo (LOC, numero di revisioni, numero di fix, numero di autori, churn, età, ecc.) ed etichetta la classe come `buggy`/`not buggy` (label `isBuggy`).
3. **Ranking**: genera classifiche delle classi per sforzo (effort), code smell, debito tecnico e valutazione applicativa.
4. **Machine Learning**: addestra e valuta classificatori (Random Forest, Naive Bayes, IBk, ecc.) tramite [Weka](https://www.cs.waikato.ac.nz/ml/weka/), con supporto a diverse tecniche di feature selection, bilanciamento delle classi (es. SMOTE), validazione e partizionamento del dataset, oltre ad analisi "what-if".

## Come funziona: `config.properties`

Il comportamento dell'applicazione è determinato dalla proprietà `application.type`, che seleziona una delle seguenti fasi della pipeline:

| Valore | Descrizione |
|---|---|
| `DATASET_INIT` | Inizializzazione del dataset |
| `DATASET_POPULATION` | Popolamento del dataset con le metriche estratte |
| `DATASET_LABELING` | Etichettatura delle classi (buggy/not buggy) |
| `EFFORT_RANKING` | Calcolo del ranking per sforzo |
| `SMELL_RANKING` | Calcolo del ranking per code smell |
| `DEBT_RANKING` | Calcolo del ranking per debito tecnico |
| `APPLICATIVE_RANKING` | Calcolo del ranking applicativo |
| `ML_TRAINING` | Addestramento dei classificatori |
| `ML_REPORTING` | Generazione dei report sulle performance dei classificatori |
| `ML_DATA_PARTITION` | Partizionamento del dataset per il training |
| `ML_WHAT_IF` | Analisi what-if sui risultati |

Altri parametri principali di `config.properties`:

- `project.name` / `project.owner` / `project.repo` — progetto target (default: Apache Syncope)
- `sonar.project.key` — chiave del progetto in SonarCloud
- `effort-ranking.file`, `smells-ranking.file`, `debt-ranking.file` — prefissi dei file di output dei ranking, con relativi contatori `*.count` per evitare di sovrascrivere i file delle release precedenti
- `*.touched` — flag che indica se l'ultimo file di ranking generato è già stato "consumato"

Le istruzioni dettagliate sono documentate in [`dataset_creation/Instructions.txt`](dataset_creation/Instructions.txt).

## Requisiti

- Java 25 (compilazione/target impostati nel `pom.xml`)
- Maven

### Dipendenze principali

- [`org.json`](https://mvnrepository.com/artifact/org.json/json) — parsing JSON (API Jira/SonarCloud)
- [`org.eclipse.jgit`](https://www.eclipse.org/jgit/) — interazione con repository Git
- `maven-invoker` — invocazione di build Maven
- [`weka-stable`](https://www.cs.waikato.ac.nz/ml/weka/) + `SMOTE` — machine learning e bilanciamento delle classi
- [`xchart`](https://knowm.org/open-source/xchart/) — generazione di grafici
- `pdfbox` — generazione/lettura di PDF
- `commons-math3` — utilità matematiche/statistiche

## Come eseguire

```bash
cd dataset_creation
# Modificare config.properties impostando application.type sulla fase desiderata
mvn compile exec:java -Dexec.mainClass="start.Main"
```

(oppure eseguire `start.Main` direttamente da un IDE, dopo aver importato il progetto Maven)

## Output generati

- **`dataset.csv` / `dataset_B.csv` / `dataset_C.csv`** (e relative versioni `.arff` per Weka) — dataset con, per ogni classe/release, metriche quali `LOC`, `numRevisions`, `numFixes`, `numAuthors`, `churn`, `age`, `numSmells`, `isBuggy`, ecc.
- **`considered_releases.csv` / `first_releases.csv`** — elenco delle release considerate del progetto Syncope, con data e ID
- **`effort_ranking_0.csv`, `smells_ranking_0.csv`, `debt_ranking_0.csv`** — classifiche delle classi per sforzo, smell e debito tecnico
- **`applicative_ranking.txt`** — ranking applicativo delle classi
- **`performance.csv`** — risultati degli esperimenti ML (accuratezza, precisione, recall, AUC, Kappa, matrice di confusione) al variare di feature selection, bilanciamento, validazione e classificatore
- **`proportion_info.txt`** — statistiche sul metodo "proportion" usato per stimare la injected version dei bug quando mancante
- **`whatif-*.csv/png`, `BoxPlot.pdf`, `Grafici_classificatori.pdf`** — grafici e tabelle di analisi what-if e confronto tra classificatori

## Relazione

Il documento [`ISW2-Gianpaolo_Pestilli_0386492.pdf`](ISW2-Gianpaolo_Pestilli_0386492.pdf) contiene la relazione completa del progetto, con la descrizione della metodologia, delle scelte progettuali e dei risultati ottenuti.

## Autore

Gianpaolo Pestilli — progetto per il corso di Ingegneria del Software 2.