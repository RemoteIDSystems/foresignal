from enum import StrEnum


class SituationTheme(StrEnum):
    AVIATION = "aviation"
    CONFLICT = "conflict"
    CYBER = "cyber"
    HEALTH = "health"
    INFRASTRUCTURE = "infrastructure"
    MARITIME = "maritime"
    MARKETS = "markets"
    MIXED = "mixed"
    SPACE = "space"
    WEATHER = "weather"

    def __str__(self) -> str:
        return str(self.value)
