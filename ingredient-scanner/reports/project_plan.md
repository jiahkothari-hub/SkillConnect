# Person 1 (DATA + NLP) — Plan and design decisions

This file records *why* the data pipeline is built the way it is. The detailed annotation rules
will live in `reports/entity_schema.md` (Stage 3); this file is the proposal it is based on.

## 1. Architecture

```
ingredient-scanner/
├── configs/data_config.yaml      every tunable number (quotas, thresholds, seed, URLs) in one place
├── data/
│   ├── raw/                      untouched downloads (git-ignored; manifest + taxonomy committed)
│   ├── interim/                  re-creatable intermediate files (git-ignored)
│   ├── processed/                project dataset + reference tables (committed: the frozen snapshot)
│   ├── annotations/              gold annotation batches (human-verified only)
│   └── splits/                   train / validation / test files for Person 2
├── src/
│   ├── data/                     STAGE 1: download, filter, taxonomy, EDA
│   ├── preprocessing/            STAGE 2: process_ingredient_text() — shared with Person 3 (OCR)
│   ├── labeling/                 STAGE 3: dictionaries, regex rules, weak labeller -> silver data
│   ├── annotation/               STAGE 4: gold sampling, Doccano export/import, agreement
│   ├── baseline/                 STAGE 6: dictionary/rule NER = the baseline Person 2 must beat
│   ├── evaluation/               STAGE 6-7: entity-level P/R/F1, error analysis
│   └── utils/                    config/paths, JSONL I/O, validation checks
├── reports/                      figures, statistics, schema, metrics, error analysis
└── tests/                        pytest unit tests for every stage
```

Integration contracts (the only things other people depend on):

| Consumer | What they use | Where |
|---|---|---|
| Person 2 (NER models) | `train/validation/test.jsonl` with `tokens`, `ner_tags`, `label2id.json` | `data/splits/` |
| Person 2 | entity-level scorer, so all models are scored identically | `src/evaluation/` |
| Person 3 (OCR + app) | `process_ingredient_text(text)` | `src/preprocessing/` |
| Person 3 (knowledge base) | additive table: number → name → functional classes | `data/processed/additives_reference.csv` |

## 2. Dataset / source

**Open Food Facts full CSV export** (`en.openfoodfacts.org.products.csv.gz`, ODbL licence),
downloaded from the official URL with the official AWS S3 mirror as fallback.

| Property | Value (snapshot 2026-10-01) |
|---|---|
| Compressed size | 1.28 GB (13 GB uncompressed, ~200 columns) |
| Products | 4,535,553 |
| `ingredients_text` missing (incl. literal "undefined") | 72.4% |
| Columns we read | 10 (`code`, `product_name`, `brands`, `countries_en`, `categories_en`, `main_category_en`, `ingredients_text`, `ingredients_tags`, `additives_n`, `additives_tags`) |
| Products sold in India | 21,188 (only ~4,400 with a usable English ingredient list) |

Why not the API: it is rate-limited and meant for apps. Why not keep everything: we need a dataset we
can inspect, annotate and explain. The project dataset is **22,355 products**: all usable Indian
products (INS-style labels) plus seeded samples from UK/Ireland (E-numbers), North America (names
such as "Red 40") and other English-speaking markets.

Second source: the **Open Food Facts additive taxonomy** (pinned GitHub commit) → 731 additive numbers
with English names, synonyms and functional classes. INS and E numbers share one numbering system
(INS 330 = E330 = citric acid), so one table serves both notations.

## 3. Proposed entity schema (to be finalised in Stage 3)

The suggested 12-label schema mixes two different questions:
*"what is this mention?"* (sugar, fat, additive, code) and *"what job does this additive do?"*
(preservative, colour, emulsifier, ...). The second question causes overlaps that annotators cannot
resolve consistently:

* Many additives have several functions (OFF lists citric acid as antioxidant **and** sequestrant;
  Codex also lists it as an acidity regulator). One span cannot carry three BIO labels.
