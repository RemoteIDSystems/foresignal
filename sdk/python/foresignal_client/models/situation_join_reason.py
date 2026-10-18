from __future__ import annotations

from collections.abc import Mapping
from typing import TYPE_CHECKING, Any, Self, TypeVar, cast

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..models.situation_join_reason_method import SituationJoinReasonMethod
from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.situation_join_reason_components import SituationJoinReasonComponents


T = TypeVar("T", bound="SituationJoinReason")


@_attrs_define
class SituationJoinReason:
    """Why a member joined a situation: the linker's score and its components, or the operator/agent action

    Attributes:
        method (SituationJoinReasonMethod): How the link was made
        score (float | None | Unset): Linker score in [0, 1] (null for manual links)
        components (SituationJoinReasonComponents | Unset): Score components (documented constants in `LinkerWeights`)
        linker_version (None | str | Unset): Linker version, e.g. `1.0.0`
        actor (None | str | Unset): Token id, `linker:<version>` or `analyst:<profile>`
    """

    method: SituationJoinReasonMethod
    score: float | None | Unset = UNSET
    components: SituationJoinReasonComponents | Unset = UNSET
    linker_version: None | str | Unset = UNSET
    actor: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        method = self.method.value

        score: float | None | Unset
        if isinstance(self.score, Unset):
            score = UNSET
        else:
            score = self.score

        components: dict[str, Any] | Unset = UNSET
        if not isinstance(self.components, Unset):
            components = self.components.to_dict()

        linker_version: None | str | Unset
        if isinstance(self.linker_version, Unset):
            linker_version = UNSET
        else:
            linker_version = self.linker_version

        actor: None | str | Unset
        if isinstance(self.actor, Unset):
            actor = UNSET
        else:
            actor = self.actor

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "method": method,
            }
        )
        if score is not UNSET:
            field_dict["score"] = score
        if components is not UNSET:
            field_dict["components"] = components
        if linker_version is not UNSET:
            field_dict["linkerVersion"] = linker_version
        if actor is not UNSET:
            field_dict["actor"] = actor

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.situation_join_reason_components import (
            SituationJoinReasonComponents,
        )

        d = dict(src_dict)
        method = SituationJoinReasonMethod(d.pop("method"))

        def _parse_score(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        score = _parse_score(d.pop("score", UNSET))

        _components = d.pop("components", UNSET)
        components: SituationJoinReasonComponents | Unset
        if isinstance(_components, Unset):
            components = UNSET
        else:
            components = SituationJoinReasonComponents.from_dict(_components)

        def _parse_linker_version(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        linker_version = _parse_linker_version(d.pop("linkerVersion", UNSET))

        def _parse_actor(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        actor = _parse_actor(d.pop("actor", UNSET))

        situation_join_reason = cls(
            method=method,
            score=score,
            components=components,
            linker_version=linker_version,
            actor=actor,
        )

        situation_join_reason.additional_properties = d
        return situation_join_reason

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
