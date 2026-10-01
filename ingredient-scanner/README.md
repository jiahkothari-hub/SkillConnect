# Ingredient Scanner: Data + NLP module (Person 1)

*NLP-Based Detection and Interpretation of Hidden Food Ingredients*

This module is the data foundation of the project:
- a filtered Open Food Facts dataset;
- a preprocessing function that also works on OCR text;
- a 10-label entity schema with annotation guidelines;
- dictionaries and rules, and a weakly-labelled (**silver**) NER dataset;
- a workflow for human-verified (**gold**) data;
- leakage-free splits;
- a dictionary/rule baseline with an entity-level scorer and error analysis.

Person 2 trains Transformer NER models on its output. Person 3 reuses its preprocessing and
interpretation functions on OCR text.

The system **identifies** ingredients and additives and **interprets** them (*INS 330 → citric acid →
acidity regulator*). It makes **no health claims**.

**Start here:** [`notebooks/ingredient_scanner_person1_pipeline.ipynb`](notebooks/ingredient_scanner_person1_pipeline.ipynb)
walks through every stage with outputs and viva explanations.

| Key document | Content |
|---|---|
| [`reports/entity_schema.md`](reports/entity_schema.md) | labels, annotation rules, overlap policy, ambiguous terms |
| [`data/splits/README.md`](data/splits/README.md) | dataset card for Person 2 (format, label2id, loading, scoring) |
| [`reports/project_plan.md`](reports/project_plan.md) | design decisions and their reasons |
| [`reports/dataset_statistics.md`](reports/dataset_statistics.md), [`reports/silver_statistics.md`](reports/silver_statistics.md) | EDA |

## Current status

| Stage | Status |
|---|---|
| Data acquisition, filtering, EDA | done: 22,355 products |
| Preprocessing, schema, dictionaries, weak labelling | done: 22,323 silver records, ~358k entities |
| Leakage-free split, Hugging Face files | done: 15,556 / 3,280 / 3,487 products |
| Gold annotation | **files ready (400 products); human annotation not done yet → 0 gold records** |
| Baseline P/R/F1 + error analysis on gold | code done and tested; **numbers appear after gold annotation** |
| Checks that need no gold | agreement with OFF's additive parser F1 0.88; OCR-noise robustness |

## 1. Installation

Python 3.10+. From this folder (`ingredient-scanner/`):

```bash
python -m venv .venv
# Windows: .venv\Scripts\activate      macOS/Linux: source .venv/bin/activate
pip install -r requirements.txt
python -m pytest -q          # 36 tests
```

Every command is run from `ingredient-scanner/` and all paths are relative.

**Run everything** (about 3 minutes; uses the committed `products.csv`, no download):

```bash
python run_pipeline.py              # add --from-raw to also re-download and re-filter (1.3 GB, ~10 min)
```

Re-running produces byte-identical files: fixed seed 42, deterministic grouping, gzip files with a fixed timestamp.

## 2. Dataset download

```bash
python -m src.data.download_dataset     # official OFF CSV export, ~1.3 GB (skipped if present)
python -m src.data.download_taxonomy    # OFF additive taxonomy, pinned commit (~1 MB)
```

* Source: the **Open Food Facts full CSV export**. The official URL is tried first, then OFF's official
  S3 mirror. `data/raw/download_manifest.json` stores URL, date, size and SHA-256. The snapshot used
  here is from 2026-10-01, 4,535,553 products.
* The taxonomy gives `data/processed/additives_reference.csv`: 731 INS/E numbers with names, synonyms
  and functional classes. INS and E numbers are one numbering system (INS 330 = E330).
* Licence: ODbL. Data © Open Food Facts contributors.

## 3. Dataset filtering

```bash
python -m src.data.filter_dataset       # one streaming pass over 4.5M rows, ~3 min
```

| Step | Products |
|---|---:|
| Full export | 4,535,553 |
| Sold in an English-speaking country group | 1,441,888 |
| Has an ingredient list (not empty / not the literal "undefined") | 531,903 |
| Length 3–1500 characters | 528,982 |
| English list (`src/data/language.py`) | 509,294 |
| No exact-duplicate list | 405,812 |
| **Sampled per market, seed 42** | **22,355** |

Markets: `india` (all 4,355 usable), `uk_ireland` 7,000, `north_america` 7,000, `other_english` 4,000.
India writes additive codes in 42% of products ("INS 330", "(330)"), the UK in ~10% ("E330"), and
North America in <1% ("Red 40"). Mixing markets covers every notation. The output is
`data/processed/products.csv`, where `ingredients_text_raw` is never modified.

## 4. EDA

```bash
python -m src.data.eda              # figures 01-06, reports/dataset_statistics.md
python -m src.labeling.silver_report # figures 07-08 (entity-label distribution), reports/silver_statistics.md
```

