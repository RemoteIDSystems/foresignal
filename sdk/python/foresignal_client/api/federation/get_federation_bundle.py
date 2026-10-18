import datetime
from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.compact_message import CompactMessage
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    since: datetime.datetime | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    json_since: str | Unset = UNSET
    if not isinstance(since, Unset):
        json_since = since.isoformat()
    params["since"] = json_since

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/federation/bundle",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | list[CompactMessage] | None:
    if response.status_code == 200:
        response_200 = []
        _response_200 = response.content
        for response_200_item_data in _response_200:
            response_200_item = CompactMessage.from_dict(response_200_item_data)

            response_200.append(response_200_item)

        return response_200

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
) -> Response[Problem | list[CompactMessage]]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    since: datetime.datetime | Unset = UNSET,
) -> Response[Problem | list[CompactMessage]]:
    """Pending compact messages as a bundle

     A CBOR array (definite length; each element a CompactMessage exactly as the 084 profile encodes it)
    of pending CompactMessages (fired and resolved alerts, intelligence events and their tombstones;
    never tracks) for store-and-forward gateways (sneakernet, DTN bundle agents).

    **Scope:** `read` with the `federation` token capability (restricted tokens, 023/035).

    Args:
        since (datetime.datetime | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | list[CompactMessage]]
    """

    kwargs = _get_kwargs(
        since=since,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    since: datetime.datetime | Unset = UNSET,
) -> Problem | list[CompactMessage] | None:
    """Pending compact messages as a bundle

     A CBOR array (definite length; each element a CompactMessage exactly as the 084 profile encodes it)
    of pending CompactMessages (fired and resolved alerts, intelligence events and their tombstones;
    never tracks) for store-and-forward gateways (sneakernet, DTN bundle agents).

    **Scope:** `read` with the `federation` token capability (restricted tokens, 023/035).

    Args:
        since (datetime.datetime | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | list[CompactMessage]
    """

    return sync_detailed(
        client=client,
        since=since,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    since: datetime.datetime | Unset = UNSET,
) -> Response[Problem | list[CompactMessage]]:
    """Pending compact messages as a bundle

     A CBOR array (definite length; each element a CompactMessage exactly as the 084 profile encodes it)
    of pending CompactMessages (fired and resolved alerts, intelligence events and their tombstones;
    never tracks) for store-and-forward gateways (sneakernet, DTN bundle agents).

    **Scope:** `read` with the `federation` token capability (restricted tokens, 023/035).

    Args:
        since (datetime.datetime | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | list[CompactMessage]]
    """

    kwargs = _get_kwargs(
        since=since,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    since: datetime.datetime | Unset = UNSET,
) -> Problem | list[CompactMessage] | None:
    """Pending compact messages as a bundle

     A CBOR array (definite length; each element a CompactMessage exactly as the 084 profile encodes it)
    of pending CompactMessages (fired and resolved alerts, intelligence events and their tombstones;
    never tracks) for store-and-forward gateways (sneakernet, DTN bundle agents).

    **Scope:** `read` with the `federation` token capability (restricted tokens, 023/035).

    Args:
        since (datetime.datetime | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | list[CompactMessage]
    """

    return (
        await asyncio_detailed(
            client=client,
            since=since,
        )
    ).parsed
