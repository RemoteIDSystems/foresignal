from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.situation import Situation
from ...types import Response


def _get_kwargs(
    situation_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/situations/{situation_id}".format(
            situation_id=quote(str(situation_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | Situation | None:
    if response.status_code == 200:
        response_200 = Situation.from_dict(response.json())

        return response_200

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 404:
        response_404 = Problem.from_dict(response.json())

        return response_404

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | Situation]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    situation_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[Problem | Situation]:
    """Get a situation

     Returns the situation evidence first (members grouped by signal family, corroboration roll-up), then
    inference (status, severity, deterministic summary), then the optional LLM brief labelled as
    speculation.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        situation_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | Situation]
    """

    kwargs = _get_kwargs(
        situation_id=situation_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    situation_id: str,
    *,
    client: AuthenticatedClient,
) -> Problem | Situation | None:
    """Get a situation

     Returns the situation evidence first (members grouped by signal family, corroboration roll-up), then
    inference (status, severity, deterministic summary), then the optional LLM brief labelled as
    speculation.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        situation_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | Situation
    """

    return sync_detailed(
        situation_id=situation_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    situation_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[Problem | Situation]:
    """Get a situation

     Returns the situation evidence first (members grouped by signal family, corroboration roll-up), then
    inference (status, severity, deterministic summary), then the optional LLM brief labelled as
    speculation.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        situation_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | Situation]
    """

    kwargs = _get_kwargs(
        situation_id=situation_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    situation_id: str,
    *,
    client: AuthenticatedClient,
) -> Problem | Situation | None:
    """Get a situation

     Returns the situation evidence first (members grouped by signal family, corroboration roll-up), then
    inference (status, severity, deterministic summary), then the optional LLM brief labelled as
    speculation.

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        situation_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | Situation
    """

    return (
        await asyncio_detailed(
            situation_id=situation_id,
            client=client,
        )
    ).parsed
