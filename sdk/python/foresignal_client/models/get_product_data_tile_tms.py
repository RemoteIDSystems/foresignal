from enum import StrEnum


class GetProductDataTileTms(StrEnum):
    WEBMERCATORQUAD = "WebMercatorQuad"
    WORLDCRS84QUAD = "WorldCRS84Quad"

    def __str__(self) -> str:
        return str(self.value)
