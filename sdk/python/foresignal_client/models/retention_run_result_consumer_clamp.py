from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="RetentionRunResultConsumerClamp")


@_attrs_define
class RetentionRunResultConsumerClamp:
    """
    Attributes:
        active (bool | Unset):
        consumer (None | str | Unset):
        cursor_seq (int | None | Unset):
    """

    active: bool | Unset = UNSET
    consumer: None | str | Unset = UNSET
    cursor_seq: int | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        active = self.active

        consumer: None | str | Unset
        if isinstance(self.consumer, Unset):
            consumer = UNSET
        else:
            consumer = self.consumer

        cursor_seq: int | None | Unset
        if isinstance(self.cursor_seq, Unset):
            cursor_seq = UNSET
        else:
            cursor_seq = self.cursor_seq

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if active is not UNSET:
            field_dict["active"] = active
        if consumer is not UNSET:
            field_dict["consumer"] = consumer
        if cursor_seq is not UNSET:
            field_dict["cursorSeq"] = cursor_seq

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        active = d.pop("active", UNSET)

        def _parse_consumer(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        consumer = _parse_consumer(d.pop("consumer", UNSET))

        def _parse_cursor_seq(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        cursor_seq = _parse_cursor_seq(d.pop("cursorSeq", UNSET))

        retention_run_result_consumer_clamp = cls(
            active=active,
            consumer=consumer,
            cursor_seq=cursor_seq,
        )

        retention_run_result_consumer_clamp.additional_properties = d
        return retention_run_result_consumer_clamp

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
