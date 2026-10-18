from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="AnalogQuery")


@_attrs_define
class AnalogQuery:
    """Region-period analog query (no situation needed)

    Attributes:
        region_code (str): 040 region code: ISO 3166-1 alpha-2 country, `CC-admin1`, or `ZZ` for untagged Example: YE.
        from_ (datetime.datetime):
        to (datetime.datetime):
        k (int | Unset):  Default: 10.
        outcome (str | Unset): Target outcome: `critical` (situation reaches critical) or `{family}/P{n}D` (a family
            within n days), e.g. `conflict:battles/P30D`
    """

    region_code: str
    from_: datetime.datetime
    to: datetime.datetime
    k: int | Unset = 10
    outcome: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        region_code = self.region_code

        from_ = self.from_.isoformat()

        to = self.to.isoformat()

        k = self.k

        outcome = self.outcome

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "regionCode": region_code,
                "from": from_,
                "to": to,
            }
        )
        if k is not UNSET:
            field_dict["k"] = k
        if outcome is not UNSET:
            field_dict["outcome"] = outcome

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        region_code = d.pop("regionCode")

        from_ = datetime.datetime.fromisoformat(d.pop("from"))

        to = datetime.datetime.fromisoformat(d.pop("to"))

        k = d.pop("k", UNSET)

        outcome = d.pop("outcome", UNSET)

        analog_query = cls(
            region_code=region_code,
            from_=from_,
            to=to,
            k=k,
            outcome=outcome,
        )

        analog_query.additional_properties = d
        return analog_query

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
