from enum import StrEnum


class NewsSubscriptionType(StrEnum):
    LANGUAGE = "language"
    OUTLET = "outlet"

    def __str__(self) -> str:
        return str(self.value)
