from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import (
    TYPE_CHECKING,
    Any,
    Literal,
    Self,
    TypeVar,
    cast,
)

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.investigation import Investigation


T = TypeVar("T", bound="InvestigationExport")


@_attrs_define
class InvestigationExport:
    """JSON export of an investigation

    Attributes:
        header (Literal['Automated analysis inputs; verify before acting']):
        generated_at (datetime.datetime):
        investigation (Investigation): An operator case file (renamed from 'case'); 130 adds owners, access lists, TLP
            and links
        attributions (list[str]):
    """

    header: Literal["Automated analysis inputs; verify before acting"]
    generated_at: datetime.datetime
    investigation: Investigation
    attributions: list[str]
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        header = self.header

        generated_at = self.generated_at.isoformat()

        investigation = self.investigation.to_dict()

        attributions = self.attributions

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "header": header,
                "generatedAt": generated_at,
                "investigation": investigation,
                "attributions": attributions,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.investigation import Investigation

        d = dict(src_dict)
        header = cast(
            Literal["Automated analysis inputs; verify before acting"], d.pop("header")
        )
        if header != "Automated analysis inputs; verify before acting":
            raise ValueError(
                f"header must match const 'Automated analysis inputs; verify before acting', got '{header}'"
            )

        generated_at = datetime.datetime.fromisoformat(d.pop("generatedAt"))

        investigation = Investigation.from_dict(d.pop("investigation"))

        attributions = cast(list[str], d.pop("attributions"))

        investigation_export = cls(
            header=header,
            generated_at=generated_at,
            investigation=investigation,
            attributions=attributions,
        )

        investigation_export.additional_properties = d
        return investigation_export

    @property
    def additional_keys(self) -> list[str]:
        return list(self.additional_properties.keys())

    def __getitem__(self, key: str) -> Any:
        return self.additional_properties[key]

    def __setitem__(self, key: str, value: Any) -> None:
        self.additional_properties[key] = value

    def __delitem__(self, key: str) -> None:
        del self.additional_properties[key]

    def __contains__(self, key: str) -> bool:
        return key in self.additional_properties
