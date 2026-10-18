from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.dossier_sections_timeline_items_item_kind import (
    DossierSectionsTimelineItemsItemKind,
)
from ..types import UNSET, Unset

T = TypeVar("T", bound="DossierSectionsTimelineItemsItem")


@_attrs_define
class DossierSectionsTimelineItemsItem:
    """Timeline item

    Attributes:
        at (datetime.datetime):
        kind (DossierSectionsTimelineItemsItemKind):
        title (str):
        ref (None | str | Unset):
        severity (None | str | Unset):
    """

    at: datetime.datetime
    kind: DossierSectionsTimelineItemsItemKind
    title: str
    ref: None | str | Unset = UNSET
    severity: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        at = self.at.isoformat()

        kind = self.kind.value

        title = self.title

        ref: None | str | Unset
        if isinstance(self.ref, Unset):
            ref = UNSET
        else:
            ref = self.ref

        severity: None | str | Unset
        if isinstance(self.severity, Unset):
            severity = UNSET
        else:
            severity = self.severity

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "at": at,
                "kind": kind,
                "title": title,
            }
        )
        if ref is not UNSET:
            field_dict["ref"] = ref
        if severity is not UNSET:
            field_dict["severity"] = severity

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        at = datetime.datetime.fromisoformat(d.pop("at"))

        kind = DossierSectionsTimelineItemsItemKind(d.pop("kind"))

        title = d.pop("title")

        def _parse_ref(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        ref = _parse_ref(d.pop("ref", UNSET))

        def _parse_severity(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        severity = _parse_severity(d.pop("severity", UNSET))

        dossier_sections_timeline_items_item = cls(
            at=at,
            kind=kind,
            title=title,
            ref=ref,
            severity=severity,
        )

        dossier_sections_timeline_items_item.additional_properties = d
        return dossier_sections_timeline_items_item

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
