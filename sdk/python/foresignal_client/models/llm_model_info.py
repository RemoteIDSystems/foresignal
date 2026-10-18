from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="LlmModelInfo")


@_attrs_define
class LlmModelInfo:
    """
    Attributes:
        id (str):
        display_name (None | str | Unset):
        context_window (int | None | Unset):
        supports_tools (bool | None | Unset):
        supports_embeddings (bool | None | Unset):
    """

    id: str
    display_name: None | str | Unset = UNSET
    context_window: int | None | Unset = UNSET
    supports_tools: bool | None | Unset = UNSET
    supports_embeddings: bool | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        display_name: None | str | Unset
        if isinstance(self.display_name, Unset):
            display_name = UNSET
        else:
            display_name = self.display_name

        context_window: int | None | Unset
        if isinstance(self.context_window, Unset):
            context_window = UNSET
        else:
            context_window = self.context_window

        supports_tools: bool | None | Unset
        if isinstance(self.supports_tools, Unset):
            supports_tools = UNSET
        else:
            supports_tools = self.supports_tools

        supports_embeddings: bool | None | Unset
        if isinstance(self.supports_embeddings, Unset):
            supports_embeddings = UNSET
        else:
            supports_embeddings = self.supports_embeddings

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
            }
        )
        if display_name is not UNSET:
            field_dict["displayName"] = display_name
        if context_window is not UNSET:
            field_dict["contextWindow"] = context_window
        if supports_tools is not UNSET:
            field_dict["supportsTools"] = supports_tools
        if supports_embeddings is not UNSET:
            field_dict["supportsEmbeddings"] = supports_embeddings

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        id = d.pop("id")

        def _parse_display_name(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        display_name = _parse_display_name(d.pop("displayName", UNSET))

        def _parse_context_window(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        context_window = _parse_context_window(d.pop("contextWindow", UNSET))

        def _parse_supports_tools(data: object) -> bool | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(bool | None | Unset, data)

        supports_tools = _parse_supports_tools(d.pop("supportsTools", UNSET))

        def _parse_supports_embeddings(data: object) -> bool | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(bool | None | Unset, data)

        supports_embeddings = _parse_supports_embeddings(
            d.pop("supportsEmbeddings", UNSET)
        )

        llm_model_info = cls(
            id=id,
            display_name=display_name,
            context_window=context_window,
            supports_tools=supports_tools,
            supports_embeddings=supports_embeddings,
        )

        llm_model_info.additional_properties = d
        return llm_model_info

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
