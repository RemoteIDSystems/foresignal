from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

T = TypeVar("T", bound="PushTestResult")


@_attrs_define
class PushTestResult:
    """Result of a native push provider or device test (032)

    Attributes:
        success (bool):
        credential_minted (bool | Unset): APNs JWT or FCM OAuth token obtained
        sent (bool | None | Unset): null when no device was targeted
        status (int | None | Unset): Push service HTTP status
        reason (None | str | Unset):
        error (None | str | Unset):
    """

    success: bool
    credential_minted: bool | Unset = UNSET
    sent: bool | None | Unset = UNSET
    status: int | None | Unset = UNSET
    reason: None | str | Unset = UNSET
    error: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        success = self.success

        credential_minted = self.credential_minted

        sent: bool | None | Unset
        if isinstance(self.sent, Unset):
            sent = UNSET
        else:
            sent = self.sent

        status: int | None | Unset
        if isinstance(self.status, Unset):
            status = UNSET
        else:
            status = self.status

        reason: None | str | Unset
        if isinstance(self.reason, Unset):
            reason = UNSET
        else:
            reason = self.reason

        error: None | str | Unset
        if isinstance(self.error, Unset):
            error = UNSET
        else:
            error = self.error

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "success": success,
            }
        )
        if credential_minted is not UNSET:
            field_dict["credentialMinted"] = credential_minted
        if sent is not UNSET:
            field_dict["sent"] = sent
        if status is not UNSET:
            field_dict["status"] = status
        if reason is not UNSET:
            field_dict["reason"] = reason
        if error is not UNSET:
            field_dict["error"] = error

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        success = d.pop("success")

        credential_minted = d.pop("credentialMinted", UNSET)

        def _parse_sent(data: object) -> bool | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(bool | None | Unset, data)

        sent = _parse_sent(d.pop("sent", UNSET))

        def _parse_status(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        status = _parse_status(d.pop("status", UNSET))

        def _parse_reason(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        reason = _parse_reason(d.pop("reason", UNSET))

        def _parse_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        error = _parse_error(d.pop("error", UNSET))

        push_test_result = cls(
            success=success,
            credential_minted=credential_minted,
            sent=sent,
            status=status,
            reason=reason,
            error=error,
        )

        push_test_result.additional_properties = d
        return push_test_result

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
