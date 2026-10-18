from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.edp_source_info_originator_class import EdpSourceInfoOriginatorClass
from ..types import UNSET, Unset

T = TypeVar("T", bound="EdpSourceInfoOriginator")


@_attrs_define
class EdpSourceInfoOriginator:
    """
    Attributes:
        id (str):
        class_ (EdpSourceInfoOriginatorClass):
        aggregates (list[str] | Unset):
    """

    id: str
    class_: EdpSourceInfoOriginatorClass
    aggregates: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        class_ = self.class_.value

        aggregates: list[str] | Unset = UNSET
        if not isinstance(self.aggregates, Unset):
            aggregates = self.aggregates

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "class": class_,
            }
        )
        if aggregates is not UNSET:
            field_dict["aggregates"] = aggregates

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        id = d.pop("id")

        class_ = EdpSourceInfoOriginatorClass(d.pop("class"))

        aggregates = cast(list[str], d.pop("aggregates", UNSET))

        edp_source_info_originator = cls(
            id=id,
            class_=class_,
            aggregates=aggregates,
        )

        edp_source_info_originator.additional_properties = d
        return edp_source_info_originator

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
