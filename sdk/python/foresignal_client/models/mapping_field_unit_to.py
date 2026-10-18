from enum import StrEnum


class MappingFieldUnitTo(StrEnum):
    CEL = "Cel"
    HPA = "hPa"
    KM = "km"
    KMH = "km/h"
    M = "m"
    MM = "mm"
    MMH = "mm/h"
    MS = "m/s"
    PA = "Pa"
    USVH = "uSv/h"
    VALUE_0 = "[degF]"
    VALUE_10 = "[in_i'Hg]"
    VALUE_13 = "[mi_i]"
    VALUE_15 = "[ft_i]"
    VALUE_18 = "%"
    VALUE_2 = "[mi_i]/h"
    VALUE_20 = "/min"
    VALUE_4 = "[kn_i]"
    VALUE_6 = "[in_i]"
    VALUE_8 = "[in_i]/h"
    WM2 = "W/m2"

    def __str__(self) -> str:
        return str(self.value)
