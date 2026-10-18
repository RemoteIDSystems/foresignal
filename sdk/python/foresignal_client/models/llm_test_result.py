from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.llm_test_result_chat import LlmTestResultChat
    from ..models.llm_test_result_embedding_type_0 import LlmTestResultEmbeddingType0


T = TypeVar("T", bound="LlmTestResult")


@_attrs_define
class LlmTestResult:
    """
    Attributes:
        success (bool):
        chat (LlmTestResultChat | Unset): Chat probe result
        embedding (LlmTestResultEmbeddingType0 | None | Unset):
        error (None | str | Unset):
        refusal_category (None | str | Unset):
    """

    success: bool
    chat: LlmTestResultChat | Unset = UNSET
    embedding: LlmTestResultEmbeddingType0 | None | Unset = UNSET
    error: None | str | Unset = UNSET
    refusal_category: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.llm_test_result_embedding_type_0 import (
            LlmTestResultEmbeddingType0,
        )

        success = self.success

        chat: dict[str, Any] | Unset = UNSET
        if not isinstance(self.chat, Unset):
            chat = self.chat.to_dict()

        embedding: dict[str, Any] | None | Unset
        if isinstance(self.embedding, Unset):
            embedding = UNSET
        elif isinstance(self.embedding, LlmTestResultEmbeddingType0):
            embedding = self.embedding.to_dict()
        else:
            embedding = self.embedding

        error: None | str | Unset
        if isinstance(self.error, Unset):
            error = UNSET
        else:
            error = self.error

        refusal_category: None | str | Unset
        if isinstance(self.refusal_category, Unset):
            refusal_category = UNSET
        else:
            refusal_category = self.refusal_category

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "success": success,
            }
        )
        if chat is not UNSET:
            field_dict["chat"] = chat
        if embedding is not UNSET:
            field_dict["embedding"] = embedding
        if error is not UNSET:
            field_dict["error"] = error
        if refusal_category is not UNSET:
            field_dict["refusalCategory"] = refusal_category

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.llm_test_result_chat import LlmTestResultChat
        from ..models.llm_test_result_embedding_type_0 import (
            LlmTestResultEmbeddingType0,
        )

        d = dict(src_dict)
        success = d.pop("success")

        _chat = d.pop("chat", UNSET)
        chat: LlmTestResultChat | Unset
        if isinstance(_chat, Unset):
            chat = UNSET
        else:
            chat = LlmTestResultChat.from_dict(_chat)

        def _parse_embedding(
            data: object,
        ) -> LlmTestResultEmbeddingType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                embedding_type_0 = LlmTestResultEmbeddingType0.from_dict(data)

                return embedding_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(LlmTestResultEmbeddingType0 | None | Unset, data)

        embedding = _parse_embedding(d.pop("embedding", UNSET))

        def _parse_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        error = _parse_error(d.pop("error", UNSET))

        def _parse_refusal_category(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        refusal_category = _parse_refusal_category(d.pop("refusalCategory", UNSET))

        llm_test_result = cls(
            success=success,
            chat=chat,
            embedding=embedding,
            error=error,
            refusal_category=refusal_category,
        )

        llm_test_result.additional_properties = d
        return llm_test_result

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
