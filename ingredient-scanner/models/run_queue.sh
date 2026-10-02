#!/bin/bash
# Runs the remaining heavy jobs one after another (CPU training does not tolerate sharing cores).
cd "$(dirname "$0")/.."
while kill -0 817 2>/dev/null; do sleep 30; done          # wait for the running DistilBERT job
echo "=== crf $(date +%T)";            python -m src.ner.crf_baseline
echo "=== distilbert_aug $(date +%T)"; python -m src.ner.transformer_ner --model distilbert-base-uncased --run distilbert_aug --augment --epochs 2 --max-train 8000 > models/runs/distilbert_aug.log 2>&1
echo "=== distilbert_crf $(date +%T)"; python -m src.ner.transformer_ner --model distilbert-base-uncased --run distilbert_crf --crf --epochs 2 --max-train 8000 > models/runs/distilbert_crf.log 2>&1
echo "=== bert $(date +%T)";           python -m src.ner.transformer_ner --model bert-base-cased --run bert --lr 3e-5 --epochs 2 --max-train 8000 > models/runs/bert.log 2>&1
echo "=== ocr $(date +%T)";            python -c "import src.ocr.evaluate_ocr as m; m.run_ocr()" > models/runs/ocr.log 2>&1
echo "=== compare $(date +%T)";        python -m src.ner.evaluate_models > models/runs/compare.log 2>&1
echo "=== ocr_eval $(date +%T)";       python -m src.ocr.evaluate_ocr > models/runs/ocr_eval.log 2>&1
echo "=== done $(date +%T)"
