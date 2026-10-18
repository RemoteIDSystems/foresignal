from enum import StrEnum


class AccessRequestGrantType0DeliveredAs(StrEnum):
    ACL = "acl"
    CHRONICLE_EXTRACT = "chronicle-extract"
    REPORT = "report"

    def __str__(self) -> str:
        return str(self.value)
