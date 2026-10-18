from enum import StrEnum


class IcdTerm(StrEnum):
    ALMOST_CERTAIN = "almost certain"
    ALMOST_NO_CHANCE = "almost no chance"
    LIKELY = "likely"
    ROUGHLY_EVEN_CHANCE = "roughly even chance"
    UNLIKELY = "unlikely"
    VERY_LIKELY = "very likely"
    VERY_UNLIKELY = "very unlikely"

    def __str__(self) -> str:
        return str(self.value)
