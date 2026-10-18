from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="PromptTemplate")


@_attrs_define
class PromptTemplate:
    """A versioned LLM prompt template (src/main/resources/llm/templates/{id}@{version}.md) (116)

    Attributes:
        id (str):
        version (str):
        sha256 (str):
        text (str):
        role (None | str | Unset):
        versions (list[str] | Unset): All available versions
        instructions (None | str | Unset): Operator instructions; returned to agent scope only
    """

    id: str
    version: str
    sha256: str
    text: str
    role: None | str | Unset = UNSET
    versions: list[str] | Unset = UNSET
    instructions: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        version = self.version

        sha256 = self.sha256

        text = self.text

        role: None | str | Unset
        if isinstance(self.role, Unset):
            role = UNSET
        else:
            role = self.role

        versions: list[str] | Unset = UNSET
        if not isinstance(self.versions, Unset):
            versions = self.versions

        instructions: None | str | Unset
        if isinstance(self.instructions, Unset):
            instructions = UNSET
        else:
            instructions = self.instructions

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "version": version,
                "sha256": sha256,
                "text": text,
            }
        )
        if role is not UNSET:
            field_dict["role"] = role
        if versions is not UNSET:
            field_dict["versions"] = versions
        if instructions is not UNSET:
            field_dict["instructions"] = instructions

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        id = d.pop("id")

        version = d.pop("version")

        sha256 = d.pop("sha256")

        text = d.pop("text")

        def _parse_role(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        role = _parse_role(d.pop("role", UNSET))

        versions = cast(list[str], d.pop("versions", UNSET))

        def _parse_instructions(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        instructions = _parse_instructions(d.pop("instructions", UNSET))

        prompt_template = cls(
            id=id,
            version=version,
            sha256=sha256,
            text=text,
            role=role,
            versions=versions,
            instructions=instructions,
        )

        prompt_template.additional_properties = d
        return prompt_template

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