* The function is often **written on the label itself**: "Acidity regulator (INS 330)". The text
  "acidity regulator" is the function; "INS 330" is the substance.
* Function classes are rare and unbalanced (sweetener ≈7% of products, flavour enhancer ≈2%), which
  makes per-class F1 unstable on a small gold test set.

Proposal — **8 mutually exclusive span labels** (NER = identification), with the functional class
looked up afterwards from the reference table (entity linking = interpretation):

| Label | Annotate | Examples |
|---|---|---|
| `SUGAR` | sugars and syrups added for sweetness | sugar, glucose syrup, dextrose, invert sugar syrup, honey, jaggery |
| `SWEETENER` | non-sugar sweeteners, incl. polyols | sucralose, aspartame, steviol glycosides, sorbitol |
| `FAT` | oils and fats | palm oil, refined palmolein, hydrogenated vegetable fat, cocoa butter, ghee |
| `ADDITIVE` | a named substance that has an INS/E number | citric acid, soy lecithin, xanthan gum, tartrazine, Red 40 |
| `INS_CODE` | an additive code in any notation | INS 330, E330, E 150d, (330), 503(ii) |
| `FUNCTION_CLASS` | the functional class name written on the label | acidity regulator, emulsifier, preservative, colour, raising agent |
| `FLAVOURING` | flavourings | natural flavour, nature-identical flavouring substances |
| `INGREDIENT` | any other food ingredient | wheat flour, milk solids, tomato paste, salt, water |

Overlap policy (one rule, in priority order): `INS_CODE` > `SWEETENER` > `SUGAR` > `FAT` >
`FLAVOURING` > `ADDITIVE` > `FUNCTION_CLASS` > `INGREDIENT`. Example: "Acidity regulator (INS 330)"
→ `FUNCTION_CLASS` + `INS_CODE`; the app then explains *INS 330 → citric acid → acidity regulator*.

Three layers, kept separate on purpose:

| Layer | Example | Who |
|---|---|---|
| Identification | "INS 330" is an `INS_CODE` | NER (Person 1 baseline, Person 2 models) |
| Interpretation | INS 330 = citric acid, used as an acidity regulator | entity linking (Person 3), reference table (Person 1) |
| Health claim | "this is bad for you" | **out of scope** — the system never makes one |

## 4. Annotation strategy

1. **Silver data (automatic).** Dictionaries + regex label all 22k products. Every entity records
   which rule produced it. Silver labels are a training resource, never treated as truth.
2. **Gold data (human-verified).** ~400 products sampled with stratification (market group, length,
   with/without codes, multiple sugars/fats, noisy/OCR-like text). Annotators correct silver
   pre-annotations in **Doccano** (free, local, span labelling, JSONL import/export). Every record
   stores annotator name and status; only `status = verified` records count as gold.
3. **Consistency.** All three team members double-annotate the same ~40 products first; we compute
   entity-level agreement (F1 between annotators), discuss disagreements and update the guideline
   before splitting the rest of the work.
4. **Rule confidence** is not invented: once gold train/validation data exists, each rule's precision is
   measured on it and stored as that rule's confidence.

## 5. Roadmap

| Stage | Deliverable | Status |
|---|---|---|
| 1 | Folder structure, download, filtering, taxonomy, EDA | **done** |
| 2 | Preprocessing (`process_ingredient_text`), tokenisation with character offsets, validation checks | next |
| 3 | Entity schema + guideline, dictionaries, regex rules, weak labeller, silver dataset, entity-label EDA | |
| 4 | Gold sampling, Doccano export/import, agreement script — **then the team annotates** | |
| 5 | Grouped (leakage-free) train/validation/test split, HF-ready files, `label2id.json` | |
| 6 | Dictionary baseline evaluated on gold test: overall + per-class P/R/F1 | needs gold |
| 7 | Error analysis CSV and limitations section | needs gold |
