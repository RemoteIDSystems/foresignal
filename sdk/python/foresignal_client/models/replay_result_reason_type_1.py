from enum import StrEnum


class ReplayResultReasonType1(StrEnum):
    COMPONENT_REMOVED = "component-removed"
    INPUTS_PURGED = "inputs-purged"
    LLM_OUTPUT = "llm-output"

    def __str__(self) -> str:
        return str(self.value)
