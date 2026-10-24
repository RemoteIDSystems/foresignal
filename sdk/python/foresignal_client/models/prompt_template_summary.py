from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

T = TypeVar("T", bound="PromptTemplateSummary")


@_attrs_define
class PromptTemplateSummary:
    """One bundled prompt template without its text (listPromptTemplates)

    Attributes:
        id (str):
        role (None | str): The LLM role that sends it
        version (str): The newest version
        versions (list[str]): Every bundled version, oldest first
    """

    id: str
    role: None | str
    version: str
    versions: list[str]
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        role: None | str
        role = self.role

        version = self.version

        versions = self.versions

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "role": role,
                "version": version,
                "versions": versions,
            }
        )

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        id = d.pop("id")

        def _parse_role(data: object) -> None | str:
            if data is None:
                return data
            return cast(None | str, data)

        role = _parse_role(d.pop("role"))

        version = d.pop("version")

        versions = cast(list[str], d.pop("versions"))

        prompt_template_summary = cls(
            id=id,
            role=role,
            version=version,
            versions=versions,
        )

        prompt_template_summary.additional_properties = d
        return prompt_template_summary

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
