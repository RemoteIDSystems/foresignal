from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.mcp_auth_settings_allowed_algs_item import McpAuthSettingsAllowedAlgsItem
from ..models.mcp_auth_settings_mode import McpAuthSettingsMode
from ..models.mcp_auth_settings_scope_claim import McpAuthSettingsScopeClaim
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.mcp_auth_settings_scope_map import McpAuthSettingsScopeMap


T = TypeVar("T", bound="McpAuthSettings")


@_attrs_define
class McpAuthSettings:
    """Settings `mcp.auth`: remote MCP authorization delegated to the operator's identity provider (no built-in
    authorization server, no device grant)

        Attributes:
            mode (McpAuthSettingsMode): `token`: Foresignal tokens only (open reads apply as on REST). `oauth`: identity-
                provider access tokens only. `both`: either. With `oauth` or `both`, `/mcp` always needs a credential Default:
                McpAuthSettingsMode.TOKEN.
            issuer (None | str | Unset): Issuer identifier, compared exactly with the token's `iss`; required with `oauth`
                or `both`
            audience (None | str | Unset): Default `{publicBaseUrl}/mcp` (RFC 8707)
            jwks_uri (None | str | Unset): Optional; else RFC 8414 then OIDC discovery from the issuer through the 035 URL
                policy
            scope_map (McpAuthSettingsScopeMap | Unset): IdP scope → Foresignal scope; `admin` is never mappable Example:
                {'foresignal:read': 'read', 'foresignal:agent': 'agent'}.
            scope_claim (McpAuthSettingsScopeClaim | Unset): Claim carrying the scopes; a space-separated string or an array
                of strings is read from either claim Default: McpAuthSettingsScopeClaim.SCOPE.
            allowed_algs (list[McpAuthSettingsAllowedAlgsItem] | Unset): Accepted JWS algorithms; `none` and HMAC algorithms
                are never accepted
    """

    mode: McpAuthSettingsMode = McpAuthSettingsMode.TOKEN
    issuer: None | str | Unset = UNSET
    audience: None | str | Unset = UNSET
    jwks_uri: None | str | Unset = UNSET
    scope_map: McpAuthSettingsScopeMap | Unset = UNSET
    scope_claim: McpAuthSettingsScopeClaim | Unset = McpAuthSettingsScopeClaim.SCOPE
    allowed_algs: list[McpAuthSettingsAllowedAlgsItem] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        mode = self.mode.value

        issuer: None | str | Unset
        if isinstance(self.issuer, Unset):
            issuer = UNSET
        else:
            issuer = self.issuer

        audience: None | str | Unset
        if isinstance(self.audience, Unset):
            audience = UNSET
        else:
            audience = self.audience

        jwks_uri: None | str | Unset
        if isinstance(self.jwks_uri, Unset):
            jwks_uri = UNSET
        else:
            jwks_uri = self.jwks_uri

        scope_map: dict[str, Any] | Unset = UNSET
        if not isinstance(self.scope_map, Unset):
            scope_map = self.scope_map.to_dict()

        scope_claim: str | Unset = UNSET
        if not isinstance(self.scope_claim, Unset):
            scope_claim = self.scope_claim.value

        allowed_algs: list[str] | Unset = UNSET
        if not isinstance(self.allowed_algs, Unset):
            allowed_algs = []
            for allowed_algs_item_data in self.allowed_algs:
                allowed_algs_item = allowed_algs_item_data.value
                allowed_algs.append(allowed_algs_item)

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "mode": mode,
            }
        )
        if issuer is not UNSET:
            field_dict["issuer"] = issuer
        if audience is not UNSET:
            field_dict["audience"] = audience
        if jwks_uri is not UNSET:
            field_dict["jwksUri"] = jwks_uri
        if scope_map is not UNSET:
            field_dict["scopeMap"] = scope_map
        if scope_claim is not UNSET:
            field_dict["scopeClaim"] = scope_claim
        if allowed_algs is not UNSET:
            field_dict["allowedAlgs"] = allowed_algs

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.mcp_auth_settings_scope_map import McpAuthSettingsScopeMap

        d = dict(src_dict)
        mode = McpAuthSettingsMode(d.pop("mode"))

        def _parse_issuer(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        issuer = _parse_issuer(d.pop("issuer", UNSET))

        def _parse_audience(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        audience = _parse_audience(d.pop("audience", UNSET))

        def _parse_jwks_uri(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        jwks_uri = _parse_jwks_uri(d.pop("jwksUri", UNSET))

        _scope_map = d.pop("scopeMap", UNSET)
        scope_map: McpAuthSettingsScopeMap | Unset
        if isinstance(_scope_map, Unset):
            scope_map = UNSET
        else:
            scope_map = McpAuthSettingsScopeMap.from_dict(_scope_map)

        _scope_claim = d.pop("scopeClaim", UNSET)
        scope_claim: McpAuthSettingsScopeClaim | Unset
        if isinstance(_scope_claim, Unset):
            scope_claim = UNSET
        else:
            scope_claim = McpAuthSettingsScopeClaim(_scope_claim)

        _allowed_algs = d.pop("allowedAlgs", UNSET)
        allowed_algs: list[McpAuthSettingsAllowedAlgsItem] | Unset = UNSET
        if _allowed_algs is not UNSET:
            allowed_algs = []
            for allowed_algs_item_data in _allowed_algs:
                allowed_algs_item = McpAuthSettingsAllowedAlgsItem(
                    allowed_algs_item_data
                )

                allowed_algs.append(allowed_algs_item)

        mcp_auth_settings = cls(
            mode=mode,
            issuer=issuer,
            audience=audience,
            jwks_uri=jwks_uri,
            scope_map=scope_map,
            scope_claim=scope_claim,
            allowed_algs=allowed_algs,
        )

        mcp_auth_settings.additional_properties = d
        return mcp_auth_settings

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
