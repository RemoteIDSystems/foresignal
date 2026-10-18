from enum import StrEnum


class AskResponseMode(StrEnum):
    RETRIEVAL = "retrieval"
    TOOLS = "tools"

    def __str__(self) -> str:
        return str(self.value)
