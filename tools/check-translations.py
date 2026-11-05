#!/usr/bin/env python3
import re
import sys
import xml.etree.ElementTree as ET
from pathlib import Path

ROOT = Path(__file__).resolve().parent.parent

RESOURCE_ROOTS = [
    ROOT / "shared/src/commonMain/composeResources",
    ROOT / "shared/src/androidMain/res",
]

EXPECTED_LOCALES = [
    "values-es",
    "values-fr",
    "values-de",
    "values-pt-rBR",
    "values-it",
    "values-ja",
    "values-ko",
    "values-zh-rCN",
    "values-ru",
    "values-tr",
]

PLURAL_CATEGORIES = {
    "en": {"one", "other"},
    "es": {"one", "other"},
    "fr": {"one", "other"},
    "de": {"one", "other"},
    "pt": {"one", "other"},
    "it": {"one", "other"},
    "tr": {"one", "other"},
    "ru": {"one", "few", "many", "other"},
    "ja": {"other"},
    "ko": {"other"},
    "zh": {"other"},
}

PLACEHOLDER = re.compile(r"%(?:(\d+)\$)?[-#+ 0,(]*\d*(?:\.\d+)?[a-zA-Z%]")
STRING_RE = re.compile(r'<string\s+name="([^"]+)"[^>]*>(.*?)</string>', re.S)
PLURALS_RE = re.compile(r'<plurals\s+name="([^"]+)"[^>]*>(.*?)</plurals>', re.S)
ITEM_RE = re.compile(r'<item\s+quantity="([^"]+)"[^>]*>(.*?)</item>', re.S)
ENTITY_RE = re.compile(r"&(amp|lt|gt|apos|quot);")


class Report:
    def __init__(self):
        self.errors = []
        self.warnings = []

    def error(self, where, message):
        self.errors.append(f"ERROR  {where}: {message}")

    def warn(self, where, message):
        self.warnings.append(f"WARN   {where}: {message}")


def language_of(directory):
    qualifier = directory[len("values-"):] if directory.startswith("values-") else "en"
    return qualifier.split("-")[0]


def placeholders(text):
    found = []
    auto = 0
    for match in PLACEHOLDER.finditer(text):
        token = match.group(0)
        if token == "%%":
            continue
        index = match.group(1)
        if index is None:
            auto += 1
            index = str(auto)
        found.append(f"{index}:{token[-1]}")
    return sorted(found)


def unescape_entities(raw):
    table = {"amp": "&", "lt": "<", "gt": ">", "apos": "'", "quot": '"'}
    return ENTITY_RE.sub(lambda m: table[m.group(1)], raw)


def escaping_problems(raw):
    problems = []
    stripped = raw.strip()
    quoted = len(stripped) >= 2 and stripped.startswith('"') and stripped.endswith('"')
    body = unescape_entities_keep_apostrophe_entity(raw)
    if re.search(r"&(?!(?:amp|lt|gt|apos|quot|#\d+|#x[0-9a-fA-F]+);)", raw):
        problems.append("unescaped ampersand")
    if not quoted:
        if re.search(r"(?<!\\)'", body):
            problems.append("unescaped apostrophe")
        if re.search(r'(?<!\\)"', body):
            problems.append("unescaped double quote")
    if re.match(r"^[@?]", stripped):
        problems.append("leading @ or ? needs a backslash")
    if "<" in raw and not re.search(r"<(?:b|i|u|xliff:g)\b", raw):
        problems.append("unexpected markup")
    return problems


def unescape_entities_keep_apostrophe_entity(raw):
    return raw.replace("&apos;", "\\'").replace("&quot;", '\\"')


def decode(raw):
    text = unescape_entities(raw.strip())
    text = text.replace("\\n", "\n").replace("\\'", "'").replace('\\"', '"').replace("\\@", "@").replace("\\?", "?")
    return text


def load_file(path, report):
    where = str(path.relative_to(ROOT))
    raw = path.read_text(encoding="utf-8")
    try:
        ET.fromstring(raw)
    except ET.ParseError as error:
        report.error(where, f"XML parse failure: {error}")
        return None
    entries = {}
    order = []
    plural_spans = [m.span() for m in PLURALS_RE.finditer(raw)]

    def inside_plural(position):
        return any(start <= position < end for start, end in plural_spans)

    tokens = []
    for match in STRING_RE.finditer(raw):
        if inside_plural(match.start()):
            continue
        tokens.append((match.start(), "string", match))
    for match in PLURALS_RE.finditer(raw):
        tokens.append((match.start(), "plurals", match))
    tokens.sort(key=lambda t: t[0])
    for _, kind, match in tokens:
        name = match.group(1)
        if name in entries:
            report.error(where, f"duplicate key {name}")
            continue
        if kind == "string":
            value = match.group(2)
            for problem in escaping_problems(value):
                report.error(where, f"{name}: {problem}")
            entries[name] = ("string", decode(value))
        else:
            items = {}
            for item in ITEM_RE.finditer(match.group(2)):
                quantity, value = item.group(1), item.group(2)
                if quantity in items:
                    report.error(where, f"{name}: duplicate plural quantity {quantity}")
                for problem in escaping_problems(value):
                    report.error(where, f"{name}[{quantity}]: {problem}")
                items[quantity] = decode(value)
            entries[name] = ("plurals", items)
        order.append(name)
    return entries, order


