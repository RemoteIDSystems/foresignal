from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar, cast

from attrs import define as _attrs_define

from ..models.translation_settings_budget_window import TranslationSettingsBudgetWindow
from ..types import UNSET, Unset

T = TypeVar("T", bound="TranslationSettingsBudget")


@_attrs_define
class TranslationSettingsBudget:
    """A spend cap for translation alone, inside the provider's own caps (162)

    Attributes:
        window (TranslationSettingsBudgetWindow | Unset): The UTC window the cap applies to Default:
            TranslationSettingsBudgetWindow.DAY.
        cap_usd (float | None | Unset): USD translation may spend per window, or null for no separate cap; once spent,
            articles are left untranslated (and recorded as skipped) until the window resets
    """

    window: TranslationSettingsBudgetWindow | Unset = (
        TranslationSettingsBudgetWindow.DAY
    )
    cap_usd: float | None | Unset = UNSET

    def to_dict(self) -> dict[str, Any]:
        window: str | Unset = UNSET
        if not isinstance(self.window, Unset):
            window = self.window.value

        cap_usd: float | None | Unset
        if isinstance(self.cap_usd, Unset):
            cap_usd = UNSET
        else:
            cap_usd = self.cap_usd

        field_dict: dict[str, Any] = {}

        field_dict.update({})
        if window is not UNSET:
            field_dict["window"] = window
        if cap_usd is not UNSET:
            field_dict["capUsd"] = cap_usd

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        _window = d.pop("window", UNSET)
        window: TranslationSettingsBudgetWindow | Unset
        if isinstance(_window, Unset):
            window = UNSET
        else:
            window = TranslationSettingsBudgetWindow(_window)

        def _parse_cap_usd(data: object) -> float | None | Unset:
            if data is None:
                return data
            if isinstance(data, Unset):
                return data
            return cast(float | None | Unset, data)

        cap_usd = _parse_cap_usd(d.pop("capUsd", UNSET))

        translation_settings_budget = cls(
            window=window,
            cap_usd=cap_usd,
        )

        return translation_settings_budget
