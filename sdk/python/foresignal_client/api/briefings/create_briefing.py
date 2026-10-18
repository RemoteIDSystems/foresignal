from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.briefing import Briefing
from ...models.briefing_input import BriefingInput
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: BriefingInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/briefings",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Briefing | Problem | None:
    if response.status_code == 201:
        response_201 = Briefing.from_dict(response.json())

        return response_201

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Briefing | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    body: BriefingInput,
) -> Response[Briefing | Problem]:
    """Create a scheduled briefing

     Creates a scheduled briefing. Validation: 5-field cron no more frequent than every 15 minutes, valid
    IANA `timezone`, existing scope ids (a private source the caller may not see is reported as
    unknown), `lookback` 1h–7d; scope members of later prompts (`placeIds`, `viewId`, `country`,
    `admin1`, `county`, `places`, `weatherProducts`) are refused with 400 until their prompt while non-
    empty, ; `audio` (141) is null or `{voice, format}` with a voice of 1–100 characters and `format`
    `mp3` (default) or `opus`, accepted without a `tts` provider (runs then record `audio.status`
    `skipped`); channels that do not exist, are disabled or are not subscribed to `briefing` produce
    `warnings`, not failures; `mode=llm` is accepted without a provider (checked at run time). The
    creating token's scope is the briefing's owner scope: only an `admin`-owned briefing lists records
    of private sources, any other counts them only.

    **Scope:** `agent`.

    Args:
        body (BriefingInput):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Briefing | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    body: BriefingInput,
) -> Briefing | Problem | None:
    """Create a scheduled briefing

     Creates a scheduled briefing. Validation: 5-field cron no more frequent than every 15 minutes, valid
    IANA `timezone`, existing scope ids (a private source the caller may not see is reported as
    unknown), `lookback` 1h–7d; scope members of later prompts (`placeIds`, `viewId`, `country`,
    `admin1`, `county`, `places`, `weatherProducts`) are refused with 400 until their prompt while non-
    empty, ; `audio` (141) is null or `{voice, format}` with a voice of 1–100 characters and `format`
    `mp3` (default) or `opus`, accepted without a `tts` provider (runs then record `audio.status`
    `skipped`); channels that do not exist, are disabled or are not subscribed to `briefing` produce
    `warnings`, not failures; `mode=llm` is accepted without a provider (checked at run time). The
    creating token's scope is the briefing's owner scope: only an `admin`-owned briefing lists records
    of private sources, any other counts them only.

    **Scope:** `agent`.

    Args:
        body (BriefingInput):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Briefing | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: BriefingInput,
) -> Response[Briefing | Problem]:
    """Create a scheduled briefing

     Creates a scheduled briefing. Validation: 5-field cron no more frequent than every 15 minutes, valid
    IANA `timezone`, existing scope ids (a private source the caller may not see is reported as
    unknown), `lookback` 1h–7d; scope members of later prompts (`placeIds`, `viewId`, `country`,
    `admin1`, `county`, `places`, `weatherProducts`) are refused with 400 until their prompt while non-
    empty, ; `audio` (141) is null or `{voice, format}` with a voice of 1–100 characters and `format`
    `mp3` (default) or `opus`, accepted without a `tts` provider (runs then record `audio.status`
    `skipped`); channels that do not exist, are disabled or are not subscribed to `briefing` produce
    `warnings`, not failures; `mode=llm` is accepted without a provider (checked at run time). The
    creating token's scope is the briefing's owner scope: only an `admin`-owned briefing lists records
    of private sources, any other counts them only.

    **Scope:** `agent`.

    Args:
        body (BriefingInput):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Briefing | Problem]
    """

    kwargs = _get_kwargs(
        body=body,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    body: BriefingInput,
) -> Briefing | Problem | None:
    """Create a scheduled briefing

     Creates a scheduled briefing. Validation: 5-field cron no more frequent than every 15 minutes, valid
    IANA `timezone`, existing scope ids (a private source the caller may not see is reported as
    unknown), `lookback` 1h–7d; scope members of later prompts (`placeIds`, `viewId`, `country`,
    `admin1`, `county`, `places`, `weatherProducts`) are refused with 400 until their prompt while non-
    empty, ; `audio` (141) is null or `{voice, format}` with a voice of 1–100 characters and `format`
    `mp3` (default) or `opus`, accepted without a `tts` provider (runs then record `audio.status`
    `skipped`); channels that do not exist, are disabled or are not subscribed to `briefing` produce
    `warnings`, not failures; `mode=llm` is accepted without a provider (checked at run time). The
    creating token's scope is the briefing's owner scope: only an `admin`-owned briefing lists records
    of private sources, any other counts them only.

    **Scope:** `agent`.

    Args:
        body (BriefingInput):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Briefing | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
