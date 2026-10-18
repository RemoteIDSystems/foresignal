from enum import StrEnum


class CorrelationRecipeDefinitionJoinsItemSameRegion(StrEnum):
    ADMIN1 = "admin1"
    COUNTRY = "country"

    def __str__(self) -> str:
        return str(self.value)
