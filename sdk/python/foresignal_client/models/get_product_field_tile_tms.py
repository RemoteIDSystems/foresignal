from enum import StrEnum


class GetProductFieldTileTms(StrEnum):
    WEBMERCATORQUAD = "WebMercatorQuad"
    WORLDCRS84QUAD = "WorldCRS84Quad"

    def __str__(self) -> str:
        return str(self.value)
