from enum import StrEnum


class LlmProviderBudgetCurrency(StrEnum):
    USD = "USD"

    def __str__(self) -> str:
        return str(self.value)
