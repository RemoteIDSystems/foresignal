from enum import StrEnum


class CoverageStatsScope(StrEnum):
    HYPOTHESIS = "hypothesis"
    SITUATION = "situation"
    STORY = "story"
    TOPIC = "topic"

    def __str__(self) -> str:
        return str(self.value)
