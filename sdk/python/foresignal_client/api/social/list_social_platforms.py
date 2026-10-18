from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.social_platform import SocialPlatform
from ...types import Response


def _get_kwargs() -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/social/platforms",
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | list[SocialPlatform] | None:
    if response.status_code == 200:
        response_200 = []
        _response_200 = response.json()
        for response_200_item_data in _response_200:
            response_200_item = SocialPlatform.from_dict(response_200_item_data)

            response_200.append(response_200_item)

        return response_200

    if response.status_code == 401:
        response_401 = Problem.from_dict(response.json())

        return response_401

    if response.status_code == 429:
        response_429 = Problem.from_dict(response.json())

        return response_429

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Problem | list[SocialPlatform]]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[Problem | list[SocialPlatform]]:
    """List social platform adapters

     Every social platform adapter with its state, mode, collection paths (complete, paged or sampled),
    health, quotas, spend this month and estimates, and a terms and cost summary. All but `bluesky-
    jetstream` ship disabled; the operator enables them with their own credentials and budget.

    **Scope:** `read` (open while `security.openReads` is on).

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | list[SocialPlatform]]
    """

    kwargs = _get_kwargs()

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
) -> Problem | list[SocialPlatform] | None:
    """List social platform adapters

     Every social platform adapter with its state, mode, collection paths (complete, paged or sampled),
    health, quotas, spend this month and estimates, and a terms and cost summary. All but `bluesky-
    jetstream` ship disabled; the operator enables them with their own credentials and budget.

    **Scope:** `read` (open while `security.openReads` is on).

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | list[SocialPlatform]
    """

    return sync_detailed(
        client=client,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[Problem | list[SocialPlatform]]:
    """List social platform adapters

     Every social platform adapter with its state, mode, collection paths (complete, paged or sampled),
    health, quotas, spend this month and estimates, and a terms and cost summary. All but `bluesky-
    jetstream` ship disabled; the operator enables them with their own credentials and budget.

    **Scope:** `read` (open while `security.openReads` is on).

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | list[SocialPlatform]]
    """

    kwargs = _get_kwargs()

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
) -> Problem | list[SocialPlatform] | None:
    """List social platform adapters

     Every social platform adapter with its state, mode, collection paths (complete, paged or sampled),
    health, quotas, spend this month and estimates, and a terms and cost summary. All but `bluesky-
    jetstream` ship disabled; the operator enables them with their own credentials and budget.

    **Scope:** `read` (open while `security.openReads` is on).

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | list[SocialPlatform]
    """

    return (
        await asyncio_detailed(
            client=client,
        )
    ).parsed
