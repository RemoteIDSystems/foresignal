from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.chronicle_activate_request_backfill_type_0 import (
        ChronicleActivateRequestBackfillType0,
    )


T = TypeVar("T", bound="ChronicleActivateRequest")


@_attrs_define
class ChronicleActivateRequest:
    """Activation options

    Attributes:
        backfill (ChronicleActivateRequestBackfillType0 | None | Unset):
    """

    backfill: ChronicleActivateRequestBackfillType0 | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.chronicle_activate_request_backfill_type_0 import (
            ChronicleActivateRequestBackfillType0,
        )

        backfill: dict[str, Any] | None | Unset
        if isinstance(self.backfill, Unset):
            backfill = UNSET
        elif isinstance(self.backfill, ChronicleActivateRequestBackfillType0):
            backfill = self.backfill.to_dict()
        else:
            backfill = self.backfill

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if backfill is not UNSET:
            field_dict["backfill"] = backfill

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.chronicle_activate_request_backfill_type_0 import (
            ChronicleActivateRequestBackfillType0,
        )

        d = dict(src_dict)

        def _parse_backfill(
            data: object,
        ) -> ChronicleActivateRequestBackfillType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                backfill_type_0 = ChronicleActivateRequestBackfillType0.from_dict(data)

                return backfill_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(ChronicleActivateRequestBackfillType0 | None | Unset, data)

        backfill = _parse_backfill(d.pop("backfill", UNSET))

        chronicle_activate_request = cls(
            backfill=backfill,
        )

        chronicle_activate_request.additional_properties = d
        return chronicle_activate_request

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
