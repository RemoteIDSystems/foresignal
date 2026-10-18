from enum import StrEnum


class AnalystProfileAllowedActionsItem(StrEnum):
    ANNOTATE_EVENT = "annotate_event"
    PROPOSE_CHRONICLE = "propose_chronicle"
    PROPOSE_HYPOTHESIS_SET = "propose_hypothesis_set"
    PROPOSE_INDICATOR = "propose_indicator"
    PROPOSE_INTEL_EVENT = "propose_intel_event"
    PROPOSE_INVESTIGATION_ITEM = "propose_investigation_item"
    PROPOSE_QUESTION = "propose_question"
    PROPOSE_SITUATION_MERGE = "propose_situation_merge"
    PROPOSE_WATCH = "propose_watch"

    def __str__(self) -> str:
        return str(self.value)
