# Ingredient Scanner — Data + NLP module (Person 1)

*NLP-Based Detection and Interpretation of Hidden Food Ingredients*

This module builds the data foundation for the project: a filtered Open Food Facts dataset,
preprocessing, an entity schema, weakly-labelled (silver) and human-verified (gold) NER data,
leakage-free splits and a dictionary/rule baseline. Person 2 trains Transformer NER models on its
output; Person 3 reuses its preprocessing function on OCR text.

The system **identifies** ingredients and additives and **interprets** them (e.g. *INS 330 → citric
acid → acidity regulator*). It does **not** make health claims.

Design decisions and the roadmap: [`reports/project_plan.md`](reports/project_plan.md).

## 1. Installation

Python 3.10 or newer. From this folder (`ingredient-scanner/`):

```bash
python -m venv .venv
# Windows: .venv\Scripts\activate      macOS/Linux: source .venv/bin/activate
pip install -r requirements.txt
python -m pytest -q          # all tests should pass
```

Every command below is run from the `ingredient-scanner/` folder with `python -m ...`, and all paths are
relative, so it works the same on every machine.

## 2. Dataset download

```bash
python -m src.data.download_dataset     # ~1.3 GB, a few minutes; skipped if already present
python -m src.data.download_taxonomy    # Open Food Facts additive list (~1 MB)
```

* Source: the official **Open Food Facts CSV export** (`en.openfoodfacts.org.products.csv.gz`).
  The official URL is tried first, then OFF's official AWS S3 mirror (see `configs/data_config.yaml`).
* `data/raw/download_manifest.json` records URL, date, size and SHA-256. Open Food Facts changes
  daily, so a later download will differ slightly — that is why the filtered dataset is committed.
* The additive taxonomy is pinned to one GitHub commit. Output: `data/processed/additives_reference.csv`
  (731 additive numbers: `E330` → *Citric acid* → `antioxidant|sequestrant`) and
  `data/processed/function_classes.csv` (53 classes with synonyms such as *raising agent = leavening*).
* Licence: Open Database License (ODbL). Data © Open Food Facts contributors.

**You do not need to run the download to work with the data** — `data/processed/products.csv` is in git.

## 3. Dataset filtering

```bash
python -m src.data.filter_dataset       # ~3-4 minutes, one pass over 4.5M rows
```

| Step | Products |
|---|---:|
| Full export | 4,535,553 |
| Sold in an English-speaking country group | 1,441,888 |
| Has an ingredient list (not empty / not the literal "undefined") | 531,903 |
| Length 3–1500 characters | 528,982 |
| English list (`src/data/language.py`) | 509,294 |
| No exact-duplicate ingredient list | 405,812 |
| **Sampled per country group (seed 42)** | **22,355** |

Country groups: `india` (all 4,355 usable products), `uk_ireland` (7,000), `north_america` (7,000),
`other_english` (4,000). They use different label conventions — India writes additive codes in 42%
of products ("INS 330" or "(330)"), the UK in ~10% ("E330"), North America in <1% ("Red 40") — so
mixing them teaches the model all the notations.

Output `data/processed/products.csv` (one row per product):

| Column | Meaning |
|---|---|
| `product_id` | barcode |
| `product_name`, `brands`, `countries`, `main_category`, `categories` | metadata |
| `country_group` | india / uk_ireland / north_america / other_english |
| `ingredients_text_raw` | **original ingredient text, never modified** |
| `n_chars`, `detected_language`, `english_score`, `english_rule` | filtering diagnostics |
| `n_products_with_same_text` | how many products shared this exact list before de-duplication |
| `off_additives_n`, `off_additives_tags` | Open Food Facts' own additive detection (EDA / sanity checks only) |

Also written: `reports/source_profile.json` (missing values and countries of the *whole* export) and
`reports/filter_log.json` (the funnel above).

## 4. Exploratory data analysis

```bash
python -m src.data.eda
```

Writes `reports/dataset_statistics.md` (all numbers + figures), `reports/dataset_statistics.json` and:

| Figure | Shows |
|---|---|
| `01_dataset_size_and_missingness.png` | filtering funnel; missing values in the full export |
| `02_ingredient_length_distribution.png` | list length in characters and in ingredient segments |
| `03_top_ingredient_terms.png` | most frequent ingredient segments |
| `04_additive_code_frequency.png` | most frequent additives; how codes are written per market |
| `05_additive_function_classes.png` | possible functional classes of detected additives |
| `06_market_groups_and_categories.png` | products per market group; top categories |

The distribution of *our* entity labels is added in Stage 3, once the weak labeller exists.

## 5–12. Later stages

Preprocessing, weak labelling, gold annotation, splitting, baseline, error analysis and the
integration notes for Person 2 and Person 3 are added to this README as each stage is built
(see the roadmap in `reports/project_plan.md`).

## Known limitations of the data (so far)

* Open Food Facts is crowd-sourced; many ingredient lists were OCR-ed from photos and contain errors
  ("lodized salt", "Milk Solrds"). This is realistic for our OCR use-case, but the gold set must be
  checked by humans.
* The language filter is a keyword heuristic. Texts with no keyword are kept only if plain ASCII
  (855 products). Bilingual English/French lists (common in Canada) are removed.
* Open Food Facts' functional classes are incomplete (e.g. lactic acid E270 has none, sodium
  bicarbonate lacks "raising agent"); they are curated in Stage 3 before being used.
* Category metadata is missing for ~23% of the project dataset.
