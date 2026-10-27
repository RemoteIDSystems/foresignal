from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.quota_usage import QuotaUsage
from ...types import Response


def _get_kwargs() -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/me/quota",
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | QuotaUsage | None:
    if response.status_code == 200:
        response_200 = QuotaUsage.from_dict(response.json())

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
) -> Response[Problem | QuotaUsage]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[Problem | QuotaUsage]:
    """Get my quota and usage

     Returns the calling user's effective quota (their own limits over the instance default; null is
    unlimited) and current usage: `alerts`, `channels` and `savedViews` (the trigger rules, notification
    channels and saved views they own), `storageBytes` (their uploaded attachments plus the JSON of the
    workspaces they own) and `apiRequestsThisMinute` (requests in the current clock minute across their
    sessions and own API tokens) (D5/189, R2-026). A session or a user's own API token (R2-025); an API
    token with no user has no quota and gets 401.

    **Scope:** `read`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | QuotaUsage]
    """

    kwargs = _get_kwargs()

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
) -> Problem | QuotaUsage | None:
    """Get my quota and usage

     Returns the calling user's effective quota (their own limits over the instance default; null is
    unlimited) and current usage: `alerts`, `channels` and `savedViews` (the trigger rules, notification
    channels and saved views they own), `storageBytes` (their uploaded attachments plus the JSON of the
    workspaces they own) and `apiRequestsThisMinute` (requests in the current clock minute across their
    sessions and own API tokens) (D5/189, R2-026). A session or a user's own API token (R2-025); an API
    token with no user has no quota and gets 401.

    **Scope:** `read`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | QuotaUsage
    """

    return sync_detailed(
        client=client,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
) -> Response[Problem | QuotaUsage]:
    """Get my quota and usage

     Returns the calling user's effective quota (their own limits over the instance default; null is
    unlimited) and current usage: `alerts`, `channels` and `savedViews` (the trigger rules, notification
    channels and saved views they own), `storageBytes` (their uploaded attachments plus the JSON of the
    workspaces they own) and `apiRequestsThisMinute` (requests in the current clock minute across their
    sessions and own API tokens) (D5/189, R2-026). A session or a user's own API token (R2-025); an API
    token with no user has no quota and gets 401.

    **Scope:** `read`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | QuotaUsage]
    """

    kwargs = _get_kwargs()

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
) -> Problem | QuotaUsage | None:
    """Get my quota and usage

     Returns the calling user's effective quota (their own limits over the instance default; null is
    unlimited) and current usage: `alerts`, `channels` and `savedViews` (the trigger rules, notification
    channels and saved views they own), `storageBytes` (their uploaded attachments plus the JSON of the
    workspaces they own) and `apiRequestsThisMinute` (requests in the current clock minute across their
    sessions and own API tokens) (D5/189, R2-026). A session or a user's own API token (R2-025); an API
    token with no user has no quota and gets 401.

    **Scope:** `read`.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | QuotaUsage
    """

    return (
        await asyncio_detailed(
            client=client,
        )
    ).parsed
