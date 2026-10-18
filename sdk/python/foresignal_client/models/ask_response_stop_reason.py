from enum import StrEnum


class AskResponseStopReason(StrEnum):
    END = "end"
    ERROR = "error"
    MAX_TOKENS = "max_tokens"
    REFUSAL = "refusal"

    def __str__(self) -> str:
        return str(self.value)
