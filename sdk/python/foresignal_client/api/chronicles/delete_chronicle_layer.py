from http import HTTPStatus
from typing import Any, cast
from urllib.parse import quote

import httpx

from ... import errors
from ...client import AuthenticatedClient, Client
from ...models.problem import Problem
from ...types import UNSET, Response, Unset


def _get_kwargs(
    chronicle_id: str,
    layer_id: str,
    *,
    purge: bool | Unset = False,
) -> dict[str, Any]:

    params: dict[str, Any] = {}

    params["purge"] = purge

    params = {k: v for k, v in params.items() if v is not UNSET and v is not None}

    _kwargs: dict[str, Any] = {
        "method": "delete",
        "url": "/v1/chronicles/{chronicle_id}/layers/{layer_id}".format(
            chronicle_id=quote(str(chronicle_id), safe=""),
            layer_id=quote(str(layer_id), safe=""),
        ),
        "params": params,
    }

    return _kwargs


def _parse_response(
    *, client: AuthenticatedClient | Client, response: httpx.Response
) -> Any | Problem | None:
    if response.status_code == 204:
        response_204 = cast(Any, None)
        return response_204

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
) -> Response[Any | Problem]:
    return Response(
        status_code=HTTPStatus(response.status_code),
        content=response.content,
        headers=response.headers,
        parsed=_parse_response(client=client, response=response),
    )


def sync_detailed(
    chronicle_id: str,
    layer_id: str,
    *,
    client: AuthenticatedClient,
    purge: bool | Unset = False,
) -> Response[Any | Problem]:
    """Remove a chronicle layer

     Stops capture for the layer; its captured data stays unless `purge=true`.

    **Scope:** `agent`.

    Args:
        chronicle_id (str):
        layer_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        purge (bool | Unset):  Default: False.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem]
    """

    kwargs = _get_kwargs(
        chronicle_id=chronicle_id,
        layer_id=layer_id,
        purge=purge,
    )

    response = client.get_httpx_client().request(
        **kwargs,
    )

    return _build_response(client=client, response=response)


def sync(
    chronicle_id: str,
    layer_id: str,
    *,
    client: AuthenticatedClient,
    purge: bool | Unset = False,
) -> Any | Problem | None:
    """Remove a chronicle layer

     Stops capture for the layer; its captured data stays unless `purge=true`.

    **Scope:** `agent`.

    Args:
        chronicle_id (str):
        layer_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        purge (bool | Unset):  Default: False.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem
    """

    return sync_detailed(
        chronicle_id=chronicle_id,
        layer_id=layer_id,
        client=client,
        purge=purge,
    ).parsed


async def asyncio_detailed(
    chronicle_id: str,
    layer_id: str,
    *,
    client: AuthenticatedClient,
    purge: bool | Unset = False,
) -> Response[Any | Problem]:
    """Remove a chronicle layer

     Stops capture for the layer; its captured data stays unless `purge=true`.

    **Scope:** `agent`.

    Args:
        chronicle_id (str):
        layer_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        purge (bool | Unset):  Default: False.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Response[Any | Problem]
    """

    kwargs = _get_kwargs(
        chronicle_id=chronicle_id,
        layer_id=layer_id,
        purge=purge,
    )

    response = await client.get_async_httpx_client().request(**kwargs)

    return _build_response(client=client, response=response)


async def asyncio(
    chronicle_id: str,
    layer_id: str,
    *,
    client: AuthenticatedClient,
    purge: bool | Unset = False,
) -> Any | Problem | None:
    """Remove a chronicle layer

     Stops capture for the layer; its captured data stays unless `purge=true`.

    **Scope:** `agent`.

    Args:
        chronicle_id (str):
        layer_id (str): ULID identifier Example: 01JA8Q3V7S2K9W4T6Y1B5N0M3C.
        purge (bool | Unset):  Default: False.

    Raises:
        errors.UnexpectedStatus: If the server returns an undocumented status code and Client.raise_on_unexpected_status is True.
        httpx.TimeoutException: If the request takes longer than Client.timeout.

    Returns:
        Any | Problem
    """

    return (
        await asyncio_detailed(
            chronicle_id=chronicle_id,
            layer_id=layer_id,
            client=client,
            purge=purge,
        )
    ).parsed
