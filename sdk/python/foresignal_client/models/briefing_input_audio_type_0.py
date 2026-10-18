from __future__ import annotations

from collections.abc import Mapping
from typing import Any, Self, TypeVar

from attrs import define as _attrs_define

from ..models.briefing_input_audio_type_0_format import BriefingInputAudioType0Format
from ..types import UNSET, Unset

T = TypeVar("T", bound="BriefingInputAudioType0")


@_attrs_define
class BriefingInputAudioType0:
    """Synthesize the plain-text rendition with the 026 `tts` role after each run (141): at most 4,000 characters per
    request; longer text is split at paragraph or sentence boundaries and the MP3 parts concatenated (at most 25
    requests), while `opus` is truncated to one request with a spoken note. A synthesis failure never blocks the text
    delivery.

        Attributes:
            voice (str): The provider's voice, for example `alloy` (OpenAI) or `af_bella` (Kokoro)
            format_ (BriefingInputAudioType0Format | Unset):  Default: BriefingInputAudioType0Format.MP3.
    """

    voice: str
    format_: BriefingInputAudioType0Format | Unset = BriefingInputAudioType0Format.MP3

    def to_dict(self) -> dict[str, Any]:
        voice = self.voice

        format_: str | Unset = UNSET
        if not isinstance(self.format_, Unset):
            format_ = self.format_.value

        field_dict: dict[str, Any] = {}

        field_dict.update(
            {
                "voice": voice,
            }
        )
        if format_ is not UNSET:
            field_dict["format"] = format_

        return field_dict

    @classmethod
    def from_dict(cls, src_dict: Mapping[str, Any]) -> Self:
        d = dict(src_dict)
        voice = d.pop("voice")

        _format_ = d.pop("format", UNSET)
        format_: BriefingInputAudioType0Format | Unset
        if isinstance(_format_, Unset):
            format_ = UNSET
        else:
            format_ = BriefingInputAudioType0Format(_format_)

        briefing_input_audio_type_0 = cls(
            voice=voice,
            format_=format_,
        )

        return briefing_input_audio_type_0
