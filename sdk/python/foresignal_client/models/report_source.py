from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.report_source_type import ReportSourceType
from ..types import UNSET, Unset

T = TypeVar("T", bound="ReportSource")


@_attrs_define
class ReportSource:
    """What a report is built from

    Attributes:
        type_ (ReportSourceType):
        ref (None | str | Unset):
        refs (list[str] | Unset): For `selection`
    """

    type_: ReportSourceType
    ref: None | str | Unset = UNSET
    refs: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        type_ = self.type_.value

        ref: None | str | Unset
        if isinstance(self.ref, Unset):
            ref = UNSET
        else:
            ref = self.ref

        refs: list[str] | Unset = UNSET
        if not isinstance(self.refs, Unset):
            refs = self.refs

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "type": type_,
            }
        )
        if ref is not UNSET:
            field_dict["ref"] = ref
        if refs is not UNSET:
            field_dict["refs"] = refs

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        type_ = ReportSourceType(d.pop("type"))

        def _parse_ref(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        ref = _parse_ref(d.pop("ref", UNSET))

        refs = cast(list[str], d.pop("refs", UNSET))

        report_source = cls(
            type_=type_,
            ref=ref,
            refs=refs,
        )

        report_source.additional_properties = d
        return report_source

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
