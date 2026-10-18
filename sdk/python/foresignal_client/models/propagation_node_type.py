from enum import StrEnum


class PropagationNodeType(StrEnum):
    EVIDENCE_GROUP = "evidence-group"
    NEWS_ARTICLE = "news-article"
    OFFICIAL_RECORD = "official-record"
    POST = "post"

    def __str__(self) -> str:
        return str(self.value)
