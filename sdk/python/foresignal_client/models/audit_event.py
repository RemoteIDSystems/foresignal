from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast
from uuid import UUID

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.audit_event_actor_kind import AuditEventActorKind
from ..models.audit_event_scope_type_1 import AuditEventScopeType1
from ..models.audit_event_scope_type_2_type_1 import AuditEventScopeType2Type1
from ..models.audit_event_scope_type_3_type_1 import AuditEventScopeType3Type1
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.audit_event_detail import AuditEventDetail


T = TypeVar("T", bound="AuditEvent")


@_attrs_define
class AuditEvent:
    """One audit log entry (023): authenticated writes, 401/403 on write paths, pairing claims, revocations, MCP tool calls
    (025), action executions (033), private reads, exports and purges (039)

        Attributes:
            id (int):
            at (datetime.datetime):
            actor_kind (AuditEventActorKind): `user` = a signed-in user (R2-013; `userId` and `userEmail` say who, `tokenId`
                is the session); `action` = a notification action capability (033); `chat` = a command from an allowlisted chat
                of an interactive channel (061; `tokenName` is the chat principal `chat:{channelId}:{chatId}[:{userId}]`);
                `mesh` = an `#E ACK` from an allowlisted sender on a private mesh channel (084; `tokenName` is `mesh:{sender}`)
            token_id (None | Unset | UUID):
            token_name (None | str | Unset):
            principal_id (None | str | Unset): Principal owning the token (v1.3)
            scope (AuditEventScopeType1 | AuditEventScopeType2Type1 | AuditEventScopeType3Type1 | None | Unset):
            method (None | str | Unset):
            path (None | str | Unset): Request path, query stripped
            operation_id (None | str | Unset):
            status (int | None | Unset):
            target_type (None | str | Unset):
            target_id (None | str | Unset):
            remote_ip (None | str | Unset):
            detail (AuditEventDetail | Unset): Context (e.g. MCP tool name, argument names, duration, outcome); never
                secrets, token values or request bodies
            user_id (None | Unset | UUID): The signed-in user who acted (actor kind `user`, R2-013), or null
            user_email (None | str | Unset): That user's email when the entry was written (kept after the user is renamed or
                deleted), or null
    """

    id: int
    at: datetime.datetime
    actor_kind: AuditEventActorKind
    token_id: None | Unset | UUID = UNSET
    token_name: None | str | Unset = UNSET
    principal_id: None | str | Unset = UNSET
    scope: (
        AuditEventScopeType1
        | AuditEventScopeType2Type1
        | AuditEventScopeType3Type1
        | None
        | Unset
    ) = UNSET
    method: None | str | Unset = UNSET
    path: None | str | Unset = UNSET
    operation_id: None | str | Unset = UNSET
    status: int | None | Unset = UNSET
    target_type: None | str | Unset = UNSET
    target_id: None | str | Unset = UNSET
    remote_ip: None | str | Unset = UNSET
    detail: AuditEventDetail | Unset = UNSET
    user_id: None | Unset | UUID = UNSET
    user_email: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        id = self.id

        at = self.at.isoformat()

        actor_kind = self.actor_kind.value

        token_id: None | str | Unset
        if isinstance(self.token_id, Unset):
            token_id = UNSET
        elif isinstance(self.token_id, UUID):
            token_id = str(self.token_id)
        else:
            token_id = self.token_id

        token_name: None | str | Unset
        if isinstance(self.token_name, Unset):
            token_name = UNSET
        else:
            token_name = self.token_name

        principal_id: None | str | Unset
        if isinstance(self.principal_id, Unset):
            principal_id = UNSET
        else:
            principal_id = self.principal_id

        scope: None | str | Unset
        if isinstance(self.scope, Unset):
            scope = UNSET
        elif (
            isinstance(self.scope, AuditEventScopeType1)
            or isinstance(self.scope, AuditEventScopeType2Type1)
            or isinstance(self.scope, AuditEventScopeType3Type1)
        ):
            scope = self.scope.value
        else:
            scope = self.scope

        method: None | str | Unset
        if isinstance(self.method, Unset):
            method = UNSET
        else:
            method = self.method

        path: None | str | Unset
        if isinstance(self.path, Unset):
            path = UNSET
        else:
            path = self.path

        operation_id: None | str | Unset
        if isinstance(self.operation_id, Unset):
            operation_id = UNSET
        else:
            operation_id = self.operation_id

        status: int | None | Unset
        if isinstance(self.status, Unset):
            status = UNSET
        else:
            status = self.status

        target_type: None | str | Unset
        if isinstance(self.target_type, Unset):
            target_type = UNSET
        else:
            target_type = self.target_type

        target_id: None | str | Unset
        if isinstance(self.target_id, Unset):
            target_id = UNSET
        else:
            target_id = self.target_id

        remote_ip: None | str | Unset
        if isinstance(self.remote_ip, Unset):
            remote_ip = UNSET
        else:
            remote_ip = self.remote_ip

        detail: dict[str, Any] | Unset = UNSET
        if not isinstance(self.detail, Unset):
            detail = self.detail.to_dict()

        user_id: None | str | Unset
        if isinstance(self.user_id, Unset):
            user_id = UNSET
        elif isinstance(self.user_id, UUID):
            user_id = str(self.user_id)
        else:
            user_id = self.user_id

        user_email: None | str | Unset
        if isinstance(self.user_email, Unset):
            user_email = UNSET
        else:
            user_email = self.user_email

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "at": at,
                "actorKind": actor_kind,
            }
        )
        if token_id is not UNSET:
            field_dict["tokenId"] = token_id
        if token_name is not UNSET:
            field_dict["tokenName"] = token_name
        if principal_id is not UNSET:
            field_dict["principalId"] = principal_id
        if scope is not UNSET:
            field_dict["scope"] = scope
        if method is not UNSET:
            field_dict["method"] = method
        if path is not UNSET:
            field_dict["path"] = path
        if operation_id is not UNSET:
            field_dict["operationId"] = operation_id
        if status is not UNSET:
            field_dict["status"] = status
        if target_type is not UNSET:
            field_dict["targetType"] = target_type
        if target_id is not UNSET:
            field_dict["targetId"] = target_id
        if remote_ip is not UNSET:
            field_dict["remoteIp"] = remote_ip
        if detail is not UNSET:
            field_dict["detail"] = detail
        if user_id is not UNSET:
            field_dict["userId"] = user_id
        if user_email is not UNSET:
            field_dict["userEmail"] = user_email

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.audit_event_detail import AuditEventDetail

        d = dict(src_dict)
        id = d.pop("id")

        at = datetime.datetime.fromisoformat(d.pop("at"))

        actor_kind = AuditEventActorKind(d.pop("actorKind"))

        def _parse_token_id(data: object) -> None | Unset | UUID:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                token_id_type_0 = UUID(data)

                return token_id_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Unset | UUID, data)

        token_id = _parse_token_id(d.pop("tokenId", UNSET))

        def _parse_token_name(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        token_name = _parse_token_name(d.pop("tokenName", UNSET))

        def _parse_principal_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        principal_id = _parse_principal_id(d.pop("principalId", UNSET))

        def _parse_scope(
            data: object,
        ) -> (
            AuditEventScopeType1
            | AuditEventScopeType2Type1
            | AuditEventScopeType3Type1
            | None
            | Unset
        ):
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                scope_type_1 = AuditEventScopeType1(data)

                return scope_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                scope_type_2_type_1 = AuditEventScopeType2Type1(data)

                return scope_type_2_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            try:
                if not isinstance(data, str):
                    raise TypeError()
                scope_type_3_type_1 = AuditEventScopeType3Type1(data)

                return scope_type_3_type_1
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(
                AuditEventScopeType1
                | AuditEventScopeType2Type1
                | AuditEventScopeType3Type1
                | None
                | Unset,
                data,
            )

        scope = _parse_scope(d.pop("scope", UNSET))

        def _parse_method(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        method = _parse_method(d.pop("method", UNSET))

        def _parse_path(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        path = _parse_path(d.pop("path", UNSET))

        def _parse_operation_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        operation_id = _parse_operation_id(d.pop("operationId", UNSET))

        def _parse_status(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        status = _parse_status(d.pop("status", UNSET))

        def _parse_target_type(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        target_type = _parse_target_type(d.pop("targetType", UNSET))

        def _parse_target_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        target_id = _parse_target_id(d.pop("targetId", UNSET))

        def _parse_remote_ip(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        remote_ip = _parse_remote_ip(d.pop("remoteIp", UNSET))

        _detail = d.pop("detail", UNSET)
        detail: AuditEventDetail | Unset
        if isinstance(_detail, Unset):
            detail = UNSET
        else:
            detail = AuditEventDetail.from_dict(_detail)

        def _parse_user_id(data: object) -> None | Unset | UUID:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                user_id_type_0 = UUID(data)

                return user_id_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(None | Unset | UUID, data)

        user_id = _parse_user_id(d.pop("userId", UNSET))

        def _parse_user_email(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        user_email = _parse_user_email(d.pop("userEmail", UNSET))

        audit_event = cls(
            id=id,
            at=at,
            actor_kind=actor_kind,
            token_id=token_id,
            token_name=token_name,
            principal_id=principal_id,
            scope=scope,
            method=method,
            path=path,
            operation_id=operation_id,
            status=status,
            target_type=target_type,
            target_id=target_id,
            remote_ip=remote_ip,
            detail=detail,
            user_id=user_id,
            user_email=user_email,
        )

        audit_event.additional_properties = d
        return audit_event

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
