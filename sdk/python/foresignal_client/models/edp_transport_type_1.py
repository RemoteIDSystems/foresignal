from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.edp_pull import EdpPull


T = TypeVar("T", bound="EdpTransportType1")


@_attrs_define
class EdpTransportType1:
    """
    Attributes:
        pull (EdpPull): Provider items endpoint Foresignal polls
    """

    pull: EdpPull
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        pull = self.pull.to_dict()

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "pull": pull,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.edp_pull import EdpPull

        d = dict(src_dict)
        pull = EdpPull.from_dict(d.pop("pull"))

        edp_transport_type_1 = cls(
            pull=pull,
        )

        edp_transport_type_1.additional_properties = d
        return edp_transport_type_1

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
