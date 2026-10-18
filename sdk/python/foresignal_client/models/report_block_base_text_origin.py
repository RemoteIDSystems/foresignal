from enum import StrEnum


class ReportBlockBaseTextOrigin(StrEnum):
    AUTHOR = "author"
    DETERMINISTIC = "deterministic"
    LLM = "llm"

    def __str__(self) -> str:
        return str(self.value)
