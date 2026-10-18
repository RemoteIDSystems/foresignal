from __future__ import annotations

from collections.abc import Mapping
from typing import (
    TYPE_CHECKING,
    Any,
    Literal,
    Self,
    TypeVar,
    cast,
)

from attrs import define as _attrs_define
from attrs import field as _attrs_field

from ..types import UNSET, Unset

if TYPE_CHECKING:
    from ..models.proposed_patch_sanity_checks import ProposedPatchSanityChecks


T = TypeVar("T", bound="ProposedPatch")


@_attrs_define
class ProposedPatch:
    """A parser patch proposed by the LLM as text; validated in memory, never written, compiled or applied

    Attributes:
        file (str):
        unified_diff (str):
        patch_applies (bool):
        label (Literal['proposed, not applied']):
        sanity_checks (ProposedPatchSanityChecks | Unset): javac-free checks on the patched file
        error (None | str | Unset): Why the patch does not apply, or why its file was refused (not in the adapter source
            catalog)
    """

    file: str
    unified_diff: str
    patch_applies: bool
    label: Literal["proposed, not applied"]
    sanity_checks: ProposedPatchSanityChecks | Unset = UNSET
    error: None | str | Unset = UNSET
    additional_properties: dict[str, Any] = _attrs_field(init=False, factory=dict)

    def to_dict(self) -> dict[str, Any]:
        file = self.file

        unified_diff = self.unified_diff

        patch_applies = self.patch_applies

        label = self.label

        sanity_checks: dict[str, Any] | Unset = UNSET
        if not isinstance(self.sanity_checks, Unset):
            sanity_checks = self.sanity_checks.to_dict()

        error: None | str | Unset
        if isinstance(self.error, Unset):
            error = UNSET
        else:
            error = self.error

        field_dict: dict[str, Any] = {}
        field_dict.update(self.additional_properties)
        field_dict.update(
            {
                "file": file,
                "unifiedDiff": unified_diff,
                "patchApplies": patch_applies,
                "label": label,
            }
        )
        if sanity_checks is not UNSET:
            field_dict["sanityChecks"] = sanity_checks
        if error is not UNSET:
            field_dict["error"] = error

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        from ..models.proposed_patch_sanity_checks import ProposedPatchSanityChecks

        d = dict(src_dict)
        file = d.pop("file")

        unified_diff = d.pop("unifiedDiff")

        patch_applies = d.pop("patchApplies")

        label = cast(Literal["proposed, not applied"], d.pop("label"))
        if label != "proposed, not applied":
            raise ValueError(
                f"label must match const 'proposed, not applied', got '{label}'"
            )

        _sanity_checks = d.pop("sanityChecks", UNSET)
        sanity_checks: ProposedPatchSanityChecks | Unset
        if isinstance(_sanity_checks, Unset):
            sanity_checks = UNSET
        else:
            sanity_checks = ProposedPatchSanityChecks.from_dict(_sanity_checks)

        def _parse_error(data: object) -> None | str | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(None | str | Unset, data)

        error = _parse_error(d.pop("error", UNSET))

        proposed_patch = cls(
            file=file,
            unified_diff=unified_diff,
            patch_applies=patch_applies,
            label=label,
            sanity_checks=sanity_checks,
            error=error,
        )

        proposed_patch.additional_properties = d
        return proposed_patch

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
