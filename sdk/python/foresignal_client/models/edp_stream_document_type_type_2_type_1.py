from enum import StrEnum


class EdpStreamDocumentTypeType2Type1(StrEnum):
    ARTICLE = "article"
    NOTE = "note"
    REPORT = "report"
    SOCIAL_POST = "social-post"
    TRANSCRIPT = "transcript"

    def __str__(self) -> str:
        return str(self.value)
