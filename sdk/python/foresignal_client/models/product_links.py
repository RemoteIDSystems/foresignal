from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ProductLinks")


@_attrs_define
class ProductLinks:
    """Fully expanded links (clients never build URLs)

    Attributes:
        self_ (str | Unset):
        frames (str | Unset):
        latest (str | Unset):
        tilejson (str | Unset):
        legend (str | Unset):
        timeline (str | Unset):
    """

    self_: str | Unset = UNSET
    frames: str | Unset = UNSET
    latest: str | Unset = UNSET
    tilejson: str | Unset = UNSET
    legend: str | Unset = UNSET
    timeline: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        self_ = self.self_

        frames = self.frames

        latest = self.latest

        tilejson = self.tilejson

        legend = self.legend

        timeline = self.timeline

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if self_ is not UNSET:
            field_dict["self"] = self_
        if frames is not UNSET:
            field_dict["frames"] = frames
        if latest is not UNSET:
            field_dict["latest"] = latest
        if tilejson is not UNSET:
            field_dict["tilejson"] = tilejson
        if legend is not UNSET:
            field_dict["legend"] = legend
        if timeline is not UNSET:
            field_dict["timeline"] = timeline

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        self_ = d.pop("self", UNSET)

        frames = d.pop("frames", UNSET)

        latest = d.pop("latest", UNSET)

        tilejson = d.pop("tilejson", UNSET)

        legend = d.pop("legend", UNSET)

        timeline = d.pop("timeline", UNSET)

        product_links = cls(
            self_=self_,
            frames=frames,
            latest=latest,
            tilejson=tilejson,
            legend=legend,
            timeline=timeline,
        )

        product_links.additional_properties = d
        return product_links

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
