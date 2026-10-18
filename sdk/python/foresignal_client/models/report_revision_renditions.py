from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="ReportRevisionRenditions")


@_attrs_define
class ReportRevisionRenditions:
    """Rendition links

    Attributes:
        markdown (str | Unset):
        html (str | Unset):
        json (str | Unset):
    """

    markdown: str | Unset = UNSET
    html: str | Unset = UNSET
    json: str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        markdown = self.markdown

        html = self.html

        json = self.json

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update({})
        if markdown is not UNSET:
            field_dict["markdown"] = markdown
        if html is not UNSET:
            field_dict["html"] = html
        if json is not UNSET:
            field_dict["json"] = json

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        markdown = d.pop("markdown", UNSET)

        html = d.pop("html", UNSET)

        json = d.pop("json", UNSET)

        report_revision_renditions = cls(
            markdown=markdown,
            html=html,
            json=json,
        )

        report_revision_renditions.additional_properties = d
        return report_revision_renditions

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
