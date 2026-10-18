from enum import StrEnum


class ProductTimelineMode(StrEnum):
    PRODUCTS = "products"
    SEAMLESS = "seamless"

    def __str__(self) -> str:
        return str(self.value)
