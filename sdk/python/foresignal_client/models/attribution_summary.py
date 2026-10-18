from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

if TYPE_CHECKING:
    from ..models.attribution import Attribution


T = TypeVar("T", bound="AttributionSummary")


@_attrs_define
class AttributionSummary:
    """Aggregated attributions for a scope, for SSE clients and map footers (109)

    Attributes:
        attributions (list[Attribution]): One entry per source and licence with the number of matching current records
        text (str): Joined attributionText (at most 1 KB, truncated with …), the value of the Foresignal-Attribution
            header
    """

    attributions: list[Attribution]
    text: str
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        attributions = []
        for attributions_item_data in self.attributions:
            attributions_item = attributions_item_data.to_dict()
            attributions.append(attributions_item)

        text = self.text

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "attributions": attributions,
                "text": text,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.attribution import Attribution

        d = dict(src_dict)
        attributions = []
        _attributions = d.pop("attributions")
        for attributions_item_data in _attributions:
            attributions_item = Attribution.from_dict(attributions_item_data)

            attributions.append(attributions_item)

        text = d.pop("text")

        attribution_summary = cls(
            attributions=attributions,
            text=text,
        )

        attribution_summary.additional_properties = d
        return attribution_summary

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
