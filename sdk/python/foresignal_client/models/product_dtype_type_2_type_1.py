from enum import StrEnum


class ProductDtypeType2Type1(StrEnum):
    F32 = "f32"
    I16 = "i16"
    RGBA8 = "rgba8"
    U16 = "u16"
    U8 = "u8"

    def __str__(self) -> str:
        return str(self.value)
