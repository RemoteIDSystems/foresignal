from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="SituationLinkerTestRequest")


@_attrs_define
class SituationLinkerTestRequest:
    """Seq range for a linker dry run

    Attributes:
        from_seq (int):
        to_seq (int):
    """

    from_seq: int
    to_seq: int
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from_seq = self.from_seq

        to_seq = self.to_seq

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "fromSeq": from_seq,
                "toSeq": to_seq,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        from_seq = d.pop("fromSeq")

        to_seq = d.pop("toSeq")

        situation_linker_test_request = cls(
            from_seq=from_seq,
            to_seq=to_seq,
        )

        situation_linker_test_request.additional_properties = d
        return situation_linker_test_request

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
