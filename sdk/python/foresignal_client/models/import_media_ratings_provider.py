from enum import StrEnum


class ImportMediaRatingsProvider(StrEnum):
    ALLSIDES = "allsides"
    MBFC = "mbfc"
    OPERATOR = "operator"
    PARTISAN_WEB = "partisan-web"
    WIKIDATA = "wikidata"

    def __str__(self) -> str:
        return str(self.value)
