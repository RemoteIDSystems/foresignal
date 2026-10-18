from http import HTTPStatus
from typing import Any

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...models.trigger_rule import TriggerRule
from ...types import UNSET, Response, Unset


def _get_kwargs(
    *,
    tag: str | Unset = UNSET,
    enabled: bool | Unset = UNSET,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["tag"] = tag

    params["enabled"] = enabled

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "get",
        "url": "/v1/triggers",
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Problem | list[TriggerRule] | None:
    if response.status_code == 200:
        response_200 = []
        _response_200 = response.json()
        for response_200_item_data in _response_200:
            response_200_item = TriggerRule.from_dict(response_200_item_data)

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
) -> Response[Problem | list[TriggerRule]]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    *,
    client: AuthenticatedClient,
    tag: str | Unset = UNSET,
    enabled: bool | Unset = UNSET,
) -> Response[Problem | list[TriggerRule]]:
    """List trigger rules

     List trigger rules

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        tag (str | Unset):
        enabled (bool | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | list[TriggerRule]]
    """

    kwargs = _get_kwargs(
        tag=tag,
        enabled=enabled,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    *,
    client: AuthenticatedClient,
    tag: str | Unset = UNSET,
    enabled: bool | Unset = UNSET,
) -> Problem | list[TriggerRule] | None:
    """List trigger rules

     List trigger rules

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        tag (str | Unset):
        enabled (bool | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | list[TriggerRule]
    """

    return sync_detailed(
        client=client,
        tag=tag,
        enabled=enabled,
    ).parsed


async def asyncio_detailed(
    *,
    client: AuthenticatedClient,
    tag: str | Unset = UNSET,
    enabled: bool | Unset = UNSET,
) -> Response[Problem | list[TriggerRule]]:
    """List trigger rules

     List trigger rules

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        tag (str | Unset):
        enabled (bool | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Problem | list[TriggerRule]]
    """

    kwargs = _get_kwargs(
        tag=tag,
        enabled=enabled,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    *,
    client: AuthenticatedClient,
    tag: str | Unset = UNSET,
    enabled: bool | Unset = UNSET,
) -> Problem | list[TriggerRule] | None:
    """List trigger rules

     List trigger rules

    **Scope:** `read` (open while `security.openReads` is on).

    Args:
        tag (str | Unset):
        enabled (bool | Unset):

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Problem | list[TriggerRule]
    """

    return (
        await asyncio_detailed(
            client=client,
            tag=tag,
            enabled=enabled,
        )
    ).parsed
