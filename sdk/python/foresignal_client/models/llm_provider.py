from __future__ import annotations

import datetime
from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.llm_provider_roles_item import LlmProviderRolesItem
from ..models.llm_provider_status import LlmProviderStatus
from ..models.llm_provider_type import LlmProviderType
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.llm_pricing import LlmPricing
    from ..models.llm_provider_options import LlmProviderOptions


T = TypeVar("T", bound="LlmProvider")


@_attrs_define
class LlmProvider:
    """
    Attributes:
        id (str):
        type_ (LlmProviderType):
        enabled (bool):
        status (LlmProviderStatus):
        base_url (None | str | Unset):
        chat_model (None | str | Unset):
        embedding_model (None | str | Unset):
        embedding_dimensions (int | None | Unset):
        api_key_set (bool | Unset):
        options (LlmProviderOptions | Unset):
        requests_per_minute (int | Unset):
        tokens_per_day (int | Unset):
        roles (list[LlmProviderRolesItem] | Unset):
        tokens_today (int | Unset):
        last_error (None | str | Unset):
        updated_at (datetime.datetime | Unset):
        supports_tools (bool | None | Unset): False when the chat model rejected tools; Ask then runs retrieval-only
        knowledge_cutoff (datetime.date | None | Unset): Training-data cutoff of the chat model; required for leak-free
            retrospective evaluation of LLM forecasters
        last_success_at (datetime.datetime | None | Unset):
        last_error_at (datetime.datetime | None | Unset):
        consecutive_failures (int | Unset):
        local (bool | Unset): Treated as local for LLM egress (ollama, or `options.local=true`)
        pricing (LlmPricing | None | Unset): Prices in USD per million tokens (162); every member null when unpriced
        hourly_cap_usd (float | None | Unset): USD cap of the UTC clock hour (it resets at the top of the hour), or null
            (162)
        daily_cap_usd (float | None | Unset): USD cap of the UTC day (it resets at 00:00 UTC, as tokensPerDay), or null
            (162)
        monthly_cap_usd (float | None | Unset): USD cap of the UTC calendar month (it resets at 00:00 UTC on the 1st),
            or null (162)
        shared (bool | Unset): True for a shared provider (no owner, managed with `llm.shared.manage`); false for a
            user's personal provider (R2-011)
        owner_principal_id (None | str | Unset): The owning user's principal for a personal provider; null for a shared
            one (R2-011)
        per_user_hourly_cap_usd (float | None | Unset): The most each user may spend on this shared provider in the UTC
            clock hour (it resets at the top of the hour), in USD, at most 4 decimal places; null means no per-user cap
            (R2-011). It counts only that user's own calls, so one user cannot exhaust the shared budget while the others
            continue; the provider's own caps still bound the total. Needs `pricing`; refused for a personal provider, whose
            own caps are already its owner's. Once reached, that user's calls are refused 429 LLM_BUDGET_EXCEEDED with
            Retry-After at the window's reset.
        per_user_daily_cap_usd (float | None | Unset): The most each user may spend on this shared provider in the UTC
            day (it resets at 00:00 UTC), in USD, at most 4 decimal places; null means no per-user cap (R2-011). It counts
            only that user's own calls, so one user cannot exhaust the shared budget while the others continue; the
            provider's own caps still bound the total. Needs `pricing`; refused for a personal provider, whose own caps are
            already its owner's. Once reached, that user's calls are refused 429 LLM_BUDGET_EXCEEDED with Retry-After at the
            window's reset.
        per_user_monthly_cap_usd (float | None | Unset): The most each user may spend on this shared provider in the UTC
            calendar month (it resets at 00:00 UTC on the 1st), in USD, at most 4 decimal places; null means no per-user cap
            (R2-011). It counts only that user's own calls, so one user cannot exhaust the shared budget while the others
            continue; the provider's own caps still bound the total. Needs `pricing`; refused for a personal provider, whose
            own caps are already its owner's. Once reached, that user's calls are refused 429 LLM_BUDGET_EXCEEDED with
            Retry-After at the window's reset.
    """

    id: str
    type_: LlmProviderType
    enabled: bool
    status: LlmProviderStatus
    base_url: None | str | Unset = UNSET
    chat_model: None | str | Unset = UNSET
    embedding_model: None | str | Unset = UNSET
    embedding_dimensions: int | None | Unset = UNSET
    api_key_set: bool | Unset = UNSET
    options: LlmProviderOptions | Unset = UNSET
    requests_per_minute: int | Unset = UNSET
    tokens_per_day: int | Unset = UNSET
    roles: list[LlmProviderRolesItem] | Unset = UNSET
    tokens_today: int | Unset = UNSET
    last_error: None | str | Unset = UNSET
    updated_at: datetime.datetime | Unset = UNSET
    supports_tools: bool | None | Unset = UNSET
    knowledge_cutoff: datetime.date | None | Unset = UNSET
    last_success_at: datetime.datetime | None | Unset = UNSET
    last_error_at: datetime.datetime | None | Unset = UNSET
    consecutive_failures: int | Unset = UNSET
    local: bool | Unset = UNSET
    pricing: LlmPricing | None | Unset = UNSET
    hourly_cap_usd: float | None | Unset = UNSET
    daily_cap_usd: float | None | Unset = UNSET
    monthly_cap_usd: float | None | Unset = UNSET
    shared: bool | Unset = UNSET
    owner_principal_id: None | str | Unset = UNSET
    per_user_hourly_cap_usd: float | None | Unset = UNSET
    per_user_daily_cap_usd: float | None | Unset = UNSET
    per_user_monthly_cap_usd: float | None | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.llm_pricing import LlmPricing

        id = self.id

        type_ = self.type_.value

        enabled = self.enabled

        status = self.status.value

        base_url: None | str | Unset
        if isinstance(self.base_url, Unset):
            base_url = UNSET
        else:
            base_url = self.base_url

        chat_model: None | str | Unset
        if isinstance(self.chat_model, Unset):
            chat_model = UNSET
        else:
            chat_model = self.chat_model

        embedding_model: None | str | Unset
        if isinstance(self.embedding_model, Unset):
            embedding_model = UNSET
        else:
            embedding_model = self.embedding_model

        embedding_dimensions: int | None | Unset
        if isinstance(self.embedding_dimensions, Unset):
            embedding_dimensions = UNSET
        else:
            embedding_dimensions = self.embedding_dimensions

        api_key_set = self.api_key_set

        options: dict[str, Any] | Unset = UNSET
        if not isinstance(self.options, Unset):
            options = self.options.to_dict()

        requests_per_minute = self.requests_per_minute

        tokens_per_day = self.tokens_per_day

        roles: list[str] | Unset = UNSET
        if not isinstance(self.roles, Unset):
            roles = []
            for roles_item_data in self.roles:
                roles_item = roles_item_data.value
                roles.append(roles_item)

        tokens_today = self.tokens_today

        last_error: None | str | Unset
        if isinstance(self.last_error, Unset):
            last_error = UNSET
        else:
            last_error = self.last_error

        updated_at: str | Unset = UNSET
        if not isinstance(self.updated_at, Unset):
            updated_at = self.updated_at.isoformat()

        supports_tools: bool | None | Unset
        if isinstance(self.supports_tools, Unset):
            supports_tools = UNSET
        else:
            supports_tools = self.supports_tools

        knowledge_cutoff: None | str | Unset
        if isinstance(self.knowledge_cutoff, Unset):
            knowledge_cutoff = UNSET
        elif isinstance(self.knowledge_cutoff, datetime.date):
            knowledge_cutoff = self.knowledge_cutoff.isoformat()
        else:
            knowledge_cutoff = self.knowledge_cutoff

        last_success_at: None | str | Unset
        if isinstance(self.last_success_at, Unset):
            last_success_at = UNSET
        elif isinstance(self.last_success_at, datetime.datetime):
            last_success_at = self.last_success_at.isoformat()
        else:
            last_success_at = self.last_success_at

        last_error_at: None | str | Unset
        if isinstance(self.last_error_at, Unset):
            last_error_at = UNSET
        elif isinstance(self.last_error_at, datetime.datetime):
            last_error_at = self.last_error_at.isoformat()
        else:
            last_error_at = self.last_error_at

        consecutive_failures = self.consecutive_failures

        local = self.local

        pricing: dict[str, Any] | None | Unset
        if isinstance(self.pricing, Unset):
            pricing = UNSET
        elif isinstance(self.pricing, LlmPricing):
            pricing = self.pricing.to_dict()
        else:
            pricing = self.pricing

        hourly_cap_usd: float | None | Unset
        if isinstance(self.hourly_cap_usd, Unset):
            hourly_cap_usd = UNSET
        else:
            hourly_cap_usd = self.hourly_cap_usd

        daily_cap_usd: float | None | Unset
        if isinstance(self.daily_cap_usd, Unset):
            daily_cap_usd = UNSET
        else:
            daily_cap_usd = self.daily_cap_usd

        monthly_cap_usd: float | None | Unset
        if isinstance(self.monthly_cap_usd, Unset):
            monthly_cap_usd = UNSET
        else:
            monthly_cap_usd = self.monthly_cap_usd

        shared = self.shared

        owner_principal_id: None | str | Unset
        if isinstance(self.owner_principal_id, Unset):
            owner_principal_id = UNSET
        else:
            owner_principal_id = self.owner_principal_id

        per_user_hourly_cap_usd: float | None | Unset
        if isinstance(self.per_user_hourly_cap_usd, Unset):
            per_user_hourly_cap_usd = UNSET
        else:
            per_user_hourly_cap_usd = self.per_user_hourly_cap_usd

        per_user_daily_cap_usd: float | None | Unset
        if isinstance(self.per_user_daily_cap_usd, Unset):
            per_user_daily_cap_usd = UNSET
        else:
            per_user_daily_cap_usd = self.per_user_daily_cap_usd

        per_user_monthly_cap_usd: float | None | Unset
        if isinstance(self.per_user_monthly_cap_usd, Unset):
            per_user_monthly_cap_usd = UNSET
        else:
            per_user_monthly_cap_usd = self.per_user_monthly_cap_usd

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "id": id,
                "type": type_,
                "enabled": enabled,
                "status": status,
            }
        )
        if base_url is not UNSET:
            field_dict["baseUrl"] = base_url
        if chat_model is not UNSET:
            field_dict["chatModel"] = chat_model
        if embedding_model is not UNSET:
            field_dict["embeddingModel"] = embedding_model
        if embedding_dimensions is not UNSET:
            field_dict["embeddingDimensions"] = embedding_dimensions
        if api_key_set is not UNSET:
            field_dict["apiKeySet"] = api_key_set
        if options is not UNSET:
            field_dict["options"] = options
        if requests_per_minute is not UNSET:
            field_dict["requestsPerMinute"] = requests_per_minute
        if tokens_per_day is not UNSET:
            field_dict["tokensPerDay"] = tokens_per_day
        if roles is not UNSET:
            field_dict["roles"] = roles
        if tokens_today is not UNSET:
            field_dict["tokensToday"] = tokens_today
        if last_error is not UNSET:
            field_dict["lastError"] = last_error
        if updated_at is not UNSET:
            field_dict["updatedAt"] = updated_at
        if supports_tools is not UNSET:
            field_dict["supportsTools"] = supports_tools
        if knowledge_cutoff is not UNSET:
            field_dict["knowledgeCutoff"] = knowledge_cutoff
        if last_success_at is not UNSET:
            field_dict["lastSuccessAt"] = last_success_at
        if last_error_at is not UNSET:
            field_dict["lastErrorAt"] = last_error_at
        if consecutive_failures is not UNSET:
            field_dict["consecutiveFailures"] = consecutive_failures
        if local is not UNSET:
            field_dict["local"] = local
        if pricing is not UNSET:
            field_dict["pricing"] = pricing
        if hourly_cap_usd is not UNSET:
            field_dict["hourlyCapUsd"] = hourly_cap_usd
        if daily_cap_usd is not UNSET:
            field_dict["dailyCapUsd"] = daily_cap_usd
        if monthly_cap_usd is not UNSET:
            field_dict["monthlyCapUsd"] = monthly_cap_usd
        if shared is not UNSET:
            field_dict["shared"] = shared
        if owner_principal_id is not UNSET:
            field_dict["ownerPrincipalId"] = owner_principal_id
        if per_user_hourly_cap_usd is not UNSET:
            field_dict["perUserHourlyCapUsd"] = per_user_hourly_cap_usd
        if per_user_daily_cap_usd is not UNSET:
            field_dict["perUserDailyCapUsd"] = per_user_daily_cap_usd
        if per_user_monthly_cap_usd is not UNSET:
            field_dict["perUserMonthlyCapUsd"] = per_user_monthly_cap_usd

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.llm_pricing import LlmPricing
        from ..models.llm_provider_options import LlmProviderOptions

        d = dict(src_dict)
        id = d.pop("id")

        type_ = LlmProviderType(d.pop("type"))

        enabled = d.pop("enabled")

        status = LlmProviderStatus(d.pop("status"))

        def _parse_base_url(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        base_url = _parse_base_url(d.pop("baseUrl", UNSET))

        def _parse_chat_model(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        chat_model = _parse_chat_model(d.pop("chatModel", UNSET))

        def _parse_embedding_model(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        embedding_model = _parse_embedding_model(d.pop("embeddingModel", UNSET))

        def _parse_embedding_dimensions(data: object) -> int | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(int | None | Unset, data)

        embedding_dimensions = _parse_embedding_dimensions(
            d.pop("embeddingDimensions", UNSET)
        )

        api_key_set = d.pop("apiKeySet", UNSET)

        _options = d.pop("options", UNSET)
        options: LlmProviderOptions | Unset
        if isinstance(_options, Unset):
            options = UNSET
        else:
            options = LlmProviderOptions.from_dict(_options)

        requests_per_minute = d.pop("requestsPerMinute", UNSET)

        tokens_per_day = d.pop("tokensPerDay", UNSET)

        _roles = d.pop("roles", UNSET)
        roles: list[LlmProviderRolesItem] | Unset = UNSET
        if _roles is not UNSET:
            roles = []
            for roles_item_data in _roles:
                roles_item = LlmProviderRolesItem(roles_item_data)

                roles.append(roles_item)

        tokens_today = d.pop("tokensToday", UNSET)

        def _parse_last_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        last_error = _parse_last_error(d.pop("lastError", UNSET))

        _updated_at = d.pop("updatedAt", UNSET)
        updated_at: datetime.datetime | Unset
        if isinstance(_updated_at, Unset):
            updated_at = UNSET
        else:
            updated_at = datetime.datetime.fromisoformat(_updated_at)

        def _parse_supports_tools(data: object) -> bool | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(bool | None | Unset, data)

        supports_tools = _parse_supports_tools(d.pop("supportsTools", UNSET))

        def _parse_knowledge_cutoff(data: object) -> datetime.date | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                knowledge_cutoff_type_0 = datetime.date.fromisoformat(data)

                return knowledge_cutoff_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.date | None | Unset, data)

        knowledge_cutoff = _parse_knowledge_cutoff(d.pop("knowledgeCutoff", UNSET))

        def _parse_last_success_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_success_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_success_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_success_at = _parse_last_success_at(d.pop("lastSuccessAt", UNSET))

        def _parse_last_error_at(data: object) -> datetime.datetime | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                last_error_at_type_0 = datetime.datetime.fromisoformat(data)

                return last_error_at_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(datetime.datetime | None | Unset, data)

        last_error_at = _parse_last_error_at(d.pop("lastErrorAt", UNSET))

        consecutive_failures = d.pop("consecutiveFailures", UNSET)

        local = d.pop("local", UNSET)

        def _parse_pricing(data: object) -> LlmPricing | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                pricing_type_0 = LlmPricing.from_dict(data)

                return pricing_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(LlmPricing | None | Unset, data)

        pricing = _parse_pricing(d.pop("pricing", UNSET))

        def _parse_hourly_cap_usd(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        hourly_cap_usd = _parse_hourly_cap_usd(d.pop("hourlyCapUsd", UNSET))

        def _parse_daily_cap_usd(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        daily_cap_usd = _parse_daily_cap_usd(d.pop("dailyCapUsd", UNSET))

        def _parse_monthly_cap_usd(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        monthly_cap_usd = _parse_monthly_cap_usd(d.pop("monthlyCapUsd", UNSET))

        shared = d.pop("shared", UNSET)

        def _parse_owner_principal_id(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        owner_principal_id = _parse_owner_principal_id(d.pop("ownerPrincipalId", UNSET))

        def _parse_per_user_hourly_cap_usd(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        per_user_hourly_cap_usd = _parse_per_user_hourly_cap_usd(
            d.pop("perUserHourlyCapUsd", UNSET)
        )

        def _parse_per_user_daily_cap_usd(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        per_user_daily_cap_usd = _parse_per_user_daily_cap_usd(
            d.pop("perUserDailyCapUsd", UNSET)
        )

        def _parse_per_user_monthly_cap_usd(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        per_user_monthly_cap_usd = _parse_per_user_monthly_cap_usd(
            d.pop("perUserMonthlyCapUsd", UNSET)
        )

        llm_provider = cls(
            id=id,
            type_=type_,
            enabled=enabled,
            status=status,
            base_url=base_url,
            chat_model=chat_model,
            embedding_model=embedding_model,
            embedding_dimensions=embedding_dimensions,
            api_key_set=api_key_set,
            options=options,
            requests_per_minute=requests_per_minute,
            tokens_per_day=tokens_per_day,
            roles=roles,
            tokens_today=tokens_today,
            last_error=last_error,
            updated_at=updated_at,
            supports_tools=supports_tools,
            knowledge_cutoff=knowledge_cutoff,
            last_success_at=last_success_at,
            last_error_at=last_error_at,
            consecutive_failures=consecutive_failures,
            local=local,
            pricing=pricing,
            hourly_cap_usd=hourly_cap_usd,
            daily_cap_usd=daily_cap_usd,
            monthly_cap_usd=monthly_cap_usd,
            shared=shared,
            owner_principal_id=owner_principal_id,
            per_user_hourly_cap_usd=per_user_hourly_cap_usd,
            per_user_daily_cap_usd=per_user_daily_cap_usd,
            per_user_monthly_cap_usd=per_user_monthly_cap_usd,
        )

        llm_provider.additional_properties = d
        return llm_provider

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
