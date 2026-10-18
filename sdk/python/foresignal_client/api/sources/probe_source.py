from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.probe_result import ProbeResult
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    source_id: str,
    *,
    max_bytes: int | Unset = 262144,
    seconds: int | Unset = 10,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["maxBytes"] = max_bytes

    params["seconds"] = seconds

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/sources/{source_id}/probe".format(
            source_id=quote(str(source_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> ProbeResult | Problem | None:
    if response.status_code == 200:
        response_200 = ProbeResult.from_dict(response.json())

        return response_200

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 403:
        response_403 = Problem.from_dict(response.json())

        return response_403

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 409:
        response_409 = Problem.from_dict(response.json())

        return response_409

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[ProbeResult | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    source_id: str,
    *,
    client: AuthenticatedClient,
    max_bytes: int | Unset = 262144,
    seconds: int | Unset = 10,
) -> Response[ProbeResult | Problem]:
    """Diagnostic probe

     Fetches once (or captures a stream for `seconds`) and returns the recorded exchanges, credentials
    redacted, plus up to 100 parsed previews with eventTime, validFrom, quality and observers. Never
    ingests, never writes fetch_log, never changes health; consumes budget. A parse failure fills
    `parseError` and still returns the exchanges. Push and derived sources return 409
    NOT_SUPPORTED_FOR_MODE.

    **Scope:** `admin`.

    Args:
        source_id (str):
        max_bytes (int | Unset):  Default: 262144.
        seconds (int | Unset):  Default: 10.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ProbeResult | Problem]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        max_bytes=max_bytes,
        seconds=seconds,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    source_id: str,
    *,
    client: AuthenticatedClient,
    max_bytes: int | Unset = 262144,
    seconds: int | Unset = 10,
) -> ProbeResult | Problem | None:
    """Diagnostic probe

     Fetches once (or captures a stream for `seconds`) and returns the recorded exchanges, credentials
    redacted, plus up to 100 parsed previews with eventTime, validFrom, quality and observers. Never
    ingests, never writes fetch_log, never changes health; consumes budget. A parse failure fills
    `parseError` and still returns the exchanges. Push and derived sources return 409
    NOT_SUPPORTED_FOR_MODE.

    **Scope:** `admin`.

    Args:
        source_id (str):
        max_bytes (int | Unset):  Default: 262144.
        seconds (int | Unset):  Default: 10.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ProbeResult | Problem
    """

    return sync_detailed(
        source_id=source_id,
        client=client,
        max_bytes=max_bytes,
        seconds=seconds,
    ).parsed


async def asyncio_detailed(
    source_id: str,
    *,
    client: AuthenticatedClient,
    max_bytes: int | Unset = 262144,
    seconds: int | Unset = 10,
) -> Response[ProbeResult | Problem]:
    """Diagnostic probe

     Fetches once (or captures a stream for `seconds`) and returns the recorded exchanges, credentials
    redacted, plus up to 100 parsed previews with eventTime, validFrom, quality and observers. Never
    ingests, never writes fetch_log, never changes health; consumes budget. A parse failure fills
    `parseError` and still returns the exchanges. Push and derived sources return 409
    NOT_SUPPORTED_FOR_MODE.

    **Scope:** `admin`.

    Args:
        source_id (str):
        max_bytes (int | Unset):  Default: 262144.
        seconds (int | Unset):  Default: 10.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[ProbeResult | Problem]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        max_bytes=max_bytes,
        seconds=seconds,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    source_id: str,
    *,
    client: AuthenticatedClient,
    max_bytes: int | Unset = 262144,
    seconds: int | Unset = 10,
) -> ProbeResult | Problem | None:
    """Diagnostic probe

     Fetches once (or captures a stream for `seconds`) and returns the recorded exchanges, credentials
    redacted, plus up to 100 parsed previews with eventTime, validFrom, quality and observers. Never
    ingests, never writes fetch_log, never changes health; consumes budget. A parse failure fills
    `parseError` and still returns the exchanges. Push and derived sources return 409
    NOT_SUPPORTED_FOR_MODE.

    **Scope:** `admin`.

    Args:
        source_id (str):
        max_bytes (int | Unset):  Default: 262144.
        seconds (int | Unset):  Default: 10.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        ProbeResult | Problem
    """

    return (
        await asyncio_detailed(
            source_id=source_id,
            client=client,
            max_bytes=max_bytes,
            seconds=seconds,
        )
    ).parsed
