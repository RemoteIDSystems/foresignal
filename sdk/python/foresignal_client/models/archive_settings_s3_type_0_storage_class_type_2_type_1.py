from enum import StrEnum


class ArchiveSettingsS3Type0StorageClassType2Type1(StrEnum):
    DEEP_ARCHIVE = "DEEP_ARCHIVE"
    GLACIER = "GLACIER"
    GLACIER_IR = "GLACIER_IR"
    INTELLIGENT_TIERING = "INTELLIGENT_TIERING"
    ONEZONE_IA = "ONEZONE_IA"
    REDUCED_REDUNDANCY = "REDUCED_REDUNDANCY"
    STANDARD = "STANDARD"
    STANDARD_IA = "STANDARD_IA"

    def __str__(self) -> str:
        return str(self.value)