| Figure | Shows |
|---|---|
| 01 | filtering funnel; missing values in the full export |
| 02 | ingredient-list length (characters, segments) |
| 03 | most frequent ingredient terms |
| 04 | most frequent additives; how codes are written per market |
| 05 | possible functional classes of detected additives |
| 06 | products per market; top categories |
| 07 | silver entities per label; % of products containing each label per market |
| 08 | which rule family produced each label |
| 11 | baseline robustness to OCR-like noise |

## 5. Preprocessing

```python
from src.preprocessing.pipeline import process_ingredient_text
r = process_ingredient_text("SUGAR, Acidity Regulator (INS 330)\nemulsi-\nfier")
r["text"]            # 'SUGAR, Acidity Regulator (INS 330) emulsifier'
r["tokens"]          # word tokens; r["token_offsets"] = character offsets into r["text"]
r["additive_codes"]  # [{'text': 'INS 330', 'number': '330', 'style': 'INS', ...}]
r["segments"], r["percentages"], r["non_latin_ratio"]
```

* **Normalisation** (`normalize.py`) changes as little as possible: NFKC Unicode, HTML entities,
  OFF allergen markup `_Milk_`, quotes/dashes/bullets, OCR line-break hyphenation, whitespace. It never
  lowercases and never removes numbers or punctuation.
* **Tokenisation** (`tokenize.py`): word tokens with offsets: `E330`, `150d`, `20.7%` stay single tokens.
* **INS/E codes** (`additive_codes.py`): `E330`, `E 330`, `e-330`, `INS 330`, `INS No. 330`, `503(ii)`,
  and bare numbers after a class word (`Emulsifier (471)`). All are normalised to `330`, `503ii`, ….
  Quantities, years and "vitamin E 400 IU" are rejected.
* **Segmentation** (`segment.py`): commas, semicolons, colons and brackets, with bracket depth for compound
  ingredients. Decimal commas (`5,5%`) are not split.

## 6. Weak labelling (silver data)

```bash
python -m src.labeling.build_vocabulary   # words seen in >=3 products (filters OCR garbage)
python -m src.labeling.build_silver       # data/processed/silver.jsonl.gz + reports/silver_log.json
```

**Schema (10 labels):** SUGAR, SWEETENER, FAT, PRESERVATIVE, COLOUR, ADDITIVE, INS_CODE, FUNCTION_CLASS,
FLAVOURING, INGREDIENT. See [`reports/entity_schema.md`](reports/entity_schema.md).

**Dictionaries** (`configs/lexicons/`, plain text, editable):
- sugars, sweeteners, fats, flavourings and functional classes (hand-curated);
- additive aliases (label names mapped to INS numbers);
- all usable names from the OFF additive taxonomy;
- `additive_label_rules.yaml`: INS number → PRESERVATIVE / COLOUR / SWEETENER / ADDITIVE, with explicit exceptions and blocked ambiguous names.

**Decision order** (`weak_labeler.py`):
1. Skip statements.
2. Codes → INS_CODE.
3. Exact dictionary match.
4. Conjunctions ("A and B").
5. Multiple dictionary terms.
6. Dictionary suffix / head word ("organic cane **sugar**", "refined palm **oil**").
7. Fallback INGREDIENT.

Each silver entity stores its **rule**. `confidence` is `null` because it is never invented. It can be
measured as each rule's precision on gold train/validation data.

## 7. Gold annotation

```bash
python -m src.annotation.sample_gold          # 400 products + Doccano files (already done)
```

* **Selection:** per split (train 220 / validation 60 / test 120), so gold test stays held out.
  - About 45% is stratified random by market × length.
  - About 55% is enriched with hard or rare cases (INS codes, preservatives, colours, sweeteners, several sugars/fats, noisy text).
  - `annotation_tracking.csv` records why each product was chosen and who annotates it.
