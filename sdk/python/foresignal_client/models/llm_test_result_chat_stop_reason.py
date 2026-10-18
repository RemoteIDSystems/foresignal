from enum import StrEnum


class LlmTestResultChatStopReason(StrEnum):
    END = "end"
    ERROR = "error"
    MAX_TOKENS = "max_tokens"
    REFUSAL = "refusal"
    TOOL_USE = "tool_use"

    def __str__(self) -> str:
        return str(self.value)
