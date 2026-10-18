from http import HTTPStatus
from typing import Any
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.indicator import Indicator
from ...models.problem import Problem
from ...types import Response


def _get_kwargs(
    indicator_id: str,
) -> dict[str, Any]:

    _kwargs: dict[str, Any] = {
        "method": "delete",
        "url": "/v1/indicators/{indicator_id}".format(
            indicator_id=quote(str(indicator_id), safe=""),
        ),
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Indicator | Problem | None:
    if response.status_code == 200:
        response_200 = Indicator.from_dict(response.json())

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

    if client.raise_on_unexpected_status:
        raise errors.UnexpectedStatus(response.status_code, response.content)
    else:
        return None


def _build_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Response[Indicator | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    indicator_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[Indicator | Problem]:
    """Retire an indicator

     Sets the indicator to `retired` and disables its watch rule; its history is kept.

    **Scope:** `agent`.

    Args:
        indicator_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Indicator | Problem]
    """

    kwargs = _get_kwargs(
        indicator_id=indicator_id,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    indicator_id: str,
    *,
    client: AuthenticatedClient,
) -> Indicator | Problem | None:
    """Retire an indicator

     Sets the indicator to `retired` and disables its watch rule; its history is kept.

    **Scope:** `agent`.

    Args:
        indicator_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Indicator | Problem
    """

    return sync_detailed(
        indicator_id=indicator_id,
        client=client,
    ).parsed


async def asyncio_detailed(
    indicator_id: str,
    *,
    client: AuthenticatedClient,
) -> Response[Indicator | Problem]:
    """Retire an indicator

     Sets the indicator to `retired` and disables its watch rule; its history is kept.

    **Scope:** `agent`.

    Args:
        indicator_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Indicator | Problem]
    """

    kwargs = _get_kwargs(
        indicator_id=indicator_id,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    indicator_id: str,
    *,
    client: AuthenticatedClient,
) -> Indicator | Problem | None:
    """Retire an indicator

     Sets the indicator to `retired` and disables its watch rule; its history is kept.

    **Scope:** `agent`.

    Args:
        indicator_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Indicator | Problem
    """

    return (
        await asyncio_detailed(
            indicator_id=indicator_id,
            client=client,
        )
    ).parsed
