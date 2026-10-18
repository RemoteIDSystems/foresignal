from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.ask_action_status import AskActionStatus
from ..models.ask_action_type import AskActionType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.ask_action_payload import AskActionPayload
    from ..models.ask_action_result_type_0 import AskActionResultType0


T = TypeVar("T", bound="AskAction")


@_attrs_define
class AskAction:
    """
    Attributes:
        id (UUID):
        type_ (AskActionType):
        description (str):
        status (AskActionStatus):
        payload (AskActionPayload | Unset):
        result (AskActionResultType0 | None | Unset):
        created_at (datetime.datetime | Unset):
        executed_at (datetime.datetime | None | Unset):
    """

    id: UUID
    type_: AskActionType
    description: str
    status: AskActionStatus
    payload: AskActionPayload | Unset = UNSET
    result: AskActionResultType0 | None | Unset = UNSET
    created_at: datetime.datetime | Unset = UNSET
    executed_at: datetime.datetime | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.ask_action_result_type_0 import AskActionResultType0

        id = str(self.id)

        type_ = self.type_.value

        description = self.description

        status = self.status.value

        payload: dict[str, Any] | Unset = UNSET
        if not isinstance(self.payload, Unset):
            payload = self.payload.to_dict()

        result: dict[str, Any] | None | Unset
        if isinstance(self.result, Unset):
            result = UNSET
        elif isinstance(self.result, AskActionResultType0):
            result = self.result.to_dict()
        else:
            result = self.result

        created_at: str | Unset = UNSET
        if not isinstance(self.created_at, Unset):
            created_at = self.created_at.isoformat()

        executed_at: None | str | Unset
        if isinstance(self.executed_at, Unset):
            executed_at = UNSET
        elif isinstance(self.executed_at, datetime.datetime):
            executed_at = self.executed_at.isoformat()
        else:
            executed_at = self.executed_at

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "type": type_,
                "description": description,
                "status": status,
            }
        )
        if payload is not UNSET:
            field_dict["payload"] = payload
        if result is not UNSET:
            field_dict["result"] = result
        if created_at is not UNSET:
            field_dict["createdAt"] = created_at
        if executed_at is not UNSET:
            field_dict["executedAt"] = executed_at

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.ask_action_payload import AskActionPayload
        from ..models.ask_action_result_type_0 import AskActionResultType0

        d = dict(src_dict)
        id = UUID(d.pop("id"))

        type_ = AskActionType(d.pop("type"))

        description = d.pop("description")

        status = AskActionStatus(d.pop("status"))

        _payload = d.pop("payload", UNSET)
        payload: AskActionPayload | Unset
        if isinstance(_payload, Unset):
            payload = UNSET
        else:
            payload = AskActionPayload.from_dict(_payload)

        def _parse_result(data: object) -> AskActionResultType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                result_type_0 = AskActionResultType0.from_dict(data)

                return result_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(AskActionResultType0 | None | Unset, data)

        result = _parse_result(d.pop("result", UNSET))

        _created_at = d.pop("createdAt", UNSET)
        created_at: datetime.datetime | Unset
        if isinstance(_created_at, Unset):
            created_at = UNSET
        else:
            created_at = datetime.datetime.fromisoformat(_created_at)

        def _parse_executed_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                executed_at_type_0 = datetime.datetime.fromisoformat(data)

                return executed_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        executed_at = _parse_executed_at(d.pop("executedAt", UNSET))

        ask_action = cls(
            id=id,
            type_=type_,
            description=description,
            status=status,
            payload=payload,
            result=result,
            created_at=created_at,
            executed_at=executed_at,
        )

        ask_action.additional_properties = d
        return ask_action

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
