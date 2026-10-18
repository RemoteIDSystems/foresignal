from enum import StrEnum


class ReferenceImportRequestDataset(StrEnum):
    ACLED = "acled"
    GDACS = "gdacs"
    UCDP_GED = "ucdp-ged"
    USGS_COMCAT = "usgs-comcat"

    def __str__(self) -> str:
        return str(self.value)
