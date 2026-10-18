from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.fetch_outcome import FetchOutcome
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    source_id: str,
    *,
    allow_shrink: bool | Unset = False,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["allowShrink"] = allow_shrink

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "post",
        "url": "/v1/sources/{source_id}/fetch".format(
            source_id=quote(str(source_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> FetchOutcome | Problem | None:
    if response.status_code == 200:
        response_200 = FetchOutcome.from_dict(response.json())

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
) -> Response[FetchOutcome | Problem]:
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
    allow_shrink: bool | Unset = False,
) -> Response[FetchOutcome | Problem]:
    """Fetch once now

     Runs one fetch immediately (trigger MANUAL) and ingests normally, even while the source is disabled.
    Consumes budget; the outcome carries per-reason counts and the fetch_log id. Streaming, push and
    derived sources return 409 NOT_SUPPORTED_FOR_MODE (use probe for streams).

    **Scope:** `admin`.

    Args:
        source_id (str):
        allow_shrink (bool | Unset):  Default: False.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[FetchOutcome | Problem]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        allow_shrink=allow_shrink,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    source_id: str,
    *,
    client: AuthenticatedClient,
    allow_shrink: bool | Unset = False,
) -> FetchOutcome | Problem | None:
    """Fetch once now

     Runs one fetch immediately (trigger MANUAL) and ingests normally, even while the source is disabled.
    Consumes budget; the outcome carries per-reason counts and the fetch_log id. Streaming, push and
    derived sources return 409 NOT_SUPPORTED_FOR_MODE (use probe for streams).

    **Scope:** `admin`.

    Args:
        source_id (str):
        allow_shrink (bool | Unset):  Default: False.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        FetchOutcome | Problem
    """

    return sync_detailed(
        source_id=source_id,
        client=client,
        allow_shrink=allow_shrink,
    ).parsed


async def asyncio_detailed(
    source_id: str,
    *,
    client: AuthenticatedClient,
    allow_shrink: bool | Unset = False,
) -> Response[FetchOutcome | Problem]:
    """Fetch once now

     Runs one fetch immediately (trigger MANUAL) and ingests normally, even while the source is disabled.
    Consumes budget; the outcome carries per-reason counts and the fetch_log id. Streaming, push and
    derived sources return 409 NOT_SUPPORTED_FOR_MODE (use probe for streams).

    **Scope:** `admin`.

    Args:
        source_id (str):
        allow_shrink (bool | Unset):  Default: False.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[FetchOutcome | Problem]
    """

    kwargs = _get_kwargs(
        source_id=source_id,
        allow_shrink=allow_shrink,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    source_id: str,
    *,
    client: AuthenticatedClient,
    allow_shrink: bool | Unset = False,
) -> FetchOutcome | Problem | None:
    """Fetch once now

     Runs one fetch immediately (trigger MANUAL) and ingests normally, even while the source is disabled.
    Consumes budget; the outcome carries per-reason counts and the fetch_log id. Streaming, push and
    derived sources return 409 NOT_SUPPORTED_FOR_MODE (use probe for streams).

    **Scope:** `admin`.

    Args:
        source_id (str):
        allow_shrink (bool | Unset):  Default: False.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        FetchOutcome | Problem
    """

    return (
        await asyncio_detailed(
            source_id=source_id,
            client=client,
            allow_shrink=allow_shrink,
        )
    ).parsed
