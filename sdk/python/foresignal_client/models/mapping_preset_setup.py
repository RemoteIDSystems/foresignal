from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="MappingPresetSetup")


@_attrs_define
class MappingPresetSetup:
    """Device-side setup for a preset: instructions and the exact ingest URL (059). On a preset's catalogue entry `url` is
    a template; on creation it is concrete and contains the key once.

        Attributes:
            instructions (str): Markdown setup steps for the device
            url_template (str):  Example: {publicBaseUrl}/v1/ingest/{sourceId}/k/{key}/ecowitt.
            url (None | str | Unset): Concrete URL (creation response only; shown once)
    """

    instructions: str
    url_template: str
    url: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        instructions = self.instructions

        url_template = self.url_template

        url: None | str | Unset
        if isinstance(self.url, Unset):
            url = UNSET
        else:
            url = self.url

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "instructions": instructions,
                "urlTemplate": url_template,
            }
        )
        if url is not UNSET:
            field_dict["url"] = url

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        instructions = d.pop("instructions")

        url_template = d.pop("urlTemplate")

        def _parse_url(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        url = _parse_url(d.pop("url", UNSET))

        mapping_preset_setup = cls(
            instructions=instructions,
            url_template=url_template,
            url=url,
        )

        mapping_preset_setup.additional_properties = d
        return mapping_preset_setup

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
