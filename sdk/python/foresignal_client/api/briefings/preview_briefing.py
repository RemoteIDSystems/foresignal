from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.briefing_input import BriefingInput
from ...models.briefing_run import BriefingRun
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    *,
    body: BriefingInput,
) -> dict[str, Any]:
    headers: dict[str, Any] = {}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/briefings/preview",
    }

    _kwargs["json"] = body.to_dict()

    headers["Content-Type"] = "application/json"

    _kwargs["headers"] = headers
    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> BriefingRun | Problem | None:
    if response.status_code == 200:
        response_200 = BriefingRun.from_dict(response.json())

        return response_200

    if response.status_code == 400:
        response_400 = Problem.from_dict(response.json())

        return response_400

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[BriefingRun | Problem]:
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
) -> Response[BriefingRun | Problem]:
    """Render a briefing without storing or delivering it

     Render a briefing without storing or delivering it and without changing any schedule (`id` and
    `briefingId` are null). The period is `lookback` back, or 24 h. LLM failures, refusals and budget
    exhaustion produce a `fallback` rendition, not an error. Private records are listed only for an
    `admin` caller.

    **Scope:** `agent` or `admin`.

    Args:
        body (BriefingInput):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[BriefingRun | Problem]
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
) -> BriefingRun | Problem | None:
    """Render a briefing without storing or delivering it

     Render a briefing without storing or delivering it and without changing any schedule (`id` and
    `briefingId` are null). The period is `lookback` back, or 24 h. LLM failures, refusals and budget
    exhaustion produce a `fallback` rendition, not an error. Private records are listed only for an
    `admin` caller.

    **Scope:** `agent` or `admin`.

    Args:
        body (BriefingInput):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        BriefingRun | Problem
    """

    return sync_detailed(
        client=client,
        body=body,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    body: BriefingInput,
) -> Response[BriefingRun | Problem]:
    """Render a briefing without storing or delivering it

     Render a briefing without storing or delivering it and without changing any schedule (`id` and
    `briefingId` are null). The period is `lookback` back, or 24 h. LLM failures, refusals and budget
    exhaustion produce a `fallback` rendition, not an error. Private records are listed only for an
    `admin` caller.

    **Scope:** `agent` or `admin`.

    Args:
        body (BriefingInput):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[BriefingRun | Problem]
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
) -> BriefingRun | Problem | None:
    """Render a briefing without storing or delivering it

     Render a briefing without storing or delivering it and without changing any schedule (`id` and
    `briefingId` are null). The period is `lookback` back, or 24 h. LLM failures, refusals and budget
    exhaustion produce a `fallback` rendition, not an error. Private records are listed only for an
    `admin` caller.

    **Scope:** `agent` or `admin`.

    Args:
        body (BriefingInput):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        BriefingRun | Problem
    """

    return (
        await asyncio_detailed(
            client=client,
            body=body,
        )
    ).parsed
