from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.cycle_run_roles_item_effort_type_0 import CycleRunRolesItemEffortType0
from ..models.cycle_run_roles_item_role import CycleRunRolesItemRole
from ..models.cycle_run_roles_item_status import CycleRunRolesItemStatus
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.cycle_run_roles_item_prompt_template_type_0 import (
        CycleRunRolesItemPromptTemplateType0,
    )


T = TypeVar("T", bound="CycleRunRolesItem")


@_attrs_define
class CycleRunRolesItem:
    """Outcome of one role in a run

    Attributes:
        role (CycleRunRolesItemRole):
        status (CycleRunRolesItemStatus):
        reason (None | str | Unset):
        provider (None | str | Unset):
        model (None | str | Unset):
        effort (CycleRunRolesItemEffortType0 | None | Unset):
        input_tokens (int | Unset):
        output_tokens (int | Unset):
        cached_tokens (int | Unset):
        forecasts_submitted (int | Unset):
        proposal_ids (list[str] | Unset):
        prompt_template (CycleRunRolesItemPromptTemplateType0 | None | Unset):
        warnings (list[str] | Unset):
    """

    role: CycleRunRolesItemRole
    status: CycleRunRolesItemStatus
    reason: None | str | Unset = UNSET
    provider: None | str | Unset = UNSET
    model: None | str | Unset = UNSET
    effort: CycleRunRolesItemEffortType0 | None | Unset = UNSET
    input_tokens: int | Unset = UNSET
    output_tokens: int | Unset = UNSET
    cached_tokens: int | Unset = UNSET
    forecasts_submitted: int | Unset = UNSET
    proposal_ids: list[str] | Unset = UNSET
    prompt_template: CycleRunRolesItemPromptTemplateType0 | None | Unset = UNSET
    warnings: list[str] | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        from ..models.cycle_run_roles_item_prompt_template_type_0 import (
            CycleRunRolesItemPromptTemplateType0,
        )

        role = self.role.value

        status = self.status.value

        reason: None | str | Unset
        if isinstance(self.reason, Unset):
            reason = UNSET
        else:
            reason = self.reason

        provider: None | str | Unset
        if isinstance(self.provider, Unset):
            provider = UNSET
        else:
            provider = self.provider

        model: None | str | Unset
        if isinstance(self.model, Unset):
            model = UNSET
        else:
            model = self.model

        effort: None | str | Unset
        if isinstance(self.effort, Unset):
            effort = UNSET
        elif isinstance(self.effort, CycleRunRolesItemEffortType0):
            effort = self.effort.value
        else:
            effort = self.effort

        input_tokens = self.input_tokens

        output_tokens = self.output_tokens

        cached_tokens = self.cached_tokens

        forecasts_submitted = self.forecasts_submitted

        proposal_ids: list[str] | Unset = UNSET
        if not isinstance(self.proposal_ids, Unset):
            proposal_ids = self.proposal_ids

        prompt_template: dict[str, Any] | None | Unset
        if isinstance(self.prompt_template, Unset):
            prompt_template = UNSET
        elif isinstance(self.prompt_template, CycleRunRolesItemPromptTemplateType0):
            prompt_template = self.prompt_template.to_dict()
        else:
            prompt_template = self.prompt_template

        warnings: list[str] | Unset = UNSET
        if not isinstance(self.warnings, Unset):
            warnings = self.warnings

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "role": role,
                "status": status,
            }
        )
        if reason is not UNSET:
            field_dict["reason"] = reason
        if provider is not UNSET:
            field_dict["provider"] = provider
        if model is not UNSET:
            field_dict["model"] = model
        if effort is not UNSET:
            field_dict["effort"] = effort
        if input_tokens is not UNSET:
            field_dict["inputTokens"] = input_tokens
        if output_tokens is not UNSET:
            field_dict["outputTokens"] = output_tokens
        if cached_tokens is not UNSET:
            field_dict["cachedTokens"] = cached_tokens
        if forecasts_submitted is not UNSET:
            field_dict["forecastsSubmitted"] = forecasts_submitted
        if proposal_ids is not UNSET:
            field_dict["proposalIds"] = proposal_ids
        if prompt_template is not UNSET:
            field_dict["promptTemplate"] = prompt_template
        if warnings is not UNSET:
            field_dict["warnings"] = warnings

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.cycle_run_roles_item_prompt_template_type_0 import (
            CycleRunRolesItemPromptTemplateType0,
        )

        d = dict(src_dict)
        role = CycleRunRolesItemRole(d.pop("role"))

        status = CycleRunRolesItemStatus(d.pop("status"))

        def _parse_reason(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        reason = _parse_reason(d.pop("reason", UNSET))

        def _parse_provider(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        provider = _parse_provider(d.pop("provider", UNSET))

        def _parse_model(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        model = _parse_model(d.pop("model", UNSET))

        def _parse_effort(data: object) -> CycleRunRolesItemEffortType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, str):
                    raise TypeError()
                effort_type_0 = CycleRunRolesItemEffortType0(data)

                return effort_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(CycleRunRolesItemEffortType0 | None | Unset, data)

        effort = _parse_effort(d.pop("effort", UNSET))

        input_tokens = d.pop("inputTokens", UNSET)

        output_tokens = d.pop("outputTokens", UNSET)

        cached_tokens = d.pop("cachedTokens", UNSET)

        forecasts_submitted = d.pop("forecastsSubmitted", UNSET)

        proposal_ids = cast(list[str], d.pop("proposalIds", UNSET))

        def _parse_prompt_template(
            data: object,
        ) -> CycleRunRolesItemPromptTemplateType0 | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            try:
                if not isinstance(data, dict):
                    raise TypeError()
                prompt_template_type_0 = CycleRunRolesItemPromptTemplateType0.from_dict(
                    data
                )

                return prompt_template_type_0
            except (TypeError, ValueError, AttributeError, KeyError):
                pass
            return cast(CycleRunRolesItemPromptTemplateType0 | None | Unset, data)

        prompt_template = _parse_prompt_template(d.pop("promptTemplate", UNSET))

        warnings = cast(list[str], d.pop("warnings", UNSET))

        cycle_run_roles_item = cls(
            role=role,
            status=status,
            reason=reason,
            provider=provider,
            model=model,
            effort=effort,
            input_tokens=input_tokens,
            output_tokens=output_tokens,
            cached_tokens=cached_tokens,
            forecasts_submitted=forecasts_submitted,
            proposal_ids=proposal_ids,
            prompt_template=prompt_template,
            warnings=warnings,
        )

        cycle_run_roles_item.additional_properties = d
        return cycle_run_roles_item

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