* **Tool: [Doccano](https://github.com/doccano/doccano)** (free, local).
  1. `pip install doccano` (or the Docker image), `doccano init`, `doccano createuser --username admin --password …`.
  2. Run `doccano webserver --port 8000` and, in a second terminal, `doccano task`.
  3. Create a *Sequence Labeling* project and import labels from `data/annotations/doccano_label_config.json`.
  4. Import your file `data/annotations/batches/<annotator>.jsonl`.
* **Annotate:** files are **pre-annotated** with silver labels. Check every span against the guideline,
  correct it, and approve the document. The first 40 items are shared by all annotators.
* **Agreement:** export (JSONL, *only approved*) to `data/annotations/exports/<annotator>.jsonl`, then
  run `python -m src.annotation.agreement` (entity F1 + Cohen's kappa per annotator pair). Discuss
  disagreements and save agreed versions in `exports/adjudicated.jsonl`.
* **Import:** `python -m src.annotation.import_annotations` writes `data/annotations/gold_verified.jsonl`
  and `data/splits/gold/*.jsonl`. Documents where annotators disagree are refused until adjudicated.

Rename `annotator_1..3` in `configs/data_config.yaml` → `gold.annotators` and re-run `sample_gold`
before you start.

## 8. Dataset splitting

```bash
python -m src.data.split_dataset
```

Products are grouped with union-find when they share any of these:
1. the same letters;
2. the same set of ingredient segments;
3. the same name + brand;
4. the same first 5 segments;
5. ingredient-set Jaccard ≥ 0.8.

Whole groups are assigned to 70/15/15 per market (seed 42). Measured leakage: no identical list
across splits, and 0.15% of val/test products have a ≥0.8-similar train product. Outputs:
- `data/splits/silver/{train,validation,test}.jsonl.gz`
- `data/splits/gold/…` (after annotation)
- `label2id.json`, `id2label.json`
- `data/processed/split_assignment.csv`, `reports/split_report.json`

## 9. Baseline and evaluation

```bash
python -m src.evaluation.evaluate_baseline    # needs gold test -> reports/baseline_metrics.{json,md}, figures 09-10
python -m src.evaluation.off_agreement        # no gold needed: agreement with OFF's additive parser
python -m src.evaluation.noise_robustness     # no gold needed: OCR-noise robustness, figure 11
```

* `src/baseline/dictionary_ner.py`: `DictionaryNER().predict(text)` (dictionaries + regex + rules).
* `src/evaluation/metrics.py`: **strict entity-level** P/R/F1 (micro, macro, per class) plus a partial-match
  score. This is the one shared scorer for the baseline, DistilBERT, BERT and BERT+CRF.
* The baseline is **never scored on silver labels**: silver is its own output, so the score would be
  100% and meaningless.

Results available now:

| Check | Result |
|---|---|
| Agreement with OFF's own additive parser (silver test products, additive numbers) | precision 0.85, recall 0.91, F1 0.88 |
| Baseline output under OCR-like noise (F1 vs. clean predictions) | 1% noise 0.97 · 5% 0.86 · 10% 0.74 |

## 10. Error analysis

```bash
python -m src.evaluation.error_analysis       # needs gold test
```

Writes `reports/error_analysis/baseline_errors.csv` with columns text, expected entity/label,
predicted entity/label, error type, entity category, and tags.
- **Error types:** false positive, false negative, boundary error, type error, boundary + type.
- **Tags:** INS-code variant, OCR noise, compound, unknown term, overlapping category, ambiguous term.

## 11. How Person 2 consumes the data

See [`data/splits/README.md`](data/splits/README.md). In short:

```python
from datasets import Dataset
train = Dataset.from_json("data/splits/silver/train.jsonl.gz")   # tokens + integer ner_tags
# label2id / id2label: data/splits/label2id.json, id2label.json (21 BIO tags)
from src.evaluation.metrics import evaluate_tag_sequences       # score on data/splits/gold/test.jsonl
```

- Train on silver train (optionally plus gold train).
- Select models on gold validation.
- Report only on gold test, using `evaluate_tag_sequences` so the numbers are comparable with the baseline.
- `src/evaluation/noise_robustness.add_ocr_noise` can be reused for augmentation.

## 12. How Person 3 consumes the preprocessing

```python
from src.baseline.dictionary_ner import DictionaryNER          # or Person 2's model
from src.labeling.interpret import interpret_entities, describe

result = DictionaryNER().predict(ocr_text)                       # normalises any OCR string internally
for ent in interpret_entities(result["text"], result["entities"]):
    print(ent["label"], describe(ent))   # e.g. "INS_CODE  INS 330 -> Citric acid -> acidity regulator"
```

* `process_ingredient_text(text)` alone gives normalised text, tokens and codes. It does not depend on
  Open Food Facts.
* `data/processed/additives_reference.csv` is the knowledge-base seed: number → name → functional classes.
* `declared_class` is the function written on the label, which is preferred. `reference_classes` are the
  possible functions from the taxonomy.

## Final processed files

| File | Content |
|---|---|
| `data/processed/ingredients_processed.csv` | **one row per product**: metadata, split, raw + normalised text, entities grouped by label (sugars, fats, preservatives, colours, INS numbers, …), additive interpretations, counts |
| `data/processed/silver_entities.csv.gz` | one row per entity: span, label, rule, INS number, linked name, declared/reference class |
| `data/processed/silver.jsonl.gz` | full silver records (tokens, offsets, BIO tags, entities) |
| `data/splits/` | Hugging Face-ready train/validation/test |

All entity columns are **silver** (automatic) until the gold annotation is imported.

## Limitations

* Silver labels are noisy. INGREDIENT (59% of entities) comes from a fallback rule. Rare words are left
  unlabelled to keep OCR garbage out.
* Open Food Facts text is crowd-sourced and often OCR-ed ("lodized salt"). This is realistic, but the
  noise also enters the silver labels.
* English only (keyword heuristic). Bilingual EN/FR lists are removed. Mostly non-Latin lists (31) are skipped.
* OFF functional classes are incomplete (e.g. lactic acid has none). The label's declared class is preferred.
* Exact dictionary matching is brittle under OCR noise (F1 0.74 of clean output at 10% noise).
* Baseline P/R/F1 and the error analysis require the gold annotation, which has not been done yet.