def load_locale(directory_path, report):
    merged = {}
    order = []
    files = sorted(directory_path.glob("strings*.xml"))
    if not files:
        return None
    for file in files:
        loaded = load_file(file, report)
        if loaded is None:
            continue
        entries, file_order = loaded
        for name in file_order:
            if name in merged:
                report.error(str(file.relative_to(ROOT)), f"duplicate key {name} across files")
                continue
            merged[name] = entries[name]
            order.append(name)
    return merged, order


def words(text):
    return len(re.findall(r"[^\s·→−–—]+", text))


def check_root(root, report, strict_locales):
    if not root.exists():
        return 0
    english_dir = root / "values"
    english = load_locale(english_dir, report)
    if english is None:
        return 0
    base, base_order = english
    label = str(root.relative_to(ROOT))
    checked = 0
    present = sorted(
        d.name for d in root.iterdir()
        if d.is_dir() and d.name.startswith("values-") and any(d.glob("strings*.xml"))
    )
    if strict_locales:
        for locale in EXPECTED_LOCALES:
            if locale not in present:
                report.error(label, f"missing locale directory {locale}")
    for locale in present:
        loaded = load_locale(root / locale, report)
        if loaded is None:
            continue
        entries, order = loaded
        where = f"{label}/{locale}"
        checked += 1
        language = language_of(locale)
        categories = PLURAL_CATEGORIES.get(language)
        if categories is None:
            report.error(where, f"no plural rules known for language {language}")
            continue
        missing = [k for k in base_order if k not in entries]
        extra = [k for k in order if k not in base]
        for key in missing:
            report.error(where, f"missing key {key}")
        for key in extra:
            report.error(where, f"extra key {key}")
        common_order = [k for k in order if k in base]
        expected_order = [k for k in base_order if k in entries]
        if common_order != expected_order:
            report.error(where, "key order differs from English")
        for key in base_order:
            if key not in entries:
                continue
            base_kind, base_value = base[key]
            kind, value = entries[key]
            if kind != base_kind:
                report.error(where, f"{key}: is {kind} but English is {base_kind}")
                continue
            if kind == "string":
                if not value.strip():
                    report.error(where, f"{key}: empty translation")
                    continue
                if placeholders(value) != placeholders(base_value):
                    report.error(
                        where,
                        f"{key}: placeholders {placeholders(value)} differ from English {placeholders(base_value)}",
                    )
                if value.strip() == base_value.strip() and words(base_value) > 3:
                    report.warn(where, f"{key}: identical to English")
            else:
                expected = placeholders(base_value["other"])
                present_categories = set(value.keys())
                for category in sorted(categories - present_categories):
                    report.error(where, f"{key}: missing plural category {category}")
                for category in sorted(present_categories - categories):
                    report.error(where, f"{key}: category {category} is not used by {language}")
                for category, text in value.items():
                    if not text.strip():
                        report.error(where, f"{key}[{category}]: empty translation")
                        continue
                    found = placeholders(text)
                    if found == expected:
                        continue
                    if category == "one" and set(found) <= set(expected):
                        report.warn(where, f"{key}[one]: omits placeholder, check it is spelled out")
                        continue
                    report.error(where, f"{key}[{category}]: placeholders {found} differ from English {expected}")
                other = value.get("other", "")
                if other.strip() == base_value["other"].strip() and words(other) > 3:
                    report.warn(where, f"{key}: identical to English")
    return checked


def main():
    report = Report()
    total = 0
    strict = "--allow-missing-locales" not in sys.argv
    for root in RESOURCE_ROOTS:
        total += check_root(root, report, strict)
    for line in report.warnings:
        print(line)
    for line in report.errors:
        print(line)
    print(f"checked {total} locale files, {len(report.errors)} errors, {len(report.warnings)} warnings")
    return 1 if report.errors else 0


if __name__ == "__main__":
    sys.exit(main())
