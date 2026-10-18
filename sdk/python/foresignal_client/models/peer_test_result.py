from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.federation_info import FederationInfo


T = TypeVar("T", bound="PeerTestResult")


@_attrs_define
class PeerTestResult:
    """Result of fetching and verifying a peer's /v1/federation/info (113)

    Attributes:
        ok (bool):
        reachable (bool):
        latency_ms (int | None | Unset):
        info (FederationInfo | None | Unset):
        signature_valid (bool | None | Unset):
        kid (None | str | Unset):
        pinned_kid (None | str | Unset):
        key_matches (bool | None | Unset):
        error (None | str | Unset):
    """

    ok: bool
    reachable: bool
    latency_ms: int | None | Unset = UNSET
    info: FederationInfo | None | Unset = UNSET
    signature_valid: bool | None | Unset = UNSET
    kid: None | str | Unset = UNSET
    pinned_kid: None | str | Unset = UNSET
    key_matches: bool | None | Unset = UNSET
    error: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.federation_info import FederationInfo

        ok = self.ok

        reachable = self.reachable

        latency_ms: int | None | Unset
        if isinstance(self.latency_ms, Unset):
            latency_ms = UNSET
        else:
            latency_ms = self.latency_ms

        info: dict[str, Any] | None | Unset
        if isinstance(self.info, Unset):
            info = UNSET
        elif isinstance(self.info, FederationInfo):
            info = self.info.to_dict()
        else:
            info = self.info

        signature_valid: bool | None | Unset
        if isinstance(self.signature_valid, Unset):
            signature_valid = UNSET
        else:
            signature_valid = self.signature_valid

        kid: None | str | Unset
        if isinstance(self.kid, Unset):
            kid = UNSET
        else:
            kid = self.kid

        pinned_kid: None | str | Unset
        if isinstance(self.pinned_kid, Unset):
            pinned_kid = UNSET
        else:
            pinned_kid = self.pinned_kid

        key_matches: bool | None | Unset
        if isinstance(self.key_matches, Unset):
            key_matches = UNSET
        else:
            key_matches = self.key_matches

        error: None | str | Unset
        if isinstance(self.error, Unset):
            error = UNSET
        else:
            error = self.error

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "ok": ok,
                "reachable": reachable,
            }
        )
        if latency_ms is not UNSET:
            field_dict["latencyMs"] = latency_ms
        if info is not UNSET:
            field_dict["info"] = info
        if signature_valid is not UNSET:
            field_dict["signatureValid"] = signature_valid
        if kid is not UNSET:
            field_dict["kid"] = kid
        if pinned_kid is not UNSET:
            field_dict["pinnedKid"] = pinned_kid
        if key_matches is not UNSET:
            field_dict["keyMatches"] = key_matches
        if error is not UNSET:
            field_dict["error"] = error

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.federation_info import FederationInfo

        d = dict(src_dict)
        ok = d.pop("ok")

        reachable = d.pop("reachable")

        def _parse_latency_ms(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        latency_ms = _parse_latency_ms(d.pop("latencyMs", UNSET))

        def _parse_info(data: object) -> FederationInfo | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                info_type_0 = FederationInfo.from_dict(data)

                return info_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(FederationInfo | None | Unset, data)

        info = _parse_info(d.pop("info", UNSET))

        def _parse_signature_valid(data: object) -> bool | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(bool | None | Unset, data)

        signature_valid = _parse_signature_valid(d.pop("signatureValid", UNSET))

        def _parse_kid(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        kid = _parse_kid(d.pop("kid", UNSET))

        def _parse_pinned_kid(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        pinned_kid = _parse_pinned_kid(d.pop("pinnedKid", UNSET))

        def _parse_key_matches(data: object) -> bool | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(bool | None | Unset, data)

        key_matches = _parse_key_matches(d.pop("keyMatches", UNSET))

        def _parse_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        error = _parse_error(d.pop("error", UNSET))

        peer_test_result = cls(
            ok=ok,
            reachable=reachable,
            latency_ms=latency_ms,
            info=info,
            signature_valid=signature_valid,
            kid=kid,
            pinned_kid=pinned_kid,
            key_matches=key_matches,
            error=error,
        )

        peer_test_result.additional_properties = d
        return peer_test_result

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
