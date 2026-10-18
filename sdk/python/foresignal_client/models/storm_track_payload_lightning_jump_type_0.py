from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import (
    Any,
    Literal,
    Self,
    TypeVar,
    cast,
)

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="StormTrackPayloadLightningJumpType0")


@_attrs_define
class StormTrackPayloadLightningJumpType0:
    """GLM lightning jump (advisory)

    Attributes:
        at (datetime.datetime):
        method (Literal['2sigma-glm-v1']):
        sigma (float | Unset):
        rate_fpm (float | Unset):
    """

    at: datetime.datetime
    method: Literal["2sigma-glm-v1"]
    sigma: float | Unset = UNSET
    rate_fpm: float | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        at = self.at.isoformat()

        method = self.method

        sigma = self.sigma

        rate_fpm = self.rate_fpm

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "at": at,
                "method": method,
            }
        )
        if sigma is not UNSET:
            field_dict["sigma"] = sigma
        if rate_fpm is not UNSET:
            field_dict["rateFpm"] = rate_fpm

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        at = datetime.datetime.fromisoformat(d.pop("at"))

        method = cast(Literal["2sigma-glm-v1"], d.pop("method"))
        if method != "2sigma-glm-v1":
            raise ValueError(f"method must match const '2sigma-glm-v1', got '{method}'")

        sigma = d.pop("sigma", UNSET)

        rate_fpm = d.pop("rateFpm", UNSET)

        storm_track_payload_lightning_jump_type_0 = cls(
            at=at,
            method=method,
            sigma=sigma,
            rate_fpm=rate_fpm,
        )

        storm_track_payload_lightning_jump_type_0.additional_properties = d
        return storm_track_payload_lightning_jump_type_0

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
