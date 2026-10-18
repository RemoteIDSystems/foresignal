from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.kind_display import KindDisplay


T = TypeVar("T", bound="KindSummary")


@_attrs_define
class KindSummary:
    """
    Attributes:
        kind (str):
        current_count (int):
        sources (list[str]):
        display (KindDisplay | None | Unset):
    """

    kind: str
    current_count: int
    sources: list[str]
    display: KindDisplay | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.kind_display import KindDisplay

        kind = self.kind

        current_count = self.current_count

        sources = self.sources

        display: dict[str, Any] | None | Unset
        if isinstance(self.display, Unset):
            display = UNSET
        elif isinstance(self.display, KindDisplay):
            display = self.display.to_dict()
        else:
            display = self.display

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "kind": kind,
                "currentCount": current_count,
                "sources": sources,
            }
        )
        if display is not UNSET:
            field_dict["display"] = display

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.kind_display import KindDisplay

        d = dict(src_dict)
        kind = d.pop("kind")

        current_count = d.pop("currentCount")

        sources = cast(list[str], d.pop("sources"))

        def _parse_display(data: object) -> KindDisplay | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                display_type_0 = KindDisplay.from_dict(data)

                return display_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(KindDisplay | None | Unset, data)

        display = _parse_display(d.pop("display", UNSET))

        kind_summary = cls(
            kind=kind,
            current_count=current_count,
            sources=sources,
            display=display,
        )

        kind_summary.additional_properties = d
        return kind_summary

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
