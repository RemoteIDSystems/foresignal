from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define

from ..types import UNSET, Unset

T = TypeVar("T", bound="BriefingInputSite")


@_attrs_define
class BriefingInputSite:
    """The hosted edition site (NP-005)

    Attributes:
        enabled (bool | Unset): Serve the latest successful run at `getEditionSite` Default: False.
        shareable (bool | Unset): Keep a read-only, revocable share link to the site Default: False.
    """

    enabled: bool | Unset = False
    shareable: bool | Unset = False

    def to_dict(self) -> dict[str, Any]:
        enabled = self.enabled

        shareable = self.shareable

        field_dict: dict[str, Any] = {}

        field_dict.update({})
        if enabled is not UNSET:
            field_dict["enabled"] = enabled
        if shareable is not UNSET:
            field_dict["shareable"] = shareable

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        enabled = d.pop("enabled", UNSET)

        shareable = d.pop("shareable", UNSET)

        briefing_input_site = cls(
            enabled=enabled,
            shareable=shareable,
        )

        return briefing_input_site
