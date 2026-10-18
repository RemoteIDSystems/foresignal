from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.ask_scope import AskScope


T = TypeVar("T", bound="AskRequest")


@_attrs_define
class AskRequest:
    """
    Attributes:
        question (str):
        thread_id (None | Unset | UUID):
        scope (AskScope | Unset):
        allow_actions (bool | Unset):  Default: False.
    """

    question: str
    thread_id: None | Unset | UUID = UNSET
    scope: AskScope | Unset = UNSET
    allow_actions: bool | Unset = False
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        question = self.question

        thread_id: None | str | Unset
        if isinstance(self.thread_id, Unset):
            thread_id = UNSET
        elif isinstance(self.thread_id, UUID):
            thread_id = str(self.thread_id)
        else:
            thread_id = self.thread_id

        scope: dict[str, Any] | Unset = UNSET
        if not isinstance(self.scope, Unset):
            scope = self.scope.to_dict()

        allow_actions = self.allow_actions

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "question": question,
            }
        )
        if thread_id is not UNSET:
            field_dict["threadId"] = thread_id
        if scope is not UNSET:
            field_dict["scope"] = scope
        if allow_actions is not UNSET:
            field_dict["allowActions"] = allow_actions

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.ask_scope import AskScope

        d = dict(src_dict)
        question = d.pop("question")

        def _parse_thread_id(data: object) -> None | Unset | UUID:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                thread_id_type_0 = UUID(data)

                return thread_id_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Unset | UUID, data)

        thread_id = _parse_thread_id(d.pop("threadId", UNSET))

        _scope = d.pop("scope", UNSET)
        scope: AskScope | Unset
        if isinstance(_scope, Unset):
            scope = UNSET
        else:
            scope = AskScope.from_dict(_scope)

        allow_actions = d.pop("allowActions", UNSET)

        ask_request = cls(
            question=question,
            thread_id=thread_id,
            scope=scope,
            allow_actions=allow_actions,
        )

        ask_request.additional_properties = d
        return ask_request

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
